// api.js
const API_BASE_URL = "http://localhost:8080/api";

// Feature flag to disable fallback data - when true, API failures will throw errors instead of returning mock data
const DISABLE_FALLBACK_DATA = process.env.REACT_APP_DISABLE_FALLBACK === 'true' || false;

// Real API call to fetch cost report from backend
export const fetchCostReport = async (teamName, startDate, endDate) => {
  console.log(
    `Making API call to fetch cost report for team: ${teamName}, dates: ${startDate} to ${endDate}`,
  );

  try {
    const response = await fetch(`${API_BASE_URL}/reports`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        teamName,
        startDate,
        endDate,
      }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log("Received data from backend:", data);

    // Transform backend response to match frontend expectations
    return data.breakdowns || [];
  } catch (error) {
    console.error("Error fetching cost report:", error);
    
    // Check feature flag - if fallback is disabled, throw the error
    if (DISABLE_FALLBACK_DATA) {
      throw new Error(`API call failed and fallback data is disabled: ${error.message}`);
    }
    
    console.warn("Using fallback data because DISABLE_FALLBACK_DATA is false");
    // Return realistic mock data as fallback for development
    const fallbackData = [];
    const baseDate = new Date('2025-01-01');
    
    // Base costs for services with realistic patterns
    const serviceBases = {
      "EC2": { base: 945.5, growth: 0.02, volatility: 0.15, team: "platform" },
      "S3": { base: 712.3, growth: 0.01, volatility: 0.08, team: "frontend" },
      "RDS": { base: 388.75, growth: 0.015, volatility: 0.12, team: "backend" },
      "Lambda": { base: 234.12, growth: 0.03, volatility: 0.20, team: "data" },
      "CloudWatch": { base: 156.89, growth: 0.005, volatility: 0.10, team: "ml" },
      "ELB": { base: 198.45, growth: 0.008, volatility: 0.14, team: "platform" },
      "VPC": { base: 89.32, growth: 0.002, volatility: 0.05, team: "backend" },
      "SNS": { base: 67.89, growth: 0.012, volatility: 0.18, team: "data" }
    };
    
    // Generate 30 days of realistic data with trends and patterns
    for (let i = 0; i < 30; i++) {
      const date = new Date(baseDate);
      date.setDate(date.getDate() + i);
      
      // Weekend effect - lower usage on weekends
      const isWeekend = date.getDay() === 0 || date.getDay() === 6;
      const weekendFactor = isWeekend ? 0.7 : 1.0;
      
      // Monthly growth trend
      const trendFactor = 1 + (i / 30) * 0.1; // 10% growth over month
      
      Object.entries(serviceBases).forEach(([service, config]) => {
        // Calculate cost with growth trend, weekend effect, and realistic volatility
        const growthFactor = Math.pow(1 + config.growth, i);
        const volatility = (Math.random() - 0.5) * 2 * config.volatility;
        const seasonalEffect = Math.sin((i / 30) * Math.PI * 2) * 0.1; // Seasonal variation
        
        const cost = config.base * growthFactor * trendFactor * weekendFactor * 
                    (1 + volatility + seasonalEffect);
        
        fallbackData.push({
          service,
          team: config.team,
          totalCost: Math.max(cost * 0.1, cost), // Ensure positive costs
          date: date.toISOString().split('T')[0],
          region: i % 4 === 0 ? 'us-east-1' : i % 4 === 1 ? 'us-west-2' : i % 4 === 2 ? 'eu-west-1' : 'ap-southeast-1',
          provider: 'aws',
          note: "Realistic fallback data - backend unavailable",
        });
      });
    }
    
    return fallbackData;
  }
};

// Get list of available teams
export const fetchTeams = async () => {
  console.log("Fetching teams from backend");

  try {
    const response = await fetch(`${API_BASE_URL}/teams`);

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const teams = await response.json();
    console.log("Received teams from backend:", teams);
    return teams;
  } catch (error) {
    console.error("Error fetching teams:", error);
    
    // Check feature flag - if fallback is disabled, throw the error
    if (DISABLE_FALLBACK_DATA) {
      throw new Error(`API call failed and fallback data is disabled: ${error.message}`);
    }
    
    console.warn("Using fallback teams data because DISABLE_FALLBACK_DATA is false");
    // Return default teams as fallback
    return [
      { name: "platform", displayName: "Platform Engineering" },
      { name: "frontend", displayName: "Frontend Development" },
      { name: "backend", displayName: "Backend Development" },
      { name: "data", displayName: "Data Engineering" },
      { name: "ml", displayName: "Machine Learning" },
    ];
  }
};

