package com.education.platform.services.implementations.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepo;
    private final ExamAttemptRepository attemptRepo;
    private final CertificationRepository certRepo;
    private final QuestionRepository questionRepo;
    private final ExamRepository examRepo;
    private final EmailService emailService;
    private final SmsService smsService;


    // ── Enroll (pay) ──────────────────────────────────────────────────────

    @Transactional
    public EnrollmentDTO enroll(Long certId, String userIdentifier) {
        return enroll(certId, userIdentifier, null);
    }

    @Transactional
    public EnrollmentDTO enroll(Long certId, String userIdentifier, String fullName) {
        Certification cert = certRepo.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certification not found"));

        Enrollment enrollment = enrollmentRepo.findByUserIdentifierAndCertificationId(userIdentifier, certId)
                .orElseGet(() -> Enrollment.builder()
                        .userIdentifier(userIdentifier)
                        .certification(cert)
                        .amountPaid(cert.getPrice())
                        .status(Enrollment.EnrollmentStatus.ENROLLED)
                        .build());

        if (fullName != null && !fullName.isBlank()) {
            enrollment.setFullName(fullName);
        }
        if (cert.getPrice().compareTo(java.math.BigDecimal.ZERO) == 0) {
            enrollment.setIsVerified(true);
        }

        return toDTO(enrollmentRepo.save(enrollment));
    }

    @Transactional
    public void confirmPayment(String userIdentifier, Long certId) {
        Certification cert = certRepo.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certification not found"));
        confirmPaymentWithName(userIdentifier, certId, null);
    }

    @Transactional
    public void confirmPaymentWithName(String userIdentifier, Long certId, String fullName) {
        confirmPaymentWithName(userIdentifier, certId, fullName, null);
    }

    @Transactional
    public void confirmPaymentWithName(String userIdentifier, Long certId, String fullName, String phoneNumber) {
        Certification cert = certRepo.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certification not found"));

        Enrollment enrollment = enrollmentRepo.findByUserIdentifierAndCertificationId(userIdentifier, certId)
                .orElseGet(() -> Enrollment.builder()
                        .userIdentifier(userIdentifier)
                        .certification(cert)
                        .amountPaid(cert.getPrice())
                        .status(Enrollment.EnrollmentStatus.ENROLLED)
                        .build());

        if (fullName != null && !fullName.isBlank()) {
            enrollment.setFullName(fullName);
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            enrollment.setPhoneNumber(phoneNumber);
        }

        // Generate a 6-digit code
        String code = String.format("%06d", new java.util.Random().nextInt(999999));
        enrollment.setVerificationCode(code);
        enrollment.setIsVerified(false);
        enrollment.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5));
        enrollmentRepo.save(enrollment);

        // Send email
        emailService.sendVerificationCode(userIdentifier, code, cert.getTitle());
    }

    @Transactional
    public void verifyCode(String userIdentifier, Long certId, String code) {
        Enrollment enrollment = enrollmentRepo.findByUserIdentifierAndCertificationId(userIdentifier, certId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        if (enrollment.getVerificationCode() == null || !enrollment.getVerificationCode().equals(code)) {
            throw new RuntimeException("Invalid verification code");
        }

        if (enrollment.getVerificationCodeExpiresAt() != null &&
            enrollment.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code has expired (valid for 5 minutes only)");
        }

        enrollment.setIsVerified(true);
        enrollment.setStatus(Enrollment.EnrollmentStatus.ENROLLED);
        enrollmentRepo.save(enrollment);

        // ── Send SMS payment confirmation (only if phone number was provided) ──
        String phone = enrollment.getPhoneNumber();
        System.out.println("[SMS] verifyCode triggered — phone on enrollment: " + phone);
        if (phone != null && !phone.isBlank()) {
            String displayName = enrollment.getFullName() != null && !enrollment.getFullName().isBlank()
                    ? enrollment.getFullName() : enrollment.getUserIdentifier();
            smsService.sendPaymentConfirmation(
                    phone,
                    displayName,
                    enrollment.getCertification().getTitle(),
                    enrollment.getAmountPaid()
            );
        } else {
            System.out.println("[SMS] No phone number on enrollment — SMS skipped.");
        }
    }

    // ── Check enrollment ──────────────────────────────────────────────────

    public EnrollmentDTO getEnrollment(Long certId, String userIdentifier) {
        return enrollmentRepo.findByUserIdentifierAndCertificationId(userIdentifier, certId)
                .map(this::toDTO)
                .orElse(null);
    }

    public List<EnrollmentDTO> getMyEnrollments(String userIdentifier) {
        return enrollmentRepo.findByUserIdentifier(userIdentifier)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ── Submit exam ───────────────────────────────────────────────────────

    @Transactional
    public ExamResultDTO submitExam(Long certId, SubmitExamRequest req) {
        Enrollment enrollment = enrollmentRepo
                .findByUserIdentifierAndCertificationId(req.userIdentifier(), certId)
                .orElseThrow(() -> new RuntimeException("Not enrolled in this certification"));

        if (enrollment.getStatus() == Enrollment.EnrollmentStatus.COMPLETED) {
            throw new RuntimeException("Exam already submitted");
        }

        // Save all answers
        for (AnswerDTO ans : req.answers()) {
            Question q = questionRepo.findById(ans.questionId())
                    .orElseThrow(() -> new RuntimeException("Question not found: " + ans.questionId()));
            ExamAttempt attempt = ExamAttempt.builder()
                    .enrollment(enrollment)
                    .question(q)
                    .userAnswer(ans.answer())
                    .build();
            attemptRepo.save(attempt);
        }

        // Auto-score: for FILL_BLANK compare case-insensitive; others get full points (manual/AI grading)
        List<Question> questions = examRepo.findByCertificationId(certId).stream()
                .flatMap(exam -> questionRepo.findByExamIdOrderByOrderIndexAsc(exam.getId()).stream())
                .collect(Collectors.toList());

        double totalPoints = questions.stream().mapToDouble(q -> q.getPoints() != null ? q.getPoints() : 10.0).sum();
        double earned = 0;

        for (AnswerDTO ans : req.answers()) {
            Question q = questions.stream().filter(qu -> qu.getId().equals(ans.questionId())).findFirst().orElse(null);
            if (q == null) continue;
            double pts = q.getPoints() != null ? q.getPoints() : 10.0;

            if (q.getType() == Question.QuestionType.FILL_BLANK && q.getExpectedAnswer() != null) {
                // Exact match (case-insensitive, trimmed)
                if (q.getExpectedAnswer().trim().equalsIgnoreCase(ans.answer() != null ? ans.answer().trim() : "")) {
                    earned += pts;
                }
            } else if (q.getType() == Question.QuestionType.MCQ
                    || q.getType() == Question.QuestionType.SCENARIO
                    || q.getType() == Question.QuestionType.MULTI_SELECT) {
                // Objective types — exact match
                if (ans.answer() != null && q.getExpectedAnswer() != null) {
                    String given   = ans.answer().trim().toUpperCase();
                    String correct = q.getExpectedAnswer().trim().toUpperCase();
                    if (given.equals(correct)) earned += pts;
                }
            } else if (q.getType() == Question.QuestionType.ORDERING) {
                // Ordering — exact sequence match
                if (ans.answer() != null && q.getExpectedAnswer() != null
                        && ans.answer().trim().equals(q.getExpectedAnswer().trim())) {
                    earned += pts;
                }
            } else {
                // EXPLAIN, WRITE, CODE (free-text), MATCH, DRAG_DROP — credit for attempting
                if (ans.answer() != null && !ans.answer().isBlank()) {
                    earned += pts;
                }
            }
        }

        double score = totalPoints > 0 ? (earned / totalPoints) * 100 : 0;
        double passingScore = enrollment.getCertification().getPassingScore() != null
                ? enrollment.getCertification().getPassingScore() : 70.0;
        boolean passed = score >= passingScore;

        enrollment.setScore(score);
        enrollment.setPassed(passed);
        enrollment.setStatus(Enrollment.EnrollmentStatus.COMPLETED);
        enrollment.setCompletedAt(LocalDateTime.now());
        // Increment attempt counter and record timestamp for cooldown
        int attempts = enrollment.getAttemptCount() != null ? enrollment.getAttemptCount() : 0;
        enrollment.setAttemptCount(attempts + 1);
        enrollment.setLastAttemptAt(LocalDateTime.now());
        enrollmentRepo.save(enrollment);

        return new ExamResultDTO(enrollment.getId(), score, passed, passingScore,
                questions.size(), req.answers().size());
    }

    // ── Retry exam (free, no new payment, max 2 total attempts, 24h cooldown) ──

    private static final long COOLDOWN_HOURS = 24;

    @Transactional
    public EnrollmentDTO retryExam(Long certId, String userIdentifier) {
        Enrollment enrollment = enrollmentRepo
                .findByUserIdentifierAndCertificationId(userIdentifier, certId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        int used = enrollment.getAttemptCount() != null ? enrollment.getAttemptCount() : 0;
        int max  = enrollment.getMaxAttempts()  != null ? enrollment.getMaxAttempts()  : 2;

        if (Boolean.TRUE.equals(enrollment.getPassed())) {
            throw new RuntimeException("You already passed this certification.");
        }
        if (used >= max) {
            throw new RuntimeException("Maximum attempts (" + max + ") reached. No more retries available.");
        }

        // ── 24-hour cooldown check ────────────────────────────────────────
        if (enrollment.getLastAttemptAt() != null) {
            LocalDateTime cooldownEnd = enrollment.getLastAttemptAt().plusHours(COOLDOWN_HOURS);
            if (LocalDateTime.now().isBefore(cooldownEnd)) {
                long minutesLeft = java.time.Duration.between(LocalDateTime.now(), cooldownEnd).toMinutes();
                long hoursLeft   = minutesLeft / 60;
                long minsLeft    = minutesLeft % 60;
                throw new RuntimeException(
                    "COOLDOWN:" + cooldownEnd.toString() +
                    ":You must wait " + hoursLeft + "h " + minsLeft + "m before retrying."
                );
            }
        }

        // Reset to ENROLLED so the exam can be taken again
        enrollment.setStatus(Enrollment.EnrollmentStatus.ENROLLED);
        enrollment.setScore(null);
        enrollment.setPassed(null);
        enrollment.setCompletedAt(null);
        enrollmentRepo.save(enrollment);

        return toDTO(enrollment);
    }

    // ── Mapper ────────────────────────────────────────────────────────────

    private EnrollmentDTO toDTO(Enrollment e) {
        return new EnrollmentDTO(
                e.getId(),
                e.getUserIdentifier(),
                e.getFullName(),
                e.getCertification().getId(),
                e.getCertification().getTitle(),
                e.getAmountPaid(),
                e.getStatus(),
                e.getScore(),
                e.getPassed(),
                e.getEnrolledAt(),
                e.getCompletedAt(),
                e.getIsVerified(),
                e.getAttemptCount() != null ? e.getAttemptCount() : 0,
                e.getMaxAttempts()  != null ? e.getMaxAttempts()  : 2,
                e.getLastAttemptAt()
        );
    }
}
