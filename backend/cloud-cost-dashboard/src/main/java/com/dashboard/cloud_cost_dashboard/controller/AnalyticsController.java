package com.dashboard.cloud_cost_dashboard.controller;

import com.dashboard.service.interfaces.AnalyticsService;
import com.dashboard.dto.analytics.PredictionRequest;
import com.dashboard.dto.analytics.TrendAnalysisRequest;
import com.dashboard.dto.analytics.ComparisonRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:3000")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @PostMapping("/predictions")
    public Object generatePredictions(@RequestBody PredictionRequest request) {
        System.out.println("Generating cost predictions for method: " + request.getMethod());
        return analyticsService.predictCosts(request);
    }

    @PostMapping("/trends")
    public Object analyzeTrends(@RequestBody TrendAnalysisRequest request) {
        System.out.println("Analyzing trends for team: " + request.getTeamName());
        return analyticsService.analyzeTrends(request);
    }

    @PostMapping("/comparison")
    public Object compareEntities(@RequestBody ComparisonRequest request) {
        System.out.println("Comparing entities by: " + request.getComparisonType());
        return analyticsService.compareEntities(request);
    }

    @GetMapping("/anomalies")
    public Object detectAnomalies(
            @RequestParam String teamName,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "2.0") Double threshold) {
        System.out.println("Detecting anomalies for team: " + teamName);
        return analyticsService.detectAnomalies(teamName, startDate, endDate, threshold);
    }
}