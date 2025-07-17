package com.dashboard.service.impl;

import com.dashboard.dto.CostReportRequest;
import com.dashboard.model.CostBreakdown;
import com.dashboard.service.interfaces.CostCalculationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CostCalculationServiceImpl implements CostCalculationService {

    @Override
    public List<CostBreakdown> calculateCosts(CostReportRequest request) {
        System.out.println("Calculating costs for team: " + request.getTeamName());

        // Placeholder: simulate cost computation with dummy data
        List<CostBreakdown> results = new ArrayList<>();

        // These would normally be computed from actual usage records
        results.add(new CostBreakdown(request.getTeamName(), "EC2", 945.50));
        results.add(new CostBreakdown(request.getTeamName(), "S3", 712.30));
        results.add(new CostBreakdown(request.getTeamName(), "RDS", 388.75));

        System.out.println("Generated " + results.size() + " cost breakdowns.");
        return results;
    }
}
