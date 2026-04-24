package tn.esprit.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiCoachApplyRequest(
        @NotBlank @Size(max = 2000) String improvedText
) {
}
