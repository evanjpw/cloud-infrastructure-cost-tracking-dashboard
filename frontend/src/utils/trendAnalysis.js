// Trend Analysis for Learning Scenarios
// Provides seasonal patterns and growth predictions for educational cost analysis

import { movingAverage, seasonalDecomposition } from './predictiveModeling';

// Identify cost trends in the data
export const identifyTrends = (costData, options = {}) => {
  const {
    windowSize = 7,
    minTrendLength = 3,
    trendThreshold = 0.05 // 5% change threshold
  } = options;

  if (!costData || costData.length < windowSize) {
    return { trends: [], summary: 'Insufficient data for trend analysis' };
  }

  // Sort data by date
  const sortedData = [...costData].sort((a, b) => 
    new Date(a.date || a.timestamp) - new Date(b.date || b.timestamp)
  );

  // Calculate moving average
  const smoothedData = movingAverage(sortedData, windowSize);
  
  // Identify trend segments
  const trends = [];
  let currentTrend = null;
  let trendStart = 0;

  for (let i = 1; i < smoothedData.length; i++) {
    const current = smoothedData[i].movingAverage || smoothedData[i].totalCost;
    const previous = smoothedData[i - 1].movingAverage || smoothedData[i - 1].totalCost;
    const changeRate = (current - previous) / previous;

    let trendType = 'stable';
    if (changeRate > trendThreshold) trendType = 'increasing';
    else if (changeRate < -trendThreshold) trendType = 'decreasing';

    if (currentTrend !== trendType) {
      // Save previous trend if it's long enough
      if (currentTrend && i - trendStart >= minTrendLength) {
        const trendData = smoothedData.slice(trendStart, i);
        const startValue = trendData[0].movingAverage || trendData[0].totalCost;
        const endValue = trendData[trendData.length - 1].movingAverage || trendData[trendData.length - 1].totalCost;
        
        trends.push({
          type: currentTrend,
          startDate: trendData[0].date || trendData[0].timestamp,
          endDate: trendData[trendData.length - 1].date || trendData[trendData.length - 1].timestamp,
          duration: trendData.length,
          startValue,
          endValue,
          changeAmount: endValue - startValue,
          changePercent: ((endValue - startValue) / startValue) * 100,
          avgDailyChange: (endValue - startValue) / trendData.length
        });
      }
      
      currentTrend = trendType;
      trendStart = i;
    }
  }

  // Add final trend
  if (currentTrend && smoothedData.length - trendStart >= minTrendLength) {
    const trendData = smoothedData.slice(trendStart);
    const startValue = trendData[0].movingAverage || trendData[0].totalCost;
    const endValue = trendData[trendData.length - 1].movingAverage || trendData[trendData.length - 1].totalCost;
    
    trends.push({
      type: currentTrend,
      startDate: trendData[0].date || trendData[0].timestamp,
      endDate: trendData[trendData.length - 1].date || trendData[trendData.length - 1].timestamp,
      duration: trendData.length,
      startValue,
      endValue,
      changeAmount: endValue - startValue,
      changePercent: ((endValue - startValue) / startValue) * 100,
      avgDailyChange: (endValue - startValue) / trendData.length
    });
  }

  // Generate summary
  const summary = generateTrendSummary(trends, sortedData);

  return { trends, smoothedData, summary };
};

