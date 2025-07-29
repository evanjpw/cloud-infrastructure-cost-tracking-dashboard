package com.dashboard.model.report;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class Report {
    private String id;
    private String title;
    private String description;
    private String type; // cost_summary, detailed_breakdown, executive_summary, etc.
    private String status; // generating, completed, failed
    private Map<String, Object> configuration; // Report generation parameters
    private Map<String, Object> data; // Report data content
    private Map<String, Object> metadata; // Size, generation time, etc.
    private List<String> sections; // Report sections/chapters
    private ReportSummary summary; // Executive summary of findings
    private List<String> recommendations; // Key recommendations
    private String format; // pdf, excel, csv, json
    private String generatedBy;
    private String createdAt;
    private String completedAt;
    private long generationTimeMs;

    // Constructors
    public Report() {
        this.status = "generating";
        this.createdAt = java.time.Instant.now().toString();
    }

    public Report(String title, String description, String type) {
        this();
        this.title = title;
        this.description = description;
        this.type = type;
    }

    // Nested class for report summary
    public static class ReportSummary {
        private BigDecimal totalCost;
        private BigDecimal costChange;
        private double costChangePercentage;
        private int timelineDay;
        private String topCostDriver;
        private BigDecimal topCostDriverAmount;
        private String period;
        private List<String> keyFindings;
        private Map<String, BigDecimal> categoryBreakdown;

        public ReportSummary() {}

        // Getters and Setters
        public BigDecimal getTotalCost() { return totalCost; }
        public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

        public BigDecimal getCostChange() { return costChange; }
        public void setCostChange(BigDecimal costChange) { this.costChange = costChange; }

        public double getCostChangePercentage() { return costChangePercentage; }
        public void setCostChangePercentage(double costChangePercentage) { this.costChangePercentage = costChangePercentage; }

        public int getTimelineDays() { return timelineDay; }
        public void setTimelineDays(int timelineDays) { this.timelineDay = timelineDays; }

        public String getTopCostDriver() { return topCostDriver; }
        public void setTopCostDriver(String topCostDriver) { this.topCostDriver = topCostDriver; }

        public BigDecimal getTopCostDriverAmount() { return topCostDriverAmount; }
        public void setTopCostDriverAmount(BigDecimal topCostDriverAmount) { this.topCostDriverAmount = topCostDriverAmount; }

        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }

        public List<String> getKeyFindings() { return keyFindings; }
        public void setKeyFindings(List<String> keyFindings) { this.keyFindings = keyFindings; }

        public Map<String, BigDecimal> getCategoryBreakdown() { return categoryBreakdown; }
        public void setCategoryBreakdown(Map<String, BigDecimal> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }
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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<String, Object> getConfiguration() { return configuration; }
    public void setConfiguration(Map<String, Object> configuration) { this.configuration = configuration; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public List<String> getSections() { return sections; }
    public void setSections(List<String> sections) { this.sections = sections; }

    public ReportSummary getSummary() { return summary; }
    public void setSummary(ReportSummary summary) { this.summary = summary; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }

    public long getGenerationTimeMs() { return generationTimeMs; }
    public void setGenerationTimeMs(long generationTimeMs) { this.generationTimeMs = generationTimeMs; }

    // Helper methods
    public boolean isCompleted() {
        return "completed".equals(status);
    }

    public boolean isFailed() {
        return "failed".equals(status);
    }

    public void markCompleted() {
        this.status = "completed";
        this.completedAt = java.time.Instant.now().toString();
        
        if (createdAt != null) {
            try {
                long startTime = java.time.Instant.parse(createdAt).toEpochMilli();
                long endTime = java.time.Instant.parse(completedAt).toEpochMilli();
                this.generationTimeMs = endTime - startTime;
            } catch (Exception e) {
                this.generationTimeMs = 0;
            }
        }
    }

    public void markFailed() {
        this.status = "failed";
        this.completedAt = java.time.Instant.now().toString();
    }

    public String getConfigurationValue(String key) {
        return configuration != null ? (String) configuration.get(key) : null;
    }

    public void setConfigurationValue(String key, Object value) {
        if (configuration == null) {
            configuration = new java.util.HashMap<>();
        }
        configuration.put(key, value);
    }
}