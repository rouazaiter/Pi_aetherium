package com.education.platform.dto.social;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerQuizRequest {

    @NotBlank(message = "Réponse requise")
    @Pattern(regexp = "^[ABCD]$", message = "La réponse doit être A, B, C ou D")
    private String selectedOption;
}
