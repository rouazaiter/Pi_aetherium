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
public class SkillCoverageDto {
    private DeveloperFamily dominantFamily;
    private Integer coverageScore;
    private List<String> coveredAreas;
    private List<String> missingAreas;
    private List<String> recommendations;
}
