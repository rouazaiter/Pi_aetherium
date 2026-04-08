package tn.esprit.backend.services.interfaces;

import tn.esprit.backend.dto.LeaderboardResponseDto;

public interface LeaderboardService {
    LeaderboardResponseDto getApplicantsLeaderboard(int days, int limit);
    LeaderboardResponseDto getCreatorsLeaderboard(int days, int limit);
}