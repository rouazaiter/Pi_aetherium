package com.education.platform.dto.social;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeLeaderboardEntryResponse {

    private int rank;
    private Long userId;
    private String username;
    private int progressValue;
    private int points;
    private boolean completed;
}
