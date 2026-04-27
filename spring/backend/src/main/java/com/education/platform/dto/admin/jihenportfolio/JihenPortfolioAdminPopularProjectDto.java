package com.education.platform.dto.admin.jihenportfolio;

import com.education.platform.entities.portfolio.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

// Jihen Portfolio Admin
@Getter
@Builder
public class JihenPortfolioAdminPopularProjectDto {

    private Long projectId;
    private String title;
    private String ownerName;
    private Integer views;
    private Integer likes;
    private Visibility visibility;
    private List<String> skillNames;
}
