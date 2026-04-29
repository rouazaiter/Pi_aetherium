package com.education.platform.dto.portfolio.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectProofDetailDto {
    private Long projectId;
    private String projectTitle;
    private Integer score;
    private String level;
    private Boolean hasDescription;
    private Boolean hasUrl;
    private Boolean hasMedia;
    private Boolean hasLinkedSkills;
    private Boolean pinned;
    private List<String> strengths;
    private List<String> issues;
}
