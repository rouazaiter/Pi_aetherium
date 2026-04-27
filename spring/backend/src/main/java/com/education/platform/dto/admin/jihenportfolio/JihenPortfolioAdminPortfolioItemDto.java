package com.education.platform.dto.admin.jihenportfolio;

import com.education.platform.entities.portfolio.ModerationStatus;
import com.education.platform.entities.portfolio.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// Jihen Portfolio Admin
@Getter
@Builder
public class JihenPortfolioAdminPortfolioItemDto {

    private Long id;
    private Long ownerId;
    private String ownerName;
    private String ownerUsername;
    private String title;
    private String job;
    private String bio;
    private Visibility visibility;
    private ModerationStatus moderationStatus;
    private String moderationReason;
    private Long views;
    private Integer projectCount;
    private Integer collectionCount;
    private Integer skillCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
