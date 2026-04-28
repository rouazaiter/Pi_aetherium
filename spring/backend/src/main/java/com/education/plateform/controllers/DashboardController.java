package com.education.plateform.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.education.plateform.dto.DashboardCategoryCountDto;
import com.education.plateform.dto.DashboardCategoryStatsDto;
import com.education.plateform.dto.DashboardEventDto;
import com.education.plateform.services.interfaces.DashboardService;

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

    @GetMapping("/categories/counts")
    public ResponseEntity<List<DashboardCategoryCountDto>> getCategoryCounts() {
        return ResponseEntity.ok(dashboardService.getCategoryCounts());
    }
}
