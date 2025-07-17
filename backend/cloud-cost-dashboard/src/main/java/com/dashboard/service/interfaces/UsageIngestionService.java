package com.dashboard.service.interfaces;

public interface UsageIngestionService {
    void ingestUsageDataFromCsv(String filePath);
}
