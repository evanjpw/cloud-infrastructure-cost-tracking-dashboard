package com.dashboard.repository;

import com.dashboard.model.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UsageRepository extends JpaRepository<UsageRecord, Long> {

    List<UsageRecord> findByTeamNameAndUsageDateBetween(String teamName, LocalDate startDate, LocalDate endDate);
}
