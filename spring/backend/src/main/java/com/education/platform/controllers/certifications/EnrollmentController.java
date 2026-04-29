package com.education.platform.controllers.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import com.education.platform.dto.certifications.*;
import com.education.platform.services.implementations.certifications.CertificateGenerationService;
import com.education.platform.services.implementations.certifications.CertificationService;
import com.education.platform.services.implementations.certifications.EnrollmentService;
import com.education.platform.services.implementations.certifications.LinkedInPostService;
import com.education.platform.services.implementations.certifications.PracticeQuestionService;
import com.education.platform.services.implementations.certifications.StripePaymentService;
import com.education.platform.repositories.certifications.CertificationRepository;
import com.education.platform.repositories.certifications.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final CertificationService certificationService;
    private final CertificateGenerationService certificateGenerationService;
    private final StripePaymentService stripePaymentService;
    private final CertificationRepository certificationRepository;
    private final LinkedInPostService linkedInPostService;
    private final EnrollmentRepository enrollmentRepository;
    private final PracticeQuestionService practiceQuestionService;

    // ── Store: list published certifications ─────────────────────────────
    @GetMapping("/api/store")
    public ResponseEntity<List<CertificationDTO>> getStore() {
        return ResponseEntity.ok(certificationService.getPublished());
    }

    @GetMapping("/api/store/{id}")
    public ResponseEntity<CertificationDetailDTO> getStoreDetail(@PathVariable Long id) {
        return ResponseEntity.ok(certificationService.getDetail(id));
    }

    @GetMapping("/api/enrollments/{id}/certificate")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long id) {
        byte[] pdf = certificateGenerationService.generateCertificate(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ── Enroll (pay) ──────────────────────────────────────────────────────
    @PostMapping("/api/store/{certId}/enroll")
    public ResponseEntity<?> enroll(@PathVariable Long certId, @RequestBody Map<String, String> data) {
        try {
            String userIdentifier = data.get("userIdentifier");
            String fullName       = data.get("fullName");
            return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enroll(certId, userIdentifier, fullName));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/api/store/{certId}/verify-code")
    public ResponseEntity<?> verifyCode(@PathVariable Long certId, @RequestBody VerifyCodeRequest req) {
        try {
            enrollmentService.verifyCode(req.userIdentifier(), certId, req.code());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    record VerifyCodeRequest(String userIdentifier, String code) {}

    @PostMapping("/api/store/{certId}/confirm-payment")
    public ResponseEntity<?> confirmPayment(@PathVariable Long certId, @RequestBody EnrollRequest req) {
        try {
            enrollmentService.confirmPaymentWithName(req.userIdentifier(), certId, req.fullName(), req.phoneNumber());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ── Check enrollment status ───────────────────────────────────────────
    @GetMapping("/api/store/{certId}/enrollment")
    public ResponseEntity<EnrollmentDTO> getEnrollment(
            @PathVariable Long certId,
            @RequestParam String userIdentifier) {
        EnrollmentDTO dto = enrollmentService.getEnrollment(certId, userIdentifier);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    // ── My enrollments ────────────────────────────────────────────────────
    @GetMapping("/api/enrollments/my")
    public ResponseEntity<List<EnrollmentDTO>> myEnrollments(@RequestParam String userIdentifier) {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(userIdentifier));
    }

    // ── Submit exam ───────────────────────────────────────────────────────
    @PostMapping("/api/store/{certId}/submit")
    public ResponseEntity<?> submitExam(@PathVariable Long certId, @RequestBody SubmitExamRequest req) {
        try {
            return ResponseEntity.ok(enrollmentService.submitExam(certId, req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ── Stripe: create PaymentIntent ──────────────────────────────────────────
    @PostMapping("/api/store/{certId}/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(
            @PathVariable Long certId,
            @RequestBody PaymentIntentRequest req) {
        try {
            var cert = certificationRepository.findById(certId)
                    .orElseThrow(() -> new RuntimeException("Certification not found"));

            if (cert.getPrice() == null || cert.getPrice().compareTo(java.math.BigDecimal.ZERO) == 0) {
                return ResponseEntity.badRequest().body(new ErrorResponse("This certification is free — no payment needed"));
            }

            String clientSecret = stripePaymentService.createPaymentIntent(
                    cert.getPrice(),
                    "SkillHub: " + cert.getTitle()
            );

            // Extract paymentIntentId from clientSecret (format: pi_xxx_secret_yyy)
            String paymentIntentId = clientSecret.split("_secret_")[0];

            return ResponseEntity.ok(new PaymentIntentResponse(
                    clientSecret, paymentIntentId, cert.getPrice().doubleValue()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Payment setup failed: " + e.getMessage()));
        }
    }

    // ── Stripe: confirm payment + send verification email ────────────────────
    @PostMapping("/api/store/{certId}/confirm-stripe-payment")
    public ResponseEntity<?> confirmStripePayment(
            @PathVariable Long certId,
            @RequestBody ConfirmPaymentRequest req) {
        try {
            // 1. Verify payment succeeded with Stripe
            boolean paid = stripePaymentService.verifyPayment(req.paymentIntentId());
            if (!paid) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Payment not confirmed by Stripe"));
            }

            // 2. Pre-create enrollment (not yet verified) and send email code
            enrollmentService.confirmPaymentWithName(req.userIdentifier(), certId, req.fullName(), req.phoneNumber());

            return ResponseEntity.ok(java.util.Map.of(
                "message", "Payment confirmed. Check your email for the verification code.",
                "email", req.userIdentifier()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ── Practice questions (LLM-generated, different from real exam) ─────────
    @GetMapping("/api/store/{certId}/practice-questions")
    public ResponseEntity<?> getPracticeQuestions(
            @PathVariable Long certId,
            @RequestParam(defaultValue = "10") int count) {
        try {
            return ResponseEntity.ok(practiceQuestionService.generatePracticeQuestions(certId, count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to generate practice questions: " + e.getMessage()));
        }
    }

    // ── Retry exam (free, no new payment) ────────────────────────────────────
    @PostMapping("/api/store/{certId}/retry")
    public ResponseEntity<?> retryExam(
            @PathVariable Long certId,
            @RequestBody Map<String, String> body) {
        try {
            String userIdentifier = body.get("userIdentifier");
            return ResponseEntity.ok(enrollmentService.retryExam(certId, userIdentifier));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    record ErrorResponse(String message) {}

    // ── LinkedIn post generation ──────────────────────────────────────────────
    @GetMapping("/api/enrollments/{id}/linkedin-post")
    public ResponseEntity<?> generateLinkedInPost(@PathVariable Long id) {
        try {
            var enrollment = enrollmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Enrollment not found"));
            if (enrollment.getPassed() == null || !enrollment.getPassed()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Enrollment not passed"));
            }
            String post = linkedInPostService.generatePost(enrollment);
            return ResponseEntity.ok(Map.of("post", post));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to generate post: " + e.getMessage()));
        }
    }
}
