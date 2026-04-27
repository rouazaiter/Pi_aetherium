package com.education.platform.dto.cv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CvAiChatResponse {

    private final String reply;
    private final int score;
    private final List<CvAiSuggestedAction> suggestedActions;
}
