package com.dashboard.model.scenario;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ScenarioComparison {
    private String id;
    private List<String> scenarioIds;
    private List<ScenarioSummary> scenarios;
    private ScenarioSummary bestScenario;
    private ScenarioSummary worstScenario;
    private Map<String, Object> comparisonMetrics;
    private List<String> recommendations;
    private String analysisMethod; // cost_optimization, performance_balance, risk_adjusted
    private String createdAt;
    private String createdBy;

    // Constructors
    public ScenarioComparison() {
        this.createdAt = java.time.Instant.now().toString();
    }

    public ScenarioComparison(List<String> scenarioIds, String analysisMethod) {
        this();
        this.scenarioIds = scenarioIds;
        this.analysisMethod = analysisMethod;
    }

    // Nested class for scenario summary in comparisons
    public static class ScenarioSummary {
        private String id;
        private String name;
        private String type;
        private BigDecimal totalCost;
        private BigDecimal costChange;
        private double percentageChange;
        private String riskLevel;
        private double riskScore;
        private int implementationComplexity; // 1-10 scale
        private int timeToImplementDays;
        private Map<String, Object> keyMetrics;

        public ScenarioSummary() {}

        public ScenarioSummary(String id, String name, String type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public BigDecimal getTotalCost() { return totalCost; }
        public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

        public BigDecimal getCostChange() { return costChange; }
        public void setCostChange(BigDecimal costChange) { this.costChange = costChange; }

        public double getPercentageChange() { return percentageChange; }
        public void setPercentageChange(double percentageChange) { this.percentageChange = percentageChange; }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

        public double getRiskScore() { return riskScore; }
        public void setRiskScore(double riskScore) { this.riskScore = riskScore; }

        public int getImplementationComplexity() { return implementationComplexity; }
        public void setImplementationComplexity(int implementationComplexity) { this.implementationComplexity = implementationComplexity; }

        public int getTimeToImplementDays() { return timeToImplementDays; }
        public void setTimeToImplementDays(int timeToImplementDays) { this.timeToImplementDays = timeToImplementDays; }

        public Map<String, Object> getKeyMetrics() { return keyMetrics; }
        public void setKeyMetrics(Map<String, Object> keyMetrics) { this.keyMetrics = keyMetrics; }

        // Helper methods
        public boolean hasSavings() {
            return costChange != null && costChange.compareTo(BigDecimal.ZERO) < 0;
        }

        public boolean isLowRisk() {
            return "low".equals(riskLevel);
        }

        public boolean isQuickWin() {
            return hasSavings() && isLowRisk() && implementationComplexity <= 3;
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<String> getScenarioIds() { return scenarioIds; }
    public void setScenarioIds(List<String> scenarioIds) { this.scenarioIds = scenarioIds; }

    public List<ScenarioSummary> getScenarios() { return scenarios; }
    public void setScenarios(List<ScenarioSummary> scenarios) { this.scenarios = scenarios; }

    public ScenarioSummary getBestScenario() { return bestScenario; }
    public void setBestScenario(ScenarioSummary bestScenario) { this.bestScenario = bestScenario; }

    public ScenarioSummary getWorstScenario() { return worstScenario; }
    public void setWorstScenario(ScenarioSummary worstScenario) { this.worstScenario = worstScenario; }

    public Map<String, Object> getComparisonMetrics() { return comparisonMetrics; }
    public void setComparisonMetrics(Map<String, Object> comparisonMetrics) { this.comparisonMetrics = comparisonMetrics; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

    public String getAnalysisMethod() { return analysisMethod; }
    public void setAnalysisMethod(String analysisMethod) { this.analysisMethod = analysisMethod; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    // Helper methods
    public BigDecimal getTotalPotentialSavings() {
        if (scenarios == null || scenarios.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return scenarios.stream()
                .filter(ScenarioSummary::hasSavings)
                .map(ScenarioSummary::getCostChange)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getQuickWinCount() {
        if (scenarios == null) {
            return 0;
        }
        
        return (int) scenarios.stream()
                .filter(ScenarioSummary::isQuickWin)
                .count();
    }

    public double getAverageRiskScore() {
        if (scenarios == null || scenarios.isEmpty()) {
            return 0.0;
        }
        
        return scenarios.stream()
                .mapToDouble(ScenarioSummary::getRiskScore)
                .average()
                .orElse(0.0);
    }
}