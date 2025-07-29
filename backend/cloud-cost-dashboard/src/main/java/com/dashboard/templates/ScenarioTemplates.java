package com.dashboard.templates;

import java.util.*;

public class ScenarioTemplates {
    
    // Beginner Templates (20 templates)
    public static List<Map<String, Object>> getBeginnerTemplates() {
        List<Map<String, Object>> templates = new ArrayList<>();
        
        // 1. Basic Cost Optimization
        templates.add(createTemplate(
            "beginner_rightsizing_ec2",
            "Right-size Over-provisioned EC2 Instances",
            "beginner",
            "rightsizing",
            "Identify EC2 instances with CPU utilization < 20% and resize to smaller instance types",
            "15-25%",
            "1-2 weeks",
            List.of("AWS Console", "CloudWatch basics", "EC2 instance types"),
            Map.of("target_utilization", "50%", "analysis_period", "14 days")
        ));
        
        // 2. Storage Cleanup
        templates.add(createTemplate(
            "beginner_ebs_cleanup",
            "Clean Up Unattached EBS Volumes",
            "beginner",
            "cost_optimization",
            "Find and delete EBS volumes that have been unattached for > 30 days",
            "5-15%",
            "2-3 days",
            List.of("AWS Console", "EBS basics"),
            Map.of("retention_days", "30", "include_snapshots", "true")
        ));
        
        // 3. S3 Lifecycle
        templates.add(createTemplate(
            "beginner_s3_lifecycle",
            "Implement S3 Lifecycle Policies",
            "beginner",
            "cost_optimization",
            "Move infrequently accessed objects to cheaper storage classes",
            "10-30%",
            "1 week",
            List.of("S3 Console", "Storage classes"),
            Map.of("transition_days", "30", "target_class", "GLACIER")
        ));
        
        // 4. Dev Environment Scheduling
        templates.add(createTemplate(
            "beginner_dev_scheduling",
            "Schedule Dev/Test Environment Shutdown",
            "beginner",
            "cost_optimization",
            "Automatically stop development instances during non-business hours",
            "40-60%",
            "3-5 days",
            List.of("AWS Lambda basics", "CloudWatch Events"),
            Map.of("schedule", "weekdays_only", "business_hours", "9-5")
        ));
        
        // 5. Snapshot Management
        templates.add(createTemplate(
            "beginner_snapshot_cleanup",
            "Delete Old EBS Snapshots",
            "beginner",
            "cost_optimization",
            "Remove snapshots older than 90 days with proper backup verification",
            "3-8%",
            "1-2 days",
            List.of("AWS Console", "Snapshot management"),
            Map.of("retention_days", "90", "keep_monthly", "true")
        ));
        
        // 6. Elastic IP Cleanup
        templates.add(createTemplate(
            "beginner_eip_cleanup",
            "Release Unused Elastic IPs",
            "beginner",
            "cost_optimization",
            "Identify and release Elastic IPs not associated with running instances",
            "2-5%",
            "1 day",
            List.of("VPC Console", "Elastic IP basics"),
            Map.of("check_associations", "true")
        ));
        
        // 7. CloudWatch Logs Retention
        templates.add(createTemplate(
            "beginner_logs_retention",
            "Set CloudWatch Logs Retention",
            "beginner",
            "cost_optimization",
            "Configure retention policies to automatically delete old logs",
            "5-10%",
            "1-2 days",
            List.of("CloudWatch Console", "Log groups"),
            Map.of("default_retention", "30 days", "critical_retention", "90 days")
        ));
        
        // 8. RDS Instance Rightsizing
        templates.add(createTemplate(
            "beginner_rds_rightsizing",
            "Right-size RDS Database Instances",
            "beginner",
            "rightsizing",
            "Analyze RDS CPU/memory usage and resize underutilized databases",
            "20-35%",
            "2 weeks",
            List.of("RDS Console", "Performance Insights basics"),
            Map.of("target_cpu", "40%", "analysis_window", "7 days")
        ));
        
        // 9. NAT Gateway Optimization
        templates.add(createTemplate(
            "beginner_nat_optimization",
            "Optimize NAT Gateway Usage",
            "beginner",
            "infrastructure_change",
            "Replace NAT Gateways with NAT Instances for low-traffic environments",
            "50-70%",
            "1 week",
            List.of("VPC basics", "NAT concepts"),
            Map.of("traffic_threshold", "10GB/month")
        ));
        
        // 10. Load Balancer Cleanup
        templates.add(createTemplate(
            "beginner_lb_cleanup",
            "Remove Unused Load Balancers",
            "beginner",
            "cost_optimization",
            "Identify and delete load balancers with no healthy targets",
            "5-10%",
            "2-3 days",
            List.of("ELB Console", "Target group basics"),
            Map.of("check_period", "7 days")
        ));
        
        // 11. Lambda Memory Optimization
        templates.add(createTemplate(
            "beginner_lambda_memory",
            "Optimize Lambda Function Memory",
            "beginner",
            "rightsizing",
            "Analyze Lambda execution metrics and adjust memory allocation",
            "10-20%",
            "1 week",
            List.of("Lambda Console", "CloudWatch Logs"),
            Map.of("performance_buffer", "20%")
        ));
        
        // 12. DynamoDB On-Demand
        templates.add(createTemplate(
            "beginner_dynamodb_ondemand",
            "Switch DynamoDB to On-Demand",
            "beginner",
            "cost_optimization",
            "Convert unpredictable workload tables from provisioned to on-demand",
            "20-40%",
            "2-3 days",
            List.of("DynamoDB Console", "Pricing models"),
            Map.of("utilization_threshold", "20%")
        ));
        
        // 13. CloudFront Optimization
        templates.add(createTemplate(
            "beginner_cloudfront_caching",
            "Optimize CloudFront Cache Settings",
            "beginner",
            "cost_optimization",
            "Increase cache hit ratio to reduce origin requests",
            "15-30%",
            "3-5 days",
            List.of("CloudFront basics", "Cache behaviors"),
            Map.of("target_hit_ratio", "90%")
        ));
        
        // 14. EC2 Instance Family Update
        templates.add(createTemplate(
            "beginner_instance_family",
            "Upgrade to Latest Instance Generation",
            "beginner",
            "infrastructure_change",
            "Move from older generation instances (m4, c4) to current gen (m6i, c6i)",
            "10-15%",
            "2 weeks",
            List.of("EC2 instance types", "Migration basics"),
            Map.of("target_generation", "6")
        ));
        
        // 15. S3 Request Optimization
        templates.add(createTemplate(
            "beginner_s3_requests",
            "Reduce S3 API Request Costs",
            "beginner",
            "cost_optimization",
            "Batch operations and implement request caching",
            "5-15%",
            "1 week",
            List.of("S3 API", "Application optimization"),
            Map.of("batch_size", "1000", "cache_ttl", "3600")
        ));
        
        // 16. RDS Backup Optimization
        templates.add(createTemplate(
            "beginner_rds_backups",
            "Optimize RDS Backup Retention",
            "beginner",
            "cost_optimization",
            "Adjust automated backup retention based on compliance needs",
            "3-7%",
            "1-2 days",
            List.of("RDS backups", "Compliance basics"),
            Map.of("default_retention", "7 days", "critical_retention", "35 days")
        ));
        
        // 17. EKS Node Group Optimization
        templates.add(createTemplate(
            "beginner_eks_nodes",
            "Right-size EKS Worker Nodes",
            "beginner",
            "rightsizing",
            "Analyze pod resource requests and optimize node instance types",
            "20-30%",
            "2 weeks",
            List.of("EKS basics", "Kubernetes resources"),
            Map.of("target_utilization", "70%")
        ));
        
        // 18. Data Transfer Optimization
        templates.add(createTemplate(
            "beginner_data_transfer",
            "Reduce Cross-AZ Data Transfer",
            "beginner",
            "infrastructure_change",
            "Co-locate frequently communicating services in same AZ",
            "10-25%",
            "1-2 weeks",
            List.of("VPC networking", "AZ concepts"),
            Map.of("transfer_threshold", "100GB/month")
        ));
        
        // 19. CloudWatch Metrics Cleanup
        templates.add(createTemplate(
            "beginner_metrics_cleanup",
            "Remove Unused Custom Metrics",
            "beginner",
            "cost_optimization",
            "Identify and stop publishing unused CloudWatch custom metrics",
            "2-5%",
            "2-3 days",
            List.of("CloudWatch metrics", "Monitoring basics"),
            Map.of("inactive_days", "30")
        ));
        
        // 20. EC2 Savings Plans
        templates.add(createTemplate(
            "beginner_savings_plans",
            "Purchase Compute Savings Plans",
            "beginner",
            "reserved_instances",
            "Commit to 1-year Compute Savings Plans for steady-state workloads",
            "20-30%",
            "1 week",
            List.of("Savings Plans basics", "Cost Explorer"),
            Map.of("commitment_term", "1year", "payment_option", "no_upfront")
        ));
        
        return templates;
    }
    
