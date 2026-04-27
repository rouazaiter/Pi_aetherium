package com.education.platform.dto.admin.jihenportfolio;

import com.education.platform.entities.portfolio.SkillCategory;
import lombok.Builder;
import lombok.Getter;

// Jihen Portfolio Admin
@Getter
@Builder
public class JihenPortfolioAdminSkillDto {

    private Long id;
    private String name;
    private SkillCategory category;
    private String description;
    private Boolean trendy;
    private Long portfolioUsageCount;
    private Long projectUsageCount;
    private Long totalUsageCount;
}
