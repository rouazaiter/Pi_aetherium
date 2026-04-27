package com.education.platform.services.implementations.cv;

import com.education.platform.common.ApiException;
import com.education.platform.dto.cv.CVEducationDto;
import com.education.platform.dto.cv.CVExperienceDto;
import com.education.platform.dto.cv.CVLanguageDto;
import com.education.platform.dto.cv.CVProfileResponse;
import com.education.platform.dto.cv.CvAiChatRequest;
import com.education.platform.dto.cv.CvAiChatResponse;
import com.education.platform.dto.cv.CvAiSuggestedAction;
import com.education.platform.entities.Profile;
import com.education.platform.entities.User;
import com.education.platform.entities.admin.jihenportfolio.AiFeature;
import com.education.platform.entities.cv.CVDraft;
import com.education.platform.entities.cv.CVSection;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.entities.portfolio.PortfolioProject;
import com.education.platform.entities.portfolio.Skill;
import com.education.platform.repositories.cv.CVDraftRepository;
import com.education.platform.repositories.portfolio.PortfolioProjectRepository;
import com.education.platform.repositories.portfolio.PortfolioRepository;
import com.education.platform.services.interfaces.admin.jihenportfolio.JihenPortfolioAdminTrackingService;
import com.education.platform.services.interfaces.cv.CVAiAssistantService;
import com.education.platform.services.interfaces.cv.CVProfileService;
import com.education.platform.services.interfaces.cv.OllamaClient;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CVAiAssistantServiceImpl implements CVAiAssistantService {

    private final CVDraftRepository cvDraftRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioProjectRepository portfolioProjectRepository;
    private final CVProfileService cvProfileService;
    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper;
    private final JihenPortfolioAdminTrackingService trackingService;

    public CVAiAssistantServiceImpl(
            CVDraftRepository cvDraftRepository,
            PortfolioRepository portfolioRepository,
            PortfolioProjectRepository portfolioProjectRepository,
            CVProfileService cvProfileService,
            OllamaClient ollamaClient,
            ObjectMapper objectMapper,
            JihenPortfolioAdminTrackingService trackingService) {
        this.cvDraftRepository = cvDraftRepository;
        this.portfolioRepository = portfolioRepository;
        this.portfolioProjectRepository = portfolioProjectRepository;
        this.cvProfileService = cvProfileService;
        this.ollamaClient = ollamaClient;
        this.objectMapper = objectMapper;
        this.trackingService = trackingService;
    }

    @Override
    @Transactional(readOnly = true)
    public CvAiChatResponse chatForUser(User user, CvAiChatRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Message is required");
        }

        CVDraft draft = resolveDraft(user, request.getDraftId());
        Portfolio portfolio = portfolioRepository.findByUser_Id(user.getId()).orElse(null);
        List<PortfolioProject> projects = portfolio == null
                ? List.of()
                : portfolioProjectRepository.findByPortfolio_IdOrderByCreatedAtDesc(portfolio.getId());
        CVProfileResponse cvProfile = cvProfileService.getForUser(user);

        int score = calculateScore(draft, cvProfile, portfolio, projects);
        String prompt = buildPrompt(user, draft, cvProfile, portfolio, projects, score, request.getMessage().trim());
        long startedAt = System.nanoTime();
        try {
            String rawReply = ollamaClient.generate(prompt);
            String reply = CvAiTextSanitizer.sanitize(rawReply);
            if (reply.isBlank()) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "AI assistant reply was empty");
            }

            trackingService.recordAiUsage(AiFeature.CV_CHAT, user == null ? null : user.getId(), true, elapsedMillis(startedAt), null);
            return CvAiChatResponse.builder()
                    .reply(reply)
                    .score(score)
                    .suggestedActions(List.<CvAiSuggestedAction>of())
                    .build();
        } catch (RuntimeException e) {
            trackingService.recordAiUsage(AiFeature.CV_CHAT, user == null ? null : user.getId(), false, elapsedMillis(startedAt), e.getMessage());
            throw e;
        }
    }

    private long elapsedMillis(long startedAt) {
        return Math.max(0L, (System.nanoTime() - startedAt) / 1_000_000L);
    }

    private CVDraft resolveDraft(User user, Long draftId) {
        return (draftId == null
                ? cvDraftRepository.findTopByUser_IdOrderByUpdatedAtDescIdDesc(user.getId())
                : cvDraftRepository.findByIdAndUser_Id(draftId, user.getId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CV draft not found"));
    }

    private String buildPrompt(
            User user,
            CVDraft draft,
            CVProfileResponse cvProfile,
            Portfolio portfolio,
            List<PortfolioProject> projects,
            int score,
            String message) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a strict CV assistant.\n");
        prompt.append("Use only the user data provided below.\n");
        prompt.append("Never invent companies, dates, metrics, degrees, tools, achievements, certifications, or responsibilities.\n");
        prompt.append("Do not auto-save or assume changes were applied.\n");
        prompt.append("Answer the user's request directly and practically, not generically.\n");
        prompt.append("If data is missing, say exactly what is missing.\n");
        prompt.append("Current CV score: ").append(score).append("/100.\n");
        prompt.append("User message: ").append(message).append("\n\n");
        prompt.append("Required answer structure:\n");
        prompt.append("CV Score: ").append(score).append("/100\n");
        prompt.append("Main issues:\n");
        prompt.append("- concise issue list tied to actual data\n");
        prompt.append("Concrete improvements:\n");
        prompt.append("- prioritized improvements tied to actual data\n");
        prompt.append("Rewritten suggestions:\n");
        prompt.append("- include rewrites only when useful and only using existing facts\n");
        prompt.append("Next steps:\n");
        prompt.append("- short actionable next steps\n\n");
        prompt.append("USER DATA JSON:\n");
        prompt.append(buildContextJson(user, draft, cvProfile, portfolio, projects)).append('\n');
        return prompt.toString();
    }

    private String buildContextJson(
            User user,
            CVDraft draft,
            CVProfileResponse cvProfile,
            Portfolio portfolio,
            List<PortfolioProject> projects) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("draftId", draft.getId());
        root.put("theme", safeText(draft.getTheme()));
        root.set("user", buildUserNode(user));
        root.set("cvProfile", buildCvProfileNode(cvProfile));
        root.set("draftSections", buildDraftSectionsNode(draft));
        root.set("portfolio", buildPortfolioNode(portfolio));
        root.set("projects", buildProjectsNode(projects));
        pruneEmpty(root);

        try {
            return objectMapper.copy()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to prepare AI assistant context");
        }
    }

    private ObjectNode buildUserNode(User user) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("username", safeText(user.getUsername()));
        node.put("email", safeText(user.getEmail()));

        Profile profile = user.getProfile();
        if (profile != null) {
            ObjectNode profileNode = objectMapper.createObjectNode();
            profileNode.put("firstName", safeText(profile.getFirstName()));
            profileNode.put("lastName", safeText(profile.getLastName()));
            profileNode.put("description", safeText(profile.getDescription()));
            if (profile.getInterests() != null && !profile.getInterests().isEmpty()) {
                ArrayNode interests = profileNode.putArray("interests");
                profile.getInterests().stream()
                        .filter(this::hasText)
                        .forEach(interests::add);
            }
            node.set("profile", profileNode);
        }
        return node;
    }

    private JsonNode buildCvProfileNode(CVProfileResponse cvProfile) {
        return objectMapper.valueToTree(cvProfile);
    }

    private ArrayNode buildDraftSectionsNode(CVDraft draft) {
        ArrayNode sections = objectMapper.createArrayNode();
        for (CVSection section : draft.getSections()) {
            ObjectNode node = sections.addObject();
            node.put("sectionId", section.getId());
            node.put("sectionType", section.getType() == null ? null : section.getType().name());
            node.put("title", safeText(section.getTitle()));
            node.put("visible", Boolean.TRUE.equals(section.getVisible()));
            node.put("orderIndex", section.getOrderIndex() == null ? 0 : section.getOrderIndex());
            node.set("content", readJson(section.getContentJson()));
        }
        return sections;
    }

    private ObjectNode buildPortfolioNode(Portfolio portfolio) {
        ObjectNode node = objectMapper.createObjectNode();
        if (portfolio == null) {
            return node;
        }
        node.put("id", portfolio.getId());
        node.put("title", safeText(portfolio.getTitle()));
        node.put("job", safeText(portfolio.getJob()));
        node.put("bio", safeText(portfolio.getBio()));
        node.put("githubUrl", safeText(portfolio.getGithubUrl()));
        node.put("linkedinUrl", safeText(portfolio.getLinkedinUrl()));
        if (portfolio.getSkills() != null && !portfolio.getSkills().isEmpty()) {
            ArrayNode skills = node.putArray("skills");
            portfolio.getSkills().stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(skill -> safeText(skill.getName()), String.CASE_INSENSITIVE_ORDER))
                    .forEach(skill -> {
                        ObjectNode skillNode = skills.addObject();
                        skillNode.put("id", skill.getId());
                        skillNode.put("name", safeText(skill.getName()));
                        skillNode.put("category", skill.getCategory() == null ? null : skill.getCategory().name());
                    });
        }
        return node;
    }

    private ArrayNode buildProjectsNode(List<PortfolioProject> projects) {
        ArrayNode array = objectMapper.createArrayNode();
        for (PortfolioProject project : projects) {
            if (project == null) {
                continue;
            }
            ObjectNode node = array.addObject();
            node.put("id", project.getId());
            node.put("title", safeText(project.getTitle()));
            node.put("description", safeText(project.getDescription()));
            node.put("projectUrl", safeText(project.getProjectUrl()));
            if (project.getSkills() != null && !project.getSkills().isEmpty()) {
                ArrayNode skills = node.putArray("skills");
                project.getSkills().stream()
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(skill -> safeText(skill.getName()), String.CASE_INSENSITIVE_ORDER))
                        .forEach(skill -> skills.add(safeText(skill.getName())));
            }
        }
        return array;
    }

    private int calculateScore(
            CVDraft draft,
            CVProfileResponse cvProfile,
            Portfolio portfolio,
            List<PortfolioProject> projects) {
        int score = 100;

        String headline = firstNonBlank(cvProfile == null ? null : cvProfile.getHeadline(), portfolio == null ? null : portfolio.getJob());
        String summary = firstNonBlank(
                cvProfile == null ? null : cvProfile.getSummary(),
                cvProfile == null ? null : cvProfile.getProfessionalSummary(),
                portfolio == null ? null : portfolio.getBio(),
                userProfileDescription(cvProfile, portfolio)
        );

        if (!hasText(headline)) {
            score -= 12;
        }
        if (!hasText(summary)) {
            score -= 18;
        } else if (summary.trim().length() < 80) {
            score -= 8;
        }

        int projectCount = (int) projects.stream().filter(Objects::nonNull).count();
        if (projectCount == 0) {
            score -= 16;
        } else if (projectCount == 1) {
            score -= 8;
        }

        long weakProjectDescriptions = projects.stream()
                .filter(Objects::nonNull)
                .map(PortfolioProject::getDescription)
                .filter(text -> !hasText(text) || text.trim().length() < 60)
                .count();
        score -= (int) Math.min(15, weakProjectDescriptions * 4);

        int skillsCount = uniqueSkillNames(portfolio, projects).size();
        if (skillsCount == 0) {
            score -= 12;
        } else if (skillsCount < 4) {
            score -= 6;
        }

        int experienceCount = cvProfile == null || cvProfile.getExperience() == null ? 0 : nonEmptyExperience(cvProfile.getExperience());
        if (experienceCount == 0) {
            score -= 12;
        }

        int educationCount = cvProfile == null || cvProfile.getEducation() == null ? 0 : nonEmptyEducation(cvProfile.getEducation());
        if (educationCount == 0) {
            score -= 6;
        }

        int visibleSections = (int) draft.getSections().stream().filter(section -> Boolean.TRUE.equals(section.getVisible())).count();
        if (visibleSections < 3) {
            score -= 8;
        }

        return Math.max(0, Math.min(100, score));
    }

    private int nonEmptyExperience(List<CVExperienceDto> experience) {
        return (int) experience.stream()
                .filter(Objects::nonNull)
                .filter(entry -> hasText(entry.getRole()) || hasText(entry.getCompany()) || hasText(entry.getSummary()))
                .count();
    }

    private int nonEmptyEducation(List<CVEducationDto> education) {
        return (int) education.stream()
                .filter(Objects::nonNull)
                .filter(entry -> hasText(entry.getSchool()) || hasText(entry.getDegree()) || hasText(entry.getFieldOfStudy()) || hasText(entry.getDescription()))
                .count();
    }

    private String userProfileDescription(CVProfileResponse cvProfile, Portfolio portfolio) {
        return portfolio == null ? null : portfolio.getBio();
    }

    private Set<String> uniqueSkillNames(Portfolio portfolio, List<PortfolioProject> projects) {
        Set<String> values = projects.stream()
                .filter(Objects::nonNull)
                .flatMap(project -> project.getSkills() == null ? java.util.stream.Stream.<Skill>empty() : project.getSkills().stream())
                .map(Skill::getName)
                .filter(this::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

        if (portfolio != null && portfolio.getSkills() != null) {
            portfolio.getSkills().stream()
                    .filter(Objects::nonNull)
                    .map(Skill::getName)
                    .filter(this::hasText)
                    .map(String::trim)
                    .forEach(values::add);
        }
        return values;
    }

    private JsonNode readJson(String value) {
        try {
            return value == null ? objectMapper.nullNode() : objectMapper.readTree(value);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to prepare AI assistant context");
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

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String safeText(String value) {
        return hasText(value) ? value.trim() : null;
    }
}
