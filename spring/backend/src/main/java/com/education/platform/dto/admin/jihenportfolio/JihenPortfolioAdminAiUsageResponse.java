package com.education.platform.dto.admin.jihenportfolio;

import lombok.Builder;
import lombok.Getter;

// Jihen Portfolio Admin
@Getter
@Builder
public class JihenPortfolioAdminAiUsageResponse {

    private Long portfolioMentorRequests;
    private Long cvImproveRequests;
    private Long cvChatRequests;
    private Long jobMatchedCvRequests;
    private Long failedAiRequests;
    private Long successfulAiRequests;
    private Long averageResponseTimeMs;
    private Long averagePortfolioMentorResponseTimeMs;
    private Long averageCvResponseTimeMs;
}
