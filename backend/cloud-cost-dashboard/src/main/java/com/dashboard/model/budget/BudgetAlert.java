package com.dashboard.model.budget;

import java.math.BigDecimal;

public class BudgetAlert {
    private String id;
    private String budgetId;
    private String budgetName;
    private String severity; // low, medium, high, critical
    private String type; // threshold_exceeded, budget_exceeded, forecast_exceeded
    private String message;
    private BigDecimal triggerAmount;
    private double triggerPercentage;
    private String triggerDate;
    private String status; // active, acknowledged, resolved
    private String createdAt;
    private String acknowledgedAt;
    private String acknowledgedBy;

    // Constructors
    public BudgetAlert() {
        this.status = "active";
        this.createdAt = java.time.Instant.now().toString();
    }

    public BudgetAlert(String budgetId, String budgetName, String severity, String type, String message) {
        this();
        this.budgetId = budgetId;
        this.budgetName = budgetName;
        this.severity = severity;
        this.type = type;
        this.message = message;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBudgetId() { return budgetId; }
    public void setBudgetId(String budgetId) { this.budgetId = budgetId; }

    public String getBudgetName() { return budgetName; }
    public void setBudgetName(String budgetName) { this.budgetName = budgetName; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public BigDecimal getTriggerAmount() { return triggerAmount; }
    public void setTriggerAmount(BigDecimal triggerAmount) { this.triggerAmount = triggerAmount; }

    public double getTriggerPercentage() { return triggerPercentage; }
    public void setTriggerPercentage(double triggerPercentage) { this.triggerPercentage = triggerPercentage; }

    public String getTriggerDate() { return triggerDate; }
    public void setTriggerDate(String triggerDate) { this.triggerDate = triggerDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(String acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }

    public String getAcknowledgedBy() { return acknowledgedBy; }
    public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }

    // Helper methods
    public boolean isActive() {
        return "active".equals(status);
    }

    public boolean isCritical() {
        return "critical".equals(severity);
    }

    public void acknowledge(String acknowledgedBy) {
        this.status = "acknowledged";
        this.acknowledgedBy = acknowledgedBy;
        this.acknowledgedAt = java.time.Instant.now().toString();
    }
}