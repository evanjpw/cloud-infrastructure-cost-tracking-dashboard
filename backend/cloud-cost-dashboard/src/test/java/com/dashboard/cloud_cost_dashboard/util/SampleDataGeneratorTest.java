package com.dashboard.cloud_cost_dashboard.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.dashboard.cloud_cost_dashboard.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class SampleDataGeneratorTest {

    @Autowired private UsageRecordRepository usageRecordRepository;

    @Autowired private TeamRepository teamRepository;

    @Autowired private ServiceRepository serviceRepository;

    @Autowired private AccountRepository accountRepository;

    @Autowired private CloudProviderRepository cloudProviderRepository;

    private SampleDataGenerator sampleDataGenerator;

    @BeforeEach
    void setUp() {
        sampleDataGenerator = new SampleDataGenerator();
        sampleDataGenerator.usageRecordRepository = usageRecordRepository;
        sampleDataGenerator.teamRepository = teamRepository;
        sampleDataGenerator.serviceRepository = serviceRepository;
        sampleDataGenerator.accountRepository = accountRepository;
        sampleDataGenerator.cloudProviderRepository = cloudProviderRepository;
    }

    @Test
    void shouldCreateBaseDataIfNeeded() throws Exception {
        // Given - empty database
        assertThat(cloudProviderRepository.count()).isEqualTo(0);
        assertThat(teamRepository.count()).isEqualTo(0);
        assertThat(serviceRepository.count()).isEqualTo(0);
        assertThat(accountRepository.count()).isEqualTo(0);

        // When
        sampleDataGenerator.run();

        // Then
        assertThat(cloudProviderRepository.count()).isEqualTo(3); // AWS, Azure, GCP
        assertThat(teamRepository.count()).isEqualTo(5); // platform, frontend, backend, data, ml
        assertThat(serviceRepository.count()).isEqualTo(24); // 8 per provider
        assertThat(accountRepository.count()).isEqualTo(9); // 3 per provider

        // Verify specific data
        assertThat(cloudProviderRepository.findByName("aws")).isPresent();
        assertThat(cloudProviderRepository.findByName("azure")).isPresent();
        assertThat(cloudProviderRepository.findByName("gcp")).isPresent();

        assertThat(teamRepository.findByName("platform")).isPresent();
        assertThat(teamRepository.findByName("frontend")).isPresent();
        assertThat(teamRepository.findByName("backend")).isPresent();
        assertThat(teamRepository.findByName("data")).isPresent();
        assertThat(teamRepository.findByName("ml")).isPresent();
    }

    @Test
    void shouldSkipDataGenerationIfUsageRecordsExist() throws Exception {
        // Given - create some usage records first
        sampleDataGenerator.run(); // First run creates base data and usage records
        long initialCount = usageRecordRepository.count();
        assertThat(initialCount).isGreaterThan(0);

        // When - run again
        sampleDataGenerator.run();

        // Then - should not create more records
        assertThat(usageRecordRepository.count()).isEqualTo(initialCount);
    }

    @Test
    void shouldCreateRealisticUsageData() throws Exception {
        // When
        sampleDataGenerator.run();

        // Then
        assertThat(usageRecordRepository.count())
                .isGreaterThan(1000); // Should generate significant data

        // Verify data quality - check that records have proper relationships
        var records = usageRecordRepository.findAll();
        assertThat(records)
                .allSatisfy(
                        record -> {
                            assertThat(record.getTeam()).isNotNull();
                            assertThat(record.getService()).isNotNull();
                            assertThat(record.getAccount()).isNotNull();
                            assertThat(record.getUsageDate()).isNotNull();
                            assertThat(record.getTotalCost()).isNotNull();
                            assertThat(record.getTotalCost())
                                    .isGreaterThanOrEqualTo(java.math.BigDecimal.ZERO);
                        });

        // Test backward compatibility
        var firstRecord = records.get(0);
        assertThat(firstRecord.getTeamName()).isNotNull();
        assertThat(firstRecord.getServiceName()).isNotNull();
    }
}
