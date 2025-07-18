# Cloud Infrastructure Cost Tracking Dashboard
> Cloud infrastructure costs can quickly become difficult to manage as engineering teams grow and operate independently. Resources are provisioned across environments without centralized tracking, and costs spread across compute, storage, and networking services. Without clear visibility, organizations risk overspending and struggle to identify where waste is occurring and how to address it effectively.

This project simulates a full-stack dashboard that addresses this challenge. It ingests mock usage data for multiple teams, calculates estimated cloud costs, and displays time-based breakdowns through a React UI. The backend is modular, containerized, and structured to reflect how a real cost-tracking system could be deployed and scaled.

## Key Capabilities 
This project replicates the core components of a cloud cost monitoring system, focused on team-level usage visibility and simplified cost breakdowns. While built with mock data, each feature mirrors the functionality you'd expect in a real infrastructure reporting tool, making it useful for understanding the architecture and tradeoffs behind cost tracking at scale.

**Key capabilities include**:
- **Usage Data Ingestion**: Parses structured usage data (e.g., CPU hours, storage used) from CSV files, simulating the way infrastructure usage is logged in real cloud environments.

- **Cost Calculation Engine**: Estimates resource costs by applying fixed pricing rules, similar to cloud billing models (e.g., per vCPU/hour or per GB stored).

- **Team Attribution**: Organizes usage records by engineering team, enabling spend analysis at the organizational unit level, which are critical for chargebacks or budget enforcement.

- **Time-Range Filtering**: Allows cost reports to be generated for specific time windows, supporting common workflows like monthly spend reviews or spike detection.

- **Frontend Dashboard**: React UI that displays usage trends, cost summaries, and team-level breakdowns in a clean, responsive layout.

- **Modular Backend Design**: The backend separates ingestion, computation, and reporting into dedicated Spring Boot services to reflect scalable service boundaries.

- **Containerized & Deployable**: The full stack is containerized with Docker and includes Kubernetes manifests, enabling deployment simulation in a cloud-native environment.

## Architecture Overview
The system follows a modular full-stack architecture that mirrors how cloud cost platforms are typically structured in production environments. The backend is responsible for simulating usage ingestion, cost computation, and reporting, while the frontend visualizes that data in an interactive dashboard.

**← COMPONENTS →**
- Frontend (React)
  - Renders cost breakdowns, usage summaries, and team-level insights
  - Sends API requests to the backend to retrieve filtered reports
  - Built with reusable components for charts, tables, and filters
- Backend (Spring Boot)
  - Ingestion Service: Parses sample usage data from CSV into memory (or a stubbed data layer)
  - Cost Calculation Service: Applies logic to convert usage into estimated costs
  - Report Generation Service: Aggregates data by team and time window, returning it via a REST API
- Data Layer
  - Uses static CSV as a mock data source for usage records
  - Simulates team ownership, resource types, and usage over time
- Containerization & Deployment
  - Both frontend and backend are containerized with Docker
  - Includes Kubernetes manifests (`/k8s`) for simulating multi-service orchestration

**← DATA FLOW →**
```bash
CSV File → Ingestion Service → Cost Calculation → Report Aggregation → API → React UI
```
Each backend module is designed as an independent service to reflect how real-world cost systems are often decomposed for scalability and maintainability.

## Tech Stack
| **Category**         | **Tools / Technologies**                         |
| -------------------- | ------------------------------------------------ |
| **Frontend**         | React, JavaScript (ES6+), Axios, HTML, CSS       |
| **Backend**          | Spring Boot, Java 17, Spring Data JPA, Thymeleaf |
| **Testing**          | JUnit, Mockito                                   |
| **Data Source**      | CSV (sample usage data)                          |
| **Database**         | MySQL                                            |
| **Containerization** | Docker, Docker Compose                           |
| **Deployment**       | Kubernetes (YAML manifests)                      |

## Installation and Setup
Follow the steps below to run the full-stack dashboard locally using Docker or manually with separate backend and frontend setups.

**← OPTION 1: Run with Docker Compose (Recommended) →**
```bash
# Clone the repository
git clone https://github.com/KatavinaNguyen/cloud-infrastructure-cost-tracking-dashboard.git
cd cloud-infrastructure-cost-tracking-dashboard

# Start the full stack (backend + frontend + MySQL)
docker-compose up --build
```

- Backend will be available at: `http://localhost:8080`
- Frontend UI will be available at: `http://localhost:3000`

**← OPTION 2: Run Manually (Backend + Frontend Separately) →**

Start the Backend

  ```bash
    cd backend
    ./mvnw spring-boot:run
  ```
    
