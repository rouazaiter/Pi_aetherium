package com.education.platform.services.interfaces.admin.jihenportfolio;

import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminAiUsageResponse;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminActivityResponse;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminCollectionItemDto;
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
import com.education.platform.entities.portfolio.ModerationStatus;
import com.education.platform.entities.portfolio.Visibility;

import java.util.List;

// Jihen Portfolio Admin
public interface JihenPortfolioAdminService {

    JihenPortfolioAdminOverviewResponse getOverview();

    JihenPortfolioAdminPageResponse<JihenPortfolioAdminPortfolioItemDto> listPortfolios(
            String q, Visibility visibility, ModerationStatus moderationStatus, Integer page, Integer size, String sort);

    JihenPortfolioAdminPageResponse<JihenPortfolioAdminProjectItemDto> listProjects(
            String q, Visibility visibility, ModerationStatus moderationStatus, Integer page, Integer size, String sort);

    JihenPortfolioAdminPageResponse<JihenPortfolioAdminCollectionItemDto> listCollections(
            String q, Visibility visibility, ModerationStatus moderationStatus, Integer page, Integer size, String sort);

    JihenPortfolioAdminPortfolioItemDto updatePortfolioVisibility(Long portfolioId, Visibility visibility);

    JihenPortfolioAdminProjectItemDto updateProjectVisibility(Long projectId, Visibility visibility);

    JihenPortfolioAdminCollectionItemDto updateCollectionVisibility(Long collectionId, Visibility visibility);

    JihenPortfolioAdminPortfolioItemDto updatePortfolioModeration(Long portfolioId, ModerationStatus status, String reason);

    JihenPortfolioAdminProjectItemDto updateProjectModeration(Long projectId, ModerationStatus status, String reason);

    JihenPortfolioAdminCollectionItemDto updateCollectionModeration(Long collectionId, ModerationStatus status, String reason);

    List<JihenPortfolioAdminSkillDto> listSkills(String q, String category, Boolean trendy);

    JihenPortfolioAdminSkillDto createSkill(JihenPortfolioAdminSkillUpsertRequest request);

    JihenPortfolioAdminSkillDto updateSkill(Long skillId, JihenPortfolioAdminSkillUpsertRequest request);

    void deleteSkill(Long skillId);

    List<JihenPortfolioAdminSkillCategoryDto> listSkillCategories();

    List<JihenPortfolioAdminFamilyAnalyticsDto> getFamilyAnalytics();

    List<JihenPortfolioAdminTrendingSkillDto> getTrendingSkills(Integer limit);

    List<JihenPortfolioAdminPopularProjectDto> getPopularProjects(Integer limit);

    JihenPortfolioAdminCvUsageResponse getCvUsage();

    JihenPortfolioAdminAiUsageResponse getAiUsage();

    List<JihenPortfolioAdminSearchStatDto> getSearchAnalytics();

    JihenPortfolioAdminActivityResponse getActivity(String range);

    List<JihenPortfolioAdminRecentItemDto> getRecentItems(Integer limit);
}
