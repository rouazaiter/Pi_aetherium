package com.education.plateform.services.interfaces;

import com.education.plateform.dto.DashboardCategoryStatsDto;
import com.education.plateform.dto.DashboardCategoryCountDto;
import com.education.plateform.dto.DashboardEventDto;

import java.util.List;

public interface DashboardService {
    List<DashboardEventDto> getEvents();

    List<DashboardCategoryStatsDto> getCategoryStats();

    List<DashboardCategoryCountDto> getCategoryCounts();
}
