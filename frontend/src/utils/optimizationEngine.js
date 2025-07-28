// Cost Optimization Engine
// Analyzes cost data and generates realistic optimization recommendations

export const generateOptimizationRecommendations = (costData) => {
  if (!costData || costData.length === 0) {
    return [];
  }

  const recommendations = [];

  // Analyze spending patterns
  const serviceSpending = analyzeServiceSpending(costData);
  const teamSpending = analyzeTeamSpending(costData);
  const timePatterns = analyzeTimePatterns(costData);

  // Generate different types of recommendations
  recommendations.push(...generateRightSizingRecommendations(serviceSpending));
  recommendations.push(...generateReservedInstanceRecommendations(serviceSpending));
  recommendations.push(...generateUnusedResourceRecommendations(serviceSpending));
  recommendations.push(...generateAnomalyRecommendations(timePatterns));
  recommendations.push(...generateGeneralOptimizationRecommendations(serviceSpending, teamSpending));

  // Sort by potential savings (highest first)
  return recommendations
    .sort((a, b) => (b.potentialSavings || 0) - (a.potentialSavings || 0))
    .slice(0, 15); // Limit to top 15 recommendations
};

const analyzeServiceSpending = (costData) => {
  const serviceMap = {};
  
  costData.forEach(item => {
    const service = item.service || item.serviceName || 'Unknown';
    if (!serviceMap[service]) {
      serviceMap[service] = {
        name: service,
        totalCost: 0,
        records: [],
        avgCost: 0
      };
    }
    serviceMap[service].totalCost += item.totalCost || 0;
    serviceMap[service].records.push(item);
  });

  // Calculate averages and patterns
  Object.values(serviceMap).forEach(service => {
    service.avgCost = service.totalCost / service.records.length;
    service.maxCost = Math.max(...service.records.map(r => r.totalCost || 0));
    service.minCost = Math.min(...service.records.map(r => r.totalCost || 0));
    service.variance = service.maxCost - service.minCost;
  });

  return Object.values(serviceMap).sort((a, b) => b.totalCost - a.totalCost);
};

const analyzeTeamSpending = (costData) => {
  const teamMap = {};
  
  costData.forEach(item => {
    const team = item.team || 'Unknown';
    if (!teamMap[team]) {
      teamMap[team] = {
        name: team,
        totalCost: 0,
        services: new Set(),
        avgCost: 0
      };
    }
    teamMap[team].totalCost += item.totalCost || 0;
    teamMap[team].services.add(item.service || item.serviceName);
  });

  return Object.values(teamMap).sort((a, b) => b.totalCost - a.totalCost);
};

const analyzeTimePatterns = (costData) => {
  // Simple time pattern analysis
  const dailySpending = {};
  
  costData.forEach(item => {
    const date = item.date || item.timestamp || new Date().toISOString().split('T')[0];
    if (!dailySpending[date]) {
      dailySpending[date] = 0;
    }
    dailySpending[date] += item.totalCost || 0;
  });

  const spendingValues = Object.values(dailySpending);
  const avgDailySpend = spendingValues.reduce((sum, val) => sum + val, 0) / spendingValues.length;
  const maxDailySpend = Math.max(...spendingValues);
  
  return {
    dailySpending,
    avgDailySpend,
    maxDailySpend,
    hasAnomalies: maxDailySpend > avgDailySpend * 2
  };
};

const generateRightSizingRecommendations = (serviceSpending) => {
  const recommendations = [];
  
  // Focus on high-cost, high-variance services
  const candidates = serviceSpending
    .filter(service => service.variance > service.avgCost * 0.5 && service.totalCost > 1000)
    .slice(0, 3);

  candidates.forEach(service => {
    const potentialSavings = Math.floor(service.totalCost * 0.25); // 25% savings potential
    
    recommendations.push({
      id: `rightsizing-${service.name}`,
      type: 'rightsizing',
      title: `Right-size ${service.name} instances`,
      description: `High cost variance detected for ${service.name}. Current usage patterns suggest over-provisioning. Right-sizing could reduce costs while maintaining performance.`,
      impact: potentialSavings > 2000 ? 'high' : potentialSavings > 500 ? 'medium' : 'low',
      potentialSavings,
      savingsPeriod: 'monthly',
      service: service.name,
      currentConfig: {
        'Instance Type': getInstanceType(service.name),
        'Current Cost': `$${service.totalCost.toLocaleString()}`,
        'Utilization Pattern': 'Variable (high variance)',
        'Peak Usage': `$${service.maxCost.toFixed(2)}/day`
      },
      recommendedConfig: {
        'Recommended Type': getRecommendedInstanceType(service.name),
        'Estimated Cost': `$${(service.totalCost - potentialSavings).toLocaleString()}`,
        'Optimization': 'Auto-scaling enabled',
        'Expected Savings': `$${potentialSavings.toLocaleString()}/month`
      },
      steps: [
        'Analyze current usage patterns and peak demand',
        'Select appropriately sized instance types',
        'Configure auto-scaling policies',
        'Migrate workloads during maintenance window',
        'Monitor performance after changes'
      ],
      risks: [
        'Temporary performance impact during migration',
        'Need to verify auto-scaling thresholds',
        'May require application optimization'
      ]
    });
  });

  return recommendations;
};

