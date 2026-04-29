package com.education.platform.services.interfaces.explore;

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

import java.util.List;

public interface ExploreService {

    List<ExploreOptionDto> listFamilies();

    List<ExploreOptionDto> listSkillCategories();

    List<SkillSummaryDto> searchSkills(User currentUser, String query);

    List<ExplorePortfolioCardResponse> searchPortfolios(
            User currentUser,
            DeveloperFamily family,
            SkillCategory category,
            List<Long> skillIds,
            String query,
            String jobTitle,
            ExploreVisibilityFilter visibility,
            ExploreSort sort);

    List<ExploreProjectCardResponse> searchProjects(
            User currentUser,
            DeveloperFamily family,
            SkillCategory category,
            List<Long> skillIds,
            String query,
            ExploreVisibilityFilter visibility,
            ExploreSort sort);

    List<ExploreCollectionResponse> searchCollections(
            User currentUser,
            DeveloperFamily family,
            SkillCategory category,
            List<Long> skillIds,
            String query,
            ExploreVisibilityFilter visibility,
            ExploreSort sort);

    ExplorePortfolioDetailResponse getPortfolioDetail(User currentUser, Long portfolioId);

    ExploreProjectDetailResponse getProjectDetail(User currentUser, Long projectId);

    ExploreCollectionDetailResponse getCollectionDetail(User currentUser, Long collectionId);

    List<ExploreCollectionResponse> getPortfolioCollections(User currentUser, Long portfolioId);
}
