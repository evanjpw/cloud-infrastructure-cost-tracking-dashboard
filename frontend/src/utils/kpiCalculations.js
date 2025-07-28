// Utility functions for calculating KPI metrics

// Calculate current period metrics
export const calculateCurrentPeriodMetrics = (aggregatedData, granularity) => {
  if (!aggregatedData || aggregatedData.length === 0) {
    return {
      currentSpend: 0,
      averageDaily: 0,
      totalDataPoints: 0,
      periodType: granularity
    };
  }

  const totalSpend = aggregatedData.reduce((sum, item) => sum + (item.totalCost || 0), 0);
  const dataPoints = aggregatedData.length;
  
  // Calculate daily average based on granularity
  let averageDaily;
  switch (granularity) {
    case "daily":
      averageDaily = totalSpend / Math.max(dataPoints, 1);
      break;
    case "weekly":
      averageDaily = totalSpend / Math.max(dataPoints * 7, 1);
      break;
    case "monthly":
      // Estimate ~30 days per month
      averageDaily = totalSpend / Math.max(dataPoints * 30, 1);
      break;
    default:
      averageDaily = totalSpend / Math.max(dataPoints, 1);
  }

  return {
    currentSpend: Math.round(totalSpend * 100) / 100,
    averageDaily: Math.round(averageDaily * 100) / 100,
    totalDataPoints: dataPoints,
    periodType: granularity
  };
};

// Calculate month-over-month change
export const calculateMonthOverMonthChange = (aggregatedData, granularity) => {
  if (!aggregatedData || aggregatedData.length < 2) {
    return { change: 0, changePercent: 0, trend: "neutral" };
  }

  // For period-over-period comparison
  const currentPeriod = aggregatedData[aggregatedData.length - 1];
  const previousPeriod = aggregatedData[aggregatedData.length - 2];

  if (!currentPeriod || !previousPeriod) {
    return { change: 0, changePercent: 0, trend: "neutral" };
  }

  const change = currentPeriod.totalCost - previousPeriod.totalCost;
  const changePercent = previousPeriod.totalCost > 0 
    ? (change / previousPeriod.totalCost) * 100 
    : 0;

  let trend = "neutral";
  if (changePercent > 5) trend = "up";
  else if (changePercent < -5) trend = "down";

  return {
    change: Math.round(change * 100) / 100,
    changePercent: Math.round(changePercent * 10) / 10,
    trend,
    currentValue: currentPeriod.totalCost,
    previousValue: previousPeriod.totalCost
  };
};

// Find top cost driver from service data
export const getTopCostDriver = (costData) => {
  if (!costData || costData.length === 0) {
    return { service: "No data", cost: 0, percentage: 0 };
  }

  // Sort by cost and get the highest
  const sorted = [...costData].sort((a, b) => (b.totalCost || 0) - (a.totalCost || 0));
  const topService = sorted[0];
  const totalCost = costData.reduce((sum, item) => sum + (item.totalCost || 0), 0);
  
  const percentage = totalCost > 0 ? (topService.totalCost / totalCost) * 100 : 0;

  return {
    service: topService.service || "Unknown Service",
    cost: Math.round((topService.totalCost || 0) * 100) / 100,
    percentage: Math.round(percentage * 10) / 10
  };
};

// Calculate forecast for next period
export const calculateForecast = (aggregatedData, granularity) => {
  if (!aggregatedData || aggregatedData.length < 3) {
    return { forecastAmount: 0, confidence: "low", trend: "neutral" };
  }

  // Simple linear regression for trend
  const recentData = aggregatedData.slice(-6); // Use last 6 periods
  const xValues = recentData.map((_, index) => index + 1);
  const yValues = recentData.map(item => item.totalCost);

  // Calculate linear trend
  const n = recentData.length;
  const sumX = xValues.reduce((a, b) => a + b, 0);
  const sumY = yValues.reduce((a, b) => a + b, 0);
  const sumXY = xValues.reduce((sum, x, i) => sum + x * yValues[i], 0);
  const sumXX = xValues.reduce((sum, x) => sum + x * x, 0);

  const slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
  const intercept = (sumY - slope * sumX) / n;

  // Forecast next period
  const nextX = n + 1;
  const forecastAmount = slope * nextX + intercept;

  // Determine confidence based on data consistency
  const variance = yValues.reduce((sum, y, i) => {
    const expected = slope * (i + 1) + intercept;
    return sum + Math.pow(y - expected, 2);
  }, 0) / n;

  const avgValue = sumY / n;
  const coefficientOfVariation = Math.sqrt(variance) / avgValue;

  let confidence = "high";
  if (coefficientOfVariation > 0.3) confidence = "medium";
  if (coefficientOfVariation > 0.6) confidence = "low";

  let trend = "neutral";
  if (slope > avgValue * 0.05) trend = "up";
  else if (slope < -avgValue * 0.05) trend = "down";

  return {
    forecastAmount: Math.max(0, Math.round(forecastAmount * 100) / 100),
    confidence,
    trend,
    trendValue: Math.round(slope * 100) / 100
  };
};

// Generate budget comparison (simulated)
export const generateBudgetComparison = (currentSpend, startDate, endDate) => {
  // Simulate a monthly budget based on current spend patterns
  const start = new Date(startDate);
  const end = new Date(endDate);
  const daysInPeriod = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
  
  // Estimate monthly budget (current spend extrapolated to 30 days)
  const estimatedMonthlySpend = (currentSpend / daysInPeriod) * 30;
  const simulatedBudget = estimatedMonthlySpend * 1.15; // 15% buffer
  
  const budgetUsed = (currentSpend / simulatedBudget) * 100;
  const remaining = simulatedBudget - currentSpend;
  
  let status = "on-track";
  if (budgetUsed > 90) status = "at-risk";
  else if (budgetUsed > 100) status = "over-budget";
  else if (budgetUsed < 60) status = "under-budget";

  return {
    budget: Math.round(simulatedBudget * 100) / 100,
    spent: currentSpend,
    remaining: Math.max(0, Math.round(remaining * 100) / 100),
    percentUsed: Math.round(budgetUsed * 10) / 10,
    status,
    daysInPeriod
  };
};

// Format currency for display
export const formatCurrency = (amount, options = {}) => {
  const { compact = false, showCents = false } = options;
  
  if (compact && amount >= 1000) {
    if (amount >= 1000000000) {
      return `$${(amount / 1000000000).toFixed(1)}B`;
    }
    if (amount >= 1000000) {
      return `$${(amount / 1000000).toFixed(1)}M`;
    }
    if (amount >= 1000) {
      return `$${(amount / 1000).toFixed(1)}K`;
    }
  }
  
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    minimumFractionDigits: showCents ? 2 : 0,
    maximumFractionDigits: showCents ? 2 : 0,
  }).format(amount);
};

// Format percentage change
export const formatPercentChange = (percent) => {
  const sign = percent > 0 ? "+" : "";
  return `${sign}${percent}%`;
};

// Get trend icon
export const getTrendIcon = (trend) => {
  switch (trend) {
    case "up": return "📈";
    case "down": return "📉";
    default: return "➡️";
  }
};

// Get status color
export const getStatusColor = (status, colors) => {
  switch (status) {
    case "up":
    case "at-risk":  
    case "over-budget":
      return colors.error;
    case "down":
    case "under-budget":
      return colors.success;
    case "on-track":
      return colors.primary[500];
    default:
      return colors.text.secondary;
  }
};