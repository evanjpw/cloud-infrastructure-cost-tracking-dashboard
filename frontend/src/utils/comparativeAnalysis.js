// Comparative Analysis for Educational Cost Exercises
// Provides team/service/region benchmarking for student learning

// Compare teams performance and costs
export const compareTeams = (costData, options = {}) => {
  const {
    metric = 'total_cost', // total_cost, efficiency, trend
    timeRange = 30, // days
    includeEfficiency = true
  } = options;

  if (!costData || costData.length === 0) {
    return {
      comparison: null,
      error: 'No cost data provided'
    };
  }

  // Group data by team
  const teamGroups = {};
  costData.forEach(item => {
    const team = item.team || 'Unknown';
    if (!teamGroups[team]) {
      teamGroups[team] = [];
    }
    teamGroups[team].push(item);
  });

  // Calculate team metrics
  const teamComparisons = Object.entries(teamGroups).map(([teamName, teamData]) => {
    const totalCost = teamData.reduce((sum, item) => sum + (item.totalCost || 0), 0);
    const avgDailyCost = totalCost / Math.max(teamData.length, 1);
    const serviceCount = new Set(teamData.map(item => item.service || item.serviceName)).size;
    
    // Calculate efficiency (cost per service)
    const efficiency = serviceCount > 0 ? totalCost / serviceCount : 0;
    
    // Calculate trend (simplified)
    const sortedData = teamData.sort((a, b) => 
      new Date(a.date || a.timestamp) - new Date(b.date || b.timestamp)
    );
    const trend = calculateTrend(sortedData);
    
    // Calculate cost distribution
    const serviceDistribution = {};
    teamData.forEach(item => {
      const service = item.service || item.serviceName || 'Unknown';
      serviceDistribution[service] = (serviceDistribution[service] || 0) + item.totalCost;
    });
    
    const topServices = Object.entries(serviceDistribution)
      .sort(([,a], [,b]) => b - a)
      .slice(0, 3)
      .map(([service, cost]) => ({
        service,
        cost,
        percentage: (cost / totalCost) * 100
      }));

    return {
      team: teamName,
      totalCost,
      avgDailyCost,
      serviceCount,
      efficiency,
      trend,
      topServices,
      dataPoints: teamData.length,
      costPerDataPoint: totalCost / teamData.length
    };
  });

  // Sort by the specified metric
  teamComparisons.sort((a, b) => {
    switch (metric) {
      case 'efficiency':
        return a.efficiency - b.efficiency; // Lower is better
      case 'trend':
        return b.trend - a.trend; // Higher trend first
      case 'total_cost':
      default:
        return b.totalCost - a.totalCost; // Highest cost first
    }
  });

  // Calculate rankings and percentiles
  const totalTeams = teamComparisons.length;
  teamComparisons.forEach((team, index) => {
    team.rank = index + 1;
    team.percentile = ((totalTeams - index) / totalTeams) * 100;
  });

  // Calculate benchmarks
  const benchmarks = calculateBenchmarks(teamComparisons);

  return {
    comparison: {
      teams: teamComparisons,
      benchmarks,
      totalTeams,
      analysisDate: new Date().toISOString(),
      metric
    },
    error: null
  };
};

