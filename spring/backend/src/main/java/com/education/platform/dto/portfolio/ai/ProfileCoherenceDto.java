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
public class ProfileCoherenceDto {
    private Integer titleAlignmentScore;
    private Integer bioAlignmentScore;
    private Integer skillAlignmentScore;
    private Integer projectProofScore;
    private Integer totalScore;
    private String status;
    private Boolean mismatchDetected;
    private List<String> recommendations;
}
