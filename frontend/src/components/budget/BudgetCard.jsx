import React from 'react';
import { colors, getCardStyle } from '../../styles/colors';
import { textStyles } from '../../styles/typography';

const BudgetCard = ({ 
  budget, 
  currentSpend = 0, 
  forecastedSpend = 0,
  daysRemaining = 0,
  isMobile = false,
  onEdit,
  onDelete 
}) => {
  const spendPercentage = budget.amount > 0 ? (currentSpend / budget.amount) * 100 : 0;
  const forecastPercentage = budget.amount > 0 ? (forecastedSpend / budget.amount) * 100 : 0;
  
  // Determine status color based on spend percentage
  const getStatusColor = () => {
    if (spendPercentage >= 100) return colors.error;
    if (spendPercentage >= 80) return colors.warning;
    if (spendPercentage >= 60) return '#ff6f00'; // Orange
    return colors.success;
  };

  const getStatusIcon = () => {
    if (spendPercentage >= 100) return '🚨';
    if (spendPercentage >= 80) return '⚠️';
    if (spendPercentage >= 60) return '⚡';
    return '✅';
  };

  const getStatusText = () => {
    if (spendPercentage >= 100) return 'OVER BUDGET';
    if (spendPercentage >= 80) return 'HIGH USAGE';
    if (spendPercentage >= 60) return 'MODERATE USAGE';
    return 'ON TRACK';
  };

  const statusColor = getStatusColor();
  const statusIcon = getStatusIcon();
  const statusText = getStatusText();

  const cardStyle = {
    ...getCardStyle(),
    padding: isMobile ? '1rem' : '1.5rem',
    marginBottom: '1rem',
    position: 'relative',
    borderLeft: `4px solid ${statusColor}`,
    transition: 'all 0.2s ease',
    cursor: 'pointer',
  };

  const progressBarStyle = {
    width: '100%',
    height: '8px',
    backgroundColor: colors.gray[200],
    borderRadius: '4px',
    overflow: 'hidden',
    marginBottom: '0.5rem',
  };

  const progressFillStyle = {
    height: '100%',
    backgroundColor: statusColor,
    width: `${Math.min(spendPercentage, 100)}%`,
    borderRadius: '4px',
    transition: 'width 0.3s ease',
  };

  const forecastBarStyle = {
    height: '100%',
    backgroundColor: `${statusColor}40`,
    width: `${Math.min(forecastPercentage, 100)}%`,
    borderRadius: '4px',
    position: 'absolute',
    top: 0,
    left: 0,
  };

  return (
    <div 
      style={cardStyle}
      onMouseEnter={(e) => {
        e.currentTarget.style.transform = 'translateY(-2px)';
        e.currentTarget.style.boxShadow = '0 8px 25px rgba(0, 0, 0, 0.15)';
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.transform = 'translateY(0)';
        e.currentTarget.style.boxShadow = '0 2px 10px rgba(0, 0, 0, 0.1)';
      }}
    >
      {/* Header */}
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'flex-start',
        marginBottom: '1rem'
      }}>
        <div>
          <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: '0.25rem' }}>
            {budget.name}
          </h3>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span style={{ ...textStyles.caption(colors.text.secondary) }}>
              {budget.scope === 'team' && `Team: ${budget.target}`}
              {budget.scope === 'service' && `Service: ${budget.target}`}
              {budget.scope === 'total' && 'Total Organization'}
            </span>
            <span style={{ ...textStyles.caption(statusColor), fontWeight: '600' }}>
              {statusIcon} {statusText}
            </span>
          </div>
        </div>

        {/* Action Buttons */}
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button
            onClick={(e) => {
              e.stopPropagation();
              onEdit?.(budget);
            }}
            style={{
              padding: '0.5rem',
              backgroundColor: 'transparent',
              border: `1px solid ${colors.primary[300]}`,
              borderRadius: '4px',
              cursor: 'pointer',
              color: colors.primary[600],
              fontSize: '0.875rem'
            }}
            title="Edit Budget"
          >
            ✏️
          </button>
          <button
            onClick={(e) => {
              e.stopPropagation();
              if (window.confirm(`Are you sure you want to delete "${budget.name}"?`)) {
                onDelete?.(budget.id);
              }
            }}
            style={{
              padding: '0.5rem',
              backgroundColor: 'transparent',
              border: `1px solid ${colors.error}40`,
              borderRadius: '4px',
              cursor: 'pointer',
              color: colors.error,
              fontSize: '0.875rem'
            }}
            title="Delete Budget"
          >
            🗑️
          </button>
        </div>
      </div>

      {/* Budget Amount and Period */}
      <div style={{ 
        display: 'grid', 
        gridTemplateColumns: isMobile ? '1fr' : '1fr 1fr',
        gap: '1rem',
        marginBottom: '1rem'
      }}>
        <div>
          <p style={{ ...textStyles.caption(colors.text.secondary), margin: 0 }}>
            Budget Amount
          </p>
          <p style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', margin: 0 }}>
            ${budget.amount.toLocaleString()}
          </p>
        </div>
        <div>
          <p style={{ ...textStyles.caption(colors.text.secondary), margin: 0 }}>
            Period
          </p>
          <p style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', margin: 0 }}>
            {budget.period} • {daysRemaining} days left
          </p>
        </div>
      </div>

      {/* Progress Bar */}
      <div style={{ marginBottom: '1rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
          <span style={{ ...textStyles.caption(colors.text.secondary) }}>
            Current Spend
          </span>
          <span style={{ ...textStyles.caption(colors.text.primary), fontWeight: '600' }}>
            ${currentSpend.toLocaleString()} ({spendPercentage.toFixed(1)}%)
          </span>
        </div>
        
        <div style={{ ...progressBarStyle, position: 'relative' }}>
          <div style={forecastBarStyle} />
          <div style={progressFillStyle} />
        </div>

        <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '0.25rem' }}>
          <span style={{ ...textStyles.caption(colors.text.secondary) }}>
            Forecasted: ${forecastedSpend.toLocaleString()}
          </span>
          <span style={{ ...textStyles.caption(colors.text.secondary) }}>
            Remaining: ${Math.max(0, budget.amount - currentSpend).toLocaleString()}
          </span>
        </div>
      </div>

      {/* Alert Threshold */}
      {budget.alertThreshold && (
        <div style={{
          padding: '0.75rem',
          backgroundColor: colors.warning + '20',
          borderRadius: '4px',
          border: `1px solid ${colors.warning}40`,
        }}>
          <p style={{ ...textStyles.caption(colors.text.primary), margin: 0 }}>
            <strong>🔔 Alert at {budget.alertThreshold}%</strong>
            {spendPercentage >= budget.alertThreshold && (
              <span style={{ color: colors.warning, marginLeft: '0.5rem' }}>
                • THRESHOLD REACHED
              </span>
            )}
          </p>
        </div>
      )}
    </div>
  );
};

export default BudgetCard;