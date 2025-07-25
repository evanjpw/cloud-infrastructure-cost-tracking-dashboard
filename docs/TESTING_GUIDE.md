# Testing Guide

## Overview

This document provides comprehensive information about the test suite for the Cloud Infrastructure Cost Tracking Dashboard.

## Test Structure

The test suite is organized into multiple layers to ensure comprehensive coverage:

### 1. Unit Tests

- **Repository Tests**: `UsageRecordRepositoryTest`

  - Tests JPA repository methods and queries
  - Verifies backward compatibility methods (`getTeamName()`)
  - Uses H2 in-memory database for fast execution
  - Tests relationships and constraints

- **Service Tests**:

  - `CostCalculationServiceTest`: Tests cost calculation logic
  - `UsageIngestionServiceTest`: Tests data ingestion workflows

- **Utility Tests**: `SampleDataGeneratorTest`
  - Tests the sample data generation logic
  - Verifies base data creation (providers, teams, services, accounts)
  - Ensures realistic data generation patterns

### 2. Integration Tests

- **API Tests**: `CostReportApiTest`

  - Tests REST endpoints with real Spring context
  - Verifies JSON request/response handling
  - Tests error scenarios and edge cases

- **Controller Tests**: `CostReportControllerTest`
  - Tests controller layer with MockMvc
  - Mocks service dependencies
  - Focuses on HTTP layer behavior

### 3. Application Tests

- **Context Loading**: `CloudCostDashboardApplicationTests`
  - Ensures Spring Boot application starts correctly
  - Verifies bean configuration and autowiring

## Test Configuration

### Database Configuration

```properties
# H2 in-memory database for tests
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

### Test Profiles

- **Active Profile**: `test`
- **Sample Data Generator**: Disabled via `@Profile("!test")`

### Key Dependencies

- **H2 Database**: In-memory database for fast test execution
- **Spring Boot Test**: Testing framework and annotations
- **AssertJ**: Fluent assertions for better readability
- **MockMvc**: Web layer testing without full HTTP stack

## Running Tests

### All Tests

```bash
./mvnw test
```

### Specific Test Classes

```bash
./mvnw test -Dtest=UsageRecordRepositoryTest
./mvnw test -Dtest=CostReportControllerTest
./mvnw test -Dtest=SampleDataGeneratorTest
```

### Test Categories

```bash
# Repository layer tests
./mvnw test -Dtest="*Repository*"

# Service layer tests
./mvnw test -Dtest="*Service*"

# Controller layer tests
./mvnw test -Dtest="*Controller*"
```

## Test Data Management

### Repository Tests

- Use `@DataJpaTest` for focused JPA testing
- Create minimal test data in `@BeforeEach` methods
- Leverage `TestEntityManager` for precise data setup

### Integration Tests

- Use `@SpringBootTest` for full application context
- Create comprehensive test fixtures
- Test real database interactions and transactions

### Data Isolation

- Each test runs in its own transaction (rollback after test)
- H2 database recreated for each test class
- No shared state between tests

## Assertions and Verification

### Repository Tests

```java
// Test data persistence
assertThat(saved.getId()).isNotNull();
assertThat(saved.getTotalCost()).isEqualByComparingTo(expected);

// Test queries
assertThat(results).hasSize(2);
assertThat(results).extracting(UsageRecord::getTotalCost)
    .containsExactlyInAnyOrder(cost1, cost2);
```

### API Tests

```java
// Test HTTP responses
mockMvc.perform(post("/api/reports")
    .contentType(MediaType.APPLICATION_JSON)
    .content(requestBody))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.breakdowns").exists());
```

## Test Coverage

### Current Coverage

✅ **Repository Layer**: Comprehensive JPA repository testing
✅ **Service Layer**: Business logic testing with mocks
✅ **Controller Layer**: HTTP layer testing with MockMvc
✅ **Utility Classes**: Sample data generation testing
✅ **Integration**: End-to-end API testing

### Areas for Enhancement

- **Frontend Component Testing**: React component tests with Jest/RTL
- **End-to-End Testing**: Full browser-based testing with realistic workflows
- **Performance Testing**: Load testing for large datasets
- **Security Testing**: Authentication and authorization testing

## Common Testing Patterns

### Test Data Creation

```java
private UsageRecord createUsageRecord(LocalDate date, BigDecimal cost) {
    UsageRecord record = new UsageRecord();
    record.setTeam(team);
    record.setService(service);
    record.setAccount(account);
    record.setUsageDate(date);
    record.setTotalCost(cost);
    // ... other required fields
    return record;
}
```

### Backward Compatibility Testing

```java
@Test
void shouldTestBackwardCompatibilityGetTeamName() {
    // Given
    UsageRecord record = createUsageRecord(LocalDate.now(), BigDecimal.TEN);
    UsageRecord saved = repository.save(record);

    // When
    String teamName = saved.getTeamName();

    // Then
    assertThat(teamName).isEqualTo("expected-team-name");
}
```

### API Integration Testing

```java
@Test
void shouldGenerateCostReport() throws Exception {
    // Given
    Map<String, Object> request = Map.of(
        "teamName", "test-team",
        "startDate", "2025-01-01",
        "endDate", "2025-01-31"
    );

    // When & Then
    mockMvc.perform(post("/api/reports")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpected(jsonPath("$.breakdowns").exists());
}
```

## Debugging Tests

### Enable SQL Logging

```properties
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Test Data Inspection

```java
@Test
void shouldInspectTestData() {
    // Create test data
    entityManager.flush(); // Force database write

    // Query and inspect
    List<UsageRecord> records = repository.findAll();
    records.forEach(System.out::println);
}
```

### MockMvc Request/Response Logging

```java
mockMvc.perform(post("/api/reports")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andDo(print()) // Prints full request/response
        .andExpect(status().isOk());
```

## Continuous Integration

### Test Execution Strategy

1. **Fast Tests First**: Unit tests run before integration tests
2. **Parallel Execution**: Repository tests run in parallel where possible
3. **Fail Fast**: Stop on first test failure in CI environment

### Test Reporting

- **Surefire Reports**: Generated in `target/surefire-reports/`
- **Coverage Reports**: Integration with JaCoCo for coverage analysis
- **Test Results**: XML and HTML reports for CI integration

## Best Practices

### Test Naming

- Use descriptive method names: `shouldFindUsageRecordsByTeamAndDateRange`
- Follow Given-When-Then structure in test body
- Use consistent naming patterns across test classes

### Test Organization

- Group related tests in inner classes when appropriate
- Use `@BeforeEach` for common setup
- Keep tests independent and isolated

### Assertions

- Use AssertJ for fluent, readable assertions
- Test both positive and negative cases
- Verify error conditions and edge cases

### Test Data

- Create minimal test data needed for each test
- Use factory methods for consistent data creation
- Avoid shared mutable test data

---

_Last Updated: July 26, 2025_
_Test Suite Status: Comprehensive unit and integration testing implemented_
