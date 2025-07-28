import React, { useState } from 'react';
import { colors, getCardStyle } from '../../styles/colors';
import { textStyles } from '../../styles/typography';

const ReportViewer = ({ 
  report, 
  onDownload, 
  onShare, 
  onSchedule,
  isMobile = false 
}) => {
  const [viewMode, setViewMode] = useState('preview'); // preview, fullscreen
  const [isLoading, setIsLoading] = useState(false);

  if (!report) {
    return (
      <div style={{
        ...getCardStyle(),
        padding: '3rem',
        textAlign: 'center',
        backgroundColor: colors.background.secondary
      }}>
        <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>📄</div>
        <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: '1rem' }}>
          No Report Selected
        </h3>
        <p style={{ ...textStyles.body(colors.text.secondary) }}>
          Generate a report using the Report Builder to view it here.
        </p>
      </div>
    );
  }

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getReportTypeIcon = (type) => {
    const icons = {
      cost_summary: '📊',
      detailed_breakdown: '🔍',
      executive_summary: '👔',
      budget_performance: '🎯',
      cost_optimization: '💡',
      chargeback: '💰'
    };
    return icons[type] || '📄';
  };

  const getFormatIcon = (format) => {
    const icons = {
      pdf: '📄',
      excel: '📊',
      csv: '📋',
      json: '🔧'
    };
    return icons[format] || '📄';
  };

  const handleAction = async (action) => {
    setIsLoading(true);
    try {
      // Simulate async operation
      await new Promise(resolve => setTimeout(resolve, 1500));
      
      switch (action) {
        case 'download':
          onDownload?.(report);
          break;
        case 'share':
          onShare?.(report);
          break;
        case 'schedule':
          onSchedule?.(report);
          break;
        default:
          break;
      }
    } catch (error) {
      console.error(`Failed to ${action} report:`, error);
    } finally {
      setIsLoading(false);
    }
  };

  const renderReportContent = () => {
    // This would contain the actual report content based on type
    switch (report.type) {
      case 'cost_summary':
        return (
          <div style={{ padding: '2rem' }}>
            <h2 style={{ ...textStyles.h2(colors.text.primary), marginBottom: '2rem' }}>
              Cost Summary Report
            </h2>
            
            {/* Executive Summary */}
            <div style={{ marginBottom: '2rem' }}>
              <h3 style={{ ...textStyles.h3(colors.text.primary), marginBottom: '1rem' }}>
                Executive Summary
              </h3>
              <div style={{
                padding: '1.5rem',
                backgroundColor: colors.background.secondary,
                borderRadius: '8px',
                marginBottom: '1rem'
              }}>
                <p style={{ ...textStyles.body(colors.text.primary), lineHeight: '1.6' }}>
                  Total cloud infrastructure costs for the reporting period amount to <strong>$45,678</strong>, 
                  representing a <span style={{ color: colors.error }}>12% increase</span> compared to the previous period. 
                  The primary cost drivers are compute services (65%) and storage (20%).
                </p>
              </div>
            </div>

            {/* Key Metrics */}
            <div style={{ marginBottom: '2rem' }}>
              <h3 style={{ ...textStyles.h3(colors.text.primary), marginBottom: '1rem' }}>
                Key Metrics
              </h3>
              <div style={{
                display: 'grid',
                gridTemplateColumns: isMobile ? '1fr' : 'repeat(auto-fit, minmax(200px, 1fr))',
                gap: '1rem'
              }}>
                {[
                  { label: 'Total Spend', value: '$45,678', change: '+12%', changeColor: colors.error },
                  { label: 'Daily Average', value: '$1,522', change: '+8%', changeColor: colors.error },
                  { label: 'Budget Utilization', value: '78%', change: '+5%', changeColor: '#ff6f00' },
                  { label: 'Cost per Service', value: '$3,806', change: '-3%', changeColor: colors.success }
                ].map((metric, index) => (
                  <div key={index} style={{
                    padding: '1rem',
                    backgroundColor: colors.white,
                    border: `1px solid ${colors.gray[200]}`,
                    borderRadius: '6px'
                  }}>
                    <div style={{ ...textStyles.caption(colors.text.secondary), marginBottom: '0.5rem' }}>
                      {metric.label}
                    </div>
                    <div style={{ ...textStyles.h3(colors.text.primary), margin: '0 0 0.25rem 0' }}>
                      {metric.value}
                    </div>
                    <div style={{ ...textStyles.caption(metric.changeColor), fontWeight: '600' }}>
                      {metric.change} vs last period
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Cost Breakdown Chart Placeholder */}
            <div style={{ marginBottom: '2rem' }}>
              <h3 style={{ ...textStyles.h3(colors.text.primary), marginBottom: '1rem' }}>
                Cost Breakdown by Service
              </h3>
              <div style={{
                height: '300px',
                backgroundColor: colors.background.secondary,
                borderRadius: '8px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                border: `2px dashed ${colors.gray[300]}`
              }}>
                <div style={{ textAlign: 'center' }}>
                  <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📊</div>
                  <p style={{ ...textStyles.body(colors.text.secondary) }}>
                    Chart visualization would appear here
                  </p>
                </div>
              </div>
            </div>

            {/* Recommendations */}
            <div>
              <h3 style={{ ...textStyles.h3(colors.text.primary), marginBottom: '1rem' }}>
                Key Recommendations
              </h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                {[
                  { 
                    title: 'Right-size EC2 instances', 
                    impact: 'High', 
                    savings: '$8,500/month',
                    description: 'Several instances show low utilization and can be downsized.'
                  },
                  { 
                    title: 'Implement Reserved Instances', 
                    impact: 'Medium', 
                    savings: '$3,200/month',
                    description: 'Purchase RIs for consistent workloads to reduce costs.'
                  },
                  { 
                    title: 'Clean up unused resources', 
                    impact: 'Low', 
                    savings: '$450/month',
                    description: 'Remove orphaned volumes and unused load balancers.'
                  }
                ].map((rec, index) => (
                  <div key={index} style={{
                    padding: '1rem',
                    backgroundColor: colors.background.secondary,
                    borderRadius: '6px',
                    borderLeft: `4px solid ${colors.primary[500]}`
                  }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.5rem' }}>
                      <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', margin: 0 }}>
                        {rec.title}
                      </h4>
                      <div style={{
                        padding: '0.25rem 0.5rem',
                        backgroundColor: rec.impact === 'High' ? colors.error + '20' : rec.impact === 'Medium' ? '#ff6f00' + '20' : colors.success + '20',
                        color: rec.impact === 'High' ? colors.error : rec.impact === 'Medium' ? '#ff6f00' : colors.success,
                        borderRadius: '4px',
                        fontSize: '0.75rem',
                        fontWeight: '600'
                      }}>
                        {rec.impact} Impact
                      </div>
                    </div>
                    <p style={{ ...textStyles.caption(colors.text.secondary), margin: '0 0 0.5rem 0' }}>
                      {rec.description}
                    </p>
                    <div style={{ ...textStyles.body(colors.success), fontWeight: '600' }}>
                      Potential savings: {rec.savings}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        );
      
      default:
        return (
          <div style={{ padding: '2rem', textAlign: 'center' }}>
            <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>
              {getReportTypeIcon(report.type)}
            </div>
            <h2 style={{ ...textStyles.h2(colors.text.primary), marginBottom: '1rem' }}>
              {report.title}
            </h2>
            <p style={{ ...textStyles.body(colors.text.secondary) }}>
              Report content for {report.type} would be displayed here.
            </p>
          </div>
        );
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
      {/* Report Actions Header */}
      <div style={{
        ...getCardStyle(),
        padding: '1.5rem',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '1rem'
      }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '1.5rem' }}>{getReportTypeIcon(report.type)}</span>
            <h3 style={{ ...textStyles.cardTitle(colors.text.primary), margin: 0 }}>
              {report.title}
            </h3>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
            <span style={{ ...textStyles.caption(colors.text.secondary) }}>
              Generated: {formatDate(report.generatedAt || new Date().toISOString())}
            </span>
            <span style={{ ...textStyles.caption(colors.text.secondary) }}>
              Format: {getFormatIcon(report.format)} {report.format?.toUpperCase()}
            </span>
            <span style={{ ...textStyles.caption(colors.text.secondary) }}>
              Time Range: {report.timeRange?.replace('_', ' ') || 'Last 30 days'}
            </span>
          </div>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <button
            onClick={() => setViewMode(viewMode === 'preview' ? 'fullscreen' : 'preview')}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: 'transparent',
              color: colors.text.secondary,
              border: `1px solid ${colors.gray[300]}`,
              borderRadius: '6px',
              fontSize: '0.9rem',
              cursor: 'pointer',
              transition: 'all 0.2s ease'
            }}
          >
            {viewMode === 'preview' ? '⛶ Fullscreen' : '⊞ Preview'}
          </button>
          
          <button
            onClick={() => handleAction('download')}
            disabled={isLoading}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: colors.primary[500],
              color: colors.white,
              border: 'none',
              borderRadius: '6px',
              fontSize: '0.9rem',
              fontWeight: '600',
              cursor: isLoading ? 'not-allowed' : 'pointer',
              opacity: isLoading ? 0.7 : 1,
              transition: 'all 0.2s ease',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem'
            }}
          >
            {isLoading ? '⏳' : '⬇'} Download
          </button>

          <button
            onClick={() => handleAction('share')}
            disabled={isLoading}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: 'transparent',
              color: colors.primary[500],
              border: `1px solid ${colors.primary[500]}`,
              borderRadius: '6px',
              fontSize: '0.9rem',
              fontWeight: '600',
              cursor: isLoading ? 'not-allowed' : 'pointer',
              opacity: isLoading ? 0.7 : 1,
              transition: 'all 0.2s ease',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem'
            }}
          >
            {isLoading ? '⏳' : '🔗'} Share
          </button>
        </div>
      </div>

      {/* Report Content */}
      <div style={{
        ...getCardStyle(),
        backgroundColor: colors.white,
        minHeight: viewMode === 'fullscreen' ? '80vh' : '600px',
        overflow: 'auto'
      }}>
        {renderReportContent()}
      </div>

      {/* Report Footer */}
      <div style={{
        ...getCardStyle(),
        padding: '1rem',
        backgroundColor: colors.background.secondary,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '1rem'
      }}>
        <div style={{ ...textStyles.caption(colors.text.secondary) }}>
          💡 Tip: Use the Download button to save this report in {report.format?.toUpperCase()} format
        </div>
        
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button
            onClick={() => handleAction('schedule')}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: 'transparent',
              color: colors.text.secondary,
              border: `1px solid ${colors.gray[300]}`,
              borderRadius: '4px',
              fontSize: '0.8rem',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '0.25rem'
            }}
          >
            📅 Schedule Similar
          </button>
        </div>
      </div>
    </div>
  );
};

export default ReportViewer;