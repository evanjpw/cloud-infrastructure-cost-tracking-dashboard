package com.dashboard.dto.budget;

import java.math.BigDecimal;

public class CreateBudgetRequest {
    private String name;
    private BigDecimal amount;
    private String period; // monthly, quarterly, yearly
    private String scope; // team, service, organization
    private String target; // team name, service name, or "all"
    private double alertThreshold; // percentage threshold for alerts (default 80%)
    private String startDate;
    private String endDate;
    private String createdBy;

    // Constructors
    public CreateBudgetRequest() {
        this.alertThreshold = 80.0; // Default alert threshold
    }

    public CreateBudgetRequest(String name, BigDecimal amount, String period, String scope, String target) {
        this();
        this.name = name;
        this.amount = amount;
        this.period = period;
        this.scope = scope;
        this.target = target;
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

    public double getAlertThreshold() { return alertThreshold; }
    public void setAlertThreshold(double alertThreshold) { this.alertThreshold = alertThreshold; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    // Validation helper methods
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               amount != null && amount.compareTo(BigDecimal.ZERO) > 0 &&
               period != null && isValidPeriod(period) &&
               scope != null && isValidScope(scope) &&
               target != null && !target.trim().isEmpty() &&
               alertThreshold > 0 && alertThreshold <= 100;
    }

    private boolean isValidPeriod(String period) {
        return "monthly".equals(period) || "quarterly".equals(period) || "yearly".equals(period);
    }

    private boolean isValidScope(String scope) {
        return "team".equals(scope) || "service".equals(scope) || "organization".equals(scope);
    }

    @Override
    public String toString() {
        return "CreateBudgetRequest{" +
                "name='" + name + '\'' +
                ", amount=" + amount +
                ", period='" + period + '\'' +
                ", scope='" + scope + '\'' +
                ", target='" + target + '\'' +
                ", alertThreshold=" + alertThreshold +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}';
    }
}