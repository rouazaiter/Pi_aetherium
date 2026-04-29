package com.education.platform.dto.certifications;

public record GradeResponse(
        boolean correct,
        int score,          // 0–100
        String feedback,    // short explanation shown to the user
        String modelAnswer  // the expected answer, shown when wrong
) {}
