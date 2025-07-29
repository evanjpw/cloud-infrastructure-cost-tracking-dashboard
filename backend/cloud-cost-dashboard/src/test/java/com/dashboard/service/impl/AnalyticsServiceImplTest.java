package com.dashboard.service.impl;

import com.dashboard.model.analytics.PredictionResult;
import com.dashboard.model.analytics.TrendAnalysis;
import com.dashboard.model.analytics.TeamComparison;
import com.dashboard.dto.analytics.PredictionRequest;
import com.dashboard.dto.analytics.TrendAnalysisRequest;
import com.dashboard.dto.analytics.ComparisonRequest;

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

@DisplayName("Analytics Service Tests")
class AnalyticsServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private List<Map<String, Object>> mockHistoricalData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockHistoricalData = createMockHistoricalData();
    }

    @Test
    @DisplayName("Should generate linear predictions successfully")
    void testPredictCosts_Linear() {
        // Given
        PredictionRequest request = new PredictionRequest("linear", 7, "platform", "2025-01-01", "2025-01-31");
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(mockHistoricalData);

        // When
        PredictionResult result = analyticsService.predictCosts(request);

        // Then
        assertNotNull(result);
        assertEquals("linear", result.getMethod());
        assertEquals(7, result.getPredictions().size());
        assertTrue(result.getConfidence() >= 0);
        assertTrue(result.getConfidence() <= 1);

        // Verify all predictions have required fields
        for (Map<String, Object> prediction : result.getPredictions()) {
            assertNotNull(prediction.get("date"));
            assertNotNull(prediction.get("predictedCost"));
            assertNotNull(prediction.get("lowerBound"));
            assertNotNull(prediction.get("upperBound"));
            
            double predictedCost = (Double) prediction.get("predictedCost");
            double lowerBound = (Double) prediction.get("lowerBound");
            double upperBound = (Double) prediction.get("upperBound");
            
            assertTrue(predictedCost >= 0, "Predicted cost should be non-negative");
            assertTrue(lowerBound >= 0, "Lower bound should be non-negative");
            assertTrue(upperBound >= predictedCost, "Upper bound should be >= predicted cost");
        }

        // Verify metadata contains regression parameters
        assertNotNull(result.getMetadata());
        assertTrue(result.getMetadata().containsKey("slope"));
        assertTrue(result.getMetadata().containsKey("intercept"));
    }

    @Test
    @DisplayName("Should generate exponential predictions successfully")
    void testPredictCosts_Exponential() {
        // Given
        PredictionRequest request = new PredictionRequest("exponential", 14, "frontend", "2025-01-01", "2025-01-31");
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(mockHistoricalData);

        // When
        PredictionResult result = analyticsService.predictCosts(request);

        // Then
        assertNotNull(result);
        assertEquals("exponential", result.getMethod());
        assertEquals(14, result.getPredictions().size());
        assertEquals(0.8, result.getConfidence(), 0.01);

        // Verify metadata contains exponential smoothing parameters
        assertNotNull(result.getMetadata());
        assertTrue(result.getMetadata().containsKey("alpha"));
        assertTrue(result.getMetadata().containsKey("trend"));
    }

    @Test
    @DisplayName("Should generate seasonal predictions successfully")
    void testPredictCosts_Seasonal() {
        // Given
        PredictionRequest request = new PredictionRequest("seasonal", 30, "backend", "2025-01-01", "2025-01-31");
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(mockHistoricalData);

        // When
        PredictionResult result = analyticsService.predictCosts(request);

        // Then
        assertNotNull(result);
        assertEquals("seasonal", result.getMethod());
        assertEquals(30, result.getPredictions().size());
        assertEquals(0.85, result.getConfidence(), 0.01);

        // Verify seasonal metadata
        assertNotNull(result.getMetadata());
        assertTrue(result.getMetadata().containsKey("seasonLength"));
        assertTrue(result.getMetadata().containsKey("avgCost"));
    }

    @Test
    @DisplayName("Should generate growth predictions successfully")
    void testPredictCosts_Growth() {
        // Given
        PredictionRequest request = new PredictionRequest("growth", 90, "data", "2025-01-01", "2025-01-31");
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(mockHistoricalData);

        // When
        PredictionResult result = analyticsService.predictCosts(request);

        // Then
        assertNotNull(result);
        assertEquals("growth", result.getMethod());
        assertEquals(90, result.getPredictions().size());
        assertEquals(0.75, result.getConfidence(), 0.01);

        // Verify growth metadata
        assertNotNull(result.getMetadata());
        assertTrue(result.getMetadata().containsKey("growthRate"));
        assertTrue(result.getMetadata().containsKey("avgCost"));
    }

    @Test
    @DisplayName("Should throw exception for unsupported prediction method")
    void testPredictCosts_UnsupportedMethod() {
        // Given
        PredictionRequest request = new PredictionRequest("unsupported", 7, "platform", "2025-01-01", "2025-01-31");
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(mockHistoricalData);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> analyticsService.predictCosts(request));
        assertTrue(exception.getMessage().contains("Unsupported prediction method"));
    }

    @Test
    @DisplayName("Should throw exception when no historical data found")
    void testPredictCosts_NoData() {
        // Given
        PredictionRequest request = new PredictionRequest("linear", 7, "nonexistent", "2025-01-01", "2025-01-31");
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(Collections.emptyList());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> analyticsService.predictCosts(request));
        assertTrue(exception.getMessage().contains("No historical data found"));
    }

    @Test
    @DisplayName("Should analyze trends successfully")
    void testAnalyzeTrends() {
        // Given
        TrendAnalysisRequest request = new TrendAnalysisRequest("platform", "2025-01-01", "2025-01-31");
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(mockHistoricalData);

        // When
        TrendAnalysis result = analyticsService.analyzeTrends(request);

        // Then
        assertNotNull(result);
        assertEquals(mockHistoricalData.size(), result.getDataPoints());
        assertEquals("2025-01-01 to 2025-01-31", result.getAnalysisPeriod());
        assertNotNull(result.getOverallTrend());
        assertNotNull(result.getAnomalies());
        assertNotNull(result.getSummary());

        // Verify summary contains expected insights
        Map<String, Object> summary = result.getSummary();
        assertTrue(summary.containsKey("overall"));
        assertTrue(summary.containsKey("recommendation"));
        assertTrue(summary.containsKey("details"));
    }

    @Test
    @DisplayName("Should compare teams successfully")
    void testCompareEntities_Teams() {
        // Given
        ComparisonRequest request = new ComparisonRequest("teams", "2025-01-01", "2025-01-31");
        List<Map<String, Object>> mockTeamData = createMockTeamComparisonData();
        when(jdbcTemplate.queryForList(anyString(), anyString(), anyString())).thenReturn(mockTeamData);

        // When
        TeamComparison result = analyticsService.compareEntities(request);

        // Then
        assertNotNull(result);
        assertEquals("teams", result.getComparisonType());
        assertEquals("2025-01-01 to 2025-01-31", result.getAnalysisPeriod());
        assertNotNull(result.getTeams());
        assertNotNull(result.getBenchmarks());
        assertTrue(result.getTotalEntities() > 0);

        // Verify team rankings are assigned
        for (Map<String, Object> team : result.getTeams()) {
            assertTrue(team.containsKey("rank"));
            assertTrue(team.containsKey("efficiency"));
        }

        // Verify benchmarks contain expected metrics
        Map<String, Object> benchmarks = result.getBenchmarks();
        assertTrue(benchmarks.containsKey("avgTotalCost"));
        assertTrue(benchmarks.containsKey("avgEfficiency"));
    }

    @Test
    @DisplayName("Should detect anomalies successfully")
    void testDetectAnomalies() {
        // Given
        String teamName = "platform";
        String startDate = "2025-01-01";
        String endDate = "2025-01-31";
        Double threshold = 2.0;
        
        // Create data with anomalies
        List<Map<String, Object>> dataWithAnomalies = createMockDataWithAnomalies();
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(dataWithAnomalies);

        // When
        List<Object> anomalies = analyticsService.detectAnomalies(teamName, startDate, endDate, threshold);

        // Then
        assertNotNull(anomalies);
        assertTrue(anomalies.size() > 0, "Should detect at least one anomaly");

        // Verify anomaly structure
        Map<String, Object> anomaly = (Map<String, Object>) anomalies.get(0);
        assertTrue(anomaly.containsKey("date"));
        assertTrue(anomaly.containsKey("service"));
        assertTrue(anomaly.containsKey("actualCost"));
        assertTrue(anomaly.containsKey("expectedCost"));
        assertTrue(anomaly.containsKey("deviationScore"));
        assertTrue(anomaly.containsKey("severity"));
        assertTrue(anomaly.containsKey("type"));

        // Verify deviation score exceeds threshold
        double deviationScore = (Double) anomaly.get("deviationScore");
        assertTrue(deviationScore > threshold, "Detected anomaly should exceed threshold");
    }

    @Test
    @DisplayName("Should handle edge cases gracefully")
    void testEdgeCases() {
        // Test with minimal data
        List<Map<String, Object>> minimalData = Collections.singletonList(
            Map.of("date", "2025-01-01", "cost", new BigDecimal("100.00"))
        );
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(minimalData);

        PredictionRequest request = new PredictionRequest("linear", 1, "test", "2025-01-01", "2025-01-01");
        
        // Should not throw exception but handle gracefully
        assertDoesNotThrow(() -> {
            PredictionResult result = analyticsService.predictCosts(request);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Should validate prediction bounds")
    void testPredictionBounds() {
        // Given
        PredictionRequest request = new PredictionRequest("linear", 5, "platform", "2025-01-01", "2025-01-31");
        when(jdbcTemplate.queryForList(anyString(), (Object[]) any())).thenReturn(mockHistoricalData);

        // When
        PredictionResult result = analyticsService.predictCosts(request);

        // Then
        for (Map<String, Object> prediction : result.getPredictions()) {
            double predictedCost = (Double) prediction.get("predictedCost");
            double lowerBound = (Double) prediction.get("lowerBound");
            double upperBound = (Double) prediction.get("upperBound");

            assertTrue(lowerBound <= predictedCost, "Lower bound should be <= predicted cost");
            assertTrue(predictedCost <= upperBound, "Predicted cost should be <= upper bound");
            assertTrue(lowerBound >= 0, "Lower bound should be non-negative");
        }
    }

    // ========================================
    // HELPER METHODS FOR TEST DATA
    // ========================================

    private List<Map<String, Object>> createMockHistoricalData() {
        List<Map<String, Object>> data = new ArrayList<>();
        
        // Generate 30 days of realistic cost data with trend
        for (int i = 1; i <= 30; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("date", "2025-01-" + String.format("%02d", i));
            record.put("team_name", "platform");
            record.put("service_name", "EC2");
            record.put("region", "us-east-1");
            record.put("provider", "aws");
            
            // Add trend and some noise
            double baseCost = 1000.0;
            double trend = i * 5.0; // $5 increase per day
            double noise = Math.sin(i * 0.5) * 50; // Seasonal variation
            double totalCost = baseCost + trend + noise;
            
            record.put("cost", new BigDecimal(String.valueOf(totalCost)));
            record.put("usage_quantity", new BigDecimal("100.0"));
            
            data.add(record);
        }
        
        return data;
    }

    private List<Map<String, Object>> createMockDataWithAnomalies() {
        List<Map<String, Object>> data = createMockHistoricalData();
        
        // Add anomalous data points
        Map<String, Object> anomaly1 = new HashMap<>();
        anomaly1.put("date", "2025-01-15");
        anomaly1.put("team_name", "platform");
        anomaly1.put("service_name", "EC2");
        anomaly1.put("cost", new BigDecimal("5000.0")); // Spike
        
        Map<String, Object> anomaly2 = new HashMap<>();
        anomaly2.put("date", "2025-01-25");
        anomaly2.put("team_name", "platform");
        anomaly2.put("service_name", "S3");
        anomaly2.put("cost", new BigDecimal("10.0")); // Drop
        
        data.add(anomaly1);
        data.add(anomaly2);
        
        return data;
    }

    private List<Map<String, Object>> createMockTeamComparisonData() {
        List<Map<String, Object>> teams = new ArrayList<>();
        
        Map<String, Object> team1 = new HashMap<>();
        team1.put("team_name", "platform");
        team1.put("total_cost", new BigDecimal("15000.00"));
        team1.put("avg_daily_cost", new BigDecimal("500.00"));
        team1.put("service_count", 5L);
        teams.add(team1);
        
        Map<String, Object> team2 = new HashMap<>();
        team2.put("team_name", "frontend");
        team2.put("total_cost", new BigDecimal("8000.00"));
        team2.put("avg_daily_cost", new BigDecimal("266.67"));
        team2.put("service_count", 3L);
        teams.add(team2);
        
        Map<String, Object> team3 = new HashMap<>();
        team3.put("team_name", "backend");
        team3.put("total_cost", new BigDecimal("12000.00"));
        team3.put("avg_daily_cost", new BigDecimal("400.00"));
        team3.put("service_count", 4L);
        teams.add(team3);
        
        return teams;
    }
}