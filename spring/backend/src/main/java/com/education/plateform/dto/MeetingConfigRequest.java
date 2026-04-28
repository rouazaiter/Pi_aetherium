package com.education.plateform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MeetingConfigRequest(
        @NotBlank @Size(max = 500) String calendlyLink,
        @Min(1) Integer durationMinutes,
        List<String> availableSlots
) {
}
