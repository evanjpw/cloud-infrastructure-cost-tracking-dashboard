package com.dashboard.dto.education;

import java.util.Map;

public class CompleteAttemptRequest {
    private Double actualSavingsAchieved;
    private Map<String, String> performanceMetrics;
    private Double implementationQuality;
    private Double bestPracticesScore;
    
    public Double getActualSavingsAchieved() { return actualSavingsAchieved; }
    public void setActualSavingsAchieved(Double actualSavingsAchieved) { this.actualSavingsAchieved = actualSavingsAchieved; }
    public Map<String, String> getPerformanceMetrics() { return performanceMetrics; }
    public void setPerformanceMetrics(Map<String, String> performanceMetrics) { this.performanceMetrics = performanceMetrics; }
    public Double getImplementationQuality() { return implementationQuality; }
    public void setImplementationQuality(Double implementationQuality) { this.implementationQuality = implementationQuality; }
    public Double getBestPracticesScore() { return bestPracticesScore; }
    public void setBestPracticesScore(Double bestPracticesScore) { this.bestPracticesScore = bestPracticesScore; }
}