package com.dashboard.model.optimization;

import java.util.Map;

public class OptimizationSummary {
    private String teamName;
    private String analysisPeriod;
    private double totalPotentialSavings;
    private int recommendationCount;
    private int highImpactCount;
    private int implementedCount;
    private double savingsPercentage;
    private Map<String, Object> typeBreakdown;
    private String generatedAt;

    // Constructors
    public OptimizationSummary() {
        this.generatedAt = java.time.Instant.now().toString();
    }

    public OptimizationSummary(String teamName, String analysisPeriod) {
        this();
        this.teamName = teamName;
        this.analysisPeriod = analysisPeriod;
    }

    // Getters and Setters
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getAnalysisPeriod() { return analysisPeriod; }
    public void setAnalysisPeriod(String analysisPeriod) { this.analysisPeriod = analysisPeriod; }

    public double getTotalPotentialSavings() { return totalPotentialSavings; }
    public void setTotalPotentialSavings(double totalPotentialSavings) { this.totalPotentialSavings = totalPotentialSavings; }

    public int getRecommendationCount() { return recommendationCount; }
    public void setRecommendationCount(int recommendationCount) { this.recommendationCount = recommendationCount; }

    public int getHighImpactCount() { return highImpactCount; }
    public void setHighImpactCount(int highImpactCount) { this.highImpactCount = highImpactCount; }

    public int getImplementedCount() { return implementedCount; }
    public void setImplementedCount(int implementedCount) { this.implementedCount = implementedCount; }

    public double getSavingsPercentage() { return savingsPercentage; }
    public void setSavingsPercentage(double savingsPercentage) { this.savingsPercentage = savingsPercentage; }

    public Map<String, Object> getTypeBreakdown() { return typeBreakdown; }
    public void setTypeBreakdown(Map<String, Object> typeBreakdown) { this.typeBreakdown = typeBreakdown; }

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
}