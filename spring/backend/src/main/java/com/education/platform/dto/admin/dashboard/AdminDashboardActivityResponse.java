package com.education.platform.dto.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardActivityResponse {

    private List<AdminMonthlyActivityPointResponse> monthlyActivity;
    private List<AdminStatusCountResponse> reclamationsByStatus;
    private List<AdminRecentActivityResponse> recentActivity;
}
