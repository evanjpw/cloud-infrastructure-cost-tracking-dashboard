package com.dashboard.service.impl;

import com.dashboard.model.UsageRecord;
import com.dashboard.repository.UsageRepository;
import com.dashboard.service.interfaces.UsageIngestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;

@Service
public class UsageIngestionServiceImpl implements UsageIngestionService {

    @Autowired
    private UsageRepository usageRepository;

    @Override
    public void ingestUsageDataFromCsv(String filePath) {
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource(filePath).getInputStream())
            );

            String line;
            List<UsageRecord> records = new ArrayList<>();
            reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                UsageRecord record = new UsageRecord();
                record.setTeamName(fields[0]);
                record.setService(fields[1]);
                record.setUsageAmount(Double.parseDouble(fields[2]));
                record.setUsageDate(LocalDate.parse(fields[3]));
                records.add(record);
            }

            usageRepository.saveAll(records);
        } catch (Exception e) {
            System.err.println("Error reading usage data: " + e.getMessage());
        }
    }
}
