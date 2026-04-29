package com.education.platform.dto.portfolio;

import com.education.platform.entities.portfolio.SkillCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSkillRequest {

    private String name;

    private SkillCategory category;

    private String description;
}
