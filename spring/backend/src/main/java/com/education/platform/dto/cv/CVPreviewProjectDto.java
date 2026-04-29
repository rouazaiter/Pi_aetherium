package com.education.platform.dto.cv;

import com.education.platform.entities.portfolio.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CVPreviewProjectDto {

    private Long id;
    private String title;
    private String description;
    private String projectUrl;
    private Visibility visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String imageUrl;
    private String collectionName;
    private List<CVPreviewSkillDto> skills;
}
