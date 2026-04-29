package com.education.platform.dto.certifications;

import java.util.List;

public record ExamResponseDTO(
        Long id,
        String title,
        Integer timeLimit,
        Double passingScore,
        List<QuestionResponseDTO> questions
) {}
