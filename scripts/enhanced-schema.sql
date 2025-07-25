-- Enhanced Cloud Cost Tracking Database Schema
-- Supports multi-cloud (AWS, Azure, GCP) cost tracking with team allocation

-- Drop existing tables if they exist
DROP TABLE IF EXISTS cost_allocations;
DROP TABLE IF EXISTS usage_records;
DROP TABLE IF EXISTS budgets;
DROP TABLE IF EXISTS budget_alerts;
DROP TABLE IF EXISTS services;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS teams;
DROP TABLE IF EXISTS cloud_providers;

-- Cloud Providers (AWS, Azure, GCP)
CREATE TABLE cloud_providers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100),
    icon_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Teams/Departments
CREATE TABLE teams (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(100),
    department VARCHAR(100),
    cost_center VARCHAR(50),
    manager_email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_team_name (`name`)
);

-- Cloud Accounts
CREATE TABLE accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    provider_id BIGINT NOT NULL,
    account_id VARCHAR(100) NOT NULL,
    account_name VARCHAR(255),
    environment ENUM(
        'production', 'staging', 'development', 'testing'
    ) DEFAULT 'production',
    `status` ENUM('active', 'suspended', 'terminated') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (provider_id) REFERENCES cloud_providers (id),
    UNIQUE KEY unique_provider_account (provider_id, account_id),
    INDEX idx_account_status (`status`)
);

-- Cloud Services
CREATE TABLE services (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    provider_id BIGINT NOT NULL,
    service_code VARCHAR(100) NOT NULL,
    service_name VARCHAR(255),
    category VARCHAR(100),
    icon_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (provider_id) REFERENCES cloud_providers (id),
    UNIQUE KEY unique_provider_service (provider_id, service_code),
    INDEX idx_service_category (category)
);

-- Usage Records (main cost data)
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
    usage_hour INT DEFAULT 0,
    usage_quantity DECIMAL(20, 6) DEFAULT 0,
    usage_unit VARCHAR(50),
    unit_price DECIMAL(12, 6) DEFAULT 0,
    total_cost DECIMAL(12, 6) DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    tags JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts (id),
    FOREIGN KEY (team_id) REFERENCES teams (id),
    FOREIGN KEY (service_id) REFERENCES services (id),
    INDEX idx_usage_date (usage_date),
    INDEX idx_team_date (team_id, usage_date),
    INDEX idx_service_date (service_id, usage_date),
    INDEX idx_resource_id (resource_id)
);

-- Cost Allocations (for shared resources)
CREATE TABLE cost_allocations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usage_record_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    allocation_type ENUM('direct', 'shared', 'overhead') DEFAULT 'direct',
    allocated_cost DECIMAL(12, 6),
    allocation_percentage DECIMAL(5, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usage_record_id) REFERENCES usage_records (id),
    FOREIGN KEY (team_id) REFERENCES teams (id),
    INDEX idx_allocation_team (team_id)
);

-- Budgets
CREATE TABLE budgets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL,
    budget_name VARCHAR(255),
    budget_type ENUM('monthly', 'quarterly', 'annual') DEFAULT 'monthly',
    amount DECIMAL(12, 2),
    currency VARCHAR(3) DEFAULT 'USD',
    start_date DATE,
    end_date DATE,
    alert_threshold_percent INT DEFAULT 80,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES teams (id),
    INDEX idx_budget_active (is_active),
    INDEX idx_budget_dates (start_date, end_date)
);

-- Budget Alerts
CREATE TABLE budget_alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    budget_id BIGINT NOT NULL,
    alert_type ENUM('threshold', 'forecast', 'anomaly') DEFAULT 'threshold',
    alert_level ENUM('info', 'warning', 'critical') DEFAULT 'warning',
    current_spend DECIMAL(12, 2),
    budget_amount DECIMAL(12, 2),
    percentage_used DECIMAL(5, 2),
    message TEXT,
    is_acknowledged BOOLEAN DEFAULT FALSE,
    acknowledged_by VARCHAR(255),
    acknowledged_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (budget_id) REFERENCES budgets (id),
    INDEX idx_alert_created (created_at),
    INDEX idx_alert_acknowledged (is_acknowledged)
);

-- Insert initial cloud providers
INSERT INTO cloud_providers (`name`, display_name) VALUES
('aws', 'Amazon Web Services'),
('azure', 'Microsoft Azure'),
('gcp', 'Google Cloud Platform');

-- Insert sample teams
INSERT INTO teams (
    `name`, display_name, department, cost_center, manager_email
) VALUES
(
    'platform',
    'Platform Engineering',
    'Engineering',
    'ENG001',
    'platform.manager@company.com'
),
(
    'data-analytics',
    'Data Analytics',
    'Engineering',
    'ENG002',
    'analytics.manager@company.com'
),
(
    'mobile',
    'Mobile Development',
    'Engineering',
    'ENG003',
    'mobile.manager@company.com'
),
(
    'web-frontend',
    'Web Frontend',
    'Engineering',
    'ENG004',
    'frontend.manager@company.com'
),
('devops', 'DevOps', 'Engineering', 'ENG005', 'devops.manager@company.com'),
(
    'security',
    'Security',
    'Engineering',
    'ENG006',
    'security.manager@company.com'
),
(
    'ml-team',
    'Machine Learning',
    'Engineering',
    'ENG007',
    'ml.manager@company.com'
),
(
    'backend-api',
    'Backend API',
    'Engineering',
    'ENG008',
    'backend.manager@company.com'
),
(
    'qa-automation',
    'QA Automation',
    'Quality',
    'QA001',
    'qa.manager@company.com'
),
(
    'infrastructure',
    'Infrastructure',
    'IT',
    'IT001',
    'infra.manager@company.com'
);

