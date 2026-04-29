package com.education.platform.dto.portfolio;

import com.education.platform.entities.portfolio.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PortfolioCollectionResponse {

    private Long id;
    private String name;
    private String description;
    private Visibility visibility;
    private Integer totalLikes;
    private List<ProjectSummaryDto> projects;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
