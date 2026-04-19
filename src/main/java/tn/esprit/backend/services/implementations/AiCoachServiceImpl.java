package tn.esprit.backend.services.implementations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.backend.dto.AiCoachPreviewRequest;
import tn.esprit.backend.dto.AiCoachPreviewResponse;
import tn.esprit.backend.entities.Application;
import tn.esprit.backend.entities.ServiceRequest;
import tn.esprit.backend.repositories.ServiceRequestRepository;
import tn.esprit.backend.services.interfaces.AiCoachService;
import tn.esprit.backend.services.interfaces.ApplicationService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiCoachServiceImpl implements AiCoachService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ApplicationService applicationService;
    private final ObjectMapper objectMapper;

    @Value("${ai.coach.enabled:true}")
    private boolean aiEnabled;

    @Value("${ai.coach.base-url:https://api.openai.com/v1/chat/completions}")
    private String aiBaseUrl;

    @Value("${ai.coach.model:gpt-4o-mini}")
    private String aiModel;

    @Value("${ai.coach.api-key:}")
    private String aiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional(readOnly = true)
    public AiCoachPreviewResponse preview(AiCoachPreviewRequest request) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(request.serviceRequestId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ServiceRequest not found: " + request.serviceRequestId()));

        String originalText = request.originalText() == null ? "" : request.originalText().trim();
        if (originalText.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "originalText cannot be empty");
        }

        if (!aiEnabled || aiApiKey == null || aiApiKey.isBlank()) {
            return localFallbackPreview(originalText, serviceRequest, request.language());
        }

        try {
            return previewFromProvider(originalText, serviceRequest, request.tone(), request.language());
        } catch (Exception ex) {
            return localFallbackPreview(originalText, serviceRequest, request.language());
        }
    }

    @Override
    @Transactional
    public Application applyImprovedText(Long applicationId, Long applicantId, String improvedText) {
        return applicationService.updateApplicationMessage(applicationId, applicantId, improvedText);
    }

    private AiCoachPreviewResponse previewFromProvider(String originalText, ServiceRequest request, String tone, String language) {
        String safeTone = normalizeTone(tone);
        String safeLanguage = normalizeLanguage(language);

        String systemPrompt = """
                Tu es un coach de candidature. Tu dois ameliorer le texte du candidat pour une offre donnee.
                Regles strictes:
                - Ne jamais inventer une experience, un diplome, une certification, ni une competence non prouvee.
                - Conserver les faits et le sens d'origine.
                - Rendre le texte plus clair, professionnel et pertinent par rapport a l'offre.
                - Si une information manque, proposer une suggestion dans missingPoints, sans inventer.
                - Repondre strictement en JSON valide, sans markdown, sans texte hors JSON.

                Schema JSON attendu:
                {
                  "improvedText": "string",
                  "relevanceScore": 0,
                  "clarityScore": 0,
                  "missingPoints": ["string"],
                  "suggestions": ["string"],
                  "changesSummary": "string"
                }
                """;

        String userPrompt = """
                Contexte offre:
                - Titre: %s
                - Description: %s
                - Categorie: %s

                Parametres:
                - Ton: %s
                - Langue: %s

                Texte candidat:
                %s
                """.formatted(
                safeText(request.getName()),
                safeText(request.getDescription()),
                request.getCategory() == null ? "N/A" : request.getCategory().name(),
                safeTone,
                safeLanguage,
                originalText
        );

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", aiModel);
        payload.put("temperature", 0.3);

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );
        payload.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(aiBaseUrl, HttpMethod.POST, entity, String.class);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI provider error", ex);
        }

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI provider returned invalid response");
        }

        return parseProviderResponse(response.getBody(), originalText, request);
    }

    private AiCoachPreviewResponse parseProviderResponse(String body, String originalText, ServiceRequest request) {
        try {
            JsonNode root = objectMapper.readTree(body);
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            if (content.isBlank()) {
                throw new IllegalArgumentException("Empty AI content");
            }

            String json = extractJsonObject(content);
            JsonNode n = objectMapper.readTree(json);

            String improvedText = n.path("improvedText").asText(originalText).trim();
            if (improvedText.isBlank()) {
                improvedText = originalText;
            }

            int relevanceScore = clampScore(n.path("relevanceScore").asInt(localRelevanceScore(originalText, request)));
            int clarityScore = clampScore(n.path("clarityScore").asInt(localClarityScore(improvedText)));

            List<String> missingPoints = readStringList(n.path("missingPoints"));
            List<String> suggestions = readStringList(n.path("suggestions"));
            String changesSummary = n.path("changesSummary").asText("Texte reformule pour ameliorer clarte et pertinence.");

            return new AiCoachPreviewResponse(
                    improvedText,
                    relevanceScore,
                    clarityScore,
                    missingPoints,
                    suggestions,
                    changesSummary,
                    true
            );
        } catch (Exception ex) {
            return localFallbackPreview(originalText, request, "fr");
        }
    }

    private AiCoachPreviewResponse localFallbackPreview(String originalText, ServiceRequest request, String language) {
        String improved = improveTextLightly(originalText);
        int relevance = localRelevanceScore(improved, request);
        int clarity = localClarityScore(improved);

        List<String> missingPoints = buildMissingPoints(improved, request);
        List<String> suggestions = new ArrayList<>();
        suggestions.add(language != null && language.equalsIgnoreCase("en")
                ? "Add one measurable outcome (number, percentage, time saved)."
                : "Ajoute un resultat mesurable (nombre, pourcentage, temps gagne).");
        suggestions.add(language != null && language.equalsIgnoreCase("en")
                ? "Mention one concrete skill used in a real project."
                : "Mentionne une competence appliquee dans un projet reel.");

        return new AiCoachPreviewResponse(
                improved,
                relevance,
                clarity,
                missingPoints,
                suggestions,
                language != null && language.equalsIgnoreCase("en")
                        ? "Text improved locally for clarity and better alignment with the offer."
                        : "Texte ameliore localement pour plus de clarte et un meilleur alignement avec l'offre.",
                false
        );
    }

    private List<String> buildMissingPoints(String text, ServiceRequest request) {
        List<String> result = new ArrayList<>();
        String lower = text.toLowerCase(Locale.ROOT);
        String offer = ((request.getName() == null ? "" : request.getName()) + " " +
                (request.getDescription() == null ? "" : request.getDescription())).toLowerCase(Locale.ROOT);

        Set<String> keywords = Arrays.stream(offer.split("[^a-zA-Z0-9]+"))
                .filter(w -> w.length() >= 5)
                .limit(20)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        long missingCount = keywords.stream().filter(k -> !lower.contains(k)).count();
        if (missingCount >= 5) {
            result.add("Ton texte reprend peu les mots-cles de l'offre. Ajoute les competences les plus attendues.");
        }
        if (!lower.matches(".*\\d+.*")) {
            result.add("Ajoute au moins un resultat chiffre pour renforcer l'impact.");
        }
        if (result.isEmpty()) {
            result.add("Ton texte est globalement coherent avec l'offre. Tu peux ajouter un exemple concret de realisation.");
        }
        return result;
    }

    private int localRelevanceScore(String text, ServiceRequest request) {
        String offer = (safeText(request.getName()) + " " + safeText(request.getDescription())).toLowerCase(Locale.ROOT);
        String candidate = safeText(text).toLowerCase(Locale.ROOT);

        Set<String> offerWords = Arrays.stream(offer.split("[^a-zA-Z0-9]+"))
                .filter(w -> w.length() >= 4)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (offerWords.isEmpty()) {
            return 60;
        }

        long matches = offerWords.stream().filter(candidate::contains).count();
        double ratio = (double) matches / offerWords.size();
        return clampScore((int) Math.round(35 + ratio * 65));
    }

    private int localClarityScore(String text) {
        String normalized = safeText(text).replaceAll("\\s+", " ").trim();
        if (normalized.isBlank()) {
            return 0;
        }

        int lengthScore;
        int len = normalized.length();
        if (len < 80) {
            lengthScore = 45;
        } else if (len <= 800) {
            lengthScore = 80;
        } else {
            lengthScore = 65;
        }

        int punctuationBonus = normalized.matches(".*[.!?].*") ? 10 : 0;
        int structureBonus = normalized.contains(",") ? 5 : 0;

        return clampScore(lengthScore + punctuationBonus + structureBonus);
    }

    private String improveTextLightly(String text) {
        String cleaned = safeText(text).replaceAll("\\s+", " ").trim();
        if (cleaned.isBlank()) {
            return cleaned;
        }
        String sentence = Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
        if (!sentence.endsWith(".") && !sentence.endsWith("!") && !sentence.endsWith("?")) {
            sentence = sentence + ".";
        }
        return sentence;
    }

    private String extractJsonObject(String content) {
        String trimmed = content.trim();
        int first = trimmed.indexOf('{');
        int last = trimmed.lastIndexOf('}');
        if (first >= 0 && last > first) {
            return trimmed.substring(first, last + 1);
        }
        return trimmed;
    }

    private List<String> readStringList(JsonNode node) {
        if (node == null || node.isMissingNode() || !node.isArray()) {
            return Collections.emptyList();
        }
        List<String> values = objectMapper.convertValue(node, new TypeReference<List<String>>() {});
        return values == null ? Collections.emptyList() : values.stream().filter(Objects::nonNull).toList();
    }

    private String normalizeTone(String tone) {
        if (tone == null || tone.isBlank()) {
            return "professionnel";
        }
        return tone.trim();
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) {
            return "fr";
        }
        return language.trim().toLowerCase(Locale.ROOT);
    }

    private int clampScore(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
