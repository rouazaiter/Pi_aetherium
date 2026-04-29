package com.education.platform.dto.explore;

import com.education.platform.entities.portfolio.SkillCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExploreSkillMiniDto {

    private Long id;
    private String name;
    private SkillCategory category;
}
