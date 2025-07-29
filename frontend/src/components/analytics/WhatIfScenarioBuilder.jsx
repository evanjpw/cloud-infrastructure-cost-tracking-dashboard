import React, { useState, useEffect } from 'react';
import { Line } from 'react-chartjs-2';
import { colors, getCardStyle } from '../../styles/colors';
import { textStyles } from '../../styles/typography';
import { 
  createWhatIfScenario, 
  compareScenarios, 
  generateScenarioTemplate,
  formatScenarioForChart,
  SCENARIO_TYPES 
} from '../../utils/whatIfModeling';

const WhatIfScenarioBuilder = ({ 
  costData = [], 
  onScenariosChange,
  isMobile = false 
}) => {
  const [scenarios, setScenarios] = useState([]);
  const [currentScenario, setCurrentScenario] = useState({
    type: SCENARIO_TYPES.INSTANCE_RIGHTSIZING,
    name: '',
    description: '',
    changes: {},
    timeHorizon: 30
  });
  const [isBuilding, setIsBuilding] = useState(false);
  const [selectedTemplate, setSelectedTemplate] = useState('');
  const [showComparison, setShowComparison] = useState(false);

  useEffect(() => {
    onScenariosChange?.(scenarios);
  }, [scenarios, onScenariosChange]);

  const scenarioTypeOptions = [
    { 
      value: SCENARIO_TYPES.INSTANCE_RIGHTSIZING, 
      label: 'Instance Right-sizing', 
      icon: '📏',
      description: 'Optimize instance sizes based on utilization'
    },
    { 
      value: SCENARIO_TYPES.RESERVED_INSTANCES, 
      label: 'Reserved Instances', 
      icon: '🎯',
      description: 'Purchase RIs for predictable workloads'
    },
    { 
      value: SCENARIO_TYPES.AUTO_SCALING, 
      label: 'Auto Scaling', 
      icon: '📈',
      description: 'Implement dynamic scaling policies'
    },
    { 
      value: SCENARIO_TYPES.REGION_MIGRATION, 
      label: 'Region Migration', 
      icon: '🌍',
      description: 'Move workloads to different regions'
    },
    { 
      value: SCENARIO_TYPES.SERVICE_MIGRATION, 
      label: 'Service Migration', 
      icon: '🔄',
      description: 'Migrate to different cloud services'
    },
    { 
      value: SCENARIO_TYPES.MULTI_CLOUD, 
      label: 'Multi-Cloud', 
      icon: '☁️',
      description: 'Distribute across multiple cloud providers'
    }
  ];

  const templateOptions = [
    { value: '', label: 'Custom Scenario' },
    { value: 'beginner_rightsizing', label: 'Beginner: Right-sizing (25% reduction)' },
    { value: 'intermediate_reserved_instances', label: 'Intermediate: Reserved Instances' },
    { value: 'advanced_multi_cloud', label: 'Advanced: Multi-Cloud Strategy' }
  ];

  const handleTemplateSelect = (templateKey) => {
    setSelectedTemplate(templateKey);
    
    if (templateKey) {
      const template = generateScenarioTemplate(templateKey);
      if (template) {
        setCurrentScenario({
          ...template,
          timeHorizon: 30
        });
      }
    } else {
      // Reset to custom
      setCurrentScenario({
        type: SCENARIO_TYPES.INSTANCE_RIGHTSIZING,
        name: '',
        description: '',
        changes: {},
        timeHorizon: 30
      });
    }
  };

  const handleScenarioChange = (field, value) => {
    setCurrentScenario(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleChangesUpdate = (changeKey, changeValue) => {
    setCurrentScenario(prev => ({
      ...prev,
      changes: {
        ...prev.changes,
        [changeKey]: changeValue
      }
    }));
  };

  const buildScenario = async () => {
    if (!currentScenario.name.trim() || costData.length === 0) {
      alert('Please provide a scenario name and ensure cost data is available');
      return;
    }

    setIsBuilding(true);
    
    try {
      const result = createWhatIfScenario(costData, currentScenario);
      
      if (result.error) {
        alert(`Error creating scenario: ${result.error}`);
      } else {
        setScenarios(prev => [...prev, result.scenario]);
        
        // Reset form
        setCurrentScenario({
          type: SCENARIO_TYPES.INSTANCE_RIGHTSIZING,
          name: '',
          description: '',
          changes: {},
          timeHorizon: 30
        });
        setSelectedTemplate('');
      }
    } catch (error) {
      console.error('Failed to build scenario:', error);
      alert('Failed to create scenario. Please try again.');
    } finally {
      setIsBuilding(false);
    }
  };

  const removeScenario = (scenarioId) => {
    setScenarios(prev => prev.filter(s => s.id !== scenarioId));
  };

  const renderScenarioForm = () => {
    const selectedType = scenarioTypeOptions.find(opt => opt.value === currentScenario.type);

    return (
      <div style={{ ...getCardStyle(), padding: '1.5rem', marginBottom: '1.5rem' }}>
        <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: '1.5rem' }}>
          🎯 Build What-If Scenario
        </h3>

        {/* Template Selection */}
        <div style={{ marginBottom: '1.5rem' }}>
          <label style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', display: 'block', marginBottom: '0.5rem' }}>
            Start with Template
          </label>
          <select
            value={selectedTemplate}
            onChange={(e) => handleTemplateSelect(e.target.value)}
            style={{
              width: '100%',
              padding: '0.75rem',
              border: `1px solid ${colors.gray[300]}`,
              borderRadius: '6px',
              fontSize: '0.9rem',
              backgroundColor: colors.white
            }}
          >
            {templateOptions.map(option => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        {/* Basic Information */}
        <div style={{ display: 'grid', gridTemplateColumns: isMobile ? '1fr' : '1fr 1fr', gap: '1rem', marginBottom: '1.5rem' }}>
          <div>
            <label style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', display: 'block', marginBottom: '0.5rem' }}>
              Scenario Name *
            </label>
            <input
              type="text"
              value={currentScenario.name}
              onChange={(e) => handleScenarioChange('name', e.target.value)}
              placeholder="e.g., Downsize Development Environment"
              style={{
                width: '100%',
                padding: '0.75rem',
                border: `1px solid ${colors.gray[300]}`,
                borderRadius: '6px',
                fontSize: '0.9rem'
              }}
            />
          </div>
          
          <div>
            <label style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', display: 'block', marginBottom: '0.5rem' }}>
              Time Horizon (days)
            </label>
            <input
              type="number"
              value={currentScenario.timeHorizon}
              onChange={(e) => handleScenarioChange('timeHorizon', parseInt(e.target.value))}
              min="7"
              max="365"
              style={{
                width: '100%',
                padding: '0.75rem',
                border: `1px solid ${colors.gray[300]}`,
                borderRadius: '6px',
                fontSize: '0.9rem'
              }}
            />
          </div>
        </div>

        {/* Scenario Type */}
        <div style={{ marginBottom: '1.5rem' }}>
          <label style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', display: 'block', marginBottom: '1rem' }}>
            Scenario Type
          </label>
          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: isMobile ? '1fr' : 'repeat(auto-fit, minmax(200px, 1fr))',
            gap: '0.75rem'
          }}>
            {scenarioTypeOptions.map(option => (
              <div
                key={option.value}
                onClick={() => handleScenarioChange('type', option.value)}
                style={{
                  padding: '1rem',
                  border: `2px solid ${currentScenario.type === option.value ? colors.primary[500] : colors.gray[300]}`,
                  borderRadius: '6px',
                  cursor: 'pointer',
                  transition: 'all 0.2s ease',
                  backgroundColor: currentScenario.type === option.value ? colors.primary[50] : colors.white
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}>
                  <span style={{ fontSize: '1.5rem' }}>{option.icon}</span>
                  <span style={{ ...textStyles.body(colors.text.primary), fontWeight: '600' }}>
                    {option.label}
                  </span>
                </div>
                <p style={{ ...textStyles.caption(colors.text.secondary), margin: 0 }}>
                  {option.description}
                </p>
              </div>
            ))}
          </div>
        </div>

        {/* Description */}
        <div style={{ marginBottom: '1.5rem' }}>
          <label style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', display: 'block', marginBottom: '0.5rem' }}>
            Description
          </label>
          <textarea
            value={currentScenario.description}
            onChange={(e) => handleScenarioChange('description', e.target.value)}
            placeholder="Describe the scenario and expected outcomes..."
            rows="3"
            style={{
              width: '100%',
              padding: '0.75rem',
              border: `1px solid ${colors.gray[300]}`,
              borderRadius: '6px',
              fontSize: '0.9rem',
              resize: 'vertical'
            }}
          />
        </div>

        {/* Scenario-specific Configuration */}
        {renderScenarioSpecificConfig()}

        {/* Actions */}
        <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '1.5rem' }}>
          <button
            onClick={buildScenario}
            disabled={isBuilding || !currentScenario.name.trim()}
            style={{
              padding: '0.75rem 1.5rem',
              backgroundColor: isBuilding ? colors.primary[300] : colors.primary[500],
              color: colors.white,
              border: 'none',
              borderRadius: '6px',
              fontSize: '0.9rem',
              fontWeight: '600',
              cursor: isBuilding ? 'not-allowed' : 'pointer',
              transition: 'all 0.2s ease'
            }}
          >
            {isBuilding ? 'Building...' : '🚀 Build Scenario'}
          </button>
        </div>
      </div>
    );
  };

  const renderScenarioSpecificConfig = () => {
    switch (currentScenario.type) {
      case SCENARIO_TYPES.INSTANCE_RIGHTSIZING:
        return (
          <div style={{ marginBottom: '1.5rem' }}>
            <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', marginBottom: '1rem' }}>
              Right-sizing Configuration
            </h4>
            <div style={{ display: 'grid', gridTemplateColumns: isMobile ? '1fr' : '1fr 1fr', gap: '1rem' }}>
              <div>
                <label style={{ ...textStyles.caption(colors.text.secondary), display: 'block', marginBottom: '0.25rem' }}>
                  Size Change
                </label>
                <select
                  value={currentScenario.changes.size_change || 'downsize_25'}
                  onChange={(e) => handleChangesUpdate('size_change', e.target.value)}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: `1px solid ${colors.gray[300]}`,
                    borderRadius: '4px',
                    fontSize: '0.8rem'
                  }}
                >
                  <option value="downsize_50">Downsize 50%</option>
                  <option value="downsize_25">Downsize 25%</option>
                  <option value="upsize_25">Upsize 25%</option>
                  <option value="upsize_50">Upsize 50%</option>
                </select>
              </div>
              <div>
                <label style={{ ...textStyles.caption(colors.text.secondary), display: 'block', marginBottom: '0.25rem' }}>
                  Implementation Delay (days)
                </label>
                <input
                  type="number"
                  value={currentScenario.changes.implementation_delay || 7}
                  onChange={(e) => handleChangesUpdate('implementation_delay', parseInt(e.target.value))}
                  min="0"
                  max="30"
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: `1px solid ${colors.gray[300]}`,
                    borderRadius: '4px',
                    fontSize: '0.8rem'
                  }}
                />
              </div>
            </div>
          </div>
        );

      case SCENARIO_TYPES.RESERVED_INSTANCES:
        return (
          <div style={{ marginBottom: '1.5rem' }}>
            <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', marginBottom: '1rem' }}>
              Reserved Instance Configuration
            </h4>
            <div style={{ display: 'grid', gridTemplateColumns: isMobile ? '1fr' : '1fr 1fr', gap: '1rem' }}>
              <div>
                <label style={{ ...textStyles.caption(colors.text.secondary), display: 'block', marginBottom: '0.25rem' }}>
                  Commitment Type
                </label>
                <select
                  value={currentScenario.changes.commitment || '1_year_partial'}
                  onChange={(e) => handleChangesUpdate('commitment', e.target.value)}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: `1px solid ${colors.gray[300]}`,
                    borderRadius: '4px',
                    fontSize: '0.8rem'
                  }}
                >
                  <option value="1_year_partial">1 Year Partial Upfront</option>
                  <option value="1_year_all">1 Year All Upfront</option>
                  <option value="3_year_partial">3 Year Partial Upfront</option>
                  <option value="3_year_all">3 Year All Upfront</option>
                </select>
              </div>
              <div>
                <label style={{ ...textStyles.caption(colors.text.secondary), display: 'block', marginBottom: '0.25rem' }}>
                  Coverage (%)
                </label>
                <input
                  type="number"
                  value={(currentScenario.changes.coverage || 0.8) * 100}
                  onChange={(e) => handleChangesUpdate('coverage', parseFloat(e.target.value) / 100)}
                  min="10"
                  max="100"
                  step="5"
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: `1px solid ${colors.gray[300]}`,
                    borderRadius: '4px',
                    fontSize: '0.8rem'
                  }}
                />
              </div>
            </div>
          </div>
        );

      default:
        return (
          <div style={{ marginBottom: '1.5rem' }}>
            <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', marginBottom: '1rem' }}>
              Custom Configuration
            </h4>
            <div style={{ display: 'grid', gridTemplateColumns: isMobile ? '1fr' : '1fr 1fr', gap: '1rem' }}>
              <div>
                <label style={{ ...textStyles.caption(colors.text.secondary), display: 'block', marginBottom: '0.25rem' }}>
                  Cost Multiplier
                </label>
                <input
                  type="number"
                  value={currentScenario.changes.cost_multiplier || 1.0}
                  onChange={(e) => handleChangesUpdate('cost_multiplier', parseFloat(e.target.value))}
                  min="0.1"
                  max="3.0"
                  step="0.1"
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: `1px solid ${colors.gray[300]}`,
                    borderRadius: '4px',
                    fontSize: '0.8rem'
                  }}
                />
              </div>
              <div>
                <label style={{ ...textStyles.caption(colors.text.secondary), display: 'block', marginBottom: '0.25rem' }}>
                  Fixed Cost Change ($)
                </label>
                <input
                  type="number"
                  value={currentScenario.changes.fixed_cost_change || 0}
                  onChange={(e) => handleChangesUpdate('fixed_cost_change', parseFloat(e.target.value))}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: `1px solid ${colors.gray[300]}`,
                    borderRadius: '4px',
                    fontSize: '0.8rem'
                  }}
                />
              </div>
            </div>
          </div>
        );
    }
  };

  const renderScenarioList = () => {
    if (scenarios.length === 0) {
      return (
        <div style={{
          ...getCardStyle(),
          padding: '2rem',
          textAlign: 'center',
          backgroundColor: colors.background.secondary
        }}>
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📊</div>
          <p style={{ ...textStyles.body(colors.text.secondary) }}>
            No scenarios created yet. Build your first what-if scenario above.
          </p>
        </div>
      );
    }

    return (
      <div style={{ ...getCardStyle(), padding: '1.5rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <h3 style={{ ...textStyles.cardTitle(colors.text.primary), margin: 0 }}>
            📈 Scenarios ({scenarios.length})
          </h3>
          {scenarios.length > 1 && (
            <button
              onClick={() => setShowComparison(!showComparison)}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: showComparison ? colors.primary[500] : 'transparent',
                color: showComparison ? colors.white : colors.primary[500],
                border: `1px solid ${colors.primary[500]}`,
                borderRadius: '6px',
                fontSize: '0.8rem',
                cursor: 'pointer'
              }}
            >
              {showComparison ? 'Hide' : 'Show'} Comparison
            </button>
          )}
        </div>

        {showComparison && scenarios.length > 1 && renderComparison()}

        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {scenarios.map((scenario, index) => (
            <div
              key={scenario.id}
              style={{
                padding: '1rem',
                border: `1px solid ${colors.gray[200]}`,
                borderRadius: '6px',
                backgroundColor: colors.white
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                <div>
                  <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', margin: '0 0 0.5rem 0' }}>
                    {scenario.name}
                  </h4>
                  <p style={{ ...textStyles.caption(colors.text.secondary), margin: 0 }}>
                    {scenario.description}
                  </p>
                </div>
                <button
                  onClick={() => removeScenario(scenario.id)}
                  style={{
                    padding: '0.25rem 0.5rem',
                    backgroundColor: 'transparent',
                    color: colors.error,
                    border: `1px solid ${colors.error}`,
                    borderRadius: '4px',
                    fontSize: '0.75rem',
                    cursor: 'pointer'
                  }}
                >
                  Remove
                </button>
              </div>

              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(120px, 1fr))',
                gap: '1rem',
                marginBottom: '1rem'
              }}>
                <div style={{ textAlign: 'center' }}>
                  <p style={{ ...textStyles.caption(colors.text.secondary), margin: '0 0 0.25rem 0' }}>
                    Cost Impact
                  </p>
                  <p style={{ 
                    ...textStyles.body(
                      scenario.impact.totalPercentageChange < 0 ? colors.success : colors.error
                    ), 
                    fontWeight: '600', 
                    margin: 0 
                  }}>
                    {scenario.impact.totalPercentageChange > 0 ? '+' : ''}
                    {scenario.impact.totalPercentageChange.toFixed(1)}%
                  </p>
                </div>
                <div style={{ textAlign: 'center' }}>
                  <p style={{ ...textStyles.caption(colors.text.secondary), margin: '0 0 0.25rem 0' }}>
                    Risk Level
                  </p>
                  <p style={{ 
                    ...textStyles.body(
                      scenario.impact.riskAssessment.level === 'low' ? colors.success :
                      scenario.impact.riskAssessment.level === 'medium' ? colors.warning : colors.error
                    ), 
                    fontWeight: '600', 
                    margin: 0 
                  }}>
                    {scenario.impact.riskAssessment.level.charAt(0).toUpperCase() + scenario.impact.riskAssessment.level.slice(1)}
                  </p>
                </div>
                <div style={{ textAlign: 'center' }}>
                  <p style={{ ...textStyles.caption(colors.text.secondary), margin: '0 0 0.25rem 0' }}>
                    Total Change
                  </p>
                  <p style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', margin: 0 }}>
                    ${Math.abs(scenario.impact.totalCostDifference).toLocaleString()}
                  </p>
                </div>
              </div>

              <div style={{ height: '200px' }}>
                <Line 
                  data={formatScenarioForChart(scenario, true)} 
                  options={{
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } },
                    scales: {
                      x: { display: false },
                      y: { 
                        beginAtZero: true,
                        ticks: { callback: (value) => `$${(value / 1000).toFixed(0)}k` }
                      }
                    }
                  }} 
                />
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  };

  const renderComparison = () => {
    const comparison = compareScenarios(scenarios);
    
    if (comparison.error) {
      return (
        <div style={{ padding: '1rem', backgroundColor: colors.error + '20', borderRadius: '6px', marginBottom: '1rem' }}>
          <p style={{ ...textStyles.caption(colors.error), margin: 0 }}>
            Error comparing scenarios: {comparison.error}
          </p>
        </div>
      );
    }

    return (
      <div style={{ 
        padding: '1rem', 
        backgroundColor: colors.background.secondary, 
        borderRadius: '6px', 
        marginBottom: '1.5rem' 
      }}>
        <h4 style={{ ...textStyles.body(colors.text.primary), fontWeight: '600', marginBottom: '1rem' }}>
          📊 Scenario Comparison
        </h4>
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
          gap: '1rem'
        }}>
          <div style={{ textAlign: 'center' }}>
            <p style={{ ...textStyles.caption(colors.text.secondary), margin: '0 0 0.5rem 0' }}>
              Most Cost Effective
            </p>
            <p style={{ ...textStyles.body(colors.success), fontWeight: '600', margin: 0 }}>
              {comparison.comparison.bestScenario.name}
            </p>
            <p style={{ ...textStyles.caption(colors.text.secondary), margin: 0 }}>
              ${comparison.comparison.bestScenario.totalCost.toLocaleString()}
            </p>
          </div>
          <div style={{ textAlign: 'center' }}>
            <p style={{ ...textStyles.caption(colors.text.secondary), margin: '0 0 0.5rem 0' }}>
              Highest Cost
            </p>
            <p style={{ ...textStyles.body(colors.error), fontWeight: '600', margin: 0 }}>
              {comparison.comparison.worstScenario.name}
            </p>
            <p style={{ ...textStyles.caption(colors.text.secondary), margin: 0 }}>
              ${comparison.comparison.worstScenario.totalCost.toLocaleString()}
            </p>
          </div>
          <div style={{ textAlign: 'center' }}>
            <p style={{ ...textStyles.caption(colors.text.secondary), margin: '0 0 0.5rem 0' }}>
              Potential Savings
            </p>
            <p style={{ ...textStyles.body(colors.primary[500]), fontWeight: '600', margin: 0 }}>
              ${(comparison.comparison.worstScenario.totalCost - comparison.comparison.bestScenario.totalCost).toLocaleString()}
            </p>
            <p style={{ ...textStyles.caption(colors.text.secondary), margin: 0 }}>
              vs worst case
            </p>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
      {renderScenarioForm()}
      {renderScenarioList()}
    </div>
  );
};

export default WhatIfScenarioBuilder;