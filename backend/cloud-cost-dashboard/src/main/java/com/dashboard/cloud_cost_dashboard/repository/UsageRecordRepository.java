package com.dashboard.cloud_cost_dashboard.repository;

import com.dashboard.cloud_cost_dashboard.model.Account;
import com.dashboard.cloud_cost_dashboard.model.UsageRecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long> {

    List<UsageRecord> findByUsageDateBetween(LocalDate startDate, LocalDate endDate);

    List<UsageRecord> findByTeamIdAndUsageDateBetween(
            Long teamId, LocalDate startDate, LocalDate endDate);

    @Query(
            "SELECT ur FROM UsageRecord ur WHERE ur.usageDate >= :startDate AND ur.usageDate <="
                    + " :endDate")
    List<UsageRecord> findUsageInDateRange(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(
            "SELECT SUM(ur.totalCost) FROM UsageRecord ur WHERE ur.team.id = :teamId AND"
                    + " ur.usageDate >= :startDate AND ur.usageDate <= :endDate")
    Double sumCostByTeamAndDateRange(
            @Param("teamId") Long teamId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query(
            "SELECT SUM(ur.totalCost) FROM UsageRecord ur WHERE ur.service.id = :serviceId AND"
                    + " ur.usageDate >= :startDate AND ur.usageDate <= :endDate")
    Double sumCostByServiceAndDateRange(
            @Param("serviceId") Long serviceId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Additional query methods for testing
    List<UsageRecord> findByTeam_NameAndUsageDateBetween(
            String teamName, LocalDate startDate, LocalDate endDate);

    List<UsageRecord> findByAccount_Environment(Account.Environment environment);
}
