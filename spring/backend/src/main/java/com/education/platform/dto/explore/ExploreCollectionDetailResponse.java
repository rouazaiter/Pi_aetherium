package com.education.platform.dto.explore;

import com.education.platform.entities.portfolio.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ExploreCollectionDetailResponse {

    private Long id;
    private Long portfolioId;
    private Long ownerId;
    private String ownerName;
    private String ownerAvatarUrl;
    private String ownerJob;
    private String name;
    private String description;
    private Visibility visibility;
    private Integer projectCount;
    private List<ExploreProjectMiniResponse> projects;
    private List<ExploreSkillMiniDto> skills;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
