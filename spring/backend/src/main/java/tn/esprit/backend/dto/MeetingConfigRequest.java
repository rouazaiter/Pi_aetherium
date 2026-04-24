package tn.esprit.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MeetingConfigRequest(
        @NotBlank @Size(max = 500) String calendlyLink,
        @NotEmpty List<@NotBlank @Size(max = 255) String> availableSlots
) {
}
