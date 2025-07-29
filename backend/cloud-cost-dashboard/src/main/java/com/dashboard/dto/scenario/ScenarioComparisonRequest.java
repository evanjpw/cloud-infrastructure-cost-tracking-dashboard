package com.dashboard.dto.scenario;

import java.util.List;

public class ScenarioComparisonRequest {
    private List<String> scenarioIds;
    private String analysisMethod; // cost_optimization, performance_balance, risk_adjusted, comprehensive
    private List<String> comparisonCriteria; // cost, risk, implementation_time, complexity, etc.
    private String priorityWeighting; // cost_focused, balanced, risk_averse
    private boolean includeRecommendations; // Whether to include actionable recommendations
    private String createdBy;

    // Constructors
    public ScenarioComparisonRequest() {
        this.analysisMethod = "cost_optimization"; // Default analysis method
        this.includeRecommendations = true; // Default to including recommendations
    }

    public ScenarioComparisonRequest(List<String> scenarioIds) {
        this();
        this.scenarioIds = scenarioIds;
    }

    public ScenarioComparisonRequest(List<String> scenarioIds, String analysisMethod) {
        this();
        this.scenarioIds = scenarioIds;
        this.analysisMethod = analysisMethod;
    }

    // Getters and Setters
    public List<String> getScenarioIds() { return scenarioIds; }
    public void setScenarioIds(List<String> scenarioIds) { this.scenarioIds = scenarioIds; }

    public String getAnalysisMethod() { return analysisMethod; }
    public void setAnalysisMethod(String analysisMethod) { this.analysisMethod = analysisMethod; }

    public List<String> getComparisonCriteria() { return comparisonCriteria; }
    public void setComparisonCriteria(List<String> comparisonCriteria) { this.comparisonCriteria = comparisonCriteria; }

    public String getPriorityWeighting() { return priorityWeighting; }
    public void setPriorityWeighting(String priorityWeighting) { this.priorityWeighting = priorityWeighting; }

    public boolean isIncludeRecommendations() { return includeRecommendations; }
    public void setIncludeRecommendations(boolean includeRecommendations) { this.includeRecommendations = includeRecommendations; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    // Validation methods
    public boolean isValid() {
        return scenarioIds != null && 
               scenarioIds.size() >= 2 && 
               scenarioIds.size() <= 10 && // Reasonable limit for comparison
               scenarioIds.stream().allMatch(id -> id != null && !id.trim().isEmpty()) &&
               (analysisMethod == null || isValidAnalysisMethod(analysisMethod)) &&
               (priorityWeighting == null || isValidPriorityWeighting(priorityWeighting));
    }

    private boolean isValidAnalysisMethod(String method) {
        return "cost_optimization".equals(method) ||
               "performance_balance".equals(method) ||
               "risk_adjusted".equals(method) ||
               "comprehensive".equals(method);
    }

    private boolean isValidPriorityWeighting(String weighting) {
        return "cost_focused".equals(weighting) ||
               "balanced".equals(weighting) ||
               "risk_averse".equals(weighting);
    }

    // Helper methods
    public int getScenarioCount() {
        return scenarioIds != null ? scenarioIds.size() : 0;
    }

    public boolean isCostFocused() {
        return "cost_focused".equals(priorityWeighting) || "cost_optimization".equals(analysisMethod);
    }

    public boolean isRiskAverse() {
        return "risk_averse".equals(priorityWeighting) || "risk_adjusted".equals(analysisMethod);
    }

    public boolean hasComparisonCriteria() {
        return comparisonCriteria != null && !comparisonCriteria.isEmpty();
    }

    // Default comparison criteria based on analysis method
    public List<String> getEffectiveComparisonCriteria() {
        if (comparisonCriteria != null && !comparisonCriteria.isEmpty()) {
            return comparisonCriteria;
        }
        
        // Return default criteria based on analysis method
        return switch (analysisMethod) {
            case "cost_optimization" -> List.of("cost", "savings_potential", "payback_period");
            case "performance_balance" -> List.of("cost", "performance_impact", "user_experience");
            case "risk_adjusted" -> List.of("risk_level", "cost", "implementation_complexity");
            case "comprehensive" -> List.of("cost", "risk_level", "performance_impact", 
                                          "implementation_complexity", "time_to_implement");
            default -> List.of("cost", "risk_level");
        };
    }

    @Override
    public String toString() {
        return "ScenarioComparisonRequest{" +
                "scenarioCount=" + getScenarioCount() +
                ", analysisMethod='" + analysisMethod + '\'' +
                ", priorityWeighting='" + priorityWeighting + '\'' +
                ", includeRecommendations=" + includeRecommendations +
                ", hasCriteria=" + hasComparisonCriteria() +
                '}';
    }
}