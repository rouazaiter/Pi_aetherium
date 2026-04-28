package tn.esprit.aetherium.skillhubbackend.controllers.blog;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.aetherium.skillhubbackend.entities.blog.Report;
import tn.esprit.aetherium.skillhubbackend.services.implementations.ReportServiceImpl;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportServiceImpl reportService;

    public ReportController(ReportServiceImpl reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<Report> submit(
            @RequestParam Long reporterId,
            @RequestParam Report.TargetType targetType,
            @RequestParam Long targetId,
            @RequestParam Report.ReportReason reason,
            @RequestBody(required = false) Map<String, String> body) {
        String details = body != null ? body.getOrDefault("details", "") : "";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.submitReport(reporterId, targetType, targetId, reason, details));
    }

    // Admin endpoints
    @GetMapping
    public ResponseEntity<List<Report>> getAll() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Report>> getPending() {
        return ResponseEntity.ok(reportService.getPendingReports());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Report> updateStatus(
            @PathVariable Long id,
            @RequestParam Report.ReportStatus status) {
        return ResponseEntity.ok(reportService.updateStatus(id, status));
    }
}
