package com.education.platform.services.implementations.admin.jihenportfolio;

import com.education.platform.common.ApiException;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminAiUsageResponse;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminActivityResponse;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminCollectionItemDto;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminCountLabelDto;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminCvUsageResponse;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminFamilyAnalyticsDto;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminOverviewResponse;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminPageResponse;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminPopularProjectDto;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminPortfolioItemDto;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminProjectItemDto;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminRecentItemDto;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminSearchStatDto;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminSkillCategoryDto;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminSkillDto;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminSkillUpsertRequest;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminTrendingSkillDto;
import com.education.platform.dto.portfolio.ai.DeveloperFamily;
import com.education.platform.entities.Role;
import com.education.platform.entities.User;
import com.education.platform.entities.admin.jihenportfolio.AiFeature;
import com.education.platform.entities.admin.jihenportfolio.AiUsageLog;
import com.education.platform.entities.admin.jihenportfolio.ExploreSearchLog;
import com.education.platform.entities.cv.CVDraft;
import com.education.platform.entities.cv.CVProfile;
import com.education.platform.entities.portfolio.CollectionProject;
import com.education.platform.entities.portfolio.ModerationStatus;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.entities.portfolio.PortfolioCollection;
import com.education.platform.entities.portfolio.PortfolioProject;
import com.education.platform.entities.portfolio.Skill;
import com.education.platform.entities.portfolio.SkillCategory;
import com.education.platform.entities.portfolio.Visibility;
import com.education.platform.repositories.admin.jihenportfolio.AiUsageLogRepository;
import com.education.platform.repositories.admin.jihenportfolio.ExploreSearchLogRepository;
import com.education.platform.repositories.cv.CVDraftRepository;
import com.education.platform.repositories.cv.CVProfileRepository;
import com.education.platform.repositories.portfolio.PortfolioCollectionRepository;
import com.education.platform.repositories.portfolio.PortfolioProjectRepository;
import com.education.platform.repositories.portfolio.PortfolioRepository;
import com.education.platform.repositories.portfolio.SkillRepository;
import com.education.platform.services.implementations.portfolio.ai.SkillFamilyWeightMapper;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.admin.jihenportfolio.JihenPortfolioAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Jihen Portfolio Admin
@Service
public class JihenPortfolioAdminServiceImpl implements JihenPortfolioAdminService {

    private final CurrentUserService currentUserService;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioProjectRepository portfolioProjectRepository;
    private final PortfolioCollectionRepository portfolioCollectionRepository;
    private final SkillRepository skillRepository;
    private final CVProfileRepository cvProfileRepository;
    private final CVDraftRepository cvDraftRepository;
    private final AiUsageLogRepository aiUsageLogRepository;
    private final ExploreSearchLogRepository exploreSearchLogRepository;

