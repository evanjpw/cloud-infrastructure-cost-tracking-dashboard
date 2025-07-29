package com.dashboard.cloud_cost_dashboard.controller;

import com.dashboard.service.interfaces.OptimizationService;
import com.dashboard.dto.optimization.OptimizationRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/optimization")
@CrossOrigin(origins = "http://localhost:3000")
public class OptimizationController {

    @Autowired
    private OptimizationService optimizationService;

    @PostMapping("/recommendations")
    public Object generateRecommendations(@RequestBody OptimizationRequest request) {
        System.out.println("Generating optimization recommendations for scope: " + request.getScope());
        return optimizationService.generateRecommendations(request);
    }

    @GetMapping("/summary")
    public Object getOptimizationSummary(
            @RequestParam(required = false) String teamName,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        System.out.println("Generating optimization summary for team: " + teamName);
        return optimizationService.getOptimizationSummary(teamName, startDate, endDate);
    }

    @GetMapping("/recommendations/{recommendationId}")
    public Object analyzeRecommendation(@PathVariable String recommendationId) {
        System.out.println("Analyzing recommendation: " + recommendationId);
        return optimizationService.analyzeRecommendation(recommendationId);
    }

    @PostMapping("/recommendations/{recommendationId}/status")
    public Object updateRecommendationStatus(
            @PathVariable String recommendationId,
            @RequestParam String action,
            @RequestParam(required = false) String notes) {
        System.out.println("Updating recommendation " + recommendationId + " with action: " + action);
        return optimizationService.updateRecommendationStatus(recommendationId, action, notes);
    }

    @GetMapping("/analytics")
    public Object getOptimizationAnalytics() {
        System.out.println("Fetching optimization analytics");
        return optimizationService.getOptimizationAnalytics();
    }
}