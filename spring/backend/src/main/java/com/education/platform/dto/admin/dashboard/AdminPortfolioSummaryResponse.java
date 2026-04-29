package com.education.platform.dto.admin.dashboard;

import com.education.platform.entities.portfolio.ModerationStatus;
import com.education.platform.entities.portfolio.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPortfolioSummaryResponse {

    private Long id;
    private String title;
    private String ownerUsername;
    private String ownerName;
    private Visibility visibility;
    private ModerationStatus moderationStatus;
    private long projectCount;
    private long totalViews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
