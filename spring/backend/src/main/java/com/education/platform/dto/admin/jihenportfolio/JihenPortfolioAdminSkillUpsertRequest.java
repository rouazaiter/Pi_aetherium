package com.education.platform.dto.admin.jihenportfolio;

import com.education.platform.entities.portfolio.SkillCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// Jihen Portfolio Admin
@Getter
@Setter
public class JihenPortfolioAdminSkillUpsertRequest {

    @NotBlank
    private String name;

    @NotNull
    private SkillCategory category;

    private String description;

    private Boolean trendy;
}
