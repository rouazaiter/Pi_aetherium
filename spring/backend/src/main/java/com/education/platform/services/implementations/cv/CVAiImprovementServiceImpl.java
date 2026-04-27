package com.education.platform.services.implementations.cv;

import com.education.platform.common.ApiException;
import com.education.platform.dto.cv.CvAiImproveRequest;
import com.education.platform.dto.cv.CvAiImproveResponse;
import com.education.platform.entities.User;
import com.education.platform.entities.admin.jihenportfolio.AiFeature;
import com.education.platform.services.interfaces.admin.jihenportfolio.JihenPortfolioAdminTrackingService;
import com.education.platform.services.interfaces.cv.CVAiImprovementService;
import com.education.platform.services.interfaces.cv.OllamaClient;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
@Service
public class CVAiImprovementServiceImpl implements CVAiImprovementService {

    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper;
    private final JihenPortfolioAdminTrackingService trackingService;

    public CVAiImprovementServiceImpl(
            OllamaClient ollamaClient,
            ObjectMapper objectMapper,
            JihenPortfolioAdminTrackingService trackingService) {
        this.ollamaClient = ollamaClient;
        this.objectMapper = objectMapper;
        this.trackingService = trackingService;
    }

    @Override
    public CvAiImproveResponse improveForUser(User user, CvAiImproveRequest request) {
        if (request == null || request.getText() == null || request.getText().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Text is required");
        }

        String prompt = buildPrompt(request);
        long startedAt = System.nanoTime();
        try {
            String rawSuggestion = ollamaClient.generate(prompt);
            String cleanedSuggestion = cleanup(rawSuggestion);
            if (cleanedSuggestion.isBlank()) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "AI suggestion was empty");
            }
            trackingService.recordAiUsage(AiFeature.CV_IMPROVE, user == null ? null : user.getId(), true, elapsedMillis(startedAt), null);
            return new CvAiImproveResponse(cleanedSuggestion);
        } catch (RuntimeException e) {
            trackingService.recordAiUsage(AiFeature.CV_IMPROVE, user == null ? null : user.getId(), false, elapsedMillis(startedAt), e.getMessage());
            throw e;
        }
    }

    private long elapsedMillis(long startedAt) {
        return Math.max(0L, (System.nanoTime() - startedAt) / 1_000_000L);
    }

    private String buildPrompt(CvAiImproveRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You improve exactly one CV field.\n");
        prompt.append("TOPIC: ").append(request.getTopic()).append('\n');
        prompt.append("SECTION_TYPE: ").append(request.getSectionType()).append('\n');
        prompt.append("FIELD: ").append(normalizeOptionalValue(request.getField())).append('\n');
        prompt.append("TARGET_TONE: ").append(request.getTargetTone()).append('\n');
        prompt.append("MAX_LENGTH: ").append(request.getMaxLength()).append('\n');
        prompt.append("TEXT TO IMPROVE:\n");
        prompt.append(request.getText().trim()).append("\n\n");
        prompt.append("CONTEXT JSON:\n");
        prompt.append(serializeContext(request.getContext())).append("\n\n");
        prompt.append("RULES:\n");
        prompt.append("- Improve ONLY the provided text.\n");
        prompt.append("- Use context only to keep coherence.\n");
        prompt.append("- Do NOT invent any facts.\n");
        prompt.append("- Do NOT add fake companies, metrics, dates, or tools.\n");
        prompt.append("- Do NOT rewrite other sections.\n");
        prompt.append("- Do NOT output explanations or thinking.\n");
        prompt.append("- Do NOT output multiple options.\n");
        prompt.append("- Do NOT output markdown.\n");
        prompt.append("- Return ONE clean ATS-friendly sentence or paragraph only.\n");
        prompt.append("- Return plain text only.\n");
        return prompt.toString();
    }

    private String serializeContext(CvAiImproveRequest.Context context) {
        if (context == null) {
            return "{}";
        }

        JsonNode node = objectMapper.valueToTree(context);
        pruneEmpty(node);
        if (node.isObject() && node.isEmpty()) {
            return "{}";
        }

        try {
            return objectMapper.copy()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to prepare AI prompt");
        }
    }

    private boolean pruneEmpty(JsonNode node) {
        if (node == null || node.isNull()) {
            return true;
        }
        if (node.isTextual()) {
            return node.asText().isBlank();
        }
        if (node.isArray()) {
            Iterator<JsonNode> iterator = node.iterator();
            while (iterator.hasNext()) {
                JsonNode child = iterator.next();
                if (pruneEmpty(child)) {
                    iterator.remove();
                }
            }
            return node.isEmpty();
        }
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                if (pruneEmpty(entry.getValue())) {
                    fields.remove();
                }
            }
            return node.isEmpty();
        }
        return false;
    }

    private String cleanup(String rawSuggestion) {
        return CvAiTextSanitizer.sanitize(rawSuggestion).replace('\n', ' ').replaceAll("\\s+", " ").trim();
    }

    private String normalizeOptionalValue(String value) {
        return value == null || value.isBlank() ? "unspecified" : value.trim();
    }
}
