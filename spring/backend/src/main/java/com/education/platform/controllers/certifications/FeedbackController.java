package com.education.platform.controllers.certifications;

import com.education.platform.dto.certifications.FeedbackInsightsDTO;
import com.education.platform.dto.certifications.FeedbackRequest;
import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class FeedbackController {

    private final ExamFeedbackRepository feedbackRepo;
    private final EnrollmentRepository   enrollmentRepo;
    private final CertificationRepository certRepo;

    // ── POST /api/feedback ────────────────────────────────────────────────────
    @PostMapping("/api/feedback")
    public ResponseEntity<?> submit(@RequestBody FeedbackRequest req) {
        try {
            // Prevent duplicate feedback per enrollment
            if (feedbackRepo.existsByEnrollmentId(req.enrollmentId())) {
                return ResponseEntity.ok(Map.of("message", "Feedback already submitted"));
            }

            Enrollment enrollment = enrollmentRepo.findById(req.enrollmentId())
                    .orElseThrow(() -> new RuntimeException("Enrollment not found"));

            if (!Boolean.TRUE.equals(enrollment.getPassed())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Feedback only available for passed exams"));
            }

            ExamFeedback fb = ExamFeedback.builder()
                    .userIdentifier(enrollment.getUserIdentifier())
                    .enrollment(enrollment)
                    .certification(enrollment.getCertification())
                    .score(enrollment.getScore())
                    .difficultyRating(req.difficultyRating())
                    .timeRating(req.timeRating())
                    .relevanceRating(req.relevanceRating())
                    .comment(req.comment() != null ? req.comment().trim() : null)
                    .build();

            feedbackRepo.save(fb);
            return ResponseEntity.ok(Map.of("message", "Feedback submitted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── GET /api/feedback/check/{enrollmentId} ────────────────────────────────
    @GetMapping("/api/feedback/check/{enrollmentId}")
    public ResponseEntity<Map<String, Boolean>> checkSubmitted(@PathVariable Long enrollmentId) {
        return ResponseEntity.ok(Map.of("submitted", feedbackRepo.existsByEnrollmentId(enrollmentId)));
    }

    // ── GET /api/feedback/insights ────────────────────────────────────────────
    @GetMapping("/api/feedback/insights")
    public ResponseEntity<List<FeedbackInsightsDTO>> getInsights(
            @RequestParam(required = false) Long certificationId) {

        List<ExamFeedback> feedbacks = certificationId != null
                ? feedbackRepo.findByCertificationId(certificationId)
                : feedbackRepo.findAll();

        // Group by certification
        Map<Long, List<ExamFeedback>> byCert = feedbacks.stream()
                .collect(Collectors.groupingBy(f -> f.getCertification().getId()));

        List<FeedbackInsightsDTO> insights = new ArrayList<>();

        for (Map.Entry<Long, List<ExamFeedback>> entry : byCert.entrySet()) {
            List<ExamFeedback> list = entry.getValue();
            if (list.isEmpty()) continue;

            String certTitle = list.get(0).getCertification().getTitle();
            long total = list.size();

            // Difficulty breakdown
            Map<String, Long> diffBreak = list.stream()
                    .collect(Collectors.groupingBy(
                            f -> f.getDifficultyRating().name(), Collectors.counting()));
            String domDiff = dominantKey(diffBreak);

            // Time breakdown
            Map<String, Long> timeBreak = list.stream()
                    .collect(Collectors.groupingBy(
                            f -> f.getTimeRating().name(), Collectors.counting()));
            String domTime = dominantKey(timeBreak);

            // Relevance breakdown
            Map<String, Long> relBreak = list.stream()
                    .collect(Collectors.groupingBy(
                            f -> f.getRelevanceRating().name(), Collectors.counting()));
            String domRel = dominantKey(relBreak);

            // Avg score
            double avgScore = list.stream()
                    .mapToDouble(f -> f.getScore() != null ? f.getScore() : 0)
                    .average().orElse(0);

            // Flags (>50% of responses)
            boolean tooHard     = pct(diffBreak, "HARD",     total) > 50;
            boolean tooEasy     = pct(diffBreak, "EASY",     total) > 50;
            boolean notRelevant = pct(relBreak,  "NO",       total) > 40;
            boolean timeTooShort= pct(timeBreak, "TOO_SHORT",total) > 50;

            insights.add(new FeedbackInsightsDTO(
                    entry.getKey(), certTitle, total,
                    diffBreak, domDiff,
                    timeBreak, domTime,
                    relBreak,  domRel,
                    tooHard, tooEasy, notRelevant, timeTooShort,
                    Math.round(avgScore * 10) / 10.0
            ));
        }

        // Sort by total feedbacks desc
        insights.sort(Comparator.comparingLong(FeedbackInsightsDTO::totalFeedbacks).reversed());
        return ResponseEntity.ok(insights);
    }

    private String dominantKey(Map<String, Long> map) {
        return map.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    private double pct(Map<String, Long> map, String key, long total) {
        if (total == 0) return 0;
        return (map.getOrDefault(key, 0L) * 100.0) / total;
    }
}
