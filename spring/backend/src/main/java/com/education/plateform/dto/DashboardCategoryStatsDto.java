package com.education.plateform.dto;

import java.math.BigDecimal;

public record DashboardCategoryStatsDto(
        String category,
        long totalEvents,
        long totalParticipants,
        BigDecimal totalAmount
) {
}
