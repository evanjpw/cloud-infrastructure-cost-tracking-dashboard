package com.dashboard.dto.analytics;

import java.util.List;

public class ComparisonRequest {
    private String comparisonType; // teams, services, regions, periods
    private String startDate;
    private String endDate;
    private String metric = "total_cost"; // total_cost, efficiency, trend
    private List<String> includeEntities; // Optional filter for specific entities
    private List<String> excludeEntities; // Optional filter to exclude entities
    private boolean includeEfficiencyMetrics = true;

    // Constructors
    public ComparisonRequest() {}

    public ComparisonRequest(String comparisonType, String startDate, String endDate) {
        this.comparisonType = comparisonType;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public String getComparisonType() { return comparisonType; }
    public void setComparisonType(String comparisonType) { this.comparisonType = comparisonType; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }

    public List<String> getIncludeEntities() { return includeEntities; }
    public void setIncludeEntities(List<String> includeEntities) { this.includeEntities = includeEntities; }

    public List<String> getExcludeEntities() { return excludeEntities; }
    public void setExcludeEntities(List<String> excludeEntities) { this.excludeEntities = excludeEntities; }

    public boolean isIncludeEfficiencyMetrics() { return includeEfficiencyMetrics; }
    public void setIncludeEfficiencyMetrics(boolean includeEfficiencyMetrics) { this.includeEfficiencyMetrics = includeEfficiencyMetrics; }
}