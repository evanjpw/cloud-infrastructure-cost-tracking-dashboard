-- Educational Scenario Tables for Cost Optimization Testing Platform

-- Table to store scenario testing sessions
CREATE TABLE scenario_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL UNIQUE,
    scenario_template_id VARCHAR(100) NOT NULL,
    scenario_name VARCHAR(255) NOT NULL,
    difficulty_level VARCHAR(20) NOT NULL,
    scenario_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    student_identifier VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    
    INDEX idx_session_id (session_id),
    INDEX idx_student_identifier (student_identifier),
    INDEX idx_created_at (created_at)
);

-- Table to store grading keys with optimization hints
CREATE TABLE grading_keys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL,
    optimization_type VARCHAR(50) NOT NULL,
    target_resource VARCHAR(255) NOT NULL,
    issue_description TEXT NOT NULL,
    expected_savings_percent DECIMAL(5,2),
    expected_savings_amount DECIMAL(12,2),
    implementation_difficulty VARCHAR(20),
    priority_order INT,
    grading_hint TEXT,
    points_available INT DEFAULT 10,
    
    FOREIGN KEY (session_id) REFERENCES scenario_sessions(session_id) ON DELETE CASCADE,
    INDEX idx_session_grading (session_id)
);

-- Table to store generated scenario data
CREATE TABLE scenario_usage_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL,
    team_id VARCHAR(50) NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(255) NOT NULL,
    cost DECIMAL(12,2) NOT NULL,
    usage_amount DECIMAL(20,6),
    usage_unit VARCHAR(50),
    date DATE NOT NULL,
    metadata JSON,
    
    FOREIGN KEY (session_id) REFERENCES scenario_sessions(session_id) ON DELETE CASCADE,
    INDEX idx_scenario_data (session_id, date),
    INDEX idx_scenario_team_service (session_id, team_id, service_name)
);

-- Table to store student submissions for grading
CREATE TABLE student_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL,
    student_identifier VARCHAR(100) NOT NULL,
    submission_data JSON NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    auto_score DECIMAL(5,2),
    manual_score DECIMAL(5,2),
    grader_notes TEXT,
    graded_at TIMESTAMP,
    graded_by VARCHAR(100),
    
    FOREIGN KEY (session_id) REFERENCES scenario_sessions(session_id) ON DELETE CASCADE,
    INDEX idx_submission_session (session_id),
    INDEX idx_submission_student (student_identifier)
);

-- Table to store scenario metadata and configuration
CREATE TABLE scenario_metadata (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL,
    total_monthly_cost DECIMAL(12,2) NOT NULL,
    potential_savings DECIMAL(12,2) NOT NULL,
    resource_count INT NOT NULL,
    service_count INT NOT NULL,
    team_count INT NOT NULL,
    date_range_start DATE NOT NULL,
    date_range_end DATE NOT NULL,
    cloud_provider VARCHAR(20) DEFAULT 'generic',
    scenario_config JSON,
    
    FOREIGN KEY (session_id) REFERENCES scenario_sessions(session_id) ON DELETE CASCADE,
    UNIQUE KEY unique_session_metadata (session_id)
);