- Make sure MySQL is running locally and matches the credentials in `application.properties`.
     
Start the Frontend

  ```bash
    cd frontend
    npm install
    npm start
  ```
    
- The React frontend will auto-open at `http://localhost:3000`.

## Running with Docker 
You can run the entire application stack — backend, frontend, and MySQL — using Docker and Docker Compose. This is the fastest way to simulate a real deployment environment.
  ```bash
    # Navigate to the project root
    cd cloud-infrastructure-cost-tracking-dashboard
    
    # Build and start all services
    docker-compose up --build
  ```

| Service      | URL                     | Description                     |
| ------------ | ----------------------- | ------------------------------- |
| **Frontend** | `http://localhost:3000` | React dashboard UI              |
| **Backend**  | `http://localhost:8080` | Spring Boot REST API            |
| **Database** | `localhost:3306`        | MySQL instance with sample data |

The `docker-compose.yml` file includes:
- `backend`: Spring Boot application containerized with OpenJDK
- `frontend`: React app built and served via Nginx
- `mysql`: MySQL database with sample schema and usage data from `init-db.sql`

```bash
  # Stop all running containers
  docker-compose down
```

## Kubernetes Deployment
You can deploy the full application stack to a local or remote Kubernetes cluster using the provided YAML manifests.

**Included Manifests (`/k8s`)**
| File                       | Purpose                                                      |
| -------------------------- | ------------------------------------------------------------ |
| `backend-deployment.yaml`  | Deploys the Spring Boot backend                              |
| `frontend-deployment.yaml` | Deploys the React frontend (via Nginx)                       |
| `service.yaml`             | Exposes both frontend and backend services internally        |
| `configmap.yaml`           | Injects environment variables for backend configuration      |
| `ingress.yaml`             | Optional ingress configuration (requires ingress controller) |

**Deploy to Local Clusters**
- `kubectl` should point to cluster: 
  ```bash
    # Apply all manifests
    kubectl apply -f k8s/
  ```

**Accessing the App**
- If using Minikube: 
  ```bash
    # Expose the frontend service
    minikube service frontend-service
  ```
- Or use port-forwarding: 
  ```bash
    # Forward frontend to port 3000
    kubectl port-forward svc/frontend-service 3000:80
    
    # Forward backend to port 8080
    kubectl port-forward svc/backend-service 8080:8080
  ```

## Sample Data
The application uses mock infrastructure usage data to simulate cost reporting across multiple engineering teams. This data is stored in a CSV file and loaded during startup.

**File Location**
```bash
  backend/src/main/resources/data/sample-usage.csv
```

**Data Format**
Each row in the CSV represents a usage record for a specific team and resource type:

| team       | resource\_type | usage\_amount | timestamp            |
| ---------- | -------------- | ------------- | -------------------- |
| team-alpha | compute        | 120.5         | 2024-05-01T10:00:00Z |
| team-beta  | storage        | 300.0         | 2024-05-01T10:00:00Z |
| team-alpha | compute        | 140.0         | 2024-05-02T10:00:00Z |

- **team** – Name of the engineering team using the resource
- **resource_type** – Type of resource (e.g. compute, storage, bandwidth)
- **usage_amount** – Quantity of resource consumed (e.g. vCPU hours, GB stored)
- **timestamp** – When the resource usage occurred

**How It’s Used**
- The **ingestion service** reads and parses the CSV on startup
- The **cost calculation service** applies basic pricing logic (e.g. $0.05/vCPU-hour)
- The **reporting service** aggregates this data into cost reports by team and date

## Future Improvements
This project is intended as a prototype for exploring how engineering organizations might structure a cost tracking system. While it simulates core concepts effectively, there are several areas where the system could be extended or made more realistic:

- **Dynamic Data Ingestion**: Replace static CSV files with real-time data ingestion from cloud billing APIs or usage logs.
- **Persistent Storage**: Integrate a real database layer with write/read operations instead of relying on in-memory or mock data.
- **Authentication & Role-Based Access**: Add login functionality and support for multiple user roles (e.g., engineers, finance teams, admins).
- **Advanced Cost Modeling**: Support more granular pricing structures, tiered billing, reserved instances, and usage discounts.
- **Alerting & Budgets**: Allow users to define cost thresholds and receive alerts when team spend approaches limits.
- **Environment Awareness**: Break down usage not just by team, but also by environment (e.g., dev, staging, production).
- **Cloud Provider Integration**: Simulate or connect to provider-specific APIs (e.g., AWS Cost Explorer, GCP Billing) to model actual cost mappings.

## License 
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
