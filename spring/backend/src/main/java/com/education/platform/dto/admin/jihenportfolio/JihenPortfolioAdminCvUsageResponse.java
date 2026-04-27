package com.education.platform.dto.admin.jihenportfolio;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// Jihen Portfolio Admin
@Getter
@Builder
public class JihenPortfolioAdminCvUsageResponse {

    private Long cvProfilesCreated;
    private Long cvDraftsGenerated;
    private Long usersWithCvDraft;
    private List<JihenPortfolioAdminCountLabelDto> mostUsedTemplates;
    private List<JihenPortfolioAdminCountLabelDto> preferredLanguages;
}
