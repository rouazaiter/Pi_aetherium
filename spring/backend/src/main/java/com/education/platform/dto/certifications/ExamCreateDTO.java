package com.education.platform.dto.certifications;

import java.util.List;

public record ExamCreateDTO(
        String title,
        Integer timeLimit,
        Double passingScore,
        List<QuestionCreateDTO> questions
) {}
