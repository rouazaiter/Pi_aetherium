package com.education.platform.dto.certifications;

import com.education.platform.entities.certifications.ExamFeedback;

public record FeedbackRequest(
        Long enrollmentId,
        ExamFeedback.DifficultyRating difficultyRating,
        ExamFeedback.TimeRating timeRating,
        ExamFeedback.RelevanceRating relevanceRating,
        String comment
) {}
