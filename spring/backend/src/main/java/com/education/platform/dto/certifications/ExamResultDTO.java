package com.education.platform.dto.certifications;

public record ExamResultDTO(
        Long enrollmentId,
        double score,
        boolean passed,
        double passingScore,
        int totalQuestions,
        int answeredQuestions
) {}
