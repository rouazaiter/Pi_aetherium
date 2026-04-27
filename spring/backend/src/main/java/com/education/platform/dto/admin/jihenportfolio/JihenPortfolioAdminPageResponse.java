package com.education.platform.dto.admin.jihenportfolio;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// Jihen Portfolio Admin
@Getter
@Builder
public class JihenPortfolioAdminPageResponse<T> {

    private List<T> items;
    private Long total;
    private Integer page;
    private Integer size;
    private Integer totalPages;
}
