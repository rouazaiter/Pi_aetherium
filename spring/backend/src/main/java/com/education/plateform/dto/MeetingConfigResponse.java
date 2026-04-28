package com.education.plateform.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MeetingConfigResponse(
        Long serviceRequestId,
        String calendlyLink,
        Integer durationMinutes,
        List<String> availableSlots,
        LocalDateTime updatedAt
) {
}
