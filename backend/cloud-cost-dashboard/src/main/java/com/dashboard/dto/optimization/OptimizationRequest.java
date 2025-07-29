package com.dashboard.dto.optimization;

import java.util.List;

public class OptimizationRequest {
    private String scope; // all, team:name, service:name
    private String startDate;
    private String endDate;
    private List<String> includeTypes; // Filter to specific recommendation types
    private String minImpact; // low, medium, high - minimum impact level
    private String maxRisk; // low, medium, high - maximum acceptable risk
    private boolean includeImplemented = false; // Include already implemented recommendations
    private int maxRecommendations = 50; // Limit number of recommendations

    // Constructors
    public OptimizationRequest() {}

    public OptimizationRequest(String scope, String startDate, String endDate) {
        this.scope = scope;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public List<String> getIncludeTypes() { return includeTypes; }
    public void setIncludeTypes(List<String> includeTypes) { this.includeTypes = includeTypes; }

    public String getMinImpact() { return minImpact; }
    public void setMinImpact(String minImpact) { this.minImpact = minImpact; }

    public String getMaxRisk() { return maxRisk; }
    public void setMaxRisk(String maxRisk) { this.maxRisk = maxRisk; }

    public boolean isIncludeImplemented() { return includeImplemented; }
    public void setIncludeImplemented(boolean includeImplemented) { this.includeImplemented = includeImplemented; }

    public int getMaxRecommendations() { return maxRecommendations; }
    public void setMaxRecommendations(int maxRecommendations) { this.maxRecommendations = maxRecommendations; }
}