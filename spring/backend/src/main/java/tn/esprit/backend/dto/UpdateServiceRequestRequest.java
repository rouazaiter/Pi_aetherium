package tn.esprit.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import tn.esprit.backend.entities.ServiceRequestCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateServiceRequestRequest(
        @Size(max = 100) String name,
        ServiceRequestCategory category,
        @Size(max = 2000) String description,
        @Future LocalDateTime expiringDate,
        @DecimalMin("1.00") @Digits(integer = 8, fraction = 2) BigDecimal price
) {
}