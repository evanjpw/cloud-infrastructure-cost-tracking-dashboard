package com.dashboard.service.interfaces;

import com.dashboard.model.optimization.OptimizationRecommendation;
import com.dashboard.model.optimization.OptimizationSummary;
import com.dashboard.dto.optimization.OptimizationRequest;

import java.util.List;

/**
 * Service interface for cost optimization recommendations and analysis
 * Generates actionable insights for cost reduction and resource efficiency
 */
public interface OptimizationService {
    
    /**
     * Generate optimization recommendations based on usage patterns
     * @param request Optimization parameters including scope, filters, and analysis depth
     * @return List of prioritized optimization recommendations
     */
    List<OptimizationRecommendation> generateRecommendations(OptimizationRequest request);
    
    /**
     * Get optimization summary with potential savings overview
     * @param teamName Optional team filter
     * @param startDate Analysis start date
     * @param endDate Analysis end date
     * @return High-level optimization summary with total potential savings
     */
    OptimizationSummary getOptimizationSummary(String teamName, String startDate, String endDate);
    
    /**
     * Analyze specific optimization opportunity in detail
     * @param recommendationId Recommendation identifier
     * @return Detailed analysis with implementation steps and impact assessment
     */
    Object analyzeRecommendation(String recommendationId);
    
    /**
     * Mark recommendation as accepted/rejected and track implementation
     * @param recommendationId Recommendation identifier
     * @param action Action taken (accept, reject, defer)
     * @param notes Optional implementation notes
     * @return Updated recommendation status
     */
    Object updateRecommendationStatus(String recommendationId, String action, String notes);
    
    /**
     * Get optimization metrics and tracking across all recommendations
     * @return Analytics on optimization adoption and realized savings
     */
    Object getOptimizationAnalytics();
}