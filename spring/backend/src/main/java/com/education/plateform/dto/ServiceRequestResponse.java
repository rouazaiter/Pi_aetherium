package com.education.plateform.dto;

import com.education.plateform.entities.ServiceRequestCategory;
import com.education.plateform.entities.PaymentStatus;
import com.education.plateform.entities.ServiceRequestStatus;

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