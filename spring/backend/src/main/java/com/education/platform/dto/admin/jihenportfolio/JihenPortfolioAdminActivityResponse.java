package com.education.platform.dto.admin.jihenportfolio;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// Jihen Portfolio Admin
@Getter
@Builder
public class JihenPortfolioAdminActivityResponse {

    private List<String> labels;
    private List<Long> portfolios;
    private List<Long> projects;
    private List<Long> collections;
}
