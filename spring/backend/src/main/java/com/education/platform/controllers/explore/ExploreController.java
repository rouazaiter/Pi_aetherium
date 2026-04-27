package com.education.platform.controllers.explore;

import com.education.platform.dto.explore.ExploreOptionDto;
import com.education.platform.dto.explore.ExploreCollectionDetailResponse;
import com.education.platform.dto.explore.ExploreCollectionResponse;
import com.education.platform.dto.explore.ExplorePortfolioDetailResponse;
import com.education.platform.dto.explore.ExplorePortfolioCardResponse;
import com.education.platform.dto.explore.ExploreProjectDetailResponse;
import com.education.platform.dto.explore.ExploreProjectCardResponse;
import com.education.platform.dto.explore.ExploreSort;
import com.education.platform.dto.explore.ExploreVisibilityFilter;
import com.education.platform.dto.portfolio.SkillSummaryDto;
import com.education.platform.dto.portfolio.ai.DeveloperFamily;
import com.education.platform.entities.User;
import com.education.platform.entities.portfolio.SkillCategory;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.admin.jihenportfolio.JihenPortfolioAdminTrackingService;
import com.education.platform.services.interfaces.explore.ExploreService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/explore")
public class ExploreController {

    private final CurrentUserService currentUserService;
    private final ExploreService exploreService;
    private final JihenPortfolioAdminTrackingService trackingService;

    public ExploreController(
            CurrentUserService currentUserService,
            ExploreService exploreService,
            JihenPortfolioAdminTrackingService trackingService) {
        this.currentUserService = currentUserService;
        this.exploreService = exploreService;
        this.trackingService = trackingService;
    }

    @GetMapping("/families")
    public List<ExploreOptionDto> listFamilies() {
        currentUserService.getCurrentUser();
        return exploreService.listFamilies();
    }

    @GetMapping("/skill-categories")
    public List<ExploreOptionDto> listSkillCategories() {
        currentUserService.getCurrentUser();
        return exploreService.listSkillCategories();
    }

    @GetMapping("/skills")
    public List<SkillSummaryDto> searchSkills(@RequestParam(name = "q", required = false) String query) {
        return exploreService.searchSkills(currentUserService.getCurrentUser(), query);
    }

    @GetMapping("/portfolios")
    public List<ExplorePortfolioCardResponse> searchPortfolios(
            @RequestParam(required = false) String family,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<Long> skillIds,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) String visibility,
            @RequestParam(required = false) String sort) {
        User currentUser = currentUserService.getCurrentUser();
        trackingService.recordExploreSearch(
                currentUser == null ? null : currentUser.getId(),
                query,
                jobTitle,
                searchFilters(family, category, skillIds, visibility, sort)
        );
        return exploreService.searchPortfolios(
                currentUser,
                parseFamily(family),
                parseCategory(category),
                skillIds,
                query,
                jobTitle,
                parseVisibility(visibility),
                parseSort(sort)
        );
    }

    @GetMapping("/projects")
    public List<ExploreProjectCardResponse> searchProjects(
            @RequestParam(required = false) String family,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<Long> skillIds,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(required = false) String visibility,
            @RequestParam(required = false) String sort) {
        User currentUser = currentUserService.getCurrentUser();
        trackingService.recordExploreSearch(
                currentUser == null ? null : currentUser.getId(),
                query,
                null,
                searchFilters(family, category, skillIds, visibility, sort)
        );
        return exploreService.searchProjects(
                currentUser,
                parseFamily(family),
                parseCategory(category),
                skillIds,
                query,
                parseVisibility(visibility),
                parseSort(sort)
        );
    }

    @GetMapping("/collections")
    public List<ExploreCollectionResponse> searchCollections(
            @RequestParam(required = false) String family,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<Long> skillIds,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(required = false) String visibility,
            @RequestParam(required = false) String sort) {
        User currentUser = currentUserService.getCurrentUser();
        trackingService.recordExploreSearch(
                currentUser == null ? null : currentUser.getId(),
                query,
                null,
                searchFilters(family, category, skillIds, visibility, sort)
        );
        return exploreService.searchCollections(
                currentUser,
                parseFamily(family),
                parseCategory(category),
                skillIds,
                query,
                parseVisibility(visibility),
                parseSort(sort)
        );
    }

    @GetMapping("/portfolios/{portfolioId}")
    public ExplorePortfolioDetailResponse getPortfolioDetail(@PathVariable Long portfolioId) {
        return exploreService.getPortfolioDetail(currentUserService.getCurrentUser(), portfolioId);
    }

    @GetMapping("/projects/{projectId}")
    public ExploreProjectDetailResponse getProjectDetail(@PathVariable Long projectId) {
        return exploreService.getProjectDetail(currentUserService.getCurrentUser(), projectId);
    }

    @GetMapping("/collections/{collectionId}")
    public ExploreCollectionDetailResponse getCollectionDetail(@PathVariable Long collectionId) {
        return exploreService.getCollectionDetail(currentUserService.getCurrentUser(), collectionId);
    }

    @GetMapping("/portfolios/{portfolioId}/collections")
    public List<ExploreCollectionResponse> getPortfolioCollections(@PathVariable Long portfolioId) {
        return exploreService.getPortfolioCollections(currentUserService.getCurrentUser(), portfolioId);
    }

    private DeveloperFamily parseFamily(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return DeveloperFamily.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private SkillCategory parseCategory(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return SkillCategory.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private ExploreVisibilityFilter parseVisibility(String value) {
        if (value == null || value.isBlank()) {
            return ExploreVisibilityFilter.PUBLIC_AND_FRIENDS;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if ("PUBLIC+FRIENDS".equals(normalized) || "PUBLIC_AND_FRIENDS".equals(normalized)) {
            return ExploreVisibilityFilter.PUBLIC_AND_FRIENDS;
        }
        if ("FRIENDS_PROJECTS_ONLY".equals(normalized)) {
            return ExploreVisibilityFilter.FRIENDS;
        }
        return ExploreVisibilityFilter.valueOf(normalized);
    }

    private ExploreSort parseSort(String value) {
        if (value == null || value.isBlank()) {
            return ExploreSort.RELEVANCE;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "RELEVANT" -> ExploreSort.RELEVANCE;
            case "RECENT" -> ExploreSort.NEWEST;
            default -> ExploreSort.valueOf(normalized);
        };
    }

    private String searchFilters(String family, String category, List<Long> skillIds, String visibility, String sort) {
        StringBuilder builder = new StringBuilder();
        appendFilter(builder, "family", family);
        appendFilter(builder, "category", category);
        if (skillIds != null && !skillIds.isEmpty()) {
            appendFilter(builder, "skillIds", skillIds.stream().map(String::valueOf).toList().toString());
        }
        appendFilter(builder, "visibility", visibility);
        appendFilter(builder, "sort", sort);
        return builder.toString();
    }

    private void appendFilter(StringBuilder builder, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(';');
        }
        builder.append(key).append('=').append(value.trim());
    }
}
