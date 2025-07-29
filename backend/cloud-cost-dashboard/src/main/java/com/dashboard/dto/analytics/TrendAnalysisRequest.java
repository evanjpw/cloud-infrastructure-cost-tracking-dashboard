package com.dashboard.dto.analytics;

public class TrendAnalysisRequest {
    private String teamName;
    private String startDate;
    private String endDate;
    private int windowSize = 7; // Default to weekly analysis
    private boolean includeAnomalyDetection = true;

    // Constructors
    public TrendAnalysisRequest() {}

    public TrendAnalysisRequest(String teamName, String startDate, String endDate) {
        this.teamName = teamName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public int getWindowSize() { return windowSize; }
    public void setWindowSize(int windowSize) { this.windowSize = windowSize; }

    public boolean isIncludeAnomalyDetection() { return includeAnomalyDetection; }
    public void setIncludeAnomalyDetection(boolean includeAnomalyDetection) { this.includeAnomalyDetection = includeAnomalyDetection; }
}