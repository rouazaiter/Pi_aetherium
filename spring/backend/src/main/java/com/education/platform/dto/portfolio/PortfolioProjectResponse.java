package com.education.platform.dto.portfolio;

import com.education.platform.entities.portfolio.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PortfolioProjectResponse {

    private Long id;
    private String title;
    private String description;
    private String projectUrl;
    private String imageUrl;
    private String videoUrl;
    private String thumbnailUrl;
    private Boolean pinned;
    private Visibility visibility;
    private Integer totalLikes;
    private List<SkillSummaryDto> skills;
    private List<ProjectMediaResponse> media;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
