package com.education.platform.dto.admin.jihenportfolio;

import com.education.platform.entities.portfolio.Visibility;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// Jihen Portfolio Admin
@Getter
@Setter
public class JihenPortfolioAdminVisibilityRequest {

    @NotNull
    private Visibility visibility;
}
