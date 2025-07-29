-- Migration 001: Enhanced Schema for Educational Platform
-- Adds comprehensive support for analytics, scenarios, budgets, and educational features

-- ========================================
-- ENHANCED USAGE DATA
-- ========================================

-- Enhanced usage records with full cloud provider details
CREATE TABLE enhanced_usage_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    date DATE NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    team_name VARCHAR(100) NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    region VARCHAR(50) NOT NULL DEFAULT 'us-east-1',
    provider VARCHAR(50) NOT NULL DEFAULT 'aws',
    resource_id VARCHAR(255),
    usage_type VARCHAR(100),
    cost DECIMAL(12, 4) NOT NULL,
    usage_quantity DECIMAL(12, 4),
    usage_unit VARCHAR(50),
    tags JSON,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_date_team (date, team_name),
    INDEX idx_service_region (service_name, region),
    INDEX idx_cost_date (cost, date),
    INDEX idx_provider_service (provider, service_name),
    INDEX idx_timestamp (timestamp)
);

-- ========================================
-- SCENARIO MANAGEMENT
-- ========================================

-- What-if scenarios for cost modeling
CREATE TABLE scenarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type ENUM(
        'instance_rightsizing', 
        'reserved_instances', 
        'auto_scaling', 
        'region_migration', 
        'service_migration', 
        'multi_cloud',
        'capacity_planning',
        'disaster_recovery'
    ) NOT NULL,
    changes JSON NOT NULL,
    time_horizon INT DEFAULT 30,
    baseline_data JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    
    INDEX idx_type (type),
    INDEX idx_created_at (created_at)
);

-- Scenario execution results
CREATE TABLE scenario_results (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    scenario_id BIGINT NOT NULL,
    baseline_cost DECIMAL(12, 2) NOT NULL,
    projected_cost DECIMAL(12, 2) NOT NULL,
    savings_amount DECIMAL(12, 2) NOT NULL,
    savings_percentage DECIMAL(5, 2) NOT NULL,
    confidence_score DECIMAL(3, 2) DEFAULT 0.75,
    risk_level ENUM('low', 'medium', 'high') DEFAULT 'medium',
    impact_analysis JSON,
    daily_projections JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (scenario_id) REFERENCES scenarios(id) ON DELETE CASCADE,
    INDEX idx_scenario_id (scenario_id),
    INDEX idx_savings (savings_amount, savings_percentage)
);

-- ========================================
-- BUDGET MANAGEMENT
-- ========================================

-- Budget definitions and tracking
CREATE TABLE budgets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    amount DECIMAL(12, 2) NOT NULL,
    period ENUM('daily', 'weekly', 'monthly', 'quarterly', 'yearly') NOT NULL,
    scope ENUM('team', 'service', 'region', 'provider', 'total') NOT NULL,
    target VARCHAR(255) NOT NULL, -- team name, service name, region, or 'organization'
    alert_threshold DECIMAL(5, 2) DEFAULT 80.0,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_scope_target (scope, target),
    INDEX idx_period_dates (period, start_date, end_date),
    INDEX idx_active (is_active)
);

-- Budget alerts and notifications
CREATE TABLE budget_alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    budget_id BIGINT NOT NULL,
    alert_type ENUM(
        'threshold_exceeded', 
        'forecast_exceeded', 
        'budget_depleted',
        'spending_spike',
        'forecast_warning'
    ) NOT NULL,
    severity ENUM('info', 'warning', 'critical') NOT NULL,
    message TEXT NOT NULL,
    current_spend DECIMAL(12, 2),
    threshold_value DECIMAL(12, 2),
    percentage_used DECIMAL(5, 2),
    triggered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    acknowledged BOOLEAN DEFAULT FALSE,
    acknowledged_by VARCHAR(255),
    acknowledged_at TIMESTAMP NULL,
    
    FOREIGN KEY (budget_id) REFERENCES budgets(id) ON DELETE CASCADE,
    INDEX idx_budget_severity (budget_id, severity),
    INDEX idx_triggered_acknowledged (triggered_at, acknowledged)
);

