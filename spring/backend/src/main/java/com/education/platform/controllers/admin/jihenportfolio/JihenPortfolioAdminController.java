package com.education.platform.controllers.admin.jihenportfolio;

import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminAiUsageResponse;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminActivityResponse;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminCollectionItemDto;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminCvUsageResponse;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminFamilyAnalyticsDto;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminModerationRequest;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminOverviewResponse;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminPageResponse;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminPopularProjectDto;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminPortfolioItemDto;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminProjectItemDto;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminRecentItemDto;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminSearchStatDto;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminSkillCategoryDto;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminSkillDto;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminSkillUpsertRequest;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminTrendingSkillDto;
import com.education.platform.dto.admin.jihenportfolio.JihenPortfolioAdminVisibilityRequest;
import com.education.platform.entities.portfolio.ModerationStatus;
import com.education.platform.entities.portfolio.Visibility;
import com.education.platform.services.interfaces.admin.jihenportfolio.JihenPortfolioAdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Jihen Portfolio Admin
@RestController
@RequestMapping("/api/admin-jihen/portfolio")
public class JihenPortfolioAdminController {

    private final JihenPortfolioAdminService adminService;

    public JihenPortfolioAdminController(JihenPortfolioAdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/overview")
    public JihenPortfolioAdminOverviewResponse overview() {
        return adminService.getOverview();
    }

    @GetMapping("/portfolios")
    public JihenPortfolioAdminPageResponse<JihenPortfolioAdminPortfolioItemDto> portfolios(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Visibility visibility,
            @RequestParam(required = false) ModerationStatus moderationStatus,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return adminService.listPortfolios(q, visibility, moderationStatus, page, size, sort);
    }

    @GetMapping("/projects")
    public JihenPortfolioAdminPageResponse<JihenPortfolioAdminProjectItemDto> projects(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Visibility visibility,
            @RequestParam(required = false) ModerationStatus moderationStatus,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return adminService.listProjects(q, visibility, moderationStatus, page, size, sort);
    }

    @GetMapping("/collections")
    public JihenPortfolioAdminPageResponse<JihenPortfolioAdminCollectionItemDto> collections(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Visibility visibility,
            @RequestParam(required = false) ModerationStatus moderationStatus,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return adminService.listCollections(q, visibility, moderationStatus, page, size, sort);
    }

    @GetMapping("/recent-items")
    public List<JihenPortfolioAdminRecentItemDto> recentItems(@RequestParam(required = false) Integer limit) {
        return adminService.getRecentItems(limit);
    }

    @PatchMapping("/portfolios/{portfolioId}/visibility")
    public JihenPortfolioAdminPortfolioItemDto updatePortfolioVisibility(
            @PathVariable Long portfolioId,
            @Valid @RequestBody JihenPortfolioAdminVisibilityRequest request) {
        return adminService.updatePortfolioVisibility(portfolioId, request.getVisibility());
    }

    @PatchMapping("/projects/{projectId}/visibility")
    public JihenPortfolioAdminProjectItemDto updateProjectVisibility(
            @PathVariable Long projectId,
            @Valid @RequestBody JihenPortfolioAdminVisibilityRequest request) {
        return adminService.updateProjectVisibility(projectId, request.getVisibility());
    }

    @PatchMapping("/collections/{collectionId}/visibility")
    public JihenPortfolioAdminCollectionItemDto updateCollectionVisibility(
            @PathVariable Long collectionId,
            @Valid @RequestBody JihenPortfolioAdminVisibilityRequest request) {
        return adminService.updateCollectionVisibility(collectionId, request.getVisibility());
    }

    @PatchMapping("/portfolios/{portfolioId}/moderation")
    public JihenPortfolioAdminPortfolioItemDto updatePortfolioModeration(
            @PathVariable Long portfolioId,
            @Valid @RequestBody JihenPortfolioAdminModerationRequest request) {
        return adminService.updatePortfolioModeration(portfolioId, request.getStatus(), request.getReason());
    }

    @PatchMapping("/projects/{projectId}/moderation")
    public JihenPortfolioAdminProjectItemDto updateProjectModeration(
            @PathVariable Long projectId,
            @Valid @RequestBody JihenPortfolioAdminModerationRequest request) {
        return adminService.updateProjectModeration(projectId, request.getStatus(), request.getReason());
    }

    @PatchMapping("/collections/{collectionId}/moderation")
    public JihenPortfolioAdminCollectionItemDto updateCollectionModeration(
            @PathVariable Long collectionId,
            @Valid @RequestBody JihenPortfolioAdminModerationRequest request) {
        return adminService.updateCollectionModeration(collectionId, request.getStatus(), request.getReason());
    }

    @GetMapping("/skills")
    public List<JihenPortfolioAdminSkillDto> skills(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean trendy) {
        return adminService.listSkills(q, category, trendy);
    }

    @PostMapping("/skills")
    public JihenPortfolioAdminSkillDto createSkill(@Valid @RequestBody JihenPortfolioAdminSkillUpsertRequest request) {
        return adminService.createSkill(request);
    }

    @PutMapping("/skills/{skillId}")
    public JihenPortfolioAdminSkillDto updateSkill(
            @PathVariable Long skillId,
            @Valid @RequestBody JihenPortfolioAdminSkillUpsertRequest request) {
        return adminService.updateSkill(skillId, request);
    }

    @DeleteMapping("/skills/{skillId}")
    public void deleteSkill(@PathVariable Long skillId) {
        adminService.deleteSkill(skillId);
    }

    @GetMapping("/skill-categories")
    public List<JihenPortfolioAdminSkillCategoryDto> skillCategories() {
        return adminService.listSkillCategories();
    }

    @GetMapping("/analytics/families")
    public List<JihenPortfolioAdminFamilyAnalyticsDto> families() {
        return adminService.getFamilyAnalytics();
    }

    @GetMapping("/analytics/trending-skills")
    public List<JihenPortfolioAdminTrendingSkillDto> trendingSkills(@RequestParam(required = false) Integer limit) {
        return adminService.getTrendingSkills(limit);
    }

    @GetMapping("/analytics/popular-projects")
    public List<JihenPortfolioAdminPopularProjectDto> popularProjects(@RequestParam(required = false) Integer limit) {
        return adminService.getPopularProjects(limit);
    }

    @GetMapping("/analytics/cv-usage")
    public JihenPortfolioAdminCvUsageResponse cvUsage() {
        return adminService.getCvUsage();
    }

    @GetMapping("/analytics/ai-usage")
    public JihenPortfolioAdminAiUsageResponse aiUsage() {
        return adminService.getAiUsage();
    }

    @GetMapping("/analytics/activity")
    public JihenPortfolioAdminActivityResponse activity(@RequestParam(required = false) String range) {
        return adminService.getActivity(range);
    }

    @GetMapping("/analytics/search")
    public List<JihenPortfolioAdminSearchStatDto> searchAnalytics() {
        return adminService.getSearchAnalytics();
    }
}
