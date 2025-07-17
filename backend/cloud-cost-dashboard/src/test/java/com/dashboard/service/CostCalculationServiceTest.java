package com.dashboard.service;

import com.dashboard.dto.CostReportRequest;
import com.dashboard.model.CostBreakdown;
import com.dashboard.service.impl.CostCalculationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CostCalculationServiceTest {

    private CostCalculationServiceImpl costCalculationService;

    @BeforeEach
    void setUp() {
        costCalculationService = new CostCalculationServiceImpl();
    }

    @Test
    void calculateCosts_shouldReturnNonEmptyList() {
        CostReportRequest request = new CostReportRequest();
        // Normally you'd use setters, this is placeholder behavior
        List<CostBreakdown> result = costCalculationService.calculateCosts(request);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        System.out.println("CostCalculationService returned " + result.size() + " items.");
    }
}
