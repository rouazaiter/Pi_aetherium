package com.education.platform.dto.admin.jihenportfolio;

import com.education.platform.entities.portfolio.ModerationStatus;
import com.education.platform.entities.portfolio.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// Jihen Portfolio Admin
@Getter
@Builder
public class JihenPortfolioAdminRecentItemDto {

    private String type;
    private Long id;
    private String title;
    private String subtitle;
    private String ownerName;
    private String ownerUsername;
    private Visibility visibility;
    private ModerationStatus moderationStatus;
    private Long views;
    private LocalDateTime createdAt;
}
