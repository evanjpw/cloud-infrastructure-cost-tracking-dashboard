package com.dashboard.model.scenario;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class WhatIfScenario {
    private String id;
    private String name;
    private String description;
    private String type; // cost_optimization, infrastructure_change, workload_migration, etc.
    private String status; // created, running, completed, failed
    private Map<String, Object> parameters; // Scenario-specific configuration
    private Map<String, Object> baselineData; // Original data for comparison
    private Map<String, Object> projectedData; // What-if projections
    private ScenarioImpact impact;
    private RiskAssessment riskAssessment;
    private List<String> implementationSteps;
    private String difficultyLevel; // beginner, intermediate, advanced
    private int timeHorizonDays;
    private String createdAt;
    private String updatedAt;
    private String createdBy;

    // Constructors
    public WhatIfScenario() {
        this.status = "created";
        this.createdAt = java.time.Instant.now().toString();
    }

    public WhatIfScenario(String name, String description, String type) {
        this();
        this.name = name;
        this.description = description;
        this.type = type;
    }

    // Nested classes for complex data structures
    public static class ScenarioImpact {
        private BigDecimal totalCostDifference;
        private double totalPercentageChange;
        private BigDecimal averageDailySavings;
        private Map<String, BigDecimal> serviceImpacts;
        private Map<String, Double> performanceImpacts;
        private List<String> benefitsAndRisks;

        public ScenarioImpact() {}

        public BigDecimal getTotalCostDifference() { return totalCostDifference; }
        public void setTotalCostDifference(BigDecimal totalCostDifference) { this.totalCostDifference = totalCostDifference; }

        public double getTotalPercentageChange() { return totalPercentageChange; }
        public void setTotalPercentageChange(double totalPercentageChange) { this.totalPercentageChange = totalPercentageChange; }

        public BigDecimal getAverageDailySavings() { return averageDailySavings; }
        public void setAverageDailySavings(BigDecimal averageDailySavings) { this.averageDailySavings = averageDailySavings; }

        public Map<String, BigDecimal> getServiceImpacts() { return serviceImpacts; }
        public void setServiceImpacts(Map<String, BigDecimal> serviceImpacts) { this.serviceImpacts = serviceImpacts; }

        public Map<String, Double> getPerformanceImpacts() { return performanceImpacts; }
        public void setPerformanceImpacts(Map<String, Double> performanceImpacts) { this.performanceImpacts = performanceImpacts; }

        public List<String> getBenefitsAndRisks() { return benefitsAndRisks; }
        public void setBenefitsAndRisks(List<String> benefitsAndRisks) { this.benefitsAndRisks = benefitsAndRisks; }
    }

    public static class RiskAssessment {
        private String level; // low, medium, high, critical
        private List<String> factors;
        private Map<String, String> mitigationStrategies;
        private double confidenceScore; // 0.0 to 1.0

        public RiskAssessment() {}

        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }

        public List<String> getFactors() { return factors; }
        public void setFactors(List<String> factors) { this.factors = factors; }

        public Map<String, String> getMitigationStrategies() { return mitigationStrategies; }
        public void setMitigationStrategies(Map<String, String> mitigationStrategies) { this.mitigationStrategies = mitigationStrategies; }

        public double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public Map<String, Object> getBaselineData() { return baselineData; }
    public void setBaselineData(Map<String, Object> baselineData) { this.baselineData = baselineData; }

    public Map<String, Object> getProjectedData() { return projectedData; }
    public void setProjectedData(Map<String, Object> projectedData) { this.projectedData = projectedData; }

    public ScenarioImpact getImpact() { return impact; }
    public void setImpact(ScenarioImpact impact) { this.impact = impact; }

    public RiskAssessment getRiskAssessment() { return riskAssessment; }
    public void setRiskAssessment(RiskAssessment riskAssessment) { this.riskAssessment = riskAssessment; }

    public List<String> getImplementationSteps() { return implementationSteps; }
    public void setImplementationSteps(List<String> implementationSteps) { this.implementationSteps = implementationSteps; }

    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public int getTimeHorizonDays() { return timeHorizonDays; }
    public void setTimeHorizonDays(int timeHorizonDays) { this.timeHorizonDays = timeHorizonDays; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    // Helper methods
    public boolean isCompleted() {
        return "completed".equals(status);
    }

    public boolean hasSavings() {
        return impact != null && impact.getTotalCostDifference() != null 
               && impact.getTotalCostDifference().compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isHighRisk() {
        return riskAssessment != null && ("high".equals(riskAssessment.getLevel()) 
                                         || "critical".equals(riskAssessment.getLevel()));
    }
}