package com.education.platform.dto.admin.jihenportfolio;

import com.education.platform.entities.portfolio.SkillCategory;
import lombok.Builder;
import lombok.Getter;

// Jihen Portfolio Admin
@Getter
@Builder
public class JihenPortfolioAdminTrendingSkillDto {

    private Long skillId;
    private String skillName;
    private SkillCategory category;
    private Long portfolioUsageCount;
    private Long projectUsageCount;
    private Long totalUsageCount;
    private Boolean trendy;
}
