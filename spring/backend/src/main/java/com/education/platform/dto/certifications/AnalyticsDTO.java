package com.education.platform.dto.certifications;

import java.util.List;
import java.util.Map;

public record AnalyticsDTO(

    // ── Overview ──────────────────────────────────────────────────────────
    long totalEnrollments,
    long completedExams,
    long passedExams,
    long failedExams,
    double overallPassRate,       // 0-100
    double averageScore,          // 0-100

    // ── Pass / Fail pie ───────────────────────────────────────────────────
    long passCount,
    long failCount,

    // ── Score distribution histogram (buckets: 0-9,10-19,...,90-100) ─────
    List<ScoreBucket> scoreDistribution,

    // ── Average score per certification ──────────────────────────────────
    List<CertStat> avgScorePerCert,

    // ── Performance trend (monthly) ───────────────────────────────────────
    List<MonthlyTrend> monthlyTrend,

    // ── Question difficulty (% correct per question) ─────────────────────
    List<QuestionDifficulty> questionDifficulty,

    // ── Most failed questions ─────────────────────────────────────────────
    List<QuestionDifficulty> mostFailedQuestions,

    // ── Enrollments per certification ─────────────────────────────────────
    List<CertStat> enrollmentsPerCert

) {
    public record ScoreBucket(String range, long count) {}
    public record CertStat(String name, double value) {}
    public record MonthlyTrend(String month, double avgScore, long count) {}
    public record QuestionDifficulty(String questionText, double correctRate, long totalAttempts) {}
}
