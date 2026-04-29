package com.education.platform.services.interfaces.admin;

import com.education.platform.dto.admin.dashboard.AdminCvSummaryResponse;
import com.education.platform.dto.admin.dashboard.AdminDashboardActivityResponse;
import com.education.platform.dto.admin.dashboard.AdminDashboardSummaryResponse;
import com.education.platform.dto.admin.dashboard.AdminPageResponse;
import com.education.platform.dto.admin.dashboard.AdminPortfolioSummaryResponse;
import com.education.platform.dto.admin.dashboard.AdminSubscriptionSummaryResponse;
import com.education.platform.dto.admin.dashboard.AdminUserDetailResponse;
import com.education.platform.dto.admin.dashboard.AdminUserSummaryResponse;

public interface AdminDashboardService {

    AdminDashboardSummaryResponse getSummary();

    AdminDashboardActivityResponse getActivity();

    AdminPageResponse<AdminUserSummaryResponse> listUsers(String q, String status, String role, Integer page, Integer size);

    AdminUserDetailResponse getUser(Long userId);

    AdminUserSummaryResponse updateUserStatus(Long userId, boolean active);

    AdminPageResponse<AdminPortfolioSummaryResponse> listPortfolios(String q, String visibility, String moderationStatus, Integer page, Integer size);

    AdminPageResponse<AdminCvSummaryResponse> listCvs(String q, Integer page, Integer size);

    AdminPageResponse<AdminSubscriptionSummaryResponse> listSubscriptions(String q, String status, Integer page, Integer size);
}