// Compare services across teams or time periods
export const compareServices = (costData, options = {}) => {
  const {
    groupBy = 'team', // team, region, provider
    metric = 'cost_efficiency',
    includeUtilization = true
  } = options;

  if (!costData || costData.length === 0) {
    return {
      comparison: null,
      error: 'No cost data provided'
    };
  }

  // Group data by service
  const serviceGroups = {};
  costData.forEach(item => {
    const service = item.service || item.serviceName || 'Unknown';
    if (!serviceGroups[service]) {
      serviceGroups[service] = [];
    }
    serviceGroups[service].push(item);
  });

  // Calculate service metrics across different groups
  const serviceComparisons = Object.entries(serviceGroups).map(([serviceName, serviceData]) => {
    const totalCost = serviceData.reduce((sum, item) => sum + (item.totalCost || 0), 0);
    const avgCost = totalCost / serviceData.length;
    
    // Group by the specified dimension
    const groups = {};
    serviceData.forEach(item => {
      const groupKey = item[groupBy] || 'Unknown';
      if (!groups[groupKey]) {
        groups[groupKey] = { cost: 0, count: 0 };
      }
      groups[groupKey].cost += item.totalCost || 0;
      groups[groupKey].count += 1;
    });

    // Calculate group statistics
    const groupStats = Object.entries(groups).map(([groupName, data]) => ({
      group: groupName,
      cost: data.cost,
      avgCost: data.cost / data.count,
      usage: data.count,
      efficiency: data.cost / data.count
    })).sort((a, b) => b.cost - a.cost);

    // Calculate variance across groups
    const costs = groupStats.map(g => g.cost);
    const variance = calculateVariance(costs);
    const consistency = 1 / (1 + variance); // Higher consistency is better

    return {
      service: serviceName,
      totalCost,
      avgCost,
      groupCount: groupStats.length,
      groupStats,
      variance,
      consistency,
      utilizationSpread: Math.max(...costs) - Math.min(...costs),
      dataPoints: serviceData.length
    };
  });

  // Sort by total cost
  serviceComparisons.sort((a, b) => b.totalCost - a.totalCost);

  return {
    comparison: {
      services: serviceComparisons,
      groupBy,
      metric,
      analysisDate: new Date().toISOString()
    },
    error: null
  };
};

// Compare regions for cost optimization opportunities
export const compareRegions = (costData, options = {}) => {
  const {
    includeLatency = false,
    includeCompliance = false
  } = options;

  if (!costData || costData.length === 0) {
    return {
      comparison: null,
      error: 'No cost data provided'
    };
  }

  // Group by region
  const regionGroups = {};
  costData.forEach(item => {
    const region = item.region || 'Unknown';
    if (!regionGroups[region]) {
      regionGroups[region] = [];
    }
    regionGroups[region].push(item);
  });

  // Calculate region metrics
  const regionComparisons = Object.entries(regionGroups).map(([regionName, regionData]) => {
    const totalCost = regionData.reduce((sum, item) => sum + (item.totalCost || 0), 0);
    const avgCost = totalCost / regionData.length;
    const serviceCount = new Set(regionData.map(item => item.service || item.serviceName)).size;
    
    // Calculate service distribution
    const serviceBreakdown = {};
    regionData.forEach(item => {
      const service = item.service || item.serviceName || 'Unknown';
      serviceBreakdown[service] = (serviceBreakdown[service] || 0) + item.totalCost;
    });

    // Provider distribution
    const providerBreakdown = {};
    regionData.forEach(item => {
      const provider = item.provider || 'Unknown';
      providerBreakdown[provider] = (providerBreakdown[provider] || 0) + item.totalCost;
    });

    // Simulate region characteristics for educational purposes
    const characteristics = getRegionCharacteristics(regionName);

    return {
      region: regionName,
      totalCost,
      avgCost,
      serviceCount,
      costPerService: totalCost / serviceCount,
      serviceBreakdown,
      providerBreakdown,
      dataPoints: regionData.length,
      characteristics,
      // Educational metrics
      costEfficiency: calculateCostEfficiency(totalCost, serviceCount),
      diversification: Object.keys(serviceBreakdown).length / serviceCount,
      providerDependency: Math.max(...Object.values(providerBreakdown)) / totalCost
    };
  });

  // Sort by total cost
  regionComparisons.sort((a, b) => b.totalCost - a.totalCost);

  // Calculate regional insights
  const insights = generateRegionalInsights(regionComparisons);

  return {
    comparison: {
      regions: regionComparisons,
      insights,
      analysisDate: new Date().toISOString()
    },
    error: null
  };
};