    // Intermediate Templates (20 templates)
    public static List<Map<String, Object>> getIntermediateTemplates() {
        List<Map<String, Object>> templates = new ArrayList<>();
        
        // 21. Multi-Region Optimization
        templates.add(createTemplate(
            "intermediate_multi_region",
            "Optimize Multi-Region Architecture",
            "intermediate",
            "infrastructure_change",
            "Consolidate non-critical workloads to fewer regions",
            "25-40%",
            "4-6 weeks",
            List.of("Multi-region design", "Data replication", "Disaster recovery"),
            Map.of("primary_regions", "2", "dr_strategy", "pilot_light")
        ));
        
        // 22. Reserved Instance Optimization
        templates.add(createTemplate(
            "intermediate_ri_optimization",
            "Optimize Reserved Instance Portfolio",
            "intermediate",
            "reserved_instances",
            "Analyze usage patterns and purchase optimal mix of RIs",
            "35-45%",
            "2-3 weeks",
            List.of("RI planning", "Capacity forecasting", "Financial modeling"),
            Map.of("coverage_target", "80%", "utilization_target", "95%")
        ));
        
        // 23. Auto Scaling Optimization
        templates.add(createTemplate(
            "intermediate_autoscaling",
            "Implement Predictive Auto Scaling",
            "intermediate",
            "scaling",
            "Use ML-based predictive scaling for better cost efficiency",
            "15-25%",
            "3-4 weeks",
            List.of("Auto Scaling advanced", "CloudWatch metrics", "ML basics"),
            Map.of("prediction_window", "24h", "scaling_metric", "target_tracking")
        ));
        
        // 24. Container Optimization
        templates.add(createTemplate(
            "intermediate_container_packing",
            "Optimize Container Resource Packing",
            "intermediate",
            "rightsizing",
            "Improve container density on ECS/EKS clusters",
            "30-50%",
            "3-4 weeks",
            List.of("Container orchestration", "Resource scheduling", "Bin packing"),
            Map.of("target_cpu_utilization", "75%", "target_memory_utilization", "80%")
        ));
        
        // 25. Spot Fleet Implementation
        templates.add(createTemplate(
            "intermediate_spot_fleet",
            "Implement Spot Fleet for Batch Jobs",
            "intermediate",
            "spot_instances",
            "Migrate batch processing workloads to Spot Instances",
            "60-80%",
            "4-5 weeks",
            List.of("Spot Fleet", "Batch computing", "Fault tolerance"),
            Map.of("spot_percentage", "80%", "interruption_handling", "checkpoint")
        ));
        
        // 26. Database Consolidation
        templates.add(createTemplate(
            "intermediate_db_consolidation",
            "Consolidate Underutilized Databases",
            "intermediate",
            "infrastructure_change",
            "Merge multiple small databases into fewer larger instances",
            "40-60%",
            "6-8 weeks",
            List.of("Database administration", "Schema design", "Migration tools"),
            Map.of("consolidation_ratio", "4:1", "isolation_method", "schema")
        ));
        
        // 27. CDN Strategy
        templates.add(createTemplate(
            "intermediate_cdn_strategy",
            "Implement Global CDN Strategy",
            "intermediate",
            "infrastructure_change",
            "Optimize content delivery with strategic CDN usage",
            "20-35%",
            "3-4 weeks",
            List.of("CDN architecture", "Cache strategies", "Global deployment"),
            Map.of("cache_strategy", "tiered", "origin_shield", "enabled")
        ));
        
        // 28. Serverless Migration
        templates.add(createTemplate(
            "intermediate_serverless",
            "Migrate Microservices to Serverless",
            "intermediate",
            "infrastructure_change",
            "Convert suitable microservices to Lambda/Fargate",
            "40-70%",
            "8-10 weeks",
            List.of("Serverless patterns", "API Gateway", "Event-driven design"),
            Map.of("migration_pattern", "strangler_fig", "api_type", "http_api")
        ));
        
        // 29. Data Lake Optimization
        templates.add(createTemplate(
            "intermediate_data_lake",
            "Optimize Data Lake Storage Tiers",
            "intermediate",
            "cost_optimization",
            "Implement intelligent tiering for data lake storage",
            "35-50%",
            "4-5 weeks",
            List.of("S3 storage classes", "Data lifecycle", "AWS Glue"),
            Map.of("hot_data_days", "7", "warm_data_days", "30", "cold_storage", "glacier")
        ));
        
        // 30. Network Optimization
        templates.add(createTemplate(
            "intermediate_network_opt",
            "Optimize Network Architecture",
            "intermediate",
            "infrastructure_change",
            "Implement VPC endpoints and optimize routing",
            "15-25%",
            "3-4 weeks",
            List.of("VPC advanced", "PrivateLink", "Network design"),
            Map.of("endpoint_services", ["s3", "dynamodb", "ec2"], "nat_consolidation", "true")
        ));
        
        // 31. Hybrid RI/Spot Strategy
        templates.add(createTemplate(
            "intermediate_hybrid_compute",
            "Implement Hybrid RI/Spot Strategy",
            "intermediate",
            "reserved_instances",
            "Balance Reserved Instances with Spot for optimal savings",
            "45-65%",
            "4-6 weeks",
            List.of("Capacity planning", "Spot strategies", "Risk management"),
            Map.of("ri_baseline", "60%", "spot_peak", "30%", "ondemand_buffer", "10%")
        ));
        
        // 32. ElastiCache Optimization
        templates.add(createTemplate(
            "intermediate_caching",
            "Implement Intelligent Caching Strategy",
            "intermediate",
            "infrastructure_change",
            "Reduce database load with strategic caching",
            "25-40%",
            "3-4 weeks",
            List.of("ElastiCache", "Caching patterns", "Redis/Memcached"),
            Map.of("cache_strategy", "write_through", "ttl_strategy", "adaptive")
        ));
        
        // 33. GPU Instance Optimization
        templates.add(createTemplate(
            "intermediate_gpu_optimization",
            "Optimize GPU Instance Usage",
            "intermediate",
            "rightsizing",
            "Share GPU instances across multiple workloads",
            "50-70%",
            "4-5 weeks",
            List.of("GPU computing", "Instance scheduling", "CUDA basics"),
            Map.of("sharing_method", "time_slicing", "utilization_target", "85%")
        ));
        
        // 34. Backup Strategy Overhaul
        templates.add(createTemplate(
            "intermediate_backup_strategy",
            "Implement Tiered Backup Strategy",
            "intermediate",
            "cost_optimization",
            "Optimize backup retention with tiered storage",
            "30-45%",
            "3-4 weeks",
            List.of("AWS Backup", "Lifecycle policies", "Compliance"),
            Map.of("tier1_retention", "7d", "tier2_retention", "30d", "archive_retention", "1y")
        ));
        
        // 35. API Gateway Optimization
        templates.add(createTemplate(
            "intermediate_api_optimization",
            "Optimize API Gateway Usage",
            "intermediate",
            "cost_optimization",
            "Implement caching and request optimization",
            "20-35%",
            "2-3 weeks",
            List.of("API Gateway advanced", "Caching", "Rate limiting"),
            Map.of("cache_ttl", "300", "throttling_rate", "10000", "burst_rate", "5000")
        ));
        
        // 36. EMR Optimization
        templates.add(createTemplate(
            "intermediate_emr_optimization",
            "Optimize EMR Cluster Usage",
            "intermediate",
            "spot_instances",
            "Implement EMR with Spot Instances for big data workloads",
            "60-75%",
            "4-5 weeks",
            List.of("EMR", "Spark optimization", "Hadoop"),
            Map.of("spot_timeout", "10m", "core_on_demand", "true", "task_spot", "100%")
        ));
        
        // 37. Cross-Account Optimization
        templates.add(createTemplate(
            "intermediate_cross_account",
            "Optimize Cross-Account Architecture",
            "intermediate",
            "infrastructure_change",
            "Consolidate resources and implement account vending",
            "15-25%",
            "6-8 weeks",
            List.of("Organizations", "Control Tower", "Account strategy"),
            Map.of("account_structure", "by_environment", "shared_services", "centralized")
        ));
        
        // 38. Kubernetes Cost Allocation
        templates.add(createTemplate(
            "intermediate_k8s_cost",
            "Implement Kubernetes Cost Allocation",
            "intermediate",
            "cost_optimization",
            "Deploy Kubecost for granular container cost visibility",
            "10-20%",
            "2-3 weeks",
            List.of("Kubernetes", "Cost allocation", "Monitoring"),
            Map.of("allocation_method", "namespace", "chargeback", "enabled")
        ));
        
        // 39. SageMaker Optimization
        templates.add(createTemplate(
            "intermediate_sagemaker",
            "Optimize SageMaker Training Jobs",
            "intermediate",
            "spot_instances",
            "Use Spot Instances for ML training workloads",
            "70-85%",
            "3-4 weeks",
            List.of("SageMaker", "ML workflows", "Spot training"),
            Map.of("spot_interruption", "checkpoint", "managed_spot", "true")
        ));
        
        // 40. Compliance-Driven Optimization
        templates.add(createTemplate(
            "intermediate_compliance_opt",
            "Optimize Within Compliance Constraints",
            "intermediate",
            "cost_optimization",
            "Reduce costs while maintaining compliance requirements",
            "20-30%",
            "4-6 weeks",
            List.of("Compliance frameworks", "Security", "Audit logging"),
            Map.of("compliance_standard", "SOC2", "data_residency", "maintained")
        ));
        
        return templates;
    }
    
