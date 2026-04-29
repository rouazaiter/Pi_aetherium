package com.education.platform.dto.explore;

import com.education.platform.entities.portfolio.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ExplorePortfolioDetailResponse {

    private Long id;
    private Long ownerId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String profileImageUrl;
    private String title;
    private String job;
    private String bio;
    private String location;
    private String githubUrl;
    private String linkedinUrl;
    private Boolean openToWork;
    private Boolean availableForFreelance;
    private Visibility visibility;
    private Long totalViews;
    private Integer projectCount;
    private Integer collectionCount;
    private List<ExploreSkillMiniDto> skills;
    private List<ExploreProjectMiniResponse> projects;
    private List<ExploreCollectionMiniResponse> collections;
    private Boolean isFriend;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
