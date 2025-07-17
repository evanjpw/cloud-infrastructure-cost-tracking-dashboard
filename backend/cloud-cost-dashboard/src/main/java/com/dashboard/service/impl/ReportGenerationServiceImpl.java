package com.dashboard.service.impl;

import com.dashboard.dto.CostReportRequest;
import com.dashboard.dto.CostReportResponse;
import com.dashboard.model.CostBreakdown;
import com.dashboard.service.interfaces.CostCalculationService;
import com.dashboard.service.interfaces.ReportGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportGenerationServiceImpl implements ReportGenerationService {

    @Autowired
    private CostCalculationService costCalculationService;

    @Override
    public CostReportResponse generateReport(CostReportRequest request) {
        System.out.println("Generating cost report for: " + request.getTeamName());

        // Simulate report generation using fake breakdowns
        List<CostBreakdown> breakdowns = costCalculationService.calculateCosts(request);
        return new CostReportResponse(breakdowns);
    }
}
