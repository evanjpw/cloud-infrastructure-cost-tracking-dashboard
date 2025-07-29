package com.dashboard.service.interfaces;

import com.dashboard.model.analytics.PredictionResult;
import com.dashboard.model.analytics.TrendAnalysis;
import com.dashboard.model.analytics.TeamComparison;
import com.dashboard.dto.analytics.PredictionRequest;
import com.dashboard.dto.analytics.TrendAnalysisRequest;
import com.dashboard.dto.analytics.ComparisonRequest;

import java.util.List;

/**
 * Service interface for advanced analytics features including:
 * - Predictive cost modeling
 * - Trend analysis with seasonal patterns
 * - Team and service comparisons
 * - Anomaly detection
 */
public interface AnalyticsService {
    
    /**
     * Generate cost predictions using various models (linear, exponential, seasonal)
     * @param request Prediction parameters including method, time horizon, and data filters
     * @return Prediction results with confidence intervals and forecasts
     */
    PredictionResult predictCosts(PredictionRequest request);
    
    /**
     * Analyze cost trends and identify patterns
     * @param request Trend analysis parameters
     * @return Detailed trend analysis with growth rates, seasonality, and insights
     */
    TrendAnalysis analyzeTrends(TrendAnalysisRequest request);
    
    /**
     * Compare teams, services, or regions for cost efficiency
     * @param request Comparison parameters and metrics
     * @return Comparative analysis with rankings and benchmarks
     */
    TeamComparison compareEntities(ComparisonRequest request);
    
    /**
     * Detect cost anomalies using statistical methods
     * @param teamName Team to analyze
     * @param startDate Analysis start date
     * @param endDate Analysis end date
     * @param threshold Anomaly detection threshold (default: 2.0 standard deviations)
     * @return List of detected anomalies with severity scores
     */
    List<Object> detectAnomalies(String teamName, String startDate, String endDate, Double threshold);
}