package com.dashboard.service.impl;

import com.dashboard.model.scenario.WhatIfScenario;
import com.dashboard.model.scenario.ScenarioComparison;
import com.dashboard.dto.scenario.CreateScenarioRequest;
import com.dashboard.dto.scenario.ScenarioComparisonRequest;

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

@DisplayName("Scenario Service Tests")
class ScenarioServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ScenarioServiceImpl scenarioService;

    private CreateScenarioRequest validCreateRequest;
    private ScenarioComparisonRequest validComparisonRequest;
    private List<Map<String, Object>> mockBaselineData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        validCreateRequest = new CreateScenarioRequest();
        validCreateRequest.setName("Test Scenario");
        validCreateRequest.setDescription("Test scenario for rightsizing");
        validCreateRequest.setType("rightsizing");
        validCreateRequest.setScope("team");
        validCreateRequest.setTarget("platform");
        validCreateRequest.setTimeHorizonDays(30);
        validCreateRequest.setDifficultyLevel("beginner");
        validCreateRequest.setCreatedBy("test-user");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("rightsizing_reduction", 25.0);
        validCreateRequest.setParameters(parameters);

        validComparisonRequest = new ScenarioComparisonRequest();
        validComparisonRequest.setScenarioIds(Arrays.asList("scenario-1", "scenario-2"));
        validComparisonRequest.setAnalysisMethod("cost_optimization");
        validComparisonRequest.setCreatedBy("test-user");

        mockBaselineData = createMockBaselineData();
    }

    @Test
    @DisplayName("Should create cost optimization scenario successfully")
    void testCreateCostOptimizationScenario() {
        // Given
        validCreateRequest.setType("cost_optimization");
        Map<String, Object> params = new HashMap<>();
        params.put("optimization_percentage", 15.0);
        validCreateRequest.setParameters(params);
        
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, any()))
            .thenReturn(mockBaselineData);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(1);

        // When
        WhatIfScenario result = scenarioService.createScenario(validCreateRequest);

        // Then
        assertNotNull(result);
        assertEquals("Test Scenario", result.getName());
        assertEquals("cost_optimization", result.getType());
        assertEquals("completed", result.getStatus());
        assertNotNull(result.getId());
        assertNotNull(result.getBaselineData());
        assertNotNull(result.getProjectedData());
        assertNotNull(result.getImpact());
        assertNotNull(result.getRiskAssessment());
        assertNotNull(result.getImplementationSteps());
        
        // Verify cost reduction
        assertTrue(result.getImpact().getTotalCostDifference().compareTo(BigDecimal.ZERO) < 0);
        assertEquals(-15.0, result.getImpact().getTotalPercentageChange(), 0.1);
        
        verify(jdbcTemplate).update(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should create rightsizing scenario successfully")
    void testCreateRightsizingScenario() {
        // Given
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, any()))
            .thenReturn(mockBaselineData);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(1);

        // When
        WhatIfScenario result = scenarioService.createScenario(validCreateRequest);

        // Then
        assertNotNull(result);
        assertEquals("rightsizing", result.getType());
        assertEquals("beginner", result.getDifficultyLevel());
        assertEquals(30, result.getTimeHorizonDays());
        
        // Verify rightsizing-specific projections
        assertNotNull(result.getProjectedData());
        assertTrue(result.getProjectedData().containsKey("rightsizingReduction"));
        assertEquals("rightsizing", result.getProjectedData().get("projectionMethod"));
        
        // Verify implementation steps are rightsizing-specific
        assertTrue(result.getImplementationSteps().stream()
            .anyMatch(step -> step.contains("utilization")));
    }

    @Test
    @DisplayName("Should create reserved instance scenario successfully")
    void testCreateReservedInstanceScenario() {
        // Given
        validCreateRequest.setType("reserved_instances");
        Map<String, Object> params = new HashMap<>();
        params.put("ri_term", "3year");
        validCreateRequest.setParameters(params);
        
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, any()))
            .thenReturn(mockBaselineData);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(1);

        // When
        WhatIfScenario result = scenarioService.createScenario(validCreateRequest);

        // Then
        assertNotNull(result);
        assertEquals("reserved_instances", result.getType());
        
        // Verify RI-specific projections
        Map<String, Object> projected = result.getProjectedData();
        assertEquals("3year", projected.get("riTerm"));
        assertEquals(40.0, projected.get("riSavingsPercentage")); // 3-year RI should have 40% savings
        assertEquals("reserved_instances", projected.get("projectionMethod"));
        
        // Verify significant cost savings
        assertTrue(result.getImpact().getTotalCostDifference().compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    @DisplayName("Should create spot instance scenario with appropriate risk assessment")
    void testCreateSpotInstanceScenario() {
        // Given
        validCreateRequest.setType("spot_instances");
        validCreateRequest.setDifficultyLevel("advanced");
        Map<String, Object> params = new HashMap<>();
        params.put("spot_percentage", 60.0);
        validCreateRequest.setParameters(params);
        
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, any()))
            .thenReturn(mockBaselineData);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(1);

        // When
        WhatIfScenario result = scenarioService.createScenario(validCreateRequest);

        // Then
        assertNotNull(result);
        assertEquals("spot_instances", result.getType());
        
        // Verify spot-specific projections
        Map<String, Object> projected = result.getProjectedData();
        assertEquals(60.0, projected.get("spotPercentage"));
        assertEquals("medium", projected.get("interruptionRisk"));
        
        // Verify high risk assessment due to spot instances
        WhatIfScenario.RiskAssessment risk = result.getRiskAssessment();
        assertTrue(risk.getFactors().stream()
            .anyMatch(factor -> factor.contains("interruption")));
        assertTrue(risk.getMitigationStrategies().containsKey("interruption_handling"));
        
        // Should still show significant savings
        assertTrue(result.getImpact().getTotalCostDifference().compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    @DisplayName("Should create scaling scenario with cost increase")
    void testCreateScalingScenario() {
        // Given
        validCreateRequest.setType("scaling");
        Map<String, Object> params = new HashMap<>();
        params.put("scaling_factor", 2.0); // 100% scale up
        validCreateRequest.setParameters(params);
        
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, any()))
            .thenReturn(mockBaselineData);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(1);

        // When
        WhatIfScenario result = scenarioService.createScenario(validCreateRequest);

        // Then
        assertNotNull(result);
        assertEquals("scaling", result.getType());
        
        // Verify scaling results in cost increase
        assertTrue(result.getImpact().getTotalCostDifference().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(100.0, result.getImpact().getTotalPercentageChange(), 0.1);
        
        // Verify scaling-specific projections
        Map<String, Object> projected = result.getProjectedData();
        assertEquals(2.0, projected.get("scalingFactor"));
        assertEquals("scaling", projected.get("projectionMethod"));
    }

    @Test
    @DisplayName("Should throw exception for invalid scenario request")
    void testCreateScenario_InvalidRequest() {
        // Given
        CreateScenarioRequest invalidRequest = new CreateScenarioRequest();
        invalidRequest.setName(""); // Empty name
        invalidRequest.setDescription("Test");
        invalidRequest.setType("invalid_type"); // Invalid type

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> scenarioService.createScenario(invalidRequest));
        assertTrue(exception.getMessage().contains("Invalid scenario request"));
    }

    @Test
    @DisplayName("Should compare scenarios successfully")
    void testCompareScenarios() {
        // Given
        List<Map<String, Object>> mockScenarioRows = createMockScenarioRows();
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, eq("scenario-1"), eq("scenario-2")))
            .thenReturn(mockScenarioRows);

        // When
        ScenarioComparison result = scenarioService.compareScenarios(validComparisonRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("cost_optimization", result.getAnalysisMethod());
        assertEquals("test-user", result.getCreatedBy());
        assertNotNull(result.getScenarios());
        assertEquals(2, result.getScenarios().size());
        assertNotNull(result.getBestScenario());
        assertNotNull(result.getWorstScenario());
        assertNotNull(result.getComparisonMetrics());
        assertNotNull(result.getRecommendations());
        
        // Verify best scenario has lower cost
        assertTrue(result.getBestScenario().getTotalCost()
            .compareTo(result.getWorstScenario().getTotalCost()) <= 0);
    }

    @Test
    @DisplayName("Should compare scenarios with risk-adjusted analysis")
    void testCompareScenarios_RiskAdjusted() {
        // Given
        validComparisonRequest.setAnalysisMethod("risk_adjusted");
        List<Map<String, Object>> mockScenarioRows = createMockScenarioRows();
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, eq("scenario-1"), eq("scenario-2")))
            .thenReturn(mockScenarioRows);

        // When
        ScenarioComparison result = scenarioService.compareScenarios(validComparisonRequest);

        // Then
        assertNotNull(result);
        assertEquals("risk_adjusted", result.getAnalysisMethod());
        
        // In risk-adjusted analysis, best scenario should have lower risk score
        assertTrue(result.getBestScenario().getRiskScore() 
            <= result.getWorstScenario().getRiskScore());
    }

    @Test
    @DisplayName("Should throw exception when scenarios not found")
    void testCompareScenarios_ScenariosNotFound() {
        // Given
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, eq("scenario-1"), eq("scenario-2")))
            .thenReturn(Collections.singletonList(createMockScenarioRows().get(0))); // Only 1 instead of 2

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> scenarioService.compareScenarios(validComparisonRequest));
        assertTrue(exception.getMessage().contains("Some scenarios not found"));
    }

    @Test
    @DisplayName("Should get scenario templates for all difficulty levels")
    void testGetScenarioTemplates_AllLevels() {
        // When
        List<Object> result = scenarioService.getScenarioTemplates(null);

        // Then
        assertNotNull(result);
        assertTrue(result.size() >= 3); // Should have templates from all difficulty levels
        
        // Verify template structure
        @SuppressWarnings("unchecked")
        Map<String, Object> firstTemplate = (Map<String, Object>) result.get(0);
        assertTrue(firstTemplate.containsKey("id"));
        assertTrue(firstTemplate.containsKey("name"));
        assertTrue(firstTemplate.containsKey("difficulty"));
        assertTrue(firstTemplate.containsKey("type"));
        assertTrue(firstTemplate.containsKey("description"));
        assertTrue(firstTemplate.containsKey("estimatedSavings"));
        assertTrue(firstTemplate.containsKey("timeToComplete"));
        assertTrue(firstTemplate.containsKey("skillsRequired"));
    }

    @Test
    @DisplayName("Should get scenario templates for specific difficulty level")
    void testGetScenarioTemplates_SpecificDifficulty() {
        // When
        List<Object> result = scenarioService.getScenarioTemplates("beginner");

        // Then
        assertNotNull(result);
        assertTrue(result.size() >= 1);
        
        // Verify all templates are beginner level
        for (Object template : result) {
            @SuppressWarnings("unchecked")
            Map<String, Object> templateMap = (Map<String, Object>) template;
            assertEquals("beginner", templateMap.get("difficulty"));
        }
    }

    @Test
    @DisplayName("Should validate scenario successfully")
    void testValidateScenario() {
        // When
        Object result = scenarioService.validateScenario(validCreateRequest);

        // Then
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        Map<String, Object> validation = (Map<String, Object>) result;
        
        assertTrue(validation.containsKey("isValid"));
        assertTrue(validation.containsKey("errors"));
        assertTrue(validation.containsKey("warnings"));
        assertTrue(validation.containsKey("feasibility"));
        assertTrue(validation.containsKey("complexityScore"));
        assertTrue(validation.containsKey("complexityLevel"));
        assertTrue(validation.containsKey("estimatedDuration"));
        assertTrue(validation.containsKey("requiredSkills"));
        
        // Should be valid
        assertTrue((Boolean) validation.get("isValid"));
        
        // Should have reasonable complexity
        Integer complexityScore = (Integer) validation.get("complexityScore");
        assertTrue(complexityScore >= 1 && complexityScore <= 10);
        
        // Should have required skills
        @SuppressWarnings("unchecked")
        List<String> skills = (List<String>) validation.get("requiredSkills");
        assertFalse(skills.isEmpty());
    }

    @Test
    @DisplayName("Should validate invalid scenario with errors")
    void testValidateScenario_Invalid() {
        // Given
        CreateScenarioRequest invalidRequest = new CreateScenarioRequest();
        invalidRequest.setName(""); // Invalid empty name
        invalidRequest.setType("invalid_type"); // Invalid type

        // When
        Object result = scenarioService.validateScenario(invalidRequest);

        // Then
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        Map<String, Object> validation = (Map<String, Object>) result;
        
        // Should be invalid
        assertFalse((Boolean) validation.get("isValid"));
        
        // Should have errors
        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) validation.get("errors");
        assertFalse(errors.isEmpty());
    }

    @Test
    @DisplayName("Should validate advanced scenario with higher complexity")
    void testValidateScenario_AdvancedComplexity() {
        // Given
        validCreateRequest.setType("infrastructure_change");
        validCreateRequest.setDifficultyLevel("advanced");
        validCreateRequest.setTimeHorizonDays(120);

        // When
        Object result = scenarioService.validateScenario(validCreateRequest);

        // Then
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        Map<String, Object> validation = (Map<String, Object>) result;
        
        Integer complexityScore = (Integer) validation.get("complexityScore");
        String complexityLevel = (String) validation.get("complexityLevel");
        
        // Advanced infrastructure change should have high complexity
        assertTrue(complexityScore >= 7);
        assertTrue("high".equals(complexityLevel) || "very_high".equals(complexityLevel));
        
        // Should have longer implementation duration
        String duration = (String) validation.get("estimatedDuration");
        assertTrue(duration.contains("month") || duration.contains("weeks"));
    }

    @Test
    @DisplayName("Should handle team-scoped scenario correctly")
    void testCreateScenario_TeamScope() {
        // Given
        validCreateRequest.setScope("team");
        validCreateRequest.setTarget("platform");
        
        when(jdbcTemplate.queryForList(contains("team_name = ?"), (Class<Map<String, Object>>) null, any(), any()))
            .thenReturn(mockBaselineData);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(1);

        // When
        WhatIfScenario result = scenarioService.createScenario(validCreateRequest);

        // Then
        assertNotNull(result);
        verify(jdbcTemplate).queryForList(contains("team_name = ?"), (Class<Map<String, Object>>) null, any(), any());
    }

    @Test
    @DisplayName("Should handle service-scoped scenario correctly")
    void testCreateScenario_ServiceScope() {
        // Given
        validCreateRequest.setScope("service");
        validCreateRequest.setTarget("EC2");
        
        when(jdbcTemplate.queryForList(contains("service_name = ?"), (Class<Map<String, Object>>) null, any(), any()))
            .thenReturn(mockBaselineData);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(1);

        // When
        WhatIfScenario result = scenarioService.createScenario(validCreateRequest);

        // Then
        assertNotNull(result);
        verify(jdbcTemplate).queryForList(contains("service_name = ?"), (Class<Map<String, Object>>) null, any(), any());
    }

    @Test
    @DisplayName("Should generate appropriate risk assessment for different scenario types")
    void testRiskAssessmentByScenarioType() {
        // Test spot instances (high risk)
        validCreateRequest.setType("spot_instances");
        when(jdbcTemplate.queryForList(anyString(), (Class<Map<String, Object>>) null, any()))
            .thenReturn(mockBaselineData);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(1);

        WhatIfScenario spotScenario = scenarioService.createScenario(validCreateRequest);
        WhatIfScenario.RiskAssessment spotRisk = spotScenario.getRiskAssessment();
        
        assertTrue(spotRisk.getFactors().stream()
            .anyMatch(factor -> factor.toLowerCase().contains("spot") || factor.toLowerCase().contains("interruption")));
        
        // Test cost optimization (low risk)
        validCreateRequest.setType("cost_optimization");
        WhatIfScenario optimizationScenario = scenarioService.createScenario(validCreateRequest);
        WhatIfScenario.RiskAssessment optimizationRisk = optimizationScenario.getRiskAssessment();
        
        assertTrue(optimizationRisk.getConfidenceScore() > spotRisk.getConfidenceScore());
    }

    // ========================================
    // HELPER METHODS FOR TEST DATA
    // ========================================

    private List<Map<String, Object>> createMockBaselineData() {
        List<Map<String, Object>> data = new ArrayList<>();
        
        // 30 days of mock data
        for (int i = 1; i <= 30; i++) {
            Map<String, Object> ec2Record = new HashMap<>();
            ec2Record.put("day", "2025-01-" + String.format("%02d", i));
            ec2Record.put("service_name", "EC2");
            ec2Record.put("daily_cost", new BigDecimal("100.00"));
            ec2Record.put("daily_usage", new BigDecimal("24.0"));
            ec2Record.put("usage_unit", "instance-hours");
            data.add(ec2Record);
            
            Map<String, Object> s3Record = new HashMap<>();
            s3Record.put("day", "2025-01-" + String.format("%02d", i));
            s3Record.put("service_name", "S3");
            s3Record.put("daily_cost", new BigDecimal("20.00"));
            s3Record.put("daily_usage", new BigDecimal("100.0"));
            s3Record.put("usage_unit", "GB-storage");
            data.add(s3Record);
        }
        
        return data;
    }

    private List<Map<String, Object>> createMockScenarioRows() {
        List<Map<String, Object>> scenarios = new ArrayList<>();
        
        // Scenario 1 - Cost optimization
        Map<String, Object> scenario1 = new HashMap<>();
        scenario1.put("id", "scenario-1");
        scenario1.put("name", "Cost Optimization Scenario");
        scenario1.put("description", "Optimize costs through rightsizing");
        scenario1.put("type", "cost_optimization");
        scenario1.put("status", "completed");
        scenario1.put("difficulty_level", "beginner");
        scenario1.put("time_horizon_days", 30);
        scenario1.put("created_by", "test-user");
        scenario1.put("created_at", "2025-01-01T00:00:00Z");
        scenario1.put("updated_at", "2025-01-01T00:00:00Z");
        scenario1.put("parameters", "{\"optimization_percentage\": 15.0}");
        scenario1.put("baseline_data", "{\"totalCost\": 3000.00}");
        scenario1.put("projected_data", "{\"totalCost\": 2550.00, \"costSavings\": 450.00}");
        scenarios.add(scenario1);
        
        // Scenario 2 - Reserved instances
        Map<String, Object> scenario2 = new HashMap<>();
        scenario2.put("id", "scenario-2");
        scenario2.put("name", "Reserved Instance Scenario");
        scenario2.put("description", "Use reserved instances for predictable workloads");
        scenario2.put("type", "reserved_instances");
        scenario2.put("status", "completed");
        scenario2.put("difficulty_level", "intermediate");
        scenario2.put("time_horizon_days", 30);
        scenario2.put("created_by", "test-user");
        scenario2.put("created_at", "2025-01-01T00:00:00Z");
        scenario2.put("updated_at", "2025-01-01T00:00:00Z");
        scenario2.put("parameters", "{\"ri_term\": \"1year\"}");
        scenario2.put("baseline_data", "{\"totalCost\": 3000.00}");
        scenario2.put("projected_data", "{\"totalCost\": 2100.00, \"costSavings\": 900.00}");
        scenarios.add(scenario2);
        
        return scenarios;
    }
}