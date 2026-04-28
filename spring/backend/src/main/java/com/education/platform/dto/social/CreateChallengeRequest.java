package com.education.platform.dto.social;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateChallengeRequest {

    @NotNull(message = "Groupe requis")
    private Long groupId;

    @NotBlank(message = "Titre requis")
    private String title;

    private String description;

    private String topic;

    @NotNull(message = "Objectif requis")
    @Min(value = 1, message = "L'objectif doit être positif")
    private Integer targetValue;

    @NotNull(message = "Date de début requise")
    private LocalDate startDate;

    @NotNull(message = "Date de fin requise")
    private LocalDate endDate;
}
