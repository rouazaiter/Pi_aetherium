package com.education.platform.controllers.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final EnrollmentRepository enrollmentRepo;
    private final ExamAttemptRepository attemptRepo;
    private final QuestionRepository questionRepo;
    private final CertificationRepository certRepo;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAnalytics(
            @RequestParam(required = false) Long certificationId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {

        // ── Date range parsing ────────────────────────────────────────────
        java.time.LocalDateTime from = null, to = null;
        try {
            if (dateFrom != null && !dateFrom.isBlank())
                from = java.time.LocalDate.parse(dateFrom).atStartOfDay();
            if (dateTo != null && !dateTo.isBlank())
                to = java.time.LocalDate.parse(dateTo).atTime(23, 59, 59);
        } catch (Exception ignored) {}

        final java.time.LocalDateTime finalFrom = from;
        final java.time.LocalDateTime finalTo   = to;

        // ── Base data ─────────────────────────────────────────────────────
        List<Enrollment> enrollments = enrollmentRepo.findAll().stream()
                .filter(e -> certificationId == null || e.getCertification().getId().equals(certificationId))
                .filter(e -> difficulty == null || difficulty.isBlank() ||
                        (e.getCertification().getDifficulty() != null &&
                         e.getCertification().getDifficulty().name().equalsIgnoreCase(difficulty)))
                .filter(e -> category == null || category.isBlank() ||
                        (e.getCertification().getCategory() != null &&
                         e.getCertification().getCategory().equalsIgnoreCase(category)))
                .filter(e -> finalFrom == null || (e.getEnrolledAt() != null && !e.getEnrolledAt().isBefore(finalFrom)))
                .filter(e -> finalTo   == null || (e.getEnrolledAt() != null && !e.getEnrolledAt().isAfter(finalTo)))
                .collect(Collectors.toList());

        List<Enrollment> completed = enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.COMPLETED)
                .collect(Collectors.toList());

        long passCount = completed.stream().filter(e -> Boolean.TRUE.equals(e.getPassed())).count();
        long failCount = completed.size() - passCount;
        double avgScore = completed.stream()
                .mapToDouble(e -> e.getScore() != null ? e.getScore() : 0)
                .average().orElse(0);
        double passRate = completed.isEmpty() ? 0 : (passCount * 100.0 / completed.size());

        // ── Score distribution (10 buckets: 0-9, 10-19, ..., 90-100) ─────
        int[] buckets = new int[10];
        for (Enrollment e : completed) {
            if (e.getScore() != null) {
                int idx = Math.min((int)(e.getScore() / 10), 9);
                buckets[idx]++;
            }
        }
        List<Map<String, Object>> scoreDist = new ArrayList<>();
        String[] ranges = {"0-9","10-19","20-29","30-39","40-49","50-59","60-69","70-79","80-89","90-100"};
        for (int i = 0; i < 10; i++) {
            scoreDist.add(Map.of("range", ranges[i], "count", buckets[i]));
        }

        // ── Avg score per certification ───────────────────────────────────
        Map<String, List<Double>> scoresByCert = new LinkedHashMap<>();
        for (Enrollment e : completed) {
            if (e.getScore() != null) {
                String name = truncate(e.getCertification().getTitle(), 30);
                scoresByCert.computeIfAbsent(name, k -> new ArrayList<>()).add(e.getScore());
            }
        }
        List<Map<String, Object>> avgScorePerCert = scoresByCert.entrySet().stream()
                .map(en -> Map.<String, Object>of(
                        "name", en.getKey(),
                        "value", Math.round(en.getValue().stream().mapToDouble(d -> d).average().orElse(0) * 10) / 10.0))
                .collect(Collectors.toList());

        // ── Monthly trend ─────────────────────────────────────────────────
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM yyyy");
        Map<String, List<Double>> byMonth = new TreeMap<>();
        for (Enrollment e : completed) {
            if (e.getCompletedAt() != null && e.getScore() != null) {
                String month = e.getCompletedAt().format(monthFmt);
                byMonth.computeIfAbsent(month, k -> new ArrayList<>()).add(e.getScore());
            }
        }
        List<Map<String, Object>> monthlyTrend = byMonth.entrySet().stream()
                .map(en -> Map.<String, Object>of(
                        "month", en.getKey(),
                        "avgScore", Math.round(en.getValue().stream().mapToDouble(d -> d).average().orElse(0) * 10) / 10.0,
                        "count", en.getValue().size()))
                .collect(Collectors.toList());

        // ── Question difficulty ───────────────────────────────────────────
        List<ExamAttempt> attempts = attemptRepo.findAll().stream()
                .filter(a -> certificationId == null || a.getEnrollment().getCertification().getId().equals(certificationId))
                .filter(a -> difficulty == null || difficulty.isBlank() ||
                        (a.getEnrollment().getCertification().getDifficulty() != null &&
                         a.getEnrollment().getCertification().getDifficulty().name().equalsIgnoreCase(difficulty)))
                .filter(a -> category == null || category.isBlank() ||
                        (a.getEnrollment().getCertification().getCategory() != null &&
                         a.getEnrollment().getCertification().getCategory().equalsIgnoreCase(category)))
                .collect(Collectors.toList());

        Map<Long, long[]> qStats = new HashMap<>(); // [correct, total]
        for (ExamAttempt a : attempts) {
            if (a.getQuestion() == null) continue;
            Long qId = a.getQuestion().getId();
            qStats.computeIfAbsent(qId, k -> new long[]{0, 0});
            qStats.get(qId)[1]++;
            // Simple correctness: for MCQ/FILL_BLANK check against expectedAnswer
            String expected = a.getQuestion().getExpectedAnswer();
            String given    = a.getUserAnswer();
            if (expected != null && given != null &&
                expected.trim().equalsIgnoreCase(given.trim())) {
                qStats.get(qId)[0]++;
            }
        }

        List<Map<String, Object>> questionDifficulty = new ArrayList<>();
        for (Map.Entry<Long, long[]> entry : qStats.entrySet()) {
            long[] s = entry.getValue();
            if (s[1] == 0) continue;
            double correctRate = Math.round((s[0] * 100.0 / s[1]) * 10) / 10.0;
            questionRepo.findById(entry.getKey()).ifPresent(q -> {
                questionDifficulty.add(Map.of(
                        "questionText", truncate(q.getQuestionText(), 45),
                        "correctRate", correctRate,
                        "totalAttempts", s[1]
                ));
            });
        }
        // Sort by correctRate ascending (hardest first)
        questionDifficulty.sort(Comparator.comparingDouble(m -> (Double) m.get("correctRate")));

        List<Map<String, Object>> mostFailed = questionDifficulty.stream()
                .filter(m -> (Double) m.get("correctRate") < 70)
                .limit(10)
                .collect(Collectors.toList());

        // ── Enrollments per certification ─────────────────────────────────
        Map<String, Long> enrollByCert = enrollments.stream()
                .collect(Collectors.groupingBy(
                        e -> truncate(e.getCertification().getTitle(), 30),
                        Collectors.counting()));
        List<Map<String, Object>> enrollmentsPerCert = enrollByCert.entrySet().stream()
                .map(en -> Map.<String, Object>of("name", en.getKey(), "value", en.getValue()))
                .sorted(Comparator.comparingLong(m -> -((Long) m.get("value"))))
                .collect(Collectors.toList());

        // ── Available filter options ──────────────────────────────────────
        List<String> availableCategories = certRepo.findAll().stream()
                .map(c -> c.getCategory()).filter(Objects::nonNull).distinct().sorted()
                .collect(Collectors.toList());

        // ── Build response ────────────────────────────────────────────────
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalEnrollments",   enrollments.size());
        result.put("completedExams",     completed.size());
        result.put("passedExams",        passCount);
        result.put("failedExams",        failCount);
        result.put("overallPassRate",    Math.round(passRate * 10) / 10.0);
        result.put("averageScore",       Math.round(avgScore * 10) / 10.0);
        result.put("passCount",          passCount);
        result.put("failCount",          failCount);
        result.put("scoreDistribution",  scoreDist);
        result.put("avgScorePerCert",    avgScorePerCert);
        result.put("monthlyTrend",       monthlyTrend);
        result.put("questionDifficulty", questionDifficulty);
        result.put("mostFailedQuestions",mostFailed);
        result.put("enrollmentsPerCert", enrollmentsPerCert);
        result.put("availableCategories", availableCategories);

        return ResponseEntity.ok(result);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}
