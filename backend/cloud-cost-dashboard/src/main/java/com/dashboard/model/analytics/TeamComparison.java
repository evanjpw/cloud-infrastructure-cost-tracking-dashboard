package com.dashboard.model.analytics;

import java.util.List;
import java.util.Map;

public class TeamComparison {
    private String comparisonType; // teams, services, regions
    private String analysisPeriod;
    private List<Map<String, Object>> teams;
    private Map<String, Object> benchmarks;
    private int totalEntities;
    private String analysisDate;

    // Constructors
    public TeamComparison() {
        this.analysisDate = java.time.Instant.now().toString();
    }

    // Getters and Setters
    public String getComparisonType() { return comparisonType; }
    public void setComparisonType(String comparisonType) { this.comparisonType = comparisonType; }

    public String getAnalysisPeriod() { return analysisPeriod; }
    public void setAnalysisPeriod(String analysisPeriod) { this.analysisPeriod = analysisPeriod; }

    public List<Map<String, Object>> getTeams() { return teams; }
    public void setTeams(List<Map<String, Object>> teams) { 
        this.teams = teams;
        this.totalEntities = teams != null ? teams.size() : 0;
    }

    public Map<String, Object> getBenchmarks() { return benchmarks; }
    public void setBenchmarks(Map<String, Object> benchmarks) { this.benchmarks = benchmarks; }

    public int getTotalEntities() { return totalEntities; }
    public void setTotalEntities(int totalEntities) { this.totalEntities = totalEntities; }

    public String getAnalysisDate() { return analysisDate; }
    public void setAnalysisDate(String analysisDate) { this.analysisDate = analysisDate; }
}