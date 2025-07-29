package com.dashboard.cloud_cost_dashboard.controller;

import com.dashboard.service.interfaces.BudgetService;
import com.dashboard.dto.budget.CreateBudgetRequest;
import com.dashboard.dto.budget.UpdateBudgetRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
@CrossOrigin(origins = "http://localhost:3000")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @PostMapping
    public Object createBudget(@RequestBody CreateBudgetRequest request) {
        System.out.println("Creating budget: " + request.getName());
        return budgetService.createBudget(request);
    }

    @PutMapping("/{budgetId}")
    public Object updateBudget(
            @PathVariable String budgetId,
            @RequestBody UpdateBudgetRequest request) {
        System.out.println("Updating budget: " + budgetId);
        return budgetService.updateBudget(budgetId, request);
    }

    @DeleteMapping("/{budgetId}")
    public void deleteBudget(@PathVariable String budgetId) {
        System.out.println("Deleting budget: " + budgetId);
        budgetService.deleteBudget(budgetId);
    }

    @GetMapping
    public Object getAllBudgets() {
        System.out.println("Fetching all budgets");
        return budgetService.getAllBudgets();
    }

    @GetMapping("/{budgetId}")
    public Object getBudget(@PathVariable String budgetId) {
        System.out.println("Fetching budget: " + budgetId);
        return budgetService.getBudget(budgetId);
    }

    @GetMapping("/alerts")
    public Object getBudgetAlerts() {
        System.out.println("Fetching budget alerts");
        return budgetService.getBudgetAlerts();
    }

    @GetMapping("/{budgetId}/metrics")
    public Object calculateBudgetMetrics(@PathVariable String budgetId) {
        System.out.println("Calculating metrics for budget: " + budgetId);
        return budgetService.calculateBudgetMetrics(budgetId);
    }

    @GetMapping("/analytics")
    public Object getBudgetAnalytics() {
        System.out.println("Fetching budget analytics");
        return budgetService.getBudgetAnalytics();
    }
}