    // Advanced Templates (15 templates)
    public static List<Map<String, Object>> getAdvancedTemplates() {
        List<Map<String, Object>> templates = new ArrayList<>();
        
        // 41. Multi-Cloud Optimization
        templates.add(createTemplate(
            "advanced_multi_cloud",
            "Implement Multi-Cloud Cost Arbitrage",
            "advanced",
            "infrastructure_change",
            "Distribute workloads across AWS, Azure, GCP based on cost",
            "30-45%",
            "12-16 weeks",
            List.of("Multi-cloud architecture", "Cloud APIs", "Terraform", "Cost modeling"),
            Map.of("primary_cloud", "AWS", "secondary_clouds", ["Azure", "GCP"], "workload_mobility", "containerized")
        ));
        
        // 42. Edge Computing Strategy
        templates.add(createTemplate(
            "advanced_edge_computing",
            "Implement Edge Computing Architecture",
            "advanced",
            "infrastructure_change",
            "Deploy compute at edge locations for latency and cost optimization",
            "25-40%",
            "10-12 weeks",
            List.of("Edge computing", "Lambda@Edge", "CloudFront Functions", "IoT"),
            Map.of("edge_locations", "global", "compute_distribution", "70_30")
        ));
        
        // 43. FinOps Platform
        templates.add(createTemplate(
            "advanced_finops_platform",
            "Build Enterprise FinOps Platform",
            "advanced",
            "cost_optimization",
            "Implement comprehensive cost management platform with ML insights",
            "35-50%",
            "16-20 weeks",
            List.of("FinOps practices", "ML/AI", "Data engineering", "Platform development"),
            Map.of("ml_models", ["anomaly_detection", "forecasting", "recommendation"], "automation_level", "full")
        ));
        
        // 44. Chaos Engineering Cost
        templates.add(createTemplate(
            "advanced_chaos_cost",
            "Implement Cost-Aware Chaos Engineering",
            "advanced",
            "infrastructure_change",
            "Build resilient architecture optimized for cost during failures",
            "20-35%",
            "8-10 weeks",
            List.of("Chaos engineering", "Resilience patterns", "Cost modeling", "Automation"),
            Map.of("failure_scenarios", ["az_failure", "region_failure"], "cost_threshold", "150%")
        ));
        
        // 45. Quantum-Ready Architecture
        templates.add(createTemplate(
            "advanced_quantum_ready",
            "Design Quantum-Ready Hybrid Architecture",
            "advanced",
            "infrastructure_change",
            "Prepare infrastructure for quantum computing integration",
            "15-25%",
            "12-16 weeks",
            List.of("Quantum computing", "Hybrid architectures", "Future-proofing"),
            Map.of("quantum_services", ["Braket", "optimization"], "classical_quantum_split", "90_10")
        ));
        
        // 46. Zero-Trust Cost Optimization
        templates.add(createTemplate(
            "advanced_zero_trust",
            "Optimize Zero-Trust Security Architecture",
            "advanced",
            "infrastructure_change",
            "Implement zero-trust while optimizing security costs",
            "10-20%",
            "10-14 weeks",
            List.of("Zero-trust architecture", "Security services", "Identity management"),
            Map.of("security_layers", "5", "consolidation_strategy", "platform_based")
        ));
        
        // 47. AI-Driven Auto-Optimization
        templates.add(createTemplate(
            "advanced_ai_optimization",
            "Implement AI-Driven Infrastructure Optimization",
            "advanced",
            "cost_optimization",
            "Deploy AI system for real-time cost optimization decisions",
            "40-60%",
            "16-20 weeks",
            List.of("AI/ML", "Real-time systems", "Automation", "Decision systems"),
            Map.of("ml_models", ["reinforcement_learning", "prediction"], "decision_frequency", "hourly")
        ));
        
        // 48. Sustainability Optimization
        templates.add(createTemplate(
            "advanced_sustainability",
            "Optimize for Cost and Carbon Footprint",
            "advanced",
            "infrastructure_change",
            "Balance cost optimization with sustainability goals",
            "20-30%",
            "8-12 weeks",
            List.of("Sustainability", "Carbon accounting", "Green computing"),
            Map.of("carbon_reduction_target", "40%", "renewable_regions", ["us-west-2", "eu-north-1"])
        ));
        
        // 49. Event-Driven Cost Architecture
        templates.add(createTemplate(
            "advanced_event_driven",
            "Build Event-Driven Cost-Optimized Platform",
            "advanced",
            "infrastructure_change",
            "Implement fully event-driven architecture for maximum efficiency",
            "50-70%",
            "14-18 weeks",
            List.of("Event-driven architecture", "Serverless", "Event streaming", "CQRS"),
            Map.of("event_bus", "custom", "processing_model", "async", "storage_pattern", "event_sourcing")
        ));
        
        // 50. Blockchain Cost Integration
        templates.add(createTemplate(
            "advanced_blockchain",
            "Integrate Blockchain for Cost Transparency",
            "advanced",
            "infrastructure_change",
            "Implement blockchain-based cost tracking and chargeback",
            "5-15%",
            "10-14 weeks",
            List.of("Blockchain", "Smart contracts", "Distributed systems"),
            Map.of("blockchain_platform", "managed", "consensus", "proof_of_authority")
        ));
        
        // 51. Mesh Architecture Optimization
        templates.add(createTemplate(
            "advanced_service_mesh",
            "Optimize Service Mesh Architecture",
            "advanced",
            "infrastructure_change",
            "Implement cost-efficient service mesh with intelligent routing",
            "25-35%",
            "8-10 weeks",
            List.of("Service mesh", "Istio/Linkerd", "Microservices", "Observability"),
            Map.of("mesh_type", "istio", "sidecar_optimization", "enabled", "routing_strategy", "cost_aware")
        ));
        
        // 52. Quantum-Safe Migration
        templates.add(createTemplate(
            "advanced_quantum_safe",
            "Migrate to Quantum-Safe Cryptography",
            "advanced",
            "infrastructure_change",
            "Future-proof security with post-quantum cryptography",
            "5-10%",
            "12-16 weeks",
            List.of("Cryptography", "Quantum computing", "Security architecture"),
            Map.of("algorithm_type", "lattice_based", "migration_strategy", "hybrid")
        ));
        
        // 53. Autonomous Infrastructure
        templates.add(createTemplate(
            "advanced_autonomous",
            "Build Self-Optimizing Infrastructure",
            "advanced",
            "cost_optimization",
            "Create infrastructure that autonomously optimizes for cost",
            "45-65%",
            "20-24 weeks",
            List.of("Autonomous systems", "ML ops", "Control theory", "Distributed AI"),
            Map.of("autonomy_level", "4", "human_oversight", "exception_only")
        ));
        
        // 54. Metaverse-Ready Architecture
        templates.add(createTemplate(
            "advanced_metaverse",
            "Design Metaverse-Scale Infrastructure",
            "advanced",
            "infrastructure_change",
            "Build cost-efficient infrastructure for metaverse applications",
            "30-40%",
            "16-20 weeks",
            List.of("3D rendering", "Real-time systems", "Edge computing", "WebRTC"),
            Map.of("rendering_strategy", "hybrid_cloud_edge", "latency_target", "20ms")
        ));
        
        // 55. Neuromorphic Computing
        templates.add(createTemplate(
            "advanced_neuromorphic",
            "Integrate Neuromorphic Computing",
            "advanced",
            "infrastructure_change",
            "Leverage neuromorphic chips for AI workload optimization",
            "60-80%",
            "18-24 weeks",
            List.of("Neuromorphic computing", "Spiking neural networks", "Hardware acceleration"),
            Map.of("workload_type", "inference", "chip_architecture", "event_driven")
        ));
        
        return templates;
    }
    