// Generate period-over-period comparisons
export const comparePeriods = (costData, periods = ['current', 'previous']) => {
  if (!costData || costData.length === 0) {
    return {
      comparison: null,
      error: 'No cost data provided'
    };
  }

  // Sort data by date
  const sortedData = [...costData].sort((a, b) => 
    new Date(a.date || a.timestamp) - new Date(b.date || b.timestamp)
  );

  // Split into periods (simplified - assumes 30 days per period)
  const midpoint = Math.floor(sortedData.length / 2);
  const previousPeriod = sortedData.slice(0, midpoint);
  const currentPeriod = sortedData.slice(midpoint);

  const periodComparisons = {
    previous: analyzePeriod(previousPeriod, 'Previous Period'),
    current: analyzePeriod(currentPeriod, 'Current Period')
  };

  // Calculate changes
  const changes = {
    totalCostChange: periodComparisons.current.totalCost - periodComparisons.previous.totalCost,
    totalCostChangePercent: ((periodComparisons.current.totalCost - periodComparisons.previous.totalCost) / periodComparisons.previous.totalCost) * 100,
    avgDailyCostChange: periodComparisons.current.avgDailyCost - periodComparisons.previous.avgDailyCost,
    serviceCountChange: periodComparisons.current.serviceCount - periodComparisons.previous.serviceCount,
    trend: periodComparisons.current.totalCost > periodComparisons.previous.totalCost ? 'increasing' : 'decreasing'
  };

  return {
    comparison: {
      periods: periodComparisons,
      changes,
      analysisDate: new Date().toISOString()
    },
    error: null
  };
};

// Helper functions
const calculateTrend = (sortedData) => {
  if (sortedData.length < 2) return 0;
  
  const firstHalf = sortedData.slice(0, Math.floor(sortedData.length / 2));
  const secondHalf = sortedData.slice(Math.floor(sortedData.length / 2));
  
  const firstAvg = firstHalf.reduce((sum, item) => sum + item.totalCost, 0) / firstHalf.length;
  const secondAvg = secondHalf.reduce((sum, item) => sum + item.totalCost, 0) / secondHalf.length;
  
  return ((secondAvg - firstAvg) / firstAvg) * 100;
};

const calculateVariance = (values) => {
  if (values.length === 0) return 0;
  
  const mean = values.reduce((sum, val) => sum + val, 0) / values.length;
  const squaredDiffs = values.map(val => Math.pow(val - mean, 2));
  return squaredDiffs.reduce((sum, diff) => sum + diff, 0) / values.length;
};

const calculateBenchmarks = (teams) => {
  const costs = teams.map(t => t.totalCost);
  const efficiencies = teams.map(t => t.efficiency);
  
  return {
    cost: {
      min: Math.min(...costs),
      max: Math.max(...costs),
      avg: costs.reduce((sum, cost) => sum + cost, 0) / costs.length,
      median: calculateMedian(costs)
    },
    efficiency: {
      min: Math.min(...efficiencies),
      max: Math.max(...efficiencies),
      avg: efficiencies.reduce((sum, eff) => sum + eff, 0) / efficiencies.length,
      median: calculateMedian(efficiencies)
    }
  };
};

const calculateMedian = (values) => {
  const sorted = [...values].sort((a, b) => a - b);
  const mid = Math.floor(sorted.length / 2);
  return sorted.length % 2 !== 0 ? sorted[mid] : (sorted[mid - 1] + sorted[mid]) / 2;
};

const getRegionCharacteristics = (regionName) => {
  // Simulate region characteristics for educational purposes
  const characteristics = {
    'us-east-1': { latency: 'Low', compliance: 'High', costFactor: 1.0 },
    'us-west-2': { latency: 'Low', compliance: 'High', costFactor: 1.1 },
    'eu-west-1': { latency: 'Medium', compliance: 'High', costFactor: 1.15 },
    'ap-southeast-1': { latency: 'High', compliance: 'Medium', costFactor: 1.2 },
    'global': { latency: 'Variable', compliance: 'High', costFactor: 1.05 }
  };
  
  return characteristics[regionName] || { latency: 'Unknown', compliance: 'Unknown', costFactor: 1.0 };
};

