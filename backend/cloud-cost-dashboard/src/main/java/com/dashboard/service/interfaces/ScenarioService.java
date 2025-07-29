package com.dashboard.service.interfaces;

import com.dashboard.model.scenario.WhatIfScenario;
import com.dashboard.model.scenario.ScenarioComparison;
import com.dashboard.dto.scenario.CreateScenarioRequest;
import com.dashboard.dto.scenario.ScenarioComparisonRequest;

import java.util.List;

/**
 * Service interface for what-if scenario modeling and analysis
 * Supports educational scenario generation and impact assessment
 */
public interface ScenarioService {
    
    /**
     * Create and execute a what-if scenario
     * @param request Scenario configuration including type, changes, and time horizon
     * @return Complete scenario with impact analysis and projections
     */
    WhatIfScenario createScenario(CreateScenarioRequest request);
    
    /**
     * Compare multiple scenarios to identify best options
     * @param request Comparison parameters and scenario IDs
     * @return Comparative analysis of scenarios with recommendations
     */
    ScenarioComparison compareScenarios(ScenarioComparisonRequest request);
    
    /**
     * Get available scenario templates for different difficulty levels
     * @param difficultyLevel Optional filter for difficulty
     * @return List of scenario templates with descriptions and parameters
     */
    List<Object> getScenarioTemplates(String difficultyLevel);
    
    /**
     * Validate scenario parameters and estimate complexity
     * @param request Scenario parameters to validate
     * @return Validation results with feasibility assessment
     */
    Object validateScenario(CreateScenarioRequest request);
}