package com.servicedesk.controller;

import com.servicedesk.dto.AnalyticsSummaryResponse;
import com.servicedesk.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsSummaryResponse> getAnalytics() {
        AnalyticsSummaryResponse summary = analyticsService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }
}
