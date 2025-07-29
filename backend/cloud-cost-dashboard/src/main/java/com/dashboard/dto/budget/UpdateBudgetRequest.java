package com.dashboard.dto.budget;

import java.math.BigDecimal;

public class UpdateBudgetRequest {
    private String name;
    private BigDecimal amount;
    private String period; // monthly, quarterly, yearly
    private String scope; // team, service, organization
    private String target; // team name, service name, or "all"
    private Double alertThreshold; // percentage threshold for alerts - nullable for partial updates
    private String startDate;
    private String endDate;
    private String status; // active, inactive

    // Constructors
    public UpdateBudgetRequest() {}

    public UpdateBudgetRequest(String name, BigDecimal amount, String period) {
        this.name = name;
        this.amount = amount;
        this.period = period;
    }

    // Getters and Setters
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

    public Double getAlertThreshold() { return alertThreshold; }
    public void setAlertThreshold(Double alertThreshold) { this.alertThreshold = alertThreshold; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Validation helper methods
    public boolean hasValidAmount() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasValidPeriod() {
        return period == null || isValidPeriod(period);
    }

    public boolean hasValidScope() {
        return scope == null || isValidScope(scope);
    }

    public boolean hasValidAlertThreshold() {
        return alertThreshold == null || (alertThreshold > 0 && alertThreshold <= 100);
    }

    public boolean hasValidStatus() {
        return status == null || "active".equals(status) || "inactive".equals(status);
    }

    private boolean isValidPeriod(String period) {
        return "monthly".equals(period) || "quarterly".equals(period) || "yearly".equals(period);
    }

    private boolean isValidScope(String scope) {
        return "team".equals(scope) || "service".equals(scope) || "organization".equals(scope);
    }

    public boolean isValid() {
        return hasValidAmount() && hasValidPeriod() && hasValidScope() && 
               hasValidAlertThreshold() && hasValidStatus() &&
               (name == null || !name.trim().isEmpty()) &&
               (target == null || !target.trim().isEmpty());
    }

    @Override
    public String toString() {
        return "UpdateBudgetRequest{" +
                "name='" + name + '\'' +
                ", amount=" + amount +
                ", period='" + period + '\'' +
                ", scope='" + scope + '\'' +
                ", target='" + target + '\'' +
                ", alertThreshold=" + alertThreshold +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}