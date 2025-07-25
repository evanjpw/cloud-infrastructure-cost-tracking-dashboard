package com.dashboard.repository;

import com.dashboard.cloud_cost_dashboard.model.UsageRecord;
import com.dashboard.cloud_cost_dashboard.repository.UsageRecordRepository;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UsageRepository extends UsageRecordRepository {

    // Backward compatibility method using team name
    default List<UsageRecord> findByTeamNameAndUsageDateBetween(
            String teamName, LocalDate startDate, LocalDate endDate) {
        return findByTeam_NameAndUsageDateBetween(teamName, startDate, endDate);
    }

    // New method using JPA relationship
    List<UsageRecord> findByTeam_NameAndUsageDateBetween(
            String teamName, LocalDate startDate, LocalDate endDate);
}
