package com.dashboard.service.impl;

import com.dashboard.dto.CostReportRequest;
import com.dashboard.dto.CostReportResponse;
import com.dashboard.model.CostBreakdown;
import com.dashboard.service.interfaces.CostCalculationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Report Generation Service Tests")
class ReportGenerationServiceImplTest {

    @Mock
    private CostCalculationService costCalculationService;

    @InjectMocks
    private ReportGenerationServiceImpl reportGenerationService;

    private List<CostBreakdown> mockCostBreakdowns;
    private CostReportRequest validRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockCostBreakdowns = createMockCostBreakdowns();
        validRequest = createValidCostReportRequest();
    }

    @Test
    @DisplayName("Should generate report successfully with valid request")
    void testGenerateReport_Success() {
        // Given
        when(costCalculationService.calculateCosts(any(CostReportRequest.class)))
                .thenReturn(mockCostBreakdowns);

        // When
        CostReportResponse result = reportGenerationService.generateReport(validRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getBreakdowns());
        assertEquals(3, result.getBreakdowns().size());
        
        // Verify the breakdowns are properly set
        List<CostBreakdown> breakdowns = result.getBreakdowns();
        assertEquals("platform", breakdowns.get(0).getTeamName());
        assertEquals("Amazon EC2", breakdowns.get(0).getService());
        assertEquals(150.50, breakdowns.get(0).getTotalCost(), 0.01);
        
        // Verify cost calculation service was called
        verify(costCalculationService).calculateCosts(eq(validRequest));
    }

    @Test
    @DisplayName("Should generate report with team-specific request")
    void testGenerateReport_TeamSpecific() {
        // Given
        CostReportRequest teamRequest = new CostReportRequest();
        teamRequest.setTeamName("frontend");
        teamRequest.setStartDate(LocalDate.of(2025, 1, 1));
        teamRequest.setEndDate(LocalDate.of(2025, 1, 31));

        List<CostBreakdown> teamBreakdowns = Arrays.asList(
            new CostBreakdown("frontend", "Amazon S3", 89.25),
            new CostBreakdown("frontend", "CloudFront", 45.75)
        );

        when(costCalculationService.calculateCosts(eq(teamRequest)))
                .thenReturn(teamBreakdowns);

        // When
        CostReportResponse result = reportGenerationService.generateReport(teamRequest);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getBreakdowns().size());
        
        // Verify team-specific data
        List<CostBreakdown> breakdowns = result.getBreakdowns();
        assertTrue(breakdowns.stream().allMatch(bd -> "frontend".equals(bd.getTeamName())));
        
        Set<String> services = breakdowns.stream()
                .map(CostBreakdown::getService)
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(services.contains("Amazon S3"));
        assertTrue(services.contains("CloudFront"));
        
        verify(costCalculationService).calculateCosts(eq(teamRequest));
    }

    @Test
    @DisplayName("Should generate report for date range request")
    void testGenerateReport_DateRange() {
        // Given
        CostReportRequest dateRangeRequest = new CostReportRequest();
        dateRangeRequest.setTeamName("backend");
        dateRangeRequest.setStartDate(LocalDate.of(2025, 2, 1));
        dateRangeRequest.setEndDate(LocalDate.of(2025, 2, 28));

        List<CostBreakdown> dateRangeBreakdowns = Arrays.asList(
            new CostBreakdown("backend", "Amazon RDS", 250.00),
            new CostBreakdown("backend", "Amazon ElastiCache", 125.50)
        );

        when(costCalculationService.calculateCosts(eq(dateRangeRequest)))
                .thenReturn(dateRangeBreakdowns);

        // When
        CostReportResponse result = reportGenerationService.generateReport(dateRangeRequest);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getBreakdowns().size());
        
        // Verify the response contains the correct data
        List<CostBreakdown> breakdowns = result.getBreakdowns();
        CostBreakdown rdsBreakdown = breakdowns.stream()
                .filter(bd -> "Amazon RDS".equals(bd.getService()))
                .findFirst().orElse(null);
        assertNotNull(rdsBreakdown);
        assertEquals(250.00, rdsBreakdown.getTotalCost(), 0.01);
        
        verify(costCalculationService).calculateCosts(eq(dateRangeRequest));
    }

    @Test
    @DisplayName("Should handle empty cost breakdowns gracefully")
    void testGenerateReport_EmptyBreakdowns() {
        // Given
        when(costCalculationService.calculateCosts(any(CostReportRequest.class)))
                .thenReturn(Collections.emptyList());

        // When
        CostReportResponse result = reportGenerationService.generateReport(validRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getBreakdowns());
        assertTrue(result.getBreakdowns().isEmpty());
        
        verify(costCalculationService).calculateCosts(eq(validRequest));
    }

    @Test
    @DisplayName("Should handle null request gracefully")
    void testGenerateReport_NullRequest() {
        // Given
        when(costCalculationService.calculateCosts(isNull()))
                .thenReturn(Collections.emptyList());

        // When
        CostReportResponse result = reportGenerationService.generateReport(null);

        // Then
        assertNotNull(result);
        assertNotNull(result.getBreakdowns());
        assertTrue(result.getBreakdowns().isEmpty());
        
        verify(costCalculationService).calculateCosts(isNull());
    }

    @Test
    @DisplayName("Should propagate cost calculation service exceptions")
    void testGenerateReport_ServiceException() {
        // Given
        when(costCalculationService.calculateCosts(any(CostReportRequest.class)))
                .thenThrow(new RuntimeException("Cost calculation failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> reportGenerationService.generateReport(validRequest));
        
        assertEquals("Cost calculation failed", exception.getMessage());
        verify(costCalculationService).calculateCosts(eq(validRequest));
    }

    @Test
    @DisplayName("Should handle large number of cost breakdowns")
    void testGenerateReport_LargeBreakdowns() {
        // Given
        List<CostBreakdown> largeBreakdownList = createLargeBreakdownList();
        when(costCalculationService.calculateCosts(any(CostReportRequest.class)))
                .thenReturn(largeBreakdownList);

        // When
        CostReportResponse result = reportGenerationService.generateReport(validRequest);

        // Then
        assertNotNull(result);
        assertEquals(50, result.getBreakdowns().size());
        
        // Verify all breakdowns are present
        Set<String> services = result.getBreakdowns().stream()
                .map(CostBreakdown::getService)
                .collect(java.util.stream.Collectors.toSet());
        assertEquals(50, services.size()); // All should be unique
        
        verify(costCalculationService).calculateCosts(eq(validRequest));
    }

    @Test
    @DisplayName("Should maintain breakdown data integrity")
    void testGenerateReport_DataIntegrity() {
        // Given
        List<CostBreakdown> originalBreakdowns = Arrays.asList(
            new CostBreakdown("team1", "Service A", 100.0),
            new CostBreakdown("team2", "Service B", 200.0),
            new CostBreakdown("team1", "Service C", 150.0)
        );

        when(costCalculationService.calculateCosts(any(CostReportRequest.class)))
                .thenReturn(originalBreakdowns);

        // When
        CostReportResponse result = reportGenerationService.generateReport(validRequest);

        // Then
        assertNotNull(result);
        List<CostBreakdown> returnedBreakdowns = result.getBreakdowns();
        assertEquals(3, returnedBreakdowns.size());
        
        // Verify data integrity - should be the same objects/values
        for (int i = 0; i < originalBreakdowns.size(); i++) {
            CostBreakdown original = originalBreakdowns.get(i);
            CostBreakdown returned = returnedBreakdowns.get(i);
            
            assertEquals(original.getTeamName(), returned.getTeamName());
            assertEquals(original.getService(), returned.getService());
            assertEquals(original.getTotalCost(), returned.getTotalCost(), 0.01);
        }
        
        verify(costCalculationService).calculateCosts(eq(validRequest));
    }

    @Test
    @DisplayName("Should handle request with null team name")
    void testGenerateReport_NullTeamName() {
        // Given
        CostReportRequest requestWithNullTeam = new CostReportRequest();
        requestWithNullTeam.setTeamName(null);
        requestWithNullTeam.setStartDate(LocalDate.of(2025, 1, 1));
        requestWithNullTeam.setEndDate(LocalDate.of(2025, 1, 31));

        when(costCalculationService.calculateCosts(eq(requestWithNullTeam)))
                .thenReturn(mockCostBreakdowns);

        // When
        CostReportResponse result = reportGenerationService.generateReport(requestWithNullTeam);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getBreakdowns().size());
        
        verify(costCalculationService).calculateCosts(eq(requestWithNullTeam));
    }

    @Test
    @DisplayName("Should verify service integration flow")
    void testGenerateReport_ServiceIntegration() {
        // Given
        CostReportRequest integrationRequest = new CostReportRequest();
        integrationRequest.setTeamName("integration-test");
        integrationRequest.setStartDate(LocalDate.of(2025, 3, 1));
        integrationRequest.setEndDate(LocalDate.of(2025, 3, 31));

        // When
        when(costCalculationService.calculateCosts(eq(integrationRequest)))
                .thenReturn(mockCostBreakdowns);

        CostReportResponse result = reportGenerationService.generateReport(integrationRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getBreakdowns());
        
        // Verify the integration flow: request -> cost calculation -> response
        verify(costCalculationService, times(1)).calculateCosts(eq(integrationRequest));
        verifyNoMoreInteractions(costCalculationService);
        
        // Verify response structure
        assertTrue(result.getBreakdowns().size() > 0);
        assertNotNull(result.getBreakdowns().get(0).getTeamName());
        assertNotNull(result.getBreakdowns().get(0).getService());
        assertTrue(result.getBreakdowns().get(0).getTotalCost() >= 0);
    }

    // ========================================
    // HELPER METHODS FOR TEST DATA
    // ========================================

    private List<CostBreakdown> createMockCostBreakdowns() {
        return Arrays.asList(
            new CostBreakdown("platform", "Amazon EC2", 150.50),
            new CostBreakdown("platform", "Amazon S3", 89.25),
            new CostBreakdown("platform", "Amazon RDS", 234.75)
        );
    }

    private List<CostBreakdown> createLargeBreakdownList() {
        List<CostBreakdown> breakdowns = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            breakdowns.add(new CostBreakdown(
                "team" + (i % 5 + 1), 
                "Service-" + i, 
                i * 10.5
            ));
        }
        return breakdowns;
    }

    private CostReportRequest createValidCostReportRequest() {
        CostReportRequest request = new CostReportRequest();
        request.setTeamName("platform");
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setEndDate(LocalDate.of(2025, 1, 31));
        return request;
    }
}