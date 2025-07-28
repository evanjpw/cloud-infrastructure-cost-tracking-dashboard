import React from 'react';
import { Line } from 'react-chartjs-2';
import { colors, getCardStyle } from '../../styles/colors';
import { textStyles } from '../../styles/typography';

const AnomalyChart = ({ 
  data = [], 
  title = "Cost Anomaly Detection",
  height = "350px",
  isMobile = false 
}) => {
  // Process data to detect anomalies
  const processedData = processAnomalyData(data);

  const chartData = {
    labels: processedData.labels,
    datasets: [
      {
        label: 'Daily Spend',
        data: processedData.costs,
        borderColor: colors.primary[500],
        backgroundColor: colors.primary[100],
        borderWidth: 2,
        fill: false,
        tension: 0.4,
        pointRadius: 4,
        pointHoverRadius: 6,
        pointBackgroundColor: colors.primary[500],
        pointBorderColor: colors.white,
        pointBorderWidth: 2,
      },
      {
        label: 'Average',
        data: processedData.average,
        borderColor: colors.success,
        backgroundColor: colors.success + '20',
        borderWidth: 2,
        borderDash: [5, 5],
        fill: false,
        pointRadius: 0,
        pointHoverRadius: 0,
      },
      {
        label: 'Upper Threshold',
        data: processedData.upperThreshold,
        borderColor: colors.warning,
        backgroundColor: colors.warning + '10',
        borderWidth: 2,
        borderDash: [3, 3],
        fill: '+1',
        pointRadius: 0,
        pointHoverRadius: 0,
      },
      {
        label: 'Anomalies',
        data: processedData.anomalies,
        borderColor: colors.error,
        backgroundColor: colors.error,
        borderWidth: 0,
        pointRadius: processedData.anomalies.map(point => point !== null ? 8 : 0),
        pointHoverRadius: processedData.anomalies.map(point => point !== null ? 10 : 0),
        pointBackgroundColor: colors.error,
        pointBorderColor: colors.white,
        pointBorderWidth: 2,
        showLine: false,
      }
    ]
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    interaction: {
      mode: 'index',
      intersect: false,
    },
    plugins: {
      legend: {
        position: isMobile ? 'bottom' : 'top',
        labels: {
          padding: 20,
          font: {
            family: "'Inter', -apple-system, BlinkMacSystemFont, sans-serif",
            size: 12,
          },
          color: colors.text.primary,
          usePointStyle: true,
          pointStyle: 'circle',
        },
      },
      tooltip: {
        backgroundColor: colors.white,
        titleColor: colors.text.primary,
        bodyColor: colors.text.secondary,
        borderColor: colors.gray[300],
        borderWidth: 1,
        padding: 12,
        cornerRadius: 8,
        displayColors: true,
        callbacks: {
          label: function(context) {
            const label = context.dataset.label || '';
            const value = context.parsed.y;
            
            if (label === 'Anomalies' && value !== null) {
              return `🚨 Anomaly: $${value.toLocaleString()}`;
            } else if (value !== null) {
              return `${label}: $${value.toLocaleString()}`;
            }
            return null;
          },
          afterBody: function(tooltipItems) {
            const anomalyItem = tooltipItems.find(item => item.dataset.label === 'Anomalies');
            if (anomalyItem && anomalyItem.parsed.y !== null) {
              const averageItem = tooltipItems.find(item => item.dataset.label === 'Average');
              if (averageItem) {
                const deviation = ((anomalyItem.parsed.y - averageItem.parsed.y) / averageItem.parsed.y * 100).toFixed(1);
                return [`Deviation: +${deviation}% above average`];
              }
            }
            return [];
          }
        }
      },
    },
    scales: {
      x: {
        display: true,
        title: {
          display: true,
          text: 'Date',
          color: colors.text.secondary,
          font: {
            family: "'Inter', -apple-system, BlinkMacSystemFont, sans-serif",
            size: 12,
            weight: '500',
          },
        },
        grid: {
          color: colors.gray[200],
          drawBorder: false,
        },
        ticks: {
          color: colors.text.secondary,
          font: {
            family: "'Inter', -apple-system, BlinkMacSystemFont, sans-serif",
            size: 11,
          },
          maxTicksLimit: isMobile ? 4 : 8,
        },
      },
      y: {
        display: true,
        title: {
          display: true,
          text: 'Cost ($)',
          color: colors.text.secondary,
          font: {
            family: "'Inter', -apple-system, BlinkMacSystemFont, sans-serif",
            size: 12,
            weight: '500',
          },
        },
        grid: {
          color: colors.gray[200],
          drawBorder: false,
        },
        ticks: {
          color: colors.text.secondary,
          font: {
            family: "'Inter', -apple-system, BlinkMacSystemFont, sans-serif",
            size: 11,
          },
          callback: function(value) {
            return '$' + value.toLocaleString();
          },
        },
      },
    },
  };

  const anomalyStats = calculateAnomalyStats(processedData);

  return (
    <div style={{
      ...getCardStyle(),
      padding: isMobile ? '1rem' : '1.5rem',
      marginBottom: '1.5rem'
    }}>
      {/* Header */}
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'flex-start',
        marginBottom: '1.5rem',
        flexWrap: 'wrap',
        gap: '1rem'
      }}>
        <div>
          <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: '0.5rem' }}>
            {title}
          </h3>
          <p style={{ ...textStyles.body(colors.text.secondary), margin: 0 }}>
            Automated detection of unusual spending patterns and cost spikes
          </p>
        </div>

        {/* Anomaly Summary */}
        <div style={{ 
          display: 'flex', 
          gap: '1rem',
          flexDirection: isMobile ? 'column' : 'row'
        }}>
          <div style={{
            textAlign: 'center',
            padding: '0.75rem',
            backgroundColor: anomalyStats.count > 0 ? colors.error + '10' : colors.success + '10',
            borderRadius: '6px',
            border: `1px solid ${anomalyStats.count > 0 ? colors.error : colors.success}40`,
            minWidth: '80px'
          }}>
            <div style={{ 
              fontSize: '1.5rem', 
              fontWeight: '600', 
              color: anomalyStats.count > 0 ? colors.error : colors.success 
            }}>
              {anomalyStats.count}
            </div>
            <div style={{ ...textStyles.caption(colors.text.secondary) }}>
              Anomalies
            </div>
          </div>

          {anomalyStats.maxDeviation > 0 && (
            <div style={{
              textAlign: 'center',
              padding: '0.75rem',
              backgroundColor: colors.warning + '10',
              borderRadius: '6px',
              border: `1px solid ${colors.warning}40`,
              minWidth: '80px'
            }}>
              <div style={{ 
                fontSize: '1.5rem', 
                fontWeight: '600', 
                color: colors.warning 
              }}>
                +{anomalyStats.maxDeviation}%
              </div>
              <div style={{ ...textStyles.caption(colors.text.secondary) }}>
                Max Spike
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Chart */}
      <div style={{ height, position: 'relative' }}>
        <Line data={chartData} options={chartOptions} />
      </div>

      {/* Insights */}
      {anomalyStats.count > 0 && (
        <div style={{
          marginTop: '1.5rem',
          padding: '1rem',
          backgroundColor: colors.primary[25],
          borderRadius: '6px',
          border: `1px solid ${colors.primary[200]}`
        }}>
          <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', marginBottom: '0.75rem' }}>
            🔍 Anomaly Insights
          </h4>
          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: isMobile ? '1fr' : 'repeat(auto-fit, minmax(200px, 1fr))',
            gap: '1rem'
          }}>
            <div>
              <p style={{ ...textStyles.caption(colors.text.secondary), margin: '0 0 0.25rem 0' }}>
                <strong>Detection Method:</strong>
              </p>
              <p style={{ ...textStyles.body(colors.text.primary), margin: 0 }}>
                Statistical threshold (2σ above mean)
              </p>
            </div>
            <div>
              <p style={{ ...textStyles.caption(colors.text.secondary), margin: '0 0 0.25rem 0' }}>
                <strong>Recommended Action:</strong>
              </p>
              <p style={{ ...textStyles.body(colors.text.primary), margin: 0 }}>
                Investigate services causing spikes
              </p>
            </div>
            <div>
              <p style={{ ...textStyles.caption(colors.text.secondary), margin: '0 0 0.25rem 0' }}>
                <strong>Impact:</strong>
              </p>
              <p style={{ ...textStyles.body(colors.text.primary), margin: 0 }}>
                ${anomalyStats.totalAnomalyCost.toLocaleString()} in unusual spend
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

// Helper functions
const processAnomalyData = (data) => {
  if (!data || data.length === 0) {
    return {
      labels: [],
      costs: [],
      average: [],
      upperThreshold: [],
      anomalies: []
    };
  }

  // Group data by date and sum costs
  const dailyCosts = {};
  data.forEach(item => {
    const date = item.date || item.timestamp || new Date().toISOString().split('T')[0];
    if (!dailyCosts[date]) {
      dailyCosts[date] = 0;
    }
    dailyCosts[date] += item.totalCost || 0;
  });

  // Sort by date and prepare arrays
  const sortedDates = Object.keys(dailyCosts).sort();
  const costs = sortedDates.map(date => dailyCosts[date]);

  // Calculate statistics
  const avgCost = costs.reduce((sum, cost) => sum + cost, 0) / costs.length;
  const variance = costs.reduce((sum, cost) => sum + Math.pow(cost - avgCost, 2), 0) / costs.length;
  const stdDev = Math.sqrt(variance);
  const upperThreshold = avgCost + (2 * stdDev); // 2 standard deviations

  // Detect anomalies
  const anomalies = costs.map(cost => cost > upperThreshold ? cost : null);
  const average = new Array(costs.length).fill(avgCost);
  const thresholdArray = new Array(costs.length).fill(upperThreshold);

  return {
    labels: sortedDates.map(date => new Date(date).toLocaleDateString()),
    costs,
    average,
    upperThreshold: thresholdArray,
    anomalies
  };
};

const calculateAnomalyStats = (processedData) => {
  const anomalies = processedData.anomalies.filter(a => a !== null);
  const avgCost = processedData.average[0] || 0;
  
  const maxDeviation = anomalies.length > 0 
    ? Math.max(...anomalies.map(anomaly => ((anomaly - avgCost) / avgCost * 100))) 
    : 0;

  const totalAnomalyCost = anomalies.reduce((sum, cost) => sum + cost, 0);

  return {
    count: anomalies.length,
    maxDeviation: Math.round(maxDeviation),
    totalAnomalyCost
  };
};

export default AnomalyChart;