const calculateCostEfficiency = (totalCost, serviceCount) => {
  if (serviceCount === 0) return 0;
  return totalCost / serviceCount;
};

const analyzePeriod = (periodData, periodName) => {
  const totalCost = periodData.reduce((sum, item) => sum + (item.totalCost || 0), 0);
  const avgDailyCost = totalCost / Math.max(periodData.length, 1);
  const serviceCount = new Set(periodData.map(item => item.service || item.serviceName)).size;
  
  return {
    name: periodName,
    totalCost,
    avgDailyCost,
    serviceCount,
    dataPoints: periodData.length,
    dateRange: {
      start: periodData[0]?.date || periodData[0]?.timestamp,
      end: periodData[periodData.length - 1]?.date || periodData[periodData.length - 1]?.timestamp
    }
  };
};

const generateRegionalInsights = (regions) => {
  const insights = [];
  
  // Find most cost-effective region
  const mostEfficient = regions.reduce((best, current) => 
    current.costPerService < best.costPerService ? current : best
  );
  insights.push({
    type: 'cost_efficiency',
    title: 'Most Cost-Effective Region',
    description: `${mostEfficient.region} offers the best cost per service ratio`,
    region: mostEfficient.region,
    metric: mostEfficient.costPerService
  });
  
  // Find highest diversity
  const mostDiverse = regions.reduce((best, current) => 
    current.diversification > best.diversification ? current : best
  );
  insights.push({
    type: 'diversification',
    title: 'Most Service-Diverse Region',
    description: `${mostDiverse.region} has the highest service diversification`,
    region: mostDiverse.region,
    metric: mostDiverse.diversification
  });
  
  return insights;
};

// Format comparison data for visualization
export const formatComparisonForChart = (comparison, chartType = 'bar') => {
  if (!comparison || !comparison.teams) {
    return { labels: [], datasets: [] };
  }

  const labels = comparison.teams.map(team => team.team);
  
  const datasets = [{
    label: 'Total Cost ($)',
    data: comparison.teams.map(team => team.totalCost),
    backgroundColor: comparison.teams.map((_, index) => 
      `hsl(${index * 360 / comparison.teams.length}, 70%, 60%)`
    ),
    borderColor: comparison.teams.map((_, index) => 
      `hsl(${index * 360 / comparison.teams.length}, 70%, 50%)`
    ),
    borderWidth: 1
  }];

  return { labels, datasets };
};

// Generate educational comparison scenarios
export const generateComparisonScenarios = () => {
  return {
    team_efficiency: {
      name: 'Team Efficiency Analysis',
      description: 'Compare team cost efficiency and identify optimization opportunities',
      metrics: ['total_cost', 'efficiency', 'trend'],
      learningObjectives: [
        'Identify high-performing teams',
        'Understand cost efficiency metrics',
        'Recognize optimization patterns'
      ]
    },
    service_utilization: {
      name: 'Service Utilization Comparison',
      description: 'Analyze service usage patterns across teams and regions',
      metrics: ['cost_per_service', 'utilization', 'consistency'],
      learningObjectives: [
        'Compare service adoption patterns',
        'Identify underutilized services',
        'Optimize service portfolio'
      ]
    },
    regional_optimization: {
      name: 'Regional Cost Optimization',
      description: 'Compare costs across regions to identify migration opportunities',
      metrics: ['cost_efficiency', 'service_availability', 'compliance'],
      learningObjectives: [
        'Understand regional cost differences',
        'Evaluate migration trade-offs',
        'Consider compliance requirements'
      ]
    }
  };
};

export default {
  compareTeams,
  compareServices,
  compareRegions,
  comparePeriods,
  formatComparisonForChart,
  generateComparisonScenarios
};