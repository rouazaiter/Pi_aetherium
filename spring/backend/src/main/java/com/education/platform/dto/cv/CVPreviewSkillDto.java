package com.education.platform.dto.cv;

import com.education.platform.entities.portfolio.SkillCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CVPreviewSkillDto {

    private Long id;
    private String name;
    private SkillCategory category;
}
