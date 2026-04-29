package com.education.platform.services.implementations.admin.jihenportfolio;

import com.education.platform.entities.admin.jihenportfolio.AiFeature;
import com.education.platform.entities.admin.jihenportfolio.AiUsageLog;
import com.education.platform.entities.admin.jihenportfolio.ExploreSearchLog;
import com.education.platform.repositories.admin.jihenportfolio.AiUsageLogRepository;
import com.education.platform.repositories.admin.jihenportfolio.ExploreSearchLogRepository;
import com.education.platform.services.interfaces.admin.jihenportfolio.JihenPortfolioAdminTrackingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Jihen Portfolio Admin
@Service
public class JihenPortfolioAdminTrackingServiceImpl implements JihenPortfolioAdminTrackingService {

    private final AiUsageLogRepository aiUsageLogRepository;
    private final ExploreSearchLogRepository exploreSearchLogRepository;

    public JihenPortfolioAdminTrackingServiceImpl(
            AiUsageLogRepository aiUsageLogRepository,
            ExploreSearchLogRepository exploreSearchLogRepository) {
        this.aiUsageLogRepository = aiUsageLogRepository;
        this.exploreSearchLogRepository = exploreSearchLogRepository;
    }

    @Override
    @Transactional
    public void recordAiUsage(AiFeature feature, Long userId, boolean success, long responseTimeMs, String errorMessage) {
        try {
            aiUsageLogRepository.save(AiUsageLog.builder()
                    .feature(feature)
                    .userId(userId)
                    .success(success)
                    .responseTimeMs(responseTimeMs)
                    .errorMessage(trimToNull(errorMessage, 2000))
                    .build());
        } catch (Exception ignored) {
            // Jihen Portfolio Admin: logging must never break AI flows
        }
    }

    @Override
    @Transactional
    public void recordExploreSearch(Long userId, String query, String jobTitle, String filters) {
        boolean hasTextQuery = hasText(query);
        boolean hasTextJobTitle = hasText(jobTitle);
        boolean hasTextFilters = hasText(filters);
        if (!hasTextQuery && !hasTextJobTitle && !hasTextFilters) {
            return;
        }
        try {
            exploreSearchLogRepository.save(ExploreSearchLog.builder()
                    .userId(userId)
                    .query(trimToNull(query, 500))
                    .jobTitle(trimToNull(jobTitle, 500))
                    .filters(trimToNull(filters, 2000))
                    .build());
        } catch (Exception ignored) {
            // Jihen Portfolio Admin: logging must never break Explore flows
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trimToNull(String value, int maxLength) {
        if (!hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}
