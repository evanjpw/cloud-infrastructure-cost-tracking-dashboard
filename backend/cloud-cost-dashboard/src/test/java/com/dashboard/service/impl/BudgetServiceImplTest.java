package com.dashboard.service.impl;

import com.dashboard.model.budget.Budget;
import com.dashboard.model.budget.BudgetAlert;
import com.dashboard.dto.budget.CreateBudgetRequest;
import com.dashboard.dto.budget.UpdateBudgetRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Budget Service Tests")
class BudgetServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private CreateBudgetRequest validCreateRequest;
    private UpdateBudgetRequest validUpdateRequest;
    private Map<String, Object> mockBudgetRow;
    private List<Map<String, Object>> mockSpendingData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        validCreateRequest = new CreateBudgetRequest();
        validCreateRequest.setName("Test Budget");
        validCreateRequest.setAmount(new BigDecimal("10000.00"));
        validCreateRequest.setPeriod("monthly");
        validCreateRequest.setScope("team");
        validCreateRequest.setTarget("platform");
        validCreateRequest.setAlertThreshold(80.0);
        validCreateRequest.setStartDate("2025-01-01");
        validCreateRequest.setEndDate("2025-01-31");
        validCreateRequest.setCreatedBy("test-user");

        validUpdateRequest = new UpdateBudgetRequest();
        validUpdateRequest.setName("Updated Budget");
        validUpdateRequest.setAmount(new BigDecimal("12000.00"));
        validUpdateRequest.setAlertThreshold(85.0);

        mockBudgetRow = createMockBudgetRow();
        mockSpendingData = createMockSpendingData();
    }

    @Test
    @DisplayName("Should create budget successfully")
    void testCreateBudget() {
        // Given
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(1);
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, anyString()))
            .thenReturn(Arrays.asList(mockBudgetRow));
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, any(), any()))
            .thenReturn(mockSpendingData);
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, anyString()))
            .thenReturn(Collections.emptyList()); // No alert history

        // When
        Budget result = budgetService.createBudget(validCreateRequest);

        // Then
        assertNotNull(result);
        assertEquals("Test Budget", result.getName());
        assertEquals(new BigDecimal("10000.00"), result.getAmount());
        assertEquals("monthly", result.getPeriod());
        assertEquals("team", result.getScope());
        assertEquals("platform", result.getTarget());
        assertEquals(80.0, result.getAlertThreshold());
        assertEquals("active", result.getStatus());
        assertNotNull(result.getCurrentSpend());
        assertTrue(result.getDaysRemaining() >= 0);
        
        verify(jdbcTemplate).update(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw exception for invalid create request")
    void testCreateBudget_InvalidRequest() {
        // Given
        CreateBudgetRequest invalidRequest = new CreateBudgetRequest();
        invalidRequest.setName(""); // Invalid empty name
        invalidRequest.setAmount(new BigDecimal("-100")); // Invalid negative amount

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> budgetService.createBudget(invalidRequest));
        assertTrue(exception.getMessage().contains("Invalid budget request"));
    }

    @Test
    @DisplayName("Should update budget successfully")
    void testUpdateBudget() {
        // Given
        String budgetId = "budget-123";
        
        when(jdbcTemplate.update(anyString(), any(), any(), any(), anyString()))
            .thenReturn(1);
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, anyString()))
            .thenReturn(Arrays.asList(mockBudgetRow));
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, any(), any()))
            .thenReturn(mockSpendingData);
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, anyString()))
            .thenReturn(Collections.emptyList());

        // When
        Budget result = budgetService.updateBudget(budgetId, validUpdateRequest);

        // Then
        assertNotNull(result);
        verify(jdbcTemplate).update(contains("UPDATE budgets SET"), any(), any(), any(), eq(budgetId));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent budget")
    void testUpdateBudget_NotFound() {
        // Given
        String budgetId = "nonexistent";
        when(jdbcTemplate.update(anyString(), any(), any(), any(), anyString()))
            .thenReturn(0);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> budgetService.updateBudget(budgetId, validUpdateRequest));
        assertTrue(exception.getMessage().contains("Budget not found"));
    }

    @Test
    @DisplayName("Should delete budget successfully")
    void testDeleteBudget() {
        // Given
        String budgetId = "budget-123";
        when(jdbcTemplate.update("DELETE FROM budgets WHERE id = ?", budgetId))
            .thenReturn(1);
        when(jdbcTemplate.update("DELETE FROM budget_alerts WHERE budget_id = ?", budgetId))
            .thenReturn(2);

        // When
        budgetService.deleteBudget(budgetId);

        // Then
        verify(jdbcTemplate).update("DELETE FROM budgets WHERE id = ?", budgetId);
        verify(jdbcTemplate).update("DELETE FROM budget_alerts WHERE budget_id = ?", budgetId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent budget")
    void testDeleteBudget_NotFound() {
        // Given
        String budgetId = "nonexistent";
        when(jdbcTemplate.update("DELETE FROM budgets WHERE id = ?", budgetId))
            .thenReturn(0);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> budgetService.deleteBudget(budgetId));
        assertTrue(exception.getMessage().contains("Budget not found"));
    }

    @Test
    @DisplayName("Should get all budgets successfully")
    void testGetAllBudgets() {
        // Given
        List<Map<String, Object>> budgetRows = Arrays.asList(mockBudgetRow, createSecondMockBudgetRow());
        when(jdbcTemplate.queryForList(contains("SELECT id, name"), (Class<Map<String, Object>>) null))
            .thenReturn(budgetRows);
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, any(), any()))
            .thenReturn(mockSpendingData);
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, anyString()))
            .thenReturn(Collections.emptyList());

        // When
        List<Budget> result = budgetService.getAllBudgets();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        Budget firstBudget = result.get(0);
        assertNotNull(firstBudget.getId());
        assertNotNull(firstBudget.getName());
        assertNotNull(firstBudget.getAmount());
        assertNotNull(firstBudget.getCurrentSpend());
        assertTrue(firstBudget.getUtilizationPercentage() >= 0);
    }

    @Test
    @DisplayName("Should get budget by ID successfully")
    void testGetBudget() {
        // Given
        String budgetId = "budget-123";
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, eq(budgetId)))
            .thenReturn(Arrays.asList(mockBudgetRow));
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, any(), any()))
            .thenReturn(mockSpendingData);
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, eq(budgetId)))
            .thenReturn(Collections.emptyList());

        // When
        Budget result = budgetService.getBudget(budgetId);

        // Then
        assertNotNull(result);
        assertEquals("budget-123", result.getId());
        assertEquals("Test Budget", result.getName());
        assertEquals(new BigDecimal("10000.00"), result.getAmount());
        assertNotNull(result.getCurrentSpend());
        assertTrue(result.getUtilizationPercentage() >= 0);
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent budget")
    void testGetBudget_NotFound() {
        // Given
        String budgetId = "nonexistent";
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, eq(budgetId)))
            .thenReturn(Collections.emptyList());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> budgetService.getBudget(budgetId));
        assertTrue(exception.getMessage().contains("Budget not found"));
    }

    @Test
    @DisplayName("Should get budget alerts successfully")
    void testGetBudgetAlerts() {
        // Given
        List<Map<String, Object>> activeBudgets = Arrays.asList(mockBudgetRow);
        List<Map<String, Object>> alertRows = createMockAlertRows();
        
        when(jdbcTemplate.queryForList("SELECT * FROM budgets WHERE status = 'active'", (Class<Map<String, Object>>) null))
            .thenReturn(activeBudgets);
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, any(), any()))
            .thenReturn(createHighSpendingData());
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, anyString()))
            .thenReturn(Collections.emptyList());
        when(jdbcTemplate.queryForList(contains("FROM budget_alerts ba"), (Class<Map<String, Object>>) null))
            .thenReturn(alertRows);

        // When
        List<BudgetAlert> result = budgetService.getBudgetAlerts();

        // Then
        assertNotNull(result);
        assertTrue(result.size() >= 0);
        
        if (!result.isEmpty()) {
            BudgetAlert alert = result.get(0);
            assertNotNull(alert.getId());
            assertNotNull(alert.getBudgetId());
            assertNotNull(alert.getSeverity());
            assertNotNull(alert.getType());
            assertNotNull(alert.getMessage());
            assertEquals("active", alert.getStatus());
        }
    }

    @Test
    @DisplayName("Should calculate budget metrics successfully")
    void testCalculateBudgetMetrics() {
        // Given
        String budgetId = "budget-123";
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, eq(budgetId)))
            .thenReturn(Arrays.asList(mockBudgetRow));
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, any(), any()))
            .thenReturn(mockSpendingData);
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, eq(budgetId)))
            .thenReturn(Collections.emptyList());
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, any(), any(), any()))
            .thenReturn(createDailySpendingData());

        // When
        Object result = budgetService.calculateBudgetMetrics(budgetId);

        // Then
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        Map<String, Object> metrics = (Map<String, Object>) result;
        
        assertTrue(metrics.containsKey("budget"));
        assertTrue(metrics.containsKey("currentSpending"));
        assertTrue(metrics.containsKey("remainingBudget"));
        assertTrue(metrics.containsKey("utilizationPercentage"));
        assertTrue(metrics.containsKey("daysRemaining"));
        assertTrue(metrics.containsKey("isOverThreshold"));
        assertTrue(metrics.containsKey("isExceeded"));
        assertTrue(metrics.containsKey("spendingTrend"));
        assertTrue(metrics.containsKey("forecast"));

        Budget budget = (Budget) metrics.get("budget");
        assertNotNull(budget);
        assertEquals(budgetId, budget.getId());
    }

    @Test
    @DisplayName("Should get budget analytics successfully")
    void testGetBudgetAnalytics() {
        // Given
        List<Map<String, Object>> overallStats = Arrays.asList(
            Map.of("total_budgets", 5L, "active_budgets", 4L, 
                   "total_budgeted", new BigDecimal("50000.00"), 
                   "avg_budget_amount", new BigDecimal("10000.00"))
        );
        
        List<Map<String, Object>> scopeBreakdown = Arrays.asList(
            Map.of("scope", "team", "count", 3L, "avg_amount", new BigDecimal("8000.00")),
            Map.of("scope", "service", "count", 2L, "avg_amount", new BigDecimal("15000.00"))
        );
        
        List<Map<String, Object>> trends = Arrays.asList(
            Map.of("date", "2025-01-01", "budgets_created", 2L, "total_amount", new BigDecimal("20000.00")),
            Map.of("date", "2025-01-02", "budgets_created", 1L, "total_amount", new BigDecimal("10000.00"))
        );
        
        List<Map<String, Object>> alertSummary = Arrays.asList(
            Map.of("severity", "high", "count", 2L),
            Map.of("severity", "medium", "count", 1L)
        );
        
        when(jdbcTemplate.queryForList(contains("COUNT(*) as total_budgets"), (Class<Map<String, Object>>) null))
            .thenReturn(overallStats);
        when(jdbcTemplate.queryForList(contains("GROUP BY scope"), (Class<Map<String, Object>>) null))
            .thenReturn(scopeBreakdown);
        when(jdbcTemplate.queryForList(contains("DATE(created_at) as date"), (Class<Map<String, Object>>) null))
            .thenReturn(trends);
        when(jdbcTemplate.queryForList(contains("GROUP BY severity"), (Class<Map<String, Object>>) null))
            .thenReturn(alertSummary);

        // When
        Object result = budgetService.getBudgetAnalytics();

        // Then
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        Map<String, Object> analytics = (Map<String, Object>) result;
        
        assertTrue(analytics.containsKey("overallStats"));
        assertTrue(analytics.containsKey("scopeBreakdown"));
        assertTrue(analytics.containsKey("creationTrends"));
        assertTrue(analytics.containsKey("alertSummary"));

        @SuppressWarnings("unchecked")
        Map<String, Object> overall = (Map<String, Object>) analytics.get("overallStats");
        assertEquals(5L, overall.get("total_budgets"));
        assertEquals(4L, overall.get("active_budgets"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> scopeData = (List<Map<String, Object>>) analytics.get("scopeBreakdown");
        assertEquals(2, scopeData.size());
        assertEquals("team", scopeData.get(0).get("scope"));
    }

    @Test
    @DisplayName("Should handle team-scoped budget spending calculation")
    void testCalculateCurrentSpending_TeamScope() {
        // Given
        mockBudgetRow.put("scope", "team");
        mockBudgetRow.put("target", "platform");
        
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, eq("budget-123")))
            .thenReturn(Arrays.asList(mockBudgetRow));
        when(jdbcTemplate.queryForList(contains("team_name = ?"), (Class<Map<String, Object>>) null, any(), any(), eq("platform")))
            .thenReturn(Arrays.asList(Map.of("total_cost", new BigDecimal("5000.00"))));
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, eq("budget-123")))
            .thenReturn(Collections.emptyList());

        // When
        Budget result = budgetService.getBudget("budget-123");

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("5000.00"), result.getCurrentSpend());
        assertEquals(50.0, result.getUtilizationPercentage(), 0.01);
    }

    @Test
    @DisplayName("Should handle service-scoped budget spending calculation")
    void testCalculateCurrentSpending_ServiceScope() {
        // Given
        mockBudgetRow.put("scope", "service");
        mockBudgetRow.put("target", "EC2");
        
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, eq("budget-123")))
            .thenReturn(Arrays.asList(mockBudgetRow));
        when(jdbcTemplate.queryForList(contains("service_name = ?"), (Class<Map<String, Object>>) null, any(), any(), eq("EC2")))
            .thenReturn(Arrays.asList(Map.of("total_cost", new BigDecimal("7500.00"))));
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, eq("budget-123")))
            .thenReturn(Collections.emptyList());

        // When
        Budget result = budgetService.getBudget("budget-123");

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("7500.00"), result.getCurrentSpend());
        assertEquals(75.0, result.getUtilizationPercentage(), 0.01);
    }

    @Test
    @DisplayName("Should handle budget with zero spending")
    void testBudgetWithZeroSpending() {
        // Given
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, eq("budget-123")))
            .thenReturn(Arrays.asList(mockBudgetRow));
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, any(), any()))
            .thenReturn(Arrays.asList(Map.of("total_cost", BigDecimal.ZERO)));
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, eq("budget-123")))
            .thenReturn(Collections.emptyList());

        // When
        Budget result = budgetService.getBudget("budget-123");

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getCurrentSpend());
        assertEquals(0.0, result.getUtilizationPercentage());
        assertFalse(result.isOverThreshold());
        assertFalse(result.isExceeded());
    }

    @Test
    @DisplayName("Should handle budget exceeding 100%")
    void testBudgetExceeded() {
        // Given
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, eq("budget-123")))
            .thenReturn(Arrays.asList(mockBudgetRow));
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, any(), any()))
            .thenReturn(Arrays.asList(Map.of("total_cost", new BigDecimal("12000.00")))); // Over budget
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, eq("budget-123")))
            .thenReturn(Collections.emptyList());

        // When
        Budget result = budgetService.getBudget("budget-123");

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("12000.00"), result.getCurrentSpend());
        assertEquals(120.0, result.getUtilizationPercentage(), 0.01);
        assertTrue(result.isOverThreshold());
        assertTrue(result.isExceeded());
        assertTrue(result.getRemainingBudget().compareTo(BigDecimal.ZERO) < 0);
    }

    // ========================================
    // HELPER METHODS FOR TEST DATA
    // ========================================

    private Map<String, Object> createMockBudgetRow() {
        Map<String, Object> row = new HashMap<>();
        row.put("id", "budget-123");
        row.put("name", "Test Budget");
        row.put("amount", new BigDecimal("10000.00"));
        row.put("period", "monthly");
        row.put("scope", "team");
        row.put("target", "platform");
        row.put("alert_threshold", new BigDecimal("80.0"));
        row.put("start_date", "2025-01-01");
        row.put("end_date", "2025-01-31");
        row.put("status", "active");
        row.put("created_by", "test-user");
        row.put("created_at", "2025-01-01T00:00:00Z");
        row.put("updated_at", "2025-01-01T00:00:00Z");
        return row;
    }

    private Map<String, Object> createSecondMockBudgetRow() {
        Map<String, Object> row = new HashMap<>();
        row.put("id", "budget-456");
        row.put("name", "EC2 Budget");
        row.put("amount", new BigDecimal("15000.00"));
        row.put("period", "quarterly");
        row.put("scope", "service");
        row.put("target", "EC2");
        row.put("alert_threshold", new BigDecimal("85.0"));
        row.put("start_date", "2025-01-01");
        row.put("end_date", "2025-03-31");
        row.put("status", "active");
        row.put("created_by", "test-user");
        row.put("created_at", "2025-01-01T00:00:00Z");
        row.put("updated_at", "2025-01-01T00:00:00Z");
        return row;
    }

    private List<Map<String, Object>> createMockSpendingData() {
        return Arrays.asList(
            Map.of("total_cost", new BigDecimal("6000.00"))
        );
    }

    private List<Map<String, Object>> createHighSpendingData() {
        return Arrays.asList(
            Map.of("total_cost", new BigDecimal("9000.00")) // 90% of budget
        );
    }

    private List<Map<String, Object>> createMockAlertRows() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        Map<String, Object> alert = new HashMap<>();
        alert.put("id", "alert-123");
        alert.put("budget_id", "budget-123");
        alert.put("budget_name", "Test Budget");
        alert.put("severity", "high");
        alert.put("type", "threshold_exceeded");
        alert.put("message", "Budget threshold exceeded!");
        alert.put("trigger_amount", new BigDecimal("9000.00"));
        alert.put("trigger_percentage", new BigDecimal("90.0"));
        alert.put("trigger_date", "2025-01-15");
        alert.put("status", "active");
        alert.put("created_at", "2025-01-15T10:00:00Z");
        alert.put("acknowledged_at", null);
        alert.put("acknowledged_by", null);
        
        alerts.add(alert);
        return alerts;
    }

    private List<Map<String, Object>> createDailySpendingData() {
        List<Map<String, Object>> dailyData = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> day = new HashMap<>();
            day.put("day", "2025-01-" + String.format("%02d", i));
            day.put("daily_cost", new BigDecimal(String.valueOf(200 + (i * 10)))); // Increasing trend
            dailyData.add(day);
        }
        
        return dailyData;
    }
}