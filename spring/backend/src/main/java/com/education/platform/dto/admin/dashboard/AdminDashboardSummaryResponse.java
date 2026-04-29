package com.education.platform.dto.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardSummaryResponse {

    private long totalUsers;
    private long totalStudents;
    private long totalAdmins;
    private long totalPortfolios;
    private long totalCvProfiles;
    private long totalCvDrafts;
    private long totalReclamations;
    private long totalSubscriptions;
    private long activeSubscriptions;
    private long pendingReclamations;
    private long pendingAdminActions;
}