-- ========================================
-- EDUCATIONAL PLATFORM
-- ========================================

-- Educational scenario templates
CREATE TABLE educational_scenarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    difficulty ENUM('beginner', 'intermediate', 'advanced', 'expert') NOT NULL,
    architecture_type VARCHAR(100) NOT NULL, -- e-commerce, data-pipeline, microservices, etc.
    learning_objectives JSON NOT NULL,
    hidden_inefficiencies JSON NOT NULL, -- Cost drivers students should find
    expected_savings DECIMAL(12, 2) NOT NULL,
    solution_template JSON NOT NULL, -- Ideal student response
    grading_rubric JSON NOT NULL,
    estimated_time_minutes INT DEFAULT 60,
    complexity_score DECIMAL(3, 2) DEFAULT 5.0, -- 1-10 scale
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_difficulty (difficulty),
    INDEX idx_architecture (architecture_type),
    INDEX idx_active_complexity (is_active, complexity_score)
);

-- Student testing sessions
CREATE TABLE student_sessions (
    id VARCHAR(36) PRIMARY KEY, -- UUID for security
    student_id VARCHAR(255) NOT NULL,
    student_name VARCHAR(255),
    scenario_id BIGINT NOT NULL,
    dataset_signature VARCHAR(64) NOT NULL, -- Hash of generated dataset for uniqueness
    session_config JSON, -- Time limits, allowed tools, etc.
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    time_limit_minutes INT DEFAULT 120,
    status ENUM('active', 'submitted', 'expired', 'graded', 'cancelled') DEFAULT 'active',
    progress_data JSON, -- Track student actions during session
    
    FOREIGN KEY (scenario_id) REFERENCES educational_scenarios(id),
    INDEX idx_student_status (student_id, status),
    INDEX idx_scenario_start (scenario_id, start_time),
    INDEX idx_session_status (status),
    
    UNIQUE KEY unique_student_scenario_active (student_id, scenario_id, status)
);

-- Generated datasets for student sessions (isolated data)
CREATE TABLE student_datasets (
    id VARCHAR(36) PRIMARY KEY, -- UUID
    session_id VARCHAR(36) NOT NULL,
    signature VARCHAR(64) NOT NULL, -- Hash for verification
    cost_data JSON NOT NULL, -- Generated cost records
    metadata JSON, -- Generation parameters, hidden inefficiencies locations
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (session_id) REFERENCES student_sessions(id) ON DELETE CASCADE,
    UNIQUE KEY unique_session_dataset (session_id)
);

-- Student submissions and analysis
CREATE TABLE student_submissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(36) NOT NULL,
    recommendations JSON NOT NULL,
    identified_issues JSON NOT NULL,
    savings_projection DECIMAL(12, 2),
    confidence_assessment DECIMAL(3, 2), -- Student's confidence in their analysis
    implementation_priority JSON,
    time_spent_minutes INT,
    submission_notes TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (session_id) REFERENCES student_sessions(id) ON DELETE CASCADE,
    INDEX idx_session_submitted (session_id, submitted_at),
    INDEX idx_savings_projection (savings_projection)
);

-- Automated and manual grading results
CREATE TABLE grading_results (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    total_score DECIMAL(5, 2) NOT NULL, -- Out of 100
    breakdown JSON NOT NULL, -- Detailed scoring by category
    automated_score DECIMAL(5, 2) NOT NULL,
    manual_adjustment DECIMAL(5, 2) DEFAULT 0.0,
    accuracy_score DECIMAL(5, 2), -- How close to expected savings
    completeness_score DECIMAL(5, 2), -- How many issues found
    methodology_score DECIMAL(5, 2), -- Quality of analysis approach
    automated_feedback TEXT,
    manual_feedback TEXT,
    graded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    graded_by VARCHAR(255),
    
    FOREIGN KEY (submission_id) REFERENCES student_submissions(id) ON DELETE CASCADE,
    INDEX idx_submission_score (submission_id, total_score),
    INDEX idx_grading_date (graded_at)
);

