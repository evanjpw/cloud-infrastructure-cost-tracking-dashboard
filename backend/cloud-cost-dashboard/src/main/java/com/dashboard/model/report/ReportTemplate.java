package com.dashboard.model.report;

import java.util.List;
import java.util.Map;

public class ReportTemplate {
    private String id;
    private String name;
    private String description;
    private String type; // cost_summary, detailed_breakdown, executive_summary, etc.
    private String icon; // Unicode emoji or icon identifier
    private String category; // financial, operational, executive, technical
    private Map<String, Object> defaultConfiguration; // Default settings
    private List<String> requiredParameters; // Required config parameters
    private List<String> optionalParameters; // Optional config parameters
    private List<String> supportedFormats; // pdf, excel, csv, json
    private List<String> sections; // Default sections included
    private String estimatedGenerationTime; // Time estimate for generation
    private String difficulty; // beginner, intermediate, advanced
    private boolean isActive; // Whether template is currently available
    private String createdAt;
    private String updatedAt;

    // Constructors
    public ReportTemplate() {
        this.isActive = true;
        this.createdAt = java.time.Instant.now().toString();
    }

    public ReportTemplate(String name, String description, String type, String icon) {
        this();
        this.name = name;
        this.description = description;
        this.type = type;
        this.icon = icon;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Map<String, Object> getDefaultConfiguration() { return defaultConfiguration; }
    public void setDefaultConfiguration(Map<String, Object> defaultConfiguration) { this.defaultConfiguration = defaultConfiguration; }

    public List<String> getRequiredParameters() { return requiredParameters; }
    public void setRequiredParameters(List<String> requiredParameters) { this.requiredParameters = requiredParameters; }

    public List<String> getOptionalParameters() { return optionalParameters; }
    public void setOptionalParameters(List<String> optionalParameters) { this.optionalParameters = optionalParameters; }

    public List<String> getSupportedFormats() { return supportedFormats; }
    public void setSupportedFormats(List<String> supportedFormats) { this.supportedFormats = supportedFormats; }

    public List<String> getSections() { return sections; }
    public void setSections(List<String> sections) { this.sections = sections; }

    public String getEstimatedGenerationTime() { return estimatedGenerationTime; }
    public void setEstimatedGenerationTime(String estimatedGenerationTime) { this.estimatedGenerationTime = estimatedGenerationTime; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean supportsFormat(String format) {
        return supportedFormats != null && supportedFormats.contains(format);
    }

    public boolean hasRequiredParameter(String parameter) {
        return requiredParameters != null && requiredParameters.contains(parameter);
    }

    public boolean isExecutiveLevel() {
        return "executive".equals(category) || "executive_summary".equals(type);
    }

    public boolean isFinancialReport() {
        return "financial".equals(category) || 
               type.contains("cost") || 
               type.contains("budget") || 
               type.contains("chargeback");
    }

    public Object getDefaultValue(String parameter) {
        return defaultConfiguration != null ? defaultConfiguration.get(parameter) : null;
    }

    public void setDefaultValue(String parameter, Object value) {
        if (defaultConfiguration == null) {
            defaultConfiguration = new java.util.HashMap<>();
        }
        defaultConfiguration.put(parameter, value);
    }

    // Factory methods for common templates
    public static ReportTemplate createCostSummaryTemplate() {
        ReportTemplate template = new ReportTemplate(
            "Cost Summary Report",
            "High-level cost overview with trends and KPIs",
            "cost_summary",
            "📊"
        );
        
        template.setCategory("financial");
        template.setDifficulty("beginner");
        template.setEstimatedGenerationTime("2-3 minutes");
        template.setSupportedFormats(List.of("pdf", "excel", "csv", "json"));
        template.setSections(List.of("Executive Summary", "Cost Trends", "Top Cost Drivers", "Recommendations"));
        template.setRequiredParameters(List.of("timeRange"));
        template.setOptionalParameters(List.of("teams", "services", "groupBy", "includeCharts"));
        
        Map<String, Object> defaults = new java.util.HashMap<>();
        defaults.put("timeRange", "last_30_days");
        defaults.put("groupBy", "team");
        defaults.put("includeCharts", true);
        template.setDefaultConfiguration(defaults);
        
        return template;
    }

    public static ReportTemplate createExecutiveSummaryTemplate() {
        ReportTemplate template = new ReportTemplate(
            "Executive Summary",
            "Executive-level insights and recommendations",
            "executive_summary",
            "👔"
        );
        
        template.setCategory("executive");
        template.setDifficulty("intermediate");
        template.setEstimatedGenerationTime("5-7 minutes");
        template.setSupportedFormats(List.of("pdf", "excel"));
        template.setSections(List.of("Executive Overview", "Financial Impact", "Strategic Recommendations", "Next Steps"));
        template.setRequiredParameters(List.of("timeRange"));
        template.setOptionalParameters(List.of("includeForecasts", "includeComparisons"));
        
        Map<String, Object> defaults = new java.util.HashMap<>();
        defaults.put("timeRange", "current_quarter");
        defaults.put("includeForecasts", true);
        defaults.put("includeComparisons", true);
        template.setDefaultConfiguration(defaults);
        
        return template;
    }

    public static ReportTemplate createCostOptimizationTemplate() {
        ReportTemplate template = new ReportTemplate(
            "Cost Optimization Report",
            "Optimization opportunities and recommendations",
            "cost_optimization",
            "💡"
        );
        
        template.setCategory("operational");
        template.setDifficulty("advanced");
        template.setEstimatedGenerationTime("7-10 minutes");
        template.setSupportedFormats(List.of("pdf", "excel", "json"));
        template.setSections(List.of("Optimization Overview", "Rightsizing Opportunities", 
                                   "Reserved Instance Analysis", "Unused Resources", "Implementation Plan"));
        template.setRequiredParameters(List.of("timeRange"));
        template.setOptionalParameters(List.of("optimizationTypes", "minimumSavings", "riskTolerance"));
        
        Map<String, Object> defaults = new java.util.HashMap<>();
        defaults.put("timeRange", "last_90_days");
        defaults.put("minimumSavings", 100.0);
        defaults.put("riskTolerance", "medium");
        template.setDefaultConfiguration(defaults);
        
        return template;
    }
}