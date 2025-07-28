// Utility functions for aggregating cost data by different granularities

// Helper to get week start date (Monday)
const getWeekStart = (date) => {
  const d = new Date(date);
  const day = d.getDay();
  const diff = d.getDate() - day + (day === 0 ? -6 : 1); // Adjust for Sunday
  return new Date(d.setDate(diff));
};

// Helper to get month start date
const getMonthStart = (date) => {
  const d = new Date(date);
  return new Date(d.getFullYear(), d.getMonth(), 1);
};

// Format date for display based on granularity
export const formatDateForGranularity = (date, granularity) => {
  const d = new Date(date);
  
  switch (granularity) {
    case "daily":
      return d.toLocaleDateString("en-US", { 
        month: "short", 
        day: "numeric",
        weekday: "short" 
      });
    case "weekly":
      const weekEnd = new Date(d);
      weekEnd.setDate(d.getDate() + 6);
      return `${d.toLocaleDateString("en-US", { month: "short", day: "numeric" })} - ${weekEnd.toLocaleDateString("en-US", { month: "short", day: "numeric" })}`;
    case "monthly":
      return d.toLocaleDateString("en-US", { 
        month: "long", 
        year: "numeric" 
      });
    default:
      return d.toLocaleDateString();
  }
};

// Generate date range based on granularity
export const generateDateRange = (startDate, endDate, granularity) => {
  const dates = [];
  const start = new Date(startDate);
  const end = new Date(endDate);
  
  let current = new Date(start);
  
  switch (granularity) {
    case "daily":
      while (current <= end) {
        dates.push(new Date(current));
        current.setDate(current.getDate() + 1);
      }
      break;
      
    case "weekly":
      // Start from the beginning of the week
      current = getWeekStart(start);
      while (current <= end) {
        dates.push(new Date(current));
        current.setDate(current.getDate() + 7);
      }
      break;
      
    case "monthly":
      // Start from the beginning of the month
      current = getMonthStart(start);
      while (current <= end) {
        dates.push(new Date(current));
        current.setMonth(current.getMonth() + 1);
      }
      break;
      
    default:
      // Default to daily
      while (current <= end) {
        dates.push(new Date(current));
        current.setDate(current.getDate() + 1);
      }
      break;
  }
  
  return dates;
};

// Aggregate cost data by granularity
export const aggregateDataByGranularity = (costData, granularity, startDate, endDate) => {
  if (!costData || costData.length === 0) {
    return generateMockDataForGranularity(granularity, startDate, endDate);
  }

  // Group data by time period
  const grouped = {};
  
  // Generate all time periods in range
  const dateRange = generateDateRange(startDate, endDate, granularity);
  
  // Initialize all periods with zero cost
  dateRange.forEach(date => {
    const key = getAggregationKey(date, granularity);
    if (!grouped[key]) {
      grouped[key] = {
        period: key,
        displayDate: formatDateForGranularity(date, granularity),
        totalCost: 0,
        services: {},
        date: date.toISOString().split('T')[0]
      };
    }
  });
  
  // Simulate aggregation from current service data
  // In a real app, this would aggregate actual time-series data
  costData.forEach(serviceData => {
    const costPerPeriod = (serviceData.totalCost || 0) / dateRange.length;
    
    dateRange.forEach(date => {
      const key = getAggregationKey(date, granularity);
      if (grouped[key]) {
        grouped[key].totalCost += costPerPeriod;
        grouped[key].services[serviceData.service] = (grouped[key].services[serviceData.service] || 0) + costPerPeriod;
      }
    });
  });
  
  // Convert to array and sort by date
  return Object.values(grouped)
    .sort((a, b) => new Date(a.date) - new Date(b.date));
};

// Get aggregation key for grouping
const getAggregationKey = (date, granularity) => {
  const d = new Date(date);
  
  switch (granularity) {
    case "daily":
      return d.toISOString().split('T')[0]; // YYYY-MM-DD
    case "weekly":
      const weekStart = getWeekStart(d);
      return `${weekStart.getFullYear()}-W${Math.ceil((weekStart.getDate()) / 7)}`;
    case "monthly":
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
    default:
      return d.toISOString().split('T')[0];
  }
};

// Generate realistic mock data for different granularities
export const generateMockDataForGranularity = (granularity, startDate, endDate) => {
  const dateRange = generateDateRange(startDate, endDate, granularity);
  
  // Base cost that varies by granularity
  const getBaseCost = () => {
    switch (granularity) {
      case "daily": return 85 + Math.random() * 30; // $85-115/day
      case "weekly": return 600 + Math.random() * 200; // $600-800/week  
      case "monthly": return 2800 + Math.random() * 800; // $2800-3600/month
      default: return 100;
    }
  };
  
  return dateRange.map((date, index) => {
    // Add some trend and seasonality
    const trendFactor = 1 + (index * 0.02); // 2% growth over time
    const seasonalFactor = 1 + Math.sin(index * 0.3) * 0.15; // ±15% seasonal variation
    const randomFactor = 0.85 + Math.random() * 0.3; // ±15% random variation
    
    const baseCost = getBaseCost();
    const totalCost = baseCost * trendFactor * seasonalFactor * randomFactor;
    
    return {
      date: date.toISOString().split('T')[0],
      displayDate: formatDateForGranularity(date, granularity),
      totalCost: Math.round(totalCost * 100) / 100,
      period: getAggregationKey(date, granularity),
      // Add some service breakdown
      services: {
        "Amazon EC2": totalCost * 0.35,
        "Amazon S3": totalCost * 0.15,
        "Amazon RDS": totalCost * 0.20,
        "Amazon Lambda": totalCost * 0.10,
        "Other Services": totalCost * 0.20
      }
    };
  });
};

// Calculate period-over-period changes
export const calculatePeriodChanges = (aggregatedData) => {
  return aggregatedData.map((current, index) => {
    if (index === 0) {
      return { ...current, change: null, changePercent: null };
    }
    
    const previous = aggregatedData[index - 1];
    const change = current.totalCost - previous.totalCost;
    const changePercent = previous.totalCost > 0 
      ? (change / previous.totalCost) * 100 
      : 0;
    
    return {
      ...current,
      change: Math.round(change * 100) / 100,
      changePercent: Math.round(changePercent * 10) / 10
    };
  });
};

// Get optimal granularity suggestion based on date range
export const suggestOptimalGranularity = (startDate, endDate) => {
  const start = new Date(startDate);
  const end = new Date(endDate);
  const daysDiff = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
  
  if (daysDiff <= 30) return "daily";
  if (daysDiff <= 180) return "weekly";
  return "monthly";
};