package com.dashboard.service.impl;

import com.dashboard.model.optimization.OptimizationRecommendation;
import com.dashboard.model.optimization.OptimizationSummary;
import com.dashboard.dto.optimization.OptimizationRequest;

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

@DisplayName("Optimization Service Tests")
class OptimizationServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private OptimizationServiceImpl optimizationService;

    private List<Map<String, Object>> mockCostData;
    private List<Map<String, Object>> mockRecommendations;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockCostData = createMockCostData();
        mockRecommendations = createMockRecommendations();
    }

    @Test
    @DisplayName("Should generate optimization recommendations successfully")
    void testGenerateRecommendations() {
        // Given
        OptimizationRequest request = new OptimizationRequest("all", "2025-01-01", "2025-01-31");
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(mockCostData);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(1);

        // When
        List<OptimizationRecommendation> result = optimizationService.generateRecommendations(request);

        // Then
        assertNotNull(result);
        assertTrue(result.size() > 0, "Should generate at least one recommendation");

        // Verify recommendations have required fields
        for (OptimizationRecommendation rec : result) {
            assertNotNull(rec.getTitle(), "Recommendation should have title");
            assertNotNull(rec.getDescription(), "Recommendation should have description");
            assertNotNull(rec.getType(), "Recommendation should have type");
            assertNotNull(rec.getImpact(), "Recommendation should have impact");
            assertNotNull(rec.getPriority(), "Recommendation should have priority");
            assertTrue(rec.getPotentialSavings() >= 0, "Potential savings should be non-negative");
            assertNotNull(rec.getImplementationEffort(), "Recommendation should have implementation effort");
            assertNotNull(rec.getRiskLevel(), "Recommendation should have risk level");
            assertEquals("pending", rec.getStatus(), "New recommendations should have pending status");
        }

        // Verify recommendations are sorted by potential savings (descending)
        for (int i = 1; i < result.size(); i++) {
            assertTrue(result.get(i-1).getPotentialSavings() >= result.get(i).getPotentialSavings(),
                "Recommendations should be sorted by potential savings in descending order");
        }
    }

    @Test
    @DisplayName("Should generate rightsizing recommendations for high-cost EC2")
    void testGenerateRightsizingRecommendations() {
        // Given - Create cost data with expensive EC2 usage
        List<Map<String, Object>> expensiveEC2Data = createExpensiveEC2Data();
        OptimizationRequest request = new OptimizationRequest("all", "2025-01-01", "2025-01-31");
        
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(expensiveEC2Data);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(1);

        // When
        List<OptimizationRecommendation> result = optimizationService.generateRecommendations(request);

        // Then
        assertTrue(result.stream().anyMatch(rec -> "rightsizing".equals(rec.getType())),
            "Should generate rightsizing recommendation for expensive EC2");
        
        OptimizationRecommendation rightsizingRec = result.stream()
            .filter(rec -> "rightsizing".equals(rec.getType()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(rightsizingRec);
        assertTrue(rightsizingRec.getTitle().contains("Rightsize"));
        assertTrue(rightsizingRec.getPotentialSavings() > 0);
        assertTrue(rightsizingRec.getAffectedServices().contains("EC2"));
    }

    @Test
    @DisplayName("Should generate reserved instance recommendations for consistent usage")
    void testGenerateReservedInstanceRecommendations() {
        // Given - Create consistent EC2 usage data
        List<Map<String, Object>> consistentEC2Data = createConsistentEC2Data();
        OptimizationRequest request = new OptimizationRequest("all", "2025-01-01", "2025-01-31");
        
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(consistentEC2Data);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(1);

        // When
        List<OptimizationRecommendation> result = optimizationService.generateRecommendations(request);

        // Then
        assertTrue(result.stream().anyMatch(rec -> "reserved_instance".equals(rec.getType())),
            "Should generate reserved instance recommendation for consistent usage");
        
        OptimizationRecommendation riRec = result.stream()
            .filter(rec -> "reserved_instance".equals(rec.getType()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(riRec);
        assertTrue(riRec.getTitle().contains("Reserved"));
        assertEquals("high", riRec.getImpact());
        assertTrue(riRec.getPotentialSavings() > 0);
    }

    @Test
    @DisplayName("Should generate unused resource recommendations")
    void testGenerateUnusedResourceRecommendations() {
        // Given - Create data with unused resources (cost but no usage)
        List<Map<String, Object>> unusedResourceData = createUnusedResourceData();
        OptimizationRequest request = new OptimizationRequest("all", "2025-01-01", "2025-01-31");
        
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(unusedResourceData);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(1);

        // When
        List<OptimizationRecommendation> result = optimizationService.generateRecommendations(request);

        // Then
        assertTrue(result.stream().anyMatch(rec -> "unused_resource".equals(rec.getType())),
            "Should generate unused resource recommendation");
        
        OptimizationRecommendation unusedRec = result.stream()
            .filter(rec -> "unused_resource".equals(rec.getType()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(unusedRec);
        assertTrue(unusedRec.getTitle().contains("Unused"));
        assertEquals("high", unusedRec.getPriority()); // Unused resources should be high priority
        assertEquals("low", unusedRec.getRiskLevel()); // Low risk to remove unused resources
    }

    @Test
    @DisplayName("Should filter recommendations by request criteria")
    void testFilterRecommendations() {
        // Given
        OptimizationRequest request = new OptimizationRequest("all", "2025-01-01", "2025-01-31");
        request.setIncludeTypes(Arrays.asList("rightsizing"));
        request.setMinImpact("high");
        request.setMaxRisk("low");
        
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(mockCostData);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(1);

        // When
        List<OptimizationRecommendation> result = optimizationService.generateRecommendations(request);

        // Then
        for (OptimizationRecommendation rec : result) {
            assertTrue(request.getIncludeTypes().contains(rec.getType()),
                "Should only include specified types");
            assertNotEquals("low", rec.getImpact(), 
                "Should not include low impact recommendations when minImpact is high");
            assertNotEquals("high", rec.getRiskLevel(),
                "Should not include high risk recommendations when maxRisk is low");
        }
    }

    @Test
    @DisplayName("Should get optimization summary successfully")
    void testGetOptimizationSummary() {
        // Given
        String teamName = "platform";
        String startDate = "2025-01-01";
        String endDate = "2025-01-31";
        
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(mockRecommendations);
        when(jdbcTemplate.queryForList(anyString(), anyString(), anyString()))
            .thenReturn(Arrays.asList(Map.of("total_cost", new BigDecimal("10000.00"))));

        // When
        OptimizationSummary result = optimizationService.getOptimizationSummary(teamName, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(teamName, result.getTeamName());
        assertEquals(startDate + " to " + endDate, result.getAnalysisPeriod());
        assertTrue(result.getTotalPotentialSavings() >= 0);
        assertTrue(result.getRecommendationCount() >= 0);
        assertTrue(result.getHighImpactCount() >= 0);
        assertTrue(result.getImplementedCount() >= 0);
        assertTrue(result.getSavingsPercentage() >= 0);
        assertNotNull(result.getTypeBreakdown());
        assertNotNull(result.getGeneratedAt());
    }

    @Test
    @DisplayName("Should analyze recommendation details")
    void testAnalyzeRecommendation() {
        // Given
        String recommendationId = "rec_123";
        Map<String, Object> mockRec = createMockRecommendation("rightsizing");
        
        when(jdbcTemplate.queryForList(anyString(), eq(recommendationId)))
            .thenReturn(Arrays.asList(mockRec));

        // When
        Object result = optimizationService.analyzeRecommendation(recommendationId);

        // Then
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        Map<String, Object> analysis = (Map<String, Object>) result;
        
        assertTrue(analysis.containsKey("recommendation"));
        assertTrue(analysis.containsKey("detailedImpact"));
        assertTrue(analysis.containsKey("implementationPlan"));
        assertTrue(analysis.containsKey("riskAssessment"));
        assertTrue(analysis.containsKey("timelineEstimate"));
        
        // Verify implementation plan contains steps
        @SuppressWarnings("unchecked")
        List<String> implementationPlan = (List<String>) analysis.get("implementationPlan");
        assertNotNull(implementationPlan);
        assertTrue(implementationPlan.size() > 0);
    }

    @Test
    @DisplayName("Should update recommendation status")
    void testUpdateRecommendationStatus() {
        // Given
        String recommendationId = "rec_123";
        String action = "accepted";
        String notes = "Approved for implementation";
        
        Map<String, Object> updatedRec = createMockRecommendation("rightsizing");
        updatedRec.put("status", action);
        updatedRec.put("status_notes", notes);
        
        when(jdbcTemplate.update(anyString(), eq(action), eq(notes), eq(recommendationId)))
            .thenReturn(1);
        when(jdbcTemplate.queryForList(anyString(), eq(recommendationId)))
            .thenReturn(Arrays.asList(updatedRec));

        // When
        Object result = optimizationService.updateRecommendationStatus(recommendationId, action, notes);

        // Then
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        Map<String, Object> recommendation = (Map<String, Object>) result;
        assertEquals(action, recommendation.get("status"));
        assertEquals(notes, recommendation.get("status_notes"));
    }

    @Test
    @DisplayName("Should throw exception when recommendation not found")
    void testUpdateRecommendationStatus_NotFound() {
        // Given
        String recommendationId = "nonexistent";
        String action = "accepted";
        String notes = "Test notes";
        
        when(jdbcTemplate.update(anyString(), eq(action), eq(notes), eq(recommendationId)))
            .thenReturn(0); // No rows updated

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> optimizationService.updateRecommendationStatus(recommendationId, action, notes));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("Should get optimization analytics")
    void testGetOptimizationAnalytics() {
        // Given
        List<Map<String, Object>> mockStats = Arrays.asList(
            Map.of(
                "total_recommendations", 15L,
                "total_potential_savings", new BigDecimal("5000.00"),
                "implemented_count", 3L,
                "high_impact_count", 8L,
                "avg_savings_per_recommendation", new BigDecimal("333.33")
            )
        );
        
        List<Map<String, Object>> mockTypeBreakdown = Arrays.asList(
            Map.of("type", "rightsizing", "count", 5L, "total_savings", new BigDecimal("2000.00")),
            Map.of("type", "reserved_instance", "count", 3L, "total_savings", new BigDecimal("1500.00"))
        );
        
        List<Map<String, Object>> mockTrends = Arrays.asList(
            Map.of("date", "2025-01-01", "recommendations_created", 2L, "implementations", 1L),
            Map.of("date", "2025-01-02", "recommendations_created", 3L, "implementations", 0L)
        );
        
        when(jdbcTemplate.queryForList(contains("COUNT(*) as total_recommendations")))
            .thenReturn(mockStats);
        when(jdbcTemplate.queryForList(contains("GROUP BY type")))
            .thenReturn(mockTypeBreakdown);
        when(jdbcTemplate.queryForList(contains("DATE(created_at) as date")))
            .thenReturn(mockTrends);

        // When
        Object result = optimizationService.getOptimizationAnalytics();

        // Then
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        Map<String, Object> analytics = (Map<String, Object>) result;
        
        assertTrue(analytics.containsKey("overallStats"));
        assertTrue(analytics.containsKey("typeBreakdown"));
        assertTrue(analytics.containsKey("implementationTrends"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> overallStats = (Map<String, Object>) analytics.get("overallStats");
        assertEquals(15L, overallStats.get("total_recommendations"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> typeBreakdown = (List<Map<String, Object>>) analytics.get("typeBreakdown");
        assertEquals(2, typeBreakdown.size());
    }

    @Test
    @DisplayName("Should handle empty cost data gracefully")
    void testGenerateRecommendations_EmptyData() {
        // Given
        OptimizationRequest request = new OptimizationRequest("all", "2025-01-01", "2025-01-31");
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(Collections.emptyList());

        // When
        List<OptimizationRecommendation> result = optimizationService.generateRecommendations(request);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Should return empty list when no cost data available");
    }

    @Test
    @DisplayName("Should handle team-specific scope")
    void testGenerateRecommendations_TeamScope() {
        // Given
        OptimizationRequest request = new OptimizationRequest("team:platform", "2025-01-01", "2025-01-31");
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(mockCostData);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(1);

        // When
        List<OptimizationRecommendation> result = optimizationService.generateRecommendations(request);

        // Then
        assertNotNull(result);
        // Verify that the SQL query was called with team filter
        verify(jdbcTemplate).queryForList(contains("team_name = ?"), (Object[]) any());
    }

    // ========================================
    // HELPER METHODS FOR TEST DATA
    // ========================================

    private List<Map<String, Object>> createMockCostData() {
        List<Map<String, Object>> data = new ArrayList<>();
        
        // Add various types of cost data for different recommendation types
        for (int i = 1; i <= 30; i++) {
            // EC2 data
            Map<String, Object> ec2Record = new HashMap<>();
            ec2Record.put("date", "2025-01-" + String.format("%02d", i));
            ec2Record.put("team_name", "platform");
            ec2Record.put("service_name", "EC2");
            ec2Record.put("region", "us-east-1");
            ec2Record.put("provider", "aws");
            ec2Record.put("cost", new BigDecimal("800.00"));
            ec2Record.put("usage_quantity", new BigDecimal("100.0"));
            ec2Record.put("usage_unit", "instance-hours");
            data.add(ec2Record);
            
            // S3 data
            Map<String, Object> s3Record = new HashMap<>();
            s3Record.put("date", "2025-01-" + String.format("%02d", i));
            s3Record.put("team_name", "data");
            s3Record.put("service_name", "S3");
            s3Record.put("region", "us-east-1");
            s3Record.put("provider", "aws");
            s3Record.put("cost", new BigDecimal("200.00"));
            s3Record.put("usage_quantity", new BigDecimal("1000.0"));
            s3Record.put("usage_unit", "GB-storage");
            data.add(s3Record);
        }
        
        return data;
    }

    private List<Map<String, Object>> createExpensiveEC2Data() {
        List<Map<String, Object>> data = new ArrayList<>();
        
        for (int i = 1; i <= 30; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("date", "2025-01-" + String.format("%02d", i));
            record.put("team_name", "platform");
            record.put("service_name", "EC2");
            record.put("cost", new BigDecimal("1500.00")); // High cost
            record.put("usage_quantity", new BigDecimal("50.0")); // Lower usage
            data.add(record);
        }
        
        return data;
    }

    private List<Map<String, Object>> createConsistentEC2Data() {
        List<Map<String, Object>> data = new ArrayList<>();
        
        // Create 25 days of very consistent EC2 usage
        for (int i = 1; i <= 25; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("date", "2025-01-" + String.format("%02d", i));
            record.put("team_name", "platform");
            record.put("service_name", "EC2");
            record.put("cost", new BigDecimal("500.00")); // Consistent cost
            record.put("usage_quantity", new BigDecimal("100.0"));
            data.add(record);
        }
        
        return data;
    }

    private List<Map<String, Object>> createUnusedResourceData() {
        List<Map<String, Object>> data = new ArrayList<>();
        
        // Create resources with cost but no usage
        Map<String, Object> unusedRecord = new HashMap<>();
        unusedRecord.put("date", "2025-01-15");
        unusedRecord.put("team_name", "backend");
        unusedRecord.put("service_name", "EBS");
        unusedRecord.put("cost", new BigDecimal("100.00")); // Has cost
        unusedRecord.put("usage_quantity", new BigDecimal("0.0")); // No usage
        data.add(unusedRecord);
        
        return data;
    }

    private List<Map<String, Object>> createMockRecommendations() {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        Map<String, Object> rec1 = new HashMap<>();
        rec1.put("id", "rec_1");
        rec1.put("type", "rightsizing");
        rec1.put("impact", "high");
        rec1.put("potential_savings", new BigDecimal("1000.00"));
        rec1.put("status", "pending");
        recommendations.add(rec1);
        
        Map<String, Object> rec2 = new HashMap<>();
        rec2.put("id", "rec_2");
        rec2.put("type", "reserved_instance");
        rec2.put("impact", "medium");
        rec2.put("potential_savings", new BigDecimal("500.00"));
        rec2.put("status", "implemented");
        recommendations.add(rec2);
        
        return recommendations;
    }

    private Map<String, Object> createMockRecommendation(String type) {
        Map<String, Object> rec = new HashMap<>();
        rec.put("id", "rec_123");
        rec.put("title", "Test Recommendation");
        rec.put("description", "Test description");
        rec.put("type", type);
        rec.put("impact", "high");
        rec.put("priority", "high");
        rec.put("potential_savings", new BigDecimal("1000.00"));
        rec.put("implementation_effort", "low");
        rec.put("risk_level", "low");
        rec.put("status", "pending");
        return rec;
    }
}