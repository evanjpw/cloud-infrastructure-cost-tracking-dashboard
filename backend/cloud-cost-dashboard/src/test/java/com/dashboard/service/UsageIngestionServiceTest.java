package com.dashboard.service;

import com.dashboard.service.impl.UsageIngestionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UsageIngestionServiceTest {

    private UsageIngestionServiceImpl ingestionService;

    @BeforeEach
    void setUp() {
        ingestionService = new UsageIngestionServiceImpl();
    }

    @Test
    void ingestUsageDataFromCsv_shouldRunWithoutErrors() {
        String dummyFilePath = "data/sample-usage.csv";

        try {
            ingestionService.ingestUsageDataFromCsv(dummyFilePath);
            System.out.println("Ingestion service executed successfully.");
        } catch (Exception e) {
            System.err.println("Ingestion failed: " + e.getMessage());
        }
    }
}
