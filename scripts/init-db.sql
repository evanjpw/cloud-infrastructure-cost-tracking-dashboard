-- Drop tables if they exist
DROP TABLE IF EXISTS usage_records;
DROP TABLE IF EXISTS teams;

-- Create teams table
CREATE TABLE teams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(100)
);

-- Create usage_records table
CREATE TABLE usage_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_name VARCHAR(100) NOT NULL,
    service VARCHAR(100) NOT NULL,
    usage_amount DOUBLE NOT NULL,
    usage_date DATE NOT NULL
);
