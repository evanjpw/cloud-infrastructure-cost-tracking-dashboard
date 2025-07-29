// Predictive Cost Modeling for Educational Scenarios
// Provides basic machine learning functionality for cost forecasting in learning scenarios

// Simple linear regression for cost prediction
export const linearRegression = (data) => {
  if (!data || data.length < 2) {
    return { slope: 0, intercept: 0, r2: 0 };
  }

  const n = data.length;
  const sumX = data.reduce((sum, point, i) => sum + i, 0);
  const sumY = data.reduce((sum, point) => sum + point.totalCost, 0);
  const sumXY = data.reduce((sum, point, i) => sum + i * point.totalCost, 0);
  const sumX2 = data.reduce((sum, point, i) => sum + i * i, 0);

  const slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
  const intercept = (sumY - slope * sumX) / n;

  // Calculate R-squared for model quality
  const meanY = sumY / n;
  const ssTotal = data.reduce((sum, point) => sum + Math.pow(point.totalCost - meanY, 2), 0);
  const ssResidual = data.reduce((sum, point, i) => {
    const predicted = slope * i + intercept;
    return sum + Math.pow(point.totalCost - predicted, 2);
  }, 0);
  const r2 = 1 - (ssResidual / ssTotal);

  return { slope, intercept, r2 };
};

// Moving average for trend smoothing
export const movingAverage = (data, window = 7) => {
  if (!data || data.length < window) return data;

  const result = [];
  for (let i = 0; i < data.length; i++) {
    if (i < window - 1) {
      result.push(data[i]);
    } else {
      const windowData = data.slice(i - window + 1, i + 1);
      const avg = windowData.reduce((sum, d) => sum + d.totalCost, 0) / window;
      result.push({
        ...data[i],
        movingAverage: avg,
        originalCost: data[i].totalCost
      });
    }
  }
  return result;
};

// Exponential smoothing for short-term predictions
export const exponentialSmoothing = (data, alpha = 0.3) => {
  if (!data || data.length === 0) return [];

  const result = [{ ...data[0], smoothed: data[0].totalCost }];
  
  for (let i = 1; i < data.length; i++) {
    const smoothed = alpha * data[i].totalCost + (1 - alpha) * result[i - 1].smoothed;
    result.push({
      ...data[i],
      smoothed,
      originalCost: data[i].totalCost
    });
  }
  
  return result;
};

// Seasonal decomposition for educational scenarios
export const seasonalDecomposition = (data, seasonLength = 30) => {
  if (!data || data.length < seasonLength * 2) {
    return { trend: [], seasonal: [], residual: [] };
  }

  // Calculate trend using moving average
  const trend = movingAverage(data, seasonLength);

  // Calculate seasonal component
  const detrended = data.map((point, i) => ({
    ...point,
    detrended: point.totalCost - (trend[i]?.movingAverage || point.totalCost)
  }));

  // Average seasonal pattern
  const seasonalPattern = [];
  for (let i = 0; i < seasonLength; i++) {
    const seasonalPoints = [];
    for (let j = i; j < detrended.length; j += seasonLength) {
      seasonalPoints.push(detrended[j].detrended);
    }
    const avgSeasonal = seasonalPoints.reduce((sum, val) => sum + val, 0) / seasonalPoints.length;
    seasonalPattern.push(avgSeasonal);
  }

  // Apply seasonal pattern to all data points
  const seasonal = data.map((point, i) => ({
    ...point,
    seasonal: seasonalPattern[i % seasonLength]
  }));

  // Calculate residual
  const residual = data.map((point, i) => ({
    ...point,
    residual: point.totalCost - (trend[i]?.movingAverage || point.totalCost) - seasonal[i].seasonal
  }));

  return { trend, seasonal, residual };
};

