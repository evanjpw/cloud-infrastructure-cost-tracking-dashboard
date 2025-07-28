# Cloud Infrastructure Cost Tracking Dashboard

This is a simulation of a generic cloud cost monitoring system. It's designed to be similar to the
billing & cost exporers of platforms like AWS, Azure, & GCP, but uses mock data.

This began as a fork of [https://github.com/evanjpw/cloud-infrastructure-cost-tracking-dashboard](https://github.com/evanjpw/cloud-infrastructure-cost-tracking-dashboard), which is described this way

> This project replicates the core components of a cloud cost monitoring system, focused on team-level
> usage visibility and simplified cost breakdowns. While built with mock data, each feature mirrors the
> functionality you'd expect in a real infrastructure reporting tool, making it useful for understanding
> the architecture and tradeoffs behind cost tracking at scale.

This project is a _substantial rewrite_ of the original [cloud-infrastructure-cost-tracking-dashboard](https://github.com/evanjpw/cloud-infrastructure-cost-tracking-dashboard) by [KatavinaNguyen](https://github.com/KatavinaNguyen). I deeply appreciate their initial vision & foundation work that inspired this project.

**What started as a fork has become a rebuild.** While I maintain the same core concept of a multi-cloud cost tracking simulator, most components have been rewritten, featuring:

- Spring Boot backend with relational database
- AWS Cost Explorer-inspired interface with responsive design
- 20,000+ realistic usage records with working data visualization
- Works seamlessly across desktop, tablet, and mobile devices

## Quick Start

```bash
# Clone the repository
git clone https://github.com/evanjpw/cloud-infrastructure-cost-tracking-dashboard.git
cd cloud-infrastructure-cost-tracking-dashboard

# Start the full stack (takes ~2 minutes for initial build)
docker-compose up --build

# Access the application
# Frontend: http://localhost:3000
# Backend API: http://localhost:8080
# Database: localhost:3306 (root/password123)
```

The system automatically generates **20,000+ realistic usage records** spanning 6 months across multiple teams, cloud providers, and services.

## Key Features

### **Phase 1 & 2 Complete: Professional Dashboard with Full Cost Management** 
*Enterprise-grade cost management platform with budgets, optimization, and reporting*

#### **Modern Dashboard Design**
- **AWS/Azure-inspired interface** with professional sidebar navigation
- **Responsive design** that works seamlessly on desktop, tablet, and mobile
- **Professional color scheme** with consistent blue/gray branding
- **Typography system** with proper hierarchy and readability

#### **Enhanced Data Visualization**
- **Chart.js integration** with professional styling and animations
- **Interactive charts**: Line charts for trends, pie charts for service breakdowns
- **Real-time updates** that respond instantly to filter changes
- **Gradient backgrounds** and smooth hover effects

#### **Advanced Filtering & Search**
- **Multi-select team filter** with search functionality and count indicators
- **Multi-select service filter** with dynamic options based on available data
- **Quick search bar** with intelligent suggestions across services, teams, and costs
- **Saved views** feature with localStorage persistence for rapid filter switching
- **Real-time filtering** that updates all visualizations instantly
- **CSV export functionality** with multiple export options (raw data, trend data, summary)
- **Group-by dimensions** supporting service, team, provider, region, environment, and resource type
- **Cost type toggle** for actual, amortized, and blended cost views

#### **Professional KPI Dashboard**
- **6 key performance indicators** with status indicators and trend analysis:
  - Current Spend with period-over-period changes
  - Daily Average across selected time periods
  - Top Cost Driver identification with percentage breakdown
  - Budget Status with remaining amounts and risk indicators
  - Cost Forecasting with confidence levels and projections
  - Cost Efficiency metrics with per-service breakdowns
- **Interactive KPI cards** with hover effects and click handlers
- **Mobile-optimized grid layout** with responsive behavior

#### **Multi-Page Navigation Structure**
- **React Router integration** with client-side routing for 5 distinct pages:
  - **Dashboard** (`/dashboard`) - Main cost overview with full functionality
  - **Cost Optimization** (`/cost-analysis`) - AI-powered recommendations and anomaly detection
  - **Budgets** (`/budgets`) - Complete budget management with tracking and alerts
  - **Reports** (`/reports`) - Custom report builder, executive dashboard, and exports
  - **Settings** (`/settings`) - Configuration and preferences
- **Active navigation states** with proper page highlighting
- **Professional page layouts** with consistent headers and descriptions

### **Phase 2.1 Complete: Budget Management**
*Comprehensive budget tracking and forecasting system*

- **Budget Creation & Management**: Set budgets by team, service, or organization-wide
- **Real-time Tracking**: Visual progress indicators with spend vs. budget
- **Forecasting**: Intelligent projections based on spending patterns
- **Alert Thresholds**: Configurable warning levels (60%, 80%, 90%, 100%)
- **Budget Cards**: Professional display with interactive elements
- **Data Persistence**: LocalStorage integration with sample budgets

### **Phase 2.2 Complete: Cost Optimization**
*AI-powered recommendations and anomaly detection*

- **Optimization Engine**: Analyzes cost data for savings opportunities
- **Right-sizing Analysis**: Detects over-provisioned resources
- **Reserved Instance Planning**: ROI calculations for RI purchases
- **Unused Resource Detection**: Identifies orphaned or underutilized services
- **Anomaly Detection**: Statistical analysis (2σ threshold) for spending spikes
- **Recommendation Cards**: Expandable details with implementation steps
- **Optimization Summary**: Potential savings, ROI metrics, and roadmap

### **Phase 2.3 Complete: Reporting & Analytics**
*Custom report generation with executive insights*

- **Report Builder**: Create custom reports with 6 report types
  - Cost Summary, Detailed Breakdown, Executive Summary
  - Budget Performance, Cost Optimization, Chargeback
- **Export Formats**: PDF, Excel, CSV, JSON with download functionality
- **Executive Dashboard**: High-level metrics and strategic insights
  - Total spend with growth trends
  - Budget utilization across organization
  - Top cost drivers and team performance
  - Risk factor analysis and recommendations
- **Report Viewer**: Interactive report display with sharing options
- **Scheduling Options**: Daily, weekly, monthly, quarterly automation

### Simulates Multi-Cloud Support

- **AWS**: EC2, S3, RDS, Lambda, VPC, CloudFront, EKS, etc.
- **Azure**: Virtual Machines, Blob Storage, SQL Database, Functions, etc.
- **GCP**: Compute Engine, Cloud Storage, Cloud SQL, Cloud Functions, etc.

### Advanced Cost Modeling

- **Realistic Pricing**: Service-specific hourly rates with regional variations
- **Time-based Adjustments**: Business hours premiums and seasonal spikes
- **Granular Tracking**: Hourly usage data with comprehensive tagging
- **Multi-currency Support**: USD with decimal precision
- **Real-time Calculations**: KPI metrics with forecasting and trend analysis

### Organization Management

- **Team-based Cost Allocation**: 5 pre-configured teams (platform, frontend, backend, data, ml)
- **Multi-team Filtering**: Select multiple teams simultaneously for comparative analysis
- **Environment Separation**: Production, staging, and development accounts
- **Department Tracking**: Cost centers and manager attribution
- **Custom Tagging**: Flexible key-value tags for resources

### Comprehensive Data

- **20,000+ Usage Records**: 6 months of realistic hourly data
- **24 Cloud Services**: 8 services per provider with proper categorization
- **9 Account Environments**: Full prod/staging/dev separation per provider
- **Relational Integrity**: Proper foreign key relationships throughout
- **Filter Persistence**: Save and restore complex filter combinations

## Architecture

### System Components

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  React Frontend │    │  Spring Boot API │    │   MySQL 8.0     │
│   (Port 3000)   │◄──►│   (Port 8080)    │◄──►│  (Port 3306)    │
│                 │    │                  │    │                 │
│ • Material-UI   │    │ • JPA/Hibernate  │    │ • Relational    │
│ • Responsive    │    │ • REST APIs      │    │ • Sample Data   │
│ • Charting      │    │ • Auto Data Gen  │    │ • Constraints   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### Database Schema

```sql
-- Core relational schema with proper foreign keys
cloud_providers → services
                → accounts → usage_records ← teams
                           ↓
                    usage_record_tags
```

### Tech Stack

| Layer              | Technologies                                  |
| ------------------ | --------------------------------------------- |
| **Frontend**       | React 19.1.0, Chart.js 4.4.0, React Router 7.7.1, Axios, Nginx |
| **Backend**        | Spring Boot 3.5.3, Spring Data JPA, Java 17   |
| **Database**       | MySQL 8.0 with comprehensive schema           |
| **UI Components** | Custom components with professional styling, responsive design |
| **Charts**        | Chart.js with gradients, animations, and interactive tooltips |
| **Navigation**    | React Router with active states and client-side routing |
| **Infrastructure** | Docker Compose, Multi-stage builds            |
| **Development**    | Hot reload, SQL logging, Auto data generation, ESLint |

## 🚀 Current Capabilities

### Fully Implemented Features (Phases 1 & 2 Complete)

#### **Phase 1: Professional Dashboard UI/UX**
- **Professional Dashboard Interface** with AWS/Azure-inspired design
- **Advanced Multi-Select Filtering** for teams and services with search
- **Real-time Data Visualization** using Chart.js with professional styling
- **KPI Dashboard** with 6 key metrics including forecasting and trend analysis
- **Quick Search** with intelligent suggestions across services, teams, and costs
- **Saved Views** with localStorage persistence for rapid filter switching
- **Multi-Page Navigation** with React Router and 5 distinct page sections
- **Responsive Design** that works seamlessly across desktop, tablet, and mobile
- **Interactive Charts** with hover details, gradients, and smooth animations
- **Data Aggregation** with daily/weekly/monthly granularity controls
- **CSV Export System** with raw data, trend data, and summary export options
- **Multi-Dimensional Grouping** by service, team, provider, region, environment, and resource type
- **Cost Type Selection** supporting actual, amortized, and blended cost methodologies

#### **Phase 2: Cost Management & Optimization**
- **Budget Management System** with creation, tracking, and alert functionality
  - Multi-scope budgets (team, service, organization-wide)
  - Real-time spend tracking with visual progress indicators
  - Budget forecasting and alert thresholds
  - Professional budget cards with status indicators
- **AI-Powered Cost Optimization** with intelligent recommendations
  - Right-sizing analysis for over-provisioned resources
  - Reserved Instance planning with ROI calculations
  - Unused resource detection and cleanup recommendations
  - Anomaly detection with statistical threshold analysis
  - Interactive recommendation cards with implementation steps
  - Optimization summary with potential savings and timeline estimates

### Backend Services

- **Multi-cloud cost tracking** across AWS, Azure, and GCP
- **Relational database schema** with proper foreign key constraints
- **Realistic sample data generator** with 20,000+ usage records spanning 6 months
- **Team-based cost allocation** and organizational tracking
- **REST API endpoints** for usage queries and reporting with team filtering
- **Docker containerization** with persistent data storage and hot reload
- **Comprehensive logging** and debugging capabilities

### Frontend Dashboard (Fully Implemented)

- **Modern React 19.1.0 interface** with custom professional components
- **Chart.js 4.4.0 integration** with gradients, animations, and responsive behavior
- **React Router 7.7.1** for client-side navigation with active states
- **Advanced filtering system** with multi-select dropdowns and quick search
- **KPI calculation engine** with real-time metrics and trend analysis
- **Professional color system** inspired by AWS Cost Explorer and Azure Cost Management
- **Typography system** with consistent hierarchy and readability
- **Mobile-first responsive design** with collapsible navigation
- **Data persistence** for user preferences and saved filter combinations

## Development Workflow

### Local Development

```bash
# Start with auto-reload
docker-compose up --build

# View logs
docker logs cloud-cost-backend
docker logs cloud-cost-frontend

# Database access
docker exec -it mysql-db mysql -u root -ppassword123 cloud_costs
```

### Making Changes

```bash
# Backend changes: Edit Java files and restart container
# Frontend changes: Hot reload automatically applies
# Database changes: Modify entities and use create-drop mode
```

### Useful Commands

```sql
-- Check generated data
SELECT COUNT(*) FROM usage_records;
SELECT name, COUNT(*) as records FROM teams t
JOIN usage_records ur ON t.id = ur.team_id GROUP BY name;

-- View recent usage
SELECT * FROM usage_records ORDER BY created_at DESC LIMIT 10;
```

## 🧪 Testing & Quality

### Current Test Coverage

- **Spring Boot testing framework** integrated
- **Sample data fixtures** for consistent testing
- **Integration test support** with H2 database
- **Docker test environment** for full stack testing

### Planned Improvements

- **API endpoint testing** with MockMvc
- **Database integration tests** with Testcontainers
- **Frontend component testing** with Jest/React Testing Library
- **End-to-end testing** with realistic data scenarios

## Deployment

### Docker Compose (Development)

```bash
docker-compose up --build
# All services start automatically with persistent data
```

### Kubernetes (Production-ready)

```bash
# Quick deployment
cd k8s/
./deploy.sh

# Manual deployment
kubectl apply -f k8s/

# Access the application
# Add to /etc/hosts: <INGRESS_IP> cloud-cost.local
# Visit: http://cloud-cost.local

# Cleanup
./undeploy.sh
```

### Configuration

- **Environment variables** for database credentials
- **Application profiles** for different environments
- **Persistent volumes** for data storage
- **Health checks** and readiness probes

## Roadmap

For detailed roadmap information, see [docs/ROADMAP.md](docs/ROADMAP.md)

## Contributing

This project follows standard Spring Boot and React development practices:

1. **Fork the repository** and create a feature branch
2. **Make changes** following existing code patterns
3. **Test thoroughly** with Docker Compose
4. **Document changes** in relevant markdown files
5. **Submit pull request** with clear description

## License

MIT - See [LICENSE](LICENSE) file for details.

---

**Last Updated**: July 28, 2025
**Version**: 2.2.0 (Phase 1 & 2 Complete)
**Phase Status**: Phase 2 Cost Management & Optimization - 100% Complete
**Next Phase**: Phase 2.3 Reporting & Analytics (Q4 2025)
**Original Inspiration**: [cloud-infrastructure-cost-tracking-dashboard](https://github.com/evanjpw/cloud-infrastructure-cost-tracking-dashboard) - Thank you to [KatavinaNguyen](https://github.com/KatavinaNguyen)
**Current Maintainer**: [evanjpw](https://github.com/evanjpw)

---
