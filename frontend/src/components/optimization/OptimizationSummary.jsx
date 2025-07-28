import React from 'react';
import { colors, getCardStyle } from '../../styles/colors';
import { textStyles } from '../../styles/typography';

const OptimizationSummary = ({ 
  recommendations = [], 
  totalCost = 0,
  isMobile = false 
}) => {
  // Calculate optimization metrics
  const metrics = calculateOptimizationMetrics(recommendations, totalCost);

  return (
    <div style={{
      ...getCardStyle(),
      padding: isMobile ? '1rem' : '1.5rem',
      marginBottom: '2rem',
      background: `linear-gradient(135deg, ${colors.primary[50]} 0%, ${colors.success}15 100%)`,
      border: `1px solid ${colors.primary[200]}`
    }}>
      {/* Header */}
      <div style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: '0.5rem' }}>
          💡 Cost Optimization Summary
        </h2>
        <p style={{ ...textStyles.body(colors.text.secondary), margin: 0 }}>
          AI-powered analysis of your cloud spending with actionable recommendations
        </p>
      </div>

      {/* Key Metrics Grid */}
      <div style={{ 
        display: 'grid', 
        gridTemplateColumns: isMobile ? '1fr' : 'repeat(4, 1fr)',
        gap: '1rem',
        marginBottom: '1.5rem'
      }}>
        {/* Total Potential Savings */}
        <div style={{
          textAlign: 'center',
          padding: '1.5rem 1rem',
          backgroundColor: colors.success + '20',
          borderRadius: '8px',
          border: `1px solid ${colors.success}40`,
          gridColumn: isMobile ? '1' : '1 / 3'
        }}>
          <div style={{ fontSize: isMobile ? '2rem' : '2.5rem', marginBottom: '0.5rem' }}>💰</div>
          <div style={{ 
            fontSize: isMobile ? '1.8rem' : '2.2rem', 
            fontWeight: '700', 
            color: colors.success,
            marginBottom: '0.25rem'
          }}>
            ${metrics.totalSavings.toLocaleString()}
          </div>
          <div style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', marginBottom: '0.25rem' }}>
            Potential Monthly Savings
          </div>
          <div style={{ ...textStyles.caption(colors.text.secondary) }}>
            {metrics.savingsPercentage}% of current spend
          </div>
        </div>

        {/* Recommendations Count */}
        <div style={{
          textAlign: 'center',
          padding: '1.5rem 1rem',
          backgroundColor: colors.primary[100],
          borderRadius: '8px',
          border: `1px solid ${colors.primary[300]}`
        }}>
          <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>📋</div>
          <div style={{ 
            fontSize: '1.8rem', 
            fontWeight: '700', 
            color: colors.primary[700],
            marginBottom: '0.25rem'
          }}>
            {recommendations.length}
          </div>
          <div style={{ ...textStyles.body(colors.text.primary), fontWeight: '600' }}>
            Recommendations
          </div>
        </div>

        {/* ROI Estimate */}
        <div style={{
          textAlign: 'center',
          padding: '1.5rem 1rem',
          backgroundColor: colors.warning + '20',
          borderRadius: '8px',
          border: `1px solid ${colors.warning}40`
        }}>
          <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>📈</div>
          <div style={{ 
            fontSize: '1.8rem', 
            fontWeight: '700', 
            color: colors.warning,
            marginBottom: '0.25rem'
          }}>
            {metrics.annualROI}%
          </div>
          <div style={{ ...textStyles.body(colors.text.primary), fontWeight: '600' }}>
            Annual ROI
          </div>
        </div>
      </div>

      {/* Priority Breakdown */}
      <div style={{ 
        display: 'grid',
        gridTemplateColumns: isMobile ? '1fr' : '1fr 2fr',
        gap: '1.5rem',
        marginBottom: '1.5rem'
      }}>
        {/* Impact Distribution */}
        <div style={{
          padding: '1rem',
          backgroundColor: colors.white,
          borderRadius: '6px',
          border: `1px solid ${colors.gray[200]}`
        }}>
          <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', marginBottom: '1rem' }}>
            Recommendations by Impact
          </h4>
          
          {['high', 'medium', 'low'].map(impact => {
            const count = metrics.byImpact[impact] || 0;
            const percentage = recommendations.length > 0 ? (count / recommendations.length * 100) : 0;
            const impactColor = impact === 'high' ? colors.success : 
                               impact === 'medium' ? colors.warning : colors.primary[500];
            
            return (
              <div key={impact} style={{ marginBottom: '0.75rem' }}>
                <div style={{ 
                  display: 'flex', 
                  justifyContent: 'space-between', 
                  alignItems: 'center',
                  marginBottom: '0.25rem'
                }}>
                  <span style={{ 
                    ...textStyles.body(colors.text.primary), 
                    fontWeight: '500',
                    textTransform: 'capitalize'
                  }}>
                    {impact} Impact
                  </span>
                  <span style={{ ...textStyles.body(impactColor), fontWeight: '600' }}>
                    {count}
                  </span>
                </div>
                <div style={{
                  width: '100%',
                  height: '6px',
                  backgroundColor: colors.gray[200],
                  borderRadius: '3px',
                  overflow: 'hidden'
                }}>
                  <div style={{
                    width: `${percentage}%`,
                    height: '100%',
                    backgroundColor: impactColor,
                    borderRadius: '3px',
                    transition: 'width 0.3s ease'
                  }} />
                </div>
              </div>
            );
          })}
        </div>

        {/* Top Recommendations Preview */}
        <div style={{
          padding: '1rem',
          backgroundColor: colors.white,
          borderRadius: '6px',
          border: `1px solid ${colors.gray[200]}`
        }}>
          <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', marginBottom: '1rem' }}>
            Top Optimization Opportunities
          </h4>
          
          {metrics.topRecommendations.slice(0, 3).map((rec, index) => (
            <div key={rec.id} style={{ 
              display: 'flex', 
              justifyContent: 'space-between', 
              alignItems: 'center',
              padding: '0.75rem',
              backgroundColor: index === 0 ? colors.success + '10' : colors.gray[50],
              borderRadius: '4px',
              marginBottom: '0.5rem',
              border: index === 0 ? `1px solid ${colors.success}40` : `1px solid ${colors.gray[200]}`
            }}>
              <div style={{ flex: 1 }}>
                <div style={{ ...textStyles.body(colors.text.primary), fontWeight: '500', marginBottom: '0.25rem' }}>
                  {index === 0 && '🏆 '}{rec.title}
                </div>
                <div style={{ ...textStyles.caption(colors.text.secondary) }}>
                  {getTypeLabel(rec.type)} • {rec.impact} impact
                </div>
              </div>
              <div style={{ textAlign: 'right', marginLeft: '1rem' }}>
                <div style={{ ...textStyles.body(colors.success), fontWeight: '600' }}>
                  ${rec.potentialSavings?.toLocaleString() || 0}
                </div>
                <div style={{ ...textStyles.caption(colors.text.secondary) }}>
                  /month
                </div>
              </div>
            </div>
          ))}

          {recommendations.length > 3 && (
            <div style={{ 
              textAlign: 'center', 
              marginTop: '0.75rem',
              color: colors.primary[600],
              fontSize: '0.9rem'
            }}>
              +{recommendations.length - 3} more recommendations below
            </div>
          )}
        </div>
      </div>

      {/* Implementation Timeline */}
      <div style={{
        padding: '1rem',
        backgroundColor: colors.primary[25],
        borderRadius: '6px',
        border: `1px solid ${colors.primary[200]}`
      }}>
        <h4 style={{ ...textStyles.body(colors.primary[700]), fontWeight: '600', marginBottom: '0.75rem' }}>
          🚀 Implementation Roadmap
        </h4>
        
        <div style={{ 
          display: 'grid',
          gridTemplateColumns: isMobile ? '1fr' : 'repeat(3, 1fr)',
          gap: '1rem'
        }}>
          <div>
            <div style={{ ...textStyles.body(colors.primary[700]), fontWeight: '600', marginBottom: '0.25rem' }}>
              Week 1-2: Quick Wins
            </div>
            <div style={{ ...textStyles.caption(colors.text.secondary) }}>
              {metrics.quickWins} low-risk optimizations • Est. ${metrics.quickWinsSavings.toLocaleString()}/mo
            </div>
          </div>
          
          <div>
            <div style={{ ...textStyles.body(colors.primary[700]), fontWeight: '600', marginBottom: '0.25rem' }}>
              Week 3-6: Major Changes
            </div>
            <div style={{ ...textStyles.caption(colors.text.secondary) }}>
              {metrics.majorChanges} infrastructure updates • Est. ${metrics.majorSavings.toLocaleString()}/mo
            </div>
          </div>
          
          <div>
            <div style={{ ...textStyles.body(colors.primary[700]), fontWeight: '600', marginBottom: '0.25rem' }}>
              Ongoing: Governance
            </div>
            <div style={{ ...textStyles.caption(colors.text.secondary) }}>
              Cost monitoring & policies • Est. ${metrics.governanceSavings.toLocaleString()}/mo
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

