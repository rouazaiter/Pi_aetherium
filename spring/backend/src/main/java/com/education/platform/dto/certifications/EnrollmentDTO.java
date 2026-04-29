package com.education.platform.dto.certifications;

import com.education.platform.entities.certifications.Enrollment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EnrollmentDTO(
        Long id,
        String userIdentifier,
        String fullName,
        Long certificationId,
        String certificationTitle,
        BigDecimal amountPaid,
        Enrollment.EnrollmentStatus status,
        Double score,
        Boolean passed,
        LocalDateTime enrolledAt,
        LocalDateTime completedAt,
        Boolean isVerified,
        Integer attemptCount,
        Integer maxAttempts,
        LocalDateTime lastAttemptAt   // null on first attempt
) {}
