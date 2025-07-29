package com.dashboard.dto.scenario;

import java.util.Map;
import java.util.List;

public class CreateScenarioRequest {
    private String name;
    private String description;
    private String type; // cost_optimization, infrastructure_change, workload_migration, scaling, etc.
    private Map<String, Object> parameters; // Scenario-specific configuration
    private String scope; // team, service, organization, region
    private String target; // specific target (team name, service name, etc.)
    private int timeHorizonDays; // Analysis period
    private String difficultyLevel; // beginner, intermediate, advanced
    private List<String> affectedServices; // Services that will be impacted
    private String createdBy;

    // Constructors
    public CreateScenarioRequest() {
        this.timeHorizonDays = 30; // Default 30-day analysis
    }

    public CreateScenarioRequest(String name, String description, String type) {
        this();
        this.name = name;
        this.description = description;
        this.type = type;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public int getTimeHorizonDays() { return timeHorizonDays; }
    public void setTimeHorizonDays(int timeHorizonDays) { this.timeHorizonDays = timeHorizonDays; }

    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public List<String> getAffectedServices() { return affectedServices; }
    public void setAffectedServices(List<String> affectedServices) { this.affectedServices = affectedServices; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    // Validation methods
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               description != null && !description.trim().isEmpty() &&
               type != null && isValidType(type) &&
               timeHorizonDays > 0 && timeHorizonDays <= 365 &&
               (difficultyLevel == null || isValidDifficultyLevel(difficultyLevel)) &&
               (scope == null || isValidScope(scope));
    }

    private boolean isValidType(String type) {
        return "cost_optimization".equals(type) ||
               "infrastructure_change".equals(type) ||
               "workload_migration".equals(type) ||
               "scaling".equals(type) ||
               "rightsizing".equals(type) ||
               "reserved_instances".equals(type) ||
               "spot_instances".equals(type) ||
               "storage_optimization".equals(type) ||
               "network_optimization".equals(type);
    }

    private boolean isValidDifficultyLevel(String level) {
        return "beginner".equals(level) || "intermediate".equals(level) || "advanced".equals(level);
    }

    private boolean isValidScope(String scope) {
        return "team".equals(scope) || "service".equals(scope) || 
               "organization".equals(scope) || "region".equals(scope);
    }

    // Helper methods for parameter validation
    public Object getParameter(String key) {
        return parameters != null ? parameters.get(key) : null;
    }

    public void setParameter(String key, Object value) {
        if (parameters == null) {
            parameters = new java.util.HashMap<>();
        }
        parameters.put(key, value);
    }

    // Common parameter getters with type safety
    public Double getParameterAsDouble(String key) {
        Object value = getParameter(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    public Integer getParameterAsInteger(String key) {
        Object value = getParameter(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    public String getParameterAsString(String key) {
        Object value = getParameter(key);
        return value != null ? value.toString() : null;
    }

    @Override
    public String toString() {
        return "CreateScenarioRequest{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", scope='" + scope + '\'' +
                ", target='" + target + '\'' +
                ", timeHorizonDays=" + timeHorizonDays +
                ", difficultyLevel='" + difficultyLevel + '\'' +
                ", parametersCount=" + (parameters != null ? parameters.size() : 0) +
                '}';
    }
}