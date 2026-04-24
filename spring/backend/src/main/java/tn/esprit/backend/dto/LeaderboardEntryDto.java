package tn.esprit.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntryDto {
    private Long userId;
    private String username;
    private String email;
    private int totalCount;
    private int successCount;
    private int pendingCount;
    private int failedCount;
    private double successRate;
    private double realScore;
    private double aiQualityScore;
    private double score;
    private int rank;
    private String badge;
}