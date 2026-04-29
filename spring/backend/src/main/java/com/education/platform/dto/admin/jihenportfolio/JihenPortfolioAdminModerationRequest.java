package com.education.platform.dto.admin.jihenportfolio;

import com.education.platform.entities.portfolio.ModerationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// Jihen Portfolio Admin
@Getter
@Setter
public class JihenPortfolioAdminModerationRequest {

    @NotNull
    private ModerationStatus status;

    private String reason;
}
