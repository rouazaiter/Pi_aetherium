package com.education.platform.dto.cv;

import com.education.platform.entities.cv.CVSectionType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCVSectionRequest {

    @NotNull
    private CVSectionType type;

    private String title;

    @NotNull
    private Integer orderIndex;

    private Boolean visible;

    private JsonNode content;
}
