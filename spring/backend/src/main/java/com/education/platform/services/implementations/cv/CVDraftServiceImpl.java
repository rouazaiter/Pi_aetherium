package com.education.platform.services.implementations.cv;

import com.education.platform.common.ApiException;
import com.education.platform.dto.cv.CVDraftResponse;
import com.education.platform.dto.cv.CVPreviewResponse;
import com.education.platform.dto.cv.CVSectionResponse;
import com.education.platform.dto.cv.UpdateCVDraftRequest;
import com.education.platform.dto.cv.UpdateCVSectionRequest;
import com.education.platform.entities.User;
import com.education.platform.entities.cv.CVDraft;
import com.education.platform.entities.cv.CVSection;
import com.education.platform.entities.cv.CVSectionType;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.repositories.cv.CVDraftRepository;
import com.education.platform.repositories.portfolio.PortfolioRepository;
import com.education.platform.services.interfaces.cv.CVBuilderService;
import com.education.platform.services.interfaces.cv.CVDraftService;
import com.education.platform.services.interfaces.cv.CVTemplateConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class CVDraftServiceImpl implements CVDraftService {

    public static final String DEFAULT_THEME = CVTemplateConstants.ATS_MINIMAL;
    private static final Set<String> SUPPORTED_THEMES = Set.of(
            CVTemplateConstants.ATS_MINIMAL,
            CVTemplateConstants.MODERN,
            CVTemplateConstants.ELEGANT,
            CVTemplateConstants.CREATIVE
    );

    private final CVDraftRepository cvDraftRepository;
    private final PortfolioRepository portfolioRepository;
    private final CVBuilderService cvBuilderService;
    private final ObjectMapper objectMapper;

    public CVDraftServiceImpl(
            CVDraftRepository cvDraftRepository,
            PortfolioRepository portfolioRepository,
            CVBuilderService cvBuilderService,
            ObjectMapper objectMapper) {
        this.cvDraftRepository = cvDraftRepository;
        this.portfolioRepository = portfolioRepository;
        this.cvBuilderService = cvBuilderService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public CVDraftResponse generateForUser(User user) {
        CVPreviewResponse preview = cvBuilderService.buildForUser(user);
        Optional<Portfolio> portfolio = portfolioRepository.findByUser_Id(user.getId());

        CVDraft draft = CVDraft.builder()
                .user(user)
                .portfolio(portfolio.orElse(null))
                .theme(DEFAULT_THEME)
                .settingsJson(toJson(buildSettings(preview, DEFAULT_THEME)))
                .build();

        addSection(draft, CVSectionType.PROFILE, "Profile", 0, preview.getProfile());
        addSection(draft, CVSectionType.SKILLS, "Skills", 1, preview.getSkillsByCategory());
        addSection(draft, CVSectionType.EXPERIENCE, "Experience", 2, preview.getExperience());
        addSection(draft, CVSectionType.EDUCATION, "Education", 3, preview.getEducation());
        addSection(draft, CVSectionType.LANGUAGES, "Languages", 4, preview.getLanguages());
        addSection(draft, CVSectionType.PROJECTS, "Projects", 5, preview.getProjects());

        return toResponse(cvDraftRepository.save(draft));
    }

    @Override
    @Transactional(readOnly = true)
    public CVDraftResponse getLatestForUser(User user) {
        CVDraft draft = cvDraftRepository.findTopByUser_IdOrderByUpdatedAtDescIdDesc(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CV draft not found"));
        return toResponse(draft);
    }

    @Override
    @Transactional
    public CVDraftResponse updateLatestForUser(User user, UpdateCVDraftRequest request) {
        CVDraft draft = cvDraftRepository.findTopByUser_IdOrderByUpdatedAtDescIdDesc(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CV draft not found"));
        return updateDraft(draft, request);
    }

    @Override
    @Transactional
    public CVDraftResponse updateForUser(User user, Long draftId, UpdateCVDraftRequest request) {
        CVDraft draft = cvDraftRepository.findByIdAndUser_Id(draftId, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CV draft not found"));
        return updateDraft(draft, request);
    }

    private CVDraftResponse updateDraft(CVDraft draft, UpdateCVDraftRequest request) {
        String theme = normalizeTheme(request == null ? null : request.getTheme());
        draft.setTheme(theme);
        draft.setSettingsJson(toJson(buildUpdatedSettings(
                draft,
                draft.getSettingsJson(),
                request == null ? null : request.getSettings(),
                theme)));

        if (request != null && request.getSections() != null) {
            replaceSections(draft, request.getSections());
        }

        return toResponse(cvDraftRepository.save(draft));
    }

    private void addSection(CVDraft draft, CVSectionType type, String title, int orderIndex, Object content) {
        if (content == null) {
            return;
        }

        if (content instanceof List<?> list && list.isEmpty()) {
            return;
        }

        draft.addSection(CVSection.builder()
                .type(type)
                .title(title)
                .orderIndex(orderIndex)
                .visible(true)
                .contentJson(toJson(content))
                .build());
    }

    private Map<String, Object> buildSettings(CVPreviewResponse preview, String theme) {
        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("theme", theme);
        settings.put("showProjectImages", shouldShowProjectImages(theme));
        settings.put("showProfileImage", defaultShowProfileImage(
                preview == null || preview.getProfile() == null ? null : preview.getProfile().getProfilePicture(),
                theme));
        settings.put("estimatedPages", preview.getMeta() == null ? null : preview.getMeta().getEstimatedPages());
        settings.put("exceedsTwoPages", preview.getMeta() != null && preview.getMeta().isExceedsTwoPages());
        return settings;
    }

    private boolean shouldShowProjectImages(String theme) {
        return !"ATS_MINIMAL".equalsIgnoreCase(theme);
    }

    private JsonNode buildUpdatedSettings(CVDraft draft, String existingSettingsJson, JsonNode requestedSettings, String theme) {
        ObjectNode settings = objectMapper.createObjectNode();
        JsonNode baseSettings = requestedSettings != null ? requestedSettings : readJson(existingSettingsJson);
        if (baseSettings != null && baseSettings.isObject()) {
            settings.setAll((ObjectNode) baseSettings);
        }
        settings.put("theme", theme);
        settings.put("showProjectImages", shouldShowProjectImages(theme));
        settings.put("showProfileImage", resolveShowProfileImage(draft, baseSettings, theme));
        return settings;
    }

    private void replaceSections(CVDraft draft, List<UpdateCVSectionRequest> sections) {
        draft.getSections().clear();
        for (UpdateCVSectionRequest section : sections) {
            if (section == null) {
                continue;
            }

            draft.addSection(CVSection.builder()
                    .type(section.getType())
                    .title(section.getTitle())
                    .orderIndex(section.getOrderIndex())
                    .visible(Boolean.TRUE.equals(section.getVisible()))
                    .contentJson(toJson(section.getContent()))
                    .build());
        }
    }

    private String normalizeTheme(String theme) {
        if (theme == null || theme.isBlank()) {
            return DEFAULT_THEME;
        }
        String normalizedTheme = CVTemplateConstants.normalizeTemplateOrAlias(theme);
        return SUPPORTED_THEMES.contains(normalizedTheme) ? normalizedTheme : DEFAULT_THEME;
    }

    private boolean resolveShowProfileImage(CVDraft draft, JsonNode baseSettings, String theme) {
        if (CVTemplateConstants.ATS_MINIMAL.equalsIgnoreCase(theme)) {
            return false;
        }

        JsonNode requestedValue = baseSettings == null ? null : baseSettings.get("showProfileImage");
        if (requestedValue != null && requestedValue.isBoolean()) {
            return requestedValue.asBoolean();
        }

        return defaultShowProfileImage(extractProfilePicture(draft), theme);
    }

    private boolean defaultShowProfileImage(String profilePicture, String theme) {
        if (CVTemplateConstants.ATS_MINIMAL.equalsIgnoreCase(theme)) {
            return false;
        }
        return profilePicture != null && !profilePicture.isBlank();
    }

    private String extractProfilePicture(CVDraft draft) {
        if (draft == null || draft.getSections() == null) {
            return null;
        }

        return draft.getSections().stream()
                .filter(section -> section != null && section.getType() == CVSectionType.PROFILE)
                .map(CVSection::getContentJson)
                .map(this::readJson)
                .map(node -> node == null ? null : node.path("profilePicture").asText(null))
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
    }

    private CVDraftResponse toResponse(CVDraft draft) {
        List<CVSectionResponse> sections = new ArrayList<>();
        for (CVSection section : draft.getSections()) {
            sections.add(CVSectionResponse.builder()
                    .id(section.getId())
                    .type(section.getType())
                    .title(section.getTitle())
                    .orderIndex(section.getOrderIndex())
                    .visible(Boolean.TRUE.equals(section.getVisible()))
                    .content(readJson(section.getContentJson()))
                    .build());
        }

        return CVDraftResponse.builder()
                .id(draft.getId())
                .userId(draft.getUser().getId())
                .portfolioId(draft.getPortfolio() == null ? null : draft.getPortfolio().getId())
                .theme(draft.getTheme())
                .settings(readJson(draft.getSettingsJson()))
                .sections(sections)
                .createdAt(draft.getCreatedAt())
                .updatedAt(draft.getUpdatedAt())
                .build();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize CV draft");
        }
    }

    private JsonNode readJson(String value) {
        try {
            return value == null ? objectMapper.nullNode() : objectMapper.readTree(value);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to deserialize CV draft");
        }
    }
}
