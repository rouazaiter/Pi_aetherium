package tn.esprit.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MeetingConfigResponse(
        Long serviceRequestId,
        String calendlyLink,
        List<String> availableSlots,
        LocalDateTime updatedAt
) {
}
