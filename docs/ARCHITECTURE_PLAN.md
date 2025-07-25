# Cloud Infrastructure Cost Tracking Dashboard - Architecture Plan

## Overview

This document outlines the architecture and implementation plan for enhancing the cloud cost tracking dashboard to provide a realistic simulation of enterprise cloud cost management platforms like AWS Cost Explorer, Azure Cost Management, and GCP Cloud Billing.

## Goals

1. Create a fully functional multi-cloud cost tracking dashboard
2. Implement realistic data models that mirror actual cloud provider billing
3. Provide interactive visualizations and actionable insights
4. Support team-based cost allocation and chargeback scenarios
5. Enable cost optimization recommendations

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Frontend (React)                          │
│  ┌─────────────┐ ┌──────────────┐ ┌────────────────────────┐  │
│  │  Dashboard  │ │   Reports    │ │   Cost Explorer        │  │
│  │  Overview   │ │  Generator   │ │   & Visualizations     │  │
│  └─────────────┘ └──────────────┘ └────────────────────────┘  │
└─────────────────────────────┬───────────────────────────────────┘
                              │ REST API
┌─────────────────────────────┴───────────────────────────────────┐
│                     Backend (Spring Boot)                        │
│  ┌─────────────┐ ┌──────────────┐ ┌────────────────────────┐  │
│  │   Cost      │ │   Usage      │ │    Recommendation      │  │
│  │ Calculation │ │  Ingestion   │ │      Engine            │  │
│  │  Service    │ │   Service    │ │                        │  │
│  └─────────────┘ └──────────────┘ └────────────────────────┘  │
│  ┌─────────────┐ ┌──────────────┐ ┌────────────────────────┐  │
│  │  Reporting  │ │  Alerting    │ │    Authentication      │  │
│  │  Service    │ │   Service    │ │      Service           │  │
│  └─────────────┘ └──────────────┘ └────────────────────────┘  │
└─────────────────────────────┬───────────────────────────────────┘
                              │
┌─────────────────────────────┴───────────────────────────────────┐
│                      Data Layer (MySQL)                          │
│  ┌─────────────┐ ┌──────────────┐ ┌────────────────────────┐  │
│  │   Usage     │ │    Cost      │ │     Configuration      │  │
│  │   Records   │ │   History    │ │      & Metadata        │  │
│  └─────────────┘ └──────────────┘ └────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
```

## Data Model Design

### Core Entities

#### 1. CloudProvider

```sql
CREATE TABLE cloud_providers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100),
    icon_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 2. Account

```sql
CREATE TABLE accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    provider_id BIGINT NOT NULL,
    account_id VARCHAR(100) NOT NULL,
    account_name VARCHAR(255),
    environment ENUM('production', 'staging', 'development', 'testing'),
    status ENUM('active', 'suspended', 'terminated'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (provider_id) REFERENCES cloud_providers(id)
);
```

#### 3. Team

