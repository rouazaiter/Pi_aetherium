package com.education.platform.dto.certifications;

import com.education.platform.entities.certifications.Question;
import java.util.List;

public record QuestionCreateDTO(
        Question.QuestionType type,
        String questionText,
        Double points,
        Integer orderIndex,
        String expectedAnswer,
        String codeLanguage,
        List<MatchPairDTO> matchPairs,  // MATCH / DRAG_DROP
        List<String> options            // MCQ / MULTI_SELECT / SCENARIO / ORDERING
) {}
