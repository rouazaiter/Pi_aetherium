package com.education.platform.dto.certifications;

import java.util.List;

public record QuestionDTO(
        String type, // "FILL_BLANK", "MATCH", "CODE", "EXPLAIN", "WRITE"
        String questionText,
        Double points,
        List<ChoiceDTO> choices
) {}    