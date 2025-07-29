// What-If Scenario Modeling for Educational Cost Analysis
// Allows students to model the cost impact of architectural changes

// Base scenario types for educational purposes
export const SCENARIO_TYPES = {
  INSTANCE_RIGHTSIZING: 'instance_rightsizing',
  RESERVED_INSTANCES: 'reserved_instances',
  AUTO_SCALING: 'auto_scaling',
  REGION_MIGRATION: 'region_migration',
  SERVICE_MIGRATION: 'service_migration',
  CAPACITY_PLANNING: 'capacity_planning',
  DISASTER_RECOVERY: 'disaster_recovery',
  MULTI_CLOUD: 'multi_cloud'
};

// Predefined change factors for realistic modeling
const CHANGE_FACTORS = {
  instance_sizes: {
    'downsize_50': 0.5,
    'downsize_25': 0.75,
    'upsize_25': 1.25,
    'upsize_50': 1.5,
    'upsize_100': 2.0
  },
  reserved_instances: {
    '1_year_partial': 0.7,
    '1_year_all': 0.65,
    '3_year_partial': 0.6,
    '3_year_all': 0.5
  },
  regions: {
    'us_east_to_west': 1.1,
    'us_to_eu': 1.15,
    'us_to_asia': 1.2,
    'premium_to_standard': 0.85
  },
  auto_scaling: {
    'enable_basic': 0.8,
    'enable_advanced': 0.7,
    'peak_optimization': 0.6
  }
};

// Create a what-if scenario
export const createWhatIfScenario = (baseData, scenarioConfig) => {
  const {
    type,
    name,
    description,
    changes,
    timeHorizon = 30, // days
    startDate = null
  } = scenarioConfig;

  if (!baseData || baseData.length === 0) {
    return {
      error: 'No base data provided',
      scenario: null
    };
  }

  // Sort base data by date
  const sortedBase = [...baseData].sort((a, b) => 
    new Date(a.date || a.timestamp) - new Date(b.date || b.timestamp)
  );

  // Apply scenario changes
  const scenarioData = applyScenarioChanges(sortedBase, type, changes, timeHorizon, startDate);

  // Calculate impact metrics
  const impact = calculateScenarioImpact(sortedBase, scenarioData);

  return {
    scenario: {
      id: generateScenarioId(),
      type,
      name,
      description,
      changes,
      timeHorizon,
      startDate: startDate || new Date().toISOString().split('T')[0],
      data: scenarioData,
      impact,
      createdAt: new Date().toISOString()
    },
    error: null
  };
};

// Apply specific changes based on scenario type
const applyScenarioChanges = (baseData, type, changes, timeHorizon, startDate) => {
  const scenarioData = [];
  const start = startDate ? new Date(startDate) : new Date();

  for (let day = 0; day < timeHorizon; day++) {
    const currentDate = new Date(start);
    currentDate.setDate(currentDate.getDate() + day);
    
    // Get base data for this day (use most recent if no exact match)
    const baseCost = getBaseCostForDate(baseData, currentDate);
    
    // Apply scenario-specific changes
    const modifiedCost = applyChangesByType(baseCost, type, changes, day);
    
    scenarioData.push({
      date: currentDate.toISOString().split('T')[0],
      originalCost: baseCost.totalCost,
      modifiedCost: modifiedCost,
      totalCost: modifiedCost,
      day: day + 1,
      scenario: type,
      changes: changes
    });
  }

  return scenarioData;
};

// Get base cost for a specific date
const getBaseCostForDate = (baseData, targetDate) => {
  // Find the closest data point
  let closest = baseData[0];
  let minDiff = Math.abs(new Date(closest.date || closest.timestamp) - targetDate);

  baseData.forEach(point => {
    const diff = Math.abs(new Date(point.date || point.timestamp) - targetDate);
    if (diff < minDiff) {
      minDiff = diff;
      closest = point;
    }
  });

  return closest;
};

