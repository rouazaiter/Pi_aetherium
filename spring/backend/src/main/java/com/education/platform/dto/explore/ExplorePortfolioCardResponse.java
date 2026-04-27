package com.education.platform.dto.explore;

import com.education.platform.dto.portfolio.SkillSummaryDto;
import com.education.platform.dto.portfolio.ai.DeveloperFamily;
import com.education.platform.entities.portfolio.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExplorePortfolioCardResponse {

    private Long portfolioId;
    private Long ownerId;
    private String ownerUsername;
    private String displayName;
    private String profileImage;
    private String location;
    private String portfolioTitle;
    private String bio;
    private String jobTitle;
    private DeveloperFamily family;
    private Visibility visibility;
    private Integer projectCount;
    private Long totalViews;
    private Integer totalLikes;
    private String githubUrl;
    private String linkedinUrl;
    private List<SkillSummaryDto> topSkills;
}
