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
public class TechnicalDepthDto {
    private DeveloperFamily dominantFamily;
    private Integer foundationalScore;
    private Integer advancedScore;
    private Integer projectDepthScore;
    private Integer depthScore;
    private String level;
    private List<String> strongSignals;
    private List<String> missingDepthAreas;
    private List<String> recommendations;
}