// Apply changes based on scenario type
const applyChangesByType = (baseCost, type, changes, dayIndex) => {
  let modifiedCost = baseCost.totalCost;

  switch (type) {
    case SCENARIO_TYPES.INSTANCE_RIGHTSIZING:
      modifiedCost = applyRightsizingChanges(modifiedCost, changes, dayIndex);
      break;
    
    case SCENARIO_TYPES.RESERVED_INSTANCES:
      modifiedCost = applyReservedInstanceChanges(modifiedCost, changes, dayIndex);
      break;
    
    case SCENARIO_TYPES.AUTO_SCALING:
      modifiedCost = applyAutoScalingChanges(modifiedCost, changes, dayIndex);
      break;
    
    case SCENARIO_TYPES.REGION_MIGRATION:
      modifiedCost = applyRegionMigrationChanges(modifiedCost, changes, dayIndex);
      break;
    
    case SCENARIO_TYPES.SERVICE_MIGRATION:
      modifiedCost = applyServiceMigrationChanges(modifiedCost, changes, dayIndex);
      break;
    
    case SCENARIO_TYPES.CAPACITY_PLANNING:
      modifiedCost = applyCapacityPlanningChanges(modifiedCost, changes, dayIndex);
      break;
    
    case SCENARIO_TYPES.DISASTER_RECOVERY:
      modifiedCost = applyDisasterRecoveryChanges(modifiedCost, changes, dayIndex);
      break;
    
    case SCENARIO_TYPES.MULTI_CLOUD:
      modifiedCost = applyMultiCloudChanges(modifiedCost, changes, dayIndex);
      break;
    
    default:
      // Custom changes
      modifiedCost = applyCustomChanges(modifiedCost, changes, dayIndex);
  }

  return Math.max(0, modifiedCost);
};

// Specific scenario change implementations
const applyRightsizingChanges = (baseCost, changes, dayIndex) => {
  const { size_change = 'downsize_25', implementation_delay = 7 } = changes;
  
  if (dayIndex < implementation_delay) {
    return baseCost; // No change during implementation period
  }
  
  const factor = CHANGE_FACTORS.instance_sizes[size_change] || 1.0;
  return baseCost * factor;
};

const applyReservedInstanceChanges = (baseCost, changes, dayIndex) => {
  const { commitment = '1_year_partial', coverage = 0.8 } = changes;
  
  const factor = CHANGE_FACTORS.reserved_instances[commitment] || 1.0;
  const riSavings = baseCost * coverage * (1 - factor);
  
  return baseCost - riSavings;
};

const applyAutoScalingChanges = (baseCost, changes, dayIndex) => {
  const { 
    type = 'enable_basic', 
    peak_reduction = 0.3,
    off_peak_reduction = 0.5,
    implementation_delay = 14
  } = changes;
  
  if (dayIndex < implementation_delay) {
    return baseCost;
  }
  
  // Simulate peak/off-peak patterns (simplified)
  const isPeakDay = (dayIndex % 7) < 5; // Weekdays are peak
  const reduction = isPeakDay ? peak_reduction : off_peak_reduction;
  
  return baseCost * (1 - reduction);
};

const applyRegionMigrationChanges = (baseCost, changes, dayIndex) => {
  const { 
    target_region = 'us_to_eu',
    migration_cost = 10000,
    migration_duration = 30
  } = changes;
  
  const pricingFactor = CHANGE_FACTORS.regions[target_region] || 1.0;
  let cost = baseCost * pricingFactor;
  
  // Add migration costs during migration period
  if (dayIndex < migration_duration) {
    cost += migration_cost / migration_duration;
  }
  
  return cost;
};

const applyServiceMigrationChanges = (baseCost, changes, dayIndex) => {
  const {
    source_service = 'EC2',
    target_service = 'Lambda',
    cost_multiplier = 0.6,
    migration_effort = 20000,
    migration_duration = 45
  } = changes;
  
  let cost = baseCost * cost_multiplier;
  
  // Add migration effort costs
  if (dayIndex < migration_duration) {
    cost += migration_effort / migration_duration;
  }
  
  return cost;
};

const applyCapacityPlanningChanges = (baseCost, changes, dayIndex) => {
  const {
    growth_rate = 0.1, // 10% growth
    capacity_buffer = 0.2, // 20% buffer
    scaling_efficiency = 0.9
  } = changes;
  
  // Calculate expected growth
  const growthFactor = 1 + (growth_rate * dayIndex / 365);
  const capacityFactor = 1 + capacity_buffer;
  
  return baseCost * growthFactor * capacityFactor * scaling_efficiency;
};

