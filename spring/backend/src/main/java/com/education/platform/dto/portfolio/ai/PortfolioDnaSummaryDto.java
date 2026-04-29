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
public class PortfolioDnaSummaryDto {
    private DeveloperFamily dominantFamily;
    private String dnaType;
    private String maturityLevel;
    private Integer profileStrengthScore;
    private String marketReadiness;
    private List<String> strongestSignals;
    private List<String> mainWeakPoints;
    private List<String> strategicPriorities;
}
