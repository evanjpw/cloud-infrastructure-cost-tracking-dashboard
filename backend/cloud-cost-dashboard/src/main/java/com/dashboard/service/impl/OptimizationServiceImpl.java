package com.dashboard.service.impl;

import com.dashboard.service.interfaces.OptimizationService;
import com.dashboard.model.optimization.OptimizationRecommendation;
import com.dashboard.model.optimization.OptimizationSummary;
import com.dashboard.dto.optimization.OptimizationRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class OptimizationServiceImpl implements OptimizationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<OptimizationRecommendation> generateRecommendations(OptimizationRequest request) {
        System.out.println("Generating optimization recommendations for scope: " + request.getScope());
        
        // Fetch cost data for analysis
        List<Map<String, Object>> costData = fetchCostDataForOptimization(
            request.getScope(), 
            request.getStartDate(), 
            request.getEndDate()
        );
        
        if (costData.isEmpty()) {
            System.out.println("No cost data found for optimization analysis");  
            return Collections.emptyList();
        }
        
        List<OptimizationRecommendation> recommendations = new ArrayList<>();
        
        // Generate different types of recommendations
        recommendations.addAll(generateRightsizingRecommendations(costData));
        recommendations.addAll(generateReservedInstanceRecommendations(costData));
        recommendations.addAll(generateUnusedResourceRecommendations(costData));
        recommendations.addAll(generateStorageOptimizationRecommendations(costData));
        recommendations.addAll(generateAnomalyBasedRecommendations(costData));
        
        // Filter by request criteria
        recommendations = filterRecommendations(recommendations, request);
        
        // Sort by potential savings (descending)
        recommendations.sort((a, b) -> Double.compare(b.getPotentialSavings(), a.getPotentialSavings()));
        
        // Save recommendations to database for tracking
        saveRecommendations(recommendations);
        
        System.out.println("Generated " + recommendations.size() + " optimization recommendations");
        return recommendations;
    }

    @Override
    public OptimizationSummary getOptimizationSummary(String teamName, String startDate, String endDate) {
        System.out.println("Getting optimization summary for team: " + teamName);
        
        // Get recent recommendations
        List<Map<String, Object>> recentRecs = getRecentRecommendations(teamName, startDate, endDate);
        
        OptimizationSummary summary = new OptimizationSummary();
        summary.setTeamName(teamName);
        summary.setAnalysisPeriod(startDate + " to " + endDate);
        
        // Calculate summary metrics
        double totalSavings = recentRecs.stream()
            .mapToDouble(rec -> ((BigDecimal) rec.get("potential_savings")).doubleValue())
            .sum();
        
        long highImpactCount = recentRecs.stream()
            .filter(rec -> "high".equals(rec.get("impact")))
            .count();
        
        long implementedCount = recentRecs.stream()
            .filter(rec -> "implemented".equals(rec.get("status")))
            .count();
        
        // Calculate current costs for savings percentage
        double currentCosts = getCurrentCosts(teamName, startDate, endDate);
        double savingsPercentage = currentCosts > 0 ? (totalSavings / currentCosts) * 100 : 0;
        
        summary.setTotalPotentialSavings(totalSavings);
        summary.setRecommendationCount(recentRecs.size());
        summary.setHighImpactCount((int) highImpactCount);
        summary.setImplementedCount((int) implementedCount);
        summary.setSavingsPercentage(savingsPercentage);
        
        // Add breakdown by type
        Map<String, Object> typeBreakdown = calculateTypeBreakdown(recentRecs);
        summary.setTypeBreakdown(typeBreakdown);
        
        return summary;
    }

    @Override
    public Object analyzeRecommendation(String recommendationId) {
        System.out.println("Analyzing recommendation: " + recommendationId);
        
        String sql = """
            SELECT * FROM optimization_recommendations 
            WHERE id = ?
            """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, recommendationId);
        
        if (results.isEmpty()) {
            throw new RuntimeException("Recommendation not found: " + recommendationId);
        }
        
        Map<String, Object> recommendation = results.get(0);
        
        // Add detailed analysis
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("recommendation", recommendation);
        analysis.put("detailedImpact", analyzeImpactDetails(recommendation));
        analysis.put("implementationPlan", generateImplementationPlan(recommendation));
        analysis.put("riskAssessment", assessImplementationRisk(recommendation));
        analysis.put("timelineEstimate", estimateImplementationTimeline(recommendation));
        
        return analysis;
    }

    @Override
    public Object updateRecommendationStatus(String recommendationId, String action, String notes) {
        System.out.println("Updating recommendation " + recommendationId + " with action: " + action);
        
        String sql = """
            UPDATE optimization_recommendations 
            SET status = ?, status_notes = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        int rowsUpdated = jdbcTemplate.update(sql, action, notes, recommendationId);
        
        if (rowsUpdated == 0) {
            throw new RuntimeException("Recommendation not found: " + recommendationId);
        }
        
        // Return updated recommendation
        return getRecommendationById(recommendationId);
    }

    @Override
    public Object getOptimizationAnalytics() {
        System.out.println("Fetching optimization analytics");
        
        Map<String, Object> analytics = new HashMap<>();
        
        // Overall statistics
        String statsSql = """
            SELECT 
                COUNT(*) as total_recommendations,
                SUM(potential_savings) as total_potential_savings,
                COUNT(CASE WHEN status = 'implemented' THEN 1 END) as implemented_count,
                COUNT(CASE WHEN impact = 'high' THEN 1 END) as high_impact_count,
                AVG(potential_savings) as avg_savings_per_recommendation
            FROM optimization_recommendations
            WHERE created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 90 DAY)
            """;
        
        List<Map<String, Object>> stats = jdbcTemplate.queryForList(statsSql);
        analytics.put("overallStats", stats.get(0));
        
        // Breakdown by type
        String typeSql = """
            SELECT 
                type,
                COUNT(*) as count,
                SUM(potential_savings) as total_savings,
                AVG(potential_savings) as avg_savings
            FROM optimization_recommendations
            WHERE created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 90 DAY)
            GROUP BY type
            ORDER BY total_savings DESC
            """;
        
        List<Map<String, Object>> typeBreakdown = jdbcTemplate.queryForList(typeSql);
        analytics.put("typeBreakdown", typeBreakdown);
        
        // Implementation trends
        String trendSql = """
            SELECT 
                DATE(created_at) as date,
                COUNT(*) as recommendations_created,
                COUNT(CASE WHEN status = 'implemented' THEN 1 END) as implementations
            FROM optimization_recommendations
            WHERE created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY)
            GROUP BY DATE(created_at)
            ORDER BY date
            """;
        
        List<Map<String, Object>> trends = jdbcTemplate.queryForList(trendSql);
        analytics.put("implementationTrends", trends);
        
        return analytics;
    }

    // ========================================
    // PRIVATE HELPER METHODS
    // ========================================

    private List<Map<String, Object>> fetchCostDataForOptimization(String scope, String startDate, String endDate) {
        String sql = """
            SELECT 
                date, team_name, service_name, region, provider, 
                resource_id, usage_type, cost, usage_quantity, usage_unit,
                tags, metadata
            FROM enhanced_usage_records 
            WHERE date BETWEEN ? AND ?
            """;
        
        List<Object> params = new ArrayList<>();
        params.add(startDate);
        params.add(endDate);
        
        if (scope != null && !scope.equals("all")) {
            if (scope.startsWith("team:")) {
                sql += " AND team_name = ?";
                params.add(scope.substring(5));
            } else if (scope.startsWith("service:")) {
                sql += " AND service_name = ?";
                params.add(scope.substring(8));
            }
        }
        
        sql += " ORDER BY date, cost DESC";
        
        return jdbcTemplate.queryForList(sql, params.toArray());
    }

    private List<OptimizationRecommendation> generateRightsizingRecommendations(List<Map<String, Object>> costData) {
        List<OptimizationRecommendation> recommendations = new ArrayList<>();
        
        // Group by service and analyze usage patterns
        Map<String, List<Map<String, Object>>> serviceGroups = costData.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                row -> (String) row.get("service_name")
            ));
        
        for (Map.Entry<String, List<Map<String, Object>>> entry : serviceGroups.entrySet()) {
            String service = entry.getKey();
            List<Map<String, Object>> serviceData = entry.getValue();
            
            // Analyze for rightsizing opportunities (simplified logic)
            if ("EC2".equals(service)) {
                double avgCost = serviceData.stream()
                    .mapToDouble(row -> ((BigDecimal) row.get("cost")).doubleValue())
                    .average().orElse(0);
                
                // If average cost is high, suggest rightsizing
                if (avgCost > 500) {
                    OptimizationRecommendation rec = new OptimizationRecommendation();
                    rec.setTitle("Rightsize Over-provisioned " + service + " Instances");
                    rec.setDescription("Analysis shows potential for downsizing " + service + " instances based on usage patterns");
                    rec.setType("rightsizing");
                    rec.setImpact(avgCost > 1000 ? "high" : "medium");
                    rec.setPriority(avgCost > 1000 ? "high" : "medium");
                    rec.setPotentialSavings(avgCost * 0.25); // 25% savings estimate
                    rec.setImplementationEffort("low");
                    rec.setRiskLevel("low");
                    rec.setAffectedServices(Arrays.asList(service));
                    rec.setAffectedTeams(getTeamsForService(serviceData));
                    rec.setStatus("pending");
                    
                    recommendations.add(rec);
                }
            }
        }
        
        return recommendations;
    }

    private List<OptimizationRecommendation> generateReservedInstanceRecommendations(List<Map<String, Object>> costData) {
        List<OptimizationRecommendation> recommendations = new ArrayList<>();
        
        // Look for consistent EC2 usage patterns
        List<Map<String, Object>> ec2Data = costData.stream()
            .filter(row -> "EC2".equals(row.get("service_name")))
            .toList();
        
        if (ec2Data.size() > 20) { // Need sufficient data for RI analysis
            double avgDailyCost = ec2Data.stream()
                .mapToDouble(row -> ((BigDecimal) row.get("cost")).doubleValue())
                .average().orElse(0);
            
            // Calculate consistency score (simplified)
            double[] costs = ec2Data.stream()
                .mapToDouble(row -> ((BigDecimal) row.get("cost")).doubleValue())
                .toArray();
            double variance = calculateVariance(costs);
            double consistencyScore = 1.0 - (variance / (avgDailyCost * avgDailyCost));
            
            if (consistencyScore > 0.7 && avgDailyCost > 200) { // High consistency and cost
                OptimizationRecommendation rec = new OptimizationRecommendation();
                rec.setTitle("Purchase Reserved Instances for Predictable EC2 Workloads");
                rec.setDescription("Consistent EC2 usage patterns detected - suitable for Reserved Instance savings");
                rec.setType("reserved_instance");
                rec.setImpact("high");
                rec.setPriority("medium");
                rec.setPotentialSavings(avgDailyCost * 30 * 0.3); // 30% savings over 30 days
                rec.setImplementationEffort("medium");
                rec.setRiskLevel("low");
                rec.setAffectedServices(Arrays.asList("EC2"));
                rec.setAffectedTeams(getTeamsForService(ec2Data));
                rec.setStatus("pending");
                
                recommendations.add(rec);
            }
        }
        
        return recommendations;
    }

    private List<OptimizationRecommendation> generateUnusedResourceRecommendations(List<Map<String, Object>> costData) {
        List<OptimizationRecommendation> recommendations = new ArrayList<>();
        
        // Look for resources with zero usage but non-zero cost
        List<Map<String, Object>> suspiciousResources = costData.stream()
            .filter(row -> {
                BigDecimal cost = (BigDecimal) row.get("cost");
                BigDecimal usage = (BigDecimal) row.get("usage_quantity");
                return cost.doubleValue() > 10 && (usage == null || usage.doubleValue() == 0);
            })
            .toList();
        
        if (!suspiciousResources.isEmpty()) {
            double totalWaste = suspiciousResources.stream()
                .mapToDouble(row -> ((BigDecimal) row.get("cost")).doubleValue())
                .sum();
            
            OptimizationRecommendation rec = new OptimizationRecommendation();
            rec.setTitle("Clean Up Unused Resources");
            rec.setDescription("Detected " + suspiciousResources.size() + " resources with costs but no usage");
            rec.setType("unused_resource");
            rec.setImpact(totalWaste > 1000 ? "high" : "medium");
            rec.setPriority("high"); // Easy wins should be high priority
            rec.setPotentialSavings(totalWaste);
            rec.setImplementationEffort("low");
            rec.setRiskLevel("low");
            rec.setAffectedServices(getUniqueServices(suspiciousResources));
            rec.setAffectedTeams(getTeamsForService(suspiciousResources));
            rec.setStatus("pending");
            
            recommendations.add(rec);
        }
        
        return recommendations;
    }

    private List<OptimizationRecommendation> generateStorageOptimizationRecommendations(List<Map<String, Object>> costData) {
        List<OptimizationRecommendation> recommendations = new ArrayList<>();
        
        // Look for S3 storage opportunities
        List<Map<String, Object>> s3Data = costData.stream()
            .filter(row -> "S3".equals(row.get("service_name")))
            .toList();
        
        if (!s3Data.isEmpty()) {
            double totalS3Cost = s3Data.stream()
                .mapToDouble(row -> ((BigDecimal) row.get("cost")).doubleValue())
                .sum();
            
            if (totalS3Cost > 500) { // Significant S3 usage
                OptimizationRecommendation rec = new OptimizationRecommendation();
                rec.setTitle("Optimize S3 Storage Classes and Lifecycle Policies");
                rec.setDescription("Implement intelligent tiering and lifecycle policies for S3 storage optimization");
                rec.setType("storage_optimization");
                rec.setImpact("medium");
                rec.setPriority("medium");
                rec.setPotentialSavings(totalS3Cost * 0.2); // 20% savings estimate
                rec.setImplementationEffort("medium");
                rec.setRiskLevel("low");
                rec.setAffectedServices(Arrays.asList("S3"));
                rec.setAffectedTeams(getTeamsForService(s3Data));
                rec.setStatus("pending");
                
                recommendations.add(rec);
            }
        }
        
        return recommendations;
    }

    private List<OptimizationRecommendation> generateAnomalyBasedRecommendations(List<Map<String, Object>> costData) {
        List<OptimizationRecommendation> recommendations = new ArrayList<>();
        
        // Group by service and detect cost spikes
        Map<String, List<Map<String, Object>>> serviceGroups = costData.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                row -> (String) row.get("service_name")
            ));
        
        for (Map.Entry<String, List<Map<String, Object>>> entry : serviceGroups.entrySet()) {
            String service = entry.getKey(); 
            List<Map<String, Object>> serviceData = entry.getValue();
            
            if (serviceData.size() < 7) continue; // Need minimum data
            
            double[] costs = serviceData.stream()
                .mapToDouble(row -> ((BigDecimal) row.get("cost")).doubleValue())
                .toArray();
            
            double mean = Arrays.stream(costs).average().orElse(0);
            double stdDev = Math.sqrt(calculateVariance(costs));
            
            // Look for outliers (> 2 standard deviations)
            long anomalyCount = Arrays.stream(costs)
                .filter(cost -> Math.abs(cost - mean) > 2 * stdDev)
                .count();
            
            if (anomalyCount > 0 && mean > 100) {
                OptimizationRecommendation rec = new OptimizationRecommendation();
                rec.setTitle("Investigate " + service + " Cost Anomalies");
                rec.setDescription("Detected " + anomalyCount + " cost anomalies in " + service + " - investigate root causes");
                rec.setType("anomaly");
                rec.setImpact("medium");
                rec.setPriority("high"); // Anomalies should be investigated quickly
                rec.setPotentialSavings(stdDev * anomalyCount); // Conservative estimate
                rec.setImplementationEffort("high"); // Investigation takes time
                rec.setRiskLevel("medium");
                rec.setAffectedServices(Arrays.asList(service));
                rec.setAffectedTeams(getTeamsForService(serviceData));
                rec.setStatus("pending");
                
                recommendations.add(rec);
            }
        }
        
        return recommendations;
    }

    private List<OptimizationRecommendation> filterRecommendations(List<OptimizationRecommendation> recommendations, OptimizationRequest request) {
        return recommendations.stream()
            .filter(rec -> {
                // Filter by include types
                if (request.getIncludeTypes() != null && !request.getIncludeTypes().isEmpty()) {
                    if (!request.getIncludeTypes().contains(rec.getType())) {
                        return false;
                    }
                }
                
                // Filter by minimum impact
                if (request.getMinImpact() != null) {
                    String minImpact = request.getMinImpact();
                    String recImpact = rec.getImpact();
                    if ("high".equals(minImpact) && !"high".equals(recImpact)) {
                        return false;
                    }
                    if ("medium".equals(minImpact) && "low".equals(recImpact)) {
                        return false;
                    }
                }
                
                // Filter by maximum risk
                if (request.getMaxRisk() != null) {
                    String maxRisk = request.getMaxRisk();
                    String recRisk = rec.getRiskLevel();
                    if ("low".equals(maxRisk) && !"low".equals(recRisk)) {
                        return false;
                    }
                    if ("medium".equals(maxRisk) && "high".equals(recRisk)) {
                        return false;
                    }
                }
                
                return true;
            })
            .toList();
    }

    private void saveRecommendations(List<OptimizationRecommendation> recommendations) {
        String sql = """
            INSERT INTO optimization_recommendations 
            (title, description, type, impact, priority, potential_savings, 
             implementation_effort, risk_level, affected_services, affected_teams, 
             implementation_steps, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        for (OptimizationRecommendation rec : recommendations) {
            try {
                jdbcTemplate.update(sql,
                    rec.getTitle(),
                    rec.getDescription(), 
                    rec.getType(),
                    rec.getImpact(),
                    rec.getPriority(),
                    rec.getPotentialSavings(),
                    rec.getImplementationEffort(),
                    rec.getRiskLevel(),
                    convertListToJson(rec.getAffectedServices()),
                    convertListToJson(rec.getAffectedTeams()),
                    convertListToJson(rec.getImplementationSteps()),
                    rec.getStatus()
                );
            } catch (Exception e) {
                System.err.println("Error saving recommendation: " + e.getMessage());
            }
        }
    }

    private List<Map<String, Object>> getRecentRecommendations(String teamName, String startDate, String endDate) {
        String sql = """
            SELECT * FROM optimization_recommendations 
            WHERE created_at >= ? 
            """;
        
        List<Object> params = new ArrayList<>();
        params.add(startDate);
        
        if (teamName != null && !teamName.equals("all")) {
            sql += " AND JSON_CONTAINS(affected_teams, JSON_QUOTE(?))";
            params.add(teamName);
        }
        
        sql += " ORDER BY created_at DESC";
        
        return jdbcTemplate.queryForList(sql, params.toArray());
    }

    private double getCurrentCosts(String teamName, String startDate, String endDate) {
        String sql = """
            SELECT SUM(cost) as total_cost 
            FROM enhanced_usage_records 
            WHERE date BETWEEN ? AND ?
            """;
        
        List<Object> params = new ArrayList<>();
        params.add(startDate);
        params.add(endDate);
        
        if (teamName != null && !teamName.equals("all")) {
            sql += " AND team_name = ?";
            params.add(teamName);
        }
        
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, params.toArray());
        BigDecimal totalCost = (BigDecimal) result.get(0).get("total_cost");
        return totalCost != null ? totalCost.doubleValue() : 0.0;
    }

    private Map<String, Object> calculateTypeBreakdown(List<Map<String, Object>> recommendations) {
        Map<String, Object> breakdown = new HashMap<>();
        
        Map<String, Long> typeCounts = recommendations.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                rec -> (String) rec.get("type"),
                java.util.stream.Collectors.counting()
            ));
        
        Map<String, Double> typeSavings = recommendations.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                rec -> (String) rec.get("type"),
                java.util.stream.Collectors.summingDouble(
                    rec -> ((BigDecimal) rec.get("potential_savings")).doubleValue()
                )
            ));
        
        breakdown.put("counts", typeCounts);
        breakdown.put("savings", typeSavings);
        
        return breakdown;
    }

    // Helper methods
    private double calculateVariance(double[] values) {
        if (values.length == 0) return 0;
        
        double mean = Arrays.stream(values).average().orElse(0);
        double sumSquaredDiffs = Arrays.stream(values)
            .map(x -> Math.pow(x - mean, 2))
            .sum();
        
        return sumSquaredDiffs / values.length;
    }

    private List<String> getTeamsForService(List<Map<String, Object>> serviceData) {
        return serviceData.stream()
            .map(row -> (String) row.get("team_name"))
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    private List<String> getUniqueServices(List<Map<String, Object>> data) {
        return data.stream()
            .map(row -> (String) row.get("service_name"))
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    private String convertListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        return "[\"" + String.join("\", \"", list) + "\"]";
    }

    private Map<String, Object> analyzeImpactDetails(Map<String, Object> recommendation) {
        Map<String, Object> impact = new HashMap<>();
        // Add detailed impact analysis
        impact.put("costSavings", recommendation.get("potential_savings"));
        impact.put("timeframe", "30-90 days");
        impact.put("confidence", "high");
        return impact;
    }

    private List<String> generateImplementationPlan(Map<String, Object> recommendation) {
        String type = (String) recommendation.get("type");
        return switch (type) {
            case "rightsizing" -> Arrays.asList(
                "Analyze instance utilization metrics", 
                "Test application performance on smaller instances",
                "Schedule maintenance window",
                "Implement changes and monitor"
            );
            case "reserved_instance" -> Arrays.asList(
                "Analyze usage patterns over 3+ months",
                "Calculate optimal RI mix",
                "Purchase Reserved Instances",
                "Monitor utilization and adjust"
            );
            default -> Arrays.asList(
                "Conduct detailed analysis",
                "Create implementation plan", 
                "Execute changes",
                "Monitor results"
            );
        };
    }

    private Map<String, Object> assessImplementationRisk(Map<String, Object> recommendation) {
        Map<String, Object> risk = new HashMap<>();
        String riskLevel = (String) recommendation.get("risk_level");
        
        risk.put("level", riskLevel);
        risk.put("factors", switch (riskLevel) {
            case "low" -> Arrays.asList("Well-tested approach", "Easy rollback");
            case "medium" -> Arrays.asList("Moderate complexity", "Some service impact");
            case "high" -> Arrays.asList("Complex implementation", "Potential service disruption");
            default -> Arrays.asList("Unknown risk factors");
        });
        
        return risk;
    }

    private String estimateImplementationTimeline(Map<String, Object> recommendation) {
        String effort = (String) recommendation.get("implementation_effort");
        return switch (effort) {
            case "low" -> "1-2 weeks";
            case "medium" -> "2-4 weeks";  
            case "high" -> "1-3 months";
            default -> "Unknown timeline";
        };
    }

    private Map<String, Object> getRecommendationById(String recommendationId) {
        String sql = "SELECT * FROM optimization_recommendations WHERE id = ?";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, recommendationId);
        
        if (results.isEmpty()) {
            throw new RuntimeException("Recommendation not found: " + recommendationId);
        }
        
        return results.get(0);
    }
}