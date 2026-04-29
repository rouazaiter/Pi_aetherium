package com.education.platform.entities.certifications;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Simulated user identifier (no auth — just a name/email string) */
    private String userIdentifier;

    /** Full name provided by the user at checkout */
    private String fullName;

    /** Phone number for SMS payment confirmation (optional, e.g. +21612345678) */
    private String phoneNumber;

    @ManyToOne
    @JoinColumn(name = "certification_id")
    private Certification certification;

    private BigDecimal amountPaid;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.ENROLLED;

    private LocalDateTime enrolledAt;

    // Exam result fields (filled after submission)
    private Double score;          // 0-100
    private Boolean passed;
    private LocalDateTime completedAt;

    private String verificationCode;
    private Boolean isVerified = false;
    private LocalDateTime verificationCodeExpiresAt;

    /** How many real exam attempts have been used (starts at 0) */
    @Builder.Default
    private Integer attemptCount = 0;

    /** Maximum allowed real exam attempts (default 2 — original + 1 free retry) */
    @Builder.Default
    private Integer maxAttempts = 2;

    /** Timestamp of the last completed exam attempt — used for 24h cooldown */
    private LocalDateTime lastAttemptAt;

    @PrePersist
    protected void onCreate() { enrolledAt = LocalDateTime.now(); }

    public enum EnrollmentStatus { ENROLLED, COMPLETED }
}
