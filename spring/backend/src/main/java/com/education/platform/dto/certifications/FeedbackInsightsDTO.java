package com.education.platform.dto.certifications;

import java.util.Map;

public record FeedbackInsightsDTO(
        Long certificationId,
        String certificationTitle,
        long totalFeedbacks,

        // Difficulty breakdown
        Map<String, Long> difficultyBreakdown,
        String dominantDifficulty,

        // Time breakdown
        Map<String, Long> timeBreakdown,
        String dominantTime,

        // Relevance breakdown
        Map<String, Long> relevanceBreakdown,
        String dominantRelevance,

        // Alerts for admin
        boolean flaggedTooHard,
        boolean flaggedTooEasy,
        boolean flaggedNotRelevant,
        boolean flaggedTimeTooShort,

        // Average score of users who gave feedback
        double avgScore
) {}
