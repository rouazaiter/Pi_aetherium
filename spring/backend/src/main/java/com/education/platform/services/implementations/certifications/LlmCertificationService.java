package com.education.platform.services.implementations.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.education.platform.entities.certifications.Certification;
import com.education.platform.entities.certifications.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LlmCertificationService {

    @Value("${openai.api.key:}")
    private String openAiKey;

    @Value("${openai.api.url:https://openrouter.ai/api/v1/chat/completions}")
    private String openAiUrl;

    @Value("${openai.model:openai/gpt-4o-mini}")
    private String model;

    /** Max questions per single LLM call — keeps output within token limits */
    private static final int BATCH_SIZE = 20;

    private final CertificationService certificationService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ImageGenerationService imageGenerationService;

    // ─── PUBLIC ENTRY POINT ──────────────────────────────────────────────────

    public CertificationDTO generateAndSave(LlmGenerateRequest req) {
        int total = req.numberOfQuestions();
        List<QuestionCreateDTO> allQuestions = new ArrayList<>();
        int batches = (int) Math.ceil((double) total / BATCH_SIZE);
        int orderOffset = 0;

        String firstRaw = callOpenAi(buildMetaAndQuestionsPrompt(req, Math.min(BATCH_SIZE, total), 1, orderOffset));
        JsonNode firstRoot = parseJson(firstRaw);
        allQuestions.addAll(extractQuestions(firstRoot, orderOffset));
        orderOffset += allQuestions.size();

        for (int b = 1; b < batches; b++) {
            int batchCount = Math.min(BATCH_SIZE, total - orderOffset);
            if (batchCount <= 0) break;
            String raw = callOpenAi(buildQuestionsOnlyPrompt(req, batchCount, b + 1, batches, orderOffset));
            JsonNode root = parseJson(raw);
            allQuestions.addAll(extractQuestions(root, orderOffset));
            orderOffset = allQuestions.size();
        }

        // Auto-generate cover image
        String coverImageUrl = null;
        try {
            coverImageUrl = imageGenerationService.generateCoverImageUrl(
                    req.topic(),
                    firstRoot.path("category").asText("Technology"),
                    req.difficulty().name()
            );
        } catch (Exception e) {
            System.err.println("[LLM] Image generation failed: " + e.getMessage());
        }

        CertificationCreateDTO dto = buildCertDTO(firstRoot, req, allQuestions, coverImageUrl);
        return certificationService.create(dto);
    }

    // ─── PROMPT: BATCH 1 (metadata + questions) ──────────────────────────────

    private String buildMetaAndQuestionsPrompt(LlmGenerateRequest req, int count, int batchNum, int startIndex) {
        Distribution d = new Distribution(count);
        return """
                You are a professional certification exam designer.
                Generate certification metadata AND exactly %d exam questions.

                Topic: %s | Difficulty: %s | Description: %s
                Batch: %d of %d | Questions %d–%d (0-indexed orderIndex starting at %d)

                QUESTION DISTRIBUTION for this batch:
                MCQ=%d, MULTI_SELECT=%d, SCENARIO=%d, CODE=%d, ORDERING+DRAG_DROP=%d

                DIFFICULTY GUIDELINES for %s:
                - BEGINNER: basic definitions, simple recall
                - INTERMEDIATE: applied concepts, moderate reasoning
                - ADVANCED: deep analysis, edge cases, complex scenarios, tricky distractors

                Return ONLY valid JSON, no markdown:
                {
                  "title":"...", "description":"...", "category":"...", "examTitle":"...",
                  "questions":[
                    {"type":"MCQ","questionText":"...","options":["A. ...","B. ...","C. ...","D. ..."],"expectedAnswer":"B","points":2,"orderIndex":%d},
                    {"type":"MULTI_SELECT","questionText":"...","options":["A. ...","B. ...","C. ...","D. ..."],"expectedAnswer":"A,C","points":3,"orderIndex":%d},
                    {"type":"SCENARIO","questionText":"A company needs to... What is the BEST approach?","options":["A. ...","B. ...","C. ...","D. ..."],"expectedAnswer":"C","points":3,"orderIndex":%d},
                    {"type":"CODE","questionText":"What is the output?\\n```%s\\n// code here\\n```","options":["A. ...","B. ...","C. ...","D. ..."],"expectedAnswer":"A","codeLanguage":"%s","points":3,"orderIndex":%d},
                    {"type":"ORDERING","questionText":"Arrange these steps:","options":["Step A","Step B","Step C","Step D"],"expectedAnswer":"2,0,3,1","points":3,"orderIndex":%d},
                    {"type":"DRAG_DROP","questionText":"Match each concept:","matchPairs":[{"left":"...","right":"..."},{"left":"...","right":"..."},{"left":"...","right":"..."},{"left":"...","right":"..."}],"points":3,"orderIndex":%d}
                  ]
                }

                STRICT RULES:
                1. Generate EXACTLY %d questions — no more, no less
                2. Every MCQ/MULTI_SELECT/SCENARIO/CODE/ORDERING MUST have "options" array with 4 items
                3. MCQ/SCENARIO/CODE expectedAnswer = single letter (A/B/C/D), shuffle correct position
                4. MULTI_SELECT expectedAnswer = comma-separated letters e.g. "A,C"
                5. ORDERING expectedAnswer = comma-separated 0-based indices of correct order
                6. DRAG_DROP uses matchPairs (4 pairs), no options
                7. For ADVANCED difficulty: use tricky distractors, edge cases, non-obvious answers
                8. CODE questions must embed actual runnable code in questionText using markdown fences
                9. SCENARIO questions must describe a realistic technical/business situation
                10. orderIndex values must start at %d and increment by 1
                """.formatted(
                count,
                req.topic(), req.difficulty(), req.description(),
                batchNum, (int) Math.ceil((double) req.numberOfQuestions() / BATCH_SIZE),
                startIndex + 1, startIndex + count, startIndex,
                d.mcq, d.multiSelect, d.scenario, d.code, d.interactive,
                req.difficulty(),
                startIndex, startIndex + 1, startIndex + 2,
                req.topic().toLowerCase(), req.topic().toLowerCase(),
                startIndex + 3, startIndex + 4, startIndex + 5,
                count, startIndex
        );
    }

    // ─── PROMPT: SUBSEQUENT BATCHES (questions only) ─────────────────────────

    private String buildQuestionsOnlyPrompt(LlmGenerateRequest req, int count, int batchNum, int totalBatches, int startIndex) {
        Distribution d = new Distribution(count);
        return """
                Continue generating exam questions for the certification below.
                DO NOT repeat any questions from previous batches.

                Topic: %s | Difficulty: %s
                Batch: %d of %d | Questions %d–%d (orderIndex %d to %d)

                QUESTION DISTRIBUTION for this batch:
                MCQ=%d, MULTI_SELECT=%d, SCENARIO=%d, CODE=%d, ORDERING+DRAG_DROP=%d

                Return ONLY valid JSON with a "questions" array — no other fields:
                {"questions":[...]}

                STRICT RULES:
                1. Generate EXACTLY %d questions
                2. Every MCQ/MULTI_SELECT/SCENARIO/CODE/ORDERING MUST have "options" array (4 items)
                3. MCQ/SCENARIO/CODE expectedAnswer = single letter, shuffle correct position
                4. MULTI_SELECT expectedAnswer = comma-separated letters
                5. ORDERING expectedAnswer = comma-separated 0-based indices
                6. DRAG_DROP uses matchPairs (4 pairs), no options
                7. For ADVANCED: tricky distractors, edge cases, non-obvious answers
                8. CODE questions must embed actual code in questionText
                9. orderIndex starts at %d and increments by 1
                10. All questions must be DIFFERENT from previous batches — cover new subtopics
                """.formatted(
                req.topic(), req.difficulty(),
                batchNum, totalBatches,
                startIndex + 1, startIndex + count, startIndex, startIndex + count - 1,
                d.mcq, d.multiSelect, d.scenario, d.code, d.interactive,
                count, startIndex
        );
    }

    // ─── OPENAI CALL ─────────────────────────────────────────────────────────

    private String callOpenAi(String prompt) {
        if (openAiKey == null || openAiKey.isBlank()) {
            throw new RuntimeException("OpenAI API key not configured");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiKey);
        headers.set("HTTP-Referer", "http://localhost:4200");
        headers.set("X-Title", "SkillHub");

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("temperature", 0.8);
        body.put("max_tokens", 4096);   // explicit limit per batch call
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(openAiUrl, entity, String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.path("choices");
            if (choices.isMissingNode() || choices.isEmpty()) {
                throw new RuntimeException("LLM returned no choices. Response: " + response.getBody());
            }

            // Log finish_reason to detect truncation
            String finishReason = choices.get(0).path("finish_reason").asText("unknown");
            if ("length".equals(finishReason)) {
                System.err.println("[LLM WARNING] Response was truncated (finish_reason=length). " +
                        "Consider reducing BATCH_SIZE or question complexity.");
            }

            return choices.get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAI response: " + e.getMessage());
        }
    }

    // ─── JSON HELPERS ─────────────────────────────────────────────────────────

    private JsonNode parseJson(String rawJson) {
        try {
            String cleaned = rawJson.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("```[a-zA-Z]*\\n?", "").replace("```", "").trim();
            }
            // Handle truncated JSON — try to recover by closing open arrays/objects
            return objectMapper.readTree(cleaned);
        } catch (Exception e) {
            // Attempt recovery: find last complete question and close the JSON
            try {
                String cleaned = rawJson.trim();
                if (cleaned.startsWith("```")) {
                    cleaned = cleaned.replaceAll("```[a-zA-Z]*\\n?", "").replace("```", "").trim();
                }
                int lastBrace = cleaned.lastIndexOf("},");
                if (lastBrace > 0) {
                    cleaned = cleaned.substring(0, lastBrace + 1) + "]}";
                    // If it's a full cert JSON, close properly
                    if (!cleaned.contains("\"questions\"")) {
                        cleaned = "{\"questions\":" + cleaned + "}";
                    }
                    return objectMapper.readTree(cleaned);
                }
            } catch (Exception ignored) {}
            throw new RuntimeException("Failed to parse LLM JSON: " + e.getMessage());
        }
    }

    private List<QuestionCreateDTO> extractQuestions(JsonNode root, int orderOffset) {
        List<QuestionCreateDTO> questions = new ArrayList<>();
        JsonNode questionsNode = root.has("questions") ? root.path("questions") : root;

        for (JsonNode qNode : questionsNode) {
            String typeStr = qNode.path("type").asText("MCQ");
            Question.QuestionType type;
            try {
                type = Question.QuestionType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                type = Question.QuestionType.MCQ;
            }

            List<String> options = null;
            if (qNode.has("options") && qNode.path("options").isArray()) {
                options = new ArrayList<>();
                for (JsonNode opt : qNode.path("options")) options.add(opt.asText());
            }

            List<MatchPairDTO> pairs = null;
            if (qNode.has("matchPairs") && qNode.path("matchPairs").isArray()) {
                pairs = new ArrayList<>();
                for (JsonNode pair : qNode.path("matchPairs")) {
                    String left  = pair.has("left")  ? pair.path("left").asText()  : pair.path("key").asText();
                    String right = pair.has("right") ? pair.path("right").asText() : pair.path("value").asText();
                    pairs.add(new MatchPairDTO(left, right));
                }
            }

            int orderIndex = qNode.path("orderIndex").asInt(orderOffset + questions.size());

            questions.add(new QuestionCreateDTO(
                    type,
                    qNode.path("questionText").asText(),
                    qNode.path("points").asDouble(2.0),
                    orderIndex,
                    qNode.path("expectedAnswer").asText(null),
                    qNode.path("codeLanguage").asText(null),
                    pairs,
                    options
            ));
        }
        return questions;
    }

    private CertificationCreateDTO buildCertDTO(JsonNode firstRoot, LlmGenerateRequest req,
                                                 List<QuestionCreateDTO> allQuestions,
                                                 String coverImageUrl) {
        ExamCreateDTO exam = new ExamCreateDTO(
                firstRoot.path("examTitle").asText(req.topic() + " Exam"),
                req.timeLimitMinutes(),
                70.0,
                allQuestions
        );

        return new CertificationCreateDTO(
                firstRoot.path("title").asText(req.topic() + " Certification"),
                firstRoot.path("description").asText(req.description()),
                firstRoot.path("category").asText("General"),
                req.difficulty(),
                Certification.Status.DRAFT,
                BigDecimal.ZERO,
                null, null,
                req.timeLimitMinutes(),
                70.0,
                List.of(exam),
                coverImageUrl
        );
    }

    // ─── DISTRIBUTION HELPER ─────────────────────────────────────────────────

    private static class Distribution {
        final int mcq, multiSelect, scenario, code, interactive;

        Distribution(int n) {
            mcq        = (int) Math.round(n * 0.40);
            multiSelect= (int) Math.round(n * 0.05);
            scenario   = (int) Math.round(n * 0.20);
            code       = (int) Math.round(n * 0.20);
            interactive= Math.max(0, n - mcq - multiSelect - scenario - code);
        }
    }
}
