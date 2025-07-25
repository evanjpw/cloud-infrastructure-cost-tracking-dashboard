# Cloud Infrastructure Cost Tracking Dashboard

This is a simulation of a generic cloud cost monitoring system. It's designed to be similar to the
billing & cost exporers of platforms like AWS, Azure, & GCP, but uses mock data.

This began as a fork of [https://github.com/evanjpw/cloud-infrastructure-cost-tracking-dashboard](https://github.com/evanjpw/cloud-infrastructure-cost-tracking-dashboard), which is described this way

> This project replicates the core components of a cloud cost monitoring system, focused on team-level
> usage visibility and simplified cost breakdowns. While built with mock data, each feature mirrors the
> functionality you'd expect in a real infrastructure reporting tool, making it useful for understanding
> the architecture and tradeoffs behind cost tracking at scale.

This project is a _substantial rewrite_ of the original [cloud-infrastructure-cost-tracking-dashboard](https://github.com/evanjpw/cloud-infrastructure-cost-tracking-dashboard) by [KatavinaNguyen](https://github.com/KatavinaNguyen). I deeply appreciate their initial vision & foundation work that inspired this project.

**What started as a fork has become a rebuild.** While I maintain the same core concept of a multi-cloud cost tracking simulator, most components has been rewritten, featuring:

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

### Simulates Multi-Cloud Support

- **AWS**: EC2, S3, RDS, Lambda, VPC, CloudFront, EKS, etc.
- **Azure**: Virtual Machines, Blob Storage, SQL Database, Functions, etc.
- **GCP**: Compute Engine, Cloud Storage, Cloud SQL, Cloud Functions, etc.

### Advanced Cost Modeling

- **Realistic Pricing**: Service-specific hourly rates with regional variations
- **Time-based Adjustments**: Business hours premiums and seasonal spikes
- **Granular Tracking**: Hourly usage data with comprehensive tagging
- **Multi-currency Support**: USD with decimal precision

### Organization Management

- **Team-based Cost Allocation**: 5 pre-configured teams (platform, frontend, backend, data, ml)
- **Environment Separation**: Production, staging, and development accounts
- **Department Tracking**: Cost centers and manager attribution
- **Custom Tagging**: Flexible key-value tags for resources

### Comprehensive Data

- **20,000+ Usage Records**: 6 months of realistic hourly data
- **24 Cloud Services**: 8 services per provider with proper categorization
- **9 Account Environments**: Full prod/staging/dev separation per provider
- **Relational Integrity**: Proper foreign key relationships throughout

## Architecture

### System Components

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   React Frontend │    │  Spring Boot API │    │   MySQL 8.0     │
│   (Port 3000)   │◄──►│   (Port 8080)   │◄──►│  (Port 3306)    │
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
| **Frontend**       | React, Material-UI, Axios, Nginx              |
| **Backend**        | Spring Boot 3.5.3, Spring Data JPA, Java 17   |
| **Database**       | MySQL 8.0 with comprehensive schema           |
| **Infrastructure** | Docker Compose, Multi-stage builds            |
| **Development**    | Hot reload, SQL logging, Auto data generation |

## Current Capabilities

### Implemented Features

- **Multi-cloud cost tracking** across AWS, Azure, and GCP
- **Relational database schema** with proper foreign key constraints
- **Realistic sample data generator** with 6 months of usage data
- **Team-based cost allocation** and organizational tracking
- **REST API endpoints** for usage queries and reporting
- **Docker containerization** with persistent data storage
- **Backward compatibility** with legacy field access patterns
- **Comprehensive logging** and debugging capabilities

### Backend Services

- **Usage ingestion** with automatic data generation
- **Cost calculation engine** with realistic pricing models
- **Report generation** with team and time-based filtering
- **JPA repositories** with custom query methods
- **Sample data seeding** for development and testing

### Frontend Dashboard

- **Modern React interface** with Material-UI components
- **Responsive design** for desktop and mobile
- **Interactive charts** and data visualization
- **Team filtering** and date range selection
- **Real-time data updates** from backend APIs

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

## 🚢 Deployment

### Docker Compose (Development)

```bash
docker-compose up --build
# All services start automatically with persistent data
```

### Kubernetes (Production-ready)

```bash
# Deploy to cluster
kubectl apply -f k8s/

# Access via port-forward
kubectl port-forward svc/frontend-service 3000:80
kubectl port-forward svc/backend-service 8080:8080
```

### Configuration

- **Environment variables** for database credentials
- **Application profiles** for different environments
- **Persistent volumes** for data storage
- **Health checks** and readiness probes

## Roadmap

TBD

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

**Last Updated**: July 26, 2025
**Version**: 2.0.0
**Original Inspiration**: [cloud-infrastructure-cost-tracking-dashboard](https://github.com/evanjpw/cloud-infrastructure-cost-tracking-dashboard) - Thank you to [KatavinaNguyen](https://github.com/KatavinaNguyen)
**Current Maintainer**: [evanjpw](https://github.com/evanjpw)

---
