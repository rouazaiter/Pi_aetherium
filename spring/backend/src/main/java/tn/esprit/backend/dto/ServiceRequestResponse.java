package tn.esprit.backend.dto;

import tn.esprit.backend.entities.ServiceRequestCategory;
import tn.esprit.backend.entities.PaymentStatus;
import tn.esprit.backend.entities.ServiceRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServiceRequestResponse(
        Long id,
        String name,
        ServiceRequestCategory category,
        String description,
        ServiceRequestStatus status,
        PaymentStatus paymentStatus,
        BigDecimal price,
        String files,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime expiringDate,
        UserSummaryResponse creator
) {
}