package com.dashboard.service.impl;

import com.dashboard.service.interfaces.AnalyticsService;
import com.dashboard.model.analytics.PredictionResult;
import com.dashboard.model.analytics.TrendAnalysis;
import com.dashboard.model.analytics.TeamComparison;
import com.dashboard.dto.analytics.PredictionRequest;
import com.dashboard.dto.analytics.TrendAnalysisRequest;
import com.dashboard.dto.analytics.ComparisonRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public PredictionResult predictCosts(PredictionRequest request) {
        System.out.println("Generating cost predictions using method: " + request.getMethod());
        
        // Fetch historical data
        List<Map<String, Object>> historicalData = fetchHistoricalData(
            request.getTeamName(), 
            request.getStartDate(), 
            request.getEndDate()
        );
        
        if (historicalData.isEmpty()) {
            throw new RuntimeException("No historical data found for prediction");
        }
        
        // Apply prediction algorithm based on method
        return switch (request.getMethod().toLowerCase()) {
            case "linear" -> generateLinearPrediction(historicalData, request);
            case "exponential" -> generateExponentialPrediction(historicalData, request);
            case "seasonal" -> generateSeasonalPrediction(historicalData, request);
            case "growth" -> generateGrowthPrediction(historicalData, request);
            default -> throw new IllegalArgumentException("Unsupported prediction method: " + request.getMethod());
        };
    }

    @Override
    public TrendAnalysis analyzeTrends(TrendAnalysisRequest request) {
        System.out.println("Analyzing trends for team: " + request.getTeamName());
        
        List<Map<String, Object>> costData = fetchHistoricalData(
            request.getTeamName(),
            request.getStartDate(),
            request.getEndDate()
        );
        
        TrendAnalysis analysis = new TrendAnalysis();
        analysis.setDataPoints(costData.size());
        analysis.setAnalysisPeriod(request.getStartDate() + " to " + request.getEndDate());
        
        // Calculate trend metrics
        Map<String, Object> trendMetrics = calculateTrendMetrics(costData);
        analysis.setOverallTrend((String) trendMetrics.get("overall"));
        analysis.setGrowthRate((Double) trendMetrics.get("growthRate"));
        analysis.setVolatility((Double) trendMetrics.get("volatility"));
        
        // Detect anomalies
        List<Object> anomalies = detectTrendAnomalies(costData);
        analysis.setAnomalies(anomalies);
        
        // Generate insights and recommendations
        Map<String, Object> insights = generateTrendInsights(trendMetrics, anomalies);
        analysis.setSummary(insights);
        
        return analysis;
    }

    @Override
    public TeamComparison compareEntities(ComparisonRequest request) {
        System.out.println("Comparing entities by: " + request.getComparisonType());
        
        TeamComparison comparison = new TeamComparison();
        
        switch (request.getComparisonType().toLowerCase()) {
            case "teams" -> {
                comparison = compareTeams(request.getStartDate(), request.getEndDate());
            }
            case "services" -> {
                comparison = compareServices(request.getStartDate(), request.getEndDate());
            }
            case "regions" -> {
                comparison = compareRegions(request.getStartDate(), request.getEndDate());
            }
            default -> throw new IllegalArgumentException("Unsupported comparison type: " + request.getComparisonType());
        }
        
        return comparison;
    }

    @Override
    public List<Object> detectAnomalies(String teamName, String startDate, String endDate, Double threshold) {
        System.out.println("Detecting anomalies for team: " + teamName + " with threshold: " + threshold);
        
        List<Map<String, Object>> costData = fetchHistoricalData(teamName, startDate, endDate);
        List<Object> anomalies = new ArrayList<>();
        
        // Calculate statistical baseline
        double[] costs = costData.stream()
            .mapToDouble(row -> ((BigDecimal) row.get("cost")).doubleValue())
            .toArray();
        
        double mean = Arrays.stream(costs).average().orElse(0.0);
        double stdDev = calculateStandardDeviation(costs, mean);
        
        // Detect anomalies using statistical thresholds
        for (int i = 0; i < costData.size(); i++) {
            Map<String, Object> dataPoint = costData.get(i);
            double cost = ((BigDecimal) dataPoint.get("cost")).doubleValue();
            double deviationScore = Math.abs(cost - mean) / stdDev;
            
            if (deviationScore > threshold) {
                Map<String, Object> anomaly = new HashMap<>();
                anomaly.put("date", dataPoint.get("date"));
                anomaly.put("service", dataPoint.get("service_name"));
                anomaly.put("actualCost", cost);
                anomaly.put("expectedCost", mean);
                anomaly.put("deviationScore", deviationScore);
                anomaly.put("severity", deviationScore > threshold * 1.5 ? "high" : "medium");
                anomaly.put("type", cost > mean ? "spike" : "drop");
                
                anomalies.add(anomaly);
            }
        }
        
        System.out.println("Detected " + anomalies.size() + " anomalies");
        return anomalies;
    }

    // ========================================
    // PRIVATE HELPER METHODS
    // ========================================

    private List<Map<String, Object>> fetchHistoricalData(String teamName, String startDate, String endDate) {
        String sql = """
            SELECT date, team_name, service_name, region, provider, 
                   SUM(cost) as cost, SUM(usage_quantity) as usage_quantity
            FROM enhanced_usage_records 
            WHERE date BETWEEN ? AND ?
            """;
        
        List<Object> params = new ArrayList<>();
        params.add(startDate);
        params.add(endDate);
        
        if (teamName != null && !teamName.equals("all")) {
            sql += " AND team_name = ?";
            params.add(teamName);
        }
        
        sql += " GROUP BY date, team_name, service_name, region, provider ORDER BY date";
        
        return jdbcTemplate.queryForList(sql, params.toArray());
    }

    private PredictionResult generateLinearPrediction(List<Map<String, Object>> data, PredictionRequest request) {
        // Simple linear regression implementation
        int n = data.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        
        for (int i = 0; i < n; i++) {
            double x = i + 1; // Day index
            double y = ((BigDecimal) data.get(i).get("cost")).doubleValue();
            
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;
        
        // Generate predictions
        List<Map<String, Object>> predictions = new ArrayList<>();
        for (int i = 1; i <= request.getDaysToPredict(); i++) {
            double predictedCost = slope * (n + i) + intercept;
            
            Map<String, Object> prediction = new HashMap<>();
            prediction.put("date", LocalDate.parse(request.getEndDate()).plusDays(i).toString());
            prediction.put("predictedCost", Math.max(0, predictedCost));
            prediction.put("lowerBound", Math.max(0, predictedCost * 0.9));
            prediction.put("upperBound", predictedCost * 1.1);
            
            predictions.add(prediction);
        }
        
        // Calculate R-squared for confidence
        double confidence = calculateRSquared(data, slope, intercept);
        
        PredictionResult result = new PredictionResult();
        result.setMethod("linear");
        result.setPredictions(predictions);
        result.setConfidence(confidence);
        result.setMetadata(Map.of("slope", slope, "intercept", intercept));
        
        return result;
    }

    private PredictionResult generateExponentialPrediction(List<Map<String, Object>> data, PredictionRequest request) {
        // Exponential smoothing implementation
        double alpha = 0.3; // Smoothing parameter
        List<Double> smoothed = new ArrayList<>();
        
        double s = ((BigDecimal) data.get(0).get("cost")).doubleValue();
        smoothed.add(s);
        
        for (int i = 1; i < data.size(); i++) {
            double actual = ((BigDecimal) data.get(i).get("cost")).doubleValue();
            s = alpha * actual + (1 - alpha) * s;
            smoothed.add(s);
        }
        
        // Calculate trend
        double trend = (smoothed.get(smoothed.size() - 1) - smoothed.get(0)) / smoothed.size();
        
        // Generate predictions
        List<Map<String, Object>> predictions = new ArrayList<>();
        double lastSmoothed = smoothed.get(smoothed.size() - 1);
        
        for (int i = 1; i <= request.getDaysToPredict(); i++) {
            double predictedCost = lastSmoothed + (trend * i);
            
            Map<String, Object> prediction = new HashMap<>();
            prediction.put("date", LocalDate.parse(request.getEndDate()).plusDays(i).toString());
            prediction.put("predictedCost", Math.max(0, predictedCost));
            prediction.put("lowerBound", Math.max(0, predictedCost * 0.85));
            prediction.put("upperBound", predictedCost * 1.15);
            
            predictions.add(prediction);
        }
        
        PredictionResult result = new PredictionResult();
        result.setMethod("exponential");
        result.setPredictions(predictions);
        result.setConfidence(0.8);
        result.setMetadata(Map.of("alpha", alpha, "trend", trend));
        
        return result;
    }

    private PredictionResult generateSeasonalPrediction(List<Map<String, Object>> data, PredictionRequest request) {
        // Simplified seasonal decomposition
        int seasonLength = 7; // Weekly seasonality
        List<Double> costs = data.stream()
            .map(row -> ((BigDecimal) row.get("cost")).doubleValue())
            .toList();
        
        // Calculate seasonal components
        double[] seasonal = new double[seasonLength];
        for (int i = 0; i < seasonLength; i++) {
            double sum = 0;
            int count = 0;
            for (int j = i; j < costs.size(); j += seasonLength) {
                sum += costs.get(j);
                count++;
            }
            seasonal[i] = count > 0 ? sum / count : 0;
        }
        
        // Generate predictions with seasonality
        List<Map<String, Object>> predictions = new ArrayList<>();
        double avgCost = costs.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        
        for (int i = 1; i <= request.getDaysToPredict(); i++) {
            double seasonalComponent = seasonal[i % seasonLength];
            double trendComponent = avgCost * (1 + (i * 0.001)); // Small growth trend
            double predictedCost = trendComponent + (seasonalComponent - avgCost) * 0.3;
            
            Map<String, Object> prediction = new HashMap<>();
            prediction.put("date", LocalDate.parse(request.getEndDate()).plusDays(i).toString());
            prediction.put("predictedCost", Math.max(0, predictedCost));
            prediction.put("lowerBound", Math.max(0, predictedCost * 0.8));
            prediction.put("upperBound", predictedCost * 1.2);
            
            predictions.add(prediction);
        }
        
        PredictionResult result = new PredictionResult();
        result.setMethod("seasonal");
        result.setPredictions(predictions);
        result.setConfidence(0.85);
        result.setMetadata(Map.of("seasonLength", seasonLength, "avgCost", avgCost));
        
        return result;
    }

    private PredictionResult generateGrowthPrediction(List<Map<String, Object>> data, PredictionRequest request) {
        // Growth rate based prediction
        double growthRate = 0.05; // 5% annual growth rate
        double avgCost = data.stream()
            .mapToDouble(row -> ((BigDecimal) row.get("cost")).doubleValue())
            .average().orElse(0);
        
        List<Map<String, Object>> predictions = new ArrayList<>();
        for (int i = 1; i <= request.getDaysToPredict(); i++) {
            double growthFactor = Math.pow(1 + growthRate / 365, i);
            double predictedCost = avgCost * growthFactor;
            
            Map<String, Object> prediction = new HashMap<>();
            prediction.put("date", LocalDate.parse(request.getEndDate()).plusDays(i).toString());
            prediction.put("predictedCost", Math.max(0, predictedCost));
            prediction.put("lowerBound", Math.max(0, predictedCost * 0.9));
            prediction.put("upperBound", predictedCost * 1.1);
            
            predictions.add(prediction);
        }
        
        PredictionResult result = new PredictionResult();
        result.setMethod("growth");
        result.setPredictions(predictions);
        result.setConfidence(0.75);
        result.setMetadata(Map.of("growthRate", growthRate, "avgCost", avgCost));
        
        return result;
    }

    private double calculateRSquared(List<Map<String, Object>> data, double slope, double intercept) {
        double sumY = data.stream()
            .mapToDouble(row -> ((BigDecimal) row.get("cost")).doubleValue())
            .sum();
        double meanY = sumY / data.size();
        
        double ssRes = 0, ssTot = 0;
        for (int i = 0; i < data.size(); i++) {
            double actual = ((BigDecimal) data.get(i).get("cost")).doubleValue();
            double predicted = slope * (i + 1) + intercept;
            
            ssRes += Math.pow(actual - predicted, 2);
            ssTot += Math.pow(actual - meanY, 2);
        }
        
        return 1 - (ssRes / ssTot);
    }

    private Map<String, Object> calculateTrendMetrics(List<Map<String, Object>> data) {
        Map<String, Object> metrics = new HashMap<>();
        
        if (data.size() < 2) {
            metrics.put("overall", "insufficient_data");
            metrics.put("growthRate", 0.0);
            metrics.put("volatility", 0.0);
            return metrics;
        }
        
        List<Double> costs = data.stream()
            .map(row -> ((BigDecimal) row.get("cost")).doubleValue())
            .toList();
        
        // Calculate growth rate
        double firstHalf = costs.subList(0, costs.size() / 2).stream()
            .mapToDouble(Double::doubleValue).average().orElse(0);
        double secondHalf = costs.subList(costs.size() / 2, costs.size()).stream()
            .mapToDouble(Double::doubleValue).average().orElse(0);
        
        double growthRate = firstHalf > 0 ? ((secondHalf - firstHalf) / firstHalf) * 100 : 0;
        
        // Calculate volatility
        double mean = costs.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = costs.stream()
            .mapToDouble(cost -> Math.pow(cost - mean, 2))
            .average().orElse(0);
        double volatility = Math.sqrt(variance) / mean * 100;
        
        // Determine overall trend
        String overall;
        if (Math.abs(growthRate) < 5) {
            overall = "stable";
        } else if (growthRate > 0) {
            overall = growthRate > 20 ? "rapidly_increasing" : "increasing";
        } else {
            overall = growthRate < -20 ? "rapidly_decreasing" : "decreasing";
        }
        
        metrics.put("overall", overall);
        metrics.put("growthRate", growthRate);
        metrics.put("volatility", volatility);
        
        return metrics;
    }

    private List<Object> detectTrendAnomalies(List<Map<String, Object>> data) {
        // Simplified anomaly detection for trends
        List<Object> anomalies = new ArrayList<>();
        
        if (data.size() < 7) return anomalies; // Need at least a week of data
        
        List<Double> costs = data.stream()
            .map(row -> ((BigDecimal) row.get("cost")).doubleValue())
            .toList();
        
        // Simple moving average anomaly detection
        int windowSize = 7;
        for (int i = windowSize; i < costs.size(); i++) {
            double windowAvg = costs.subList(i - windowSize, i).stream()
                .mapToDouble(Double::doubleValue).average().orElse(0);
            
            double current = costs.get(i);
            double deviation = Math.abs(current - windowAvg) / windowAvg;
            
            if (deviation > 0.3) { // 30% deviation threshold
                Map<String, Object> anomaly = new HashMap<>();
                anomaly.put("date", data.get(i).get("date"));
                anomaly.put("type", "trend_deviation");
                anomaly.put("severity", deviation);
                anomaly.put("expected", windowAvg);
                anomaly.put("actual", current);
                anomalies.add(anomaly);
            }
        }
        
        return anomalies;
    }

    private Map<String, Object> generateTrendInsights(Map<String, Object> metrics, List<Object> anomalies) {
        Map<String, Object> insights = new HashMap<>();
        
        String overall = (String) metrics.get("overall");
        double growthRate = (Double) metrics.get("growthRate");
        double volatility = (Double) metrics.get("volatility");
        
        // Generate summary
        String summary = switch (overall) {
            case "stable" -> "Costs are remaining relatively stable with minimal growth or decline";
            case "increasing" -> String.format("Costs are increasing at %.1f%% rate", growthRate);
            case "rapidly_increasing" -> String.format("Costs are rapidly increasing at %.1f%% rate - immediate attention needed", growthRate);
            case "decreasing" -> String.format("Costs are decreasing at %.1f%% rate", Math.abs(growthRate));
            case "rapidly_decreasing" -> String.format("Costs are rapidly decreasing at %.1f%% rate", Math.abs(growthRate));
            default -> "Insufficient data for trend analysis";
        };
        
        // Generate recommendation
        String recommendation;
        if (volatility > 50) {
            recommendation = "High volatility detected - investigate cost spikes and implement better forecasting";
        } else if (growthRate > 20) {
            recommendation = "Rapid cost growth - review optimization opportunities and budget adjustments";
        } else if (anomalies.size() > 5) {
            recommendation = "Multiple anomalies detected - investigate unusual spending patterns";
        } else {
            recommendation = "Cost trends are within normal parameters - continue monitoring";
        }
        
        insights.put("overall", summary);
        insights.put("recommendation", recommendation);
        insights.put("details", List.of(
            String.format("Growth rate: %.1f%%", growthRate),
            String.format("Volatility: %.1f%%", volatility),
            String.format("Anomalies detected: %d", anomalies.size())
        ));
        
        return insights;
    }

    private TeamComparison compareTeams(String startDate, String endDate) {
        String sql = """
            SELECT team_name, 
                   SUM(cost) as total_cost,
                   AVG(cost) as avg_daily_cost,
                   COUNT(DISTINCT service_name) as service_count
            FROM enhanced_usage_records 
            WHERE date BETWEEN ? AND ?
            GROUP BY team_name 
            ORDER BY total_cost DESC
            """;
        
        List<Map<String, Object>> teamData = jdbcTemplate.queryForList(sql, startDate, endDate);
        
        TeamComparison comparison = new TeamComparison();
        comparison.setComparisonType("teams");
        comparison.setAnalysisPeriod(startDate + " to " + endDate);
        comparison.setTeams(teamData);
        
        // Add rankings and efficiency metrics
        for (int i = 0; i < teamData.size(); i++) {
            Map<String, Object> team = teamData.get(i);
            team.put("rank", i + 1);
            
            BigDecimal totalCost = (BigDecimal) team.get("total_cost");
            Long serviceCount = (Long) team.get("service_count");
            double efficiency = serviceCount > 0 ? totalCost.doubleValue() / serviceCount : 0;
            team.put("efficiency", efficiency);
        }
        
        // Calculate benchmarks
        double avgCost = teamData.stream()
            .mapToDouble(team -> ((BigDecimal) team.get("total_cost")).doubleValue())
            .average().orElse(0);
        
        double avgEfficiency = teamData.stream()
            .mapToDouble(team -> (Double) team.get("efficiency"))
            .average().orElse(0);
        
        Map<String, Object> benchmarks = new HashMap<>();
        benchmarks.put("avgTotalCost", avgCost);
        benchmarks.put("avgEfficiency", avgEfficiency);
        
        comparison.setBenchmarks(benchmarks);
        
        return comparison;
    }

    private TeamComparison compareServices(String startDate, String endDate) {
        String sql = """
            SELECT service_name, 
                   SUM(cost) as total_cost,
                   AVG(cost) as avg_cost,
                   COUNT(DISTINCT team_name) as team_count,
                   COUNT(DISTINCT region) as region_count
            FROM enhanced_usage_records 
            WHERE date BETWEEN ? AND ?
            GROUP BY service_name 
            ORDER BY total_cost DESC
            """;
        
        List<Map<String, Object>> serviceData = jdbcTemplate.queryForList(sql, startDate, endDate);
        
        TeamComparison comparison = new TeamComparison();
        comparison.setComparisonType("services");
        comparison.setAnalysisPeriod(startDate + " to " + endDate);
        comparison.setTeams(serviceData); // Reusing teams field for services
        
        return comparison;
    }

    private TeamComparison compareRegions(String startDate, String endDate) {
        String sql = """
            SELECT region, 
                   SUM(cost) as total_cost,
                   COUNT(DISTINCT service_name) as service_count,
                   COUNT(DISTINCT team_name) as team_count
            FROM enhanced_usage_records 
            WHERE date BETWEEN ? AND ?
            GROUP BY region 
            ORDER BY total_cost DESC
            """;
        
        List<Map<String, Object>> regionData = jdbcTemplate.queryForList(sql, startDate, endDate);
        
        TeamComparison comparison = new TeamComparison();
        comparison.setComparisonType("regions");
        comparison.setAnalysisPeriod(startDate + " to " + endDate);
        comparison.setTeams(regionData); // Reusing teams field for regions
        
        return comparison;
    }

    private double calculateStandardDeviation(double[] values, double mean) {
        double sum = 0;
        for (double value : values) {
            sum += Math.pow(value - mean, 2);
        }
        return Math.sqrt(sum / values.length);
    }
}