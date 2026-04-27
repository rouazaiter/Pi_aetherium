package com.education.platform.dto.social;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizLeaderboardEntryResponse {

    private int rank;
    private Long userId;
    private String username;
    private int quizzesAnswered;
    private int correctAnswers;
    private int totalScore;
    private int accuracyPercent;
}
