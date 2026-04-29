package com.education.platform.controllers.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import com.education.platform.dto.certifications.*;
import com.education.platform.services.implementations.certifications.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/certifications")
@RequiredArgsConstructor
public class CertificationController {

    private final CertificationService certificationService;
    private final LlmCertificationService llmService;
    private final PdfCertificationService pdfService;
    private final ImageGenerationService imageGenerationService;
    private final com.education.platform.repositories.certifications.CertificationRepository certificationRepository;

    // ─── CRUD ────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<CertificationDTO>> getAll() {
        return ResponseEntity.ok(certificationService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificationDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(certificationService.getById(id));
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<CertificationDetailDTO> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(certificationService.getDetail(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CertificationDTO>> search(@RequestParam String title) {
        return ResponseEntity.ok(certificationService.search(title));
    }

    @PostMapping
    public ResponseEntity<CertificationDTO> create(@RequestBody CertificationCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(certificationService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CertificationDTO> update(@PathVariable Long id, @RequestBody CertificationCreateDTO dto) {
        return ResponseEntity.ok(certificationService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        certificationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─── LLM GENERATION ──────────────────────────────────────────────────────

    @PostMapping("/generate")
    public ResponseEntity<?> generateFromLlm(@RequestBody LlmGenerateRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(llmService.generateAndSave(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("LLM generation failed: " + e.getMessage()));
        }
    }

    // ─── PDF IMPORT ───────────────────────────────────────────────────────────

    @PostMapping("/import-pdf")
    public ResponseEntity<?> importFromPdf(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("File is empty"));
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(pdfService.convertPdfAndSave(file));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("PDF import failed: " + e.getMessage()));
        }
    }

    // ─── GENERATE COVER IMAGE ─────────────────────────────────────────────────

    @PostMapping("/{id}/generate-image")
    public ResponseEntity<?> generateImage(@PathVariable Long id) {
        try {
            var cert = certificationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Certification not found: " + id));
            String url = imageGenerationService.generateCoverImageUrl(
                    cert.getTitle(),
                    cert.getCategory(),
                    cert.getDifficulty() != null ? cert.getDifficulty().name() : "INTERMEDIATE"
            );
            cert.setCoverImageUrl(url);
            certificationRepository.save(cert);
            return ResponseEntity.ok(java.util.Map.of("coverImageUrl", url != null ? url : ""));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Image generation failed: " + e.getMessage()));
        }
    }

    record ErrorResponse(String message) {
    }
}
