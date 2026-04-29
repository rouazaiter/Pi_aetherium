package com.education.platform.dto.certifications;

import java.time.LocalDateTime;

/**
 * Public-facing certificate verification response.
 * Contains only what a third party (employer) needs to see — no sensitive data.
 */
public record CertificateVerificationDTO(
        String certificateId,
        boolean valid,

        // Holder info
        String holderName,

        // Certification info
        String certificationTitle,
        String category,
        String difficulty,

        // Result
        double score,
        boolean passed,
        LocalDateTime issuedAt,

        // Issuer
        String issuedBy,
        String issuerTitle,
        String platform
) {}
