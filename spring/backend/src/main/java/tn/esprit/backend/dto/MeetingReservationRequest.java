package tn.esprit.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MeetingReservationRequest(
        @NotBlank String source,
        @NotBlank @Size(max = 255) String slot,
        @Size(max = 500) String calendlyEventUrl
) {
}
