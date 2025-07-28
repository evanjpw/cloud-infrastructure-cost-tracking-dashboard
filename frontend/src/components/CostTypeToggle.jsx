import React from 'react';
import { colors, getCardStyle } from '../styles/colors';
import { textStyles } from '../styles/typography';

const CostTypeToggle = ({ 
  selectedCostType = "actual", 
  onCostTypeChange, 
  isMobile = false 
}) => {
  const costTypes = [
    {
      value: "actual",
      label: "Actual Costs",
      icon: "💰",
      description: "Real costs incurred based on usage",
      color: colors.primary[500]
    },
    {
      value: "amortized", 
      label: "Amortized Costs",
      icon: "📊",
      description: "Costs spread evenly over reserved instance terms",
      color: colors.success
    },
    {
      value: "blended",
      label: "Blended Costs", 
      icon: "⚖️",
      description: "Average costs across consolidated billing",
      color: colors.warning
    }
  ];

  const handleToggle = (costType) => {
    onCostTypeChange(costType);
  };

  return (
    <div style={{ marginBottom: '1.5rem' }}>
      <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: '1rem' }}>
        💲 Cost Type
      </h3>
      
      <div style={{ 
        display: 'flex', 
        flexDirection: isMobile ? 'column' : 'row',
        gap: '0.75rem',
        backgroundColor: colors.gray[100],
        padding: '0.5rem',
        borderRadius: '10px',
        border: `1px solid ${colors.gray[300]}`
      }}>
        {costTypes.map((costType) => {
          const isSelected = selectedCostType === costType.value;
          
          const buttonStyle = {
            flex: 1,
            padding: '0.75rem 1rem',
            borderRadius: '6px',
            border: 'none',
            cursor: 'pointer',
            transition: 'all 0.2s ease',
            backgroundColor: isSelected ? costType.color : 'transparent',
            color: isSelected ? colors.white : colors.text.primary,
            fontWeight: isSelected ? '600' : '500',
            fontSize: '0.9rem',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '0.25rem',
            boxShadow: isSelected ? '0 2px 4px rgba(0, 0, 0, 0.1)' : 'none',
            transform: isSelected ? 'scale(1.02)' : 'scale(1)',
          };

          const hoverStyle = !isSelected ? {
            backgroundColor: colors.gray[200],
            transform: 'scale(1.01)',
          } : {};

          return (
            <button
              key={costType.value}
              style={buttonStyle}
              onClick={() => handleToggle(costType.value)}
              onMouseEnter={(e) => {
                if (!isSelected) {
                  Object.assign(e.target.style, hoverStyle);
                }
              }}
              onMouseLeave={(e) => {
                if (!isSelected) {
                  Object.assign(e.target.style, { 
                    backgroundColor: 'transparent',
                    transform: 'scale(1)'
                  });
                }
              }}
              title={costType.description}
            >
              <span style={{ fontSize: '1.2em', marginBottom: '0.25rem' }}>
                {costType.icon}
              </span>
              <span style={{ fontWeight: 'inherit' }}>
                {costType.label}
              </span>
              {!isMobile && (
                <span style={{ 
                  fontSize: '0.75rem', 
                  opacity: 0.8,
                  textAlign: 'center',
                  lineHeight: '1.2'
                }}>
                  {costType.description}
                </span>
              )}
            </button>
          );
        })}
      </div>

      {/* Cost Type Information Panel */}
      <div style={{
        marginTop: '1rem',
        padding: '1rem',
        backgroundColor: colors.gray[50],
        borderRadius: '6px',
        border: `1px solid ${colors.gray[200]}`
      }}>
        <div style={{ 
          display: 'flex', 
          alignItems: 'center', 
          gap: '0.5rem',
          marginBottom: '0.5rem'
        }}>
          <span style={{ fontSize: '1.1em' }}>
            {costTypes.find(ct => ct.value === selectedCostType)?.icon}
          </span>
          <span style={{ ...textStyles.body(colors.text.primary), fontWeight: '600' }}>
            {costTypes.find(ct => ct.value === selectedCostType)?.label}
          </span>
        </div>
        
        <p style={{ ...textStyles.body(colors.text.secondary), margin: 0 }}>
          {costTypes.find(ct => ct.value === selectedCostType)?.description}
        </p>

        {/* Additional cost type explanations */}
        {selectedCostType === "actual" && (
          <div style={{ marginTop: '0.75rem' }}>
            <p style={{ ...textStyles.caption(colors.text.secondary), margin: 0 }}>
              <strong>📈 Best for:</strong> Understanding real spending patterns and optimizing current usage
            </p>
          </div>
        )}

        {selectedCostType === "amortized" && (
          <div style={{ marginTop: '0.75rem' }}>
            <p style={{ ...textStyles.caption(colors.text.secondary), margin: 0 }}>
              <strong>📊 Best for:</strong> Budget planning and understanding true cost allocation across time periods
            </p>
          </div>
        )}

        {selectedCostType === "blended" && (
          <div style={{ marginTop: '0.75rem' }}>
            <p style={{ ...textStyles.caption(colors.text.secondary), margin: 0 }}>
              <strong>⚖️ Best for:</strong> Cross-team cost analysis in organizations with consolidated billing
            </p>
          </div>
        )}
      </div>

      {/* Simulation Note */}
      <div style={{
        marginTop: '1rem',
        padding: '0.75rem',
        backgroundColor: colors.primary[25],
        borderRadius: '6px',
        border: `1px solid ${colors.primary[200]}`
      }}>
        <p style={{ ...textStyles.caption(colors.primary[700]), margin: 0 }}>
          <strong>🎭 Simulation Note:</strong> In this demo, all cost types show the same data. 
          In a real cloud billing system, these would reflect different cost calculation methodologies.
        </p>
      </div>
    </div>
  );
};

export default CostTypeToggle;