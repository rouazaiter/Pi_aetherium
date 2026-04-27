package com.education.platform.dto.portfolio;

import com.education.platform.entities.portfolio.MediaType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectMediaResponse {

    private Long id;
    private String mediaUrl;
    private String imageUrl;
    private String videoUrl;
    private String thumbnailUrl;
    private MediaType mediaType;
    private Integer orderIndex;
}
