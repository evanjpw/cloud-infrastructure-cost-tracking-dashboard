package com.dashboard.service.interfaces;

import com.dashboard.dto.CostReportRequest;
import com.dashboard.dto.CostReportResponse;

public interface ReportGenerationService {
    CostReportResponse generateReport(CostReportRequest request);
}
