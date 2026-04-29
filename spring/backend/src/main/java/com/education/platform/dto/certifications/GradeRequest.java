package com.education.platform.dto.certifications;

public record GradeRequest(
        String questionType,      // EXPLAIN, WRITE, CODE, etc.
        String questionText,
        String expectedAnswer,    // model answer / rubric
        String userAnswer,
        String codeLanguage       // nullable, for CODE type
) {}
