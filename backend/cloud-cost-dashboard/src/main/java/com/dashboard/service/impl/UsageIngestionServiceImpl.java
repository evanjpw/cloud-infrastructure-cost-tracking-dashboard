package com.dashboard.service.impl;

import com.dashboard.cloud_cost_dashboard.model.UsageRecord;
import com.dashboard.cloud_cost_dashboard.repository.UsageRecordRepository;
import com.dashboard.service.interfaces.UsageIngestionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsageIngestionServiceImpl implements UsageIngestionService {

    @Autowired private UsageRecordRepository usageRepository;

    @Override
    public void ingestUsageDataFromCsv(String filePath) {
        System.out.println("Starting ingestion from file: " + filePath);

        // Placeholder: simulate reading and saving records
        try {
            List<UsageRecord> mockRecords =
                    List.of(
                            new UsageRecord(), // pretend record 1
                            new UsageRecord() // pretend record 2
                            );

            usageRepository.saveAll(mockRecords);
            System.out.println("Successfully ingested usage records from CSV.");
        } catch (Exception e) {
            System.err.println("Failed to ingest usage data: " + e.getMessage());
        }
    }
}