-- Insert sample accounts
INSERT INTO accounts (provider_id, account_id, account_name, environment) VALUES
(1, '123456789012', 'Production AWS', 'production'),
(1, '234567890123', 'Development AWS', 'development'),
(2, 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Production Azure', 'production'),
(2, 'b2c3d4e5-f6a7-8901-bcde-f23456789012', 'Development Azure', 'development'),
(3, 'my-gcp-project-prod', 'Production GCP', 'production'),
(3, 'my-gcp-project-dev', 'Development GCP', 'development');

-- Insert AWS services
INSERT INTO services (provider_id, service_code, service_name, category) VALUES
(1, 'AmazonEC2', 'EC2 - Compute', 'Compute'),
(1, 'AmazonS3', 'S3 - Storage', 'Storage'),
(1, 'AmazonRDS', 'RDS - Database', 'Database'),
(1, 'AWSLambda', 'Lambda - Serverless', 'Compute'),
(1, 'AmazonCloudWatch', 'CloudWatch - Monitoring', 'Management'),
(1, 'AmazonVPC', 'VPC - Networking', 'Networking'),
(1, 'AmazonCloudFront', 'CloudFront - CDN', 'Networking'),
(1, 'AmazonDynamoDB', 'DynamoDB - NoSQL', 'Database'),
(1, 'ElasticLoadBalancing', 'ELB - Load Balancer', 'Networking'),
(1, 'AmazonEKS', 'EKS - Kubernetes', 'Containers');

-- Insert Azure services
INSERT INTO services (provider_id, service_code, service_name, category) VALUES
(2, 'VirtualMachines', 'Virtual Machines', 'Compute'),
(2, 'Storage', 'Blob Storage', 'Storage'),
(2, 'SQLDatabase', 'SQL Database', 'Database'),
(2, 'Functions', 'Azure Functions', 'Compute'),
(2, 'Monitor', 'Azure Monitor', 'Management'),
(2, 'VirtualNetwork', 'Virtual Network', 'Networking'),
(2, 'CDN', 'Azure CDN', 'Networking'),
(2, 'CosmosDB', 'Cosmos DB', 'Database'),
(2, 'LoadBalancer', 'Load Balancer', 'Networking'),
(2, 'ContainerInstances', 'AKS - Kubernetes', 'Containers');

-- Insert GCP services
INSERT INTO services (provider_id, service_code, service_name, category) VALUES
(3, 'ComputeEngine', 'Compute Engine', 'Compute'),
(3, 'CloudStorage', 'Cloud Storage', 'Storage'),
(3, 'CloudSQL', 'Cloud SQL', 'Database'),
(3, 'CloudFunctions', 'Cloud Functions', 'Compute'),
(3, 'Stackdriver', 'Cloud Monitoring', 'Management'),
(3, 'VPCNetwork', 'VPC Network', 'Networking'),
(3, 'CloudCDN', 'Cloud CDN', 'Networking'),
(3, 'Firestore', 'Firestore', 'Database'),
(3, 'LoadBalancing', 'Cloud Load Balancing', 'Networking'),
(3, 'GKE', 'GKE - Kubernetes', 'Containers');

-- Create views for easier reporting
CREATE VIEW v_daily_costs AS
SELECT
    ur.usage_date,
    t.`name` AS team_name,
    t.display_name AS team_display_name,
    cp.`name` AS provider_name,
    cp.display_name AS provider_display_name,
    s.service_name,
    s.category AS service_category,
    a.environment,
    ur.currency,
    SUM(ur.total_cost) AS daily_cost
FROM usage_records AS ur
INNER JOIN teams AS t ON ur.team_id = t.id
INNER JOIN services AS s ON ur.service_id = s.id
INNER JOIN accounts AS a ON ur.account_id = a.id
INNER JOIN cloud_providers AS cp ON s.provider_id = cp.id
GROUP BY ur.usage_date, t.id, s.id, a.environment;

CREATE VIEW v_monthly_team_costs AS
SELECT
    t.`name` AS team_name,
    t.display_name AS team_display_name,
    t.department,
    ur.currency,
    DATE_FORMAT(ur.usage_date, '%Y-%m') AS `month`,
    SUM(ur.total_cost) AS monthly_cost
FROM usage_records AS ur
INNER JOIN teams AS t ON ur.team_id = t.id
GROUP BY DATE_FORMAT(ur.usage_date, '%Y-%m'), t.id;

CREATE VIEW v_service_costs AS
SELECT
    cp.display_name AS provider_name,
    s.service_name,
    s.category,
    ur.currency,
    DATE_FORMAT(ur.usage_date, '%Y-%m') AS `month`,
    SUM(ur.total_cost) AS total_cost,
    COUNT(DISTINCT ur.resource_id) AS resource_count
FROM usage_records AS ur
INNER JOIN services AS s ON ur.service_id = s.id
INNER JOIN cloud_providers AS cp ON s.provider_id = cp.id
GROUP BY DATE_FORMAT(ur.usage_date, '%Y-%m'), s.id;
