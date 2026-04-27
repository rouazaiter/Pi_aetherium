package com.education.platform.dto.social;

import com.education.platform.entities.GoalVisibility;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateGoalRequest {

    @NotBlank(message = "Titre requis")
    private String title;

    private String topic;

    @NotNull(message = "Objectif requis")
    @Min(value = 1, message = "L'objectif doit être positif")
    private Integer targetValue;

    private LocalDate deadline;

    @NotNull(message = "Visibilité requise")
    private GoalVisibility visibility;

    private Long groupId;
}
