package com.dashboard.service.interfaces;

import com.dashboard.model.budget.Budget;
import com.dashboard.model.budget.BudgetAlert;
import com.dashboard.dto.budget.CreateBudgetRequest;
import com.dashboard.dto.budget.UpdateBudgetRequest;

import java.util.List;

/**
 * Service interface for budget management and tracking
 * Supports multi-scope budgets (team, service, organization-wide) with alerting
 */
public interface BudgetService {
    
    /**
     * Create a new budget with specified parameters
     * @param request Budget configuration including scope, amount, period, and alert thresholds
     * @return Created budget with calculated metrics
     */
    Budget createBudget(CreateBudgetRequest request);
    
    /**
     * Update an existing budget
     * @param budgetId Budget identifier
     * @param request Updated budget parameters
     * @return Updated budget with recalculated metrics
     */
    Budget updateBudget(String budgetId, UpdateBudgetRequest request);
    
    /**
     * Delete a budget
     * @param budgetId Budget identifier
     */
    void deleteBudget(String budgetId);
    
    /**
     * Get all budgets with current spending and forecasts
     * @return List of budgets with real-time metrics
     */
    List<Budget> getAllBudgets();
    
    /**
     * Get budget by ID with detailed metrics
     * @param budgetId Budget identifier
     * @return Budget with spending analysis and projections
     */
    Budget getBudget(String budgetId);
    
    /**
     * Get budgets that have exceeded their alert thresholds
     * @return List of budget alerts with severity levels
     */
    List<BudgetAlert> getBudgetAlerts();
    
    /**
     * Calculate budget utilization and forecast for a specific budget
     * @param budgetId Budget identifier
     * @return Detailed budget analysis with projections
     */
    Object calculateBudgetMetrics(String budgetId);
    
    /**
     * Get budget performance analytics across all budgets
     * @return Summary metrics and trends for budget management
     */
    Object getBudgetAnalytics();
}