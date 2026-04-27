package com.education.platform.dto.admin.jihenportfolio;

import lombok.Builder;
import lombok.Getter;

// Jihen Portfolio Admin
@Getter
@Builder
public class JihenPortfolioAdminSearchStatDto {

    private String keyword;
    private Long searchCount;
}
