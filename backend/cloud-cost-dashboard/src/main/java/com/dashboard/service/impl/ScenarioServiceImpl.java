package com.dashboard.service.impl;

import com.dashboard.service.interfaces.ScenarioService;
import com.dashboard.model.scenario.WhatIfScenario;
import com.dashboard.model.scenario.ScenarioComparison;
import com.dashboard.dto.scenario.CreateScenarioRequest;
import com.dashboard.dto.scenario.ScenarioComparisonRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ScenarioServiceImpl implements ScenarioService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public WhatIfScenario createScenario(CreateScenarioRequest request) {
        System.out.println("Creating scenario: " + request.getName());
        
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid scenario request: " + request);
        }
        
        // Generate scenario ID
        String scenarioId = UUID.randomUUID().toString();
        
        // Create scenario object
        WhatIfScenario scenario = new WhatIfScenario(request.getName(), request.getDescription(), request.getType());
        scenario.setId(scenarioId);
        scenario.setParameters(request.getParameters());
        scenario.setDifficultyLevel(request.getDifficultyLevel());
        scenario.setTimeHorizonDays(request.getTimeHorizonDays());
        scenario.setCreatedBy(request.getCreatedBy());
        
        // Get baseline data based on scope and target
        Map<String, Object> baselineData = fetchBaselineData(request);
        scenario.setBaselineData(baselineData);
        
        // Generate what-if projections based on scenario type
        Map<String, Object> projectedData = generateProjections(request, baselineData);
        scenario.setProjectedData(projectedData);
        
        // Calculate impact analysis
        WhatIfScenario.ScenarioImpact impact = calculateScenarioImpact(baselineData, projectedData, request);
        scenario.setImpact(impact);
        
        // Assess implementation risk
        WhatIfScenario.RiskAssessment riskAssessment = assessScenarioRisk(request, impact);
        scenario.setRiskAssessment(riskAssessment);
        
        // Generate implementation steps
        List<String> implementationSteps = generateImplementationSteps(request);
        scenario.setImplementationSteps(implementationSteps);
        
        scenario.setStatus("completed");
        
        // Save scenario to database
        saveScenario(scenario);
        
        System.out.println("Created scenario with ID: " + scenarioId);
        return scenario;
    }

    @Override
    public ScenarioComparison compareScenarios(ScenarioComparisonRequest request) {
        System.out.println("Comparing " + request.getScenarioCount() + " scenarios");
        
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid comparison request: " + request);
        }
        
        // Fetch scenarios from database
        List<WhatIfScenario> scenarios = fetchScenarios(request.getScenarioIds());
        
        if (scenarios.size() != request.getScenarioIds().size()) {
            throw new RuntimeException("Some scenarios not found. Expected: " + request.getScenarioIds().size() 
                                     + ", Found: " + scenarios.size());
        }
        
        // Create comparison object
        ScenarioComparison comparison = new ScenarioComparison(request.getScenarioIds(), request.getAnalysisMethod());
        comparison.setId(UUID.randomUUID().toString());
        comparison.setCreatedBy(request.getCreatedBy());
        
        // Convert scenarios to summaries
        List<ScenarioComparison.ScenarioSummary> summaries = scenarios.stream()
            .map(this::convertToSummary)
            .toList();
        comparison.setScenarios(summaries);
        
        // Determine best and worst scenarios based on analysis method
        ScenarioComparison.ScenarioSummary bestScenario = findBestScenario(summaries, request);
        ScenarioComparison.ScenarioSummary worstScenario = findWorstScenario(summaries, request);
        comparison.setBestScenario(bestScenario);
        comparison.setWorstScenario(worstScenario);
        
        // Generate comparison metrics
        Map<String, Object> comparisonMetrics = generateComparisonMetrics(summaries, request);
        comparison.setComparisonMetrics(comparisonMetrics);
        
        // Generate recommendations if requested
        if (request.isIncludeRecommendations()) {
            List<String> recommendations = generateComparisonRecommendations(summaries, request);
            comparison.setRecommendations(recommendations);
        }
        
        System.out.println("Completed scenario comparison");
        return comparison;
    }

    @Override
    public List<Object> getScenarioTemplates(String difficultyLevel) {
        System.out.println("Fetching scenario templates for difficulty: " + difficultyLevel);
        
        List<Object> templates = new ArrayList<>();
        
        // Use the comprehensive ScenarioTemplates class
        if (difficultyLevel == null || difficultyLevel.isEmpty()) {
            templates.addAll(com.dashboard.templates.ScenarioTemplates.getAllTemplates());
        } else {
            templates.addAll(com.dashboard.templates.ScenarioTemplates.getTemplatesByDifficulty(difficultyLevel));
        }
        
        System.out.println("Retrieved " + templates.size() + " scenario templates");
        return templates;
    }

    @Override
    public Object validateScenario(CreateScenarioRequest request) {
        System.out.println("Validating scenario: " + request.getName());
        
        Map<String, Object> validation = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Basic validation
        if (!request.isValid()) {
            errors.add("Invalid basic request parameters");
        }
        
        // Parameter-specific validation based on scenario type
        validateScenarioParameters(request, errors, warnings);
        
        // Feasibility assessment
        Map<String, Object> feasibility = assessScenarioFeasibility(request);
        
        // Complexity estimation
        int complexityScore = estimateComplexity(request);
        String complexityLevel = switch (complexityScore) {
            case 1, 2, 3 -> "low";
            case 4, 5, 6 -> "medium";
            case 7, 8 -> "high";
            default -> "very_high";
        };
        
        validation.put("isValid", errors.isEmpty());
        validation.put("errors", errors);
        validation.put("warnings", warnings);
        validation.put("feasibility", feasibility);
        validation.put("complexityScore", complexityScore);
        validation.put("complexityLevel", complexityLevel);
        validation.put("estimatedDuration", estimateImplementationDuration(complexityScore));
        validation.put("requiredSkills", getRequiredSkills(request.getType()));
        
        return validation;
    }

    // ========================================
    // PRIVATE HELPER METHODS
    // ========================================

    private Map<String, Object> fetchBaselineData(CreateScenarioRequest request) {
        String sql = """
            SELECT 
                DATE(date) as day,
                service_name,
                SUM(cost) as daily_cost,
                SUM(usage_quantity) as daily_usage,
                usage_unit
            FROM enhanced_usage_records
            WHERE date >= DATE_SUB(CURRENT_DATE, INTERVAL ? DAY)
            """;
        
        List<Object> params = new ArrayList<>();
        params.add(Math.max(request.getTimeHorizonDays(), 30)); // At least 30 days of history
        
        // Add scope filtering
        if ("team".equals(request.getScope()) && request.getTarget() != null) {
            sql += " AND team_name = ?";
            params.add(request.getTarget());
        } else if ("service".equals(request.getScope()) && request.getTarget() != null) {
            sql += " AND service_name = ?";
            params.add(request.getTarget());
        }
        
        sql += " GROUP BY DATE(date), service_name ORDER BY day, service_name";
        
        List<Map<String, Object>> rawData = jdbcTemplate.queryForList(sql, params.toArray());
        
        // Process and aggregate baseline data
        Map<String, Object> baseline = new HashMap<>();
        baseline.put("rawData", rawData);
        baseline.put("totalCost", calculateTotalCost(rawData));
        baseline.put("averageDailyCost", calculateAverageDailyCost(rawData));
        baseline.put("serviceBreakdown", calculateServiceBreakdown(rawData));
        baseline.put("trendAnalysis", analyzeCostTrend(rawData));
        
        return baseline;
    }

    private Map<String, Object> generateProjections(CreateScenarioRequest request, Map<String, Object> baselineData) {
        Map<String, Object> projections = new HashMap<>();
        
        // Generate projections based on scenario type
        switch (request.getType()) {
            case "cost_optimization" -> projections = generateCostOptimizationProjections(request, baselineData);
            case "rightsizing" -> projections = generateRightsizingProjections(request, baselineData);
            case "reserved_instances" -> projections = generateReservedInstanceProjections(request, baselineData);
            case "spot_instances" -> projections = generateSpotInstanceProjections(request, baselineData);
            case "scaling" -> projections = generateScalingProjections(request, baselineData);
            case "infrastructure_change" -> projections = generateInfrastructureChangeProjections(request, baselineData);
            default -> projections = generateGenericProjections(request, baselineData);
        }
        
        return projections;
    }

    private Map<String, Object> generateCostOptimizationProjections(CreateScenarioRequest request, Map<String, Object> baselineData) {
        Map<String, Object> projections = new HashMap<>();
        
        BigDecimal baselineCost = (BigDecimal) baselineData.get("totalCost");
        Double optimizationPercentage = request.getParameterAsDouble("optimization_percentage");
        if (optimizationPercentage == null) optimizationPercentage = 15.0; // Default 15% optimization
        
        BigDecimal projectedCost = baselineCost.multiply(
            BigDecimal.valueOf(1.0 - (optimizationPercentage / 100.0))
        ).setScale(2, RoundingMode.HALF_UP);
        
        projections.put("totalCost", projectedCost);
        projections.put("costSavings", baselineCost.subtract(projectedCost));
        projections.put("optimizationPercentage", optimizationPercentage);
        projections.put("projectionMethod", "percentage_reduction");
        
        return projections;
    }

    private Map<String, Object> generateRightsizingProjections(CreateScenarioRequest request, Map<String, Object> baselineData) {
        Map<String, Object> projections = new HashMap<>();
        
        BigDecimal baselineCost = (BigDecimal) baselineData.get("totalCost");
        String instanceType = request.getParameterAsString("target_instance_type");
        Double rightsizingReduction = request.getParameterAsDouble("rightsizing_reduction");
        if (rightsizingReduction == null) rightsizingReduction = 25.0; // Default 25% reduction
        
        BigDecimal projectedCost = baselineCost.multiply(
            BigDecimal.valueOf(1.0 - (rightsizingReduction / 100.0))
        ).setScale(2, RoundingMode.HALF_UP);
        
        projections.put("totalCost", projectedCost);
        projections.put("costSavings", baselineCost.subtract(projectedCost));
        projections.put("rightsizingReduction", rightsizingReduction);
        projections.put("targetInstanceType", instanceType);
        projections.put("projectionMethod", "rightsizing");
        
        return projections;
    }

    private Map<String, Object> generateReservedInstanceProjections(CreateScenarioRequest request, Map<String, Object> baselineData) {
        Map<String, Object> projections = new HashMap<>();
        
        BigDecimal baselineCost = (BigDecimal) baselineData.get("totalCost");
        String riTerm = request.getParameterAsString("ri_term"); // 1year, 3year
        Double riSavings = "3year".equals(riTerm) ? 40.0 : 30.0; // 3-year RIs save more
        
        BigDecimal projectedCost = baselineCost.multiply(
            BigDecimal.valueOf(1.0 - (riSavings / 100.0))
        ).setScale(2, RoundingMode.HALF_UP);
        
        projections.put("totalCost", projectedCost);
        projections.put("costSavings", baselineCost.subtract(projectedCost));
        projections.put("riSavingsPercentage", riSavings);
        projections.put("riTerm", riTerm);
        projections.put("projectionMethod", "reserved_instances");
        
        return projections;
    }

    private Map<String, Object> generateSpotInstanceProjections(CreateScenarioRequest request, Map<String, Object> baselineData) {
        Map<String, Object> projections = new HashMap<>();
        
        BigDecimal baselineCost = (BigDecimal) baselineData.get("totalCost");
        Double spotPercentage = request.getParameterAsDouble("spot_percentage");
        if (spotPercentage == null) spotPercentage = 50.0; // Default 50% spot usage
        
        // Spot instances typically save 70-90% but have interruption risk
        Double spotSavings = 75.0;
        BigDecimal spotCostReduction = baselineCost.multiply(
            BigDecimal.valueOf((spotPercentage / 100.0) * (spotSavings / 100.0))
        );
        
        BigDecimal projectedCost = baselineCost.subtract(spotCostReduction).setScale(2, RoundingMode.HALF_UP);
        
        projections.put("totalCost", projectedCost);
        projections.put("costSavings", spotCostReduction);
        projections.put("spotPercentage", spotPercentage);
        projections.put("spotSavingsRate", spotSavings);
        projections.put("interruptionRisk", "medium");
        projections.put("projectionMethod", "spot_instances");
        
        return projections;
    }

    private Map<String, Object> generateScalingProjections(CreateScenarioRequest request, Map<String, Object> baselineData) {
        Map<String, Object> projections = new HashMap<>();
        
        BigDecimal baselineCost = (BigDecimal) baselineData.get("totalCost");
        Double scalingFactor = request.getParameterAsDouble("scaling_factor");
        if (scalingFactor == null) scalingFactor = 1.5; // Default 50% scale up
        
        BigDecimal projectedCost = baselineCost.multiply(
            BigDecimal.valueOf(scalingFactor)
        ).setScale(2, RoundingMode.HALF_UP);
        
        projections.put("totalCost", projectedCost);
        projections.put("costIncrease", projectedCost.subtract(baselineCost));
        projections.put("scalingFactor", scalingFactor);
        projections.put("projectionMethod", "scaling");
        
        return projections;
    }

    private Map<String, Object> generateInfrastructureChangeProjections(CreateScenarioRequest request, Map<String, Object> baselineData) {
        Map<String, Object> projections = new HashMap<>();
        
        BigDecimal baselineCost = (BigDecimal) baselineData.get("totalCost");
        String changeType = request.getParameterAsString("change_type");
        Double impactPercentage = request.getParameterAsDouble("impact_percentage");
        if (impactPercentage == null) impactPercentage = 10.0; // Default 10% impact
        
        BigDecimal multiplier = "upgrade".equals(changeType) ? 
            BigDecimal.valueOf(1.0 + (impactPercentage / 100.0)) :
            BigDecimal.valueOf(1.0 - (impactPercentage / 100.0));
        
        BigDecimal projectedCost = baselineCost.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
        
        projections.put("totalCost", projectedCost);
        projections.put("costDifference", projectedCost.subtract(baselineCost));
        projections.put("changeType", changeType);
        projections.put("impactPercentage", impactPercentage);
        projections.put("projectionMethod", "infrastructure_change");
        
        return projections;
    }

    private Map<String, Object> generateGenericProjections(CreateScenarioRequest request, Map<String, Object> baselineData) {
        Map<String, Object> projections = new HashMap<>();
        
        BigDecimal baselineCost = (BigDecimal) baselineData.get("totalCost");
        Double changePercentage = request.getParameterAsDouble("change_percentage");
        if (changePercentage == null) changePercentage = 5.0; // Default 5% change
        
        BigDecimal projectedCost = baselineCost.multiply(
            BigDecimal.valueOf(1.0 + (changePercentage / 100.0))
        ).setScale(2, RoundingMode.HALF_UP);
        
        projections.put("totalCost", projectedCost);
        projections.put("costDifference", projectedCost.subtract(baselineCost));
        projections.put("changePercentage", changePercentage);
        projections.put("projectionMethod", "generic");
        
        return projections;
    }

    private WhatIfScenario.ScenarioImpact calculateScenarioImpact(Map<String, Object> baseline, Map<String, Object> projected, CreateScenarioRequest request) {
        WhatIfScenario.ScenarioImpact impact = new WhatIfScenario.ScenarioImpact();
        
        BigDecimal baselineCost = (BigDecimal) baseline.get("totalCost");
        BigDecimal projectedCost = (BigDecimal) projected.get("totalCost");
        BigDecimal costDifference = projectedCost.subtract(baselineCost);
        
        impact.setTotalCostDifference(costDifference);
        
        double percentageChange = baselineCost.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
            costDifference.divide(baselineCost, 4, RoundingMode.HALF_UP)
                         .multiply(BigDecimal.valueOf(100)).doubleValue();
        impact.setTotalPercentageChange(percentageChange);
        
        BigDecimal dailySavings = costDifference.divide(
            BigDecimal.valueOf(request.getTimeHorizonDays()), 4, RoundingMode.HALF_UP);
        impact.setAverageDailySavings(dailySavings);
        
        // Generate service-level impacts (simplified)
        Map<String, BigDecimal> serviceImpacts = new HashMap<>();
        serviceImpacts.put("EC2", costDifference.multiply(BigDecimal.valueOf(0.6)));
        serviceImpacts.put("S3", costDifference.multiply(BigDecimal.valueOf(0.2)));
        serviceImpacts.put("RDS", costDifference.multiply(BigDecimal.valueOf(0.2)));
        impact.setServiceImpacts(serviceImpacts);
        
        // Benefits and risks
        List<String> benefitsAndRisks = generateBenefitsAndRisks(request, costDifference);
        impact.setBenefitsAndRisks(benefitsAndRisks);
        
        return impact;
    }

    private WhatIfScenario.RiskAssessment assessScenarioRisk(CreateScenarioRequest request, WhatIfScenario.ScenarioImpact impact) {
        WhatIfScenario.RiskAssessment risk = new WhatIfScenario.RiskAssessment();
        
        // Calculate risk score based on multiple factors
        double riskScore = 0.0;
        List<String> riskFactors = new ArrayList<>();
        
        // Cost change magnitude risk
        double percentageChange = Math.abs(impact.getTotalPercentageChange());
        if (percentageChange > 50) {
            riskScore += 0.3;
            riskFactors.add("High cost impact (>" + percentageChange + "%)");
        } else if (percentageChange > 25) {
            riskScore += 0.2;
            riskFactors.add("Moderate cost impact (" + percentageChange + "%)");
        }
        
        // Scenario type risk
        switch (request.getType()) {
            case "spot_instances" -> {
                riskScore += 0.3;
                riskFactors.add("Spot instance interruption risk");
            }
            case "infrastructure_change" -> {
                riskScore += 0.25;
                riskFactors.add("Infrastructure migration complexity");
            }
            case "scaling" -> {
                riskScore += 0.2;
                riskFactors.add("Scaling operation complexity");
            }
            case "cost_optimization", "rightsizing" -> {
                riskScore += 0.1;
                riskFactors.add("Well-established optimization approach");
            }
        }
        
        // Difficulty level risk
        switch (request.getDifficultyLevel()) {
            case "advanced" -> {
                riskScore += 0.2;
                riskFactors.add("Advanced implementation complexity");
            }
            case "intermediate" -> {
                riskScore += 0.1;
                riskFactors.add("Moderate implementation complexity");
            }
            case "beginner" -> {
                riskFactors.add("Simple implementation");
            }
        }
        
        // Determine risk level
        String riskLevel;
        if (riskScore >= 0.7) {
            riskLevel = "critical";
        } else if (riskScore >= 0.5) {
            riskLevel = "high";
        } else if (riskScore >= 0.3) {
            riskLevel = "medium";
        } else {
            riskLevel = "low";
        }
        
        risk.setLevel(riskLevel);
        risk.setFactors(riskFactors);
        risk.setConfidenceScore(1.0 - riskScore); // Inverse relationship
        
        // Generate mitigation strategies
        Map<String, String> mitigationStrategies = generateMitigationStrategies(request.getType(), riskLevel);
        risk.setMitigationStrategies(mitigationStrategies);
        
        return risk;
    }

    private List<String> generateImplementationSteps(CreateScenarioRequest request) {
        return switch (request.getType()) {
            case "cost_optimization" -> List.of(
                "Analyze current resource utilization",
                "Identify optimization opportunities",
                "Create implementation plan with rollback strategy",
                "Execute optimizations in phases",
                "Monitor cost impact and performance"
            );
            case "rightsizing" -> List.of(
                "Collect instance utilization data over 2-4 weeks",
                "Analyze CPU, memory, and network usage patterns",
                "Identify over-provisioned instances",
                "Test application performance on smaller instance types",
                "Schedule maintenance window for rightsizing",
                "Monitor performance after changes"
            );
            case "reserved_instances" -> List.of(
                "Analyze historical usage patterns for 3+ months",
                "Calculate optimal Reserved Instance mix",
                "Purchase Reserved Instances for stable workloads",
                "Monitor utilization and optimize mix quarterly",
                "Track savings and ROI"
            );
            case "spot_instances" -> List.of(
                "Identify fault-tolerant workloads suitable for Spot",
                "Implement Spot instance request diversification",
                "Configure automatic failover to On-Demand",
                "Test interruption handling mechanisms",
                "Monitor Spot availability and pricing trends"
            );
            default -> List.of(
                "Conduct detailed analysis of current state",
                "Design implementation approach",
                "Create testing and rollback plan",
                "Execute changes in controlled manner",
                "Monitor results and adjust as needed"
            );
        };
    }

    private List<String> generateBenefitsAndRisks(CreateScenarioRequest request, BigDecimal costDifference) {
        List<String> benefitsAndRisks = new ArrayList<>();
        
        if (costDifference.compareTo(BigDecimal.ZERO) < 0) {
            benefitsAndRisks.add("Cost savings of $" + costDifference.abs().toString() + " over analysis period");
        } else {
            benefitsAndRisks.add("Additional cost of $" + costDifference.toString() + " over analysis period");
        }
        
        switch (request.getType()) {
            case "spot_instances" -> {
                benefitsAndRisks.add("Significant cost savings potential");
                benefitsAndRisks.add("Risk of workload interruptions");
                benefitsAndRisks.add("Requires fault-tolerant application design");
            }
            case "rightsizing" -> {
                benefitsAndRisks.add("Improved resource efficiency");
                benefitsAndRisks.add("Potential performance impact if undersized");
                benefitsAndRisks.add("Requires careful monitoring post-implementation");
            }
            case "reserved_instances" -> {
                benefitsAndRisks.add("Predictable cost savings");
                benefitsAndRisks.add("Commitment risk if usage patterns change");
                benefitsAndRisks.add("Limited flexibility for workload changes");
            }
        }
        
        return benefitsAndRisks;
    }

    private Map<String, String> generateMitigationStrategies(String scenarioType, String riskLevel) {
        Map<String, String> strategies = new HashMap<>();
        
        switch (scenarioType) {
            case "spot_instances" -> {
                strategies.put("interruption_handling", "Implement graceful shutdown and state preservation");
                strategies.put("diversification", "Use multiple instance types and availability zones");
                strategies.put("fallback", "Configure automatic fallback to On-Demand instances");
            }
            case "rightsizing" -> {
                strategies.put("performance_monitoring", "Continuous monitoring of application performance metrics");
                strategies.put("gradual_rollout", "Implement changes gradually with ability to rollback");
                strategies.put("capacity_planning", "Maintain buffer capacity for peak usage periods");
            }
            case "infrastructure_change" -> {
                strategies.put("testing", "Comprehensive testing in staging environment");
                strategies.put("rollback_plan", "Detailed rollback procedures and automation");
                strategies.put("monitoring", "Enhanced monitoring during transition period");
            }
        }
        
        if ("high".equals(riskLevel) || "critical".equals(riskLevel)) {
            strategies.put("change_management", "Formal change management process with approvals");
            strategies.put("communication", "Clear communication plan to stakeholders");
        }
        
        return strategies;
    }

    private BigDecimal calculateTotalCost(List<Map<String, Object>> rawData) {
        return rawData.stream()
            .map(row -> (BigDecimal) row.get("daily_cost"))
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateAverageDailyCost(List<Map<String, Object>> rawData) {
        if (rawData.isEmpty()) return BigDecimal.ZERO;
        
        BigDecimal totalCost = calculateTotalCost(rawData);
        long distinctDays = rawData.stream()
            .map(row -> row.get("day"))
            .distinct()
            .count();
        
        return distinctDays > 0 ? 
            totalCost.divide(BigDecimal.valueOf(distinctDays), 2, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
    }

    private Map<String, BigDecimal> calculateServiceBreakdown(List<Map<String, Object>> rawData) {
        return rawData.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                row -> (String) row.get("service_name"),
                java.util.stream.Collectors.reducing(
                    BigDecimal.ZERO,
                    row -> (BigDecimal) row.get("daily_cost"),
                    BigDecimal::add
                )
            ));
    }

    private Map<String, Object> analyzeCostTrend(List<Map<String, Object>> rawData) {
        Map<String, Object> trend = new HashMap<>();
        
        if (rawData.size() < 2) {
            trend.put("direction", "insufficient_data");
            return trend;
        }
        
        // Group by day and sum costs
        Map<String, BigDecimal> dailyCosts = rawData.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                row -> row.get("day").toString(),
                java.util.stream.Collectors.reducing(
                    BigDecimal.ZERO,
                    row -> (BigDecimal) row.get("daily_cost"),
                    BigDecimal::add
                )
            ));
        
        List<String> sortedDays = dailyCosts.keySet().stream().sorted().toList();
        if (sortedDays.size() < 2) {
            trend.put("direction", "insufficient_data");
            return trend;
        }
        
        BigDecimal firstDayCost = dailyCosts.get(sortedDays.get(0));
        BigDecimal lastDayCost = dailyCosts.get(sortedDays.get(sortedDays.size() - 1));
        
        if (lastDayCost.compareTo(firstDayCost) > 0) {
            trend.put("direction", "increasing");
        } else if (lastDayCost.compareTo(firstDayCost) < 0) {
            trend.put("direction", "decreasing");
        } else {
            trend.put("direction", "stable");
        }
        
        BigDecimal change = lastDayCost.subtract(firstDayCost);
        double changePercentage = firstDayCost.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
            change.divide(firstDayCost, 4, RoundingMode.HALF_UP)
                  .multiply(BigDecimal.valueOf(100)).doubleValue();
        
        trend.put("changeAmount", change);
        trend.put("changePercentage", changePercentage);
        
        return trend;
    }

    private void saveScenario(WhatIfScenario scenario) {
        String sql = """
            INSERT INTO scenarios 
            (id, name, description, type, status, parameters, baseline_data, projected_data,
             difficulty_level, time_horizon_days, created_by, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;
        
        try {
            jdbcTemplate.update(sql,
                scenario.getId(),
                scenario.getName(),
                scenario.getDescription(),
                scenario.getType(),
                scenario.getStatus(),
                convertMapToJson(scenario.getParameters()),
                convertMapToJson(scenario.getBaselineData()),
                convertMapToJson(scenario.getProjectedData()),
                scenario.getDifficultyLevel(),
                scenario.getTimeHorizonDays(),
                scenario.getCreatedBy()
            );
        } catch (Exception e) {
            System.err.println("Error saving scenario: " + e.getMessage());
            throw new RuntimeException("Failed to save scenario", e);
        }
    }

    private List<WhatIfScenario> fetchScenarios(List<String> scenarioIds) {
        if (scenarioIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        String sql = """
            SELECT id, name, description, type, status, parameters, baseline_data, projected_data,
                   difficulty_level, time_horizon_days, created_by, created_at, updated_at
            FROM scenarios 
            WHERE id IN (""" + String.join(",", Collections.nCopies(scenarioIds.size(), "?")) + ")";
        
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, scenarioIds.toArray());
        
        return rows.stream()
            .map(this::mapRowToScenario)
            .toList();
    }

    private WhatIfScenario mapRowToScenario(Map<String, Object> row) {
        WhatIfScenario scenario = new WhatIfScenario();
        scenario.setId((String) row.get("id"));
        scenario.setName((String) row.get("name"));
        scenario.setDescription((String) row.get("description"));
        scenario.setType((String) row.get("type"));
        scenario.setStatus((String) row.get("status"));
        scenario.setDifficultyLevel((String) row.get("difficulty_level"));
        scenario.setTimeHorizonDays((Integer) row.get("time_horizon_days"));
        scenario.setCreatedBy((String) row.get("created_by"));
        scenario.setCreatedAt(row.get("created_at").toString());
        if (row.get("updated_at") != null) {
            scenario.setUpdatedAt(row.get("updated_at").toString());
        }
        
        // Parse JSON fields (simplified - in real implementation would use proper JSON parsing)
        scenario.setParameters(parseJsonToMap((String) row.get("parameters")));
        scenario.setBaselineData(parseJsonToMap((String) row.get("baseline_data")));
        scenario.setProjectedData(parseJsonToMap((String) row.get("projected_data")));
        
        return scenario;
    }

    private ScenarioComparison.ScenarioSummary convertToSummary(WhatIfScenario scenario) {
        ScenarioComparison.ScenarioSummary summary = new ScenarioComparison.ScenarioSummary(
            scenario.getId(), scenario.getName(), scenario.getType());
        
        if (scenario.getImpact() != null) {
            summary.setCostChange(scenario.getImpact().getTotalCostDifference());
            summary.setPercentageChange(scenario.getImpact().getTotalPercentageChange());
        }
        
        if (scenario.getRiskAssessment() != null) {
            summary.setRiskLevel(scenario.getRiskAssessment().getLevel());
            summary.setRiskScore(1.0 - scenario.getRiskAssessment().getConfidenceScore());
        }
        
        // Estimate implementation complexity and time
        summary.setImplementationComplexity(estimateComplexity(scenario));
        summary.setTimeToImplementDays(estimateImplementationTime(scenario));
        
        if (scenario.getProjectedData() != null) {
            summary.setTotalCost((BigDecimal) scenario.getProjectedData().get("totalCost"));
        }
        
        return summary;
    }

    private int estimateComplexity(WhatIfScenario scenario) {
        int complexity = 1;
        
        switch (scenario.getType()) {
            case "cost_optimization", "rightsizing" -> complexity = 2;
            case "reserved_instances" -> complexity = 3;
            case "spot_instances" -> complexity = 5;
            case "scaling" -> complexity = 4;
            case "infrastructure_change" -> complexity = 7;
        }
        
        switch (scenario.getDifficultyLevel()) {
            case "intermediate" -> complexity += 1;
            case "advanced" -> complexity += 2;
        }
        
        return Math.min(complexity, 10);
    }

    private int estimateImplementationTime(WhatIfScenario scenario) {
        return switch (scenario.getType()) {
            case "cost_optimization" -> 7;
            case "rightsizing" -> 14;
            case "reserved_instances" -> 3;
            case "spot_instances" -> 21;
            case "scaling" -> 10;
            case "infrastructure_change" -> 45;
            default -> 14;
        };
    }

    private ScenarioComparison.ScenarioSummary findBestScenario(List<ScenarioComparison.ScenarioSummary> summaries, ScenarioComparisonRequest request) {
        if (summaries.isEmpty()) return null;
        
        return switch (request.getAnalysisMethod()) {
            case "cost_optimization" -> summaries.stream()
                .min((a, b) -> {
                    BigDecimal aCost = a.getTotalCost() != null ? a.getTotalCost() : BigDecimal.ZERO;
                    BigDecimal bCost = b.getTotalCost() != null ? b.getTotalCost() : BigDecimal.ZERO;
                    return aCost.compareTo(bCost);
                })
                .orElse(null);
            case "risk_adjusted" -> summaries.stream()
                .min(Comparator.comparingDouble(ScenarioComparison.ScenarioSummary::getRiskScore))
                .orElse(null);
            default -> summaries.stream()
                .filter(s -> s.getTotalCost() != null)
                .min(Comparator.comparing(ScenarioComparison.ScenarioSummary::getTotalCost))
                .orElse(null);
        };
    }

    private ScenarioComparison.ScenarioSummary findWorstScenario(List<ScenarioComparison.ScenarioSummary> summaries, ScenarioComparisonRequest request) {
        if (summaries.isEmpty()) return null;
        
        return switch (request.getAnalysisMethod()) {
            case "cost_optimization" -> summaries.stream()
                .max((a, b) -> {
                    BigDecimal aCost = a.getTotalCost() != null ? a.getTotalCost() : BigDecimal.ZERO;
                    BigDecimal bCost = b.getTotalCost() != null ? b.getTotalCost() : BigDecimal.ZERO;
                    return aCost.compareTo(bCost);
                })
                .orElse(null);
            case "risk_adjusted" -> summaries.stream()
                .max(Comparator.comparingDouble(ScenarioComparison.ScenarioSummary::getRiskScore))
                .orElse(null);
            default -> summaries.stream()
                .filter(s -> s.getTotalCost() != null)
                .max(Comparator.comparing(ScenarioComparison.ScenarioSummary::getTotalCost))
                .orElse(null);
        };
    }

    private Map<String, Object> generateComparisonMetrics(List<ScenarioComparison.ScenarioSummary> summaries, ScenarioComparisonRequest request) {
        Map<String, Object> metrics = new HashMap<>();
        
        if (summaries.isEmpty()) {
            return metrics;
        }
        
        // Cost metrics
        BigDecimal totalPotentialSavings = summaries.stream()
            .filter(s -> s.getCostChange() != null && s.getCostChange().compareTo(BigDecimal.ZERO) < 0)
            .map(s -> s.getCostChange().abs())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        metrics.put("totalPotentialSavings", totalPotentialSavings);
        metrics.put("scenarioCount", summaries.size());
        metrics.put("quickWinCount", summaries.stream().mapToInt(s -> s.isQuickWin() ? 1 : 0).sum());
        
        // Risk distribution
        Map<String, Long> riskDistribution = summaries.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                ScenarioComparison.ScenarioSummary::getRiskLevel,
                java.util.stream.Collectors.counting()
            ));
        metrics.put("riskDistribution", riskDistribution);
        
        // Complexity metrics
        double avgComplexity = summaries.stream()
            .mapToInt(ScenarioComparison.ScenarioSummary::getImplementationComplexity)
            .average()
            .orElse(0.0);
        metrics.put("averageComplexity", avgComplexity);
        
        return metrics;
    }

    private List<String> generateComparisonRecommendations(List<ScenarioComparison.ScenarioSummary> summaries, ScenarioComparisonRequest request) {
        List<String> recommendations = new ArrayList<>();
        
        // Quick wins
        long quickWinCount = summaries.stream().filter(ScenarioComparison.ScenarioSummary::isQuickWin).count();
        if (quickWinCount > 0) {
            recommendations.add("Prioritize " + quickWinCount + " quick win scenario(s) for immediate implementation");
        }
        
        // High savings scenarios
        summaries.stream()
            .filter(s -> s.getCostChange() != null && s.getCostChange().compareTo(new BigDecimal("-1000")) < 0)
            .forEach(s -> recommendations.add("Consider high-savings scenario: " + s.getName() + 
                " (saves $" + s.getCostChange().abs() + ")"));
        
        // Risk warnings
        summaries.stream()
            .filter(s -> "high".equals(s.getRiskLevel()) || "critical".equals(s.getRiskLevel()))
            .forEach(s -> recommendations.add("Exercise caution with high-risk scenario: " + s.getName()));
        
        // Implementation sequencing
        if (summaries.size() > 2) {
            recommendations.add("Implement scenarios in phases, starting with lowest risk and complexity");
        }
        
        return recommendations;
    }


    // Validation helper methods
    private void validateScenarioParameters(CreateScenarioRequest request, List<String> errors, List<String> warnings) {
        Map<String, Object> params = request.getParameters();
        if (params == null) {
            warnings.add("No scenario parameters provided - using defaults");
            return;
        }
        
        switch (request.getType()) {
            case "rightsizing" -> {
                if (!params.containsKey("rightsizing_reduction")) {
                    warnings.add("No rightsizing reduction percentage specified - using default 25%");
                }
            }
            case "spot_instances" -> {
                if (!params.containsKey("spot_percentage")) {
                    warnings.add("No spot instance percentage specified - using default 50%");
                }
            }
            case "scaling" -> {
                if (!params.containsKey("scaling_factor")) {
                    warnings.add("No scaling factor specified - using default 1.5x");
                }
            }
        }
    }

    private Map<String, Object> assessScenarioFeasibility(CreateScenarioRequest request) {
        Map<String, Object> feasibility = new HashMap<>();
        
        // Technical feasibility
        boolean technicallyFeasible = true;
        List<String> technicalConstraints = new ArrayList<>();
        
        switch (request.getType()) {
            case "spot_instances" -> {
                technicalConstraints.add("Requires fault-tolerant application architecture");
                technicalConstraints.add("Not suitable for stateful or time-critical workloads");
            }
            case "rightsizing" -> {
                technicalConstraints.add("Requires performance testing and monitoring");
                technicalConstraints.add("May impact application performance if under-sized");
            }
        }
        
        feasibility.put("technicallyFeasible", technicallyFeasible);
        feasibility.put("technicalConstraints", technicalConstraints);
        
        // Business feasibility
        boolean businessFeasible = true;
        List<String> businessConstraints = new ArrayList<>();
        
        if (request.getTimeHorizonDays() < 7) {
            businessConstraints.add("Very short analysis period may not show reliable results");
        }
        
        feasibility.put("businessFeasible", businessFeasible);
        feasibility.put("businessConstraints", businessConstraints);
        
        return feasibility;
    }

    private int estimateComplexity(CreateScenarioRequest request) {
        int complexity = 1;
        
        // Base complexity by scenario type
        complexity += switch (request.getType()) {
            case "cost_optimization" -> 1;
            case "rightsizing" -> 2;
            case "reserved_instances" -> 2;
            case "spot_instances" -> 4;
            case "scaling" -> 3;
            case "infrastructure_change" -> 6;
            default -> 2;
        };
        
        // Difficulty level adjustment
        if ("intermediate".equals(request.getDifficultyLevel())) {
            complexity += 1;
        } else if ("advanced".equals(request.getDifficultyLevel())) {
            complexity += 2;
        }
        
        // Time horizon adjustment
        if (request.getTimeHorizonDays() > 90) {
            complexity += 1;
        }
        
        return Math.min(complexity, 10);
    }

    private String estimateImplementationDuration(int complexityScore) {
        return switch (complexityScore) {
            case 1, 2 -> "1-3 days";
            case 3, 4 -> "1-2 weeks";
            case 5, 6 -> "2-4 weeks";
            case 7, 8 -> "1-2 months";
            default -> "2+ months";
        };
    }

    private List<String> getRequiredSkills(String scenarioType) {
        return switch (scenarioType) {
            case "cost_optimization" -> List.of("AWS Console", "Cost analysis", "CloudWatch");
            case "rightsizing" -> List.of("Performance monitoring", "Instance types", "Application testing");
            case "reserved_instances" -> List.of("Capacity planning", "Financial analysis", "Usage forecasting");
            case "spot_instances" -> List.of("Auto Scaling", "Fault tolerance", "Advanced AWS networking");
            case "scaling" -> List.of("Load testing", "Auto Scaling", "Performance optimization");
            case "infrastructure_change" -> List.of("Architecture design", "Migration planning", "Risk management");
            default -> List.of("AWS basics", "Cost management");
        };
    }

    // Utility methods
    private String convertMapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        // Simplified JSON conversion - in production use Jackson or similar
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Map<String, Object> parseJsonToMap(String json) {
        if (json == null || json.trim().isEmpty() || "{}".equals(json)) {
            return new HashMap<>();
        }
        // Simplified JSON parsing - in production use Jackson or similar
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}