import React, { useState, useEffect } from 'react';
import { colors, getCardStyle } from '../../styles/colors';
import { textStyles } from '../../styles/typography';

const BudgetForm = ({ 
  budget = null, 
  teams = [], 
  services = [],
  onSave, 
  onCancel, 
  isMobile = false 
}) => {
  const [formData, setFormData] = useState({
    name: '',
    amount: '',
    period: 'monthly',
    scope: 'team',
    target: '',
    alertThreshold: 80,
    description: '',
    startDate: '',
    endDate: ''
  });

  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (budget) {
      setFormData({
        name: budget.name || '',
        amount: budget.amount?.toString() || '',
        period: budget.period || 'monthly',
        scope: budget.scope || 'team',
        target: budget.target || '',
        alertThreshold: budget.alertThreshold || 80,
        description: budget.description || '',
        startDate: budget.startDate || '',
        endDate: budget.endDate || ''
      });
    } else {
      // Set default dates for new budget
      const now = new Date();
      const startDate = now.toISOString().slice(0, 10);
      const endDate = new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString().slice(0, 10);
      
      setFormData(prev => ({
        ...prev,
        startDate,
        endDate
      }));
    }
  }, [budget]);

  const validateForm = () => {
    const newErrors = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Budget name is required';
    }

    if (!formData.amount || parseFloat(formData.amount) <= 0) {
      newErrors.amount = 'Budget amount must be greater than 0';
    }

    if (!formData.target.trim()) {
      newErrors.target = 'Target selection is required';
    }

    if (!formData.startDate) {
      newErrors.startDate = 'Start date is required';
    }

    if (!formData.endDate) {
      newErrors.endDate = 'End date is required';
    }

    if (formData.startDate && formData.endDate && formData.startDate >= formData.endDate) {
      newErrors.endDate = 'End date must be after start date';
    }

    if (formData.alertThreshold < 0 || formData.alertThreshold > 100) {
      newErrors.alertThreshold = 'Alert threshold must be between 0 and 100';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (validateForm()) {
      const budgetData = {
        ...formData,
        amount: parseFloat(formData.amount),
        alertThreshold: parseInt(formData.alertThreshold),
        id: budget?.id || Date.now().toString()
      };
      
      onSave(budgetData);
    }
  };

  const handleChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    
    // Clear error when user starts typing
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  const getTargetOptions = () => {
    switch (formData.scope) {
      case 'team':
        return teams.map(team => ({ value: team.name, label: team.displayName || team.name }));
      case 'service':
        return services.map(service => ({ value: service, label: service }));
      case 'total':
        return [{ value: 'organization', label: 'Total Organization' }];
      default:
        return [];
    }
  };

  const inputStyle = {
    width: '100%',
    padding: '0.75rem',
    border: `1px solid ${colors.gray[300]}`,
    borderRadius: '6px',
    fontSize: '0.9rem',
    backgroundColor: colors.white,
    transition: 'border-color 0.2s ease',
  };

  const errorInputStyle = {
    ...inputStyle,
    borderColor: colors.error,
    backgroundColor: '#ffebee',
  };

  const selectStyle = {
    ...inputStyle,
    cursor: 'pointer',
  };

  const buttonStyle = {
    padding: '0.75rem 1.5rem',
    borderRadius: '6px',
    fontSize: '0.9rem',
    fontWeight: '600',
    cursor: 'pointer',
    transition: 'all 0.2s ease',
    border: 'none',
  };

  const primaryButtonStyle = {
    ...buttonStyle,
    backgroundColor: colors.primary[500],
    color: colors.white,
  };

  const secondaryButtonStyle = {
    ...buttonStyle,
    backgroundColor: 'transparent',
    color: colors.text.secondary,
    border: `1px solid ${colors.gray[300]}`,
  };

  return (
    <div style={{
      ...getCardStyle(),
      padding: isMobile ? '1rem' : '2rem',
      maxWidth: '600px',
      margin: '0 auto'
    }}>
      <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: '1.5rem' }}>
        {budget ? '✏️ Edit Budget' : '➕ Create New Budget'}
      </h3>

      <form onSubmit={handleSubmit}>
        {/* Budget Name */}
        <div style={{ marginBottom: '1.5rem' }}>
          <label style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', display: 'block', marginBottom: '0.5rem' }}>
            Budget Name *
          </label>
          <input
            type="text"
            value={formData.name}
            onChange={(e) => handleChange('name', e.target.value)}
            placeholder="e.g., Q4 Platform Team Budget"
            style={errors.name ? errorInputStyle : inputStyle}
          />
          {errors.name && (
            <p style={{ color: colors.error, fontSize: '0.8rem', margin: '0.25rem 0 0 0' }}>
              {errors.name}
            </p>
          )}
        </div>

        {/* Budget Amount */}
        <div style={{ marginBottom: '1.5rem' }}>
          <label style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', display: 'block', marginBottom: '0.5rem' }}>
            Budget Amount ($) *
          </label>
          <input
            type="number"
            value={formData.amount}
            onChange={(e) => handleChange('amount', e.target.value)}
            placeholder="10000"
            min="0"
            step="0.01"
            style={errors.amount ? errorInputStyle : inputStyle}
          />
          {errors.amount && (
            <p style={{ color: colors.error, fontSize: '0.8rem', margin: '0.25rem 0 0 0' }}>
              {errors.amount}
            </p>
          )}
        </div>

        {/* Period and Scope Row */}
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: isMobile ? '1fr' : '1fr 1fr',
          gap: '1rem',
          marginBottom: '1.5rem'
        }}>
          {/* Period */}
          <div>
            <label style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', display: 'block', marginBottom: '0.5rem' }}>
              Period
            </label>
            <select
              value={formData.period}
              onChange={(e) => handleChange('period', e.target.value)}
              style={selectStyle}
            >
              <option value="monthly">Monthly</option>
              <option value="quarterly">Quarterly</option>
              <option value="yearly">Yearly</option>
              <option value="custom">Custom Period</option>
            </select>
          </div>

          {/* Scope */}
          <div>
            <label style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', display: 'block', marginBottom: '0.5rem' }}>
              Scope
            </label>
            <select
              value={formData.scope}
              onChange={(e) => handleChange('scope', e.target.value)}
              style={selectStyle}
            >
              <option value="team">Team</option>
              <option value="service">Service</option>
              <option value="total">Total Organization</option>
            </select>
          </div>
        </div>

        {/* Target */}
        <div style={{ marginBottom: '1.5rem' }}>
          <label style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', display: 'block', marginBottom: '0.5rem' }}>
            Target {formData.scope === 'total' ? '(Auto-selected)' : '*'}
          </label>
          <select
            value={formData.target}
            onChange={(e) => handleChange('target', e.target.value)}
            style={errors.target ? { ...selectStyle, borderColor: colors.error } : selectStyle}
            disabled={formData.scope === 'total'}
          >
            <option value="">Select {formData.scope}...</option>
            {getTargetOptions().map(option => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
          {errors.target && (
            <p style={{ color: colors.error, fontSize: '0.8rem', margin: '0.25rem 0 0 0' }}>
              {errors.target}
            </p>
          )}
        </div>

        {/* Date Range */}
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: isMobile ? '1fr' : '1fr 1fr',
          gap: '1rem',
          marginBottom: '1.5rem'
        }}>
          <div>
            <label style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', display: 'block', marginBottom: '0.5rem' }}>
              Start Date *
            </label>
            <input
              type="date"
              value={formData.startDate}
              onChange={(e) => handleChange('startDate', e.target.value)}
              style={errors.startDate ? errorInputStyle : inputStyle}
            />
            {errors.startDate && (
              <p style={{ color: colors.error, fontSize: '0.8rem', margin: '0.25rem 0 0 0' }}>
                {errors.startDate}
              </p>
            )}
          </div>

          <div>
            <label style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', display: 'block', marginBottom: '0.5rem' }}>
              End Date *
            </label>
            <input
              type="date"
              value={formData.endDate}
              onChange={(e) => handleChange('endDate', e.target.value)}
              style={errors.endDate ? errorInputStyle : inputStyle}
            />
            {errors.endDate && (
              <p style={{ color: colors.error, fontSize: '0.8rem', margin: '0.25rem 0 0 0' }}>
                {errors.endDate}
              </p>
            )}
          </div>
        </div>

        {/* Alert Threshold */}
        <div style={{ marginBottom: '1.5rem' }}>
          <label style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', display: 'block', marginBottom: '0.5rem' }}>
            Alert Threshold (%) - {formData.alertThreshold}%
          </label>
          <input
            type="range"
            min="0"
            max="100"
            step="5"
            value={formData.alertThreshold}
            onChange={(e) => handleChange('alertThreshold', e.target.value)}
            style={{ width: '100%', marginBottom: '0.5rem' }}
          />
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span style={{ ...textStyles.caption(colors.text.secondary) }}>0%</span>
            <span style={{ ...textStyles.caption(colors.text.secondary) }}>50%</span>
            <span style={{ ...textStyles.caption(colors.text.secondary) }}>100%</span>
          </div>
        </div>

        {/* Description */}
        <div style={{ marginBottom: '2rem' }}>
          <label style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', display: 'block', marginBottom: '0.5rem' }}>
            Description (Optional)
          </label>
          <textarea
            value={formData.description}
            onChange={(e) => handleChange('description', e.target.value)}
            placeholder="Budget description or notes..."
            rows="3"
            style={{ ...inputStyle, resize: 'vertical', minHeight: '80px' }}
          />
        </div>

        {/* Action Buttons */}
        <div style={{ 
          display: 'flex', 
          gap: '1rem',
          justifyContent: 'flex-end',
          flexDirection: isMobile ? 'column' : 'row'
        }}>
          <button
            type="button"
            onClick={onCancel}
            style={secondaryButtonStyle}
            onMouseEnter={(e) => {
              e.target.style.backgroundColor = colors.gray[100];
            }}
            onMouseLeave={(e) => {
              e.target.style.backgroundColor = 'transparent';
            }}
          >
            Cancel
          </button>
          <button
            type="submit"
            style={primaryButtonStyle}
            onMouseEnter={(e) => {
              e.target.style.backgroundColor = colors.primary[600];
            }}
            onMouseLeave={(e) => {
              e.target.style.backgroundColor = colors.primary[500];
            }}
          >
            {budget ? 'Update Budget' : 'Create Budget'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default BudgetForm;