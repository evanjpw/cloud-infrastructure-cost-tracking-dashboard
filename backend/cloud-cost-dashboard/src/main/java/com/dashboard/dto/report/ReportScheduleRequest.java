package com.dashboard.dto.report;

import java.util.List;

public class ReportScheduleRequest {
    private String name;
    private String description;
    private GenerateReportRequest reportConfig; // Base report configuration
    private String frequency; // daily, weekly, monthly, quarterly
    private String startDate; // When to start the schedule
    private String endDate; // When to end the schedule (optional)
    private List<String> recipients; // Email addresses for report delivery
    private String deliveryFormat; // pdf, excel, csv, json
    private boolean isActive; // Whether schedule is currently active
    private String timezone; // Timezone for schedule execution
    private String createdBy;

    // Constructors
    public ReportScheduleRequest() {
        this.isActive = true;
        this.deliveryFormat = "pdf";
        this.timezone = "UTC";
    }

    public ReportScheduleRequest(String name, GenerateReportRequest reportConfig, String frequency) {
        this();
        this.name = name;
        this.reportConfig = reportConfig;
        this.frequency = frequency;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public GenerateReportRequest getReportConfig() { return reportConfig; }
    public void setReportConfig(GenerateReportRequest reportConfig) { this.reportConfig = reportConfig; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public List<String> getRecipients() { return recipients; }
    public void setRecipients(List<String> recipients) { this.recipients = recipients; }

    public String getDeliveryFormat() { return deliveryFormat; }
    public void setDeliveryFormat(String deliveryFormat) { this.deliveryFormat = deliveryFormat; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    // Validation methods
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               reportConfig != null && reportConfig.isValid() &&
               frequency != null && isValidFrequency(frequency) &&
               startDate != null && !startDate.isEmpty() &&
               deliveryFormat != null && isValidDeliveryFormat(deliveryFormat) &&
               (recipients == null || recipients.stream().allMatch(this::isValidEmail));
    }

    private boolean isValidFrequency(String frequency) {
        return "daily".equals(frequency) ||
               "weekly".equals(frequency) ||
               "monthly".equals(frequency) ||
               "quarterly".equals(frequency);
    }

    private boolean isValidDeliveryFormat(String format) {
        return "pdf".equals(format) ||
               "excel".equals(format) ||
               "csv".equals(format) ||
               "json".equals(format);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    // Helper methods
    public boolean hasRecipients() {
        return recipients != null && !recipients.isEmpty();
    }

    public boolean hasEndDate() {
        return endDate != null && !endDate.isEmpty();
    }

    public boolean isDailySchedule() {
        return "daily".equals(frequency);
    }

    public boolean isWeeklySchedule() {
        return "weekly".equals(frequency);
    }

    public boolean isMonthlySchedule() {
        return "monthly".equals(frequency);
    }

    public boolean isQuarterlySchedule() {
        return "quarterly".equals(frequency);
    }

    @Override
    public String toString() {
        return "ReportScheduleRequest{" +
                "name='" + name + '\'' +
                ", frequency='" + frequency + '\'' +
                ", startDate='" + startDate + '\'' +
                ", deliveryFormat='" + deliveryFormat + '\'' +
                ", isActive=" + isActive +
                ", recipientCount=" + (recipients != null ? recipients.size() : 0) +
                '}';
    }
}