// ========================================
// ANALYTICS APIs
// ========================================

// Generate cost predictions using various models
export const generatePredictions = async (method, daysToPredict, teamName, startDate, endDate, options = {}) => {
  console.log(`Generating predictions using ${method} for ${daysToPredict} days`);
  
  try {
    const response = await fetch(`${API_BASE_URL}/analytics/predictions`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        method,
        daysToPredict,
        teamName,
        startDate,
        endDate,
        includeSeasonality: options.includeSeasonality || true,
        confidenceLevel: options.confidenceLevel || 0.95,
        options
      }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log("Received prediction data:", data);
    return data;
  } catch (error) {
    console.error("Error generating predictions:", error);
    
    if (DISABLE_FALLBACK_DATA) {
      throw new Error(`Predictions API failed and fallback data is disabled: ${error.message}`);
    }
    
    console.warn("Using fallback prediction data because DISABLE_FALLBACK_DATA is false");
    // Fallback prediction data
    return generateFallbackPredictions(method, daysToPredict, startDate, endDate);
  }
};

// Analyze cost trends and patterns
export const analyzeTrends = async (teamName, startDate, endDate, options = {}) => {
  console.log(`Analyzing trends for team: ${teamName}`);
  
  try {
    const response = await fetch(`${API_BASE_URL}/analytics/trends`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        teamName,
        startDate,
        endDate,
        windowSize: options.windowSize || 7,
        includeAnomalyDetection: options.includeAnomalyDetection !== false
      }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log("Received trend analysis:", data);
    return data;
  } catch (error) {
    console.error("Error analyzing trends:", error);
    
    if (DISABLE_FALLBACK_DATA) {
      throw new Error(`Trends API failed and fallback data is disabled: ${error.message}`);
    }
    
    console.warn("Using fallback trend data because DISABLE_FALLBACK_DATA is false");
    return generateFallbackTrendAnalysis(teamName, startDate, endDate);
  }
};

// Compare teams, services, or regions
export const compareEntities = async (comparisonType, startDate, endDate, options = {}) => {
  console.log(`Comparing ${comparisonType} from ${startDate} to ${endDate}`);
  
  try {
    const response = await fetch(`${API_BASE_URL}/analytics/comparison`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        comparisonType,
        startDate,
        endDate,
        metric: options.metric || 'total_cost',
        includeEntities: options.includeEntities,
        excludeEntities: options.excludeEntities,
        includeEfficiencyMetrics: options.includeEfficiencyMetrics !== false
      }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log("Received comparison data:", data);
    return data;
  } catch (error) {
    console.error("Error comparing entities:", error);
    
    if (DISABLE_FALLBACK_DATA) {
      throw new Error(`Comparison API failed and fallback data is disabled: ${error.message}`);
    }
    
    console.warn("Using fallback comparison data because DISABLE_FALLBACK_DATA is false");
    return generateFallbackComparison(comparisonType, startDate, endDate);
  }
};

// Detect cost anomalies
export const detectAnomalies = async (teamName, startDate, endDate, threshold = 2.0) => {
  console.log(`Detecting anomalies for team: ${teamName} with threshold: ${threshold}`);
  
  try {
    const response = await fetch(`${API_BASE_URL}/analytics/anomalies?teamName=${teamName}&startDate=${startDate}&endDate=${endDate}&threshold=${threshold}`);

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log("Received anomaly data:", data);
    return data;
  } catch (error) {
    console.error("Error detecting anomalies:", error);
    
    if (DISABLE_FALLBACK_DATA) {
      throw new Error(`Anomaly detection API failed and fallback data is disabled: ${error.message}`);
    }
    
    console.warn("Using fallback anomaly data because DISABLE_FALLBACK_DATA is false");
    return generateFallbackAnomalies(teamName, startDate, endDate);
  }
};

// ========================================
// OPTIMIZATION APIs  
// ========================================

// Generate optimization recommendations
export const generateOptimizationRecommendations = async (scope, startDate, endDate, options = {}) => {
  console.log(`Generating optimization recommendations for scope: ${scope}`);
  
  try {
    const response = await fetch(`${API_BASE_URL}/optimization/recommendations`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        scope,
        startDate,
        endDate,
        includeTypes: options.includeTypes,
        minImpact: options.minImpact,
        maxRisk: options.maxRisk
      }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log("Received optimization recommendations:", data);
    return data;
  } catch (error) {
    console.error("Error generating recommendations:", error);
    
    if (DISABLE_FALLBACK_DATA) {
      throw new Error(`Optimization API failed and fallback data is disabled: ${error.message}`);
    }
    
    console.warn("Using fallback optimization data because DISABLE_FALLBACK_DATA is false");
    return generateFallbackOptimizationRecommendations(scope);
  }
};

