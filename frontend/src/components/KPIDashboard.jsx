import React from "react";
import KPICard from "./KPICard";
import {
  calculateCurrentPeriodMetrics,
  calculateMonthOverMonthChange,
  getTopCostDriver,
  calculateForecast,
  generateBudgetComparison,
  formatCurrency,
  formatPercentChange
} from "../utils/kpiCalculations";

const KPIDashboard = ({ 
  costData, 
  aggregatedTrendData, 
  granularity, 
  startDate, 
  endDate,
  selectedTeam,
  isMobile = false 
}) => {
  // Calculate all KPI metrics
  const currentMetrics = calculateCurrentPeriodMetrics(aggregatedTrendData, granularity);
  const periodChange = calculateMonthOverMonthChange(aggregatedTrendData, granularity);
  const topDriver = getTopCostDriver(costData);
  const forecast = calculateForecast(aggregatedTrendData, granularity);
  const budget = generateBudgetComparison(currentMetrics.currentSpend, startDate, endDate);

  // Determine period label based on granularity
  const getPeriodLabel = () => {
    switch (granularity) {
      case "daily": return "Daily Average";
      case "weekly": return "Weekly Total";  
      case "monthly": return "Monthly Total";
      default: return "Period Total";
    }
  };

  const getChangeLabel = () => {
    switch (granularity) {
      case "daily": return "vs yesterday";
      case "weekly": return "vs last week";
      case "monthly": return "vs last month";
      default: return "vs previous";
    }
  };

  const getForecastLabel = () => {
    switch (granularity) {
      case "daily": return "tomorrow";
      case "weekly": return "next week";
      case "monthly": return "next month";
      default: return "next period";
    }
  };

  // KPI cards configuration
  const kpiCards = [
    {
      title: "Current Spend",
      value: formatCurrency(currentMetrics.currentSpend),
      subtitle: `${getPeriodLabel()} for ${selectedTeam} team`,
      icon: "💰",
      trend: periodChange.trend,
      trendValue: formatPercentChange(periodChange.changePercent),
      trendLabel: getChangeLabel(),
      status: periodChange.trend === "up" ? "warning" : periodChange.trend === "down" ? "good" : "neutral",
      isClickable: true
    },
    {
      title: "Daily Average",
      value: formatCurrency(currentMetrics.averageDaily),
      subtitle: `Across ${currentMetrics.totalDataPoints} ${granularity} periods`,
      icon: "📊",
      trend: currentMetrics.averageDaily > 100 ? "up" : currentMetrics.averageDaily < 50 ? "down" : "neutral",
      trendValue: `${currentMetrics.totalDataPoints} periods`,
      trendLabel: "data points",
      status: "info"
    },
    {
      title: "Top Cost Driver",
      value: topDriver.service,
      subtitle: `${formatCurrency(topDriver.cost)} (${topDriver.percentage}% of total)`,
      icon: "🎯",
      trend: topDriver.percentage > 40 ? "up" : "neutral",
      trendValue: `${topDriver.percentage}%`,
      trendLabel: "of total spend",
      status: topDriver.percentage > 50 ? "warning" : "neutral",
      isClickable: true
    },
    {
      title: "Budget Status",
      value: `${budget.percentUsed}%`,
      subtitle: `${formatCurrency(budget.remaining)} remaining of ${formatCurrency(budget.budget)}`,
      icon: "🎯",  
      trend: budget.percentUsed > 90 ? "up" : budget.percentUsed < 60 ? "down" : "neutral",
      trendValue: formatCurrency(budget.remaining),
      trendLabel: "remaining",
      status: budget.status === "over-budget" ? "critical" : 
              budget.status === "at-risk" ? "warning" : 
              budget.status === "under-budget" ? "good" : "neutral",
      isClickable: true
    },
    {
      title: `Forecast (${getForecastLabel()})`,
      value: formatCurrency(forecast.forecastAmount),
      subtitle: `${forecast.confidence} confidence based on trends`,
      icon: "🔮",
      trend: forecast.trend,
      trendValue: forecast.trendValue > 0 ? `+${formatCurrency(forecast.trendValue)}` : formatCurrency(forecast.trendValue),
      trendLabel: "trend",
      status: forecast.trend === "up" ? "warning" : forecast.trend === "down" ? "good" : "neutral",
      isClickable: true
    },
    {
      title: "Cost Efficiency",
      value: currentMetrics.currentSpend > 3000 ? "High Usage" : 
             currentMetrics.currentSpend > 1500 ? "Medium Usage" : "Low Usage",
      subtitle: `${formatCurrency(currentMetrics.currentSpend / Math.max(costData.length, 1))} per service`,
      icon: "⚡",
      trend: "neutral",
      trendValue: `${costData.length}`,
      trendLabel: "services",
      status: "info"
    }
  ];

  // Handle card clicks
  const handleCardClick = (cardTitle) => {
    console.log(`KPI Card clicked: ${cardTitle}`);
    // In a real app, this would navigate to detailed views or open modals
  };

  return (
    <div style={{ marginBottom: "2rem" }}>
      {/* Header */}
      <div style={{ marginBottom: "1.5rem" }}>
        <h3
          style={{
            fontSize: isMobile ? "1.25rem" : "1.5rem",
            fontWeight: 600,
            color: "#212121",
            margin: 0,
            marginBottom: "0.5rem"
          }}
        >
          📈 Cost Overview
        </h3>
        <p
          style={{
            fontSize: "0.875rem",
            color: "#757575",
            margin: 0
          }}
        >
          Key metrics for {selectedTeam} team • {startDate} to {endDate} • {granularity} view
        </p>
      </div>

      {/* KPI Cards Grid */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: isMobile 
            ? "1fr" 
            : "repeat(auto-fit, minmax(280px, 1fr))",
          gap: isMobile ? "1rem" : "1.5rem",
        }}
      >
        {kpiCards.map((card, index) => (
          <KPICard
            key={index}
            title={card.title}
            value={card.value}
            subtitle={card.subtitle}
            icon={card.icon}
            trend={card.trend}
            trendValue={card.trendValue}
            trendLabel={card.trendLabel}
            status={card.status}
            isClickable={card.isClickable}
            onClick={() => handleCardClick(card.title)}
            isMobile={isMobile}
          />
        ))}
      </div>

      {/* Summary Footer */}
      <div
        style={{
          marginTop: "1.5rem",
          padding: "1rem",
          backgroundColor: "#f8f9fa",
          borderRadius: "8px",
          border: "1px solid #e9ecef",
          fontSize: "0.75rem",
          color: "#6c757d",
          textAlign: "center"
        }}
      >
        💡 <strong>Tip:</strong> Click on cards to drill down into detailed analysis. 
        Budget values are simulated based on current spending patterns.
      </div>
    </div>
  );
};

export default KPIDashboard;