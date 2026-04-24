package tn.esprit.backend.services.interfaces;

import tn.esprit.backend.dto.LeaderboardResponseDto;
import tn.esprit.backend.entities.ServiceRequestCategory;

public interface LeaderboardService {
    default LeaderboardResponseDto getApplicantsLeaderboard(int days, int limit) {
        return getApplicantsLeaderboard(days, limit, null);
    }

    default LeaderboardResponseDto getCreatorsLeaderboard(int days, int limit) {
        return getCreatorsLeaderboard(days, limit, null);
    }

    LeaderboardResponseDto getApplicantsLeaderboard(int days, int limit, ServiceRequestCategory category);
    LeaderboardResponseDto getCreatorsLeaderboard(int days, int limit, ServiceRequestCategory category);
}
