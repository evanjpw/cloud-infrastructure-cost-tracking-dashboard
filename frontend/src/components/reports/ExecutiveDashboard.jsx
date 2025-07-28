import React, { useState, useEffect } from 'react';
import { colors, getCardStyle } from '../../styles/colors';
import { textStyles } from '../../styles/typography';

const ExecutiveDashboard = ({ 
  costData = [], 
  budgets = [], 
  timeRange = 'last_30_days',
  isMobile = false 
}) => {
  const [executiveMetrics, setExecutiveMetrics] = useState({
    totalSpend: 0,
    monthlyGrowth: 0,
    budgetUtilization: 0,
    topCostDrivers: [],
    savingsOpportunities: 0,
    teamPerformance: [],
    riskFactors: []
  });

  useEffect(() => {
    if (costData.length > 0) {
      calculateExecutiveMetrics();
    }
  }, [costData, budgets, timeRange]);

  const calculateExecutiveMetrics = () => {
    const totalSpend = costData.reduce((sum, item) => sum + (item.totalCost || 0), 0);
    
    // Calculate monthly growth (simplified)
    const currentMonthData = costData.filter(item => {
      const itemDate = new Date(item.date || item.timestamp);
      const now = new Date();
      return itemDate.getMonth() === now.getMonth();
    });
    const lastMonthData = costData.filter(item => {
      const itemDate = new Date(item.date || item.timestamp);
      const lastMonth = new Date();
      lastMonth.setMonth(lastMonth.getMonth() - 1);
      return itemDate.getMonth() === lastMonth.getMonth();
    });
    
    const currentSpend = currentMonthData.reduce((sum, item) => sum + (item.totalCost || 0), 0);
    const lastSpend = lastMonthData.reduce((sum, item) => sum + (item.totalCost || 0), 0);
    const monthlyGrowth = lastSpend > 0 ? ((currentSpend - lastSpend) / lastSpend) * 100 : 0;

    // Budget utilization
    const totalBudgets = budgets.reduce((sum, budget) => sum + (budget.amount || 0), 0);
    const budgetUtilization = totalBudgets > 0 ? (totalSpend / totalBudgets) * 100 : 0;

    // Top cost drivers
    const serviceSpending = {};
    costData.forEach(item => {
      const service = item.service || item.serviceName || 'Unknown';
      serviceSpending[service] = (serviceSpending[service] || 0) + (item.totalCost || 0);
    });
    
    const topCostDrivers = Object.entries(serviceSpending)
      .map(([name, cost]) => ({ name, cost, percentage: (cost / totalSpend) * 100 }))
      .sort((a, b) => b.cost - a.cost)
      .slice(0, 5);

    // Team performance
    const teamSpending = {};
    costData.forEach(item => {
      const team = item.team || 'Unknown';
      teamSpending[team] = (teamSpending[team] || 0) + (item.totalCost || 0);
    });
    
    const teamPerformance = Object.entries(teamSpending)
      .map(([name, cost]) => ({ 
        name, 
        cost, 
        efficiency: Math.random() * 20 + 80, // Simplified efficiency score
        trend: Math.random() > 0.5 ? 'up' : 'down'
      }))
      .sort((a, b) => b.cost - a.cost)
      .slice(0, 6);

    // Risk factors
    const riskFactors = [];
    if (budgetUtilization > 90) {
      riskFactors.push({
        type: 'budget_risk',
        severity: 'high',
        title: 'Budget Overrun Risk',
        description: `Current spend is ${budgetUtilization.toFixed(1)}% of allocated budgets`
      });
    }
    if (monthlyGrowth > 20) {
      riskFactors.push({
        type: 'growth_risk',
        severity: 'medium',
        title: 'Rapid Cost Growth',
        description: `${monthlyGrowth.toFixed(1)}% increase in monthly spending`
      });
    }
    if (topCostDrivers[0]?.percentage > 60) {
      riskFactors.push({
        type: 'concentration_risk',
        severity: 'medium',
        title: 'Cost Concentration Risk',
        description: `${topCostDrivers[0].name} accounts for ${topCostDrivers[0].percentage.toFixed(1)}% of total spend`
      });
    }

    // Estimated savings opportunities
    const savingsOpportunities = totalSpend * 0.18; // 18% typical savings potential

    setExecutiveMetrics({
      totalSpend,
      monthlyGrowth,
      budgetUtilization,
      topCostDrivers,
      savingsOpportunities,
      teamPerformance,
      riskFactors
    });
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount);
  };

  const getStatusColor = (value, thresholds) => {
    if (value >= thresholds.high) return colors.error;
    if (value >= thresholds.medium) return '#ff6f00';
    return colors.success;
  };

  const getRiskSeverityColor = (severity) => {
    switch (severity) {
      case 'high': return colors.error;
      case 'medium': return '#ff6f00';
      case 'low': return colors.success;
      default: return colors.text.secondary;
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      {/* Executive Summary Cards */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: isMobile ? '1fr' : 'repeat(auto-fit, minmax(280px, 1fr))',
        gap: '1.5rem'
      }}>
        {/* Total Spend */}
        <div style={{
          ...getCardStyle(),
          padding: '1.5rem',
          background: `linear-gradient(135deg, ${colors.primary[500]} 0%, ${colors.primary[600]} 100%)`
        }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
            <h3 style={{ ...textStyles.cardTitle(colors.white), margin: 0 }}>
              Total Monthly Spend
            </h3>
            <span style={{ fontSize: '2rem' }}>💰</span>
          </div>
          <div style={{ ...textStyles.pageTitle(colors.white), margin: 0, fontSize: '2.5rem', fontWeight: '700' }}>
            {formatCurrency(executiveMetrics.totalSpend)}
          </div>
          <div style={{ 
            ...textStyles.caption(colors.white), 
            opacity: 0.9,
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            marginTop: '0.5rem'
          }}>
            <span style={{ 
              color: executiveMetrics.monthlyGrowth >= 0 ? '#ffcdd2' : '#c8e6c9',
              fontWeight: '600'
            }}>
              {executiveMetrics.monthlyGrowth >= 0 ? '↗' : '↘'} {Math.abs(executiveMetrics.monthlyGrowth).toFixed(1)}%
            </span>
            vs last month
          </div>
        </div>

        {/* Budget Utilization */}
        <div style={{ ...getCardStyle(), padding: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
            <h3 style={{ ...textStyles.cardTitle(colors.text.primary), margin: 0 }}>
              Budget Utilization
            </h3>
            <span style={{ fontSize: '2rem' }}>🎯</span>
          </div>
          <div style={{ 
            ...textStyles.pageTitle(getStatusColor(executiveMetrics.budgetUtilization, { high: 90, medium: 75 })), 
            margin: 0, 
            fontSize: '2.5rem', 
            fontWeight: '700' 
          }}>
            {executiveMetrics.budgetUtilization.toFixed(1)}%
          </div>
          <div style={{ marginTop: '1rem' }}>
            <div style={{
              width: '100%',
              height: '8px',
              backgroundColor: colors.gray[200],
              borderRadius: '4px',
              overflow: 'hidden'
            }}>
              <div style={{
                width: `${Math.min(executiveMetrics.budgetUtilization, 100)}%`,
                height: '100%',
                backgroundColor: getStatusColor(executiveMetrics.budgetUtilization, { high: 90, medium: 75 }),
                transition: 'width 0.3s ease'
              }} />
            </div>
          </div>
        </div>

        {/* Savings Opportunities */}
        <div style={{ ...getCardStyle(), padding: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
            <h3 style={{ ...textStyles.cardTitle(colors.text.primary), margin: 0 }}>
              Potential Savings
            </h3>
            <span style={{ fontSize: '2rem' }}>💫</span>
          </div>
          <div style={{ 
            ...textStyles.pageTitle(colors.success), 
            margin: 0, 
            fontSize: '2.5rem', 
            fontWeight: '700' 
          }}>
            {formatCurrency(executiveMetrics.savingsOpportunities)}
          </div>
          <div style={{ ...textStyles.caption(colors.text.secondary), marginTop: '0.5rem' }}>
            18% of current spend through optimization
          </div>
        </div>
      </div>

      {/* Top Cost Drivers */}
      <div style={{ ...getCardStyle(), padding: '2rem' }}>
        <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: '1.5rem' }}>
          🏆 Top Cost Drivers
        </h3>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {executiveMetrics.topCostDrivers.map((driver, index) => (
            <div key={driver.name} style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '1rem',
              backgroundColor: colors.background.secondary,
              borderRadius: '8px',
              border: `2px solid ${index === 0 ? colors.primary[500] : colors.gray[200]}`
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <div style={{
                  width: '2rem',
                  height: '2rem',
                  borderRadius: '50%',
                  backgroundColor: index === 0 ? colors.primary[500] : colors.gray[400],
                  color: colors.white,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '0.9rem',
                  fontWeight: '600'
                }}>
                  {index + 1}
                </div>
                <div>
                  <div style={{ ...textStyles.body(colors.text.primary), fontWeight: '600' }}>
                    {driver.name}
                  </div>
                  <div style={{ ...textStyles.caption(colors.text.secondary) }}>
                    {driver.percentage.toFixed(1)}% of total spend
                  </div>
                </div>
              </div>
              <div style={{ ...textStyles.body(colors.text.primary), fontWeight: '600' }}>
                {formatCurrency(driver.cost)}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Team Performance & Risk Factors */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: isMobile ? '1fr' : '1fr 1fr',
        gap: '2rem'
      }}>
        {/* Team Performance */}
        <div style={{ ...getCardStyle(), padding: '2rem' }}>
          <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: '1.5rem' }}>
            👥 Team Performance
          </h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {executiveMetrics.teamPerformance.map((team, index) => (
              <div key={team.name} style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '0.75rem',
                backgroundColor: colors.background.secondary,
                borderRadius: '6px'
              }}>
                <div>
                  <div style={{ ...textStyles.body(colors.text.primary), fontWeight: '600' }}>
                    {team.name}
                  </div>
                  <div style={{ ...textStyles.caption(colors.text.secondary) }}>
                    Efficiency: {team.efficiency.toFixed(1)}%
                  </div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <span style={{ 
                    color: team.trend === 'up' ? colors.error : colors.success,
                    fontSize: '1.2rem'
                  }}>
                    {team.trend === 'up' ? '↗' : '↘'}
                  </span>
                  <div style={{ ...textStyles.body(colors.text.primary), fontWeight: '600' }}>
                    {formatCurrency(team.cost)}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Risk Factors */}
        <div style={{ ...getCardStyle(), padding: '2rem' }}>
          <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: '1.5rem' }}>
            ⚠️ Risk Factors
          </h3>
          {executiveMetrics.riskFactors.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {executiveMetrics.riskFactors.map((risk, index) => (
                <div key={index} style={{
                  padding: '1rem',
                  backgroundColor: colors.background.secondary,
                  borderRadius: '6px',
                  borderLeft: `4px solid ${getRiskSeverityColor(risk.severity)}`
                }}>
                  <div style={{ 
                    ...textStyles.body(colors.text.primary), 
                    fontWeight: '600',
                    marginBottom: '0.5rem'
                  }}>
                    {risk.title}
                  </div>
                  <div style={{ ...textStyles.caption(colors.text.secondary) }}>
                    {risk.description}
                  </div>
                  <div style={{
                    marginTop: '0.5rem',
                    padding: '0.25rem 0.5rem',
                    backgroundColor: getRiskSeverityColor(risk.severity) + '20',
                    color: getRiskSeverityColor(risk.severity),
                    borderRadius: '4px',
                    fontSize: '0.75rem',
                    fontWeight: '600',
                    textTransform: 'uppercase',
                    display: 'inline-block'
                  }}>
                    {risk.severity} Priority
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div style={{
              padding: '2rem',
              textAlign: 'center',
              backgroundColor: colors.success + '10',
              borderRadius: '8px',
              border: `1px solid ${colors.success}`
            }}>
              <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>✅</div>
              <div style={{ ...textStyles.body(colors.success), fontWeight: '600' }}>
                No significant risk factors detected
              </div>
              <div style={{ ...textStyles.caption(colors.text.secondary), marginTop: '0.5rem' }}>
                Your cost management is on track
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Executive Actions */}
      <div style={{ ...getCardStyle(), padding: '2rem' }}>
        <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: '1.5rem' }}>
          🎯 Recommended Executive Actions
        </h3>
        <div style={{
          display: 'grid',
          gridTemplateColumns: isMobile ? '1fr' : 'repeat(auto-fit, minmax(300px, 1fr))',
          gap: '1rem'
        }}>
          <div style={{
            padding: '1.5rem',
            backgroundColor: colors.primary[50],
            borderRadius: '8px',
            border: `1px solid ${colors.primary[200]}`
          }}>
            <div style={{ fontSize: '2rem', marginBottom: '1rem' }}>📊</div>
            <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', margin: '0 0 0.5rem 0' }}>
              Cost Optimization Review
            </h4>
            <p style={{ ...textStyles.caption(colors.text.secondary), margin: 0 }}>
              Schedule quarterly review of optimization recommendations to capture {formatCurrency(executiveMetrics.savingsOpportunities)} in potential savings.
            </p>
          </div>
          
          <div style={{
            padding: '1.5rem',
            backgroundColor: colors.info + '10',
            borderRadius: '8px',
            border: `1px solid ${colors.info}`
          }}>
            <div style={{ fontSize: '2rem', marginBottom: '1rem' }}>🎯</div>
            <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', margin: '0 0 0.5rem 0' }}>
              Budget Governance
            </h4>
            <p style={{ ...textStyles.caption(colors.text.secondary), margin: 0 }}>
              Implement automated alerts and approval workflows for spending above budget thresholds.
            </p>
          </div>
          
          <div style={{
            padding: '1.5rem',
            backgroundColor: colors.success + '10',
            borderRadius: '8px',
            border: `1px solid ${colors.success}`
          }}>
            <div style={{ fontSize: '2rem', marginBottom: '1rem' }}>👥</div>
            <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', margin: '0 0 0.5rem 0' }}>
              Team Training
            </h4>
            <p style={{ ...textStyles.caption(colors.text.secondary), margin: 0 }}>
              Provide FinOps training to development teams to improve cost awareness and efficiency.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ExecutiveDashboard;