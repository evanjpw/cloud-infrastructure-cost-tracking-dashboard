package com.dashboard.model.optimization;

import java.util.List;

public class OptimizationRecommendation {
    private String id;
    private String title;
    private String description;
    private String type; // rightsizing, reserved_instance, unused_resource, etc.
    private String impact; // low, medium, high
    private String priority; // low, medium, high, critical
    private double potentialSavings;
    private String implementationEffort; // low, medium, high
    private String riskLevel; // low, medium, high
    private List<String> affectedServices;
    private List<String> affectedTeams;
    private List<String> implementationSteps;
    private String status; // pending, accepted, rejected, implemented, deferred
    private String statusNotes;
    private String createdAt;
    private String updatedAt;

    // Constructors
    public OptimizationRecommendation() {}

    public OptimizationRecommendation(String title, String description, String type, String impact) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.impact = impact;
        this.status = "pending";
        this.createdAt = java.time.Instant.now().toString();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getImpact() { return impact; }
    public void setImpact(String impact) { this.impact = impact; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public double getPotentialSavings() { return potentialSavings; }
    public void setPotentialSavings(double potentialSavings) { this.potentialSavings = potentialSavings; }

    public String getImplementationEffort() { return implementationEffort; }
    public void setImplementationEffort(String implementationEffort) { this.implementationEffort = implementationEffort; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public List<String> getAffectedServices() { return affectedServices; }
    public void setAffectedServices(List<String> affectedServices) { this.affectedServices = affectedServices; }

    public List<String> getAffectedTeams() { return affectedTeams; }
    public void setAffectedTeams(List<String> affectedTeams) { this.affectedTeams = affectedTeams; }

    public List<String> getImplementationSteps() { return implementationSteps; }
    public void setImplementationSteps(List<String> implementationSteps) { this.implementationSteps = implementationSteps; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusNotes() { return statusNotes; }
    public void setStatusNotes(String statusNotes) { this.statusNotes = statusNotes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}