```sql
CREATE TABLE teams (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    department VARCHAR(100),
    cost_center VARCHAR(50),
    manager_email VARCHAR(255),
    budget_monthly DECIMAL(12,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 4. Service

```sql
CREATE TABLE services (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    provider_id BIGINT NOT NULL,
    service_code VARCHAR(100) NOT NULL,
    service_name VARCHAR(255),
    category VARCHAR(100),
    icon_url VARCHAR(255),
    FOREIGN KEY (provider_id) REFERENCES cloud_providers(id),
    UNIQUE KEY unique_provider_service (provider_id, service_code)
);
```

#### 5. UsageRecord

```sql
CREATE TABLE usage_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    resource_id VARCHAR(255),
    resource_name VARCHAR(255),
    resource_type VARCHAR(100),
    region VARCHAR(50),
    usage_date DATE NOT NULL,
    usage_hour INT,
    usage_quantity DECIMAL(20,6),
    usage_unit VARCHAR(50),
    raw_cost DECIMAL(12,6),
    tags JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id),
    FOREIGN KEY (team_id) REFERENCES teams(id),
    FOREIGN KEY (service_id) REFERENCES services(id),
    INDEX idx_usage_date (usage_date),
    INDEX idx_team_date (team_id, usage_date)
);
```

#### 6. CostAllocation

```sql
CREATE TABLE cost_allocations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usage_record_id BIGINT NOT NULL,
    allocation_type ENUM('direct', 'shared', 'overhead'),
    allocated_cost DECIMAL(12,6),
    allocation_percentage DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usage_record_id) REFERENCES usage_records(id)
);
```

#### 7. Budget

```sql
CREATE TABLE budgets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL,
    budget_type ENUM('monthly', 'quarterly', 'annual'),
    amount DECIMAL(12,2),
    start_date DATE,
    end_date DATE,
    alert_threshold_percent INT DEFAULT 80,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES teams(id)
);
```

## Backend Services Design

### 1. Usage Ingestion Service

- **Purpose**: Import usage data from CSV files or simulate API ingestion
- **Key Methods**:
  - `ingestCSVData(MultipartFile file)`
  - `validateUsageRecord(UsageRecordDTO record)`
  - `enrichWithMetadata(UsageRecord record)`
  - `processInBatch(List<UsageRecord> records)`

### 2. Cost Calculation Service

- **Purpose**: Calculate costs based on usage and pricing rules
- **Key Methods**:
  - `calculateCost(UsageRecord usage, PricingRule rule)`
  - `applyDiscounts(BigDecimal cost, Account account)`
  - `calculateReservedInstanceSavings()`
  - `projectMonthlyCost(Team team)`

### 3. Reporting Service

- **Purpose**: Generate various cost reports and analytics
- **Key Methods**:
  - `generateCostBreakdown(ReportRequest request)`
  - `getTopSpendingServices(String teamId, DateRange range)`
  - `calculateCostTrends(String teamId, int months)`
  - `comparePeriodsReport(DateRange period1, DateRange period2)`

### 4. Recommendation Engine

- **Purpose**: Provide cost optimization recommendations
- **Key Methods**:
  - `analyzeUnusedResources()`
  - `recommendRightSizing()`
  - `suggestReservedInstances()`
  - `identifyCostAnomalies()`

### 5. Alert Service

- **Purpose**: Monitor budgets and send alerts
- **Key Methods**:
  - `checkBudgetThresholds()`
  - `detectCostSpikes()`
  - `sendAlertNotification(Alert alert)`

## API Endpoints

### Cost Reports

- `GET /api/v1/costs/overview` - Dashboard overview data
- `GET /api/v1/costs/breakdown` - Cost breakdown by service/team/tag
- `GET /api/v1/costs/trends` - Historical cost trends
- `GET /api/v1/costs/forecast` - Cost forecasting

### Teams & Accounts

- `GET /api/v1/teams` - List all teams
- `GET /api/v1/teams/{id}/costs` - Team-specific costs
- `GET /api/v1/accounts` - List all accounts
- `GET /api/v1/accounts/{id}/usage` - Account usage details

### Recommendations

- `GET /api/v1/recommendations` - Cost optimization recommendations
- `GET /api/v1/recommendations/savings` - Potential savings summary

### Budgets & Alerts

- `GET /api/v1/budgets` - List budgets
- `POST /api/v1/budgets` - Create budget
- `GET /api/v1/alerts` - List active alerts

### Data Management

- `POST /api/v1/usage/import` - Import usage data
- `GET /api/v1/usage/export` - Export cost data

## Frontend Components

### 1. Dashboard Overview

- **Total Spend Card**: Current month spend with trend
- **Budget Status**: Progress bars for each team's budget
- **Top Services**: Pie chart of top 5 services by cost
- **Cost Trend**: Line chart showing last 6 months
- **Alerts Panel**: Active cost alerts and anomalies

### 2. Cost Explorer

- **Interactive Filters**:
  - Date range picker
  - Team multi-select
  - Service multi-select
  - Tag filters
  - Grouping options (by service, team, tag, region)
- **Visualizations**:
  - Stacked area chart for cost over time
  - Treemap for hierarchical cost breakdown
  - Heatmap for daily cost patterns

### 3. Reports Section

- **Saved Reports**: List of saved report configurations
- **Report Builder**: Drag-and-drop report designer
- **Export Options**: PDF, CSV, Excel formats
- **Scheduled Reports**: Email delivery configuration

### 4. Recommendations

- **Savings Opportunities**: Cards showing potential savings
- **Resource Optimization**: List of underutilized resources
- **Reserved Instance Planner**: RI recommendation calculator

### 5. Team Management

- **Team List**: DataGrid with cost summaries
- **Team Details**: Detailed cost breakdown per team
- **Chargeback Reports**: Generate chargeback invoices

## Sample Data Strategy

### Data Generation Requirements

1. **Multi-Cloud**: AWS, Azure, GCP services
2. **Time Range**: 6 months of historical data
3. **Teams**: 8-10 engineering teams
4. **Services**: 15-20 services per provider
5. **Patterns**:
   - Weekly patterns (lower weekend usage)
   - Monthly growth trends
   - Occasional spikes (deployments, batch jobs)
   - Seasonal variations

### Sample Scenarios

1. **Development Team**: High compute usage during business hours
2. **Data Analytics Team**: Large storage and data processing costs
3. **Platform Team**: Consistent infrastructure costs
4. **Mobile Team**: CDN and API gateway usage
5. **Security Team**: Logging and monitoring services

## Implementation Phases

### Phase 1: Core Infrastructure (Week 1)

- [ ] Update database schema
- [ ] Implement basic data models
- [ ] Create sample data generator
- [ ] Basic CRUD APIs

### Phase 2: Cost Engine (Week 2)

- [ ] Implement cost calculation logic
- [ ] Add pricing rules for major services
- [ ] Create aggregation queries
- [ ] Build reporting APIs

### Phase 3: Frontend Foundation (Week 3)

- [ ] Setup component library (Material-UI or Ant Design)
- [ ] Create layout and navigation
- [ ] Build dashboard overview
- [ ] Implement basic charts

### Phase 4: Advanced Features (Week 4)

- [ ] Cost Explorer with filters
- [ ] Recommendation engine
- [ ] Budget management
- [ ] Alert system

### Phase 5: Polish & Testing (Week 5)

- [ ] Add loading states and error handling
- [ ] Implement caching
- [ ] Write tests
- [ ] Documentation

## Technology Decisions

### Backend

- **Framework**: Spring Boot 3.x
- **Database**: MySQL 8.0
- **Caching**: Spring Cache with Caffeine
- **API Documentation**: OpenAPI 3.0 (Swagger)

### Frontend

- **Framework**: React 18
- **State Management**: Redux Toolkit or Zustand
- **UI Library**: Material-UI v5
- **Charts**: Recharts or Chart.js
- **HTTP Client**: Axios
- **Routing**: React Router v6

### DevOps

- **Containerization**: Docker
- **Orchestration**: Kubernetes manifests
- **CI/CD**: GitHub Actions
- **Monitoring**: Prometheus + Grafana (optional)

## Security Considerations

1. **Authentication**: JWT-based auth (simplified)
2. **Authorization**: Role-based access (Admin, Manager, Viewer)
3. **Data Security**: Encryption at rest for sensitive data
4. **API Security**: Rate limiting, CORS configuration

## Performance Targets

- Dashboard load time: < 2 seconds
- Report generation: < 5 seconds for 1 month of data
- API response time: < 500ms for most endpoints
- Support for 100k+ usage records per month

## Success Metrics

1. Functional dashboard with real-time data
2. Accurate cost calculations matching cloud provider logic
3. Intuitive UI comparable to commercial solutions
4. Comprehensive API coverage for all features
5. Scalable architecture supporting growth
