package com.dashboard.dto.report;

import java.util.List;
import java.util.Map;

public class GenerateReportRequest {
    private String title;
    private String description;
    private String type; // cost_summary, detailed_breakdown, executive_summary, etc.
    private String timeRange; // last_7_days, last_30_days, current_month, custom, etc.
    private String customStartDate; // For custom time range
    private String customEndDate; // For custom time range
    private List<String> teams; // Filter by specific teams
    private List<String> services; // Filter by specific services
    private List<String> providers; // Filter by specific providers (AWS, Azure, GCP)
    private List<String> regions; // Filter by specific regions
    private String groupBy; // team, service, provider, region, environment, date
    private boolean includeCharts; // Whether to include visual charts
    private boolean includeRecommendations; // Whether to include optimization recommendations
    private boolean includeForecasts; // Whether to include cost forecasts
    private boolean includeComparisons; // Whether to include period comparisons
    private String format; // pdf, excel, csv, json
    private String schedule; // none, daily, weekly, monthly, quarterly
    private Map<String, Object> additionalParameters; // Type-specific parameters
    private String generatedBy; // User who requested the report

    // Constructors
    public GenerateReportRequest() {
        this.includeCharts = true;
        this.includeRecommendations = false;
        this.includeForecasts = false;
        this.includeComparisons = false;
        this.format = "pdf";
        this.schedule = "none";
    }