// Analyze seasonal patterns in cost data
export const analyzeSeasonalPatterns = (costData, options = {}) => {
  const {
    seasonLength = 30, // Monthly seasons by default
    minDataPoints = 60 // Need at least 2 seasons
  } = options;

  if (!costData || costData.length < minDataPoints) {
    return {
      hasSeasonality: false,
      patterns: [],
      strength: 0,
      summary: 'Insufficient data for seasonal analysis'
    };
  }

  // Perform seasonal decomposition
  const { trend, seasonal, residual } = seasonalDecomposition(costData, seasonLength);

  // Calculate seasonality strength
  const seasonalVariance = seasonal.reduce((sum, s) => sum + Math.pow(s.seasonal || 0, 2), 0) / seasonal.length;
  const totalVariance = costData.reduce((sum, d, i) => {
    const mean = costData.reduce((s, d) => s + d.totalCost, 0) / costData.length;
    return sum + Math.pow(d.totalCost - mean, 2);
  }, 0) / costData.length;
  
  const seasonalityStrength = seasonalVariance / totalVariance;
  const hasSeasonality = seasonalityStrength > 0.1; // 10% threshold

  // Identify seasonal patterns
  const patterns = [];
  if (hasSeasonality) {
    // Find peak and trough periods
    const seasonalPattern = seasonal.slice(0, seasonLength).map((s, i) => ({
      position: i,
      value: s.seasonal || 0,
      dayOfMonth: i + 1
    }));

    // Sort to find peaks and troughs
    const sortedPattern = [...seasonalPattern].sort((a, b) => b.value - a.value);
    const peaks = sortedPattern.slice(0, 3).filter(p => p.value > 0);
    const troughs = sortedPattern.slice(-3).filter(p => p.value < 0);

    patterns.push({
      type: 'monthly',
      peaks: peaks.map(p => ({
        dayOfMonth: p.dayOfMonth,
        impact: p.value,
        impactPercent: (p.value / (totalVariance ** 0.5)) * 100
      })),
      troughs: troughs.map(t => ({
        dayOfMonth: t.dayOfMonth,
        impact: t.value,
        impactPercent: (t.value / (totalVariance ** 0.5)) * 100
      })),
      strength: seasonalityStrength
    });

    // Check for day-of-week patterns
    const dayOfWeekPattern = analyzeDayOfWeekPattern(costData);
    if (dayOfWeekPattern.hasPattern) {
      patterns.push(dayOfWeekPattern);
    }
  }

  const summary = generateSeasonalSummary(patterns, hasSeasonality, seasonalityStrength);

  return {
    hasSeasonality,
    patterns,
    strength: seasonalityStrength,
    decomposition: { trend, seasonal, residual },
    summary
  };
};

// Analyze day-of-week patterns
const analyzeDayOfWeekPattern = (costData) => {
  const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
  const dayAggregates = Array(7).fill(null).map(() => ({ total: 0, count: 0 }));

  costData.forEach(point => {
    const date = new Date(point.date || point.timestamp);
    const dayOfWeek = date.getDay();
    dayAggregates[dayOfWeek].total += point.totalCost;
    dayAggregates[dayOfWeek].count += 1;
  });

  const dayAverages = dayAggregates.map((agg, i) => ({
    day: dayNames[i],
    dayIndex: i,
    average: agg.count > 0 ? agg.total / agg.count : 0,
    count: agg.count
  }));

  // Calculate if there's a significant day-of-week pattern
  const overallAverage = costData.reduce((sum, d) => sum + d.totalCost, 0) / costData.length;
  const maxDeviation = Math.max(...dayAverages.map(d => Math.abs(d.average - overallAverage)));
  const hasPattern = maxDeviation > overallAverage * 0.15; // 15% deviation threshold

  if (hasPattern) {
    const sortedDays = [...dayAverages].sort((a, b) => b.average - a.average);
    return {
      type: 'weekly',
      hasPattern: true,
      highCostDays: sortedDays.slice(0, 2).map(d => ({
        day: d.day,
        average: d.average,
        deviationPercent: ((d.average - overallAverage) / overallAverage) * 100
      })),
      lowCostDays: sortedDays.slice(-2).map(d => ({
        day: d.day,
        average: d.average,
        deviationPercent: ((d.average - overallAverage) / overallAverage) * 100
      })),
      strength: maxDeviation / overallAverage
    };
  }

  return { type: 'weekly', hasPattern: false };
};

