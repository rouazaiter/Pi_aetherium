package com.education.platform.dto.admin.dashboard;

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
public class AdminCvSummaryResponse {

    private Long userId;
    private Long profileId;
    private String username;
    private String fullName;
    private String headline;
    private Visibility visibility;
    private String preferredTemplate;
    private long draftCount;
    private LocalDateTime profileUpdatedAt;
    private LocalDateTime latestDraftUpdatedAt;
}