const generateReservedInstanceRecommendations = (serviceSpending) => {
  const recommendations = [];
  
  // Target high-cost, consistent services
  const candidates = serviceSpending
    .filter(service => 
      service.totalCost > 1500 && 
      service.variance < service.avgCost * 0.3 && 
      (service.name.includes('EC2') || service.name.includes('RDS') || service.name.includes('Compute'))
    )
    .slice(0, 2);

  candidates.forEach(service => {
    const potentialSavings = Math.floor(service.totalCost * 0.40); // 40% savings with RIs
    
    recommendations.push({
      id: `ri-${service.name}`,
      type: 'reserved_instance',
      title: `Purchase Reserved Instances for ${service.name}`,
      description: `${service.name} shows consistent usage patterns. Reserved Instances could provide significant cost savings with minimal risk.`,
      impact: potentialSavings > 2000 ? 'high' : potentialSavings > 800 ? 'medium' : 'low',
      potentialSavings,
      savingsPeriod: 'annual',
      service: service.name,
      currentConfig: {
        'Current Model': 'On-Demand',
        'Monthly Cost': `$${service.totalCost.toLocaleString()}`,
        'Usage Pattern': 'Consistent (low variance)',
        'Annual Cost': `$${(service.totalCost * 12).toLocaleString()}`
      },
      recommendedConfig: {
        'Recommended Model': '1-Year Reserved Instance',
        'Monthly Cost': `$${(service.totalCost - potentialSavings/12).toLocaleString()}`,
        'Commitment': '1 Year',
        'Annual Savings': `$${potentialSavings.toLocaleString()}`
      },
      steps: [
        'Review usage patterns over past 6 months',
        'Calculate optimal RI mix (Standard vs Convertible)',
        'Purchase Reserved Instances through cloud console',
        'Monitor RI utilization and coverage',
        'Set up alerts for RI expiration'
      ],
      risks: [
        'Upfront payment commitment required',
        'Less flexibility for workload changes',
        'Need to track RI expiration dates'
      ]
    });
  });

  return recommendations;
};

const generateUnusedResourceRecommendations = (serviceSpending) => {
  const recommendations = [];
  
  // Target low-cost, potentially unused services
  const candidates = serviceSpending
    .filter(service => service.totalCost < 200 && service.avgCost < 10)
    .slice(0, 2);

  candidates.forEach(service => {
    const potentialSavings = Math.floor(service.totalCost * 0.90); // 90% savings by removing
    
    recommendations.push({
      id: `unused-${service.name}`,
      type: 'unused_resource',
      title: `Review unused ${service.name} resources`,
      description: `${service.name} shows very low utilization costs. These resources may be orphaned or no longer needed.`,
      impact: 'low',
      potentialSavings,
      savingsPeriod: 'monthly',
      service: service.name,
      steps: [
        'Audit resource usage and dependencies',
        'Check if resources are attached to active workloads',
        'Contact resource owners for confirmation',
        'Schedule cleanup during maintenance window',
        'Document cleanup process for future reference'
      ],
      risks: [
        'May impact dependent services',
        'Need owner confirmation before deletion',
        'Consider backup before cleanup'
      ]
    });
  });

  return recommendations;
};

const generateAnomalyRecommendations = (timePatterns) => {
  const recommendations = [];
  
  if (timePatterns.hasAnomalies) {
    recommendations.push({
      id: 'spending-anomaly',
      type: 'anomaly',
      title: 'Investigate spending anomalies',
      description: `Detected spending spikes up to $${timePatterns.maxDailySpend.toFixed(0)} compared to average of $${timePatterns.avgDailySpend.toFixed(0)}. This suggests potential cost optimization opportunities.`,
      impact: 'medium',
      potentialSavings: Math.floor((timePatterns.maxDailySpend - timePatterns.avgDailySpend) * 30),
      savingsPeriod: 'monthly',
      steps: [
        'Identify services causing spending spikes',
        'Review resource scaling policies',
        'Implement cost monitoring alerts',
        'Optimize auto-scaling thresholds',
        'Set up budget alerts for early detection'
      ],
      risks: [
        'May indicate legitimate business growth',
        'Need to balance cost vs performance',
        'Require ongoing monitoring'
      ]
    });
  }

  return recommendations;
};

const generateGeneralOptimizationRecommendations = (serviceSpending, teamSpending) => {
  const recommendations = [];
  
  // General optimization recommendations
  if (serviceSpending.length > 0) {
    const totalCost = serviceSpending.reduce((sum, service) => sum + service.totalCost, 0);
    
    recommendations.push({
      id: 'general-optimization',
      type: 'optimization',
      title: 'Implement cost monitoring and governance',
      description: 'Establish comprehensive cost monitoring, tagging strategies, and governance policies to prevent future cost overruns.',
      impact: 'high',
      potentialSavings: Math.floor(totalCost * 0.15), // 15% through better governance
      savingsPeriod: 'ongoing',
      steps: [
        'Implement mandatory resource tagging strategy',
        'Set up automated cost monitoring and alerts',
        'Establish team-based cost allocation',
        'Create monthly cost review process',
        'Implement approval workflows for large resources'
      ],
      risks: [
        'Requires organizational change management',
        'May slow down development initially',
        'Need executive buy-in for governance policies'
      ]
    });
  }

  return recommendations;
};

// Utility functions for realistic recommendations
const getInstanceType = (serviceName) => {
  if (serviceName.includes('EC2')) return 'm5.2xlarge';
  if (serviceName.includes('RDS')) return 'db.r5.xlarge';
  if (serviceName.includes('Lambda')) return 'N/A (Serverless)';
  return 'Standard';
};

const getRecommendedInstanceType = (serviceName) => {
  if (serviceName.includes('EC2')) return 'm5.large + Auto Scaling';
  if (serviceName.includes('RDS')) return 'db.r5.large';
  if (serviceName.includes('Lambda')) return 'Optimized Memory';
  return 'Optimized';
};

export default {
  generateOptimizationRecommendations
};