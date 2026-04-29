package com.education.platform.dto.cv;

import com.education.platform.entities.cv.CVSectionType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CVSectionResponse {

    private Long id;
    private CVSectionType type;
    private String title;
    private Integer orderIndex;
    private Boolean visible;
    private JsonNode content;
}