const applyDisasterRecoveryChanges = (baseCost, changes, dayIndex) => {
  const {
    dr_strategy = 'warm_standby',
    cost_multipliers = { warm_standby: 1.4, cold_standby: 1.1, hot_standby: 1.8 }
  } = changes;
  
  const multiplier = cost_multipliers[dr_strategy] || 1.2;
  return baseCost * multiplier;
};

const applyMultiCloudChanges = (baseCost, changes, dayIndex) => {
  const {
    cloud_distribution = { aws: 0.6, azure: 0.3, gcp: 0.1 },
    complexity_overhead = 0.15,
    cost_optimization = 0.1
  } = changes;
  
  // Apply distribution and complexity
  const distributedCost = baseCost * (1 + complexity_overhead) * (1 - cost_optimization);
  return distributedCost;
};

const applyCustomChanges = (baseCost, changes, dayIndex) => {
  const {
    cost_multiplier = 1.0,
    fixed_cost_change = 0,
    percentage_change = 0,
    implementation_delay = 0
  } = changes;
  
  if (dayIndex < implementation_delay) {
    return baseCost;
  }
  
  let cost = baseCost * cost_multiplier;
  cost += fixed_cost_change;
  cost *= (1 + percentage_change);
  
  return cost;
};

// Calculate impact metrics
const calculateScenarioImpact = (baseData, scenarioData) => {
  const baseTotalCost = baseData.reduce((sum, d) => sum + d.totalCost, 0);
  const scenarioTotalCost = scenarioData.reduce((sum, d) => sum + d.totalCost, 0);
  
  const costDifference = scenarioTotalCost - baseTotalCost;
  const percentageChange = (costDifference / baseTotalCost) * 100;
  
  const dailyImpacts = scenarioData.map((day, index) => ({
    date: day.date,
    baseCost: day.originalCost,
    scenarioCost: day.totalCost,
    difference: day.totalCost - day.originalCost,
    percentageChange: ((day.totalCost - day.originalCost) / day.originalCost) * 100
  }));
  
  return {
    totalCostDifference: costDifference,
    totalPercentageChange: percentageChange,
    averageDailySavings: costDifference / scenarioData.length,
    maxDailyImpact: Math.max(...dailyImpacts.map(d => Math.abs(d.difference))),
    breakEvenDay: findBreakEvenDay(dailyImpacts),
    riskAssessment: assessScenarioRisk(scenarioData, percentageChange),
    dailyImpacts
  };
};

// Find break-even day for scenarios with upfront costs
const findBreakEvenDay = (dailyImpacts) => {
  let cumulativeSavings = 0;
  
  for (let i = 0; i < dailyImpacts.length; i++) {
    cumulativeSavings += -dailyImpacts[i].difference; // Negative difference means savings
    if (cumulativeSavings > 0) {
      return i + 1;
    }
  }
  
  return null; // No break-even within the time horizon
};

// Assess scenario risk
const assessScenarioRisk = (scenarioData, percentageChange) => {
  const volatility = calculateVolatility(scenarioData);
  
  let riskLevel = 'low';
  let riskFactors = [];
  
  if (Math.abs(percentageChange) > 50) {
    riskLevel = 'high';
    riskFactors.push('Large cost change impact');
  } else if (Math.abs(percentageChange) > 20) {
    riskLevel = 'medium';
    riskFactors.push('Moderate cost change impact');
  }
  
  if (volatility > 0.3) {
    riskLevel = 'high';
    riskFactors.push('High cost volatility');
  } else if (volatility > 0.15) {
    if (riskLevel === 'low') riskLevel = 'medium';
    riskFactors.push('Moderate cost volatility');
  }
  
  return { level: riskLevel, factors: riskFactors, volatility };
};

const calculateVolatility = (data) => {
  if (data.length < 2) return 0;
  
  const returns = [];
  for (let i = 1; i < data.length; i++) {
    const change = (data[i].totalCost - data[i-1].totalCost) / data[i-1].totalCost;
    returns.push(change);
  }
  
  const avgReturn = returns.reduce((sum, r) => sum + r, 0) / returns.length;
  const variance = returns.reduce((sum, r) => sum + Math.pow(r - avgReturn, 2), 0) / returns.length;
  
  return Math.sqrt(variance);
};

