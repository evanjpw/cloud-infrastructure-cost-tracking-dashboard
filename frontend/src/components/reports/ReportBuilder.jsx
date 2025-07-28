import React, { useState, useEffect } from 'react';
import { colors, getCardStyle } from '../../styles/colors';
import { textStyles } from '../../styles/typography';

const ReportBuilder = ({ 
  teams = [], 
  services = [], 
  onGenerateReport, 
  isMobile = false 
}) => {
  const [reportConfig, setReportConfig] = useState({
    title: '',
    description: '',
    type: 'cost_summary',
    timeRange: 'last_30_days',
    customStartDate: '',
    customEndDate: '',
    teams: [],
    services: [],
    groupBy: 'team',
    includeCharts: true,
    includeRecommendations: false,
    format: 'pdf',
    schedule: 'none'
  });

  const [isGenerating, setIsGenerating] = useState(false);
  const [errors, setErrors] = useState({});

  const reportTypes = [
    {
      value: 'cost_summary',
      label: 'Cost Summary Report',
      description: 'High-level cost overview with trends and KPIs',
      icon: '📊'
    },
    {
      value: 'detailed_breakdown',
      label: 'Detailed Cost Breakdown',
      description: 'Service-by-service detailed analysis',
      icon: '🔍'
    },
    {
      value: 'executive_summary',
      label: 'Executive Summary',
      description: 'Executive-level insights and recommendations',
      icon: '👔'
    },
    {
      value: 'budget_performance',
      label: 'Budget Performance',
      description: 'Budget vs actual spending analysis',
      icon: '🎯'
    },
    {
      value: 'cost_optimization',
      label: 'Cost Optimization Report',
      description: 'Optimization opportunities and recommendations',
      icon: '💡'
    },
    {
      value: 'chargeback',
      label: 'Chargeback Report',
      description: 'Team-based cost allocation for billing',
      icon: '💰'
    }
  ];

  const timeRanges = [
    { value: 'last_7_days', label: 'Last 7 Days' },
    { value: 'last_30_days', label: 'Last 30 Days' },
    { value: 'last_90_days', label: 'Last 90 Days' },
    { value: 'current_month', label: 'Current Month' },
    { value: 'last_month', label: 'Last Month' },
    { value: 'current_quarter', label: 'Current Quarter' },
    { value: 'last_quarter', label: 'Last Quarter' },
    { value: 'current_year', label: 'Current Year' },
    { value: 'custom', label: 'Custom Range' }
  ];

  const groupByOptions = [
    { value: 'team', label: 'Team', icon: '👥' },
    { value: 'service', label: 'Service', icon: '🔧' },
    { value: 'provider', label: 'Provider', icon: '☁️' },
    { value: 'region', label: 'Region', icon: '🌍' },
    { value: 'environment', label: 'Environment', icon: '🏗️' },
    { value: 'date', label: 'Date', icon: '📅' }
  ];

  const formatOptions = [
    { value: 'pdf', label: 'PDF', icon: '📄', description: 'Professional report format' },
    { value: 'excel', label: 'Excel', icon: '📊', description: 'Spreadsheet with data and charts' },
    { value: 'csv', label: 'CSV', icon: '📋', description: 'Raw data export' },
    { value: 'json', label: 'JSON', icon: '🔧', description: 'API-friendly format' }
  ];

  const scheduleOptions = [
    { value: 'none', label: 'Generate Once' },
    { value: 'daily', label: 'Daily' },
    { value: 'weekly', label: 'Weekly' },
    { value: 'monthly', label: 'Monthly' },
    { value: 'quarterly', label: 'Quarterly' }
  ];

  const validateForm = () => {
    const newErrors = {};

    if (!reportConfig.title.trim()) {
      newErrors.title = 'Report title is required';
    }

    if (reportConfig.timeRange === 'custom') {
      if (!reportConfig.customStartDate) {
        newErrors.customStartDate = 'Start date is required';
      }
      if (!reportConfig.customEndDate) {
        newErrors.customEndDate = 'End date is required';
      }
      if (reportConfig.customStartDate && reportConfig.customEndDate && 
          reportConfig.customStartDate >= reportConfig.customEndDate) {
        newErrors.customEndDate = 'End date must be after start date';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleGenerateReport = async () => {
    if (!validateForm()) return;

    setIsGenerating(true);
    try {
      // Simulate report generation
      await new Promise(resolve => setTimeout(resolve, 2000));
      onGenerateReport?.(reportConfig);
    } catch (error) {
      console.error('Report generation failed:', error);
    } finally {
      setIsGenerating(false);
    }
  };

  const handleChange = (field, value) => {
    setReportConfig(prev => ({ ...prev, [field]: value }));
    
    // Clear related errors
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  const selectedReportType = reportTypes.find(type => type.value === reportConfig.type);

  return (
    <div style={{
      ...getCardStyle(),
      padding: isMobile ? '1rem' : '2rem',
      marginBottom: '2rem'
    }}>
      <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: '1.5rem' }}>
        📝 Custom Report Builder
      </h3>

      {/* Report Title & Description */}
      <div style={{ marginBottom: '2rem' }}>
        <div style={{ marginBottom: '1rem' }}>
          <label style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', display: 'block', marginBottom: '0.5rem' }}>
            Report Title *
          </label>
          <input
            type="text"
            value={reportConfig.title}
            onChange={(e) => handleChange('title', e.target.value)}
            placeholder="e.g., Monthly Cost Analysis Report"
            style={{
              width: '100%',
              padding: '0.75rem',
              border: `1px solid ${errors.title ? colors.error : colors.gray[300]}`,
              borderRadius: '6px',
              fontSize: '0.9rem',
              backgroundColor: errors.title ? '#ffebee' : colors.white,
            }}
          />
          {errors.title && (
            <p style={{ color: colors.error, fontSize: '0.8rem', margin: '0.25rem 0 0 0' }}>
              {errors.title}
            </p>
          )}
        </div>

        <div>
          <label style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', display: 'block', marginBottom: '0.5rem' }}>
            Description (Optional)
          </label>
          <textarea
            value={reportConfig.description}
            onChange={(e) => handleChange('description', e.target.value)}
            placeholder="Brief description of the report purpose..."
            rows="2"
            style={{
              width: '100%',
              padding: '0.75rem',
              border: `1px solid ${colors.gray[300]}`,
              borderRadius: '6px',
              fontSize: '0.9rem',
              backgroundColor: colors.white,
              resize: 'vertical'
            }}
          />
        </div>
      </div>

      {/* Report Type Selection */}
      <div style={{ marginBottom: '2rem' }}>
        <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', marginBottom: '1rem' }}>
          Report Type
        </h4>
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: isMobile ? '1fr' : 'repeat(auto-fit, minmax(250px, 1fr))',
          gap: '1rem'
        }}>
          {reportTypes.map(type => (
            <div
              key={type.value}
              onClick={() => handleChange('type', type.value)}
              style={{
                padding: '1rem',
                border: `2px solid ${reportConfig.type === type.value ? colors.primary[500] : colors.gray[300]}`,
                borderRadius: '8px',
                cursor: 'pointer',
                transition: 'all 0.2s ease',
                backgroundColor: reportConfig.type === type.value ? colors.primary[50] : colors.white,
              }}
              onMouseEnter={(e) => {
                if (reportConfig.type !== type.value) {
                  e.currentTarget.style.borderColor = colors.primary[300];
                  e.currentTarget.style.backgroundColor = colors.gray[50];
                }
              }}
              onMouseLeave={(e) => {
                if (reportConfig.type !== type.value) {
                  e.currentTarget.style.borderColor = colors.gray[300];
                  e.currentTarget.style.backgroundColor = colors.white;
                }
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
                <span style={{ fontSize: '1.5rem' }}>{type.icon}</span>
                <h5 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', margin: 0 }}>
                  {type.label}
                </h5>
              </div>
              <p style={{ ...textStyles.caption(colors.text.secondary), margin: 0, lineHeight: '1.4' }}>
                {type.description}
              </p>
            </div>
          ))}
        </div>
      </div>

      {/* Time Range */}
      <div style={{ 
        display: 'grid',
        gridTemplateColumns: isMobile ? '1fr' : '1fr 1fr',
        gap: '2rem',
        marginBottom: '2rem'
      }}>
        <div>
          <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', marginBottom: '1rem' }}>
            Time Range
          </h4>
          <select
            value={reportConfig.timeRange}
            onChange={(e) => handleChange('timeRange', e.target.value)}
            style={{
              width: '100%',
              padding: '0.75rem',
              border: `1px solid ${colors.gray[300]}`,
              borderRadius: '6px',
              fontSize: '0.9rem',
              backgroundColor: colors.white,
              cursor: 'pointer'
            }}
          >
            {timeRanges.map(range => (
              <option key={range.value} value={range.value}>
                {range.label}
              </option>
            ))}
          </select>

          {reportConfig.timeRange === 'custom' && (
            <div style={{ marginTop: '1rem', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div>
                <label style={{ ...textStyles.caption(colors.text.secondary), display: 'block', marginBottom: '0.25rem' }}>
                  Start Date
                </label>
                <input
                  type="date"
                  value={reportConfig.customStartDate}
                  onChange={(e) => handleChange('customStartDate', e.target.value)}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: `1px solid ${errors.customStartDate ? colors.error : colors.gray[300]}`,
                    borderRadius: '4px',
                    fontSize: '0.8rem'
                  }}
                />
                {errors.customStartDate && (
                  <p style={{ color: colors.error, fontSize: '0.75rem', margin: '0.25rem 0 0 0' }}>
                    {errors.customStartDate}
                  </p>
                )}
              </div>
              <div>
                <label style={{ ...textStyles.caption(colors.text.secondary), display: 'block', marginBottom: '0.25rem' }}>
                  End Date
                </label>
                <input
                  type="date"
                  value={reportConfig.customEndDate}
                  onChange={(e) => handleChange('customEndDate', e.target.value)}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: `1px solid ${errors.customEndDate ? colors.error : colors.gray[300]}`,
                    borderRadius: '4px',
                    fontSize: '0.8rem'
                  }}
                />
                {errors.customEndDate && (
                  <p style={{ color: colors.error, fontSize: '0.75rem', margin: '0.25rem 0 0 0' }}>
                    {errors.customEndDate}
                  </p>
                )}
              </div>
            </div>
          )}
        </div>

        <div>
          <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', marginBottom: '1rem' }}>
            Group By
          </h4>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
            {groupByOptions.map(option => (
              <button
                key={option.value}
                onClick={() => handleChange('groupBy', option.value)}
                style={{
                  padding: '0.5rem 1rem',
                  backgroundColor: reportConfig.groupBy === option.value ? colors.primary[500] : 'transparent',
                  color: reportConfig.groupBy === option.value ? colors.white : colors.text.secondary,
                  border: `1px solid ${reportConfig.groupBy === option.value ? colors.primary[500] : colors.gray[300]}`,
                  borderRadius: '6px',
                  fontSize: '0.8rem',
                  fontWeight: '500',
                  cursor: 'pointer',
                  transition: 'all 0.2s ease',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.25rem'
                }}
              >
                <span style={{ fontSize: '0.9em' }}>{option.icon}</span>
                {option.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Options */}
      <div style={{ marginBottom: '2rem' }}>
        <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', marginBottom: '1rem' }}>
          Report Options
        </h4>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          <label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
            <input
              type="checkbox"
              checked={reportConfig.includeCharts}
              onChange={(e) => handleChange('includeCharts', e.target.checked)}
              style={{ marginRight: '0.75rem', transform: 'scale(1.2)' }}
            />
            <span style={{ ...textStyles.body(colors.text.primary) }}>
              📊 Include charts and visualizations
            </span>
          </label>
          <label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
            <input
              type="checkbox"
              checked={reportConfig.includeRecommendations}
              onChange={(e) => handleChange('includeRecommendations', e.target.checked)}
              style={{ marginRight: '0.75rem', transform: 'scale(1.2)' }}
            />
            <span style={{ ...textStyles.body(colors.text.primary) }}>
              💡 Include optimization recommendations
            </span>
          </label>
        </div>
      </div>

      {/* Format & Schedule */}
      <div style={{ 
        display: 'grid',
        gridTemplateColumns: isMobile ? '1fr' : '1fr 1fr',
        gap: '2rem',
        marginBottom: '2rem'
      }}>
        <div>
          <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', marginBottom: '1rem' }}>
            Export Format
          </h4>
          <div style={{ display: 'grid', gap: '0.5rem' }}>
            {formatOptions.map(format => (
              <label key={format.value} style={{ display: 'flex', alignItems: 'center', cursor: 'pointer', padding: '0.5rem' }}>
                <input
                  type="radio"
                  name="format"
                  value={format.value}
                  checked={reportConfig.format === format.value}
                  onChange={(e) => handleChange('format', e.target.value)}
                  style={{ marginRight: '0.75rem' }}
                />
                <span style={{ fontSize: '1.2em', marginRight: '0.5rem' }}>{format.icon}</span>
                <div>
                  <div style={{ ...textStyles.body(colors.text.primary), fontWeight: '500' }}>
                    {format.label}
                  </div>
                  <div style={{ ...textStyles.caption(colors.text.secondary) }}>
                    {format.description}
                  </div>
                </div>
              </label>
            ))}
          </div>
        </div>

        <div>
          <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', marginBottom: '1rem' }}>
            Schedule
          </h4>
          <select
            value={reportConfig.schedule}
            onChange={(e) => handleChange('schedule', e.target.value)}
            style={{
              width: '100%',
              padding: '0.75rem',
              border: `1px solid ${colors.gray[300]}`,
              borderRadius: '6px',
              fontSize: '0.9rem',
              backgroundColor: colors.white,
              cursor: 'pointer'
            }}
          >
            {scheduleOptions.map(schedule => (
              <option key={schedule.value} value={schedule.value}>
                {schedule.label}
              </option>
            ))}
          </select>
          {reportConfig.schedule !== 'none' && (
            <p style={{ ...textStyles.caption(colors.text.secondary), marginTop: '0.5rem' }}>
              ℹ️ Scheduled reports will be delivered via email
            </p>
          )}
        </div>
      </div>

      {/* Generate Button */}
      <div style={{ 
        borderTop: `1px solid ${colors.gray[200]}`,
        paddingTop: '1.5rem',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        flexWrap: 'wrap',
        gap: '1rem'
      }}>
        <div>
          <p style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', margin: 0 }}>
            {selectedReportType?.icon} {selectedReportType?.label}
          </p>
          <p style={{ ...textStyles.caption(colors.text.secondary), margin: 0 }}>
            Format: {formatOptions.find(f => f.value === reportConfig.format)?.label} • 
            Schedule: {scheduleOptions.find(s => s.value === reportConfig.schedule)?.label}
          </p>
        </div>

        <button
          onClick={handleGenerateReport}
          disabled={isGenerating}
          style={{
            padding: '1rem 2rem',
            backgroundColor: isGenerating ? colors.primary[300] : colors.primary[500],
            color: colors.white,
            border: 'none',
            borderRadius: '8px',
            fontSize: '1rem',
            fontWeight: '600',
            cursor: isGenerating ? 'not-allowed' : 'pointer',
            transition: 'all 0.2s ease',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            transform: isGenerating ? 'scale(0.98)' : 'scale(1)',
            boxShadow: isGenerating ? 'none' : '0 4px 8px rgba(33, 150, 243, 0.3)',
          }}
        >
          <span style={{ fontSize: '1.2em' }}>
            {isGenerating ? '⏳' : '📊'}
          </span>
          {isGenerating ? 'Generating Report...' : 'Generate Report'}
        </button>
      </div>
    </div>
  );
};

export default ReportBuilder;