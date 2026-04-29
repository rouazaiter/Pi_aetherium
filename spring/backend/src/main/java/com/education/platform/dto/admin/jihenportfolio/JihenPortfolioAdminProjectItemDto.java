package com.education.platform.dto.admin.jihenportfolio;

import com.education.platform.entities.portfolio.ModerationStatus;
import com.education.platform.entities.portfolio.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

// Jihen Portfolio Admin
@Getter
@Builder
public class JihenPortfolioAdminProjectItemDto {

    private Long id;
    private Long portfolioId;
    private String portfolioTitle;
    private Long ownerId;
    private String ownerName;
    private String title;
    private String description;
    private Visibility visibility;
    private ModerationStatus moderationStatus;
    private String moderationReason;
    private Integer views;
    private Integer likes;
    private List<String> skillNames;
    private List<String> skillIcons;
    private Integer mediaCount;
    private String mediaThumbnailUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