// Compare multiple scenarios
export const compareScenarios = (scenarios) => {
  if (!scenarios || scenarios.length === 0) {
    return { comparison: null, error: 'No scenarios to compare' };
  }

  const comparison = {
    totalScenarios: scenarios.length,
    bestScenario: null,
    worstScenario: null,
    scenarios: scenarios.map(s => ({
      name: s.name,
      type: s.type,
      totalCost: s.data.reduce((sum, d) => sum + d.totalCost, 0),
      averageDailyCost: s.data.reduce((sum, d) => sum + d.totalCost, 0) / s.data.length,
      costChange: s.impact.totalPercentageChange,
      riskLevel: s.impact.riskAssessment.level,
      breakEvenDay: s.impact.breakEvenDay
    }))
  };

  // Find best and worst scenarios
  comparison.bestScenario = comparison.scenarios.reduce((best, current) => 
    current.totalCost < best.totalCost ? current : best
  );
  
  comparison.worstScenario = comparison.scenarios.reduce((worst, current) => 
    current.totalCost > worst.totalCost ? current : worst
  );

  return { comparison, error: null };
};

// Generate educational scenario templates
export const generateScenarioTemplate = (templateType) => {
  const templates = {
    beginner_rightsizing: {
      type: SCENARIO_TYPES.INSTANCE_RIGHTSIZING,
      name: "Beginner: Right-size Over-provisioned Instances",
      description: "Reduce instance sizes by 25% for over-provisioned resources",
      changes: {
        size_change: 'downsize_25',
        implementation_delay: 7
      },
      learningObjectives: [
        "Understand the impact of instance sizing on costs",
        "Learn about implementation delays in cost changes",
        "Practice calculating ROI for rightsizing initiatives"
      ],
      expectedOutcome: "20-30% cost reduction with minimal risk"
    },
    
    intermediate_reserved_instances: {
      type: SCENARIO_TYPES.RESERVED_INSTANCES,
      name: "Intermediate: Reserved Instance Strategy",
      description: "Purchase 1-year RIs for 80% of steady-state workloads",
      changes: {
        commitment: '1_year_partial',
        coverage: 0.8
      },
      learningObjectives: [
        "Analyze the trade-offs of Reserved Instances",
        "Calculate break-even points for RI investments",
        "Understand capacity planning implications"
      ],
      expectedOutcome: "30-35% savings on committed capacity"
    },
    
    advanced_multi_cloud: {
      type: SCENARIO_TYPES.MULTI_CLOUD,
      name: "Advanced: Multi-Cloud Architecture",
      description: "Distribute workloads across AWS (60%), Azure (30%), GCP (10%)",
      changes: {
        cloud_distribution: { aws: 0.6, azure: 0.3, gcp: 0.1 },
        complexity_overhead: 0.15,
        cost_optimization: 0.12
      },
      learningObjectives: [
        "Evaluate multi-cloud cost implications",
        "Consider operational complexity costs",
        "Balance cost optimization with resilience"
      ],
      expectedOutcome: "Variable impact depending on workload suitability"
    }
  };

  return templates[templateType] || null;
};

// Generate a unique scenario ID
const generateScenarioId = () => {
  return 'scenario_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
};

// Format scenario data for chart visualization
export const formatScenarioForChart = (scenario, includeBaseline = true) => {
  const labels = scenario.data.map(d => {
    const date = new Date(d.date);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  });

  const datasets = [];

  if (includeBaseline) {
    datasets.push({
      label: 'Original Cost',
      data: scenario.data.map(d => d.originalCost),
      borderColor: 'rgb(156, 163, 175)',
      backgroundColor: 'rgba(156, 163, 175, 0.1)',
      borderDash: [5, 5],
      tension: 0.4
    });
  }

  datasets.push({
    label: `${scenario.name} Cost`,
    data: scenario.data.map(d => d.totalCost),
    borderColor: scenario.impact.totalPercentageChange < 0 ? 'rgb(34, 197, 94)' : 'rgb(239, 68, 68)',
    backgroundColor: scenario.impact.totalPercentageChange < 0 ? 'rgba(34, 197, 94, 0.1)' : 'rgba(239, 68, 68, 0.1)',
    tension: 0.4,
    borderWidth: 2
  });

  return { labels, datasets };
};

export default {
  SCENARIO_TYPES,
  createWhatIfScenario,
  compareScenarios,
  generateScenarioTemplate,
  formatScenarioForChart
};