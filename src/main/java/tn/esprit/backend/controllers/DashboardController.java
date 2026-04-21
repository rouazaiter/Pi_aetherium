package tn.esprit.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.backend.dto.DashboardCategoryStatsDto;
import tn.esprit.backend.dto.DashboardEventDto;
import tn.esprit.backend.services.interfaces.DashboardService;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/events")
    public ResponseEntity<List<DashboardEventDto>> getEventsStats() {
        return ResponseEntity.ok(dashboardService.getEvents());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<DashboardCategoryStatsDto>> getCategoryStats() {
        return ResponseEntity.ok(dashboardService.getCategoryStats());
    }
}