-- ========================================
-- OPTIMIZATION RECOMMENDATIONS
-- ========================================

-- Cost optimization recommendations
CREATE TABLE optimization_recommendations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    type ENUM(
        'rightsizing',
        'reserved_instance',
        'unused_resource',
        'storage_optimization',
        'data_transfer',
        'scheduling',
        'governance',
        'anomaly'
    ) NOT NULL,
    impact ENUM('low', 'medium', 'high') NOT NULL,
    priority ENUM('low', 'medium', 'high', 'critical') NOT NULL,
    potential_savings DECIMAL(12, 2) NOT NULL,
    implementation_effort ENUM('low', 'medium', 'high') NOT NULL,
    risk_level ENUM('low', 'medium', 'high') NOT NULL,
    affected_services JSON,
    affected_teams JSON,
    implementation_steps JSON,
    status ENUM('pending', 'accepted', 'rejected', 'implemented', 'deferred') DEFAULT 'pending',
    status_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_type_impact (type, impact),
    INDEX idx_status_priority (status, priority),
    INDEX idx_savings (potential_savings),
    INDEX idx_created_at (created_at)
);

-- ========================================
-- ANALYTICS AND PREDICTIONS
-- ========================================

-- Cached prediction results for performance
CREATE TABLE prediction_cache (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cache_key VARCHAR(255) NOT NULL,
    method VARCHAR(50) NOT NULL,
    parameters JSON NOT NULL,
    results JSON NOT NULL,
    confidence_score DECIMAL(3, 2),
    valid_until TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_cache_key (cache_key),
    INDEX idx_valid_until (valid_until),
    INDEX idx_method (method)
);

-- Detected anomalies for tracking
CREATE TABLE cost_anomalies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    detection_date DATE NOT NULL,
    team_name VARCHAR(100),
    service_name VARCHAR(100),
    anomaly_type ENUM('spike', 'drop', 'trend_change', 'pattern_break') NOT NULL,
    severity DECIMAL(3, 2) NOT NULL, -- Standard deviations from normal
    expected_cost DECIMAL(12, 2) NOT NULL,
    actual_cost DECIMAL(12, 2) NOT NULL,
    deviation_percentage DECIMAL(5, 2) NOT NULL,
    context JSON, -- Additional details about the anomaly
    investigated BOOLEAN DEFAULT FALSE,
    investigation_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_detection_team (detection_date, team_name),
    INDEX idx_severity_type (severity, anomaly_type),
    INDEX idx_investigated (investigated)
);

-- ========================================
-- SAMPLE DATA INSERTION
-- ========================================

-- Insert enhanced sample data with realistic patterns
INSERT INTO enhanced_usage_records (date, team_name, service_name, region, provider, cost, usage_quantity, usage_unit, tags) VALUES
-- Platform team - higher costs, multiple regions
('2025-01-01', 'platform', 'EC2', 'us-east-1', 'aws', 1245.50, 168.0, 'instance-hours', '{"environment": "production", "owner": "platform-team"}'),
('2025-01-01', 'platform', 'ELB', 'us-east-1', 'aws', 89.25, 24.0, 'load-balancer-hours', '{"environment": "production", "owner": "platform-team"}'),
('2025-01-01', 'platform', 'EC2', 'us-west-2', 'aws', 892.75, 120.0, 'instance-hours', '{"environment": "staging", "owner": "platform-team"}'),

-- Frontend team - moderate costs, CDN usage
('2025-01-01', 'frontend', 'S3', 'us-east-1', 'aws', 234.80, 500.0, 'GB-storage', '{"environment": "production", "content-type": "static"}'),
('2025-01-01', 'frontend', 'CloudFront', 'global', 'aws', 156.90, 1000.0, 'GB-transfer', '{"environment": "production", "content-type": "cdn"}'),

