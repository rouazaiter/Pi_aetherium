package tn.esprit.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AiCoachPreviewRequest(
        @NotNull Long serviceRequestId,
        @NotBlank @Size(max = 2000) String originalText,
        @Size(max = 40) String tone,
        @Size(max = 10) String language
) {
}
