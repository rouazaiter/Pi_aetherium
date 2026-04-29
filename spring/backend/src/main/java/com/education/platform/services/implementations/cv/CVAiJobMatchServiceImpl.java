package com.education.platform.services.implementations.cv;

import com.education.platform.common.ApiException;
import com.education.platform.dto.cv.CVEducationDto;
import com.education.platform.dto.cv.CVExperienceDto;
import com.education.platform.dto.cv.CVPreviewProjectDto;
import com.education.platform.dto.cv.CVPreviewSkillDto;
import com.education.platform.dto.cv.CVPreviewSkillGroupDto;
import com.education.platform.dto.cv.CvAiJobMatchRequest;
import com.education.platform.dto.cv.CvAiJobMatchResponse;
import com.education.platform.dto.cv.CvAiJobMatchedProjectResponse;
import com.education.platform.entities.User;
import com.education.platform.entities.admin.jihenportfolio.AiFeature;
import com.education.platform.entities.cv.CVDraft;
import com.education.platform.entities.cv.CVSection;
import com.education.platform.entities.cv.CVSectionType;
import com.education.platform.repositories.cv.CVDraftRepository;
import com.education.platform.services.interfaces.admin.jihenportfolio.JihenPortfolioAdminTrackingService;
import com.education.platform.services.interfaces.cv.CVAiJobMatchService;
import com.education.platform.services.interfaces.cv.OllamaClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CVAiJobMatchServiceImpl implements CVAiJobMatchService {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[a-z0-9+#.]{2,}");
    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile("(?s)\\{.*}");
    private static final TypeReference<List<CVPreviewProjectDto>> PROJECT_LIST_TYPE = new TypeReference<>() { };
    private static final TypeReference<List<CVPreviewSkillGroupDto>> SKILL_GROUP_LIST_TYPE = new TypeReference<>() { };
    private static final TypeReference<List<CVExperienceDto>> EXPERIENCE_LIST_TYPE = new TypeReference<>() { };
    private static final TypeReference<List<CVEducationDto>> EDUCATION_LIST_TYPE = new TypeReference<>() { };
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "in", "into", "is", "it",
            "of", "on", "or", "that", "the", "their", "this", "to", "with", "you", "your", "will",
            "using", "used", "use", "our", "we", "they", "them", "have", "has", "had", "were", "was",
            "can", "should", "must", "such", "than", "then", "who", "how", "what", "when", "where",
            "why", "all", "any", "both", "each", "few", "more", "most", "other", "some", "very",
            "about", "after", "before", "over", "under", "through", "across", "per", "via", "if",
            "but", "not", "no", "yes", "role", "team", "teams", "work", "working", "experience",
            "experienced", "strong", "ability", "skills", "skill", "knowledge", "plus", "preferred",
            "requirements", "requirement", "responsibilities", "responsibility", "candidate"
    );

    private final CVDraftRepository cvDraftRepository;
    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper;
    private final JihenPortfolioAdminTrackingService trackingService;

    public CVAiJobMatchServiceImpl(
            CVDraftRepository cvDraftRepository,
            OllamaClient ollamaClient,
            ObjectMapper objectMapper,
            JihenPortfolioAdminTrackingService trackingService) {
        this.cvDraftRepository = cvDraftRepository;
        this.ollamaClient = ollamaClient;
        this.objectMapper = objectMapper;
        this.trackingService = trackingService;
    }

    @Override
    @Transactional(readOnly = true)
    public CvAiJobMatchResponse matchForUser(User user, CvAiJobMatchRequest request) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Request is required");
        }

        CVDraft draft = cvDraftRepository.findByIdAndUser_Id(request.getDraftId(), user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CV draft not found"));

        DraftData draftData = extractDraftData(draft);
        JobAnalysis analysis = analyzeJobDescription(request, draftData);
        long startedAt = System.nanoTime();

        try {
            String raw = ollamaClient.generate(buildPrompt(request, draftData, analysis));
            trackingService.recordAiUsage(AiFeature.JOB_MATCHED_CV, user == null ? null : user.getId(), true, elapsedMillis(startedAt), null);
            return mergeAiResponse(request, draftData, analysis, raw);
        } catch (Exception ignored) {
            trackingService.recordAiUsage(AiFeature.JOB_MATCHED_CV, user == null ? null : user.getId(), false, elapsedMillis(startedAt), ignored.getMessage());
            return buildFallbackResponse(request, draftData, analysis);
        }
    }

    private long elapsedMillis(long startedAt) {
        return Math.max(0L, (System.nanoTime() - startedAt) / 1_000_000L);
    }

    private DraftData extractDraftData(CVDraft draft) {
        String summary = null;
        List<CVPreviewProjectDto> projects = List.of();
        List<String> skills = List.of();
        List<CVExperienceDto> experience = List.of();
        List<CVEducationDto> education = List.of();

        for (CVSection section : draft.getSections()) {
            if (section == null || section.getType() == null) {
                continue;
            }

            JsonNode content = readJson(section.getContentJson());
            switch (section.getType()) {
                case PROFILE -> summary = extractSummary(content);
                case PROJECTS -> projects = convertList(content, PROJECT_LIST_TYPE);
                case SKILLS -> skills = extractSkillNames(content);
                case EXPERIENCE -> experience = convertList(content, EXPERIENCE_LIST_TYPE);
                case EDUCATION -> education = convertList(content, EDUCATION_LIST_TYPE);
                default -> {
                }
            }
        }

        return new DraftData(
                normalizeText(summary),
                projects.stream().filter(Objects::nonNull).toList(),
                skills.stream().filter(this::hasText).toList(),
                experience.stream().filter(Objects::nonNull).toList(),
                education.stream().filter(Objects::nonNull).toList()
        );
    }

    private String extractSummary(JsonNode content) {
        if (content == null || content.isNull() || !content.isObject()) {
            return null;
        }
        return firstNonBlank(
                textValue(content.get("summary")),
                textValue(content.get("professionalSummary")),
                textValue(content.get("headline"))
        );
    }

    private List<String> extractSkillNames(JsonNode content) {
        List<CVPreviewSkillGroupDto> groups = convertList(content, SKILL_GROUP_LIST_TYPE);
        LinkedHashSet<String> skillNames = new LinkedHashSet<>();
        for (CVPreviewSkillGroupDto group : groups) {
            if (group == null || group.getSkills() == null) {
                continue;
            }
            for (CVPreviewSkillDto skill : group.getSkills()) {
                if (skill != null && hasText(skill.getName())) {
                    skillNames.add(skill.getName().trim());
                }
            }
        }
        return List.copyOf(skillNames);
    }

    private JobAnalysis analyzeJobDescription(CvAiJobMatchRequest request, DraftData draftData) {
        LinkedHashMap<String, String> extracted = new LinkedHashMap<>();
        Map<String, String> userKeywordMap = buildUserKeywordMap(draftData);

        collectPhraseMatches(extracted, request.getTargetJobTitle(), request.getJobDescription(), userKeywordMap.values());
        collectFrequentTokens(extracted, request.getTargetJobTitle(), 6);
        collectFrequentTokens(extracted, request.getJobDescription(), 14);

        List<String> extractedKeywords = extracted.values().stream().limit(15).toList();
        List<String> matchingKeywords = extracted.entrySet().stream()
                .filter(entry -> userKeywordMap.containsKey(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();
        List<String> missingKeywords = extracted.entrySet().stream()
                .filter(entry -> !userKeywordMap.containsKey(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();

        return new JobAnalysis(
                extractedKeywords,
                matchingKeywords,
                missingKeywords,
                detectRoleType(request.getTargetJobTitle(), request.getJobDescription())
        );
    }

    private Map<String, String> buildUserKeywordMap(DraftData draftData) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        for (String skill : draftData.skills()) {
            addKeyword(map, skill, skill);
        }
        for (CVPreviewProjectDto project : draftData.projects()) {
            if (hasText(project.getTitle())) {
                addKeyword(map, project.getTitle(), project.getTitle().trim());
                collectFrequentTokens(map, project.getTitle(), 4);
            }
            if (project.getSkills() != null) {
                for (CVPreviewSkillDto skill : project.getSkills()) {
                    if (skill != null && hasText(skill.getName())) {
                        addKeyword(map, skill.getName(), skill.getName().trim());
                    }
                }
            }
        }
        for (CVExperienceDto item : draftData.experience()) {
            addKeyword(map, item.getRole(), normalizeText(item.getRole()));
            addKeyword(map, item.getCompany(), normalizeText(item.getCompany()));
            collectFrequentTokens(map, item.getSummary(), 4);
        }
        for (CVEducationDto item : draftData.education()) {
            addKeyword(map, item.getDegree(), normalizeText(item.getDegree()));
            addKeyword(map, item.getFieldOfStudy(), normalizeText(item.getFieldOfStudy()));
        }

        return map;
    }

    private void collectPhraseMatches(Map<String, String> output, String targetJobTitle, String jobDescription, Iterable<String> supportedPhrases) {
        String combined = (safeValue(targetJobTitle) + " " + safeValue(jobDescription)).toLowerCase(Locale.ROOT);
        for (String phrase : supportedPhrases) {
            if (!hasText(phrase)) {
                continue;
            }
            String normalized = normalizeKeyword(phrase);
            if (normalized.isBlank()) {
                continue;
            }
            if (combined.contains(normalized.toLowerCase(Locale.ROOT))) {
                output.putIfAbsent(normalized.toLowerCase(Locale.ROOT), phrase.trim());
            }
        }
    }

    private void collectFrequentTokens(Map<String, String> output, String text, int limit) {
        if (!hasText(text)) {
            return;
        }

        LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
        Matcher matcher = TOKEN_PATTERN.matcher(text.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            String token = matcher.group();
            if (token.length() < 3 || STOP_WORDS.contains(token)) {
                continue;
            }
            counts.merge(token, 1, Integer::sum);
        }

        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
                .limit(limit)
                .forEach(entry -> output.putIfAbsent(entry.getKey(), entry.getKey()));
    }

    private String detectRoleType(String targetJobTitle, String jobDescription) {
        String combined = (safeValue(targetJobTitle) + " " + safeValue(jobDescription)).toLowerCase(Locale.ROOT);
        if (combined.contains("full stack") || combined.contains("fullstack")) {
            return "FULL_STACK_DEVELOPER";
        }
        if (combined.contains("frontend") || combined.contains("front-end") || combined.contains("angular") || combined.contains("react") || combined.contains("vue")) {
            return "FRONTEND_DEVELOPER";
        }
        if (combined.contains("backend") || combined.contains("back-end") || combined.contains("spring") || combined.contains("java") || combined.contains("api")) {
            return "BACKEND_DEVELOPER";
        }
        if (combined.contains("data") || combined.contains("machine learning") || combined.contains("ml ") || combined.contains("analytics")) {
            return "DATA";
        }
        if (combined.contains("devops") || combined.contains("cloud") || combined.contains("kubernetes") || combined.contains("docker") || combined.contains("infrastructure")) {
            return "DEVOPS";
        }
        return "GENERAL_SOFTWARE";
    }

    private String buildPrompt(CvAiJobMatchRequest request, DraftData draftData, JobAnalysis analysis) {
        ObjectNode context = objectMapper.createObjectNode();
        context.put("targetJobTitle", request.getTargetJobTitle().trim());
        context.put("roleType", analysis.roleType());
        context.put("tone", normalizeOptionalValue(request.getTone(), "professional"));
        context.put("language", normalizeOptionalValue(request.getLanguage(), "en"));
        context.put("currentSummary", safeValue(draftData.summary()));
        context.set("projects", objectMapper.valueToTree(draftData.projects()));
        context.set("skills", objectMapper.valueToTree(draftData.skills()));
        context.set("experience", objectMapper.valueToTree(draftData.experience()));
        context.set("education", objectMapper.valueToTree(draftData.education()));
        context.set("extractedKeywords", objectMapper.valueToTree(analysis.extractedKeywords()));
        context.set("matchingKeywords", objectMapper.valueToTree(analysis.matchingKeywords()));
        context.set("missingKeywords", objectMapper.valueToTree(analysis.missingKeywords()));
        context.put("jobDescription", request.getJobDescription().trim());

        StringBuilder prompt = new StringBuilder();
        prompt.append("You generate a job-matched CV improvement plan.\n");
        prompt.append("Return JSON only.\n");
        prompt.append("Be ATS-friendly, concise, action-oriented, and use strong verbs.\n");
        prompt.append("Use relevant keywords only if they are supported by the user data.\n");
        prompt.append("Do NOT invent skills, projects, experience, education, metrics, employers, results, or tools.\n");
        prompt.append("If evidence is missing, keep the original meaning and stay conservative.\n");
        prompt.append("Use the requested language when writing text.\n");
        prompt.append("Required JSON shape:\n");
        prompt.append("{\"improvedSummary\":\"...\",\"improvedProjects\":[{\"projectId\":1,\"improvedDescription\":\"...\"}],\"recommendations\":[\"...\"]}\n");
        prompt.append("Every improved project must reference an existing projectId from the context.\n");
        prompt.append("Recommendations must be practical and based on the actual gaps.\n");
        prompt.append("CONTEXT JSON:\n");
        prompt.append(writeJson(context)).append('\n');
        return prompt.toString();
    }

    private CvAiJobMatchResponse mergeAiResponse(
            CvAiJobMatchRequest request,
            DraftData draftData,
            JobAnalysis analysis,
            String raw) {
        JsonNode root = parseJsonObject(raw);
        String improvedSummary = firstNonBlank(
                textValue(root.get("improvedSummary")),
                draftData.summary(),
                ""
        );

        Map<Long, CVPreviewProjectDto> projectsById = draftData.projects().stream()
                .filter(project -> project.getId() != null)
                .collect(Collectors.toMap(CVPreviewProjectDto::getId, project -> project, (left, right) -> left, LinkedHashMap::new));

        List<CvAiJobMatchedProjectResponse> improvedProjects = new ArrayList<>();
        JsonNode projectArray = root.get("improvedProjects");
        if (projectArray instanceof ArrayNode arrayNode) {
            for (JsonNode item : arrayNode) {
                Long projectId = item.hasNonNull("projectId") ? item.get("projectId").asLong() : null;
                if (projectId == null) {
                    continue;
                }
                CVPreviewProjectDto project = projectsById.get(projectId);
                if (project == null) {
                    continue;
                }
                improvedProjects.add(CvAiJobMatchedProjectResponse.builder()
                        .projectId(projectId)
                        .originalDescription(normalizeText(project.getDescription()))
                        .improvedDescription(firstNonBlank(textValue(item.get("improvedDescription")), normalizeText(project.getDescription()), ""))
                        .build());
            }
        }

        if (improvedProjects.isEmpty()) {
            improvedProjects = buildFallbackProjects(draftData.projects());
        } else {
            Set<Long> coveredIds = improvedProjects.stream()
                    .map(CvAiJobMatchedProjectResponse::getProjectId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            for (CVPreviewProjectDto project : draftData.projects()) {
                if (project.getId() != null && !coveredIds.contains(project.getId())) {
                    improvedProjects.add(CvAiJobMatchedProjectResponse.builder()
                            .projectId(project.getId())
                            .originalDescription(normalizeText(project.getDescription()))
                            .improvedDescription(normalizeText(project.getDescription()))
                            .build());
                }
            }
        }

        List<String> recommendations = readStringArray(root.get("recommendations"));
        if (recommendations.isEmpty()) {
            recommendations = buildFallbackRecommendations(draftData, analysis);
        }

        return CvAiJobMatchResponse.builder()
                .targetJobTitle(request.getTargetJobTitle().trim())
                .extractedKeywords(analysis.extractedKeywords())
                .matchingKeywords(analysis.matchingKeywords())
                .missingKeywords(analysis.missingKeywords())
                .improvedSummary(improvedSummary)
                .improvedProjects(improvedProjects.stream()
                        .sorted(Comparator.comparing(item -> item.getProjectId() == null ? Long.MAX_VALUE : item.getProjectId()))
                        .toList())
                .recommendations(recommendations)
                .build();
    }

    private CvAiJobMatchResponse buildFallbackResponse(
            CvAiJobMatchRequest request,
            DraftData draftData,
            JobAnalysis analysis) {
        return CvAiJobMatchResponse.builder()
                .targetJobTitle(request.getTargetJobTitle().trim())
                .extractedKeywords(analysis.extractedKeywords())
                .matchingKeywords(analysis.matchingKeywords())
                .missingKeywords(analysis.missingKeywords())
                .improvedSummary(firstNonBlank(draftData.summary(), ""))
                .improvedProjects(buildFallbackProjects(draftData.projects()))
                .recommendations(buildFallbackRecommendations(draftData, analysis))
                .build();
    }

    private List<CvAiJobMatchedProjectResponse> buildFallbackProjects(List<CVPreviewProjectDto> projects) {
        return projects.stream()
                .filter(Objects::nonNull)
                .map(project -> CvAiJobMatchedProjectResponse.builder()
                        .projectId(project.getId())
                        .originalDescription(normalizeText(project.getDescription()))
                        .improvedDescription(normalizeText(project.getDescription()))
                        .build())
                .toList();
    }

    private List<String> buildFallbackRecommendations(DraftData draftData, JobAnalysis analysis) {
        List<String> recommendations = new ArrayList<>();
        if (!hasText(draftData.summary())) {
            recommendations.add("Add a concise ATS summary aligned to the target role using only your verified experience and skills.");
        }
        if (!analysis.missingKeywords().isEmpty()) {
            recommendations.add("If you have real evidence for " + String.join(", ", analysis.missingKeywords().stream().limit(4).toList())
                    + ", make it explicit in your summary or project bullets; otherwise do not add those keywords.");
        }
        boolean hasWeakProject = draftData.projects().stream()
                .anyMatch(project -> !hasText(project.getDescription()) || project.getDescription().trim().length() < 80);
        if (hasWeakProject) {
            recommendations.add("Strengthen project descriptions with action verbs, scope, and stack details only where your existing project data supports them.");
        }
        if (recommendations.isEmpty()) {
            recommendations.add("Prioritize the strongest matching skills and projects for this target role, and keep every claim grounded in existing evidence.");
        }
        return recommendations;
    }

    private JsonNode parseJsonObject(String raw) {
        String cleaned = raw == null ? "" : raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").trim();
        }

        Matcher matcher = JSON_BLOCK_PATTERN.matcher(cleaned);
        if (matcher.find()) {
            cleaned = matcher.group();
        }

        try {
            return objectMapper.readTree(cleaned);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "AI job match response was not valid JSON");
        }
    }

    private <T> List<T> convertList(JsonNode node, TypeReference<List<T>> type) {
        if (node == null || node.isNull() || !node.isArray()) {
            return List.of();
        }
        return objectMapper.convertValue(node, type);
    }

    private JsonNode readJson(String value) {
        try {
            return value == null ? objectMapper.nullNode() : objectMapper.readTree(value);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read CV draft section");
        }
    }

    private List<String> readStringArray(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (JsonNode item : node) {
            String value = textValue(item);
            if (hasText(value)) {
                values.add(value.trim());
            }
        }
        return values;
    }

    private String writeJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to prepare AI job match context");
        }
    }

    private void addKeyword(Map<String, String> map, String rawKeyword, String displayValue) {
        if (!hasText(rawKeyword) || !hasText(displayValue)) {
            return;
        }
        String normalized = normalizeKeyword(rawKeyword);
        if (normalized.isBlank()) {
            return;
        }
        map.putIfAbsent(normalized.toLowerCase(Locale.ROOT), displayValue.trim());
    }

    private String normalizeKeyword(String value) {
        return Arrays.stream(safeValue(value).trim().split("[^A-Za-z0-9+#.]+"))
                .filter(token -> !token.isBlank())
                .collect(Collectors.joining(" "))
                .trim();
    }

    private String normalizeOptionalValue(String value, String fallback) {
        return hasText(value) ? value.trim() : fallback;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String textValue(JsonNode node) {
        return node == null || node.isNull() ? null : node.asText();
    }

    private String normalizeText(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record DraftData(
            String summary,
            List<CVPreviewProjectDto> projects,
            List<String> skills,
            List<CVExperienceDto> experience,
            List<CVEducationDto> education) {
    }

    private record JobAnalysis(
            List<String> extractedKeywords,
            List<String> matchingKeywords,
            List<String> missingKeywords,
            String roleType) {
    }
}