// Basic cost prediction for educational scenarios
export const predictCosts = (historicalData, daysToPredict = 30, options = {}) => {
  const {
    method = 'linear',
    includeSeasonality = true,
    confidenceLevel = 0.95,
    growthRate = null
  } = options;

  if (!historicalData || historicalData.length < 7) {
    return { predictions: [], confidence: 0, method: 'insufficient_data' };
  }

  // Sort data by date
  const sortedData = [...historicalData].sort((a, b) => 
    new Date(a.date || a.timestamp) - new Date(b.date || b.timestamp)
  );

  let predictions = [];
  let confidence = 0;

  switch (method) {
    case 'linear': {
      const regression = linearRegression(sortedData);
      const lastIndex = sortedData.length - 1;
      
      for (let i = 1; i <= daysToPredict; i++) {
        const predictedCost = regression.slope * (lastIndex + i) + regression.intercept;
        
        // Safe date handling with fallback
        let predictionDate;
        const baseDate = sortedData[lastIndex]?.date || sortedData[lastIndex]?.timestamp;
        if (baseDate) {
          const date = new Date(baseDate);
          if (!isNaN(date.getTime())) {
            date.setDate(date.getDate() + i);
            predictionDate = date.toISOString().split('T')[0];
          } else {
            const fallbackDate = new Date();
            fallbackDate.setDate(fallbackDate.getDate() + i);
            predictionDate = fallbackDate.toISOString().split('T')[0];
          }
        } else {
          const fallbackDate = new Date();
          fallbackDate.setDate(fallbackDate.getDate() + i);
          predictionDate = fallbackDate.toISOString().split('T')[0];
        }
        
        predictions.push({
          date: predictionDate,
          predictedCost: Math.max(0, predictedCost),
          lowerBound: Math.max(0, predictedCost * 0.9),
          upperBound: predictedCost * 1.1,
          confidence: regression.r2
        });
      }
      confidence = regression.r2;
      break;
    }

    case 'exponential': {
      const smoothed = exponentialSmoothing(sortedData, 0.3);
      const lastSmoothed = smoothed[smoothed.length - 1].smoothed;
      const trend = (smoothed[smoothed.length - 1].smoothed - smoothed[0].smoothed) / smoothed.length;
      
      for (let i = 1; i <= daysToPredict; i++) {
        const predictedCost = lastSmoothed + (trend * i);
        
        // Safe date handling with fallback
        let predictionDate;
        const baseDate = sortedData[sortedData.length - 1]?.date || sortedData[sortedData.length - 1]?.timestamp;
        if (baseDate) {
          const date = new Date(baseDate);
          if (!isNaN(date.getTime())) {
            date.setDate(date.getDate() + i);
            predictionDate = date.toISOString().split('T')[0];
          } else {
            const fallbackDate = new Date();
            fallbackDate.setDate(fallbackDate.getDate() + i);
            predictionDate = fallbackDate.toISOString().split('T')[0];
          }
        } else {
          const fallbackDate = new Date();
          fallbackDate.setDate(fallbackDate.getDate() + i);
          predictionDate = fallbackDate.toISOString().split('T')[0];
        }
        
        predictions.push({
          date: predictionDate,
          predictedCost: Math.max(0, predictedCost),
          lowerBound: Math.max(0, predictedCost * 0.85),
          upperBound: predictedCost * 1.15,
          confidence: 0.8
        });
      }
      confidence = 0.8;
      break;
    }

    case 'seasonal': {
      const { trend, seasonal } = seasonalDecomposition(sortedData);
      const regression = linearRegression(trend.filter(t => t.movingAverage));
      const lastIndex = sortedData.length - 1;
      
      for (let i = 1; i <= daysToPredict; i++) {
        const trendPrediction = regression.slope * (lastIndex + i) + regression.intercept;
        const seasonalComponent = seasonal[i % seasonal.length]?.seasonal || 0;
        const predictedCost = trendPrediction + (includeSeasonality ? seasonalComponent : 0);
        
        // Safe date handling with fallback
        let predictionDate;
        const baseDate = sortedData[lastIndex]?.date || sortedData[lastIndex]?.timestamp;
        if (baseDate) {
          const date = new Date(baseDate);
          if (!isNaN(date.getTime())) {
            date.setDate(date.getDate() + i);
            predictionDate = date.toISOString().split('T')[0];
          } else {
            const fallbackDate = new Date();
            fallbackDate.setDate(fallbackDate.getDate() + i);
            predictionDate = fallbackDate.toISOString().split('T')[0];
          }
        } else {
          const fallbackDate = new Date();
          fallbackDate.setDate(fallbackDate.getDate() + i);
          predictionDate = fallbackDate.toISOString().split('T')[0];
        }
        
        predictions.push({
          date: predictionDate,
          predictedCost: Math.max(0, predictedCost),
          lowerBound: Math.max(0, predictedCost * 0.8),
          upperBound: predictedCost * 1.2,
          confidence: regression.r2 * 0.9
        });
      }
      confidence = regression.r2 * 0.9;
      break;
    }

    case 'growth': {
      // Custom growth rate prediction for educational scenarios
      const avgCost = sortedData.reduce((sum, d) => sum + d.totalCost, 0) / sortedData.length;
      const actualGrowthRate = growthRate || 0.05; // 5% default growth
      
      for (let i = 1; i <= daysToPredict; i++) {
        const growthFactor = Math.pow(1 + actualGrowthRate / 365, i);
        const predictedCost = avgCost * growthFactor;
        
        // Safe date handling with fallback
        let predictionDate;
        const baseDate = sortedData[sortedData.length - 1]?.date || sortedData[sortedData.length - 1]?.timestamp;
        if (baseDate) {
          const date = new Date(baseDate);
          if (!isNaN(date.getTime())) {
            date.setDate(date.getDate() + i);
            predictionDate = date.toISOString().split('T')[0];
          } else {
            const fallbackDate = new Date();
            fallbackDate.setDate(fallbackDate.getDate() + i);
            predictionDate = fallbackDate.toISOString().split('T')[0];
          }
        } else {
          const fallbackDate = new Date();
          fallbackDate.setDate(fallbackDate.getDate() + i);
          predictionDate = fallbackDate.toISOString().split('T')[0];
        }
        
        predictions.push({
          date: predictionDate,
          predictedCost: Math.max(0, predictedCost),
          lowerBound: Math.max(0, predictedCost * 0.9),
          upperBound: predictedCost * 1.1,
          confidence: 0.75
        });
      }
      confidence = 0.75;
      break;
    }

    default:
      return { predictions: [], confidence: 0, method: 'unknown' };
  }

  return {
    predictions,
    confidence,
    method,
    historicalDataPoints: sortedData.length,
    lastActualCost: sortedData[sortedData.length - 1].totalCost,
    averageHistoricalCost: sortedData.reduce((sum, d) => sum + d.totalCost, 0) / sortedData.length
  };
};

