package com.dashboard.service.impl;

import com.dashboard.dto.CostReportRequest;
import com.dashboard.model.CostBreakdown;
import com.dashboard.service.interfaces.CostCalculationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CostCalculationServiceImpl implements CostCalculationService {

    @Override
    public List<CostBreakdown> calculateCosts(CostReportRequest request) {
        // Placeholder logic: returning static values
        return List.of(
            new CostBreakdown(request.getTeamName(), "EC2", 1023.45),
            new CostBreakdown(request.getTeamName(), "S3", 876.90)
        );
    }
}
