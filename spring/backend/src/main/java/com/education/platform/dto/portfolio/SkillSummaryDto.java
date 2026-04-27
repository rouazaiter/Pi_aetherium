package com.education.platform.dto.portfolio;

import com.education.platform.entities.portfolio.SkillCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SkillSummaryDto {

    private Long id;
    private String name;
    private SkillCategory category;
}
