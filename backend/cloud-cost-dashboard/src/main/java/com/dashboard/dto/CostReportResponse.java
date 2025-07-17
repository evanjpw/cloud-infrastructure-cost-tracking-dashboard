package com.dashboard.dto;

import com.dashboard.model.CostBreakdown;
import java.util.List;

public class CostReportResponse {
    private List<CostBreakdown> breakdowns;

    public CostReportResponse() {}

    public CostReportResponse(List<CostBreakdown> breakdowns) {
        this.breakdowns = breakdowns;
    }

    public List<CostBreakdown> getBreakdowns() {
        return breakdowns;
    }
}
