package com.dashboard.service.impl;

import com.dashboard.model.report.Report;
import com.dashboard.model.report.ReportTemplate;
import com.dashboard.dto.report.GenerateReportRequest;
import com.dashboard.dto.report.ReportScheduleRequest;

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

@DisplayName("Report Service Tests")
class ReportServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ReportServiceImpl reportService;

    private List<Map<String, Object>> mockCostData;
    private GenerateReportRequest validRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockCostData = createMockCostData();
        validRequest = createValidReportRequest();
    }

    @Test
    @DisplayName("Should generate cost summary report successfully")
    void testGenerateReport_CostSummary() {
        // Given
        GenerateReportRequest request = new GenerateReportRequest("Monthly Cost Summary", "cost_summary", "last_30_days");
        request.setGeneratedBy("test-user");
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(mockCostData);

        // When
        Report result = reportService.generateReport(request);

        // Then
        assertNotNull(result);
        assertEquals("Monthly Cost Summary", result.getTitle());
        assertEquals("cost_summary", result.getType());
        assertEquals("completed", result.getStatus());
        assertNotNull(result.getId());
        assertNotNull(result.getData());
        assertNotNull(result.getSummary());
        assertNotNull(result.getRecommendations());
        assertTrue(result.getGenerationTimeMs() >= 0);
        verify(jdbcTemplate, atLeastOnce()).queryForList(anyString(), (Object[]) any());
    }

    @Test
    @DisplayName("Should generate detailed breakdown report successfully")
    void testGenerateReport_DetailedBreakdown() {
        // Given
        GenerateReportRequest request = new GenerateReportRequest("Detailed Cost Breakdown", "detailed_breakdown", "current_month");
        request.setGroupBy("service");
        request.setIncludeCharts(true);
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(mockCostData);

        // When
        Report result = reportService.generateReport(request);

        // Then
        assertNotNull(result);
        assertEquals("detailed_breakdown", result.getType());
        assertEquals("completed", result.getStatus());
        assertNotNull(result.getData());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> reportData = (Map<String, Object>) result.getData();
        assertTrue(reportData.containsKey("serviceBreakdown"));
        assertTrue(reportData.containsKey("totalCost"));
    }

    @Test
    @DisplayName("Should generate executive summary report successfully")
    void testGenerateReport_ExecutiveSummary() {
        // Given
        GenerateReportRequest request = new GenerateReportRequest("Executive Summary", "executive_summary", "current_quarter");
        request.setIncludeForecasts(true);
        request.setIncludeComparisons(true);
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(mockCostData);

        // When
        Report result = reportService.generateReport(request);

        // Then
        assertNotNull(result);
        assertEquals("executive_summary", result.getType());
        assertNotNull(result.getSummary());
        assertNotNull(result.getRecommendations());
        assertTrue(result.getRecommendations().size() > 0);
    }

    @Test
    @DisplayName("Should generate budget performance report successfully")
    void testGenerateReport_BudgetPerformance() {
        // Given
        GenerateReportRequest request = new GenerateReportRequest("Budget Performance", "budget_performance", "current_month");
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(mockCostData);
        when(jdbcTemplate.queryForList(contains("budgets"), (Object[]) any())).thenReturn(createMockBudgetData());

        // When
        Report result = reportService.generateReport(request);

        // Then
        assertNotNull(result);
        assertEquals("budget_performance", result.getType());
        assertNotNull(result.getData());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> reportData = (Map<String, Object>) result.getData();
        assertTrue(reportData.containsKey("budgetUtilization"));
        assertTrue(reportData.containsKey("totalBudget"));
    }

    @Test
    @DisplayName("Should generate cost optimization report successfully")
    void testGenerateReport_CostOptimization() {
        // Given
        GenerateReportRequest request = new GenerateReportRequest("Cost Optimization", "cost_optimization", "last_90_days");
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(mockCostData);
        when(jdbcTemplate.queryForList(contains("optimization_recommendations"), (Object[]) any()))
            .thenReturn(createMockOptimizationData());

        // When
        Report result = reportService.generateReport(request);

        // Then
        assertNotNull(result);
        assertEquals("cost_optimization", result.getType());
        assertNotNull(result.getData());
        assertNotNull(result.getRecommendations());
        assertTrue(result.getRecommendations().size() > 0);
    }

    @Test
    @DisplayName("Should generate chargeback report successfully")
    void testGenerateReport_Chargeback() {
        // Given
        GenerateReportRequest request = new GenerateReportRequest("Team Chargeback", "chargeback", "current_month");
        request.setGroupBy("team");
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(mockCostData);

        // When
        Report result = reportService.generateReport(request);

        // Then
        assertNotNull(result);
        assertEquals("chargeback", result.getType());
        assertNotNull(result.getData());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> reportData = (Map<String, Object>) result.getData();
        assertTrue(reportData.containsKey("chargebackBreakdown"));
        assertTrue(reportData.containsKey("allocationMethod"));
    }

    @Test
    @DisplayName("Should handle invalid report request")
    void testGenerateReport_InvalidRequest() {
        // Given
        GenerateReportRequest invalidRequest = new GenerateReportRequest();
        // Don't set required fields

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> reportService.generateReport(invalidRequest));
        assertTrue(exception.getMessage().contains("Invalid report request"));
    }

    @Test
    @DisplayName("Should handle empty cost data gracefully")
    void testGenerateReport_EmptyData() {
        // Given
        GenerateReportRequest request = new GenerateReportRequest("Empty Data Report", "cost_summary", "last_7_days");
        request.setGeneratedBy("test-user");
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(Collections.emptyList());

        // When
        Report result = reportService.generateReport(request);

        // Then
        assertNotNull(result);
        assertEquals("completed", result.getStatus());
        assertNotNull(result.getData());
        assertNotNull(result.getSummary());
        assertEquals(BigDecimal.ZERO, result.getSummary().getTotalCost());
    }

    @Test
    @DisplayName("Should get report templates by type")
    void testGetReportTemplates() {
        // When
        List<ReportTemplate> templates = reportService.getReportTemplates("cost_summary");

        // Then
        assertNotNull(templates);
        assertTrue(templates.size() > 0);
        assertTrue(templates.stream().anyMatch(t -> "cost_summary".equals(t.getType())));
    }

    @Test
    @DisplayName("Should get all report templates when type is null")
    void testGetReportTemplates_AllTypes() {
        // When
        List<ReportTemplate> templates = reportService.getReportTemplates(null);

        // Then
        assertNotNull(templates);
        assertTrue(templates.size() >= 6); // Should have at least 6 template types
        Set<String> types = new HashSet<>();
        templates.forEach(t -> types.add(t.getType()));
        assertTrue(types.contains("cost_summary"));
        assertTrue(types.contains("executive_summary"));
        assertTrue(types.contains("cost_optimization"));
    }

    @Test
    @DisplayName("Should retrieve existing report by ID")
    void testGetReport_ExistingReport() {
        // Given
        String reportId = "test-report-123";
        List<Map<String, Object>> mockReportData = Collections.singletonList(
            Map.of(
                "id", reportId,
                "title", "Test Report",
                "type", "cost_summary",
                "status", "completed",
                "created_at", "2025-01-01 10:00:00",
                "generated_by", "test-user"
            )
        );
        when(jdbcTemplate.queryForList(contains("SELECT * FROM reports"), eq(reportId)))
            .thenReturn(mockReportData);

        // When
        Report result = reportService.getReport(reportId);

        // Then
        assertNotNull(result);
        assertEquals(reportId, result.getId());
        assertEquals("Test Report", result.getTitle());
        assertEquals("cost_summary", result.getType());
        assertEquals("completed", result.getStatus());
    }

    @Test
    @DisplayName("Should return null for non-existent report")
    void testGetReport_NonExistent() {
        // Given
        String reportId = "non-existent-report";
        when(jdbcTemplate.queryForList(anyString(), eq(reportId)))
            .thenReturn(Collections.emptyList());

        // When
        Report result = reportService.getReport(reportId);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should get user reports with filters")
    void testGetReports_WithFilters() {
        // Given
        String userId = "test-user";
        String reportType = "cost_summary";
        Integer limit = 10;
        
        List<Map<String, Object>> mockReportsData = Arrays.asList(
            Map.of("id", "report-1", "title", "Report 1", "type", "cost_summary", "status", "completed"),
            Map.of("id", "report-2", "title", "Report 2", "type", "cost_summary", "status", "completed")
        );
        when(jdbcTemplate.queryForList(anyString(), eq(userId), eq(reportType), eq(limit)))
            .thenReturn(mockReportsData);

        // When
        List<Report> result = reportService.getReports(userId, reportType, limit);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("report-1", result.get(0).getId());
        assertEquals("Report 1", result.get(0).getTitle());
    }

    @Test
    @DisplayName("Should get all user reports when no filters applied")
    void testGetReports_NoFilters() {
        // Given
        String userId = "test-user";
        List<Map<String, Object>> mockReportsData = Collections.singletonList(
            Map.of("id", "report-1", "title", "Report 1", "type", "cost_summary", "status", "completed")
        );
        when(jdbcTemplate.queryForList(contains("SELECT * FROM reports"), eq(userId)))
            .thenReturn(mockReportsData);

        // When
        List<Report> result = reportService.getReports(userId, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should schedule report successfully")
    void testScheduleReport() {
        // Given
        ReportScheduleRequest request = new ReportScheduleRequest();
        request.setName("Weekly Cost Report");
        request.setFrequency("weekly");
        request.setStartDate("2025-01-01");
        request.setReportConfig(validRequest);
        request.setRecipients(Arrays.asList("user@example.com"));
        
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(1);

        // When
        Object result = reportService.scheduleReport(request);

        // Then
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        Map<String, Object> scheduleResult = (Map<String, Object>) result;
        assertTrue((Boolean) scheduleResult.get("success"));
        assertNotNull(scheduleResult.get("scheduleId"));
        assertTrue(scheduleResult.get("message").toString().contains("scheduled successfully"));
    }

    @Test
    @DisplayName("Should export report in requested format")
    void testExportReport() {
        // Given
        String reportId = "test-report-123";
        String format = "pdf";
        
        List<Map<String, Object>> mockReportData = Collections.singletonList(
            Map.of(
                "id", reportId,
                "title", "Test Report",
                "type", "cost_summary",
                "status", "completed"
            )
        );
        when(jdbcTemplate.queryForList(anyString(), eq(reportId)))
            .thenReturn(mockReportData);

        // When
        Object result = reportService.exportReport(reportId, format);

        // Then
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        Map<String, Object> exportResult = (Map<String, Object>) result;
        assertTrue((Boolean) exportResult.get("success"));
        assertEquals(format, exportResult.get("format"));
        assertNotNull(exportResult.get("downloadUrl"));
    }

    @Test
    @DisplayName("Should get report analytics")
    void testGetReportAnalytics() {
        // Given
        when(jdbcTemplate.queryForList(contains("COUNT(*) as total_reports")))
            .thenReturn(Collections.singletonList(Map.of("total_reports", 25L)));
        when(jdbcTemplate.queryForList(contains("GROUP BY type")))
            .thenReturn(Arrays.asList(
                Map.of("type", "cost_summary", "count", 10L),
                Map.of("type", "executive_summary", "count", 8L)
            ));
        when(jdbcTemplate.queryForList(contains("DATE(created_at) as date")))
            .thenReturn(Arrays.asList(
                Map.of("date", "2025-01-01", "count", 5L),
                Map.of("date", "2025-01-02", "count", 3L)
            ));

        // When
        Object result = reportService.getReportAnalytics();

        // Then
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        Map<String, Object> analytics = (Map<String, Object>) result;
        assertEquals(25L, analytics.get("totalReports"));
        assertNotNull(analytics.get("reportsByType"));
        assertNotNull(analytics.get("reportsOverTime"));
    }

    // ========================================
    // HELPER METHODS FOR TEST DATA
    // ========================================

    private List<Map<String, Object>> createMockCostData() {
        return Arrays.asList(
            Map.of(
                "date", "2025-01-01",
                "team_name", "platform",
                "service_name", "AWS Lambda",
                "cost", new BigDecimal("100.50"),
                "usage_quantity", new BigDecimal("50"),
                "region", "us-east-1",
                "provider", "aws"
            ),
            Map.of(
                "date", "2025-01-02",
                "team_name", "frontend",
                "service_name", "Amazon S3",
                "cost", new BigDecimal("75.25"),
                "usage_quantity", new BigDecimal("200"),
                "region", "us-west-2",
                "provider", "aws"
            ),
            Map.of(
                "date", "2025-01-03",
                "team_name", "platform",
                "service_name", "Google Compute Engine",
                "cost", new BigDecimal("200.00"),
                "usage_quantity", new BigDecimal("10"),
                "region", "us-central1",
                "provider", "gcp"
            )
        );
    }

    private List<Map<String, Object>> createMockBudgetData() {
        return Arrays.asList(
            Map.of(
                "id", "budget-1",
                "name", "Platform Team Budget",
                "amount", new BigDecimal("5000.00"),
                "scope", "team",
                "scope_value", "platform",
                "period", "monthly"
            ),
            Map.of(
                "id", "budget-2",
                "name", "AWS Lambda Budget",
                "amount", new BigDecimal("1000.00"),
                "scope", "service",
                "scope_value", "AWS Lambda",
                "period", "monthly"
            )
        );
    }

    private List<Map<String, Object>> createMockOptimizationData() {
        return Arrays.asList(
            Map.of(
                "id", "opt-1",
                "type", "rightsizing",
                "resource_id", "i-1234567890abcdef0",
                "current_type", "m5.large",
                "recommended_type", "m5.medium",
                "potential_savings", new BigDecimal("50.00"),
                "confidence_level", 0.85
            ),
            Map.of(
                "id", "opt-2",
                "type", "unused_resource",
                "resource_id", "vol-abcdef1234567890",
                "description", "Unused EBS volume",
                "potential_savings", new BigDecimal("25.00"),
                "confidence_level", 0.95
            )
        );
    }

    private GenerateReportRequest createValidReportRequest() {
        GenerateReportRequest request = new GenerateReportRequest("Test Report", "cost_summary", "last_30_days");
        request.setGeneratedBy("test-user");
        request.setFormat("pdf");
        request.setIncludeCharts(true);
        return request;
    }
}