package com.education.platform.dto.cv;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CvAiJobMatchResponse {

    private String targetJobTitle;
    private List<String> extractedKeywords;
    private List<String> matchingKeywords;
    private List<String> missingKeywords;
    private String improvedSummary;
    private List<CvAiJobMatchedProjectResponse> improvedProjects;
    private List<String> recommendations;
}
