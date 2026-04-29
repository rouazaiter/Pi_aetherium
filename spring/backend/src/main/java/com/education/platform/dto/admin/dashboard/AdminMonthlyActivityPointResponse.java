package com.education.platform.dto.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMonthlyActivityPointResponse {

    private String label;
    private long users;
    private long portfolios;
    private long cvDrafts;
    private long reclamations;
    private long subscriptions;
}
