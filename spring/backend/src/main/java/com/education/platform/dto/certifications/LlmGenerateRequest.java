package com.education.platform.dto.certifications;

import com.education.platform.entities.certifications.Certification;

public record LlmGenerateRequest(
        String topic,
        String description,
        Certification.Difficulty difficulty,
        int numberOfQuestions,
        int timeLimitMinutes
) {}
