import React, { useState } from 'react';
import { colors, getCardStyle } from '../styles/colors';
import { textStyles } from '../styles/typography';

const GroupBySelector = ({ 
  selectedGroupBy = "service", 
  onGroupByChange, 
  data = [],
  isMobile = false 
}) => {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  const groupByOptions = [
    { 
      value: "service", 
      label: "Service", 
      icon: "🔧",
      description: "Group costs by cloud service (EC2, S3, RDS, etc.)"
    },
    { 
      value: "team", 
      label: "Team", 
      icon: "👥",
      description: "Group costs by team ownership"
    },
    { 
      value: "provider", 
      label: "Provider", 
      icon: "☁️",
      description: "Group costs by cloud provider (AWS, Azure, GCP)"
    },
    { 
      value: "region", 
      label: "Region", 
      icon: "🌍",
      description: "Group costs by geographic region"
    },
    { 
      value: "environment", 
      label: "Environment", 
      icon: "🏗️",
      description: "Group costs by environment (prod, staging, dev)"
    },
    { 
      value: "resourceType", 
      label: "Resource Type", 
      icon: "📦",
      description: "Group costs by resource category"
    }
  ];

  const selectedOption = groupByOptions.find(option => option.value === selectedGroupBy) || groupByOptions[0];

  const handleOptionSelect = (option) => {
    onGroupByChange(option.value);
    setIsDropdownOpen(false);
  };

  const getGroupedData = () => {
    if (!data || data.length === 0) return {};

    const grouped = {};
    
    data.forEach(item => {
      let groupKey;
      
      switch (selectedGroupBy) {
        case "service":
          groupKey = item.service || item.serviceName || "Unknown Service";
          break;
        case "team":
          groupKey = item.team || "Unknown Team";
          break;
        case "provider":
          // Extract provider from service name or use a mapping
          if (item.service) {
            if (item.service.toLowerCase().includes("ec2") || 
                item.service.toLowerCase().includes("s3") || 
                item.service.toLowerCase().includes("rds") ||
                item.service.toLowerCase().includes("lambda")) {
              groupKey = "AWS";
            } else if (item.service.toLowerCase().includes("vm") || 
                       item.service.toLowerCase().includes("blob") || 
                       item.service.toLowerCase().includes("sql")) {
              groupKey = "Azure";
            } else if (item.service.toLowerCase().includes("compute") || 
                       item.service.toLowerCase().includes("storage") || 
                       item.service.toLowerCase().includes("cloud")) {
              groupKey = "GCP";
            } else {
              groupKey = "Unknown Provider";
            }
          } else {
            groupKey = "Unknown Provider";
          }
          break;
        case "region":
          groupKey = item.region || "Unknown Region";
          break;
        case "environment":
          groupKey = item.environment || "Unknown Environment";
          break;
        case "resourceType":
          // Categorize by resource type based on service name
          if (item.service) {
            if (item.service.toLowerCase().includes("compute") || 
                item.service.toLowerCase().includes("ec2") || 
                item.service.toLowerCase().includes("vm")) {
              groupKey = "Compute";
            } else if (item.service.toLowerCase().includes("storage") || 
                       item.service.toLowerCase().includes("s3") || 
                       item.service.toLowerCase().includes("blob")) {
              groupKey = "Storage";
            } else if (item.service.toLowerCase().includes("database") || 
                       item.service.toLowerCase().includes("rds") || 
                       item.service.toLowerCase().includes("sql")) {
              groupKey = "Database";
            } else if (item.service.toLowerCase().includes("network") || 
                       item.service.toLowerCase().includes("vpc") || 
                       item.service.toLowerCase().includes("cdn")) {
              groupKey = "Networking";
            } else if (item.service.toLowerCase().includes("lambda") || 
                       item.service.toLowerCase().includes("function")) {
              groupKey = "Serverless";
            } else {
              groupKey = "Other";
            }
          } else {
            groupKey = "Unknown Type";
          }
          break;
        default:
          groupKey = "Unknown";
      }

      if (!grouped[groupKey]) {
        grouped[groupKey] = {
          name: groupKey,
          totalCost: 0,
          count: 0,
          items: []
        };
      }

      grouped[groupKey].totalCost += item.totalCost || 0;
      grouped[groupKey].count += 1;
      grouped[groupKey].items.push(item);
    });

    return grouped;
  };

  const groupedData = getGroupedData();
  const groupedArray = Object.values(groupedData).sort((a, b) => b.totalCost - a.totalCost);

  const containerStyle = {
    position: 'relative',
    display: 'inline-block',
    minWidth: isMobile ? '100%' : '250px',
  };

  const buttonStyle = {
    width: '100%',
    padding: '0.75rem 1rem',
    backgroundColor: colors.white,
    border: `2px solid ${colors.primary[300]}`,
    borderRadius: '8px',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    fontSize: '0.9rem',
    fontWeight: '600',
    color: colors.text.primary,
    transition: 'all 0.2s ease',
    boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
  };

  const dropdownStyle = {
    position: 'absolute',
    top: '100%',
    left: 0,
    right: 0,
    backgroundColor: colors.white,
    border: `1px solid ${colors.primary[300]}`,
    borderRadius: '8px',
    marginTop: '0.25rem',
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
    zIndex: 1000,
    maxHeight: '300px',
    overflowY: 'auto',
  };

  const optionStyle = {
    padding: '0.75rem 1rem',
    cursor: 'pointer',
    borderBottom: `1px solid ${colors.gray[200]}`,
    transition: 'background-color 0.2s ease',
  };

  return (
    <div style={{ marginBottom: '1.5rem' }}>
      <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: '1rem' }}>
        📊 Group Data By
      </h3>
      
      <div style={{ 
        display: 'grid', 
        gridTemplateColumns: isMobile ? '1fr' : '250px 1fr',
        gap: '1.5rem',
        alignItems: 'start'
      }}>
        {/* Group By Selector */}
        <div style={containerStyle}>
          <button
            style={buttonStyle}
            onClick={() => setIsDropdownOpen(!isDropdownOpen)}
            onMouseEnter={(e) => {
              e.target.style.borderColor = colors.primary[500];
              e.target.style.boxShadow = '0 4px 8px rgba(33, 150, 243, 0.2)';
            }}
            onMouseLeave={(e) => {
              e.target.style.borderColor = colors.primary[300];
              e.target.style.boxShadow = '0 2px 4px rgba(0, 0, 0, 0.1)';
            }}
          >
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <span style={{ fontSize: '1.1em' }}>{selectedOption.icon}</span>
              <span>{selectedOption.label}</span>
            </span>
            <span style={{ transform: isDropdownOpen ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform 0.2s ease' }}>
              ▼
            </span>
          </button>

          {isDropdownOpen && (
            <div style={dropdownStyle}>
              {groupByOptions.map((option, index) => (
                <div
                  key={option.value}
                  style={{
                    ...optionStyle,
                    backgroundColor: option.value === selectedGroupBy ? colors.primary[50] : 'transparent',
                    borderBottom: index === groupByOptions.length - 1 ? 'none' : `1px solid ${colors.gray[200]}`
                  }}
                  onClick={() => handleOptionSelect(option)}
                  onMouseEnter={(e) => {
                    if (option.value !== selectedGroupBy) {
                      e.target.style.backgroundColor = colors.gray[50];
                    }
                  }}
                  onMouseLeave={(e) => {
                    if (option.value !== selectedGroupBy) {
                      e.target.style.backgroundColor = 'transparent';
                    }
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                    <span style={{ fontSize: '1.1em' }}>{option.icon}</span>
                    <div>
                      <div style={{ fontWeight: '600', color: colors.text.primary }}>
                        {option.label}
                      </div>
                      <div style={{ ...textStyles.caption(colors.text.secondary), marginTop: '0.25rem' }}>
                        {option.description}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Results Summary */}
        <div style={{
          ...getCardStyle(),
          padding: '1rem',
          backgroundColor: colors.primary[25],
          border: `1px solid ${colors.primary[200]}`
        }}>
          <div style={{ marginBottom: '0.75rem' }}>
            <span style={{ ...textStyles.body(colors.text.primary), fontWeight: '600' }}>
              📈 Grouped by {selectedOption.label}: {Object.keys(groupedData).length} groups
            </span>
          </div>
          
          {groupedArray.length > 0 && (
            <div style={{ display: 'grid', gap: '0.5rem' }}>
              {groupedArray.slice(0, 3).map((group, index) => (
                <div key={group.name} style={{ 
                  display: 'flex', 
                  justifyContent: 'space-between', 
                  alignItems: 'center',
                  padding: '0.5rem',
                  backgroundColor: index === 0 ? colors.primary[100] : colors.gray[50],
                  borderRadius: '4px'
                }}>
                  <span style={{ ...textStyles.body(colors.text.primary), fontWeight: index === 0 ? '600' : '500' }}>
                    {index === 0 && '🏆 '}{group.name}
                  </span>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ ...textStyles.body(colors.text.primary), fontWeight: '600' }}>
                      ${group.totalCost.toFixed(2)}
                    </div>
                    <div style={{ ...textStyles.caption(colors.text.secondary) }}>
                      {group.count} services
                    </div>
                  </div>
                </div>
              ))}
              
              {groupedArray.length > 3 && (
                <div style={{ 
                  ...textStyles.caption(colors.text.secondary), 
                  textAlign: 'center',
                  padding: '0.5rem'
                }}>
                  +{groupedArray.length - 3} more groups
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Close dropdown when clicking outside */}
      {isDropdownOpen && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            zIndex: 999,
          }}
          onClick={() => setIsDropdownOpen(false)}
        />
      )}
    </div>
  );
};

export default GroupBySelector;