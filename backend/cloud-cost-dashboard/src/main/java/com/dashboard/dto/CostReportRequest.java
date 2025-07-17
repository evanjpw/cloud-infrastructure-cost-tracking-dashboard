package com.dashboard.dto;

import java.time.LocalDate;

public class CostReportRequest {
    private String teamName;
    private LocalDate startDate;
    private LocalDate endDate;

    public CostReportRequest() {}

    public String getTeamName() {
        return teamName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