    // Helper method to create template
    private static Map<String, Object> createTemplate(
            String id,
            String name,
            String difficulty,
            String type,
            String description,
            String estimatedSavings,
            String timeToComplete,
            List<String> skillsRequired,
            Map<String, Object> parameters) {
        
        Map<String, Object> template = new HashMap<>();
        template.put("id", id);
        template.put("name", name);
        template.put("difficulty", difficulty);
        template.put("type", type);
        template.put("description", description);
        template.put("estimatedSavings", estimatedSavings);
        template.put("timeToComplete", timeToComplete);
        template.put("skillsRequired", skillsRequired);
        template.put("parameters", parameters);
        template.put("category", categorizeTemplate(type));
        template.put("complexity", calculateComplexity(difficulty, skillsRequired.size()));
        template.put("prerequisites", generatePrerequisites(difficulty, type));
        template.put("learningObjectives", generateLearningObjectives(type, difficulty));
        
        return template;
    }
    
    private static String categorizeTemplate(String type) {
        return switch (type) {
            case "rightsizing" -> "Resource Optimization";
            case "reserved_instances" -> "Commitment Optimization";
            case "spot_instances" -> "Spot Strategy";
            case "scaling" -> "Scaling Optimization";
            case "infrastructure_change" -> "Architecture Transformation";
            default -> "General Optimization";
        };
    }
    
