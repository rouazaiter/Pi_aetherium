package com.education.platform.dto.cv;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CvAiJobMatchedProjectResponse {

    private Long projectId;
    private String originalDescription;
    private String improvedDescription;
}
