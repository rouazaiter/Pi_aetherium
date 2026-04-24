package tn.esprit.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DashboardEventDto(
        Long id,
        String eventName,
        LocalDateTime eventDate,
        String category,
        String status,
        BigDecimal amount,
        long participants
) {
}