// Anomaly detection using statistical methods
export const detectAnomalies = (data, threshold = 2) => {
  if (!data || data.length < 7) return [];

  const costs = data.map(d => d.totalCost);
  const mean = costs.reduce((sum, cost) => sum + cost, 0) / costs.length;
  const variance = costs.reduce((sum, cost) => sum + Math.pow(cost - mean, 2), 0) / costs.length;
  const stdDev = Math.sqrt(variance);

  const anomalies = data.map((point, index) => {
    const zScore = Math.abs((point.totalCost - mean) / stdDev);
    const isAnomaly = zScore > threshold;
    
    return {
      ...point,
      isAnomaly,
      zScore,
      anomalyScore: zScore / threshold,
      expectedRange: {
        lower: mean - (threshold * stdDev),
        upper: mean + (threshold * stdDev)
      }
    };
  }).filter(point => point.isAnomaly);

  return anomalies;
};

// Generate prediction scenarios for educational use
export const generatePredictionScenarios = (baseData, scenarios = ['optimistic', 'realistic', 'pessimistic']) => {
  const scenarioResults = {};

  scenarios.forEach(scenario => {
    let options = {};
    
    switch (scenario) {
      case 'optimistic':
        options = { method: 'growth', growthRate: -0.1 }; // 10% reduction
        break;
      case 'realistic':
        options = { method: 'seasonal', includeSeasonality: true };
        break;
      case 'pessimistic':
        options = { method: 'growth', growthRate: 0.2 }; // 20% growth
        break;
      default:
        options = { method: 'linear' };
    }

    const prediction = predictCosts(baseData, 30, options);
    scenarioResults[scenario] = {
      ...prediction,
      scenario,
      totalPredictedCost: prediction.predictions.reduce((sum, p) => sum + p.predictedCost, 0),
      averageDailyCost: prediction.predictions.reduce((sum, p) => sum + p.predictedCost, 0) / prediction.predictions.length
    };
  });

  return scenarioResults;
};

// Utility function to format predictions for visualization
export const formatPredictionsForChart = (predictions, includeConfidenceBands = true) => {
  const labels = predictions.predictions.map(p => p.date);
  const datasets = [
    {
      label: 'Predicted Cost',
      data: predictions.predictions.map(p => p.predictedCost),
      borderColor: 'rgb(75, 192, 192)',
      backgroundColor: 'rgba(75, 192, 192, 0.2)',
      tension: 0.4,
      borderWidth: 2
    }
  ];

  if (includeConfidenceBands) {
    datasets.push({
      label: 'Upper Bound',
      data: predictions.predictions.map(p => p.upperBound),
      borderColor: 'rgba(255, 99, 132, 0.5)',
      backgroundColor: 'rgba(255, 99, 132, 0.1)',
      borderDash: [5, 5],
      borderWidth: 1,
      fill: false
    });

    datasets.push({
      label: 'Lower Bound',
      data: predictions.predictions.map(p => p.lowerBound),
      borderColor: 'rgba(54, 162, 235, 0.5)',
      backgroundColor: 'rgba(54, 162, 235, 0.1)',
      borderDash: [5, 5],
      borderWidth: 1,
      fill: false
    });
  }

  return { labels, datasets };
};

export default {
  linearRegression,
  movingAverage,
  exponentialSmoothing,
  seasonalDecomposition,
  predictCosts,
  detectAnomalies,
  generatePredictionScenarios,
  formatPredictionsForChart
};