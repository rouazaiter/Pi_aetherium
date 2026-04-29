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
public class StrengthsGapsDto {
    private DeveloperFamily dominantFamily;
    private String summary;
    private List<StrengthGapItemDto> strengths;
    private List<StrengthGapItemDto> gaps;
    private List<String> recommendations;
}
