package com.dashboard.model.analytics;

import java.util.List;
import java.util.Map;

public class TrendAnalysis {
    private String analysisPeriod;
    private int dataPoints;
    private String overallTrend;
    private double growthRate;
    private double volatility;
    private List<Object> anomalies;
    private Map<String, Object> summary;

    // Constructors
    public TrendAnalysis() {}

    // Getters and Setters
    public String getAnalysisPeriod() { return analysisPeriod; }
    public void setAnalysisPeriod(String analysisPeriod) { this.analysisPeriod = analysisPeriod; }

    public int getDataPoints() { return dataPoints; }
    public void setDataPoints(int dataPoints) { this.dataPoints = dataPoints; }

    public String getOverallTrend() { return overallTrend; }
    public void setOverallTrend(String overallTrend) { this.overallTrend = overallTrend; }

    public double getGrowthRate() { return growthRate; }
    public void setGrowthRate(double growthRate) { this.growthRate = growthRate; }

    public double getVolatility() { return volatility; }
    public void setVolatility(double volatility) { this.volatility = volatility; }

    public List<Object> getAnomalies() { return anomalies; }
    public void setAnomalies(List<Object> anomalies) { this.anomalies = anomalies; }

    public Map<String, Object> getSummary() { return summary; }
    public void setSummary(Map<String, Object> summary) { this.summary = summary; }
}