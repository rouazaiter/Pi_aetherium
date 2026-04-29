package com.education.platform.dto.admin.jihenportfolio;

import lombok.Builder;
import lombok.Getter;

// Jihen Portfolio Admin
@Getter
@Builder
public class JihenPortfolioAdminOverviewResponse {

    private Long totalUsersWithPortfolio;
    private Long totalPortfolios;
    private Double portfoliosGrowthPercent;
    private Long totalProjects;
    private Double projectsGrowthPercent;
    private Long totalCollections;
    private Double collectionsGrowthPercent;
    private Long totalSkills;
    private Double skillsGrowthPercent;
    private Double usersWithPortfolioGrowthPercent;
    private Long publicPortfolios;
    private Long friendsOnlyPortfolios;
    private Long privatePortfolios;
    private Long publicProjects;
    private Long friendsOnlyProjects;
    private Long privateProjects;
    private Long publicCollections;
    private Long friendsOnlyCollections;
    private Long privateCollections;
    private Long cvProfilesCreated;
    private Long cvDraftsGenerated;
    private Long usersWithCvDraft;
    private Long aiCvImproveRequests;
    private Long aiCvChatRequests;
    private Long aiJobMatchedCvRequests;
    private Long aiPortfolioMentorRequests;
}
