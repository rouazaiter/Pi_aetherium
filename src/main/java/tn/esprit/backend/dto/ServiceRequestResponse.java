package tn.esprit.backend.dto;

import tn.esprit.backend.entities.ServiceRequestCategory;
import tn.esprit.backend.entities.ServiceRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServiceRequestResponse(
        Long id,
        String name,
        ServiceRequestCategory category,
        String description,
        ServiceRequestStatus status,
        BigDecimal price,
        String files,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime expiringDate,
        UserSummaryResponse creator
) {
}