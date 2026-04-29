package com.education.platform.controllers.admin;

import com.education.platform.dto.admin.dashboard.AdminCvSummaryResponse;
import com.education.platform.dto.admin.dashboard.AdminDashboardActivityResponse;
import com.education.platform.dto.admin.dashboard.AdminDashboardSummaryResponse;
import com.education.platform.dto.admin.dashboard.AdminPageResponse;
import com.education.platform.dto.admin.dashboard.AdminPortfolioSummaryResponse;
import com.education.platform.dto.admin.dashboard.AdminSubscriptionSummaryResponse;
import com.education.platform.dto.admin.dashboard.AdminUserDetailResponse;
import com.education.platform.dto.admin.dashboard.AdminUserStatusUpdateRequest;
import com.education.platform.dto.admin.dashboard.AdminUserSummaryResponse;
import com.education.platform.services.interfaces.admin.AdminDashboardService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/dashboard/summary")
    public AdminDashboardSummaryResponse summary() {
        return adminDashboardService.getSummary();
    }

    @GetMapping("/dashboard/activity")
    public AdminDashboardActivityResponse activity() {
        return adminDashboardService.getActivity();
    }

    @GetMapping("/users")
    public AdminPageResponse<AdminUserSummaryResponse> users(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return adminDashboardService.listUsers(q, status, role, page, size);
    }

    @GetMapping("/users/{id}")
    public AdminUserDetailResponse user(@PathVariable Long id) {
        return adminDashboardService.getUser(id);
    }

    @PatchMapping("/users/{id}/status")
    public AdminUserSummaryResponse updateUserStatus(@PathVariable Long id, @Valid @RequestBody AdminUserStatusUpdateRequest request) {
        return adminDashboardService.updateUserStatus(id, request.getActive());
    }

    @GetMapping("/portfolios")
    public AdminPageResponse<AdminPortfolioSummaryResponse> portfolios(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String visibility,
            @RequestParam(required = false) String moderationStatus,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return adminDashboardService.listPortfolios(q, visibility, moderationStatus, page, size);
    }

    @GetMapping("/cvs")
    public AdminPageResponse<AdminCvSummaryResponse> cvs(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return adminDashboardService.listCvs(q, page, size);
    }

    @GetMapping("/subscriptions")
    public AdminPageResponse<AdminSubscriptionSummaryResponse> subscriptions(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return adminDashboardService.listSubscriptions(q, status, page, size);
    }
}
