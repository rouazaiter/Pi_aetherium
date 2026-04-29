package com.education.platform.services.implementations.certifications;

import com.education.platform.entities.certifications.*;
import com.education.platform.repositories.certifications.*;
import com.education.platform.dto.certifications.*;

import com.education.platform.dto.certifications.AnalyticsDTO;
import com.education.platform.entities.certifications.Enrollment;
import com.education.platform.entities.certifications.ExamAttempt;
import com.education.platform.entities.certifications.Question;
import com.education.platform.repositories.certifications.CertificationRepository;
import com.education.platform.repositories.certifications.EnrollmentRepository;
import com.education.platform.repositories.certifications.ExamAttemptRepository;
import com.education.platform.repositories.certifications.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final EnrollmentRepository enrollmentRepo;
    private final ExamAttemptRepository attemptRepo;
    private final CertificationRepository certRepo;
    private final QuestionRepository questionRepo;

    public AnalyticsDTO getAnalytics(Long certificationId) {

        // ── Fetch enrollments ─────────────────────────────────────────────
        List<Enrollment> allEnrollments = certificationId != null
                ? enrollmentRepo.findAll().stream()
                    .filter(e -> e.getCertification().getId().equals(certificationId))
                    .collect(Collectors.toList())
                : enrollmentRepo.findAll();

        List<Enrollment> completed = allEnrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.COMPLETED)
                .collect(Collectors.toList());

        long passCount = completed.stream().filter(e -> Boolean.TRUE.equals(e.getPassed())).count();
        long failCount = completed.stream().filter(e -> Boolean.FALSE.equals(e.getPassed())).count();
        double overallPassRate = completed.isEmpty() ? 0 :
                (double) passCount / completed.size() * 100;
        double avgScore = completed.stream()
                .filter(e -> e.getScore() != null)
                .mapToDouble(Enrollment::getScore)
                .average().orElse(0);

        // ── Score distribution ────────────────────────────────────────────
        String[] ranges = {"0-9","10-19","20-29","30-39","40-49","50-59","60-69","70-79","80-89","90-100"};
        long[] buckets = new long[10];
        completed.stream().filter(e -> e.getScore() != null).forEach(e -> {
            int idx = Math.min((int)(e.getScore() / 10), 9);
            buckets[idx]++;
        });
        List<AnalyticsDTO.ScoreBucket> scoreDistribution = new ArrayList<>();
        for (int i = 0; i < 10; i++) scoreDistribution.add(new AnalyticsDTO.ScoreBucket(ranges[i], buckets[i]));

        // ── Avg score per certification ───────────────────────────────────
        Map<String, List<Enrollment>> byCert = completed.stream()
                .collect(Collectors.groupingBy(e -> e.getCertification().getTitle()));
        List<AnalyticsDTO.CertStat> avgScorePerCert = byCert.entrySet().stream()
                .map(entry -> {
                    double avg = entry.getValue().stream()
                            .filter(e -> e.getScore() != null)
                            .mapToDouble(Enrollment::getScore)
                            .average().orElse(0);
                    return new AnalyticsDTO.CertStat(
                            entry.getKey().length() > 30 ? entry.getKey().substring(0, 30) + "…" : entry.getKey(),
                            Math.round(avg * 10.0) / 10.0);
                })
                .sorted(Comparator.comparingDouble(AnalyticsDTO.CertStat::value).reversed())
                .limit(10)
                .collect(Collectors.toList());

        // ── Monthly trend ─────────────────────────────────────────────────
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");
        Map<String, List<Enrollment>> byMonth = completed.stream()
                .filter(e -> e.getCompletedAt() != null)
                .collect(Collectors.groupingBy(e -> e.getCompletedAt().format(fmt)));
        List<AnalyticsDTO.MonthlyTrend> monthlyTrend = byMonth.entrySet().stream()
                .map(entry -> {
                    double avg = entry.getValue().stream()
                            .filter(e -> e.getScore() != null)
                            .mapToDouble(Enrollment::getScore)
                            .average().orElse(0);
                    return new AnalyticsDTO.MonthlyTrend(entry.getKey(),
                            Math.round(avg * 10.0) / 10.0, entry.getValue().size());
                })
                .sorted(Comparator.comparing(AnalyticsDTO.MonthlyTrend::month))
                .collect(Collectors.toList());

        // ── Question difficulty ───────────────────────────────────────────
        List<ExamAttempt> allAttempts = attemptRepo.findAll();
        if (certificationId != null) {
            Set<Long> certEnrollmentIds = allEnrollments.stream()
                    .map(Enrollment::getId).collect(Collectors.toSet());
            allAttempts = allAttempts.stream()
                    .filter(a -> certEnrollmentIds.contains(a.getEnrollment().getId()))
                    .collect(Collectors.toList());
        }

        Map<Long, List<ExamAttempt>> byQuestion = allAttempts.stream()
                .collect(Collectors.groupingBy(a -> a.getQuestion().getId()));

        List<AnalyticsDTO.QuestionDifficulty> questionDifficulty = byQuestion.entrySet().stream()
                .map(entry -> {
                    Question q = entry.getValue().get(0).getQuestion();
                    long total = entry.getValue().size();
                    long correct = entry.getValue().stream()
                            .filter(a -> isCorrect(a, q))
                            .count();
                    double rate = total > 0 ? (double) correct / total * 100 : 0;
                    String text = q.getQuestionText() != null && q.getQuestionText().length() > 50
                            ? q.getQuestionText().substring(0, 50) + "…"
                            : q.getQuestionText();
                    return new AnalyticsDTO.QuestionDifficulty(text, Math.round(rate * 10.0) / 10.0, total);
                })
                .filter(qd -> qd.totalAttempts() >= 1)
                .sorted(Comparator.comparingDouble(AnalyticsDTO.QuestionDifficulty::correctRate))
                .collect(Collectors.toList());

        // Most failed = lowest correct rate
        List<AnalyticsDTO.QuestionDifficulty> mostFailed = questionDifficulty.stream()
                .limit(10)
                .collect(Collectors.toList());

        // ── Enrollments per cert ──────────────────────────────────────────
        Map<String, Long> enrollByCert = allEnrollments.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCertification().getTitle().length() > 30
                                ? e.getCertification().getTitle().substring(0, 30) + "…"
                                : e.getCertification().getTitle(),
                        Collectors.counting()));
        List<AnalyticsDTO.CertStat> enrollmentsPerCert = enrollByCert.entrySet().stream()
                .map(e -> new AnalyticsDTO.CertStat(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingDouble(AnalyticsDTO.CertStat::value).reversed())
                .limit(10)
                .collect(Collectors.toList());

        return new AnalyticsDTO(
                allEnrollments.size(), completed.size(), passCount, failCount,
                Math.round(overallPassRate * 10.0) / 10.0,
                Math.round(avgScore * 10.0) / 10.0,
                passCount, failCount,
                scoreDistribution, avgScorePerCert, monthlyTrend,
                questionDifficulty.stream().limit(20).collect(Collectors.toList()),
                mostFailed, enrollmentsPerCert
        );
    }

    private boolean isCorrect(ExamAttempt attempt, Question q) {
        if (attempt.getUserAnswer() == null || attempt.getUserAnswer().isBlank()) return false;
        if (q.getExpectedAnswer() == null) return !attempt.getUserAnswer().isBlank();
        if (q.getType() == Question.QuestionType.FILL_BLANK) {
            return q.getExpectedAnswer().trim().equalsIgnoreCase(attempt.getUserAnswer().trim());
        }
        if (q.getType() == Question.QuestionType.MCQ ||
            q.getType() == Question.QuestionType.SCENARIO ||
            q.getType() == Question.QuestionType.CODE) {
            return q.getExpectedAnswer().trim().equalsIgnoreCase(attempt.getUserAnswer().trim());
        }
        return !attempt.getUserAnswer().isBlank();
    }
}