    public JihenPortfolioAdminServiceImpl(
            CurrentUserService currentUserService,
            PortfolioRepository portfolioRepository,
            PortfolioProjectRepository portfolioProjectRepository,
            PortfolioCollectionRepository portfolioCollectionRepository,
            SkillRepository skillRepository,
            CVProfileRepository cvProfileRepository,
            CVDraftRepository cvDraftRepository,
            AiUsageLogRepository aiUsageLogRepository,
            ExploreSearchLogRepository exploreSearchLogRepository) {
        this.currentUserService = currentUserService;
        this.portfolioRepository = portfolioRepository;
        this.portfolioProjectRepository = portfolioProjectRepository;
        this.portfolioCollectionRepository = portfolioCollectionRepository;
        this.skillRepository = skillRepository;
        this.cvProfileRepository = cvProfileRepository;
        this.cvDraftRepository = cvDraftRepository;
        this.aiUsageLogRepository = aiUsageLogRepository;
        this.exploreSearchLogRepository = exploreSearchLogRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public JihenPortfolioAdminOverviewResponse getOverview() {
        requireAdmin();
        List<Portfolio> portfolios = portfolioRepository.findAll();
        List<PortfolioProject> projects = portfolioProjectRepository.findAll();
        List<PortfolioCollection> collections = portfolioCollectionRepository.findAll();
        List<Skill> skills = skillRepository.findAll();
        List<CVProfile> cvProfiles = cvProfileRepository.findAll();
        List<CVDraft> cvDrafts = cvDraftRepository.findAll();
        List<AiUsageLog> aiLogs = aiUsageLogRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentWindowStart = now.minusDays(30);
        LocalDateTime previousWindowStart = now.minusDays(60);

        return JihenPortfolioAdminOverviewResponse.builder()
                .totalUsersWithPortfolio((long) portfolios.size())
                .totalPortfolios((long) portfolios.size())
                .portfoliosGrowthPercent(growthPercent(
                        countCreatedBetween(portfolios, currentWindowStart, now, Portfolio::getCreatedAt),
                        countCreatedBetween(portfolios, previousWindowStart, currentWindowStart, Portfolio::getCreatedAt)))
                .totalProjects((long) projects.size())
                .projectsGrowthPercent(growthPercent(
                        countCreatedBetween(projects, currentWindowStart, now, PortfolioProject::getCreatedAt),
                        countCreatedBetween(projects, previousWindowStart, currentWindowStart, PortfolioProject::getCreatedAt)))
                .totalCollections((long) collections.size())
                .collectionsGrowthPercent(growthPercent(
                        countCreatedBetween(collections, currentWindowStart, now, PortfolioCollection::getCreatedAt),
                        countCreatedBetween(collections, previousWindowStart, currentWindowStart, PortfolioCollection::getCreatedAt)))
                .totalSkills((long) skills.size())
                .skillsGrowthPercent(growthPercent(
                        countCreatedBetween(skills, currentWindowStart, now, skill -> null),
                        countCreatedBetween(skills, previousWindowStart, currentWindowStart, skill -> null)))
                .usersWithPortfolioGrowthPercent(growthPercent(
                        countCreatedBetween(portfolios, currentWindowStart, now, Portfolio::getCreatedAt),
                        countCreatedBetween(portfolios, previousWindowStart, currentWindowStart, Portfolio::getCreatedAt)))
                .publicPortfolios(countByVisibility(portfolios, Visibility.PUBLIC))
                .friendsOnlyPortfolios(countByVisibility(portfolios, Visibility.FRIENDS_ONLY))
                .privatePortfolios(countByVisibility(portfolios, Visibility.PRIVATE))
                .publicProjects(countByVisibility(projects, Visibility.PUBLIC))
                .friendsOnlyProjects(countByVisibility(projects, Visibility.FRIENDS_ONLY))
                .privateProjects(countByVisibility(projects, Visibility.PRIVATE))
                .publicCollections(countByVisibility(collections, Visibility.PUBLIC))
                .friendsOnlyCollections(countByVisibility(collections, Visibility.FRIENDS_ONLY))
                .privateCollections(countByVisibility(collections, Visibility.PRIVATE))
                .cvProfilesCreated((long) cvProfiles.size())
                .cvDraftsGenerated((long) cvDrafts.size())
                .usersWithCvDraft(cvDrafts.stream().map(draft -> draft.getUser() == null ? null : draft.getUser().getId()).filter(Objects::nonNull).distinct().count())
                .aiCvImproveRequests(countAiFeature(aiLogs, AiFeature.CV_IMPROVE))
                .aiCvChatRequests(countAiFeature(aiLogs, AiFeature.CV_CHAT))
                .aiJobMatchedCvRequests(countAiFeature(aiLogs, AiFeature.JOB_MATCHED_CV))
                .aiPortfolioMentorRequests(countAiFeature(aiLogs, AiFeature.PORTFOLIO_MENTOR))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public JihenPortfolioAdminPageResponse<JihenPortfolioAdminPortfolioItemDto> listPortfolios(
            String q, Visibility visibility, ModerationStatus moderationStatus, Integer page, Integer size, String sort) {
        requireAdmin();
        List<JihenPortfolioAdminPortfolioItemDto> items = portfolioRepository.findAll().stream()
                .filter(portfolio -> visibility == null || portfolio.getVisibility() == visibility)
                .filter(portfolio -> moderationStatus == null || moderationOf(portfolio) == moderationStatus)
                .filter(portfolio -> matchesPortfolioQuery(portfolio, q))
                .map(this::toPortfolioItem)
                .sorted(portfolioItemComparator(sort))
                .toList();
        return page(items, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public JihenPortfolioAdminPageResponse<JihenPortfolioAdminProjectItemDto> listProjects(
            String q, Visibility visibility, ModerationStatus moderationStatus, Integer page, Integer size, String sort) {
        requireAdmin();
        List<JihenPortfolioAdminProjectItemDto> items = portfolioProjectRepository.findAll().stream()
                .filter(project -> visibility == null || project.getVisibility() == visibility)
                .filter(project -> moderationStatus == null || moderationOf(project) == moderationStatus)
                .filter(project -> matchesProjectQuery(project, q))
                .map(this::toProjectItem)
                .sorted(projectItemComparator(sort))
                .toList();
        return page(items, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public JihenPortfolioAdminPageResponse<JihenPortfolioAdminCollectionItemDto> listCollections(
            String q, Visibility visibility, ModerationStatus moderationStatus, Integer page, Integer size, String sort) {
        requireAdmin();
        List<JihenPortfolioAdminCollectionItemDto> items = portfolioCollectionRepository.findAll().stream()
                .filter(collection -> visibility == null || collection.getVisibility() == visibility)
                .filter(collection -> moderationStatus == null || moderationOf(collection) == moderationStatus)
                .filter(collection -> matchesCollectionQuery(collection, q))
                .map(this::toCollectionItem)
                .sorted(collectionItemComparator(sort))
                .toList();
        return page(items, page, size);
    }

    @Override
    @Transactional
    public JihenPortfolioAdminPortfolioItemDto updatePortfolioVisibility(Long portfolioId, Visibility visibility) {
        requireAdmin();
        Portfolio portfolio = requirePortfolio(portfolioId);
        portfolio.setVisibility(visibility);
        return toPortfolioItem(portfolio);
    }

    @Override
    @Transactional
    public JihenPortfolioAdminProjectItemDto updateProjectVisibility(Long projectId, Visibility visibility) {
        requireAdmin();
        PortfolioProject project = requireProject(projectId);
        project.setVisibility(visibility);
        return toProjectItem(project);
    }

    @Override
    @Transactional
    public JihenPortfolioAdminCollectionItemDto updateCollectionVisibility(Long collectionId, Visibility visibility) {
        requireAdmin();
        PortfolioCollection collection = requireCollection(collectionId);
        collection.setVisibility(visibility);
        return toCollectionItem(collection);
    }

    @Override
    @Transactional
    public JihenPortfolioAdminPortfolioItemDto updatePortfolioModeration(Long portfolioId, ModerationStatus status, String reason) {
        requireAdmin();
        Portfolio portfolio = requirePortfolio(portfolioId);
        portfolio.setModerationStatus(status);
        portfolio.setModerationReason(normalizeReason(reason, status));
        return toPortfolioItem(portfolio);
    }

    @Override
    @Transactional
    public JihenPortfolioAdminProjectItemDto updateProjectModeration(Long projectId, ModerationStatus status, String reason) {
        requireAdmin();
        PortfolioProject project = requireProject(projectId);
        project.setModerationStatus(status);
        project.setModerationReason(normalizeReason(reason, status));
        return toProjectItem(project);
    }

    @Override
    @Transactional
    public JihenPortfolioAdminCollectionItemDto updateCollectionModeration(Long collectionId, ModerationStatus status, String reason) {
        requireAdmin();
        PortfolioCollection collection = requireCollection(collectionId);
        collection.setModerationStatus(status);
        collection.setModerationReason(normalizeReason(reason, status));
        return toCollectionItem(collection);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JihenPortfolioAdminSkillDto> listSkills(String q, String category, Boolean trendy) {
        requireAdmin();
        SkillCategory requestedCategory = parseSkillCategory(category);
        return skillRepository.findAll().stream()
                .filter(skill -> requestedCategory == null || skill.getCategory() == requestedCategory)
                .filter(skill -> trendy == null || Objects.equals(Boolean.TRUE.equals(skill.getTrendy()), trendy))
                .filter(skill -> matchesSkillQuery(skill, q))
                .sorted(Comparator.comparing(skill -> normalize(skill.getName())))
                .map(this::toSkillDto)
                .toList();
    }

    @Override
    @Transactional
    public JihenPortfolioAdminSkillDto createSkill(JihenPortfolioAdminSkillUpsertRequest request) {
        requireAdmin();
        String normalizedName = Skill.normalizeName(request.getName());
        skillRepository.findByNormalizedName(normalizedName)
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.CONFLICT, "A skill with this name already exists");
                });
        Skill skill = Skill.builder()
                .name(request.getName().trim())
                .category(request.getCategory())
                .description(trimToNull(request.getDescription()))
                .trendy(Boolean.TRUE.equals(request.getTrendy()))
                .build();
        return toSkillDto(skillRepository.save(skill));
    }

    @Override
    @Transactional
    public JihenPortfolioAdminSkillDto updateSkill(Long skillId, JihenPortfolioAdminSkillUpsertRequest request) {
        requireAdmin();
        Skill skill = requireSkill(skillId);
        String normalizedName = Skill.normalizeName(request.getName());
        skillRepository.findByNormalizedName(normalizedName)
                .filter(existing -> !Objects.equals(existing.getId(), skillId))
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.CONFLICT, "A skill with this name already exists");
                });
        skill.setName(request.getName().trim());
        skill.setCategory(request.getCategory());
        skill.setDescription(trimToNull(request.getDescription()));
        skill.setTrendy(Boolean.TRUE.equals(request.getTrendy()));
        return toSkillDto(skill);
    }

    @Override
    @Transactional
    public void deleteSkill(Long skillId) {
        requireAdmin();
        Skill skill = requireSkill(skillId);
        long portfolioUsage = skill.getPortfolios() == null ? 0 : skill.getPortfolios().size();
        long projectUsage = skill.getProjects() == null ? 0 : skill.getProjects().size();
        if (portfolioUsage > 0 || projectUsage > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Skill is already used by portfolios or projects and cannot be deleted");
        }
        skillRepository.delete(skill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JihenPortfolioAdminSkillCategoryDto> listSkillCategories() {
        requireAdmin();
        return EnumSet.allOf(SkillCategory.class).stream()
                .map(category -> JihenPortfolioAdminSkillCategoryDto.builder()
                        .value(category.name())
                        .label(formatCategoryLabel(category))
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JihenPortfolioAdminFamilyAnalyticsDto> getFamilyAnalytics() {
        requireAdmin();
        List<Portfolio> portfolios = portfolioRepository.findAll();
        List<PortfolioProject> projects = portfolioProjectRepository.findAll();

        Map<DeveloperFamily, Long> portfolioCounts = portfolios.stream()
                .collect(Collectors.groupingBy(this::detectPortfolioFamily, () -> new java.util.EnumMap<>(DeveloperFamily.class), Collectors.counting()));
        Map<DeveloperFamily, Long> projectCounts = projects.stream()
                .collect(Collectors.groupingBy(this::detectProjectFamily, () -> new java.util.EnumMap<>(DeveloperFamily.class), Collectors.counting()));

        long total = Math.max(1L, portfolios.size());
        return Arrays.stream(DeveloperFamily.values())
                .map(family -> JihenPortfolioAdminFamilyAnalyticsDto.builder()
                        .family(family)
                        .label(formatFamilyLabel(family))
                        .portfolioCount(portfolioCounts.getOrDefault(family, 0L))
                        .projectCount(projectCounts.getOrDefault(family, 0L))
                        .percentage((int) Math.round((portfolioCounts.getOrDefault(family, 0L) * 100.0) / total))
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JihenPortfolioAdminTrendingSkillDto> getTrendingSkills(Integer limit) {
        requireAdmin();
        int effectiveLimit = positiveOrDefault(limit, 10);
        return skillRepository.findAll().stream()
                .map(this::toTrendingSkillDto)
                .sorted(Comparator
                        .comparing(JihenPortfolioAdminTrendingSkillDto::getTotalUsageCount, Comparator.reverseOrder())
                        .thenComparing(dto -> Boolean.TRUE.equals(dto.getTrendy()) ? 0 : 1)
                        .thenComparing(JihenPortfolioAdminTrendingSkillDto::getProjectUsageCount, Comparator.reverseOrder())
                        .thenComparing(JihenPortfolioAdminTrendingSkillDto::getSkillName, String.CASE_INSENSITIVE_ORDER))
                .limit(effectiveLimit)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JihenPortfolioAdminPopularProjectDto> getPopularProjects(Integer limit) {
        requireAdmin();
        int effectiveLimit = positiveOrDefault(limit, 10);
        return portfolioProjectRepository.findAll().stream()
                .map(this::toPopularProjectDto)
                .sorted(Comparator
                        .comparing((JihenPortfolioAdminPopularProjectDto dto) -> dto.getViews() + dto.getLikes(), Comparator.reverseOrder())
                        .thenComparing(JihenPortfolioAdminPopularProjectDto::getViews, Comparator.reverseOrder())
                        .thenComparing(JihenPortfolioAdminPopularProjectDto::getLikes, Comparator.reverseOrder()))
                .limit(effectiveLimit)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public JihenPortfolioAdminCvUsageResponse getCvUsage() {
        requireAdmin();
        List<CVProfile> profiles = cvProfileRepository.findAll();
        List<CVDraft> drafts = cvDraftRepository.findAll();

        List<JihenPortfolioAdminCountLabelDto> templates = drafts.stream()
                .filter(Objects::nonNull)
                .map(CVDraft::getTheme)
                .filter(this::hasText)
                .collect(Collectors.groupingBy(theme -> theme.trim(), LinkedHashMap::new, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
                .map(entry -> JihenPortfolioAdminCountLabelDto.builder().template(entry.getKey()).count(entry.getValue()).build())
                .toList();

        List<JihenPortfolioAdminCountLabelDto> languages = profiles.stream()
                .filter(Objects::nonNull)
                .map(CVProfile::getLanguage)
                .filter(this::hasText)
                .collect(Collectors.groupingBy(language -> language.trim().toUpperCase(Locale.ROOT), LinkedHashMap::new, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
                .map(entry -> JihenPortfolioAdminCountLabelDto.builder().language(entry.getKey()).count(entry.getValue()).build())
                .toList();

        return JihenPortfolioAdminCvUsageResponse.builder()
                .cvProfilesCreated((long) profiles.size())
                .cvDraftsGenerated((long) drafts.size())
                .usersWithCvDraft(drafts.stream().map(draft -> draft.getUser() == null ? null : draft.getUser().getId()).filter(Objects::nonNull).distinct().count())
                .mostUsedTemplates(templates)
                .preferredLanguages(languages)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public JihenPortfolioAdminAiUsageResponse getAiUsage() {
        requireAdmin();
        List<AiUsageLog> logs = aiUsageLogRepository.findAll();
        return JihenPortfolioAdminAiUsageResponse.builder()
                .portfolioMentorRequests(countAiFeature(logs, AiFeature.PORTFOLIO_MENTOR))
                .cvImproveRequests(countAiFeature(logs, AiFeature.CV_IMPROVE))
                .cvChatRequests(countAiFeature(logs, AiFeature.CV_CHAT))
                .jobMatchedCvRequests(countAiFeature(logs, AiFeature.JOB_MATCHED_CV))
                .failedAiRequests(logs.stream().filter(log -> Boolean.FALSE.equals(log.getSuccess())).count())
                .successfulAiRequests(logs.stream().filter(log -> Boolean.TRUE.equals(log.getSuccess())).count())
                .averageResponseTimeMs(averageResponseTime(logs))
                .averagePortfolioMentorResponseTimeMs(averageResponseTime(filterAi(logs, AiFeature.PORTFOLIO_MENTOR)))
                .averageCvResponseTimeMs(averageResponseTime(logs.stream()
                        .filter(log -> log.getFeature() == AiFeature.CV_IMPROVE || log.getFeature() == AiFeature.CV_CHAT || log.getFeature() == AiFeature.JOB_MATCHED_CV)
                        .toList()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JihenPortfolioAdminSearchStatDto> getSearchAnalytics() {
        requireAdmin();
        return exploreSearchLogRepository.findAll().stream()
                .map(ExploreSearchLog::getQuery)
                .filter(this::hasText)
                .map(String::trim)
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry::getKey, String.CASE_INSENSITIVE_ORDER))
                .map(entry -> JihenPortfolioAdminSearchStatDto.builder()
                        .keyword(entry.getKey())
                        .searchCount(entry.getValue())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public JihenPortfolioAdminActivityResponse getActivity(String range) {
        requireAdmin();
        int days = parseRangeDays(range);
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(days - 1L);
        List<String> labels = new ArrayList<>();
        List<Long> portfolioCounts = new ArrayList<>();
        List<Long> projectCounts = new ArrayList<>();
        List<Long> collectionCounts = new ArrayList<>();
        List<Portfolio> portfolios = portfolioRepository.findAll();
        List<PortfolioProject> projects = portfolioProjectRepository.findAll();
        List<PortfolioCollection> collections = portfolioCollectionRepository.findAll();

        for (int offset = 0; offset < days; offset++) {
            LocalDate day = start.plusDays(offset);
            labels.add(day.toString());
            portfolioCounts.add(countCreatedOnDay(portfolios, day, Portfolio::getCreatedAt));
            projectCounts.add(countCreatedOnDay(projects, day, PortfolioProject::getCreatedAt));
            collectionCounts.add(countCreatedOnDay(collections, day, PortfolioCollection::getCreatedAt));
        }

        return JihenPortfolioAdminActivityResponse.builder()
                .labels(labels)
                .portfolios(portfolioCounts)
                .projects(projectCounts)
                .collections(collectionCounts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JihenPortfolioAdminRecentItemDto> getRecentItems(Integer limit) {
        requireAdmin();
        int effectiveLimit = positiveOrDefault(limit, 10);
        List<JihenPortfolioAdminRecentItemDto> items = new ArrayList<>();
        portfolioRepository.findAll().forEach(portfolio -> items.add(toRecentPortfolioItem(portfolio)));
        portfolioProjectRepository.findAll().forEach(project -> items.add(toRecentProjectItem(project)));
        portfolioCollectionRepository.findAll().forEach(collection -> items.add(toRecentCollectionItem(collection)));
        return items.stream()
                .sorted(Comparator.comparing(JihenPortfolioAdminRecentItemDto::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(JihenPortfolioAdminRecentItemDto::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(effectiveLimit)
                .toList();
    }

    private void requireAdmin() {
        User currentUser = currentUserService.getCurrentUser();
        // TODO Jihen Portfolio Admin: switch to method-security once JWT authorities are fully mapped.
        if (currentUser.getRole() != Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin access is required");
        }
    }

    private Portfolio requirePortfolio(Long portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Portfolio not found"));
    }

    private PortfolioProject requireProject(Long projectId) {
        return portfolioProjectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private PortfolioCollection requireCollection(Long collectionId) {
        return portfolioCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Collection not found"));
    }

    private Skill requireSkill(Long skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Skill not found"));
    }

    private long countAiFeature(List<AiUsageLog> logs, AiFeature feature) {
        return logs.stream().filter(log -> log.getFeature() == feature).count();
    }

    private long averageResponseTime(List<AiUsageLog> logs) {
        return Math.round(logs.stream()
                .map(AiUsageLog::getResponseTimeMs)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .average()
                .orElse(0));
    }

    private List<AiUsageLog> filterAi(List<AiUsageLog> logs, AiFeature feature) {
        return logs.stream().filter(log -> log.getFeature() == feature).toList();
    }

    private <T> JihenPortfolioAdminPageResponse<T> page(List<T> items, Integer page, Integer size) {
        int safePage = Math.max(0, page == null ? 0 : page);
        int safeSize = Math.max(1, size == null ? 20 : size);
        int from = Math.min(items.size(), safePage * safeSize);
        int to = Math.min(items.size(), from + safeSize);
        int totalPages = items.isEmpty() ? 0 : (int) Math.ceil(items.size() / (double) safeSize);
        return JihenPortfolioAdminPageResponse.<T>builder()
                .items(items.subList(from, to))
                .total((long) items.size())
                .page(safePage)
                .size(safeSize)
                .totalPages(totalPages)
                .build();
    }

    private long countByVisibility(Collection<? extends Object> items, Visibility visibility) {
        return items.stream().filter(item -> {
            if (item instanceof Portfolio portfolio) {
                return portfolio.getVisibility() == visibility;
            }
            if (item instanceof PortfolioProject project) {
                return project.getVisibility() == visibility;
            }
            if (item instanceof PortfolioCollection collection) {
                return collection.getVisibility() == visibility;
            }
            return false;
        }).count();
    }

    private JihenPortfolioAdminPortfolioItemDto toPortfolioItem(Portfolio portfolio) {
        return JihenPortfolioAdminPortfolioItemDto.builder()
                .id(portfolio.getId())
                .ownerId(portfolio.getUser() == null ? null : portfolio.getUser().getId())
                .ownerName(displayName(portfolio.getUser()))
                .ownerUsername(portfolio.getUser() == null ? null : portfolio.getUser().getUsername())
                .title(portfolio.getTitle())
                .job(portfolio.getJob())
                .bio(portfolio.getBio())
                .visibility(portfolio.getVisibility())
                .moderationStatus(moderationOf(portfolio))
                .moderationReason(portfolio.getModerationReason())
                .views(portfolio.getTotalViews() == null ? 0L : portfolio.getTotalViews())
                .projectCount(portfolio.getProjects() == null ? 0 : portfolio.getProjects().size())
                .collectionCount(portfolio.getCollections() == null ? 0 : portfolio.getCollections().size())
                .skillCount(portfolio.getSkills() == null ? 0 : portfolio.getSkills().size())
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }

    private JihenPortfolioAdminProjectItemDto toProjectItem(PortfolioProject project) {
        Portfolio portfolio = project.getPortfolio();
        return JihenPortfolioAdminProjectItemDto.builder()
                .id(project.getId())
                .portfolioId(portfolio == null ? null : portfolio.getId())
                .portfolioTitle(portfolio == null ? null : portfolio.getTitle())
                .ownerId(portfolio == null || portfolio.getUser() == null ? null : portfolio.getUser().getId())
                .ownerName(displayName(portfolio == null ? null : portfolio.getUser()))
                .title(project.getTitle())
                .description(project.getDescription())
                .visibility(project.getVisibility())
                .moderationStatus(moderationOf(project))
                .moderationReason(project.getModerationReason())
                .views(project.getViews() == null ? 0 : project.getViews())
                .likes(project.getLikes() == null ? 0 : project.getLikes())
                .skillNames(skillNames(project.getSkills()))
                .skillIcons(List.of())
                .mediaCount(project.getMedia() == null ? 0 : project.getMedia().size())
                .mediaThumbnailUrl(mediaThumbnailUrl(project))
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    private JihenPortfolioAdminCollectionItemDto toCollectionItem(PortfolioCollection collection) {
        Portfolio portfolio = collection.getPortfolio();
        return JihenPortfolioAdminCollectionItemDto.builder()
                .id(collection.getId())
                .portfolioId(portfolio == null ? null : portfolio.getId())
                .ownerId(portfolio == null || portfolio.getUser() == null ? null : portfolio.getUser().getId())
                .ownerName(displayName(portfolio == null ? null : portfolio.getUser()))
                .name(collection.getName())
                .description(collection.getDescription())
                .visibility(collection.getVisibility())
                .moderationStatus(moderationOf(collection))
                .moderationReason(collection.getModerationReason())
                .projectCount(collection.getCollectionProjects() == null ? 0 : collection.getCollectionProjects().size())
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .build();
    }

    private JihenPortfolioAdminSkillDto toSkillDto(Skill skill) {
        long portfolioUsage = skill.getPortfolios() == null ? 0 : skill.getPortfolios().size();
        long projectUsage = skill.getProjects() == null ? 0 : skill.getProjects().size();
        return JihenPortfolioAdminSkillDto.builder()
                .id(skill.getId())
                .name(skill.getName())
                .category(skill.getCategory())
                .description(skill.getDescription())
                .trendy(Boolean.TRUE.equals(skill.getTrendy()))
                .portfolioUsageCount(portfolioUsage)
                .projectUsageCount(projectUsage)
                .totalUsageCount(portfolioUsage + projectUsage)
                .build();
    }

    private JihenPortfolioAdminTrendingSkillDto toTrendingSkillDto(Skill skill) {
        long portfolioUsage = skill.getPortfolios() == null ? 0 : skill.getPortfolios().size();
        long projectUsage = skill.getProjects() == null ? 0 : skill.getProjects().size();
        return JihenPortfolioAdminTrendingSkillDto.builder()
                .skillId(skill.getId())
                .skillName(skill.getName())
                .category(skill.getCategory())
                .portfolioUsageCount(portfolioUsage)
                .projectUsageCount(projectUsage)
                .totalUsageCount(portfolioUsage + projectUsage)
                .trendy(Boolean.TRUE.equals(skill.getTrendy()))
                .build();
    }

    private JihenPortfolioAdminPopularProjectDto toPopularProjectDto(PortfolioProject project) {
        Portfolio portfolio = project.getPortfolio();
        return JihenPortfolioAdminPopularProjectDto.builder()
                .projectId(project.getId())
                .title(project.getTitle())
                .ownerName(displayName(portfolio == null ? null : portfolio.getUser()))
                .views(project.getViews() == null ? 0 : project.getViews())
                .likes(project.getLikes() == null ? 0 : project.getLikes())
                .visibility(project.getVisibility())
                .skillNames(skillNames(project.getSkills()))
                .build();
    }

    private JihenPortfolioAdminRecentItemDto toRecentPortfolioItem(Portfolio portfolio) {
        User owner = portfolio.getUser();
        return JihenPortfolioAdminRecentItemDto.builder()
                .type("PORTFOLIO")
                .id(portfolio.getId())
                .title(portfolio.getTitle())
                .subtitle(portfolio.getJob())
                .ownerName(displayName(owner))
                .ownerUsername(owner == null ? null : owner.getUsername())
                .visibility(portfolio.getVisibility())
                .moderationStatus(moderationOf(portfolio))
                .views(portfolio.getTotalViews() == null ? 0L : portfolio.getTotalViews())
                .createdAt(portfolio.getCreatedAt())
                .build();
    }

    private JihenPortfolioAdminRecentItemDto toRecentProjectItem(PortfolioProject project) {
        Portfolio portfolio = project.getPortfolio();
        User owner = portfolio == null ? null : portfolio.getUser();
        return JihenPortfolioAdminRecentItemDto.builder()
                .type("PROJECT")
                .id(project.getId())
                .title(project.getTitle())
                .subtitle(portfolio == null ? null : portfolio.getTitle())
                .ownerName(displayName(owner))
                .ownerUsername(owner == null ? null : owner.getUsername())
                .visibility(project.getVisibility())
                .moderationStatus(moderationOf(project))
                .views(project.getViews() == null ? 0L : project.getViews().longValue())
                .createdAt(project.getCreatedAt())
                .build();
    }

    private JihenPortfolioAdminRecentItemDto toRecentCollectionItem(PortfolioCollection collection) {
        Portfolio portfolio = collection.getPortfolio();
        User owner = portfolio == null ? null : portfolio.getUser();
        long views = collection.getCollectionProjects() == null ? 0L : collection.getCollectionProjects().stream()
                .map(CollectionProject::getProject)
                .filter(Objects::nonNull)
                .map(PortfolioProject::getViews)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
        return JihenPortfolioAdminRecentItemDto.builder()
                .type("COLLECTION")
                .id(collection.getId())
                .title(collection.getName())
                .subtitle(collection.getDescription())
                .ownerName(displayName(owner))
                .ownerUsername(owner == null ? null : owner.getUsername())
                .visibility(collection.getVisibility())
                .moderationStatus(moderationOf(collection))
                .views(views)
                .createdAt(collection.getCreatedAt())
                .build();
    }

    private boolean matchesPortfolioQuery(Portfolio portfolio, String q) {
        if (!hasText(q)) {
            return true;
        }
        return containsAny(q,
                portfolio.getTitle(),
                portfolio.getJob(),
                portfolio.getBio(),
                portfolio.getUser() == null ? null : portfolio.getUser().getUsername(),
                fullName(portfolio.getUser()),
                skillNamesJoined(portfolio.getSkills()));
    }

    private boolean matchesProjectQuery(PortfolioProject project, String q) {
        if (!hasText(q)) {
            return true;
        }
        Portfolio portfolio = project.getPortfolio();
        return containsAny(q,
                project.getTitle(),
                project.getDescription(),
                displayName(portfolio == null ? null : portfolio.getUser()),
                portfolio == null ? null : portfolio.getTitle(),
                skillNamesJoined(project.getSkills()));
    }

    private boolean matchesCollectionQuery(PortfolioCollection collection, String q) {
        if (!hasText(q)) {
            return true;
        }
        Portfolio portfolio = collection.getPortfolio();
        return containsAny(q,
                collection.getName(),
                collection.getDescription(),
                displayName(portfolio == null ? null : portfolio.getUser()),
                projectTitlesJoined(collection),
                skillNamesJoined(skillsFromCollection(collection)));
    }

    private boolean matchesSkillQuery(Skill skill, String q) {
        if (!hasText(q)) {
            return true;
        }
        return containsAny(q, skill.getName(), skill.getDescription(), formatCategoryLabel(skill.getCategory()));
    }

    private boolean containsAny(String q, String... values) {
        String normalizedQuery = normalize(q);
        for (String value : values) {
            if (normalize(value).contains(normalizedQuery)) {
                return true;
            }
        }
        return false;
    }

    private String projectTitlesJoined(PortfolioCollection collection) {
        if (collection.getCollectionProjects() == null) {
            return "";
        }
        return collection.getCollectionProjects().stream()
                .map(CollectionProject::getProject)
                .filter(Objects::nonNull)
                .map(PortfolioProject::getTitle)
                .filter(this::hasText)
                .collect(Collectors.joining(" "));
    }

    private Set<Skill> skillsFromCollection(PortfolioCollection collection) {
        if (collection.getCollectionProjects() == null) {
            return Set.of();
        }
        return collection.getCollectionProjects().stream()
                .map(CollectionProject::getProject)
                .filter(Objects::nonNull)
                .flatMap(project -> project.getSkills() == null ? Stream.<Skill>empty() : project.getSkills().stream())
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<String> skillNames(Collection<Skill> skills) {
        if (skills == null) {
            return List.of();
        }
        return skills.stream()
                .filter(Objects::nonNull)
                .map(Skill::getName)
                .filter(this::hasText)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private String skillNamesJoined(Collection<Skill> skills) {
        return String.join(" ", skillNames(skills));
    }

    private DeveloperFamily detectPortfolioFamily(Portfolio portfolio) {
        Set<SkillCategory> categories = Stream.concat(
                        portfolio.getSkills() == null ? Stream.<Skill>empty() : portfolio.getSkills().stream(),
                        portfolio.getProjects() == null ? Stream.<Skill>empty() : portfolio.getProjects().stream()
                                .filter(Objects::nonNull)
                                .flatMap(project -> project.getSkills() == null ? Stream.<Skill>empty() : project.getSkills().stream()))
                .filter(Objects::nonNull)
                .map(Skill::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(SkillCategory.class)));
        return SkillFamilyWeightMapper.dominantFamily(categories);
    }

    private DeveloperFamily detectProjectFamily(PortfolioProject project) {
        Set<SkillCategory> categories = project.getSkills() == null
                ? Set.<SkillCategory>of()
                : project.getSkills().stream()
                .filter(Objects::nonNull)
                .map(Skill::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(SkillCategory.class)));
        return SkillFamilyWeightMapper.dominantFamily(categories);
    }

    private String displayName(User user) {
        String fullName = fullName(user);
        return hasText(fullName) ? fullName : (user == null ? null : user.getUsername());
    }

    private String fullName(User user) {
        if (user == null || user.getProfile() == null) {
            return user == null ? null : user.getUsername();
        }
        String fullName = Stream.of(user.getProfile().getFirstName(), user.getProfile().getLastName())
                .filter(this::hasText)
                .map(String::trim)
                .collect(Collectors.joining(" "));
        return hasText(fullName) ? fullName : user.getUsername();
    }

    private ModerationStatus moderationOf(Portfolio portfolio) {
        return portfolio.getModerationStatus() == null ? ModerationStatus.ACTIVE : portfolio.getModerationStatus();
    }

    private ModerationStatus moderationOf(PortfolioProject project) {
        return project.getModerationStatus() == null ? ModerationStatus.ACTIVE : project.getModerationStatus();
    }

    private ModerationStatus moderationOf(PortfolioCollection collection) {
        return collection.getModerationStatus() == null ? ModerationStatus.ACTIVE : collection.getModerationStatus();
    }

    private String normalizeReason(String reason, ModerationStatus status) {
        return status == ModerationStatus.ACTIVE ? null : trimToNull(reason);
    }

    private String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private SkillCategory parseSkillCategory(String category) {
        if (!hasText(category)) {
            return null;
        }
        return SkillCategory.valueOf(category.trim().toUpperCase(Locale.ROOT));
    }

    private int positiveOrDefault(Integer value, int fallback) {
        return value == null || value <= 0 ? fallback : value;
    }

    private int parseRangeDays(String range) {
        if (!hasText(range)) {
            return 30;
        }
        String normalized = range.trim().toLowerCase(Locale.ROOT);
        if (normalized.endsWith("d")) {
            try {
                int days = Integer.parseInt(normalized.substring(0, normalized.length() - 1));
                return days > 0 ? days : 30;
            } catch (NumberFormatException ignored) {
                return 30;
            }
        }
        return 30;
    }

    private <T> long countCreatedBetween(
            Collection<T> items,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive,
            Function<T, LocalDateTime> extractor) {
        return items.stream()
                .filter(Objects::nonNull)
                .map(extractor)
                .filter(Objects::nonNull)
                .filter(createdAt -> !createdAt.isBefore(startInclusive) && createdAt.isBefore(endExclusive))
                .count();
    }

    private <T> long countCreatedOnDay(Collection<T> items, LocalDate day, Function<T, LocalDateTime> extractor) {
        return items.stream()
                .filter(Objects::nonNull)
                .map(extractor)
                .filter(Objects::nonNull)
                .map(LocalDateTime::toLocalDate)
                .filter(day::equals)
                .count();
    }

    private double growthPercent(long current, long previous) {
        if (previous <= 0) {
            return current <= 0 ? 0.0 : 100.0;
        }
        return ((current - previous) * 100.0) / previous;
    }

    private String mediaThumbnailUrl(PortfolioProject project) {
        if (project.getMedia() == null) {
            return null;
        }
        return project.getMedia().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing((com.education.platform.entities.portfolio.ProjectMedia media) -> media.getOrderIndex() == null ? Integer.MAX_VALUE : media.getOrderIndex())
                        .thenComparing(media -> media.getId() == null ? Long.MAX_VALUE : media.getId()))
                .map(com.education.platform.entities.portfolio.ProjectMedia::getMediaUrl)
                .filter(this::hasText)
                .findFirst()
                .orElse(null);
    }

    private Comparator<JihenPortfolioAdminPortfolioItemDto> portfolioItemComparator(String sort) {
        String normalized = normalize(sort);
        return switch (normalized) {
            case "most_viewed", "views" -> Comparator.comparing(JihenPortfolioAdminPortfolioItemDto::getViews, Comparator.reverseOrder());
            case "oldest" -> Comparator.comparing(JihenPortfolioAdminPortfolioItemDto::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(JihenPortfolioAdminPortfolioItemDto::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
        };
    }

    private Comparator<JihenPortfolioAdminProjectItemDto> projectItemComparator(String sort) {
        String normalized = normalize(sort);
        return switch (normalized) {
            case "most_viewed", "views" -> Comparator.comparing(JihenPortfolioAdminProjectItemDto::getViews, Comparator.reverseOrder());
            case "most_liked", "likes" -> Comparator.comparing(JihenPortfolioAdminProjectItemDto::getLikes, Comparator.reverseOrder());
            case "oldest" -> Comparator.comparing(JihenPortfolioAdminProjectItemDto::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(JihenPortfolioAdminProjectItemDto::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
        };
    }

    private Comparator<JihenPortfolioAdminCollectionItemDto> collectionItemComparator(String sort) {
        String normalized = normalize(sort);
        return switch (normalized) {
            case "largest", "project_count" -> Comparator.comparing(JihenPortfolioAdminCollectionItemDto::getProjectCount, Comparator.reverseOrder());
            case "oldest" -> Comparator.comparing(JihenPortfolioAdminCollectionItemDto::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(JihenPortfolioAdminCollectionItemDto::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
        };
    }

    private String formatCategoryLabel(SkillCategory category) {
        if (category == null) {
            return "";
        }
        String[] parts = category.name().toLowerCase(Locale.ROOT).split("_");
        List<String> formatted = new ArrayList<>();
        for (String part : parts) {
            if (!part.isBlank()) {
                formatted.add(Character.toUpperCase(part.charAt(0)) + part.substring(1));
            }
        }
        return String.join(" ", formatted);
    }

    private String formatFamilyLabel(DeveloperFamily family) {
        if (family == null) {
            return "General";
        }
        String lower = family.name().toLowerCase(Locale.ROOT).replace("_", " ");
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
