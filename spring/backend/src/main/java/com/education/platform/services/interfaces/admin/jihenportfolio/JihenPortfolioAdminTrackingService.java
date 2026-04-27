package com.education.platform.services.interfaces.admin.jihenportfolio;

import com.education.platform.entities.admin.jihenportfolio.AiFeature;

// Jihen Portfolio Admin
public interface JihenPortfolioAdminTrackingService {

    void recordAiUsage(AiFeature feature, Long userId, boolean success, long responseTimeMs, String errorMessage);

    void recordExploreSearch(Long userId, String query, String jobTitle, String filters);
}
