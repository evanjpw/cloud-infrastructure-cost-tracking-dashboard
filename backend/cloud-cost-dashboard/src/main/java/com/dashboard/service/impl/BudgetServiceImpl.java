package com.dashboard.service.impl;

import com.dashboard.service.interfaces.BudgetService;
import com.dashboard.model.budget.Budget;
import com.dashboard.model.budget.BudgetAlert;
import com.dashboard.dto.budget.CreateBudgetRequest;
import com.dashboard.dto.budget.UpdateBudgetRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
public class BudgetServiceImpl implements BudgetService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Budget createBudget(CreateBudgetRequest request) {
        System.out.println("Creating budget: " + request.getName());
        
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid budget request: " + request);
        }
        
        // Generate ID and create budget
        String budgetId = UUID.randomUUID().toString();
        
        String sql = """
            INSERT INTO budgets 
            (id, name, amount, period, scope, target, alert_threshold, start_date, end_date, 
             status, created_by, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'active', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;
        
        jdbcTemplate.update(sql,
            budgetId,
            request.getName(),
            request.getAmount(),
            request.getPeriod(),
            request.getScope(),
            request.getTarget(),
            request.getAlertThreshold(),
            request.getStartDate(),
            request.getEndDate(),
            request.getCreatedBy()
        );
        
        // Return the created budget with calculated metrics
        Budget budget = getBudget(budgetId);
        System.out.println("Created budget with ID: " + budgetId);
        return budget;
    }

    @Override
    public Budget updateBudget(String budgetId, UpdateBudgetRequest request) {
        System.out.println("Updating budget: " + budgetId);
        
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid update request: " + request);
        }
        
        // Build dynamic update query based on provided fields
        List<String> setParts = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        
        if (request.getName() != null) {
            setParts.add("name = ?");
            params.add(request.getName());
        }
        if (request.getAmount() != null) {
            setParts.add("amount = ?");
            params.add(request.getAmount());
        }
        if (request.getPeriod() != null) {
            setParts.add("period = ?");
            params.add(request.getPeriod());
        }
        if (request.getScope() != null) {
            setParts.add("scope = ?");
            params.add(request.getScope());
        }
        if (request.getTarget() != null) {
            setParts.add("target = ?");
            params.add(request.getTarget());
        }
        if (request.getAlertThreshold() != null) {
            setParts.add("alert_threshold = ?");
            params.add(request.getAlertThreshold());
        }
        if (request.getStartDate() != null) {
            setParts.add("start_date = ?");
            params.add(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            setParts.add("end_date = ?");
            params.add(request.getEndDate());
        }
        if (request.getStatus() != null) {
            setParts.add("status = ?");
            params.add(request.getStatus());
        }
        
        if (setParts.isEmpty()) {
            throw new IllegalArgumentException("No fields provided for update");
        }
        
        setParts.add("updated_at = CURRENT_TIMESTAMP");
        params.add(budgetId);
        
        String sql = "UPDATE budgets SET " + String.join(", ", setParts) + " WHERE id = ?";
        
        int rowsUpdated = jdbcTemplate.update(sql, params.toArray());
        
        if (rowsUpdated == 0) {
            throw new RuntimeException("Budget not found: " + budgetId);
        }
        
        Budget budget = getBudget(budgetId);
        System.out.println("Updated budget: " + budgetId);
        return budget;
    }

    @Override
    public void deleteBudget(String budgetId) {
        System.out.println("Deleting budget: " + budgetId);
        
        String sql = "DELETE FROM budgets WHERE id = ?";
        int rowsDeleted = jdbcTemplate.update(sql, budgetId);
        
        if (rowsDeleted == 0) {
            throw new RuntimeException("Budget not found: " + budgetId);
        }
        
        // Also delete related alerts
        jdbcTemplate.update("DELETE FROM budget_alerts WHERE budget_id = ?", budgetId);
        
        System.out.println("Deleted budget: " + budgetId);
    }

    @Override
    public List<Budget> getAllBudgets() {
        System.out.println("Fetching all budgets");
        
        String sql = """
            SELECT id, name, amount, period, scope, target, alert_threshold, 
                   start_date, end_date, status, created_by, created_at, updated_at
            FROM budgets 
            ORDER BY created_at DESC
            """;
        
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        List<Budget> budgets = new ArrayList<>();
        
        for (Map<String, Object> row : rows) {
            Budget budget = mapRowToBudget(row);
            enrichBudgetWithMetrics(budget);
            budgets.add(budget);
        }
        
        System.out.println("Retrieved " + budgets.size() + " budgets");
        return budgets;
    }

    @Override
    public Budget getBudget(String budgetId) {
        System.out.println("Fetching budget: " + budgetId);
        
        String sql = """
            SELECT id, name, amount, period, scope, target, alert_threshold, 
                   start_date, end_date, status, created_by, created_at, updated_at
            FROM budgets 
            WHERE id = ?
            """;
        
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, budgetId);
        
        if (rows.isEmpty()) {
            throw new RuntimeException("Budget not found: " + budgetId);
        }
        
        Budget budget = mapRowToBudget(rows.get(0));
        enrichBudgetWithMetrics(budget);
        
        return budget;
    }

    @Override
    public List<BudgetAlert> getBudgetAlerts() {
        System.out.println("Fetching budget alerts");
        
        // First, check all active budgets for threshold violations
        generateAlertsForActiveBudgets();
        
        // Then return all active alerts
        String sql = """
            SELECT ba.id, ba.budget_id, ba.budget_name, ba.severity, ba.type, ba.message,
                   ba.trigger_amount, ba.trigger_percentage, ba.trigger_date, ba.status,
                   ba.created_at, ba.acknowledged_at, ba.acknowledged_by
            FROM budget_alerts ba
            WHERE ba.status = 'active'
            ORDER BY ba.created_at DESC
            """;
        
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        List<BudgetAlert> alerts = new ArrayList<>();
        
        for (Map<String, Object> row : rows) {
            alerts.add(mapRowToBudgetAlert(row));
        }
        
        System.out.println("Retrieved " + alerts.size() + " active alerts");
        return alerts;
    }

    @Override
    public Object calculateBudgetMetrics(String budgetId) {
        System.out.println("Calculating budget metrics for: " + budgetId);
        
        Budget budget = getBudget(budgetId);
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("budget", budget);
        metrics.put("currentSpending", budget.getCurrentSpend());
        metrics.put("remainingBudget", budget.getRemainingBudget());
        metrics.put("utilizationPercentage", budget.getUtilizationPercentage());
        metrics.put("daysRemaining", budget.getDaysRemaining());
        metrics.put("isOverThreshold", budget.isOverThreshold());
        metrics.put("isExceeded", budget.isExceeded());
        
        // Add spending trend analysis
        Map<String, Object> trendAnalysis = calculateSpendingTrend(budget);
        metrics.put("spendingTrend", trendAnalysis);
        
        // Add forecast
        Map<String, Object> forecast = calculateBudgetForecast(budget);
        metrics.put("forecast", forecast);
        
        return metrics;
    }

    @Override
    public Object getBudgetAnalytics() {
        System.out.println("Fetching budget analytics");
        
        Map<String, Object> analytics = new HashMap<>();
        
        // Overall budget statistics
        String overallSql = """
            SELECT 
                COUNT(*) as total_budgets,
                COUNT(CASE WHEN status = 'active' THEN 1 END) as active_budgets,
                SUM(amount) as total_budgeted,
                AVG(amount) as avg_budget_amount
            FROM budgets
            """;
        
        List<Map<String, Object>> overallStats = jdbcTemplate.queryForList(overallSql);
        analytics.put("overallStats", overallStats.get(0));
        
        // Budget utilization by scope
        String scopeSql = """
            SELECT scope, COUNT(*) as count, AVG(amount) as avg_amount
            FROM budgets
            WHERE status = 'active'
            GROUP BY scope
            ORDER BY count DESC
            """;
        
        List<Map<String, Object>> scopeBreakdown = jdbcTemplate.queryForList(scopeSql);
        analytics.put("scopeBreakdown", scopeBreakdown);
        
        // Budget utilization trends
        String trendSql = """
            SELECT 
                DATE(created_at) as date,
                COUNT(*) as budgets_created,
                SUM(amount) as total_amount
            FROM budgets
            WHERE created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY)
            GROUP BY DATE(created_at)
            ORDER BY date
            """;
        
        List<Map<String, Object>> trends = jdbcTemplate.queryForList(trendSql);
        analytics.put("creationTrends", trends);
        
        // Active alerts summary
        String alertSql = """
            SELECT 
                severity,
                COUNT(*) as count
            FROM budget_alerts
            WHERE status = 'active'
            GROUP BY severity
            ORDER BY 
                CASE severity 
                    WHEN 'critical' THEN 1 
                    WHEN 'high' THEN 2 
                    WHEN 'medium' THEN 3 
                    WHEN 'low' THEN 4 
                END
            """;
        
        List<Map<String, Object>> alertSummary = jdbcTemplate.queryForList(alertSql);
        analytics.put("alertSummary", alertSummary);
        
        return analytics;
    }

    // ========================================
    // PRIVATE HELPER METHODS
    // ========================================

    private Budget mapRowToBudget(Map<String, Object> row) {
        Budget budget = new Budget();
        budget.setId((String) row.get("id"));
        budget.setName((String) row.get("name"));
        budget.setAmount((BigDecimal) row.get("amount"));
        budget.setPeriod((String) row.get("period"));
        budget.setScope((String) row.get("scope"));
        budget.setTarget((String) row.get("target"));
        budget.setAlertThreshold(((BigDecimal) row.get("alert_threshold")).doubleValue());
        budget.setStartDate((String) row.get("start_date"));
        budget.setEndDate((String) row.get("end_date"));
        budget.setStatus((String) row.get("status"));
        budget.setCreatedBy((String) row.get("created_by"));
        budget.setCreatedAt(row.get("created_at").toString());
        if (row.get("updated_at") != null) {
            budget.setUpdatedAt(row.get("updated_at").toString());
        }
        return budget;
    }

    private BudgetAlert mapRowToBudgetAlert(Map<String, Object> row) {
        BudgetAlert alert = new BudgetAlert();
        alert.setId((String) row.get("id"));
        alert.setBudgetId((String) row.get("budget_id"));
        alert.setBudgetName((String) row.get("budget_name"));
        alert.setSeverity((String) row.get("severity"));
        alert.setType((String) row.get("type"));
        alert.setMessage((String) row.get("message"));
        if (row.get("trigger_amount") != null) {
            alert.setTriggerAmount((BigDecimal) row.get("trigger_amount"));
        }
        if (row.get("trigger_percentage") != null) {
            alert.setTriggerPercentage(((BigDecimal) row.get("trigger_percentage")).doubleValue());
        }
        alert.setTriggerDate((String) row.get("trigger_date"));
        alert.setStatus((String) row.get("status"));
        alert.setCreatedAt(row.get("created_at").toString());
        if (row.get("acknowledged_at") != null) {
            alert.setAcknowledgedAt(row.get("acknowledged_at").toString());
        }
        alert.setAcknowledgedBy((String) row.get("acknowledged_by"));
        return alert;
    }

    private void enrichBudgetWithMetrics(Budget budget) {
        // Calculate current spending
        BigDecimal currentSpend = calculateCurrentSpending(budget);
        budget.setCurrentSpend(currentSpend);
        
        // Calculate days remaining
        int daysRemaining = calculateDaysRemaining(budget.getEndDate());
        budget.setDaysRemaining(daysRemaining);
        
        // Load alert history
        List<String> alertHistory = getAlertHistory(budget.getId());
        budget.setAlertHistory(alertHistory);
    }

    private BigDecimal calculateCurrentSpending(Budget budget) {
        String sql = """
            SELECT COALESCE(SUM(cost), 0) as total_cost
            FROM enhanced_usage_records
            WHERE date BETWEEN ? AND ?
            """;
        
        List<Object> params = new ArrayList<>();
        params.add(budget.getStartDate());
        params.add(budget.getEndDate());
        
        // Add scope-specific filtering
        if ("team".equals(budget.getScope())) {
            sql += " AND team_name = ?";
            params.add(budget.getTarget());
        } else if ("service".equals(budget.getScope())) {
            sql += " AND service_name = ?";
            params.add(budget.getTarget());
        }
        // For "organization" scope, no additional filter needed
        
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, params.toArray());
        BigDecimal totalCost = (BigDecimal) result.get(0).get("total_cost");
        
        return totalCost != null ? totalCost : BigDecimal.ZERO;
    }

    private int calculateDaysRemaining(String endDate) {
        try {
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate now = LocalDate.now();
            return (int) ChronoUnit.DAYS.between(now, end);
        } catch (Exception e) {
            System.err.println("Error calculating days remaining: " + e.getMessage());
            return 0;
        }
    }

    private List<String> getAlertHistory(String budgetId) {
        String sql = """
            SELECT message, created_at
            FROM budget_alerts
            WHERE budget_id = ?
            ORDER BY created_at DESC
            LIMIT 10
            """;
        
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, budgetId);
        List<String> history = new ArrayList<>();
        
        for (Map<String, Object> row : rows) {
            String entry = row.get("created_at") + ": " + row.get("message");
            history.add(entry);
        }
        
        return history;
    }

    private void generateAlertsForActiveBudgets() {
        String sql = "SELECT * FROM budgets WHERE status = 'active'";
        List<Map<String, Object>> budgets = jdbcTemplate.queryForList(sql);
        
        for (Map<String, Object> budgetRow : budgets) {
            Budget budget = mapRowToBudget(budgetRow);
            enrichBudgetWithMetrics(budget);
            
            checkAndCreateAlert(budget);
        }
    }

    private void checkAndCreateAlert(Budget budget) {
        double utilization = budget.getUtilizationPercentage();
        String severity;
        String message;
        
        if (utilization >= 100.0) {
            severity = "critical";
            message = "Budget exceeded! Current spending: " + 
                     String.format("%.2f%%", utilization) + " of budget";
        } else if (utilization >= budget.getAlertThreshold()) {
            severity = "high";
            message = "Budget threshold exceeded! Current spending: " + 
                     String.format("%.2f%%", utilization) + " of budget";
        } else if (utilization >= (budget.getAlertThreshold() - 10)) {
            severity = "medium";
            message = "Approaching budget threshold. Current spending: " + 
                     String.format("%.2f%%", utilization) + " of budget";
        } else {
            return; // No alert needed
        }
        
        // Check if similar alert already exists
        String checkSql = """
            SELECT COUNT(*) as count
            FROM budget_alerts
            WHERE budget_id = ? AND severity = ? AND status = 'active'
            AND DATE(created_at) = CURRENT_DATE
            """;
        
        List<Map<String, Object>> existing = jdbcTemplate.queryForList(checkSql, budget.getId(), severity);
        long existingCount = (Long) existing.get(0).get("count");
        
        if (existingCount == 0) {
            createBudgetAlert(budget, severity, message, utilization);
        }
    }

    private void createBudgetAlert(Budget budget, String severity, String message, double utilization) {
        String alertId = UUID.randomUUID().toString();
        String type = utilization >= 100.0 ? "budget_exceeded" : "threshold_exceeded";
        
        String sql = """
            INSERT INTO budget_alerts 
            (id, budget_id, budget_name, severity, type, message, trigger_amount, 
             trigger_percentage, trigger_date, status, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_DATE, 'active', CURRENT_TIMESTAMP)
            """;
        
        jdbcTemplate.update(sql,
            alertId,
            budget.getId(),
            budget.getName(),
            severity,
            type,
            message,
            budget.getCurrentSpend(),
            utilization,
            LocalDate.now().toString()
        );
        
        System.out.println("Created " + severity + " alert for budget: " + budget.getName());
    }

    private Map<String, Object> calculateSpendingTrend(Budget budget) {
        // Get daily spending data for the budget period
        String sql = """
            SELECT DATE(date) as day, SUM(cost) as daily_cost
            FROM enhanced_usage_records
            WHERE date BETWEEN ? AND ?
            """;
        
        List<Object> params = new ArrayList<>();
        params.add(budget.getStartDate());
        params.add(budget.getEndDate());
        
        if ("team".equals(budget.getScope())) {
            sql += " AND team_name = ?";
            params.add(budget.getTarget());
        } else if ("service".equals(budget.getScope())) {
            sql += " AND service_name = ?";
            params.add(budget.getTarget());
        }
        
        sql += " GROUP BY DATE(date) ORDER BY day";
        
        List<Map<String, Object>> dailySpending = jdbcTemplate.queryForList(sql, params.toArray());
        
        Map<String, Object> trend = new HashMap<>();
        trend.put("dailySpending", dailySpending);
        
        if (dailySpending.size() >= 2) {
            // Calculate trend direction
            BigDecimal firstWeek = calculateAverageForPeriod(dailySpending, 0, Math.min(7, dailySpending.size()));
            BigDecimal lastWeek = calculateAverageForPeriod(dailySpending, 
                Math.max(0, dailySpending.size() - 7), dailySpending.size());
            
            if (lastWeek.compareTo(firstWeek) > 0) {
                trend.put("direction", "increasing");
                trend.put("changePercentage", 
                    lastWeek.subtract(firstWeek).divide(firstWeek, 4, BigDecimal.ROUND_HALF_UP)
                           .multiply(BigDecimal.valueOf(100)).doubleValue());
            } else if (lastWeek.compareTo(firstWeek) < 0) {
                trend.put("direction", "decreasing");
                trend.put("changePercentage", 
                    firstWeek.subtract(lastWeek).divide(firstWeek, 4, BigDecimal.ROUND_HALF_UP)
                           .multiply(BigDecimal.valueOf(100)).doubleValue());
            } else {
                trend.put("direction", "stable");
                trend.put("changePercentage", 0.0);
            }
        } else {
            trend.put("direction", "insufficient_data");
            trend.put("changePercentage", 0.0);
        }
        
        return trend;
    }

    private BigDecimal calculateAverageForPeriod(List<Map<String, Object>> data, int start, int end) {
        if (start >= end || start >= data.size()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        
        for (int i = start; i < Math.min(end, data.size()); i++) {
            BigDecimal cost = (BigDecimal) data.get(i).get("daily_cost");
            if (cost != null) {
                sum = sum.add(cost);
                count++;
            }
        }
        
        return count > 0 ? sum.divide(BigDecimal.valueOf(count), 4, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
    }

    private Map<String, Object> calculateBudgetForecast(Budget budget) {
        Map<String, Object> forecast = new HashMap<>();
        
        if (budget.getDaysRemaining() <= 0) {
            forecast.put("projectedSpend", budget.getCurrentSpend());
            forecast.put("projectedUtilization", budget.getUtilizationPercentage());
            forecast.put("forecastAccuracy", "period_ended");
            return forecast;
        }
        
        // Calculate average daily spend
        LocalDate start = LocalDate.parse(budget.getStartDate());
        LocalDate now = LocalDate.now();
        long daysElapsed = ChronoUnit.DAYS.between(start, now);
        
        if (daysElapsed <= 0) {
            forecast.put("projectedSpend", BigDecimal.ZERO);
            forecast.put("projectedUtilization", 0.0);
            forecast.put("forecastAccuracy", "insufficient_data");
            return forecast;
        }
        
        BigDecimal avgDailySpend = budget.getCurrentSpend().divide(
            BigDecimal.valueOf(daysElapsed), 4, BigDecimal.ROUND_HALF_UP);
        
        // Project to end of period
        LocalDate end = LocalDate.parse(budget.getEndDate());
        long totalDays = ChronoUnit.DAYS.between(start, end);
        
        BigDecimal projectedSpend = avgDailySpend.multiply(BigDecimal.valueOf(totalDays));
        double projectedUtilization = projectedSpend.divide(budget.getAmount(), 4, BigDecimal.ROUND_HALF_UP)
                                                   .multiply(BigDecimal.valueOf(100)).doubleValue();
        
        forecast.put("projectedSpend", projectedSpend);
        forecast.put("projectedUtilization", projectedUtilization);
        forecast.put("avgDailySpend", avgDailySpend);
        forecast.put("forecastAccuracy", daysElapsed >= 7 ? "high" : "low");
        
        return forecast;
    }
}