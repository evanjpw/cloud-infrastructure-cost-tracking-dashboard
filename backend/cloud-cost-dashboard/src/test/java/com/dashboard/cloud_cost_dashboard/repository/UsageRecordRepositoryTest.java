package com.dashboard.cloud_cost_dashboard.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.dashboard.cloud_cost_dashboard.model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
@ActiveProfiles("test")
class UsageRecordRepositoryTest {

    @Autowired private TestEntityManager entityManager;

    @Autowired private UsageRecordRepository usageRecordRepository;

    private Team team;
    private Service service;
    private Account account;
    private CloudProvider provider;

    @BeforeEach
    void setUp() {
        // Create test data
        provider = new CloudProvider();
        provider.setName("test-aws");
        provider.setDisplayName("Test AWS");
        entityManager.persist(provider);

        team = new Team();
        team.setName("test-team");
        team.setDisplayName("Test Team");
        team.setDepartment("Engineering");
        entityManager.persist(team);

        service = new Service();
        service.setCloudProvider(provider);
        service.setServiceCode("TestEC2");
        service.setServiceName("Test EC2");
        service.setCategory("Compute");
        entityManager.persist(service);

        account = new Account();
        account.setCloudProvider(provider);
        account.setAccountId("test-123");
        account.setAccountName("Test Account");
        account.setEnvironment(Account.Environment.PRODUCTION);
        account.setStatus(Account.AccountStatus.ACTIVE);
        entityManager.persist(account);

        entityManager.flush();
    }

    @Test
    void shouldSaveAndFindUsageRecord() {
        // Given
        UsageRecord record = new UsageRecord();
        record.setTeam(team);
        record.setService(service);
        record.setAccount(account);
        record.setUsageDate(LocalDate.now());
        record.setUsageHour(12);
        record.setUsageQuantity(new BigDecimal("10.5"));
        record.setUnitPrice(new BigDecimal("0.05"));
        record.setTotalCost(new BigDecimal("0.525"));
        record.setCurrency("USD");
        record.setRegion("us-east-1");
        record.setResourceType("Instance");
        record.setResourceId("i-1234567890abcdef0");
        record.setUsageUnit("Instance-Hours");
        record.setCreatedAt(LocalDateTime.now());

        // When
        UsageRecord saved = usageRecordRepository.save(record);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTeam().getName()).isEqualTo("test-team");
        assertThat(saved.getService().getServiceCode()).isEqualTo("TestEC2");
        assertThat(saved.getAccount().getAccountId()).isEqualTo("test-123");
        assertThat(saved.getTotalCost()).isEqualByComparingTo(new BigDecimal("0.525"));
    }

    @Test
    void shouldFindByTeamNameAndUsageDateBetween() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now();

        UsageRecord record1 = createUsageRecord(startDate.plusDays(1), new BigDecimal("10.00"));
        UsageRecord record2 = createUsageRecord(startDate.plusDays(3), new BigDecimal("15.00"));
        UsageRecord record3 =
                createUsageRecord(startDate.minusDays(1), new BigDecimal("5.00")); // Outside range

        usageRecordRepository.saveAll(List.of(record1, record2, record3));
        entityManager.flush();

        // When
        List<UsageRecord> results =
                usageRecordRepository.findByTeam_NameAndUsageDateBetween(
                        "test-team", startDate, endDate);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(UsageRecord::getTotalCost)
                .containsExactlyInAnyOrder(new BigDecimal("10.00"), new BigDecimal("15.00"));
    }

    @Test
    void shouldFindByAccountEnvironment() {
        // Given
        UsageRecord record = createUsageRecord(LocalDate.now(), new BigDecimal("20.00"));
        usageRecordRepository.save(record);
        entityManager.flush();

        // When
        List<UsageRecord> results =
                usageRecordRepository.findByAccount_Environment(Account.Environment.PRODUCTION);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAccount().getEnvironment())
                .isEqualTo(Account.Environment.PRODUCTION);
    }

    @Test
    void shouldTestBackwardCompatibilityGetTeamName() {
        // Given
        UsageRecord record = createUsageRecord(LocalDate.now(), new BigDecimal("25.00"));
        UsageRecord saved = usageRecordRepository.save(record);

        // When
        String teamName = saved.getTeamName();

        // Then
        assertThat(teamName).isEqualTo("test-team");
    }

    private UsageRecord createUsageRecord(LocalDate usageDate, BigDecimal totalCost) {
        UsageRecord record = new UsageRecord();
        record.setTeam(team);
        record.setService(service);
        record.setAccount(account);
        record.setUsageDate(usageDate);
        record.setUsageHour(12);
        record.setUsageQuantity(new BigDecimal("1.0"));
        record.setUnitPrice(totalCost);
        record.setTotalCost(totalCost);
        record.setCurrency("USD");
        record.setRegion("us-east-1");
        record.setResourceType("Instance");
        record.setResourceId("i-test");
        record.setUsageUnit("Instance-Hours");
        record.setCreatedAt(LocalDateTime.now());
        return record;
    }
}
