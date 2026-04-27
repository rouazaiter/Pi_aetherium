package com.education.platform.dto.admin.jihenportfolio;

import com.education.platform.entities.portfolio.ModerationStatus;
import com.education.platform.entities.portfolio.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// Jihen Portfolio Admin
@Getter
@Builder
public class JihenPortfolioAdminCollectionItemDto {

    private Long id;
    private Long portfolioId;
    private Long ownerId;
    private String ownerName;
    private String name;
    private String description;
    private Visibility visibility;
    private ModerationStatus moderationStatus;
    private String moderationReason;
    private Integer projectCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
