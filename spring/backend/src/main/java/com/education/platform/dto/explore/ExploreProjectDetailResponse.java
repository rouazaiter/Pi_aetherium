package com.education.platform.dto.explore;

import com.education.platform.dto.portfolio.ProjectMediaResponse;
import com.education.platform.entities.portfolio.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ExploreProjectDetailResponse {

    private Long id;
    private Long portfolioId;
    private Long ownerId;
    private String ownerName;
    private String ownerAvatarUrl;
    private String ownerJob;
    private String title;
    private String description;
    private String projectUrl;
    private String imageUrl;
    private String videoUrl;
    private String thumbnailUrl;
    private Visibility visibility;
    private Integer views;
    private Integer likes;
    private List<ProjectMediaResponse> media;
    private List<ExploreSkillMiniDto> skills;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
