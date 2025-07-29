package com.dashboard.dto.analytics;

import java.util.Map;

public class PredictionRequest {
    private String method; // linear, exponential, seasonal, growth
    private int daysToPredict;
    private boolean includeSeasonality;
    private double confidenceLevel;
    private String teamName;
    private String startDate;
    private String endDate;
    private Map<String, Object> options;

    // Constructors
    public PredictionRequest() {}

    public PredictionRequest(String method, int daysToPredict, String teamName, String startDate, String endDate) {
        this.method = method;
        this.daysToPredict = daysToPredict;
        this.teamName = teamName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.includeSeasonality = true;
        this.confidenceLevel = 0.95;
    }

    // Getters and Setters
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public int getDaysToPredict() { return daysToPredict; }
    public void setDaysToPredict(int daysToPredict) { this.daysToPredict = daysToPredict; }

    public boolean isIncludeSeasonality() { return includeSeasonality; }
    public void setIncludeSeasonality(boolean includeSeasonality) { this.includeSeasonality = includeSeasonality; }

    public double getConfidenceLevel() { return confidenceLevel; }
    public void setConfidenceLevel(double confidenceLevel) { this.confidenceLevel = confidenceLevel; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public Map<String, Object> getOptions() { return options; }
    public void setOptions(Map<String, Object> options) { this.options = options; }
}