package com.education.platform.services.implementations.explore;

import com.education.platform.common.ApiException;
import com.education.platform.dto.explore.ExploreCollectionDetailResponse;
import com.education.platform.dto.explore.ExploreCollectionMiniResponse;
import com.education.platform.dto.explore.ExploreCollectionResponse;
import com.education.platform.dto.explore.ExploreOptionDto;
import com.education.platform.dto.explore.ExplorePortfolioCardResponse;
import com.education.platform.dto.explore.ExplorePortfolioDetailResponse;
import com.education.platform.dto.explore.ExploreProjectCardResponse;
import com.education.platform.dto.explore.ExploreProjectDetailResponse;
import com.education.platform.dto.explore.ExploreProjectMiniResponse;
import com.education.platform.dto.explore.ExploreSkillMiniDto;
import com.education.platform.dto.explore.ExploreSort;
import com.education.platform.dto.explore.ExploreVisibilityFilter;
import com.education.platform.dto.portfolio.ProjectMediaResponse;
import com.education.platform.dto.portfolio.SkillSummaryDto;
import com.education.platform.dto.portfolio.ai.DeveloperFamily;
import com.education.platform.entities.Profile;
import com.education.platform.entities.User;
import com.education.platform.entities.portfolio.CollectionProject;
import com.education.platform.entities.portfolio.MediaType;
import com.education.platform.entities.portfolio.ModerationStatus;
import com.education.platform.entities.portfolio.Portfolio;
import com.education.platform.entities.portfolio.PortfolioCollection;
import com.education.platform.entities.portfolio.PortfolioProject;
import com.education.platform.entities.portfolio.ProjectMedia;
import com.education.platform.entities.portfolio.Skill;
import com.education.platform.entities.portfolio.SkillCategory;
import com.education.platform.entities.portfolio.Visibility;
import com.education.platform.repositories.UserRepository;
import com.education.platform.repositories.portfolio.PortfolioCollectionRepository;
import com.education.platform.repositories.portfolio.PortfolioProjectRepository;
import com.education.platform.repositories.portfolio.PortfolioRepository;
import com.education.platform.repositories.portfolio.SkillRepository;
import com.education.platform.services.implementations.portfolio.PortfolioMapper;
import com.education.platform.services.interfaces.explore.ExploreService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ExploreServiceImpl implements ExploreService {

    private static final Comparator<Skill> SKILL_ORDER =
            Comparator.comparing(skill -> skill.getName() == null ? "" : skill.getName(), String.CASE_INSENSITIVE_ORDER);

    private static final Comparator<ProjectMedia> MEDIA_ORDER =
            Comparator.comparing((ProjectMedia media) -> media.getOrderIndex() == null ? Integer.MAX_VALUE : media.getOrderIndex())
                    .thenComparing(media -> media.getId() == null ? Long.MAX_VALUE : media.getId());

    private static final Comparator<PortfolioProject> PROJECT_ORDER =
            Comparator.comparing(PortfolioProject::getPinned, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(PortfolioProject::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(PortfolioProject::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(PortfolioProject::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private static final Comparator<PortfolioCollection> COLLECTION_ORDER =
            Comparator.comparing(PortfolioCollection::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(PortfolioCollection::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(PortfolioCollection::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private static final Comparator<CollectionProject> COLLECTION_LINK_ORDER =
            Comparator.comparing(CollectionProject::getAddedDate, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(link -> link.getOrderIndex() == null ? Integer.MAX_VALUE : link.getOrderIndex())
                    .thenComparing(link -> link.getId() == null ? Long.MAX_VALUE : link.getId());

    private final PortfolioRepository portfolioRepository;
    private final PortfolioProjectRepository portfolioProjectRepository;
    private final PortfolioCollectionRepository portfolioCollectionRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final PortfolioMapper portfolioMapper;

    public ExploreServiceImpl(
            PortfolioRepository portfolioRepository,
            PortfolioProjectRepository portfolioProjectRepository,
            PortfolioCollectionRepository portfolioCollectionRepository,
            SkillRepository skillRepository,
            UserRepository userRepository,
            PortfolioMapper portfolioMapper) {
        this.portfolioRepository = portfolioRepository;
        this.portfolioProjectRepository = portfolioProjectRepository;
        this.portfolioCollectionRepository = portfolioCollectionRepository;
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
        this.portfolioMapper = portfolioMapper;
    }

    @Override
    public List<ExploreOptionDto> listFamilies() {
        return ExploreSearchSupport.familyOptions();
    }

    @Override
    public List<ExploreOptionDto> listSkillCategories() {
        return ExploreSearchSupport.categoryOptions();
    }

    @Override
    public List<SkillSummaryDto> searchSkills(User currentUser, String query) {
        if (currentUser == null || currentUser.getId() == null) {
            return List.of();
        }
        List<Skill> skills = ExploreSearchSupport.hasText(query)
                ? skillRepository.searchByNamePrefixOrContains(query.trim())
                : skillRepository.findAll().stream()
                .sorted(Comparator.comparing(skill -> skill.getName() == null ? "" : skill.getName(), String.CASE_INSENSITIVE_ORDER))
                .limit(50)
                .toList();
        return skills.stream()
                .map(portfolioMapper::toSkillSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExplorePortfolioCardResponse> searchPortfolios(
            User currentUser,
            DeveloperFamily family,
            SkillCategory category,
            List<Long> skillIds,
            String query,
            String jobTitle,
            ExploreVisibilityFilter visibility,
            ExploreSort sort) {
        Set<Long> selectedSkillIds = normalizeSkillIds(skillIds);
        List<String> jobKeywords = ExploreSearchSupport.keywordsForJobTitle(jobTitle);

        return portfolioRepository.findAll().stream()
                .filter(Objects::nonNull)
                .filter(portfolio -> canSeePortfolioForFilter(currentUser, portfolio, visibility))
                .filter(portfolio -> matchesFamily(portfolio, family, currentUser))
                .filter(portfolio -> matchesCategory(portfolio, category, currentUser))
                .filter(portfolio -> matchesSkillIds(portfolio, selectedSkillIds, currentUser))
                .filter(portfolio -> matchesQuery(portfolio, query, currentUser))
                .filter(portfolio -> matchesJobTitle(portfolio, jobTitle, jobKeywords, currentUser))
                .sorted(portfolioComparator(sort, selectedSkillIds, query, jobKeywords, currentUser))
                .map(portfolio -> toPortfolioCard(portfolio, currentUser))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExploreProjectCardResponse> searchProjects(
            User currentUser,
            DeveloperFamily family,
            SkillCategory category,
            List<Long> skillIds,
            String query,
            ExploreVisibilityFilter visibility,
            ExploreSort sort) {
        Set<Long> selectedSkillIds = normalizeSkillIds(skillIds);

        return portfolioProjectRepository.findAll().stream()
                .filter(Objects::nonNull)
                .filter(project -> project.getPortfolio() != null)
                .filter(project -> canSeeProjectForFilter(currentUser, project, visibility))
                .filter(project -> matchesFamily(project, family))
                .filter(project -> matchesCategory(project, category))
                .filter(project -> matchesSkillIds(project, selectedSkillIds))
                .filter(project -> matchesQuery(project, query))
                .sorted(projectComparator(sort, selectedSkillIds, query))
                .map(this::toProjectCard)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExploreCollectionResponse> searchCollections(
            User currentUser,
            DeveloperFamily family,
            SkillCategory category,
            List<Long> skillIds,
            String query,
            ExploreVisibilityFilter visibility,
            ExploreSort sort) {
        Set<Long> selectedSkillIds = normalizeSkillIds(skillIds);

        return portfolioCollectionRepository.findAll().stream()
                .filter(Objects::nonNull)
                .filter(collection -> canSeeCollectionForFilter(currentUser, collection, visibility))
                .filter(collection -> matchesFamily(collection, family, currentUser))
                .filter(collection -> matchesCategory(collection, category, currentUser))
                .filter(collection -> matchesSkillIds(collection, selectedSkillIds, currentUser))
                .filter(collection -> matchesQuery(collection, query, currentUser))
                .sorted(collectionComparator(sort, selectedSkillIds, query, currentUser))
                .map(collection -> toCollectionResponse(collection, currentUser))
                .toList();
    }

    @Override
    @Transactional
    public ExplorePortfolioDetailResponse getPortfolioDetail(User currentUser, Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Portfolio introuvable"));
        if (!canSeePortfolio(currentUser, portfolio)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Acces refuse a ce portfolio");
        }

        portfolio.setTotalViews((portfolio.getTotalViews() == null ? 0L : portfolio.getTotalViews()) + 1);
        List<PortfolioProject> visibleProjects = visibleProjects(portfolio, currentUser);
        List<PortfolioCollection> visibleCollections = visibleCollections(portfolio, currentUser);

        return ExplorePortfolioDetailResponse.builder()
                .id(portfolio.getId())
                .ownerId(portfolio.getUser() == null ? null : portfolio.getUser().getId())
                .username(portfolio.getUser() == null ? null : portfolio.getUser().getUsername())
                .fullName(fullName(portfolio.getUser()))
                .avatarUrl(profilePicture(portfolio.getUser()))
                .profileImageUrl(profilePicture(portfolio.getUser()))
                .title(portfolio.getTitle())
                .job(portfolio.getJob())
                .bio(portfolio.getBio())
                .location(null)
                .githubUrl(portfolio.getGithubUrl())
                .linkedinUrl(portfolio.getLinkedinUrl())
                .openToWork(portfolio.isOpenToWork())
                .availableForFreelance(portfolio.isAvailableForFreelance())
                .visibility(portfolio.getVisibility())
                .totalViews(portfolio.getTotalViews() == null ? 0L : portfolio.getTotalViews())
                .projectCount(visibleProjects.size())
                .collectionCount(visibleCollections.size())
                .skills(toExploreSkillMiniList(portfolio.getSkills()))
                .projects(visibleProjects.stream().map(this::toProjectMini).toList())
                .collections(visibleCollections.stream().map(collection -> toCollectionMini(collection, currentUser)).toList())
                .isFriend(isFriendWithOwner(currentUser, portfolio.getUser()))
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public ExploreProjectDetailResponse getProjectDetail(User currentUser, Long projectId) {
        PortfolioProject project = portfolioProjectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Projet introuvable"));
        if (!canSeeProject(currentUser, project)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Acces refuse a ce projet");
        }

        project.setViews((project.getViews() == null ? 0 : project.getViews()) + 1);
        Portfolio portfolio = project.getPortfolio();
        User owner = portfolio == null ? null : portfolio.getUser();
        String imageUrl = firstMediaUrl(project, MediaType.IMAGE);
        String videoUrl = firstMediaUrl(project, MediaType.VIDEO);
        String thumbnailUrl = thumbnailUrl(project);
        return ExploreProjectDetailResponse.builder()
                .id(project.getId())
                .portfolioId(portfolio == null ? null : portfolio.getId())
                .ownerId(owner == null ? null : owner.getId())
                .ownerName(displayName(owner))
                .ownerAvatarUrl(profilePicture(owner))
                .ownerJob(portfolio == null ? null : portfolio.getJob())
                .title(project.getTitle())
                .description(project.getDescription())
                .projectUrl(project.getProjectUrl())
                .imageUrl(imageUrl)
                .videoUrl(videoUrl)
                .thumbnailUrl(thumbnailUrl)
                .visibility(project.getVisibility())
                .views(project.getViews() == null ? 0 : project.getViews())
                .likes(project.getLikes() == null ? 0 : project.getLikes())
                .media(project.getMedia().stream().filter(Objects::nonNull).sorted(MEDIA_ORDER).map(this::toProjectMediaResponse).toList())
                .skills(toExploreSkillMiniList(project.getSkills()))
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ExploreCollectionDetailResponse getCollectionDetail(User currentUser, Long collectionId) {
        PortfolioCollection collection = portfolioCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Collection introuvable"));
        if (!canSeeCollection(currentUser, collection)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Acces refuse a cette collection");
        }

        Portfolio portfolio = collection.getPortfolio();
        User owner = portfolio == null ? null : portfolio.getUser();
        List<PortfolioProject> visibleProjects = visibleProjects(collection, currentUser);

        return ExploreCollectionDetailResponse.builder()
                .id(collection.getId())
                .portfolioId(portfolio == null ? null : portfolio.getId())
                .ownerId(owner == null ? null : owner.getId())
                .ownerName(displayName(owner))
                .ownerAvatarUrl(profilePicture(owner))
                .ownerJob(portfolio == null ? null : portfolio.getJob())
                .name(collection.getName())
                .description(collection.getDescription())
                .visibility(collection.getVisibility())
                .projectCount(visibleProjects.size())
                .projects(visibleProjects.stream().map(this::toProjectMini).toList())
                .skills(toExploreSkillMiniList(skillsFromProjects(visibleProjects)))
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExploreCollectionResponse> getPortfolioCollections(User currentUser, Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Portfolio introuvable"));
        if (!canSeePortfolio(currentUser, portfolio)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Acces refuse a ce portfolio");
        }
        return visibleCollections(portfolio, currentUser).stream()
                .map(collection -> toCollectionResponse(collection, currentUser))
                .toList();
    }

    private ExplorePortfolioCardResponse toPortfolioCard(Portfolio portfolio, User currentUser) {
        Profile profile = portfolio.getUser() == null ? null : portfolio.getUser().getProfile();
        List<PortfolioProject> visibleProjects = visibleProjects(portfolio, currentUser);
        DeveloperFamily family = ExploreSearchSupport.detectFamily(skillsForPortfolio(portfolio, currentUser));
        return ExplorePortfolioCardResponse.builder()
                .portfolioId(portfolio.getId())
                .ownerId(portfolio.getUser() == null ? null : portfolio.getUser().getId())
                .ownerUsername(portfolio.getUser() == null ? null : portfolio.getUser().getUsername())
                .displayName(displayName(portfolio.getUser()))
                .profileImage(profile == null ? null : profile.getProfilePicture())
                .location(null)
                .portfolioTitle(portfolio.getTitle())
                .bio(portfolio.getBio())
                .jobTitle(portfolio.getJob())
                .family(family)
                .visibility(portfolio.getVisibility())
                .projectCount(visibleProjects.size())
                .totalViews(portfolio.getTotalViews() == null ? 0L : portfolio.getTotalViews())
                .totalLikes(totalLikes(visibleProjects))
                .githubUrl(portfolio.getGithubUrl())
                .linkedinUrl(portfolio.getLinkedinUrl())
                .topSkills(topSkills(portfolio.getSkills(), 6))
                .build();
    }

    private ExploreProjectCardResponse toProjectCard(PortfolioProject project) {
        Portfolio portfolio = project.getPortfolio();
        User owner = portfolio == null ? null : portfolio.getUser();
        Profile profile = owner == null ? null : owner.getProfile();
        ProjectMedia coverMedia = firstMedia(project);

        return ExploreProjectCardResponse.builder()
                .projectId(project.getId())
                .portfolioId(portfolio == null ? null : portfolio.getId())
                .ownerId(owner == null ? null : owner.getId())
                .ownerUsername(owner == null ? null : owner.getUsername())
                .ownerDisplayName(displayName(owner))
                .ownerProfileImage(profile == null ? null : profile.getProfilePicture())
                .title(project.getTitle())
                .description(project.getDescription())
                .projectUrl(project.getProjectUrl())
                .mediaUrl(coverMedia == null ? null : coverMedia.getMediaUrl())
                .imageUrl(firstMediaUrl(project, MediaType.IMAGE))
                .videoUrl(firstMediaUrl(project, MediaType.VIDEO))
                .thumbnailUrl(thumbnailUrl(project))
                .mediaType(coverMedia == null ? null : coverMedia.getMediaType())
                .family(detectFamilyForProject(project))
                .visibility(project.getVisibility())
                .views(project.getViews() == null ? 0 : project.getViews())
                .likes(project.getLikes() == null ? 0 : project.getLikes())
                .topSkills(topSkills(project.getSkills(), 6))
                .build();
    }

    private ExploreCollectionResponse toCollectionResponse(PortfolioCollection collection, User currentUser) {
        Portfolio portfolio = collection.getPortfolio();
        User owner = portfolio == null ? null : portfolio.getUser();
        List<PortfolioProject> visibleProjects = visibleProjects(collection, currentUser);
        return ExploreCollectionResponse.builder()
                .id(collection.getId())
                .portfolioId(portfolio == null ? null : portfolio.getId())
                .ownerId(owner == null ? null : owner.getId())
                .ownerName(displayName(owner))
                .ownerAvatarUrl(profilePicture(owner))
                .name(collection.getName())
                .description(collection.getDescription())
                .visibility(collection.getVisibility())
                .projectCount(visibleProjects.size())
                .coverMediaUrl(coverMediaUrl(visibleProjects))
                .skills(toExploreSkillMiniList(skillsFromProjects(visibleProjects)))
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .build();
    }

    private boolean canSeePortfolio(User currentUser, Portfolio portfolio) {
        return portfolio != null
                && canSeeModeration(currentUser, portfolio.getUser(), portfolio.getModerationStatus())
                && canSeeVisibility(currentUser, portfolio.getUser(), portfolio.getVisibility());
    }

    private boolean canSeeProject(User currentUser, PortfolioProject project) {
        if (project == null || project.getPortfolio() == null) {
            return false;
        }
        return canSeeModeration(currentUser, project.getPortfolio().getUser(), project.getModerationStatus())
                && canSeeVisibility(currentUser, project.getPortfolio().getUser(), project.getVisibility());
    }

    private boolean canSeeCollection(User currentUser, PortfolioCollection collection) {
        if (collection == null || collection.getPortfolio() == null) {
            return false;
        }
        return canSeeModeration(currentUser, collection.getPortfolio().getUser(), collection.getModerationStatus())
                && canSeeVisibility(currentUser, collection.getPortfolio().getUser(), collection.getVisibility());
    }

    private boolean canSeePortfolioForFilter(User currentUser, Portfolio portfolio, ExploreVisibilityFilter visibilityFilter) {
        return portfolio != null
                && isListedModeration(portfolio.getModerationStatus())
                && matchesVisibilityFilter(currentUser, portfolio.getUser(), portfolio.getVisibility(), visibilityFilter);
    }

    private boolean canSeeProjectForFilter(User currentUser, PortfolioProject project, ExploreVisibilityFilter visibilityFilter) {
        if (project == null || project.getPortfolio() == null) {
            return false;
        }
        return isListedModeration(project.getModerationStatus())
                && matchesVisibilityFilter(currentUser, project.getPortfolio().getUser(), project.getVisibility(), visibilityFilter);
    }

    private boolean canSeeCollectionForFilter(User currentUser, PortfolioCollection collection, ExploreVisibilityFilter visibilityFilter) {
        if (collection == null || collection.getPortfolio() == null) {
            return false;
        }
        return isListedModeration(collection.getModerationStatus())
                && matchesVisibilityFilter(currentUser, collection.getPortfolio().getUser(), collection.getVisibility(), visibilityFilter);
    }

    private boolean isListedModeration(ModerationStatus status) {
        return effectiveModeration(status) == ModerationStatus.ACTIVE;
    }

    private boolean canSeeModeration(User currentUser, User owner, ModerationStatus status) {
        return isOwner(currentUser, owner) || effectiveModeration(status) == ModerationStatus.ACTIVE;
    }

    private ModerationStatus effectiveModeration(ModerationStatus status) {
        return status == null ? ModerationStatus.ACTIVE : status;
    }

    private boolean matchesVisibilityFilter(User currentUser, User owner, Visibility contentVisibility, ExploreVisibilityFilter visibilityFilter) {
        if (contentVisibility == null) {
            return false;
        }
        boolean ownerView = isOwner(currentUser, owner);
        boolean friendView = isFriendWithOwner(currentUser, owner);
        ExploreVisibilityFilter effectiveFilter = visibilityFilter == null ? ExploreVisibilityFilter.PUBLIC_AND_FRIENDS : visibilityFilter;
        return switch (effectiveFilter) {
            case PRIVATE -> contentVisibility == Visibility.PRIVATE && ownerView;
            case PUBLIC -> contentVisibility == Visibility.PUBLIC;
            case FRIENDS -> contentVisibility == Visibility.FRIENDS_ONLY && (ownerView || friendView);
            case PUBLIC_AND_FRIENDS -> contentVisibility == Visibility.PUBLIC
                    || (contentVisibility == Visibility.FRIENDS_ONLY && (ownerView || friendView));
        };
    }

    private boolean canSeeVisibility(User currentUser, User owner, Visibility visibility) {
        if (visibility == null || currentUser == null || currentUser.getId() == null || owner == null || owner.getId() == null) {
            return false;
        }
        if (isOwner(currentUser, owner)) {
            return true;
        }
        return switch (visibility) {
            case PUBLIC -> true;
            case FRIENDS_ONLY -> isFriendWithOwner(currentUser, owner);
            case PRIVATE -> false;
        };
    }

    private boolean isOwner(User currentUser, User owner) {
        return currentUser != null
                && owner != null
                && currentUser.getId() != null
                && owner.getId() != null
                && Objects.equals(currentUser.getId(), owner.getId());
    }

    private boolean isFriendWithOwner(User currentUser, User owner) {
        if (currentUser == null || owner == null || currentUser.getId() == null || owner.getId() == null) {
            return false;
        }
        if (Objects.equals(currentUser.getId(), owner.getId())) {
            return false;
        }
        return userRepository.areFriends(currentUser.getId(), owner.getId());
    }

    private boolean matchesFamily(Portfolio portfolio, DeveloperFamily family, User currentUser) {
        if (family == null || family == DeveloperFamily.GENERAL) {
            return true;
        }
        return ExploreSearchSupport.detectFamily(skillsForPortfolio(portfolio, currentUser)) == family;
    }

    private boolean matchesFamily(PortfolioProject project, DeveloperFamily family) {
        if (family == null || family == DeveloperFamily.GENERAL) {
            return true;
        }
        return detectFamilyForProject(project) == family;
    }

    private boolean matchesFamily(PortfolioCollection collection, DeveloperFamily family, User currentUser) {
        if (family == null || family == DeveloperFamily.GENERAL) {
            return true;
        }
        return ExploreSearchSupport.detectFamily(skillsFromProjects(visibleProjects(collection, currentUser))) == family;
    }

    private DeveloperFamily detectFamilyForPortfolio(Portfolio portfolio) {
        return ExploreSearchSupport.detectFamily(Stream.concat(
                portfolio.getSkills() == null ? Stream.<Skill>empty() : portfolio.getSkills().stream(),
                portfolio.getProjects() == null
                        ? Stream.<Skill>empty()
                        : portfolio.getProjects().stream()
                        .filter(Objects::nonNull)
                        .flatMap(project -> project.getSkills() == null ? Stream.<Skill>empty() : project.getSkills().stream())
        ).filter(Objects::nonNull).toList());
    }

    private DeveloperFamily detectFamilyForProject(PortfolioProject project) {
        return ExploreSearchSupport.detectFamily(project.getSkills());
    }

    private boolean matchesCategory(Portfolio portfolio, SkillCategory category, User currentUser) {
        if (category == null) {
            return true;
        }
        return skillsForPortfolio(portfolio, currentUser).stream()
                .filter(Objects::nonNull)
                .anyMatch(skill -> category == skill.getCategory());
    }

    private boolean matchesCategory(PortfolioProject project, SkillCategory category) {
        if (category == null) {
            return true;
        }
        return project.getSkills() != null && project.getSkills().stream()
                .filter(Objects::nonNull)
                .anyMatch(skill -> category == skill.getCategory());
    }

    private boolean matchesCategory(PortfolioCollection collection, SkillCategory category, User currentUser) {
        if (category == null) {
            return true;
        }
        return skillsFromProjects(visibleProjects(collection, currentUser)).stream()
                .filter(Objects::nonNull)
                .anyMatch(skill -> category == skill.getCategory());
    }

    private boolean matchesSkillIds(Portfolio portfolio, Set<Long> selectedSkillIds, User currentUser) {
        if (selectedSkillIds.isEmpty()) {
            return true;
        }
        Set<Long> skillIds = skillsForPortfolio(portfolio, currentUser).stream()
                .map(Skill::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return selectedSkillIds.stream().anyMatch(skillIds::contains);
    }

    private boolean matchesSkillIds(PortfolioProject project, Set<Long> selectedSkillIds) {
        if (selectedSkillIds.isEmpty()) {
            return true;
        }
        Set<Long> skillIds = project.getSkills() == null
                ? Set.of()
                : project.getSkills().stream()
                .filter(Objects::nonNull)
                .map(Skill::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return selectedSkillIds.stream().anyMatch(skillIds::contains);
    }

    private boolean matchesSkillIds(PortfolioCollection collection, Set<Long> selectedSkillIds, User currentUser) {
        if (selectedSkillIds.isEmpty()) {
            return true;
        }
        Set<Long> skillIds = skillsFromProjects(visibleProjects(collection, currentUser)).stream()
                .map(Skill::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return selectedSkillIds.stream().anyMatch(skillIds::contains);
    }

    private boolean matchesQuery(Portfolio portfolio, String query, User currentUser) {
        if (!ExploreSearchSupport.hasText(query)) {
            return true;
        }
        return ExploreSearchSupport.matchesAnyPrefixOrContains(
                query,
                portfolio.getTitle(),
                portfolio.getJob(),
                portfolio.getSkills() == null ? null : portfolio.getSkills().stream()
                        .map(Skill::getName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" ")),
                visibleProjects(portfolio, currentUser).stream()
                        .map(PortfolioProject::getTitle)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" "))
        );
    }

    private boolean matchesQuery(PortfolioProject project, String query) {
        if (!ExploreSearchSupport.hasText(query)) {
            return true;
        }
        Portfolio portfolio = project.getPortfolio();
        return ExploreSearchSupport.matchesAnyPrefixOrContains(
                query,
                project.getTitle(),
                portfolio == null ? null : portfolio.getTitle(),
                portfolio == null ? null : portfolio.getJob(),
                project.getSkills() == null ? null : project.getSkills().stream()
                        .map(Skill::getName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" "))
        );
    }

    private boolean matchesQuery(PortfolioCollection collection, String query, User currentUser) {
        if (!ExploreSearchSupport.hasText(query)) {
            return true;
        }
        Portfolio portfolio = collection.getPortfolio();
        User owner = portfolio == null ? null : portfolio.getUser();
        List<PortfolioProject> visibleProjects = visibleProjects(collection, currentUser);
        return ExploreSearchSupport.matchesAnyPrefixOrContains(
                query,
                collection.getName(),
                collection.getDescription(),
                owner == null ? null : owner.getUsername(),
                fullName(owner),
                visibleProjects.stream().map(PortfolioProject::getTitle).filter(Objects::nonNull).collect(Collectors.joining(" ")),
                visibleProjects.stream().map(PortfolioProject::getDescription).filter(Objects::nonNull).collect(Collectors.joining(" ")),
                skillsFromProjects(visibleProjects).stream().map(Skill::getName).filter(Objects::nonNull).collect(Collectors.joining(" "))
        );
    }

    private boolean matchesJobTitle(Portfolio portfolio, String jobTitle, List<String> keywords, User currentUser) {
        if (!ExploreSearchSupport.hasText(jobTitle)) {
            return true;
        }
        String directBlob = String.join(" ", safe(portfolio.getJob()), safe(portfolio.getTitle()), safe(portfolio.getBio()));
        return ExploreSearchSupport.matchesText(directBlob, jobTitle)
                || ExploreSearchSupport.matchesAnyKeyword(portfolioBlob(portfolio, currentUser), keywords)
                || matchesFamily(portfolio, familyFromJobTitle(jobTitle), currentUser);
    }

    private DeveloperFamily familyFromJobTitle(String jobTitle) {
        String normalized = ExploreSearchSupport.normalize(jobTitle);
        if (normalized.contains("backend")) {
            return DeveloperFamily.BACKEND;
        }
        if (normalized.contains("frontend")) {
            return DeveloperFamily.FRONTEND;
        }
        if (normalized.contains("full stack") || normalized.contains("fullstack")) {
            return DeveloperFamily.FULL_STACK;
        }
        if (normalized.contains("devops") || normalized.contains("cloud")) {
            return DeveloperFamily.DEVOPS_CLOUD;
        }
        if (normalized.contains("data") || normalized.contains("ai") || normalized.contains("machine learning")) {
            return DeveloperFamily.DATA_AI;
        }
        if (normalized.contains("security")) {
            return DeveloperFamily.SECURITY;
        }
        if (normalized.contains("design") || normalized.contains("creative")) {
            return DeveloperFamily.DESIGN_CREATIVE;
        }
        return DeveloperFamily.GENERAL;
    }

    private int scorePortfolio(Portfolio portfolio, Set<Long> selectedSkillIds, String query, List<String> jobKeywords, User currentUser) {
        int score = 0;
        if (!selectedSkillIds.isEmpty()) {
            score += (int) skillsForPortfolio(portfolio, currentUser).stream()
                    .map(Skill::getId)
                    .filter(Objects::nonNull)
                    .filter(selectedSkillIds::contains)
                    .distinct()
                    .count() * 4;
        }
        if (ExploreSearchSupport.hasText(query)) {
            score += ExploreSearchSupport.prefixOrContainsScore(
                    query,
                    portfolio.getTitle(),
                    portfolio.getJob(),
                    portfolio.getSkills() == null ? null : portfolio.getSkills().stream()
                            .map(Skill::getName)
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining(" ")),
                    visibleProjects(portfolio, currentUser).stream()
                            .map(PortfolioProject::getTitle)
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining(" "))
            ) * 3;
        }
        if (!jobKeywords.isEmpty() && ExploreSearchSupport.matchesAnyKeyword(portfolioBlob(portfolio, currentUser), jobKeywords)) {
            score += 2;
        }
        if (portfolio.getVisibility() == Visibility.PUBLIC) {
            score += 1;
        }
        return score;
    }

    private int scoreProject(PortfolioProject project, Set<Long> selectedSkillIds, String query) {
        int score = 0;
        if (!selectedSkillIds.isEmpty() && project.getSkills() != null) {
            score += (int) project.getSkills().stream()
                    .filter(Objects::nonNull)
                    .map(Skill::getId)
                    .filter(Objects::nonNull)
                    .filter(selectedSkillIds::contains)
                    .distinct()
                    .count() * 4;
        }
        if (Boolean.TRUE.equals(project.getPinned())) {
            score += 2;
        }
        if (ExploreSearchSupport.hasText(query)) {
            Portfolio portfolio = project.getPortfolio();
            score += ExploreSearchSupport.prefixOrContainsScore(
                    query,
                    project.getTitle(),
                    portfolio == null ? null : portfolio.getTitle(),
                    portfolio == null ? null : portfolio.getJob(),
                    project.getSkills() == null ? null : project.getSkills().stream()
                            .map(Skill::getName)
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining(" "))
            ) * 3;
        }
        return score;
    }

    private int scoreCollection(PortfolioCollection collection, Set<Long> selectedSkillIds, String query, User currentUser) {
        List<PortfolioProject> visibleProjects = visibleProjects(collection, currentUser);
        int score = 0;
        if (!selectedSkillIds.isEmpty()) {
            score += (int) skillsFromProjects(visibleProjects).stream()
                    .map(Skill::getId)
                    .filter(Objects::nonNull)
                    .filter(selectedSkillIds::contains)
                    .distinct()
                    .count() * 4;
        }
        if (ExploreSearchSupport.hasText(query)) {
            Portfolio portfolio = collection.getPortfolio();
            User owner = portfolio == null ? null : portfolio.getUser();
            score += ExploreSearchSupport.prefixOrContainsScore(
                    query,
                    collection.getName(),
                    collection.getDescription(),
                    owner == null ? null : owner.getUsername(),
                    fullName(owner),
                    visibleProjects.stream().map(PortfolioProject::getTitle).filter(Objects::nonNull).collect(Collectors.joining(" ")),
                    visibleProjects.stream().map(PortfolioProject::getDescription).filter(Objects::nonNull).collect(Collectors.joining(" ")),
                    skillsFromProjects(visibleProjects).stream().map(Skill::getName).filter(Objects::nonNull).collect(Collectors.joining(" "))
            ) * 3;
        }
        return score;
    }

    private Comparator<Portfolio> portfolioComparator(ExploreSort sort, Set<Long> selectedSkillIds, String query, List<String> jobKeywords, User currentUser) {
        ExploreSort effectiveSort = sort == null ? ExploreSort.RELEVANCE : sort;
        return switch (effectiveSort) {
            case NEWEST -> Comparator
                    .comparing(Portfolio::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(Portfolio::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(Portfolio::getId, Comparator.nullsLast(Comparator.naturalOrder()));
            case MOST_VIEWED -> Comparator
                    .comparing((Portfolio portfolio) -> portfolio.getTotalViews() == null ? 0L : portfolio.getTotalViews(), Comparator.reverseOrder())
                    .thenComparing(Portfolio::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(Portfolio::getId, Comparator.nullsLast(Comparator.naturalOrder()));
            case MOST_LIKED -> Comparator
                    .comparing((Portfolio portfolio) -> totalLikes(visibleProjects(portfolio, currentUser)), Comparator.reverseOrder())
                    .thenComparing(Portfolio::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(Portfolio::getId, Comparator.nullsLast(Comparator.naturalOrder()));
            case RELEVANCE -> Comparator
                    .comparing((Portfolio portfolio) -> scorePortfolio(portfolio, selectedSkillIds, query, jobKeywords, currentUser), Comparator.reverseOrder())
                    .thenComparing(Portfolio::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(Portfolio::getId, Comparator.nullsLast(Comparator.naturalOrder()));
        };
    }

    private Comparator<PortfolioProject> projectComparator(ExploreSort sort, Set<Long> selectedSkillIds, String query) {
        ExploreSort effectiveSort = sort == null ? ExploreSort.RELEVANCE : sort;
        return switch (effectiveSort) {
            case NEWEST -> Comparator
                    .comparing(PortfolioProject::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(PortfolioProject::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(PortfolioProject::getId, Comparator.nullsLast(Comparator.naturalOrder()));
            case MOST_VIEWED -> Comparator
                    .comparing((PortfolioProject project) -> project.getViews() == null ? 0 : project.getViews(), Comparator.reverseOrder())
                    .thenComparing(PortfolioProject::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(PortfolioProject::getId, Comparator.nullsLast(Comparator.naturalOrder()));
            case MOST_LIKED -> Comparator
                    .comparing((PortfolioProject project) -> project.getLikes() == null ? 0 : project.getLikes(), Comparator.reverseOrder())
                    .thenComparing(PortfolioProject::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(PortfolioProject::getId, Comparator.nullsLast(Comparator.naturalOrder()));
            case RELEVANCE -> Comparator
                    .comparing((PortfolioProject project) -> scoreProject(project, selectedSkillIds, query), Comparator.reverseOrder())
                    .thenComparing(PortfolioProject::getPinned, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(PortfolioProject::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(PortfolioProject::getId, Comparator.nullsLast(Comparator.naturalOrder()));
        };
    }

    private Comparator<PortfolioCollection> collectionComparator(ExploreSort sort, Set<Long> selectedSkillIds, String query, User currentUser) {
        ExploreSort effectiveSort = sort == null ? ExploreSort.RELEVANCE : sort;
        return switch (effectiveSort) {
            case NEWEST -> Comparator
                    .comparing(PortfolioCollection::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(PortfolioCollection::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(PortfolioCollection::getId, Comparator.nullsLast(Comparator.naturalOrder()));
            case MOST_VIEWED -> Comparator
                    .comparing((PortfolioCollection collection) -> totalProjectViews(visibleProjects(collection, currentUser)), Comparator.reverseOrder())
                    .thenComparing(PortfolioCollection::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(PortfolioCollection::getId, Comparator.nullsLast(Comparator.naturalOrder()));
            case MOST_LIKED -> Comparator
                    .comparing((PortfolioCollection collection) -> collection.getLikes() == null ? 0 : collection.getLikes(), Comparator.reverseOrder())
                    .thenComparing(PortfolioCollection::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(PortfolioCollection::getId, Comparator.nullsLast(Comparator.naturalOrder()));
            case RELEVANCE -> Comparator
                    .comparing((PortfolioCollection collection) -> scoreCollection(collection, selectedSkillIds, query, currentUser), Comparator.reverseOrder())
                    .thenComparing(PortfolioCollection::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(PortfolioCollection::getId, Comparator.nullsLast(Comparator.naturalOrder()));
        };
    }

    private String portfolioBlob(Portfolio portfolio, User currentUser) {
        return Stream.of(
                        safe(portfolio.getTitle()),
                        safe(portfolio.getBio()),
                        safe(portfolio.getJob()),
                        portfolio.getSkills() == null ? "" : portfolio.getSkills().stream().map(Skill::getName).filter(Objects::nonNull).collect(Collectors.joining(" ")),
                        visibleProjects(portfolio, currentUser).stream()
                                .flatMap(project -> Stream.of(project.getTitle(), project.getDescription()))
                                .filter(Objects::nonNull)
                                .collect(Collectors.joining(" "))
                )
                .collect(Collectors.joining(" "));
    }

    private Set<Long> normalizeSkillIds(List<Long> skillIds) {
        return skillIds == null ? Set.of() : skillIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private List<SkillSummaryDto> topSkills(Collection<Skill> skills, int limit) {
        if (skills == null) {
            return List.of();
        }
        return skills.stream()
                .filter(Objects::nonNull)
                .sorted(SKILL_ORDER)
                .limit(limit)
                .map(portfolioMapper::toSkillSummary)
                .toList();
    }

    private List<ExploreSkillMiniDto> toExploreSkillMiniList(Collection<Skill> skills) {
        if (skills == null) {
            return List.of();
        }
        return skills.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Skill::getId,
                        skill -> skill,
                        (left, right) -> left,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .sorted(SKILL_ORDER)
                .map(this::toExploreSkillMini)
                .toList();
    }

    private ExploreSkillMiniDto toExploreSkillMini(Skill skill) {
        return ExploreSkillMiniDto.builder()
                .id(skill.getId())
                .name(skill.getName())
                .category(skill.getCategory())
                .build();
    }

    private ExploreProjectMiniResponse toProjectMini(PortfolioProject project) {
        return ExploreProjectMiniResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .projectUrl(project.getProjectUrl())
                .imageUrl(firstMediaUrl(project, MediaType.IMAGE))
                .videoUrl(firstMediaUrl(project, MediaType.VIDEO))
                .thumbnailUrl(thumbnailUrl(project))
                .visibility(project.getVisibility())
                .views(project.getViews() == null ? 0 : project.getViews())
                .likes(project.getLikes() == null ? 0 : project.getLikes())
                .coverMediaUrl(coverMediaUrl(project))
                .skills(toExploreSkillMiniList(project.getSkills()))
                .build();
    }

    private ExploreCollectionMiniResponse toCollectionMini(PortfolioCollection collection, User currentUser) {
        List<PortfolioProject> visibleProjects = visibleProjects(collection, currentUser);
        return ExploreCollectionMiniResponse.builder()
                .id(collection.getId())
                .name(collection.getName())
                .description(collection.getDescription())
                .visibility(collection.getVisibility())
                .projectCount(visibleProjects.size())
                .coverMediaUrl(coverMediaUrl(visibleProjects))
                .build();
    }

    private ProjectMediaResponse toProjectMediaResponse(ProjectMedia media) {
        return ProjectMediaResponse.builder()
                .id(media.getId())
                .mediaUrl(media.getMediaUrl())
                .imageUrl(media.getMediaType() == MediaType.IMAGE ? media.getMediaUrl() : null)
                .videoUrl(media.getMediaType() == MediaType.VIDEO ? media.getMediaUrl() : null)
                .thumbnailUrl(media.getMediaType() == MediaType.IMAGE ? media.getMediaUrl() : null)
                .mediaType(media.getMediaType())
                .orderIndex(media.getOrderIndex())
                .build();
    }

    private String displayName(User user) {
        String fullName = fullName(user);
        return ExploreSearchSupport.hasText(fullName) ? fullName : (user == null ? "Unknown" : user.getUsername());
    }

    private String fullName(User user) {
        if (user == null || user.getProfile() == null) {
            return user == null ? null : user.getUsername();
        }
        String name = Stream.of(user.getProfile().getFirstName(), user.getProfile().getLastName())
                .filter(ExploreSearchSupport::hasText)
                .map(String::trim)
                .collect(Collectors.joining(" "));
        return name.isBlank() ? user.getUsername() : name;
    }

    private String profilePicture(User user) {
        return user == null || user.getProfile() == null ? null : user.getProfile().getProfilePicture();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private Integer totalLikes(List<PortfolioProject> projects) {
        if (projects == null) {
            return 0;
        }
        return projects.stream()
                .filter(Objects::nonNull)
                .map(PortfolioProject::getLikes)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);
    }

    private Integer totalProjectViews(List<PortfolioProject> projects) {
        return projects.stream()
                .filter(Objects::nonNull)
                .map(PortfolioProject::getViews)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);
    }

    private ProjectMedia firstMedia(PortfolioProject project) {
        return project.getMedia() == null
                ? null
                : project.getMedia().stream()
                .filter(Objects::nonNull)
                .sorted(MEDIA_ORDER)
                .findFirst()
                .orElse(null);
    }

    private String coverMediaUrl(PortfolioProject project) {
        ProjectMedia media = firstMedia(project);
        return media == null ? null : media.getMediaUrl();
    }

    private String firstMediaUrl(PortfolioProject project, MediaType type) {
        if (project == null || project.getMedia() == null) {
            return null;
        }
        return project.getMedia().stream()
                .filter(Objects::nonNull)
                .filter(media -> media.getMediaType() == type)
                .sorted(MEDIA_ORDER)
                .map(ProjectMedia::getMediaUrl)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private String thumbnailUrl(PortfolioProject project) {
        String imageUrl = firstMediaUrl(project, MediaType.IMAGE);
        if (imageUrl != null) {
            return imageUrl;
        }
        return coverMediaUrl(project);
    }

    private String coverMediaUrl(List<PortfolioProject> projects) {
        return projects.stream()
                .map(this::coverMediaUrl)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private List<PortfolioProject> visibleProjects(Portfolio portfolio, User currentUser) {
        if (portfolio == null || portfolio.getProjects() == null) {
            return List.of();
        }
        return portfolio.getProjects().stream()
                .filter(Objects::nonNull)
                .filter(project -> canSeeProject(currentUser, project))
                .sorted(PROJECT_ORDER)
                .toList();
    }

    private List<PortfolioCollection> visibleCollections(Portfolio portfolio, User currentUser) {
        if (portfolio == null || portfolio.getCollections() == null) {
            return List.of();
        }
        return portfolio.getCollections().stream()
                .filter(Objects::nonNull)
                .filter(collection -> canSeeCollection(currentUser, collection))
                .sorted(COLLECTION_ORDER)
                .toList();
    }

    private List<PortfolioProject> visibleProjects(PortfolioCollection collection, User currentUser) {
        if (collection == null || collection.getCollectionProjects() == null) {
            return List.of();
        }
        return collection.getCollectionProjects().stream()
                .filter(Objects::nonNull)
                .sorted(COLLECTION_LINK_ORDER)
                .map(CollectionProject::getProject)
                .filter(Objects::nonNull)
                .filter(project -> canSeeProject(currentUser, project))
                .toList();
    }

    private List<Skill> skillsForPortfolio(Portfolio portfolio, User currentUser) {
        if (portfolio == null) {
            return List.of();
        }
        return Stream.concat(
                        portfolio.getSkills() == null ? Stream.<Skill>empty() : portfolio.getSkills().stream(),
                        visibleProjects(portfolio, currentUser).stream()
                                .flatMap(project -> project.getSkills() == null ? Stream.<Skill>empty() : project.getSkills().stream()))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Skill::getId,
                        skill -> skill,
                        (left, right) -> left,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
    }

    private List<Skill> skillsFromProjects(List<PortfolioProject> projects) {
        return projects.stream()
                .filter(Objects::nonNull)
                .flatMap(project -> project.getSkills() == null ? Stream.<Skill>empty() : project.getSkills().stream())
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Skill::getId,
                        skill -> skill,
                        (left, right) -> left,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
    }
}
