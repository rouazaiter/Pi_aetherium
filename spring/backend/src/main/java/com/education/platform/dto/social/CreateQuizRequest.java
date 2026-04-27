package com.education.platform.dto.social;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateQuizRequest {

    @NotNull(message = "Groupe requis")
    private Long groupId;

    @NotBlank(message = "Question requise")
    private String question;

    @NotBlank(message = "Option A requise")
    private String optionA;

    @NotBlank(message = "Option B requise")
    private String optionB;

    @NotBlank(message = "Option C requise")
    private String optionC;

    @NotBlank(message = "Option D requise")
    private String optionD;

    @NotBlank(message = "Bonne réponse requise")
    @Pattern(regexp = "^[ABCD]$", message = "La bonne réponse doit être A, B, C ou D")
    private String correctOption;
}
