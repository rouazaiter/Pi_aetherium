package com.education.platform.dto.social;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateGoalProgressRequest {

    @NotNull(message = "Progression requise")
    private Integer currentValue;
}
