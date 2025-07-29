package com.dashboard.model.budget;

import java.math.BigDecimal;
import java.util.List;

public class Budget {
    private String id;
    private String name;
    private BigDecimal amount;
    private String period; // monthly, quarterly, yearly
    private String scope; // team, service, organization
    private String target; // team name, service name, or "all"
    private double alertThreshold; // percentage threshold for alerts
    private String startDate;
    private String endDate;
    private BigDecimal currentSpend;
    private double utilizationPercentage;
    private int daysRemaining;
    private String status; // active, inactive, exceeded
    private List<String> alertHistory;
    private String createdAt;
    private String updatedAt;
    private String createdBy;

    // Constructors
    public Budget() {
        this.status = "active";
        this.createdAt = java.time.Instant.now().toString();
    }

    public Budget(String name, BigDecimal amount, String period, String scope, String target) {
        this();
        this.name = name;
        this.amount = amount;
        this.period = period;
        this.scope = scope;
        this.target = target;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public double getAlertThreshold() { return alertThreshold; }
    public void setAlertThreshold(double alertThreshold) { this.alertThreshold = alertThreshold; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public BigDecimal getCurrentSpend() { return currentSpend; }
    public void setCurrentSpend(BigDecimal currentSpend) { 
        this.currentSpend = currentSpend;
        updateUtilizationPercentage();
    }

    public double getUtilizationPercentage() { return utilizationPercentage; }
    public void setUtilizationPercentage(double utilizationPercentage) { this.utilizationPercentage = utilizationPercentage; }

    public int getDaysRemaining() { return daysRemaining; }
    public void setDaysRemaining(int daysRemaining) { this.daysRemaining = daysRemaining; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getAlertHistory() { return alertHistory; }
    public void setAlertHistory(List<String> alertHistory) { this.alertHistory = alertHistory; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    // Helper methods
    private void updateUtilizationPercentage() {
        if (amount != null && currentSpend != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.utilizationPercentage = currentSpend.divide(amount, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
        }
    }

    public boolean isOverThreshold() {
        return utilizationPercentage >= alertThreshold;
    }

    public boolean isExceeded() {
        return utilizationPercentage >= 100.0;
    }

    public BigDecimal getRemainingBudget() {
        if (amount != null && currentSpend != null) {
            return amount.subtract(currentSpend);
        }
        return BigDecimal.ZERO;
    }
}