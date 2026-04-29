package com.education.platform.services.implementations.cv;

import com.education.platform.dto.cv.CVEducationDto;
import com.education.platform.dto.cv.CVExperienceDto;
import com.education.platform.dto.cv.CVLanguageDto;
import com.education.platform.dto.cv.CVPreviewMetaDto;
import com.education.platform.dto.cv.CVPreviewOptions;
import com.education.platform.dto.cv.CVPreviewProfileDto;
import com.education.platform.dto.cv.CVPreviewProjectDto;
import com.education.platform.dto.cv.CVPreviewResponse;
import com.education.platform.dto.cv.CVPreviewSkillGroupDto;
import com.education.platform.dto.cv.CVPreviewSkillDto;
import com.education.platform.entities.Profile;
import com.education.platform.entities.User;
import com.education.platform.entities.cv.CVEducationEntry;
import com.education.platform.entities.cv.CVExperienceEntry;
import com.education.platform.entities.cv.CVLanguageEntry;
import com.education.platform.entities.cv.CVProfile;
import com.education.platform.entities.portfolio.CollectionProject;
import com.education.platform.entities.portfolio.MediaType;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.entities.portfolio.PortfolioProject;
import com.education.platform.entities.portfolio.Skill;
import com.education.platform.entities.portfolio.Visibility;
import com.education.platform.repositories.portfolio.PortfolioProjectRepository;
import com.education.platform.repositories.portfolio.PortfolioRepository;
import com.education.platform.services.interfaces.cv.CVBuilderService;
import com.education.platform.services.interfaces.cv.CVProfileService;
import com.education.platform.services.interfaces.cv.CVTemplateConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CVBuilderServiceImpl implements CVBuilderService {

    private static final String DEFAULT_LANGUAGE = "en";

    private static final Comparator<Skill> SKILL_ORDER =
            Comparator.comparing(CVBuilderServiceImpl::categoryNameForSort)
                    .thenComparing(skill -> safeString(skill.getName()), String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(skill -> skill.getId() == null ? Long.MAX_VALUE : skill.getId());

    private static final Comparator<PortfolioProject> PROJECT_ORDER =
            Comparator.comparing(CVBuilderServiceImpl::projectSortDate, Comparator.nullsLast(Comparator.reverseOrder()));

    private final PortfolioRepository portfolioRepository;
    private final PortfolioProjectRepository portfolioProjectRepository;
    private final CVProfileService cvProfileService;

    public CVBuilderServiceImpl(
            PortfolioRepository portfolioRepository,
            PortfolioProjectRepository portfolioProjectRepository,
            CVProfileService cvProfileService) {
        this.portfolioRepository = portfolioRepository;
        this.portfolioProjectRepository = portfolioProjectRepository;
        this.cvProfileService = cvProfileService;
    }

    @Override
    @Transactional(readOnly = true)
    public CVPreviewResponse buildForUser(User user) {
        return buildForUser(user, CVPreviewOptions.builder().build());
    }

    @Override
    @Transactional(readOnly = true)
    public CVPreviewResponse buildForUser(User user, CVPreviewOptions options) {
        Optional<CVProfile> cvProfile = cvProfileService.findEntityForUser(user);
        Optional<Portfolio> portfolio = portfolioRepository.findByUser_Id(user.getId());
        List<Long> selectedProjectIds = resolveSelectedProjectIds(cvProfile.orElse(null));
        List<PortfolioProject> projects = portfolio
                .map(value -> portfolioProjectRepository.findByPortfolio_IdOrderByCreatedAtDesc(value.getId()))
                .orElseGet(List::of)
                .stream()
                .filter(Objects::nonNull)
                .filter(project -> belongsToPortfolio(project, portfolio.orElse(null)))
                .filter(project -> isSelectedProject(project, selectedProjectIds))
                .collect(Collectors.toMap(
                        project -> project.getId() == null ? "object-" + System.identityHashCode(project) : "id-" + project.getId(),
                        project -> project,
                        (left, right) -> left,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .filter(this::hasProjectContent)
                .sorted(projectComparator(selectedProjectIds))
                .limit(resolveProjectLimit(options))
                .toList();

        List<CVPreviewSkillGroupDto> skillsByCategory = buildSkillsByCategory(portfolio.orElse(null), projects);
        List<CVPreviewProjectDto> projectDtos = buildProjects(projects);

        return CVPreviewResponse.builder()
                .profile(buildProfile(user, portfolio.orElse(null), cvProfile.orElse(null), options))
                .skillsByCategory(skillsByCategory.isEmpty() ? null : skillsByCategory)
                .projects(projectDtos.isEmpty() ? null : projectDtos)
                .education(buildEducation(cvProfile.orElse(null)))
                .experience(buildExperience(cvProfile.orElse(null)))
                .languages(buildLanguages(cvProfile.orElse(null)))
                .meta(buildMeta(cvProfile.orElse(null), projectDtos))
                .build();
    }

    private CVPreviewProfileDto buildProfile(User user, Portfolio portfolio, CVProfile cvProfile, CVPreviewOptions options) {
        Profile profile = user.getProfile();

        return CVPreviewProfileDto.builder()
                .fullName(buildFullName(user, profile))
                .email(defaultIfBlank(user.getEmail(), null))
                .phone(cvProfile == null ? null : defaultIfBlank(cvProfile.getPhone(), null))
                .location(cvProfile == null ? null : defaultIfBlank(cvProfile.getLocation(), null))
                .profilePicture(profile == null ? null : profile.getProfilePicture())
                .headline(firstNonBlank(
                        cvProfile == null ? null : cvProfile.getHeadline(),
                        portfolio == null ? null : portfolio.getJob(),
                        portfolio == null ? null : portfolio.getTitle()))
                .summary(firstNonBlank(
                        cvProfile == null ? null : cvProfile.getSummary(),
                        portfolio == null ? null : portfolio.getBio(),
                        profile == null ? null : profile.getDescription()))
                .githubUrl(portfolio == null ? null : defaultIfBlank(portfolio.getGithubUrl(), null))
                .linkedInUrl(portfolio == null ? null : defaultIfBlank(portfolio.getLinkedinUrl(), null))
                .preferredTemplate(resolveTemplate(cvProfile, options))
                .language(resolveLanguage(cvProfile, options))
                .visibility(resolveVisibility(cvProfile))
                .build();
    }

    private List<CVPreviewSkillGroupDto> buildSkillsByCategory(Portfolio portfolio, List<PortfolioProject> projects) {
        Set<Skill> uniqueSkills = new LinkedHashSet<>();

        if (portfolio != null) {
            uniqueSkills.addAll(safeSkills(portfolio.getSkills()));
        }
        for (PortfolioProject project : projects) {
            uniqueSkills.addAll(safeSkills(project.getSkills()));
        }

        Map<String, List<CVPreviewSkillDto>> grouped = uniqueSkills.stream()
                .filter(Objects::nonNull)
                .sorted(SKILL_ORDER)
                .collect(LinkedHashMap::new,
                        (map, skill) -> map
                                .computeIfAbsent(categoryName(skill), ignored -> new java.util.ArrayList<>())
                                .add(toSkillDto(skill)),
                        Map::putAll);

        return grouped.entrySet().stream()
                .map(entry -> CVPreviewSkillGroupDto.builder()
                        .category(entry.getKey())
                        .skills(entry.getValue().isEmpty() ? null : entry.getValue())
                        .build())
                .filter(group -> group.getSkills() != null && !group.getSkills().isEmpty())
                .toList();
    }

    private List<CVPreviewProjectDto> buildProjects(List<PortfolioProject> projects) {
        return projects.stream()
                .filter(Objects::nonNull)
                .map(project -> CVPreviewProjectDto.builder()
                        .id(project.getId())
                        .title(project.getTitle())
                        .description(cleanProjectDescription(project.getDescription()))
                        .projectUrl(project.getProjectUrl())
                        .visibility(project.getVisibility())
                        .createdAt(project.getCreatedAt())
                        .updatedAt(project.getUpdatedAt())
                        .imageUrl(resolveImageUrl(project))
                        .collectionName(resolveCollectionName(project))
                        .skills(toSkillDtos(project.getSkills()))
                        .build())
                .filter(this::hasProjectContent)
                .toList();
    }

    private List<CVEducationDto> buildEducation(CVProfile cvProfile) {
        if (cvProfile == null || cvProfile.getEducation() == null) {
            return null;
        }
        List<CVEducationDto> values = cvProfile.getEducation().stream()
                .filter(this::hasEducationContent)
                .map(entry -> CVEducationDto.builder()
                        .school(entry.getSchool())
                        .degree(entry.getDegree())
                        .fieldOfStudy(entry.getFieldOfStudy())
                        .location(entry.getLocation())
                        .startDate(entry.getStartDate())
                        .endDate(entry.getEndDate())
                        .current(entry.getCurrent())
                        .description(entry.getDescription())
                        .build())
                .toList();
        return values.isEmpty() ? null : values;
    }

    private List<CVExperienceDto> buildExperience(CVProfile cvProfile) {
        if (cvProfile == null || cvProfile.getExperience() == null) {
            return null;
        }
        List<CVExperienceDto> values = cvProfile.getExperience().stream()
                .filter(this::hasExperienceContent)
                .map(entry -> CVExperienceDto.builder()
                        .company(entry.getCompany())
                        .role(entry.getRole())
                        .location(entry.getLocation())
                        .startDate(entry.getStartDate())
                        .endDate(entry.getEndDate())
                        .current(entry.getCurrent())
                        .summary(entry.getSummary())
                        .build())
                .toList();
        return values.isEmpty() ? null : values;
    }

    private List<CVLanguageDto> buildLanguages(CVProfile cvProfile) {
        if (cvProfile == null || cvProfile.getLanguages() == null) {
            return null;
        }
        List<CVLanguageDto> values = cvProfile.getLanguages().stream()
                .filter(this::hasLanguageContent)
                .map(entry -> CVLanguageDto.builder()
                        .name(entry.getName())
                        .proficiency(entry.getProficiency())
                        .build())
                .toList();
        return values.isEmpty() ? null : values;
    }

    private CVPreviewMetaDto buildMeta(CVProfile cvProfile, List<CVPreviewProjectDto> projectDtos) {
        int estimatedPages = estimatePages(cvProfile, projectDtos);
        boolean exceedsTwoPages = estimatedPages > 2;
        return CVPreviewMetaDto.builder()
                .estimatedPages(estimatedPages)
                .exceedsTwoPages(exceedsTwoPages)
                .warning(exceedsTwoPages ? "This CV may exceed 2 pages." : null)
                .build();
    }

    private List<CVPreviewSkillDto> toSkillDtos(Collection<Skill> skills) {
        List<CVPreviewSkillDto> values = skills == null
                ? List.of()
                : skills.stream()
                .filter(Objects::nonNull)
                .sorted(SKILL_ORDER)
                .map(this::toSkillDto)
                .toList();
        return values.isEmpty() ? null : values;
    }

    private CVPreviewSkillDto toSkillDto(Skill skill) {
        return CVPreviewSkillDto.builder()
                .id(skill.getId())
                .name(skill.getName())
                .category(skill.getCategory())
                .build();
    }

    private String buildFullName(User user, Profile profile) {
        String fullName = joinNonBlank(
                profile == null ? null : profile.getFirstName(),
                profile == null ? null : profile.getLastName());
        return firstNonBlank(fullName, user.getUsername());
    }

    private static LocalDateTime projectSortDate(PortfolioProject project) {
        return project.getCreatedAt();
    }

    private static String categoryName(Skill skill) {
        return skill.getCategory() == null ? "OTHER" : skill.getCategory().name();
    }

    private static String categoryNameForSort(Skill skill) {
        return categoryName(skill);
    }

    private static String safeString(String value) {
        return value == null ? "" : value;
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String resolveTemplate(CVProfile cvProfile, CVPreviewOptions options) {
        String requestedTemplate = options == null ? null : options.getTemplate();
        if (requestedTemplate != null && !requestedTemplate.isBlank()) {
            return CVTemplateConstants.normalizeTemplate(requestedTemplate);
        }
        return CVTemplateConstants.normalizeTemplate(cvProfile == null ? null : cvProfile.getPreferredTemplate());
    }

    private String resolveLanguage(CVProfile cvProfile, CVPreviewOptions options) {
        String requestedLanguage = options == null ? null : options.getLanguage();
        if (requestedLanguage != null && !requestedLanguage.isBlank()) {
            return requestedLanguage.trim();
        }
        return defaultIfBlank(cvProfile == null ? null : cvProfile.getLanguage(), DEFAULT_LANGUAGE);
    }

    private long resolveProjectLimit(CVPreviewOptions options) {
        if (options == null || options.getProjectLimit() == null || options.getProjectLimit() <= 0) {
            return Long.MAX_VALUE;
        }
        return options.getProjectLimit();
    }

    private List<Long> resolveSelectedProjectIds(CVProfile cvProfile) {
        if (cvProfile == null || cvProfile.getSelectedProjectIds() == null) {
            return List.of();
        }
        return cvProfile.getSelectedProjectIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private Comparator<PortfolioProject> projectComparator(List<Long> selectedProjectIds) {
        if (selectedProjectIds != null && !selectedProjectIds.isEmpty()) {
            Map<Long, Integer> order = new LinkedHashMap<>();
            for (int i = 0; i < selectedProjectIds.size(); i++) {
                order.putIfAbsent(selectedProjectIds.get(i), i);
            }
            return Comparator
                    .comparingInt((PortfolioProject project) -> order.getOrDefault(project.getId(), Integer.MAX_VALUE))
                    .thenComparing(PROJECT_ORDER);
        }
        return PROJECT_ORDER;
    }

    private Visibility resolveVisibility(CVProfile cvProfile) {
        return cvProfile == null || cvProfile.getVisibility() == null ? Visibility.PRIVATE : cvProfile.getVisibility();
    }

    private Set<Skill> safeSkills(Collection<Skill> skills) {
        if (skills == null || skills.isEmpty()) {
            return Set.of();
        }

        return skills.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean belongsToPortfolio(PortfolioProject project, Portfolio portfolio) {
        if (project == null || portfolio == null || project.getPortfolio() == null) {
            return true;
        }
        Long expectedPortfolioId = portfolio.getId();
        Long actualPortfolioId = project.getPortfolio().getId();
        if (expectedPortfolioId == null || actualPortfolioId == null) {
            return project.getPortfolio() == portfolio;
        }
        return expectedPortfolioId.equals(actualPortfolioId);
    }

    private boolean isSelectedProject(PortfolioProject project, List<Long> selectedProjectIds) {
        if (selectedProjectIds == null || selectedProjectIds.isEmpty()) {
            return true;
        }
        return project.getId() != null && selectedProjectIds.contains(project.getId());
    }

    private boolean hasProjectContent(PortfolioProject project) {
        if (project == null) {
            return false;
        }
        return hasText(project.getTitle())
                || hasText(cleanProjectDescription(project.getDescription()))
                || !safeSkills(project.getSkills()).isEmpty()
                || hasText(project.getProjectUrl())
                || resolveImageUrl(project) != null
                || resolveCollectionName(project) != null;
    }

    private boolean hasProjectContent(CVPreviewProjectDto project) {
        if (project == null) {
            return false;
        }
        return hasText(project.getTitle())
                || hasText(project.getDescription())
                || (project.getSkills() != null && !project.getSkills().isEmpty())
                || hasText(project.getProjectUrl())
                || hasText(project.getImageUrl())
                || hasText(project.getCollectionName());
    }

    private boolean hasEducationContent(CVEducationEntry entry) {
        return entry != null && (
                hasText(entry.getSchool())
                        || hasText(entry.getDegree())
                        || hasText(entry.getFieldOfStudy())
                        || hasText(entry.getDescription())
                        || entry.getStartDate() != null
                        || entry.getEndDate() != null
        );
    }

    private boolean hasExperienceContent(CVExperienceEntry entry) {
        return entry != null && (
                hasText(entry.getCompany())
                        || hasText(entry.getRole())
                        || hasText(entry.getSummary())
                        || entry.getStartDate() != null
                        || entry.getEndDate() != null
        );
    }

    private boolean hasLanguageContent(CVLanguageEntry entry) {
        return entry != null && (hasText(entry.getName()) || hasText(entry.getProficiency()));
    }

    private String cleanProjectDescription(String description) {
        String cleaned = defaultIfBlank(description, null);
        if (cleaned == null) {
            return null;
        }
        return "No project summary provided".equalsIgnoreCase(cleaned.trim()) ? null : cleaned;
    }

    private int estimatePages(CVProfile cvProfile, List<CVPreviewProjectDto> projects) {
        int score = 1;
        if (cvProfile != null) {
            score += sizeOf(cvProfile.getEducation());
            score += sizeOf(cvProfile.getExperience()) * 2;
            score += Math.max(0, sizeOf(cvProfile.getLanguages()) / 3);
            score += textWeight(cvProfile.getSummary());
        }
        score += projects == null ? 0 : projects.stream()
                .mapToInt(project -> 1 + textWeight(project.getDescription()) + (project.getSkills() == null ? 0 : Math.max(0, project.getSkills().size() / 4)))
                .sum();
        return Math.max(1, (int) Math.ceil(score / 6.0));
    }

    private int textWeight(String text) {
        if (!hasText(text)) {
            return 0;
        }
        return Math.max(1, text.trim().length() / 300);
    }

    private int sizeOf(List<?> values) {
        return values == null ? 0 : values.size();
    }

    private String resolveImageUrl(PortfolioProject project) {
        if (project == null || project.getMedia() == null) {
            return null;
        }

        return project.getMedia().stream()
                .filter(Objects::nonNull)
                .filter(media -> media.getMediaType() == MediaType.IMAGE)
                .sorted(Comparator
                        .comparing((com.education.platform.entities.portfolio.ProjectMedia media) -> media.getOrderIndex() == null ? Integer.MAX_VALUE : media.getOrderIndex())
                        .thenComparing(media -> media.getCreatedAt(), Comparator.nullsLast(Comparator.naturalOrder())))
                .map(com.education.platform.entities.portfolio.ProjectMedia::getMediaUrl)
                .filter(this::hasText)
                .findFirst()
                .orElse(null);
    }

    private String resolveCollectionName(PortfolioProject project) {
        if (project == null || project.getCollectionProjects() == null) {
            return null;
        }

        return project.getCollectionProjects().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(CollectionProject::getAddedDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(CollectionProject::getPortfolioCollection)
                .filter(Objects::nonNull)
                .map(com.education.platform.entities.portfolio.PortfolioCollection::getName)
                .filter(this::hasText)
                .findFirst()
                .orElse(null);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String joinNonBlank(String... values) {
        return java.util.Arrays.stream(values)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .reduce((left, right) -> left + " " + right)
                .orElse(null);
    }
}
