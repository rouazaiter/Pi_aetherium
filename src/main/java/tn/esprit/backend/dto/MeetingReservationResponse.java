package tn.esprit.backend.dto;

import java.time.LocalDateTime;

public record MeetingReservationResponse(
        Long id,
        Long applicationId,
        Long serviceRequestId,
        Long applicantId,
        String applicantUsername,
        String source,
        String slot,
        String calendlyEventUrl,
        String status,
        LocalDateTime createdAt,
        LocalDateTime confirmedAt
) {
}
