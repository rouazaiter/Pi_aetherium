package com.education.platform.dto.portfolio;

import com.education.platform.entities.portfolio.SkillCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSkillRequest {

    @NotBlank
    private String name;

    private SkillCategory category;

    private String description;
}