    public GenerateReportRequest(String title, String type, String timeRange) {
        this();
        this.title = title;
        this.type = type;
        this.timeRange = timeRange;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTimeRange() { return timeRange; }
    public void setTimeRange(String timeRange) { this.timeRange = timeRange; }

    public String getCustomStartDate() { return customStartDate; }
    public void setCustomStartDate(String customStartDate) { this.customStartDate = customStartDate; }

    public String getCustomEndDate() { return customEndDate; }
    public void setCustomEndDate(String customEndDate) { this.customEndDate = customEndDate; }

    public List<String> getTeams() { return teams; }
    public void setTeams(List<String> teams) { this.teams = teams; }

    public List<String> getServices() { return services; }
    public void setServices(List<String> services) { this.services = services; }

    public List<String> getProviders() { return providers; }
    public void setProviders(List<String> providers) { this.providers = providers; }

    public List<String> getRegions() { return regions; }
    public void setRegions(List<String> regions) { this.regions = regions; }

    public String getGroupBy() { return groupBy; }
    public void setGroupBy(String groupBy) { this.groupBy = groupBy; }

    public boolean isIncludeCharts() { return includeCharts; }
    public void setIncludeCharts(boolean includeCharts) { this.includeCharts = includeCharts; }

    public boolean isIncludeRecommendations() { return includeRecommendations; }
    public void setIncludeRecommendations(boolean includeRecommendations) { this.includeRecommendations = includeRecommendations; }

    public boolean isIncludeForecasts() { return includeForecasts; }
    public void setIncludeForecasts(boolean includeForecasts) { this.includeForecasts = includeForecasts; }

    public boolean isIncludeComparisons() { return includeComparisons; }
    public void setIncludeComparisons(boolean includeComparisons) { this.includeComparisons = includeComparisons; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

    public Map<String, Object> getAdditionalParameters() { return additionalParameters; }
    public void setAdditionalParameters(Map<String, Object> additionalParameters) { this.additionalParameters = additionalParameters; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    // Validation methods
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
               type != null && isValidType(type) &&
               timeRange != null && isValidTimeRange(timeRange) &&
               (format == null || isValidFormat(format)) &&
               (groupBy == null || isValidGroupBy(groupBy)) &&
               validateCustomTimeRange();
    }

    private boolean isValidType(String type) {
        return "cost_summary".equals(type) ||
               "detailed_breakdown".equals(type) ||
               "executive_summary".equals(type) ||
               "budget_performance".equals(type) ||
               "cost_optimization".equals(type) ||
               "chargeback".equals(type);
    }

    private boolean isValidTimeRange(String range) {
        return "last_7_days".equals(range) ||
               "last_30_days".equals(range) ||
               "last_90_days".equals(range) ||
               "current_month".equals(range) ||
               "last_month".equals(range) ||
               "current_quarter".equals(range) ||
               "last_quarter".equals(range) ||
               "current_year".equals(range) ||
               "custom".equals(range);
    }

    private boolean isValidFormat(String format) {
        return "pdf".equals(format) ||
               "excel".equals(format) ||
               "csv".equals(format) ||
               "json".equals(format);
    }

    private boolean isValidGroupBy(String groupBy) {
        return "team".equals(groupBy) ||
               "service".equals(groupBy) ||
               "provider".equals(groupBy) ||
               "region".equals(groupBy) ||
               "environment".equals(groupBy) ||
               "date".equals(groupBy);
    }

    private boolean validateCustomTimeRange() {
        if ("custom".equals(timeRange)) {
            return customStartDate != null && !customStartDate.isEmpty() &&
                   customEndDate != null && !customEndDate.isEmpty() &&
                   customStartDate.compareTo(customEndDate) < 0;
        }
        return true;
    }

    // Helper methods
    public boolean hasTeamFilter() {
        return teams != null && !teams.isEmpty();
    }

    public boolean hasServiceFilter() {
        return services != null && !services.isEmpty();
    }

    public boolean hasProviderFilter() {
        return providers != null && !providers.isEmpty();
    }

    public boolean hasRegionFilter() {
        return regions != null && !regions.isEmpty();
    }

    public boolean isScheduled() {
        return schedule != null && !"none".equals(schedule);
    }

    public boolean isCustomTimeRange() {
        return "custom".equals(timeRange);
    }

    public Object getAdditionalParameter(String key) {
        return additionalParameters != null ? additionalParameters.get(key) : null;
    }

    public void setAdditionalParameter(String key, Object value) {
        if (additionalParameters == null) {
            additionalParameters = new java.util.HashMap<>();
        }
        additionalParameters.put(key, value);
    }

    // Time range resolution methods
    public String[] getEffectiveDateRange() {
        if (isCustomTimeRange()) {
            return new String[]{customStartDate, customEndDate};
        }
        
        // Calculate date range based on timeRange
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate startDate;
        java.time.LocalDate endDate = now;
        
        switch (timeRange) {
            case "last_7_days" -> startDate = now.minusDays(7);
            case "last_30_days" -> startDate = now.minusDays(30);
            case "last_90_days" -> startDate = now.minusDays(90);
            case "current_month" -> {
                startDate = now.withDayOfMonth(1);
                endDate = now;
            }
            case "last_month" -> {
                java.time.LocalDate lastMonth = now.minusMonths(1);
                startDate = lastMonth.withDayOfMonth(1);
                endDate = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
            }
            case "current_quarter" -> {
                int quarter = (now.getMonthValue() - 1) / 3;
                startDate = now.withMonth(quarter * 3 + 1).withDayOfMonth(1);
                endDate = now;
            }
            case "last_quarter" -> {
                java.time.LocalDate lastQuarter = now.minusMonths(3);
                int quarter = (lastQuarter.getMonthValue() - 1) / 3;
                startDate = lastQuarter.withMonth(quarter * 3 + 1).withDayOfMonth(1);
                endDate = startDate.plusMonths(3).minusDays(1);
            }
            case "current_year" -> {
                startDate = now.withDayOfYear(1);
                endDate = now;
            }
            default -> startDate = now.minusDays(30); // Default to last 30 days
        }
        
        return new String[]{startDate.toString(), endDate.toString()};
    }

    @Override
    public String toString() {
        return "GenerateReportRequest{" +
                "title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", timeRange='" + timeRange + '\'' +
                ", format='" + format + '\'' +
                ", groupBy='" + groupBy + '\'' +
                ", includeCharts=" + includeCharts +
                ", includeRecommendations=" + includeRecommendations +
                ", teamsCount=" + (teams != null ? teams.size() : 0) +
                ", servicesCount=" + (services != null ? services.size() : 0) +
                '}';
    }
}