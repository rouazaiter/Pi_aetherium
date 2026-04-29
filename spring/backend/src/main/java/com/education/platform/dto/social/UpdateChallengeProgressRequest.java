package com.education.platform.dto.social;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateChallengeProgressRequest {

    @NotNull(message = "Progression requise")
    private Integer progressValue;
}
