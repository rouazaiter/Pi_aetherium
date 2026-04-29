package com.education.platform.controllers.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import com.education.platform.dto.certifications.CertificateVerificationDTO;
import com.education.platform.entities.certifications.Enrollment;
import com.education.platform.repositories.certifications.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
public class VerificationController {

    private final EnrollmentRepository enrollmentRepository;

    // ── GET /api/verify?id=SKH-000017  (QR code / direct link) ───────────────
    @GetMapping("/api/verify")
    public ResponseEntity<CertificateVerificationDTO> verifyById(@RequestParam String id) {
        return ResponseEntity.ok(doVerify(id));
    }

    // ── POST /api/verify/upload  (PDF file upload) ────────────────────────────
    @PostMapping(value = "/api/verify/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CertificateVerificationDTO> verifyByUpload(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(invalidResult("NO_FILE"));
        }

        // Only accept PDFs
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            return ResponseEntity.badRequest().body(invalidResult("INVALID_FORMAT"));
        }

        // Extract text from PDF and find SKH-XXXXXX pattern
        String certId;
        try {
            certId = extractCertIdFromPdf(file);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(invalidResult("PARSE_ERROR"));
        }

        if (certId == null) {
            return ResponseEntity.ok(invalidResult("NOT_FOUND_IN_PDF"));
        }

        return ResponseEntity.ok(doVerify(certId));
    }

    // ── Core verification logic ───────────────────────────────────────────────

    private CertificateVerificationDTO doVerify(String id) {
        String normalised = id.trim().toUpperCase();
        if (!normalised.startsWith("SKH-")) {
            normalised = "SKH-" + String.format("%06d", parseLong(normalised));
        }

        final String certId = normalised;
        Optional<Enrollment> opt = enrollmentRepository.findByCertificateId(certId);

        if (opt.isEmpty()) {
            return invalidResult(certId);
        }

        Enrollment e = opt.get();
        boolean valid = Boolean.TRUE.equals(e.getPassed())
                && e.getStatus() == Enrollment.EnrollmentStatus.COMPLETED;

        String holderName = resolveDisplayName(e);
        String category   = e.getCertification().getCategory() != null
                ? e.getCertification().getCategory() : "Technology";
        String difficulty = e.getCertification().getDifficulty() != null
                ? e.getCertification().getDifficulty().name() : "N/A";

        return new CertificateVerificationDTO(
                certId, valid,
                holderName,
                e.getCertification().getTitle(),
                category, difficulty,
                e.getScore() != null ? e.getScore() : 0,
                Boolean.TRUE.equals(e.getPassed()),
                e.getCompletedAt(),
                "Kahia Ghassen", "Chief Executive Officer", "SkillHub"
        );
    }

    // ── PDF text extraction ───────────────────────────────────────────────────

    private String extractCertIdFromPdf(MultipartFile file) throws IOException {
        try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            Pattern pattern = Pattern.compile("SKH-\\d{6}", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group().toUpperCase();
            }
            return null;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CertificateVerificationDTO invalidResult(String certId) {
        return new CertificateVerificationDTO(
                certId, false,
                null, null, null, null,
                0, false, null,
                null, null, null
        );
    }

    private String resolveDisplayName(Enrollment e) {
        String name = e.getFullName();
        if (name == null || name.isBlank()) {
            String email = e.getUserIdentifier();
            name = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
            String[] parts = name.replace('.', ' ').replace('_', ' ').split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (String p : parts) {
                if (!p.isEmpty())
                    sb.append(Character.toUpperCase(p.charAt(0)))
                      .append(p.substring(1).toLowerCase()).append(" ");
            }
            name = sb.toString().trim();
        }
        return name;
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s.replaceAll("[^0-9]", "")); }
        catch (NumberFormatException e) { return 0; }
    }
}
