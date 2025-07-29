import React, { useState, useEffect } from 'react';
import { Line } from 'react-chartjs-2';
import { colors, getCardStyle } from '../../styles/colors';
import { textStyles } from '../../styles/typography';
import { formatPredictionsForChart } from '../../utils/predictiveModeling';

const PredictiveCostChart = ({ 
  historicalData = [], 
  predictions = null,
  title = "Cost Prediction Analysis",
  height = 300,
  isMobile = false 
}) => {
  const [chartData, setChartData] = useState(null);
  const [selectedView, setSelectedView] = useState('combined'); // combined, historical, predicted

  useEffect(() => {
    if (historicalData.length > 0) {
      generateChartData();
    }
  }, [historicalData, predictions, selectedView]);

  const generateChartData = () => {
    // Sort and prepare historical data
    const sortedHistorical = [...historicalData]
      .sort((a, b) => new Date(a.date || a.timestamp) - new Date(b.date || b.timestamp))
      .slice(-30); // Last 30 days for clarity

    const historicalLabels = sortedHistorical.map(d => {
      const date = new Date(d.date || d.timestamp);
      return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    });
    
    const historicalCosts = sortedHistorical.map(d => d.totalCost);

    // Prepare datasets based on view
    const datasets = [];

    if (selectedView === 'historical' || selectedView === 'combined') {
      datasets.push({
        label: 'Historical Cost',
        data: historicalCosts,
        borderColor: colors.primary[500],
        backgroundColor: `${colors.primary[500]}20`,
        tension: 0.4,
        borderWidth: 2,
        pointRadius: 3,
        pointHoverRadius: 5
      });
    }

    if ((selectedView === 'predicted' || selectedView === 'combined') && predictions) {
      const predictionData = formatPredictionsForChart(predictions, true);
      
      if (selectedView === 'combined') {
        // Extend historical data with predictions
        const combinedLabels = [...historicalLabels, ...predictionData.labels.map(date => 
          new Date(date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
        )];
        
        // Add null values to historical data for prediction range
        const extendedHistorical = [...historicalCosts, ...new Array(predictionData.labels.length).fill(null)];
        
        // Add null values to prediction data for historical range
        const extendedPredictions = [...new Array(historicalCosts.length).fill(null), ...predictionData.datasets[0].data];
        const extendedUpper = predictionData.datasets[1] ? 
          [...new Array(historicalCosts.length).fill(null), ...predictionData.datasets[1].data] : [];
        const extendedLower = predictionData.datasets[2] ? 
          [...new Array(historicalCosts.length).fill(null), ...predictionData.datasets[2].data] : [];

        datasets[0].data = extendedHistorical;
        
        datasets.push({
          label: 'Predicted Cost',
          data: extendedPredictions,
          borderColor: colors.success,
          backgroundColor: `${colors.success}20`,
          borderDash: [5, 5],
          tension: 0.4,
          borderWidth: 2,
          pointRadius: 0
        });

        if (extendedUpper.length > 0) {
          datasets.push({
            label: 'Confidence Upper',
            data: extendedUpper,
            borderColor: `${colors.error}50`,
            backgroundColor: `${colors.error}10`,
            borderDash: [2, 2],
            borderWidth: 1,
            pointRadius: 0,
            fill: '+1'
          });
        }

        if (extendedLower.length > 0) {
          datasets.push({
            label: 'Confidence Lower',
            data: extendedLower,
            borderColor: `${colors.info}50`,
            backgroundColor: `${colors.info}10`,
            borderDash: [2, 2],
            borderWidth: 1,
            pointRadius: 0,
            fill: false
          });
        }

        setChartData({
          labels: combinedLabels,
          datasets
        });
      } else {
        // Predicted only
        setChartData(predictionData);
      }
    } else if (selectedView === 'historical') {
      setChartData({
        labels: historicalLabels,
        datasets
      });
    }
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
        display: !isMobile,
        position: 'top',
        labels: {
          usePointStyle: true,
          padding: 15,
          font: {
            size: 12
          }
        }
      },
      tooltip: {
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        padding: 12,
        cornerRadius: 8,
        titleFont: {
          size: 14,
          weight: 'bold'
        },
        bodyFont: {
          size: 13
        },
        callbacks: {
          label: (context) => {
            const label = context.dataset.label || '';
            const value = context.parsed.y;
            if (value === null) return null;
            return `${label}: $${value.toLocaleString(undefined, {
              minimumFractionDigits: 0,
              maximumFractionDigits: 0
            })}`;
          }
        }
      }
    },
    scales: {
      x: {
        grid: {
          display: false
        },
        ticks: {
          maxRotation: 45,
          minRotation: 0,
          autoSkip: true,
          maxTicksLimit: isMobile ? 6 : 12,
          font: {
            size: isMobile ? 10 : 12
          }
        }
      },
      y: {
        beginAtZero: true,
        grid: {
          color: colors.gray[200],
          drawBorder: false
        },
        ticks: {
          callback: (value) => `$${(value / 1000).toFixed(0)}k`,
          font: {
            size: isMobile ? 10 : 12
          }
        }
      }
    }
  };

  const viewOptions = [
    { value: 'combined', label: 'Combined View', icon: '📊' },
    { value: 'historical', label: 'Historical Only', icon: '📈' },
    { value: 'predicted', label: 'Predictions Only', icon: '🔮' }
  ];

  if (!chartData) {
    return (
      <div style={{
        ...getCardStyle(),
        padding: '2rem',
        textAlign: 'center',
        height: height + 100
      }}>
        <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📊</div>
        <p style={{ ...textStyles.body(colors.text.secondary) }}>
          No data available for prediction analysis
        </p>
      </div>
    );
  }

  return (
    <div style={{ ...getCardStyle(), padding: isMobile ? '1rem' : '1.5rem' }}>
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        marginBottom: '1rem',
        flexWrap: 'wrap',
        gap: '1rem'
      }}>
        <h3 style={{ ...textStyles.cardTitle(colors.text.primary), margin: 0 }}>
          {title}
        </h3>
        
        {predictions && (
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            {viewOptions.map(option => (
              <button
                key={option.value}
                onClick={() => setSelectedView(option.value)}
                style={{
                  padding: '0.5rem 1rem',
                  backgroundColor: selectedView === option.value ? colors.primary[500] : 'transparent',
                  color: selectedView === option.value ? colors.white : colors.text.secondary,
                  border: `1px solid ${selectedView === option.value ? colors.primary[500] : colors.gray[300]}`,
                  borderRadius: '6px',
                  fontSize: '0.8rem',
                  cursor: 'pointer',
                  transition: 'all 0.2s ease',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.25rem'
                }}
              >
                <span style={{ fontSize: '1em' }}>{option.icon}</span>
                {!isMobile && option.label}
              </button>
            ))}
          </div>
        )}
      </div>

      {predictions && (
        <div style={{
          padding: '1rem',
          backgroundColor: colors.background.secondary,
          borderRadius: '6px',
          marginBottom: '1rem',
          display: 'flex',
          flexWrap: 'wrap',
          gap: '2rem',
          justifyContent: 'space-around'
        }}>
          <div style={{ textAlign: 'center' }}>
            <p style={{ ...textStyles.caption(colors.text.secondary), margin: '0 0 0.25rem 0' }}>
              Prediction Method
            </p>
            <p style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', margin: 0 }}>
              {predictions.method?.charAt(0).toUpperCase() + predictions.method?.slice(1)}
            </p>
          </div>
          <div style={{ textAlign: 'center' }}>
            <p style={{ ...textStyles.caption(colors.text.secondary), margin: '0 0 0.25rem 0' }}>
              Confidence Level
            </p>
            <p style={{ 
              ...textStyles.body(
                predictions.confidence > 0.8 ? colors.success : 
                predictions.confidence > 0.6 ? colors.warning : colors.error
              ), 
              fontWeight: '600', 
              margin: 0 
            }}>
              {(predictions.confidence * 100).toFixed(1)}%
            </p>
          </div>
          <div style={{ textAlign: 'center' }}>
            <p style={{ ...textStyles.caption(colors.text.secondary), margin: '0 0 0.25rem 0' }}>
              30-Day Forecast
            </p>
            <p style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', margin: 0 }}>
              ${predictions.predictions?.reduce((sum, p) => sum + p.predictedCost, 0).toLocaleString(undefined, {
                minimumFractionDigits: 0,
                maximumFractionDigits: 0
              })}
            </p>
          </div>
        </div>
      )}

      <div style={{ height: `${height}px`, position: 'relative' }}>
        <Line data={chartData} options={chartOptions} />
      </div>

      {predictions && predictions.confidence < 0.6 && (
        <div style={{
          marginTop: '1rem',
          padding: '0.75rem',
          backgroundColor: `${colors.warning}20`,
          border: `1px solid ${colors.warning}`,
          borderRadius: '6px'
        }}>
          <p style={{ ...textStyles.caption(colors.text.primary), margin: 0 }}>
            ⚠️ Low confidence prediction. Consider gathering more historical data for better accuracy.
          </p>
        </div>
      )}
    </div>
  );
};

export default PredictiveCostChart;