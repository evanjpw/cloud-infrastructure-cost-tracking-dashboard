package com.dashboard.service.impl;

import com.dashboard.templates.ScenarioTemplates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScenarioDataService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private static final Random RANDOM = new Random();
    
    // Resource type mappings for different scenario types
    private static final Map<String, List<String>> SCENARIO_RESOURCES = Map.of(
        "rightsizing", List.of("EC2 Instance", "RDS Instance", "ElastiCache Node", "ECS Task"),
        "cost_optimization", List.of("EBS Volume", "S3 Bucket", "CloudWatch Logs", "Elastic IP"),
        "reserved_instances", List.of("EC2 Instance", "RDS Instance", "ElastiCache Node"),
        "spot_instances", List.of("EC2 Instance", "ECS Task", "EMR Cluster"),
        "infrastructure_change", List.of("NAT Gateway", "Load Balancer", "VPC Endpoint", "Transit Gateway"),
        "scaling", List.of("Auto Scaling Group", "Lambda Function", "DynamoDB Table")
    );
    
    // Service mappings for resource types
    private static final Map<String, String> RESOURCE_SERVICE_MAP = Map.of(
        "EC2 Instance", "EC2",
        "RDS Instance", "RDS",
        "S3 Bucket", "S3",
        "EBS Volume", "EBS",
        "Lambda Function", "Lambda",
        "NAT Gateway", "VPC",
        "Load Balancer", "ELB",
        "ElastiCache Node", "ElastiCache",
        "DynamoDB Table", "DynamoDB",
        "Auto Scaling Group", "EC2"
    );
    
    /**
     * Generate a complete scenario session with data and grading keys
     */
    public String generateScenarioSession(String templateId, String studentIdentifier) {
        // Get scenario template
        Map<String, Object> template = ScenarioTemplates.getTemplateById(templateId);
        if (template == null) {
            throw new IllegalArgumentException("Invalid template ID: " + templateId);
        }
        
        // Create session
        String sessionId = UUID.randomUUID().toString();
        createScenarioSession(sessionId, template, studentIdentifier);
        
        // Generate usage data with intentional inefficiencies
        generateScenarioData(sessionId, template);
        
        // Generate grading keys
        generateGradingKeys(sessionId, template);
        
        // Generate metadata
        generateScenarioMetadata(sessionId, template);
        
        return sessionId;
    }
    
    /**
     * Create scenario session record
     */
    private void createScenarioSession(String sessionId, Map<String, Object> template, String studentIdentifier) {
        String sql = "INSERT INTO scenario_sessions (session_id, scenario_template_id, scenario_name, " +
                    "difficulty_level, scenario_type, student_identifier, started_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.update(sql,
            sessionId,
            template.get("id"),
            template.get("name"),
            template.get("difficulty"),
            template.get("type"),
            studentIdentifier,
            LocalDateTime.now()
        );
    }
    
    /**
     * Generate realistic usage data with known inefficiencies
     */
    private void generateScenarioData(String sessionId, Map<String, Object> template) {
        String scenarioType = (String) template.get("type");
        String difficulty = (String) template.get("difficulty");
        
        // Determine date range (last 30 days)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        
        // Generate teams based on difficulty
        List<String> teams = generateTeams(difficulty);
        
        // Get appropriate resource types for scenario
        List<String> resourceTypes = SCENARIO_RESOURCES.getOrDefault(scenarioType, 
            List.of("EC2 Instance", "S3 Bucket", "RDS Instance"));
        
        // Generate usage records with inefficiencies
        List<Map<String, Object>> usageRecords = new ArrayList<>();
        
        for (String team : teams) {
            for (String resourceType : resourceTypes) {
                int resourceCount = getResourceCount(difficulty, resourceType);
                
                for (int i = 0; i < resourceCount; i++) {
                    String resourceId = generateResourceId(resourceType, team, i);
                    String service = RESOURCE_SERVICE_MAP.getOrDefault(resourceType, "Other");
                    
                    // Generate daily usage records
                    LocalDate currentDate = startDate;
                    while (!currentDate.isAfter(endDate)) {
                        Map<String, Object> record = generateUsageRecord(
                            sessionId, team, service, resourceType, resourceId, 
                            currentDate, scenarioType, difficulty
                        );
                        usageRecords.add(record);
                        currentDate = currentDate.plusDays(1);
                    }
                }
            }
        }
        
        // Batch insert usage records
        batchInsertUsageRecords(usageRecords);
    }
    
    /**
     * Generate grading keys with optimization hints
     */
    private void generateGradingKeys(String sessionId, Map<String, Object> template) {
        String scenarioType = (String) template.get("type");
        String difficulty = (String) template.get("difficulty");
        String estimatedSavings = (String) template.get("estimatedSavings");
        
        List<Map<String, Object>> gradingKeys = new ArrayList<>();
        
        // Parse savings range (e.g., "20-35%")
        String[] savingsRange = estimatedSavings.replace("%", "").split("-");
        double minSavings = Double.parseDouble(savingsRange[0]);
        double maxSavings = savingsRange.length > 1 ? Double.parseDouble(savingsRange[1]) : minSavings;
        
        // Generate specific optimization opportunities based on scenario type
        switch (scenarioType) {
            case "rightsizing":
                gradingKeys.addAll(generateRightsizingKeys(sessionId, minSavings, maxSavings));
                break;
            case "cost_optimization":
                gradingKeys.addAll(generateCostOptimizationKeys(sessionId, minSavings, maxSavings));
                break;
            case "reserved_instances":
                gradingKeys.addAll(generateReservedInstanceKeys(sessionId, minSavings, maxSavings));
                break;
            case "spot_instances":
                gradingKeys.addAll(generateSpotInstanceKeys(sessionId, minSavings, maxSavings));
                break;
            case "infrastructure_change":
                gradingKeys.addAll(generateInfrastructureChangeKeys(sessionId, minSavings, maxSavings));
                break;
            default:
                gradingKeys.addAll(generateGenericOptimizationKeys(sessionId, minSavings, maxSavings));
        }
        
        // Insert grading keys
        batchInsertGradingKeys(gradingKeys);
    }
    
    /**
     * Generate scenario metadata
     */
    private void generateScenarioMetadata(String sessionId, Map<String, Object> template) {
        // Calculate totals from generated data
        String sql = "INSERT INTO scenario_metadata (session_id, total_monthly_cost, potential_savings, " +
                    "resource_count, service_count, team_count, date_range_start, date_range_end, scenario_config) " +
                    "SELECT ?, SUM(cost), SUM(cost) * ? / 100, COUNT(DISTINCT resource_id), " +
                    "COUNT(DISTINCT service_name), COUNT(DISTINCT team_id), MIN(date), MAX(date), ? " +
                    "FROM scenario_usage_data WHERE session_id = ?";
        
        String estimatedSavings = (String) template.get("estimatedSavings");
        double avgSavings = parseAverageSavings(estimatedSavings);
        
        Map<String, Object> config = new HashMap<>();
        config.put("template", template);
        config.put("generatedAt", LocalDateTime.now().toString());
        
        jdbcTemplate.update(sql, sessionId, avgSavings, 
            new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(config).toString(), 
            sessionId);
    }
    
    // Helper methods
    
    private List<String> generateTeams(String difficulty) {
        switch (difficulty) {
            case "beginner":
                return List.of("Engineering", "Marketing", "Sales");
            case "intermediate":
                return List.of("Engineering", "Marketing", "Sales", "DataScience", "Operations");
            case "advanced":
                return List.of("Engineering", "Marketing", "Sales", "DataScience", 
                              "Operations", "Finance", "HR", "Research");
            default:
                return List.of("Engineering", "Marketing");
        }
    }
    
    private int getResourceCount(String difficulty, String resourceType) {
        int base = resourceType.contains("Instance") ? 5 : 3;
        switch (difficulty) {
            case "beginner":
                return base;
            case "intermediate":
                return base * 2;
            case "advanced":
                return base * 3;
            default:
                return base;
        }
    }
    
    private String generateResourceId(String resourceType, String team, int index) {
        String prefix = resourceType.toLowerCase()
            .replace(" ", "-")
            .substring(0, Math.min(3, resourceType.length()));
        return String.format("%s-%s-%04d", prefix, team.toLowerCase(), index + 1);
    }
    
    private Map<String, Object> generateUsageRecord(String sessionId, String team, String service,
                                                   String resourceType, String resourceId,
                                                   LocalDate date, String scenarioType, String difficulty) {
        Map<String, Object> record = new HashMap<>();
        record.put("session_id", sessionId);
        record.put("team_id", team);
        record.put("service_name", service);
        record.put("resource_type", resourceType);
        record.put("resource_id", resourceId);
        record.put("date", date);
        
        // Generate cost with inefficiencies
        double baseCost = generateBaseCost(resourceType, difficulty);
        double inefficiencyMultiplier = generateInefficiencyMultiplier(scenarioType, resourceType);
        double dailyCost = baseCost * inefficiencyMultiplier;
        
        // Add some randomness
        dailyCost *= (0.8 + RANDOM.nextDouble() * 0.4); // ±20% variation
        record.put("cost", BigDecimal.valueOf(dailyCost).setScale(2, RoundingMode.HALF_UP));
        
        // Generate usage metrics
        Map<String, Object> metadata = generateResourceMetadata(resourceType, scenarioType, inefficiencyMultiplier);
        record.put("usage_amount", metadata.get("usage_amount"));
        record.put("usage_unit", metadata.get("usage_unit"));
        record.put("metadata", new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(metadata).toString());
        
        return record;
    }
    
    private double generateBaseCost(String resourceType, String difficulty) {
        Map<String, Double> baseCosts = Map.of(
            "EC2 Instance", 50.0,
            "RDS Instance", 75.0,
            "S3 Bucket", 10.0,
            "EBS Volume", 5.0,
            "Lambda Function", 15.0,
            "NAT Gateway", 45.0,
            "Load Balancer", 25.0,
            "ElastiCache Node", 35.0
        );
        
        double base = baseCosts.getOrDefault(resourceType, 20.0);
        
        // Scale by difficulty
        switch (difficulty) {
            case "intermediate":
                base *= 1.5;
                break;
            case "advanced":
                base *= 2.5;
                break;
        }
        
        return base;
    }
    
    private double generateInefficiencyMultiplier(String scenarioType, String resourceType) {
        switch (scenarioType) {
            case "rightsizing":
                // Oversized resources
                return resourceType.contains("Instance") ? 2.5 : 1.2;
            case "cost_optimization":
                // Unused or underutilized resources
                return RANDOM.nextDouble() < 0.3 ? 0.1 : 1.3;
            case "reserved_instances":
                // On-demand pricing when RI would be better
                return 1.5;
            case "spot_instances":
                // Using on-demand for batch workloads
                return 2.0;
            case "infrastructure_change":
                // Inefficient architecture
                return 1.8;
            default:
                return 1.2;
        }
    }
    
    private Map<String, Object> generateResourceMetadata(String resourceType, String scenarioType, double inefficiency) {
        Map<String, Object> metadata = new HashMap<>();
        
        switch (resourceType) {
            case "EC2 Instance":
                metadata.put("instance_type", inefficiency > 2 ? "m5.4xlarge" : "m5.large");
                metadata.put("cpu_utilization", inefficiency > 2 ? 15.0 : 60.0);
                metadata.put("memory_utilization", inefficiency > 2 ? 20.0 : 70.0);
                metadata.put("usage_amount", 24.0);
                metadata.put("usage_unit", "hours");
                break;
            case "S3 Bucket":
                double storage = 100 + RANDOM.nextInt(900);
                metadata.put("storage_gb", storage);
                metadata.put("requests", RANDOM.nextInt(10000));
                metadata.put("usage_amount", storage);
                metadata.put("usage_unit", "GB");
                break;
            case "RDS Instance":
                metadata.put("instance_class", inefficiency > 1.5 ? "db.r5.2xlarge" : "db.t3.medium");
                metadata.put("storage_gb", 500);
                metadata.put("cpu_utilization", inefficiency > 1.5 ? 10.0 : 50.0);
                metadata.put("usage_amount", 24.0);
                metadata.put("usage_unit", "hours");
                break;
            default:
                metadata.put("usage_amount", 1.0);
                metadata.put("usage_unit", "unit");
        }
        
        return metadata;
    }
    
    private List<Map<String, Object>> generateRightsizingKeys(String sessionId, double minSavings, double maxSavings) {
        List<Map<String, Object>> keys = new ArrayList<>();
        
        keys.add(createGradingKey(sessionId, "rightsizing", "ec2-engineering-*", 
            "EC2 instances with CPU utilization < 20% for 30 days", 
            35.0, "medium", 1, 
            "Students should identify oversized m5.4xlarge instances that could be downsized to m5.large"));
        
        keys.add(createGradingKey(sessionId, "rightsizing", "rds-*", 
            "RDS instances with CPU utilization < 15% consistently", 
            25.0, "medium", 2, 
            "Database instances are significantly oversized for actual workload"));
        
        return keys;
    }
    
    private List<Map<String, Object>> generateCostOptimizationKeys(String sessionId, double minSavings, double maxSavings) {
        List<Map<String, Object>> keys = new ArrayList<>();
        
        keys.add(createGradingKey(sessionId, "unused_resources", "ebs-*-0001 to ebs-*-0003", 
            "Unattached EBS volumes for > 30 days", 
            5.0, "easy", 1, 
            "Look for volumes with very low or zero IOPS"));
        
        keys.add(createGradingKey(sessionId, "lifecycle_policy", "s3-*", 
            "S3 buckets without lifecycle policies for old data", 
            15.0, "medium", 2, 
            "Large amount of data could be moved to cheaper storage classes"));
        
        return keys;
    }
    
    private Map<String, Object> createGradingKey(String sessionId, String optimizationType,
                                                String targetResource, String issue,
                                                double expectedSavings, String difficulty,
                                                int priority, String hint) {
        Map<String, Object> key = new HashMap<>();
        key.put("session_id", sessionId);
        key.put("optimization_type", optimizationType);
        key.put("target_resource", targetResource);
        key.put("issue_description", issue);
        key.put("expected_savings_percent", expectedSavings);
        key.put("implementation_difficulty", difficulty);
        key.put("priority_order", priority);
        key.put("grading_hint", hint);
        key.put("points_available", difficulty.equals("easy") ? 10 : difficulty.equals("medium") ? 15 : 20);
        return key;
    }
    
    private void batchInsertUsageRecords(List<Map<String, Object>> records) {
        String sql = "INSERT INTO scenario_usage_data (session_id, team_id, service_name, resource_type, " +
                    "resource_id, cost, usage_amount, usage_unit, date, metadata) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.batchUpdate(sql, records, records.size(), (ps, record) -> {
            ps.setString(1, (String) record.get("session_id"));
            ps.setString(2, (String) record.get("team_id"));
            ps.setString(3, (String) record.get("service_name"));
            ps.setString(4, (String) record.get("resource_type"));
            ps.setString(5, (String) record.get("resource_id"));
            ps.setBigDecimal(6, (BigDecimal) record.get("cost"));
            ps.setDouble(7, (Double) record.get("usage_amount"));
            ps.setString(8, (String) record.get("usage_unit"));
            ps.setDate(9, java.sql.Date.valueOf((LocalDate) record.get("date")));
            ps.setString(10, (String) record.get("metadata"));
        });
    }
    
    private void batchInsertGradingKeys(List<Map<String, Object>> keys) {
        String sql = "INSERT INTO grading_keys (session_id, optimization_type, target_resource, " +
                    "issue_description, expected_savings_percent, implementation_difficulty, " +
                    "priority_order, grading_hint, points_available) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.batchUpdate(sql, keys, keys.size(), (ps, key) -> {
            ps.setString(1, (String) key.get("session_id"));
            ps.setString(2, (String) key.get("optimization_type"));
            ps.setString(3, (String) key.get("target_resource"));
            ps.setString(4, (String) key.get("issue_description"));
            ps.setDouble(5, (Double) key.get("expected_savings_percent"));
            ps.setString(6, (String) key.get("implementation_difficulty"));
            ps.setInt(7, (Integer) key.get("priority_order"));
            ps.setString(8, (String) key.get("grading_hint"));
            ps.setInt(9, (Integer) key.get("points_available"));
        });
    }
    
    private double parseAverageSavings(String savingsRange) {
        String[] parts = savingsRange.replace("%", "").split("-");
        double min = Double.parseDouble(parts[0]);
        double max = parts.length > 1 ? Double.parseDouble(parts[1]) : min;
        return (min + max) / 2;
    }
    
    // Additional key generation methods would follow similar patterns...
    private List<Map<String, Object>> generateReservedInstanceKeys(String sessionId, double minSavings, double maxSavings) {
        return List.of(createGradingKey(sessionId, "reserved_instances", "ec2-*", 
            "Steady-state workloads using on-demand pricing", 
            30.0, "medium", 1, 
            "Look for instances running 24/7 with consistent utilization"));
    }
    
    private List<Map<String, Object>> generateSpotInstanceKeys(String sessionId, double minSavings, double maxSavings) {
        return List.of(createGradingKey(sessionId, "spot_instances", "ec2-batch-*", 
            "Batch processing using on-demand instances", 
            70.0, "hard", 1, 
            "Fault-tolerant workloads that could use spot instances"));
    }
    
    private List<Map<String, Object>> generateInfrastructureChangeKeys(String sessionId, double minSavings, double maxSavings) {
        return List.of(createGradingKey(sessionId, "architecture_optimization", "nat-*", 
            "Multiple NAT Gateways that could be consolidated", 
            40.0, "hard", 1, 
            "Low traffic volumes don't justify multiple NAT Gateways"));
    }
    
    private List<Map<String, Object>> generateGenericOptimizationKeys(String sessionId, double minSavings, double maxSavings) {
        return generateCostOptimizationKeys(sessionId, minSavings, maxSavings);
    }
}