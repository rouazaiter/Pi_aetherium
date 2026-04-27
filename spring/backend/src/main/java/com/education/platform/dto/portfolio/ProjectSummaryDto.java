package com.education.platform.dto.portfolio;

import com.education.platform.entities.portfolio.Visibility;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectSummaryDto {

    private Long id;
    private String title;
    private String coverImage;
    private String imageUrl;
    private String videoUrl;
    private String thumbnailUrl;
    private Visibility visibility;
}
