package com.dashboard.service.interfaces;

import com.dashboard.dto.CostReportRequest;
import com.dashboard.model.CostBreakdown;

import java.util.List;

public interface CostCalculationService {
    List<CostBreakdown> calculateCosts(CostReportRequest request);
}
