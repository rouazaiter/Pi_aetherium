package com.education.platform.dto.admin.jihenportfolio;

import com.education.platform.dto.portfolio.ai.DeveloperFamily;
import lombok.Builder;
import lombok.Getter;

// Jihen Portfolio Admin
@Getter
@Builder
public class JihenPortfolioAdminFamilyAnalyticsDto {

    private DeveloperFamily family;
    private String label;
    private Long portfolioCount;
    private Long projectCount;
    private Integer percentage;
}
