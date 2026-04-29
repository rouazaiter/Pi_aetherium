package com.education.platform.dto.certifications;

public record ChoiceResponseDTO(
        Long id,
        String matchLeft,
        String matchRight,
        String text,
        boolean isCorrect
) {}