-- Backend team - database heavy
('2025-01-01', 'backend', 'RDS', 'us-east-1', 'aws', 567.25, 720.0, 'db-hours', '{"environment": "production", "db-type": "mysql"}'),
('2025-01-01', 'backend', 'ElastiCache', 'us-east-1', 'aws', 123.45, 168.0, 'cache-hours', '{"environment": "production", "cache-type": "redis"}'),

-- Data team - processing intensive
('2025-01-01', 'data', 'Lambda', 'us-east-1', 'aws', 345.60, 50000.0, 'invocations', '{"environment": "production", "runtime": "python3.9"}'),
('2025-01-01', 'data', 'S3', 'us-east-1', 'aws', 789.30, 2000.0, 'GB-storage', '{"environment": "production", "content-type": "data-lake"}'),

-- ML team - compute heavy
('2025-01-01', 'ml', 'EC2', 'us-east-1', 'aws', 1567.80, 96.0, 'gpu-hours', '{"environment": "production", "instance-type": "p3.2xlarge"}'),
('2025-01-01', 'ml', 'SageMaker', 'us-east-1', 'aws', 445.20, 168.0, 'training-hours', '{"environment": "production", "model-type": "deep-learning"}');

-- Insert sample educational scenario
INSERT INTO educational_scenarios (
    name, 
    description, 
    difficulty, 
    architecture_type, 
    learning_objectives, 
    hidden_inefficiencies,
    expected_savings,
    solution_template,
    grading_rubric
) VALUES (
    'E-commerce Platform Cost Analysis',
    'Analyze costs for a typical e-commerce platform with web servers, databases, CDN, and caching layers',
    'beginner',
    'e-commerce',
    '["Identify over-provisioned resources", "Understand reserved instance opportunities", "Recognize unused resources"]',
    '{"oversized_instances": {"service": "EC2", "waste_percentage": 35}, "unused_volumes": {"count": 3, "monthly_cost": 150}, "inefficient_storage": {"service": "S3", "potential_savings": 25}}',
    1250.00,
    '{"recommendations": [{"type": "rightsizing", "impact": "high", "savings": 800}, {"type": "reserved_instances", "impact": "medium", "savings": 300}, {"type": "cleanup", "impact": "low", "savings": 150}]}',
    '{"cost_identification": 30, "savings_accuracy": 30, "recommendations_quality": 25, "implementation_feasibility": 15}'
);

-- Insert sample budget
INSERT INTO budgets (name, amount, period, scope, target, alert_threshold, start_date, end_date) VALUES
('Platform Team Monthly Budget', 15000.00, 'monthly', 'team', 'platform', 85.0, '2025-01-01', '2025-01-31'),
('EC2 Quarterly Budget', 45000.00, 'quarterly', 'service', 'EC2', 80.0, '2025-01-01', '2025-03-31'),
('Annual Organization Budget', 500000.00, 'yearly', 'total', 'organization', 90.0, '2025-01-01', '2025-12-31');

-- Insert sample optimization recommendation  
INSERT INTO optimization_recommendations (
    title,
    description,
    type,
    impact,
    priority,
    potential_savings,
    implementation_effort,
    risk_level,
    affected_services,
    affected_teams,
    implementation_steps
) VALUES (
    'Rightsize Over-provisioned EC2 Instances',
    'Several EC2 instances in the platform team are consistently running at <20% CPU utilization and can be downsized',
    'rightsizing',
    'high',
    'high',
    850.00,
    'low',
    'low',
    '["EC2"]',
    '["platform"]',
    '["Analyze instance utilization metrics", "Test application performance on smaller instance", "Schedule maintenance window for resize", "Monitor post-change performance"]'
);

COMMIT;