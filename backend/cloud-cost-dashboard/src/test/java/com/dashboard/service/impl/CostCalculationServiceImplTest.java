package com.dashboard.service.impl;

import com.dashboard.cloud_cost_dashboard.model.UsageRecord;
import com.dashboard.cloud_cost_dashboard.model.Team;
import com.dashboard.cloud_cost_dashboard.model.Service;
import com.dashboard.cloud_cost_dashboard.repository.UsageRecordRepository;
import com.dashboard.dto.CostReportRequest;
import com.dashboard.model.CostBreakdown;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Cost Calculation Service Tests")
class CostCalculationServiceImplTest {

    @Mock
    private UsageRecordRepository usageRecordRepository;

    @InjectMocks
    private CostCalculationServiceImpl costCalculationService;

    private List<UsageRecord> mockUsageRecords;
    private CostReportRequest validRequest;
    private Team mockTeam;
    private Service mockService1, mockService2, mockService3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        setupMockData();
        validRequest = createValidCostReportRequest();
    }

    @Test
    @DisplayName("Should calculate costs for specific team successfully")
    void testCalculateCosts_WithTeamName() {
        // Given
        CostReportRequest request = new CostReportRequest();
        request.setTeamName("platform");
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setEndDate(LocalDate.of(2025, 1, 31));

        when(usageRecordRepository.findByTeam_NameAndUsageDateBetween(
                eq("platform"), 
                eq(LocalDate.of(2025, 1, 1)), 
                eq(LocalDate.of(2025, 1, 31))
        )).thenReturn(mockUsageRecords);

        // When
        List<CostBreakdown> result = costCalculationService.calculateCosts(request);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Verify calculations
        CostBreakdown ec2Breakdown = result.stream()
                .filter(cb -> "Amazon EC2".equals(cb.getService()))
                .findFirst().orElse(null);
        assertNotNull(ec2Breakdown);
        assertEquals("platform", ec2Breakdown.getTeamName());
        assertEquals(250.0, ec2Breakdown.getTotalCost(), 0.01);

        CostBreakdown s3Breakdown = result.stream()
                .filter(cb -> "Amazon S3".equals(cb.getService()))
                .findFirst().orElse(null);
        assertNotNull(s3Breakdown);
        assertEquals(100.0, s3Breakdown.getTotalCost(), 0.01);

        verify(usageRecordRepository).findByTeam_NameAndUsageDateBetween(
                eq("platform"), 
                eq(LocalDate.of(2025, 1, 1)), 
                eq(LocalDate.of(2025, 1, 31))
        );
    }

    @Test
    @DisplayName("Should calculate costs for all teams when no team specified")
    void testCalculateCosts_AllTeams() {
        // Given
        CostReportRequest request = new CostReportRequest();
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setEndDate(LocalDate.of(2025, 1, 31));
        // teamName is null

        when(usageRecordRepository.findByUsageDateBetween(
                eq(LocalDate.of(2025, 1, 1)), 
                eq(LocalDate.of(2025, 1, 31))
        )).thenReturn(mockUsageRecords);

        // When
        List<CostBreakdown> result = costCalculationService.calculateCosts(request);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        
        verify(usageRecordRepository).findByUsageDateBetween(
                eq(LocalDate.of(2025, 1, 1)), 
                eq(LocalDate.of(2025, 1, 31))
        );
    }

    @Test
    @DisplayName("Should calculate costs with empty team name")
    void testCalculateCosts_EmptyTeamName() {
        // Given
        CostReportRequest request = new CostReportRequest();
        request.setTeamName(""); // Empty string
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setEndDate(LocalDate.of(2025, 1, 31));

        when(usageRecordRepository.findByUsageDateBetween(
                eq(LocalDate.of(2025, 1, 1)), 
                eq(LocalDate.of(2025, 1, 31))
        )).thenReturn(mockUsageRecords);

        // When
        List<CostBreakdown> result = costCalculationService.calculateCosts(request);

        // Then
        assertNotNull(result);
        verify(usageRecordRepository).findByUsageDateBetween(
                eq(LocalDate.of(2025, 1, 1)), 
                eq(LocalDate.of(2025, 1, 31))
        );
    }

    @Test
    @DisplayName("Should use default date range when dates are null")
    void testCalculateCosts_NullDates() {
        // Given
        CostReportRequest request = new CostReportRequest();
        request.setTeamName("platform");
        // startDate and endDate are null

        when(usageRecordRepository.findByTeam_NameAndUsageDateBetween(
                eq("platform"), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(mockUsageRecords);

        // When
        List<CostBreakdown> result = costCalculationService.calculateCosts(request);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Verify that repository was called with some date range (defaults to last 30 days)
        verify(usageRecordRepository).findByTeam_NameAndUsageDateBetween(
                eq("platform"), any(LocalDate.class), any(LocalDate.class)
        );
    }

    @Test
    @DisplayName("Should handle records with null service gracefully")
    void testCalculateCosts_NullService() {
        // Given
        List<UsageRecord> recordsWithNullService = createMockUsageRecordsWithNullService();
        when(usageRecordRepository.findByTeam_NameAndUsageDateBetween(
                anyString(), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(recordsWithNullService);

        // When
        List<CostBreakdown> result = costCalculationService.calculateCosts(validRequest);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Should have one "Unknown Service" entry
        CostBreakdown unknownServiceBreakdown = result.stream()
                .filter(cb -> "Unknown Service".equals(cb.getService()))
                .findFirst().orElse(null);
        assertNotNull(unknownServiceBreakdown);
        assertEquals(150.0, unknownServiceBreakdown.getTotalCost(), 0.01);
    }

    @Test
    @DisplayName("Should handle records with null cost gracefully")
    void testCalculateCosts_NullCost() {
        // Given
        List<UsageRecord> recordsWithNullCost = createMockUsageRecordsWithNullCost();
        when(usageRecordRepository.findByTeam_NameAndUsageDateBetween(
                anyString(), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(recordsWithNullCost);

        // When
        List<CostBreakdown> result = costCalculationService.calculateCosts(validRequest);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Records with null cost should contribute 0.0 to the total
        double totalCost = result.stream()
                .mapToDouble(CostBreakdown::getTotalCost)
                .sum();
        assertEquals(200.0, totalCost, 0.01); // Only non-null costs
    }

    @Test
    @DisplayName("Should return fallback data when no records found")
    void testCalculateCosts_NoDataFound() {
        // Given
        when(usageRecordRepository.findByTeam_NameAndUsageDateBetween(
                anyString(), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(Collections.emptyList());

        // When
        List<CostBreakdown> result = costCalculationService.calculateCosts(validRequest);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Should contain fallback data
        Set<String> services = result.stream()
                .map(CostBreakdown::getService)
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(services.contains("EC2"));
        assertTrue(services.contains("S3"));
        assertTrue(services.contains("RDS"));
        
        // Verify fallback costs
        CostBreakdown ec2Fallback = result.stream()
                .filter(cb -> "EC2".equals(cb.getService()))
                .findFirst().orElse(null);
        assertNotNull(ec2Fallback);
        assertEquals(945.50, ec2Fallback.getTotalCost(), 0.01);
    }

    @Test
    @DisplayName("Should return fallback data when repository throws exception")
    void testCalculateCosts_RepositoryException() {
        // Given
        when(usageRecordRepository.findByTeam_NameAndUsageDateBetween(
                anyString(), any(LocalDate.class), any(LocalDate.class)
        )).thenThrow(new RuntimeException("Database connection failed"));

        // When
        List<CostBreakdown> result = costCalculationService.calculateCosts(validRequest);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Should contain fallback data
        Set<String> services = result.stream()
                .map(CostBreakdown::getService)
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(services.contains("EC2"));
        assertTrue(services.contains("S3"));
        assertTrue(services.contains("RDS"));
    }

    @Test
    @DisplayName("Should aggregate costs correctly for duplicate services")
    void testCalculateCosts_DuplicateServices() {
        // Given
        List<UsageRecord> duplicateServiceRecords = createMockUsageRecordsWithDuplicates();
        when(usageRecordRepository.findByTeam_NameAndUsageDateBetween(
                anyString(), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(duplicateServiceRecords);

        // When
        List<CostBreakdown> result = costCalculationService.calculateCosts(validRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        CostBreakdown ec2Breakdown = result.get(0);
        assertEquals("Amazon EC2", ec2Breakdown.getService());
        assertEquals(300.0, ec2Breakdown.getTotalCost(), 0.01); // 100 + 200 = 300
    }

    @Test
    @DisplayName("Should handle mixed valid and invalid data")
    void testCalculateCosts_MixedData() {
        // Given
        List<UsageRecord> mixedRecords = createMockMixedDataRecords();
        when(usageRecordRepository.findByTeam_NameAndUsageDateBetween(
                anyString(), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(mixedRecords);

        // When
        List<CostBreakdown> result = costCalculationService.calculateCosts(validRequest);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Should handle valid records and treat invalid ones appropriately
        Set<String> services = result.stream()
                .map(CostBreakdown::getService)
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(services.contains("Amazon EC2"));
        assertTrue(services.contains("Unknown Service"));
    }

    // ========================================
    // HELPER METHODS FOR TEST DATA
    // ========================================

    private void setupMockData() {
        // Create mock Team
        mockTeam = new Team();
        mockTeam.setId(1L);
        mockTeam.setName("platform");

        // Create mock Services
        mockService1 = new Service();
        mockService1.setId(1L);
        mockService1.setServiceName("Amazon EC2");

        mockService2 = new Service();
        mockService2.setId(2L);
        mockService2.setServiceName("Amazon S3");

        mockService3 = new Service();
        mockService3.setId(3L);
        mockService3.setServiceName("Amazon RDS");

        // Create mock usage records
        mockUsageRecords = Arrays.asList(
            createUsageRecord(1L, mockTeam, mockService1, new BigDecimal("150.00"), LocalDate.of(2025, 1, 15)),
            createUsageRecord(2L, mockTeam, mockService1, new BigDecimal("100.00"), LocalDate.of(2025, 1, 16)),
            createUsageRecord(3L, mockTeam, mockService2, new BigDecimal("75.00"), LocalDate.of(2025, 1, 17)),
            createUsageRecord(4L, mockTeam, mockService2, new BigDecimal("25.00"), LocalDate.of(2025, 1, 18)),
            createUsageRecord(5L, mockTeam, mockService3, new BigDecimal("200.00"), LocalDate.of(2025, 1, 19))
        );
    }

    private UsageRecord createUsageRecord(Long id, Team team, Service service, BigDecimal cost, LocalDate date) {
        UsageRecord record = new UsageRecord();
        record.setId(id);
        record.setTeam(team);
        record.setService(service);
        record.setTotalCost(cost);
        record.setUsageDate(date);
        record.setUsageQuantity(new BigDecimal("10"));
        record.setUnitPrice(cost.divide(new BigDecimal("10"), 2, BigDecimal.ROUND_HALF_UP));
        return record;
    }

    private List<UsageRecord> createMockUsageRecordsWithNullService() {
        UsageRecord recordWithNullService = new UsageRecord();
        recordWithNullService.setId(1L);
        recordWithNullService.setTeam(mockTeam);
        recordWithNullService.setService(null); // Null service
        recordWithNullService.setTotalCost(new BigDecimal("150.00"));
        recordWithNullService.setUsageDate(LocalDate.of(2025, 1, 15));

        UsageRecord normalRecord = createUsageRecord(2L, mockTeam, mockService1, new BigDecimal("100.00"), LocalDate.of(2025, 1, 16));

        return Arrays.asList(recordWithNullService, normalRecord);
    }

    private List<UsageRecord> createMockUsageRecordsWithNullCost() {
        UsageRecord recordWithNullCost = new UsageRecord();
        recordWithNullCost.setId(1L);
        recordWithNullCost.setTeam(mockTeam);
        recordWithNullCost.setService(mockService1);
        recordWithNullCost.setTotalCost(null); // Null cost
        recordWithNullCost.setUsageDate(LocalDate.of(2025, 1, 15));

        UsageRecord normalRecord1 = createUsageRecord(2L, mockTeam, mockService1, new BigDecimal("100.00"), LocalDate.of(2025, 1, 16));
        UsageRecord normalRecord2 = createUsageRecord(3L, mockTeam, mockService2, new BigDecimal("100.00"), LocalDate.of(2025, 1, 17));

        return Arrays.asList(recordWithNullCost, normalRecord1, normalRecord2);
    }

    private List<UsageRecord> createMockUsageRecordsWithDuplicates() {
        UsageRecord record1 = createUsageRecord(1L, mockTeam, mockService1, new BigDecimal("100.00"), LocalDate.of(2025, 1, 15));
        UsageRecord record2 = createUsageRecord(2L, mockTeam, mockService1, new BigDecimal("200.00"), LocalDate.of(2025, 1, 16));
        
        return Arrays.asList(record1, record2);
    }

    private List<UsageRecord> createMockMixedDataRecords() {
        // Valid record
        UsageRecord validRecord = createUsageRecord(1L, mockTeam, mockService1, new BigDecimal("100.00"), LocalDate.of(2025, 1, 15));
        
        // Record with null service
        UsageRecord nullServiceRecord = new UsageRecord();
        nullServiceRecord.setId(2L);
        nullServiceRecord.setTeam(mockTeam);
        nullServiceRecord.setService(null);
        nullServiceRecord.setTotalCost(new BigDecimal("50.00"));
        nullServiceRecord.setUsageDate(LocalDate.of(2025, 1, 16));
        
        // Record with null cost
        UsageRecord nullCostRecord = new UsageRecord();
        nullCostRecord.setId(3L);
        nullCostRecord.setTeam(mockTeam);
        nullCostRecord.setService(mockService2);
        nullCostRecord.setTotalCost(null);
        nullCostRecord.setUsageDate(LocalDate.of(2025, 1, 17));
        
        return Arrays.asList(validRecord, nullServiceRecord, nullCostRecord);
    }

    private CostReportRequest createValidCostReportRequest() {
        CostReportRequest request = new CostReportRequest();
        request.setTeamName("platform");
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setEndDate(LocalDate.of(2025, 1, 31));
        return request;
    }
}