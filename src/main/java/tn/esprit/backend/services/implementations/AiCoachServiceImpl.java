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

    @Value("${ai.coach.model:gpt-4.1-mini}")
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
                Tu es un coach de candidature expert.
                Tu dois transformer un texte brut en un message de candidature plus convaincant, plus clair, plus professionnel et plus adapte a l'offre.
                Regles strictes:
                - Ne jamais inventer une experience, un diplome, une certification, ni une competence non prouvee.
                - Conserver les faits et le sens d'origine.
                - Produire une reformulation plus naturelle, plus fluide et plus mature que la phrase source.
                - Si le texte est tres court, l'enrichir avec des formulations professionnelles, sans ajouter de fausses informations.
                - Favoriser 2 a 4 phrases courtes et percutantes plutot qu'une seule phrase brute.
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
        payload.put("temperature", 0.45);

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
        String improved = improveTextLightly(originalText, request, language);
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

    private String improveTextLightly(String text, ServiceRequest request, String language) {
        String cleaned = safeText(text).replaceAll("\\s+", " ").trim();
        if (cleaned.isBlank()) {
            return cleaned;
        }

        String normalized = normalizeSpelling(cleaned);
        String lowered = normalized.toLowerCase(Locale.ROOT);

        String offerName = safeText(request.getName()).trim();
        String roleFragment = offerName.isBlank() ? "the opportunity" : offerName;

        if (language != null && language.equalsIgnoreCase("en")) {
            if (lowered.contains("i can help you") || lowered.contains("i can help")) {
                return buildEnglishRewrite(normalized, roleFragment, request);
            }
            if (lowered.contains("i have experience") || lowered.contains("i have an experience")) {
                return buildEnglishRewrite(normalized, roleFragment, request);
            }
            return buildEnglishRewrite(normalized, roleFragment, request);
        }

        return buildFrenchRewrite(normalized, roleFragment, request);
    }

    private String normalizeSpelling(String text) {
        String value = safeText(text).replaceAll("\\s+", " ").trim();
        value = value.replaceAll("(?i)\\bweb develop+pment\\b", "web development");
        value = value.replaceAll("(?i)\\bdevelop+pment\\b", "development");
        value = value.replaceAll("(?i)\\bmanag+ement\\b", "management");
        value = value.replaceAll("(?i)\\bexpereince\\b", "experience");
        value = value.replaceAll("(?i)\\bproffesional\\b", "professional");
        value = value.replaceAll("(?i)\\bcommmunication\\b", "communication");
        value = value.replaceAll("(?i)\\bweb develpment\\b", "web development");
        value = value.replaceAll("(?i)\\bdeveloppment\\b", "development");
        value = value.replaceAll("(?i)\\bhelpp?\\b", "help");
        value = value.replaceAll("(?i)\\bplataform\\b", "platform");
        return value;
    }

    private String buildEnglishRewrite(String text, String roleFragment, ServiceRequest request) {
        String clean = text.replaceAll("[\\s]+", " ").trim();
        String experiencePhrase = extractExperiencePhrase(clean, true);
        String skillPhrase = extractSkillPhrase(clean, true);

        StringBuilder result = new StringBuilder();
        result.append("I am confident I can contribute to ").append(roleFragment).append(" with a professional and proactive approach.");
        if (!experiencePhrase.isBlank()) {
            result.append(' ').append(experiencePhrase);
        } else {
            result.append(" I have hands-on experience delivering clean, reliable digital work.");
        }

        if (!skillPhrase.isBlank()) {
            result.append(' ').append(skillPhrase);
        } else {
            result.append(" I am comfortable working on practical tasks, collaborating with teams, and adapting quickly to project needs.");
        }

        result.append(' ').append(buildEnglishClosing(request));
        return capitalizeSentences(result.toString());
    }

    private String buildFrenchRewrite(String text, String roleFragment, ServiceRequest request) {
        String clean = text.replaceAll("[\\s]+", " ").trim();
        String experiencePhrase = extractExperiencePhrase(clean, false);
        String skillPhrase = extractSkillPhrase(clean, false);

        StringBuilder result = new StringBuilder();
        result.append("Je souhaite mettre mes competences au service de ").append(roleFragment).append(" avec une approche serieuse, claire et orientee resultat.");
        if (!experiencePhrase.isBlank()) {
            result.append(' ').append(experiencePhrase);
        } else {
            result.append(" Je dispose d'une base solide pour contribuer efficacement aux besoins du poste.");
        }

        if (!skillPhrase.isBlank()) {
            result.append(' ').append(skillPhrase);
        } else {
            result.append(" Je suis capable de m'adapter rapidement, de travailler avec rigueur et de collaborer efficacement.");
        }

        result.append(' ').append(buildFrenchClosing(request));
        return capitalizeSentences(result.toString());
    }

    private String extractExperiencePhrase(String text, boolean english) {
        String lowered = text.toLowerCase(Locale.ROOT);
        if (lowered.contains("web development")) {
            return english
                    ? "I have experience in web development and can contribute to building clean, functional, and user-friendly solutions."
                    : "J'ai de l'experience en developpement web et je peux contribuer a la creation de solutions propres, fonctionnelles et utiles."
        ;
        }
        if (lowered.contains("development")) {
            return english
                    ? "I have experience in development and I am used to turning requirements into practical results."
                    : "J'ai de l'experience en developpement et j'ai l'habitude de transformer des besoins en resultats concrets."
        ;
        }
        if (lowered.contains("project")) {
            return english
                    ? "I can bring a practical mindset and a strong focus on project delivery."
                    : "Je peux apporter une approche concrete et un vrai sens de la livraison de projet."
        ;
        }
        return "";
    }

    private String extractSkillPhrase(String text, boolean english) {
        String lowered = text.toLowerCase(Locale.ROOT);
        if (lowered.contains("frontend") || lowered.contains("front-end")) {
            return english
                    ? "I am comfortable contributing on the front end and adapting the message to business goals."
                    : "Je suis a l'aise pour contribuer cote front-end et adapter mon travail aux objectifs du besoin."
        ;
        }
        if (lowered.contains("backend") || lowered.contains("back-end")) {
            return english
                    ? "I also pay attention to structure, reliability, and maintainable implementation."
                    : "Je fais aussi attention a la structure, a la fiabilite et a la maintenabilite."
        ;
        }
        if (lowered.contains("team") || lowered.contains("collabor")) {
            return english
                    ? "I work well in collaborative environments and communicate clearly."
                    : "Je travaille bien en equipe et je communique clairement."
        ;
        }
        return "";
    }

    private String buildEnglishClosing(ServiceRequest request) {
        String offerName = safeText(request.getName()).trim();
        if (offerName.isBlank()) {
            return "I would be glad to discuss how my background can support your needs.";
        }
        return "I would be glad to discuss how my background can support " + offerName + ".";
    }

    private String buildFrenchClosing(ServiceRequest request) {
        String offerName = safeText(request.getName()).trim();
        if (offerName.isBlank()) {
            return "Je serais ravi d'echanger sur la maniere dont mon profil peut repondre a vos besoins.";
        }
        return "Je serais ravi d'echanger sur la maniere dont mon profil peut repondre a " + offerName + ".";
    }

    private String cleanupFragment(String fragment) {
        String value = safeText(fragment).trim();
        if (value.isBlank()) {
            return value;
        }

        value = value.replaceAll("\\s+", " ");
        value = value.replaceAll("\\s+([,.;:!?])", "$1");
        value = value.replaceAll("([,.;:!?])(\\S)", "$1 $2");
        value = value.replaceAll("(?i)\\bi\\b", "I");
        value = value.replaceAll("(?i)\\bi'm\\b", "I'm");
        value = value.replaceAll("(?i)\\bcan't\\b", "cannot");
        value = value.replaceAll("(?i)\\bdon't\\b", "do not");
        value = value.replaceAll("(?i)\\bwon't\\b", "will not");
        value = value.replaceAll("(?i)\\bweb develop+pment\\b", "web development");
        value = value.replaceAll("(?i)\\bdevelop+pment\\b", "development");
        value = value.replaceAll("(?i)\\bplatfom\\b", "platform");
        value = value.replaceAll("(?i)\\bexpereince\\b", "experience");
        value = value.replaceAll("(?i)\\bsucces\\b", "success");
        value = value.replaceAll("(?i)\\bhelping you\\b", "helping clients");

        if (!value.isEmpty()) {
            value = Character.toUpperCase(value.charAt(0)) + value.substring(1);
        }
        if (!value.endsWith(".") && !value.endsWith("!") && !value.endsWith("?")) {
            value = value + ".";
        }
        return value;
    }

    private String capitalizeSentences(String text) {
        String normalized = safeText(text).replaceAll("\\s+", " ").trim();
        if (normalized.isBlank()) {
            return normalized;
        }

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (int i = 0; i < normalized.length(); i++) {
            char current = normalized.charAt(i);
            if (capitalizeNext && Character.isLetter(current)) {
                result.append(Character.toUpperCase(current));
                capitalizeNext = false;
            } else {
                result.append(current);
            }

            if (current == '.' || current == '!' || current == '?') {
                capitalizeNext = true;
            }
        }
        return result.toString().replaceAll("\\s+", " ").trim();
    }

    private String buildOfferSignal(ServiceRequest request, String language) {
        String offerName = safeText(request.getName()).trim();
        String category = request.getCategory() == null ? "" : request.getCategory().name().toLowerCase(Locale.ROOT).replace('_', ' ');

        if (language != null && language.equalsIgnoreCase("en")) {
            if (!offerName.isBlank()) {
                return "I am aligning this message with the role: " + offerName + ".";
            }
            if (!category.isBlank()) {
                return "I am aligning this message with the category: " + category + ".";
            }
            return "I am aligning this message with the opportunity.";
        }

        if (!offerName.isBlank()) {
            return "Je rends ce message plus pertinent pour l'offre: " + offerName + ".";
        }
        if (!category.isBlank()) {
            return "Je rends ce message plus pertinent pour la categorie: " + category + ".";
        }
        return "Je rends ce message plus pertinent pour l'offre.";
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
