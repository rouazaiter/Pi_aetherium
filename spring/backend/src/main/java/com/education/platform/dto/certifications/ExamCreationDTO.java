package com.education.platform.dto.certifications;

import java.util.List;

public record ExamCreationDTO(
        String title,
        Integer timeLimit,
        Double passingScore,
        Long certificationId, // We need to know which cert this belongs to!
        List<QuestionDTO> questions
) {}