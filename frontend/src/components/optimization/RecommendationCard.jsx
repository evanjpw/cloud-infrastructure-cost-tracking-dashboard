import React, { useState } from 'react';
import { colors, getCardStyle } from '../../styles/colors';
import { textStyles } from '../../styles/typography';

const RecommendationCard = ({ 
  recommendation, 
  onAccept, 
  onDismiss, 
  isMobile = false 
}) => {
  const [isExpanded, setIsExpanded] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);

  const getImpactColor = (impact) => {
    switch (impact.toLowerCase()) {
      case 'high': return colors.success;
      case 'medium': return colors.warning;
      case 'low': return colors.primary[500];
      default: return colors.text.secondary;
    }
  };

  const getTypeIcon = (type) => {
    switch (type.toLowerCase()) {
      case 'rightsizing': return '📏';
      case 'reserved_instance': return '💰';
      case 'unused_resource': return '🗑️';
      case 'anomaly': return '🚨';
      case 'optimization': return '⚡';
      default: return '💡';
    }
  };

  const getTypeLabel = (type) => {
    switch (type.toLowerCase()) {
      case 'rightsizing': return 'Right-sizing';
      case 'reserved_instance': return 'Reserved Instance';
      case 'unused_resource': return 'Unused Resource';
      case 'anomaly': return 'Cost Anomaly';
      case 'optimization': return 'Optimization';
      default: return 'Recommendation';
    }
  };

  const handleAccept = async () => {
    setIsProcessing(true);
    try {
      await new Promise(resolve => setTimeout(resolve, 1000)); // Simulate API call
      onAccept?.(recommendation.id);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleDismiss = async () => {
    setIsProcessing(true);
    try {
      await new Promise(resolve => setTimeout(resolve, 500)); // Simulate API call
      onDismiss?.(recommendation.id);
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <div style={{
      ...getCardStyle(),
      padding: isMobile ? '1rem' : '1.5rem',
      marginBottom: '1rem',
      borderLeft: `4px solid ${getImpactColor(recommendation.impact)}`,
      transition: 'all 0.2s ease',
      cursor: 'pointer',
    }}
    onMouseEnter={(e) => {
      e.currentTarget.style.transform = 'translateY(-2px)';
      e.currentTarget.style.boxShadow = '0 8px 25px rgba(0, 0, 0, 0.15)';
    }}
    onMouseLeave={(e) => {
      e.currentTarget.style.transform = 'translateY(0)';
      e.currentTarget.style.boxShadow = '0 2px 10px rgba(0, 0, 0, 0.1)';
    }}
    onClick={() => setIsExpanded(!isExpanded)}
    >
      {/* Header */}
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'flex-start',
        marginBottom: '1rem'
      }}>
        <div style={{ flex: 1 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '1.5rem' }}>{getTypeIcon(recommendation.type)}</span>
            <div>
              <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: '0.25rem' }}>
                {recommendation.title}
              </h3>
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
                <span style={{ 
                  ...textStyles.caption(getImpactColor(recommendation.impact)), 
                  fontWeight: '600',
                  backgroundColor: `${getImpactColor(recommendation.impact)}20`,
                  padding: '0.25rem 0.5rem',
                  borderRadius: '4px',
                  fontSize: '0.75rem',
                  textTransform: 'uppercase'
                }}>
                  {recommendation.impact} Impact
                </span>
                <span style={{ ...textStyles.caption(colors.text.secondary) }}>
                  {getTypeLabel(recommendation.type)}
                </span>
                {recommendation.service && (
                  <span style={{ ...textStyles.caption(colors.text.secondary) }}>
                    Service: {recommendation.service}
                  </span>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Potential Savings */}
        <div style={{ textAlign: 'right', minWidth: '120px' }}>
          <div style={{ ...textStyles.body(colors.success), fontWeight: '600', fontSize: '1.1rem' }}>
            ${recommendation.potentialSavings?.toLocaleString() || 0}
          </div>
          <div style={{ ...textStyles.caption(colors.text.secondary) }}>
            {recommendation.savingsPeriod || 'monthly'} savings
          </div>
        </div>
      </div>

      {/* Summary */}
      <p style={{ ...textStyles.body(colors.text.secondary), marginBottom: '1rem', lineHeight: '1.5' }}>
        {recommendation.description}
      </p>

      {/* Expandable Details */}
      {isExpanded && (
        <div style={{ 
          borderTop: `1px solid ${colors.gray[200]}`,
          paddingTop: '1rem',
          marginTop: '1rem'
        }}>
          {/* Current vs Recommended */}
          {recommendation.currentConfig && recommendation.recommendedConfig && (
            <div style={{ 
              display: 'grid', 
              gridTemplateColumns: isMobile ? '1fr' : '1fr 1fr',
              gap: '1rem',
              marginBottom: '1rem'
            }}>
              <div style={{
                padding: '1rem',
                backgroundColor: colors.error + '10',
                borderRadius: '6px',
                border: `1px solid ${colors.error}40`
              }}>
                <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', marginBottom: '0.5rem' }}>
                  Current Configuration
                </h4>
                <ul style={{ margin: 0, paddingLeft: '1rem', color: colors.text.secondary }}>
                  {Object.entries(recommendation.currentConfig).map(([key, value]) => (
                    <li key={key} style={{ marginBottom: '0.25rem' }}>
                      <strong>{key}:</strong> {value}
                    </li>
                  ))}
                </ul>
              </div>

              <div style={{
                padding: '1rem',
                backgroundColor: colors.success + '10',
                borderRadius: '6px',
                border: `1px solid ${colors.success}40`
              }}>
                <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', marginBottom: '0.5rem' }}>
                  Recommended Configuration
                </h4>
                <ul style={{ margin: 0, paddingLeft: '1rem', color: colors.text.secondary }}>
                  {Object.entries(recommendation.recommendedConfig).map(([key, value]) => (
                    <li key={key} style={{ marginBottom: '0.25rem' }}>
                      <strong>{key}:</strong> {value}
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          )}

          {/* Implementation Steps */}
          {recommendation.steps && (
            <div style={{ marginBottom: '1rem' }}>
              <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', marginBottom: '0.75rem' }}>
                Implementation Steps:
              </h4>
              <ol style={{ margin: 0, paddingLeft: '1.5rem', color: colors.text.secondary }}>
                {recommendation.steps.map((step, index) => (
                  <li key={index} style={{ marginBottom: '0.5rem' }}>
                    {step}
                  </li>
                ))}
              </ol>
            </div>
          )}

          {/* Risk Assessment */}
          {recommendation.risks && recommendation.risks.length > 0 && (
            <div style={{
              padding: '1rem',
              backgroundColor: colors.warning + '10',
              borderRadius: '6px',
              border: `1px solid ${colors.warning}40`,
              marginBottom: '1rem'
            }}>
              <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', marginBottom: '0.5rem' }}>
                ⚠️ Considerations:
              </h4>
              <ul style={{ margin: 0, paddingLeft: '1rem', color: colors.text.secondary }}>
                {recommendation.risks.map((risk, index) => (
                  <li key={index} style={{ marginBottom: '0.25rem' }}>
                    {risk}
                  </li>
                ))}
              </ul>
            </div>
          )}

          {/* Action Buttons */}
          <div style={{ 
            display: 'flex', 
            gap: '1rem',
            justifyContent: 'flex-end',
            flexDirection: isMobile ? 'column' : 'row'
          }}>
            <button
              onClick={(e) => {
                e.stopPropagation();
                handleDismiss();
              }}
              disabled={isProcessing}
              style={{
                padding: '0.75rem 1rem',
                backgroundColor: 'transparent',
                color: colors.text.secondary,
                border: `1px solid ${colors.gray[300]}`,
                borderRadius: '6px',
                fontSize: '0.9rem',
                fontWeight: '500',
                cursor: isProcessing ? 'not-allowed' : 'pointer',
                transition: 'all 0.2s ease',
                opacity: isProcessing ? 0.6 : 1
              }}
            >
              {isProcessing ? 'Processing...' : 'Dismiss'}
            </button>
            
            <button
              onClick={(e) => {
                e.stopPropagation();
                handleAccept();
              }}
              disabled={isProcessing}
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: getImpactColor(recommendation.impact),
                color: colors.white,
                border: 'none',
                borderRadius: '6px',
                fontSize: '0.9rem',
                fontWeight: '600',
                cursor: isProcessing ? 'not-allowed' : 'pointer',
                transition: 'all 0.2s ease',
                opacity: isProcessing ? 0.6 : 1
              }}
            >
              {isProcessing ? 'Processing...' : 'Accept Recommendation'}
            </button>
          </div>
        </div>
      )}

      {/* Expand Indicator */}
      <div style={{ 
        textAlign: 'center', 
        marginTop: '0.5rem',
        color: colors.text.secondary,
        fontSize: '0.8rem'
      }}>
        {isExpanded ? '▼ Click to collapse' : '▶ Click to expand details'}
      </div>
    </div>
  );
};

export default RecommendationCard;