// Helper functions
const calculateOptimizationMetrics = (recommendations, totalCost) => {
  const totalSavings = recommendations.reduce((sum, rec) => sum + (rec.potentialSavings || 0), 0);
  const savingsPercentage = totalCost > 0 ? Math.round((totalSavings / totalCost) * 100) : 0;
  const annualROI = Math.round((totalSavings * 12) / Math.max(totalCost, 1) * 100);

  // Group by impact
  const byImpact = recommendations.reduce((acc, rec) => {
    acc[rec.impact] = (acc[rec.impact] || 0) + 1;
    return acc;
  }, {});

  // Sort by potential savings
  const topRecommendations = [...recommendations]
    .sort((a, b) => (b.potentialSavings || 0) - (a.potentialSavings || 0));

  // Implementation timeline estimates
  const quickWins = recommendations.filter(rec => 
    rec.type === 'unused_resource' || rec.impact === 'low'
  ).length;
  const quickWinsSavings = recommendations
    .filter(rec => rec.type === 'unused_resource' || rec.impact === 'low')
    .reduce((sum, rec) => sum + (rec.potentialSavings || 0), 0);

  const majorChanges = recommendations.filter(rec => 
    rec.type === 'rightsizing' || rec.type === 'reserved_instance'
  ).length;
  const majorSavings = recommendations
    .filter(rec => rec.type === 'rightsizing' || rec.type === 'reserved_instance')
    .reduce((sum, rec) => sum + (rec.potentialSavings || 0), 0);

  const governanceSavings = recommendations
    .filter(rec => rec.type === 'optimization')
    .reduce((sum, rec) => sum + (rec.potentialSavings || 0), 0);

  return {
    totalSavings,
    savingsPercentage,
    annualROI,
    byImpact,
    topRecommendations,
    quickWins,
    quickWinsSavings,
    majorChanges,
    majorSavings,
    governanceSavings
  };
};

const getTypeLabel = (type) => {
  switch (type.toLowerCase()) {
    case 'rightsizing': return 'Right-sizing';
    case 'reserved_instance': return 'Reserved Instance';
    case 'unused_resource': return 'Cleanup';
    case 'anomaly': return 'Anomaly';
    case 'optimization': return 'Governance';
    default: return 'Optimization';
  }
};

export default OptimizationSummary;