package tn.esprit.backend.services.interfaces;

import tn.esprit.backend.dto.DashboardCategoryStatsDto;
import tn.esprit.backend.dto.DashboardEventDto;

import java.util.List;

public interface DashboardService {
    List<DashboardEventDto> getEvents();

    List<DashboardCategoryStatsDto> getCategoryStats();
}