// Generate trend summary for educational insights
const generateTrendSummary = (trends, originalData) => {
  if (trends.length === 0) {
    return {
      overall: 'No significant trends detected',
      details: [],
      recommendation: 'Continue monitoring for emerging patterns'
    };
  }

  const totalDays = originalData.length;
  const overallChange = originalData[originalData.length - 1].totalCost - originalData[0].totalCost;
  const overallChangePercent = (overallChange / originalData[0].totalCost) * 100;

  const increasingTrends = trends.filter(t => t.type === 'increasing');
  const decreasingTrends = trends.filter(t => t.type === 'decreasing');
  const stableTrends = trends.filter(t => t.type === 'stable');

  const details = [];
  
  if (increasingTrends.length > 0) {
    const avgIncrease = increasingTrends.reduce((sum, t) => sum + t.changePercent, 0) / increasingTrends.length;
    details.push(`${increasingTrends.length} increasing periods with average ${avgIncrease.toFixed(1)}% growth`);
  }
  
  if (decreasingTrends.length > 0) {
    const avgDecrease = decreasingTrends.reduce((sum, t) => sum + t.changePercent, 0) / decreasingTrends.length;
    details.push(`${decreasingTrends.length} decreasing periods with average ${Math.abs(avgDecrease).toFixed(1)}% reduction`);
  }
  
  if (stableTrends.length > 0) {
    details.push(`${stableTrends.length} stable periods`);
  }

  const recommendation = generateTrendRecommendation(trends, overallChangePercent);

  return {
    overall: `${overallChangePercent >= 0 ? 'Increasing' : 'Decreasing'} trend of ${Math.abs(overallChangePercent).toFixed(1)}% over ${totalDays} days`,
    details,
    recommendation,
    metrics: {
      totalChange: overallChange,
      totalChangePercent: overallChangePercent,
      avgDailyChange: overallChange / totalDays,
      volatility: calculateVolatility(originalData)
    }
  };
};

// Generate seasonal summary for educational insights
const generateSeasonalSummary = (patterns, hasSeasonality, strength) => {
  if (!hasSeasonality) {
    return {
      overall: 'No significant seasonal patterns detected',
      details: [],
      recommendation: 'Costs appear consistent without seasonal variations'
    };
  }

  const details = [];
  const monthlyPattern = patterns.find(p => p.type === 'monthly');
  const weeklyPattern = patterns.find(p => p.type === 'weekly');

  if (monthlyPattern) {
    details.push(`Monthly pattern detected with ${(strength * 100).toFixed(1)}% impact on costs`);
    if (monthlyPattern.peaks.length > 0) {
      details.push(`Peak spending around day ${monthlyPattern.peaks[0].dayOfMonth} of month`);
    }
    if (monthlyPattern.troughs.length > 0) {
      details.push(`Lowest spending around day ${monthlyPattern.troughs[0].dayOfMonth} of month`);
    }
  }

  if (weeklyPattern && weeklyPattern.hasPattern) {
    details.push(`Weekly pattern: highest costs on ${weeklyPattern.highCostDays[0].day}`);
    details.push(`Lowest costs on ${weeklyPattern.lowCostDays[0].day}`);
  }

  const recommendation = generateSeasonalRecommendation(patterns, strength);

  return {
    overall: `Seasonal patterns account for ${(strength * 100).toFixed(1)}% of cost variation`,
    details,
    recommendation
  };
};

// Generate recommendations based on trends
const generateTrendRecommendation = (trends, overallChange) => {
  if (Math.abs(overallChange) < 5) {
    return 'Costs are relatively stable. Monitor for future changes.';
  } else if (overallChange > 20) {
    return 'Significant cost increase detected. Investigate root causes and consider optimization.';
  } else if (overallChange < -20) {
    return 'Significant cost reduction achieved. Document successful optimizations.';
  } else if (overallChange > 0) {
    return 'Moderate cost increase observed. Review recent changes and usage patterns.';
  } else {
    return 'Cost reduction trend identified. Continue current optimization efforts.';
  }
};

