package com.education.platform.dto.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRecentActivityResponse {

    private String type;
    private String title;
    private String subtitle;
    private String status;
    private Instant createdAt;
}
