package com.education.platform.dto.explore;

import com.education.platform.dto.portfolio.SkillSummaryDto;
import com.education.platform.dto.portfolio.ai.DeveloperFamily;
import com.education.platform.entities.portfolio.MediaType;
import com.education.platform.entities.portfolio.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExploreProjectCardResponse {

    private Long projectId;
    private Long portfolioId;
    private Long ownerId;
    private String ownerUsername;
    private String ownerDisplayName;
    private String ownerProfileImage;
    private String title;
    private String description;
    private String projectUrl;
    private String mediaUrl;
    private String imageUrl;
    private String videoUrl;
    private String thumbnailUrl;
    private MediaType mediaType;
    private DeveloperFamily family;
    private Visibility visibility;
    private Integer views;
    private Integer likes;
    private List<SkillSummaryDto> topSkills;
}
