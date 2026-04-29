package com.education.platform.dto.certifications;

import com.education.platform.entities.certifications.Question;
import java.util.List;

public record QuestionResponseDTO(
        Long id,
        Question.QuestionType type,
        String questionText,
        String expectedAnswer,
        String codeLanguage,
        Double points,
        Integer orderIndex,
        List<ChoiceResponseDTO> choices,
        List<String> options            // MCQ / MULTI_SELECT / SCENARIO / ORDERING
) {}
