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
public class ProjectProofStrengthDto {
    private DeveloperFamily dominantFamily;
    private Integer proofScore;
    private Integer strongProjects;
    private Integer weakProjects;
    private List<ProjectProofDetailDto> projectDetails;
    private List<String> recommendations;
}
