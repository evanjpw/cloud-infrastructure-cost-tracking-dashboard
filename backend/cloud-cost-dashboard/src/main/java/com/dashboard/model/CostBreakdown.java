package com.dashboard.model;

public class CostBreakdown {
    private String teamName;
    private String service;
    private double totalCost;

    public CostBreakdown() {}

    public CostBreakdown(String teamName, String service, double totalCost) {
        this.teamName = teamName;
        this.service = service;
        this.totalCost = totalCost;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getService() {
        return service;
    }

    public double getTotalCost() {
        return totalCost;
    }
}