// Generate recommendations based on seasonal patterns
const generateSeasonalRecommendation = (patterns, strength) => {
  if (strength < 0.1) {
    return 'Minimal seasonal impact. Focus on other optimization areas.';
  } else if (strength > 0.3) {
    return 'Strong seasonal patterns. Plan capacity and budgets according to peak periods.';
  } else {
    return 'Moderate seasonal variations. Consider time-based scaling strategies.';
  }
};

// Calculate cost volatility
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

// Format trends for visualization
export const formatTrendsForVisualization = (trendAnalysis) => {
  const { trends, smoothedData } = trendAnalysis;
  
  // Create segments for each trend
  const segments = trends.map(trend => ({
    data: smoothedData.filter(d => {
      const date = new Date(d.date || d.timestamp);
      const startDate = new Date(trend.startDate);
      const endDate = new Date(trend.endDate);
      return date >= startDate && date <= endDate;
    }),
    type: trend.type,
    metadata: trend
  }));

  return segments;
};

// Generate educational scenarios based on trends
export const generateTrendScenarios = (costData) => {
  const scenarios = [];

  // Scenario 1: Steady Growth
  scenarios.push({
    name: 'Steady Growth',
    description: 'Consistent 5% monthly increase in costs',
    data: generateScenarioData(costData, 'linear', { growthRate: 0.05 })
  });

  // Scenario 2: Seasonal Variation
  scenarios.push({
    name: 'Seasonal Pattern',
    description: 'Monthly peaks at mid-month, troughs at month-end',
    data: generateScenarioData(costData, 'seasonal', { amplitude: 0.2 })
  });

  // Scenario 3: Volatile Growth
  scenarios.push({
    name: 'Volatile Growth',
    description: 'Unpredictable spikes and drops with upward trend',
    data: generateScenarioData(costData, 'volatile', { volatility: 0.3 })
  });

  // Scenario 4: Cost Optimization
  scenarios.push({
    name: 'Optimization Success',
    description: 'Gradual reduction through optimization efforts',
    data: generateScenarioData(costData, 'optimization', { reductionRate: 0.03 })
  });

  return scenarios;
};

// Generate scenario data for educational purposes
const generateScenarioData = (baseData, type, params) => {
  const days = 90; // 3 months of data
  const baselineCost = baseData.length > 0 ? 
    baseData.reduce((sum, d) => sum + d.totalCost, 0) / baseData.length : 
    1000;

  const data = [];
  const startDate = new Date();
  startDate.setDate(startDate.getDate() - days);

  for (let i = 0; i < days; i++) {
    const date = new Date(startDate);
    date.setDate(date.getDate() + i);
    
    let cost = baselineCost;

    switch (type) {
      case 'linear':
        cost = baselineCost * (1 + (params.growthRate * i / 30));
        break;
      
      case 'seasonal':
        const dayOfMonth = date.getDate();
        const seasonalFactor = Math.sin((dayOfMonth / 30) * 2 * Math.PI) * params.amplitude;
        cost = baselineCost * (1 + seasonalFactor) * (1 + (0.02 * i / 30));
        break;
      
      case 'volatile':
        const randomFactor = (Math.random() - 0.5) * params.volatility;
        cost = baselineCost * (1 + randomFactor) * (1 + (0.03 * i / 30));
        break;
      
      case 'optimization':
        cost = baselineCost * Math.pow(1 - params.reductionRate, i / 30);
        break;
    }

    data.push({
      date: date.toISOString().split('T')[0],
      totalCost: Math.max(0, cost + (Math.random() - 0.5) * cost * 0.05), // Add small noise
      scenario: type
    });
  }

  return data;
};

export default {
  identifyTrends,
  analyzeSeasonalPatterns,
  formatTrendsForVisualization,
  generateTrendScenarios
};