    private static int calculateComplexity(String difficulty, int skillCount) {
        int base = switch (difficulty) {
            case "beginner" -> 1;
            case "intermediate" -> 4;
            case "advanced" -> 7;
            default -> 1;
        };
        return Math.min(base + (skillCount / 3), 10);
    }
    
    private static List<String> generatePrerequisites(String difficulty, String type) {
        List<String> prerequisites = new ArrayList<>();
        
        switch (difficulty) {
            case "beginner" -> {
                prerequisites.add("Basic AWS Console navigation");
                prerequisites.add("Understanding of cloud cost concepts");
            }
            case "intermediate" -> {
                prerequisites.add("Completed 5+ beginner scenarios");
                prerequisites.add("Understanding of AWS service architectures");
                prerequisites.add("Basic scripting knowledge");
            }
            case "advanced" -> {
                prerequisites.add("Completed 10+ intermediate scenarios");
                prerequisites.add("Deep understanding of distributed systems");
                prerequisites.add("Programming proficiency in Python/Java");
                prerequisites.add("Infrastructure as Code experience");
            }
        }
        
        return prerequisites;
    }
    
    private static List<String> generateLearningObjectives(String type, String difficulty) {
        List<String> objectives = new ArrayList<>();
        
        // Base objectives
        objectives.add("Understand cost drivers for " + type + " scenarios");
        objectives.add("Learn to identify optimization opportunities");
        objectives.add("Practice cost-benefit analysis");
        
        // Type-specific objectives
        switch (type) {
            case "rightsizing" -> {
                objectives.add("Master resource utilization analysis");
                objectives.add("Learn performance testing methodologies");
            }
            case "reserved_instances" -> {
                objectives.add("Understand commitment strategies");
                objectives.add("Learn capacity planning techniques");
            }
            case "spot_instances" -> {
                objectives.add("Master fault-tolerant design patterns");
                objectives.add("Understand spot market dynamics");
            }
            case "infrastructure_change" -> {
                objectives.add("Learn migration planning and risk assessment");
                objectives.add("Understand architectural trade-offs");
            }
        }
        
        return objectives;
    }
    
    // Get all templates
    public static List<Map<String, Object>> getAllTemplates() {
        List<Map<String, Object>> allTemplates = new ArrayList<>();
        allTemplates.addAll(getBeginnerTemplates());
        allTemplates.addAll(getIntermediateTemplates());
        allTemplates.addAll(getAdvancedTemplates());
        return allTemplates;
    }
    
    // Get templates by difficulty
    public static List<Map<String, Object>> getTemplatesByDifficulty(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "beginner" -> getBeginnerTemplates();
            case "intermediate" -> getIntermediateTemplates();
            case "advanced" -> getAdvancedTemplates();
            default -> getAllTemplates();
        };
    }
    
    // Get templates by type
    public static List<Map<String, Object>> getTemplatesByType(String type) {
        return getAllTemplates().stream()
            .filter(template -> type.equals(template.get("type")))
            .toList();
    }
    
    // Get template by ID
    public static Map<String, Object> getTemplateById(String id) {
        return getAllTemplates().stream()
            .filter(template -> id.equals(template.get("id")))
            .findFirst()
            .orElse(null);
    }
}