package com.education.platform.dto.portfolio;

import com.education.platform.entities.portfolio.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PortfolioDataDto {

    private Long id;
    private String title;
    private String bio;
    private String coverImage;
    private String job;
    private String githubUrl;
    private String linkedinUrl;
    private Boolean openToWork;
    private Boolean availableForFreelance;
    private Visibility visibility;
    private Long totalViews;
    private Boolean verified;
    private List<SkillSummaryDto> skills;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