// Get optimization summary
export const getOptimizationSummary = async (teamName, startDate, endDate) => {
  console.log(`Getting optimization summary for team: ${teamName}`);
  
  try {
    const params = new URLSearchParams({ startDate, endDate });
    if (teamName && teamName !== 'all') {
      params.append('teamName', teamName);
    }

    const response = await fetch(`${API_BASE_URL}/optimization/summary?${params}`);

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log("Received optimization summary:", data);
    return data;
  } catch (error) {
    console.error("Error getting optimization summary:", error);
    
    if (DISABLE_FALLBACK_DATA) {
      throw new Error(`Optimization summary API failed and fallback data is disabled: ${error.message}`);
    }
    
    console.warn("Using fallback optimization summary because DISABLE_FALLBACK_DATA is false");
    return generateFallbackOptimizationSummary(teamName);
  }
};

// ========================================
// SCENARIO APIs
// ========================================

// Create what-if scenario
export const createScenario = async (scenarioConfig) => {
  console.log(`Creating scenario: ${scenarioConfig.name}`);
  
  try {
    const response = await fetch(`${API_BASE_URL}/scenarios`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(scenarioConfig),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log("Created scenario:", data);
    return data;
  } catch (error) {
    console.error("Error creating scenario:", error);
    
    if (DISABLE_FALLBACK_DATA) {
      throw new Error(`Scenario creation API failed and fallback data is disabled: ${error.message}`);
    }
    
    console.warn("Using fallback scenario creation because DISABLE_FALLBACK_DATA is false");
    return generateFallbackScenario(scenarioConfig);
  }
};

// Compare multiple scenarios
export const compareScenarios = async (scenarioIds) => {
  console.log(`Comparing ${scenarioIds.length} scenarios`);
  
  try {
    const response = await fetch(`${API_BASE_URL}/scenarios/compare`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ scenarioIds }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log("Scenario comparison results:", data);
    return data;
  } catch (error) {
    console.error("Error comparing scenarios:", error);
    
    if (DISABLE_FALLBACK_DATA) {
      throw new Error(`Scenario comparison API failed and fallback data is disabled: ${error.message}`);
    }
    
    console.warn("Using fallback scenario comparison because DISABLE_FALLBACK_DATA is false");
    return generateFallbackScenarioComparison(scenarioIds);
  }
};

// Get scenario templates
export const getScenarioTemplates = async (difficulty = null) => {
  console.log(`Getting scenario templates for difficulty: ${difficulty}`);
  
  try {
    const params = difficulty ? `?difficulty=${difficulty}` : '';
    const response = await fetch(`${API_BASE_URL}/scenarios/templates${params}`);

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log("Received scenario templates:", data);
    return data;
  } catch (error) {
    console.error("Error getting scenario templates:", error);
    
    if (DISABLE_FALLBACK_DATA) {
      throw new Error(`Scenario templates API failed and fallback data is disabled: ${error.message}`);
    }
    
    console.warn("Using fallback scenario templates because DISABLE_FALLBACK_DATA is false");
    return generateFallbackScenarioTemplates(difficulty);
  }
};

// ========================================
// BUDGET APIs
// ========================================

// Get all budgets
export const getBudgets = async () => {
  console.log("Fetching all budgets");
  
  try {
    const response = await fetch(`${API_BASE_URL}/budgets`);

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log("Received budgets:", data);
    return data;
  } catch (error) {
    console.error("Error fetching budgets:", error);
    
    if (DISABLE_FALLBACK_DATA) {
      throw new Error(`Budgets API failed and fallback data is disabled: ${error.message}`);
    }
    
    console.warn("Using fallback budget data because DISABLE_FALLBACK_DATA is false");
    return generateFallbackBudgets();
  }
};

// Create budget
export const createBudget = async (budgetData) => {
  console.log(`Creating budget: ${budgetData.name}`);
  
  try {
    const response = await fetch(`${API_BASE_URL}/budgets`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(budgetData),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log("Created budget:", data);
    return data;
  } catch (error) {
    console.error("Error creating budget:", error);
    
    if (DISABLE_FALLBACK_DATA) {
      throw new Error(`Budget creation API failed and fallback data is disabled: ${error.message}`);
    }
    
    console.warn("Using fallback budget creation because DISABLE_FALLBACK_DATA is false");
    return { ...budgetData, id: Date.now().toString(), createdAt: new Date().toISOString() };
  }
};

// Update budget
export const updateBudget = async (budgetId, budgetData) => {
  console.log(`Updating budget: ${budgetId}`);
  
  try {
    const response = await fetch(`${API_BASE_URL}/budgets/${budgetId}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(budgetData),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log("Updated budget:", data);
    return data;
  } catch (error) {
    console.error("Error updating budget:", error);
    
    if (DISABLE_FALLBACK_DATA) {
      throw new Error(`Budget update API failed and fallback data is disabled: ${error.message}`);
    }
    
    console.warn("Using fallback budget update because DISABLE_FALLBACK_DATA is false");
    return { ...budgetData, id: budgetId, updatedAt: new Date().toISOString() };
  }
};

// Delete budget
export const deleteBudget = async (budgetId) => {
  console.log(`Deleting budget: ${budgetId}`);
  
  try {
    const response = await fetch(`${API_BASE_URL}/budgets/${budgetId}`, {
      method: "DELETE",
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    console.log("Deleted budget:", budgetId);
    return { success: true };
  } catch (error) {
    console.error("Error deleting budget:", error);
    
    if (DISABLE_FALLBACK_DATA) {
      throw new Error(`Budget deletion API failed and fallback data is disabled: ${error.message}`);
    }
    
    console.warn("Using fallback budget deletion because DISABLE_FALLBACK_DATA is false");
    return { success: true };
  }
};

// ========================================
// FALLBACK DATA GENERATORS
// ========================================

const generateFallbackPredictions = (method, daysToPredict, startDate, endDate) => {
  const predictions = [];
  const baseDate = new Date(endDate);
  
  for (let i = 1; i <= daysToPredict; i++) {
    const predictionDate = new Date(baseDate);
    predictionDate.setDate(predictionDate.getDate() + i);
    
    const baseCost = 1000 + (i * 10) + (Math.random() * 200 - 100);
    
    predictions.push({
      date: predictionDate.toISOString().split('T')[0],
      predictedCost: Math.max(0, baseCost),
      lowerBound: Math.max(0, baseCost * 0.85),
      upperBound: baseCost * 1.15,
      confidence: 0.8
    });
  }
  
  return {
    method,
    predictions,
    confidence: 0.8,
    metadata: { note: "Fallback prediction data" }
  };
};

const generateFallbackTrendAnalysis = (teamName, startDate, endDate) => {
  return {
    analysisPeriod: `${startDate} to ${endDate}`,
    dataPoints: 30,
    overallTrend: "increasing",
    growthRate: 5.2,
    volatility: 15.8,
    anomalies: [
      {
        date: "2025-01-15",
        type: "spike",
        severity: 2.3,
        description: "Cost spike detected in EC2 usage"
      }
    ],
    summary: {
      overall: "Costs are increasing at a moderate 5.2% rate",
      recommendation: "Monitor EC2 usage patterns and consider rightsizing opportunities",
      details: [
        "Growth rate: 5.2%",
        "Volatility: 15.8%",
        "Anomalies detected: 1"
      ]
    }
  };
};

const generateFallbackComparison = (comparisonType, startDate, endDate) => {
  const mockTeams = [
    { team: "platform", totalCost: 15000, serviceCount: 5, efficiency: 3000, rank: 1 },
    { team: "backend", totalCost: 12000, serviceCount: 4, efficiency: 3000, rank: 2 },
    { team: "frontend", totalCost: 8000, serviceCount: 3, efficiency: 2667, rank: 3 },
    { team: "data", totalCost: 6000, serviceCount: 2, efficiency: 3000, rank: 4 },
    { team: "ml", totalCost: 4000, serviceCount: 2, efficiency: 2000, rank: 5 }
  ];

  return {
    comparison: {
      teams: mockTeams,
      benchmarks: {
        efficiency: { avg: 2733, min: 2000, max: 3000 }
      },
      totalTeams: mockTeams.length,
      analysisDate: new Date().toISOString(),
      metric: comparisonType
    }
  };
};

const generateFallbackAnomalies = (teamName, startDate, endDate) => {
  return [
    {
      date: "2025-01-15",
      service: "EC2",
      actualCost: 5000,
      expectedCost: 1200,
      deviationScore: 3.2,
      severity: "high",
      type: "spike"
    },
    {
      date: "2025-01-22",
      service: "S3",
      actualCost: 50,
      expectedCost: 300,
      deviationScore: 2.1,
      severity: "medium",
      type: "drop"
    }
  ];
};

const generateFallbackOptimizationRecommendations = (scope) => {
  return [
    {
      id: "rec_1",
      title: "Rightsize Over-provisioned EC2 Instances",
      description: "Several EC2 instances are running at <20% CPU utilization",
      type: "rightsizing",
      impact: "high",
      priority: "high",
      potentialSavings: 850.00,
      implementationEffort: "low",
      riskLevel: "low",
      affectedServices: ["EC2"],
      affectedTeams: ["platform"]
    },
    {
      id: "rec_2", 
      title: "Purchase Reserved Instances for Predictable Workloads",
      description: "Consistent EC2 usage patterns suitable for RIs",
      type: "reserved_instance",
      impact: "medium",
      priority: "medium",
      potentialSavings: 600.00,
      implementationEffort: "medium",
      riskLevel: "low",
      affectedServices: ["EC2"],
      affectedTeams: ["platform", "backend"]
    }
  ];
};

const generateFallbackOptimizationSummary = (teamName) => {
  return {
    totalPotentialSavings: 1450.00,
    recommendationCount: 2,
    highImpactCount: 1,
    implementationCount: 0,
    savingsPercentage: 12.5
  };
};

const generateFallbackScenario = (scenarioConfig) => {
  return {
    id: `scenario_${Date.now()}`,
    ...scenarioConfig,
    impact: {
      totalCostDifference: -500.00,
      totalPercentageChange: -8.5,
      averageDailySavings: 16.67,
      riskAssessment: {
        level: "low",
        factors: ["Well-tested optimization approach"]
      }
    },
    data: [], // Would contain projected cost data
    createdAt: new Date().toISOString()
  };
};

const generateFallbackScenarioComparison = (scenarioIds) => {
  return {
    comparison: {
      bestScenario: { name: "Scenario 1", totalCost: 4500 },
      worstScenario: { name: "Scenario 2", totalCost: 5200 },
      scenarios: scenarioIds.map((id, index) => ({
        name: `Scenario ${index + 1}`,
        totalCost: 4500 + (index * 350),
        costChange: -8.5 + (index * 2),
        riskLevel: "low"
      }))
    }
  };
};

const generateFallbackScenarioTemplates = (difficulty) => {
  return [
    {
      id: "template_1",
      name: "Beginner: Right-size Over-provisioned Instances",
      difficulty: "beginner",
      type: "instance_rightsizing",
      expectedSavings: 25.0,
      description: "Reduce instance sizes by 25% for over-provisioned resources"
    },
    {
      id: "template_2", 
      name: "Intermediate: Reserved Instance Strategy",
      difficulty: "intermediate",
      type: "reserved_instances",
      expectedSavings: 30.0,
      description: "Purchase 1-year RIs for 80% of steady-state workloads"
    }
  ];
};

const generateFallbackBudgets = () => {
  return [
    {
      id: "1",
      name: "Platform Team Monthly Budget",
      amount: 15000.00,
      period: "monthly",
      scope: "team",
      target: "platform",
      alertThreshold: 85.0,
      startDate: "2025-01-01",
      endDate: "2025-01-31",
      currentSpend: 12750.00,
      utilizationPercentage: 85.0,
      daysRemaining: 8
    },
    {
      id: "2",
      name: "EC2 Quarterly Budget", 
      amount: 45000.00,
      period: "quarterly",
      scope: "service",
      target: "EC2",
      alertThreshold: 80.0,
      startDate: "2025-01-01",
      endDate: "2025-03-31",
      currentSpend: 28000.00,
      utilizationPercentage: 62.2,
      daysRemaining: 68
    }
  ];
};

// Get usage records for a specific team and date range
export const fetchUsageRecords = async (teamName, startDate, endDate) => {
  console.log(`Fetching usage records for team: ${teamName}`);

  try {
    const response = await fetch(
      `${API_BASE_URL}/usage?team=${encodeURIComponent(
        teamName,
      )}&startDate=${startDate}&endDate=${endDate}`,
    );

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const records = await response.json();
    console.log("Received usage records from backend:", records);
    return records;
  } catch (error) {
    console.error("Error fetching usage records:", error);
    return [];
  }
};
