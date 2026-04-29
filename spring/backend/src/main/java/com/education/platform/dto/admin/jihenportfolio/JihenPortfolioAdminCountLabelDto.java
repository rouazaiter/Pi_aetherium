package com.education.platform.dto.admin.jihenportfolio;

import lombok.Builder;
import lombok.Getter;

// Jihen Portfolio Admin
@Getter
@Builder
public class JihenPortfolioAdminCountLabelDto {

    private String template;
    private String language;
    private Long count;
}
