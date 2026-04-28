package com.education.plateform.services.interfaces;

import com.education.plateform.dto.LeaderboardResponseDto;
import com.education.plateform.entities.ServiceRequestCategory;

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
