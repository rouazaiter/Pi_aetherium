package com.education.platform.dto.certifications;

import java.util.List;

public record SubmitExamRequest(
        String userIdentifier,
        List<AnswerDTO> answers
) {}
