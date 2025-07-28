import React from "react";
import { colors, getButtonStyle } from "../styles/colors";
import { textStyles } from "../styles/typography";

const GranularitySelector = ({ 
  selectedGranularity, 
  onGranularityChange, 
  dateRange,
  isMobile = false 
}) => {
  const granularityOptions = [
    { 
      value: "daily", 
      label: "Daily", 
      icon: "📅",
      description: "Daily cost breakdown"
    },
    { 
      value: "weekly", 
      label: "Weekly", 
      icon: "📊",
      description: "Weekly cost aggregation"
    },
    { 
      value: "monthly", 
      label: "Monthly", 
      icon: "🗓️",
      description: "Monthly cost totals"
    }
  ];

  // Calculate if granularity makes sense for date range
  const getOptionStatus = (granularity) => {
    if (!dateRange || !dateRange.start || !dateRange.end) return "enabled";
    
    const start = new Date(dateRange.start);
    const end = new Date(dateRange.end);
    const daysDiff = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
    
    switch (granularity) {
      case "daily":
        if (daysDiff > 90) return "warning"; // Too many data points
        return "enabled";
      case "weekly":
        if (daysDiff < 14) return "warning"; // Too few data points
        return "enabled";
      case "monthly":
        if (daysDiff < 60) return "warning"; // Too few data points
        return "enabled";
      default:
        return "enabled";
    }
  };

  const getButtonVariant = (option) => {
    if (selectedGranularity === option.value) return "primary";
    
    const status = getOptionStatus(option.value);
    if (status === "warning") return "secondary";
    
    return "outline";
  };

  const getButtonStyles = (option) => {
    const variant = getButtonVariant(option);
    const baseStyle = getButtonStyle(variant);
    
    if (selectedGranularity === option.value) {
      return {
        ...baseStyle,
        backgroundColor: colors.primary[500],
        color: colors.white,
        borderColor: colors.primary[500],
      };
    }
    
    const status = getOptionStatus(option.value);
    if (status === "warning") {
      return {
        ...baseStyle,
        backgroundColor: colors.gray[100],
        color: colors.text.secondary,
        borderColor: colors.gray[300],
        opacity: 0.7,
      };
    }
    
    return {
      ...baseStyle,
      backgroundColor: colors.white,
      color: colors.text.primary,
      borderColor: colors.gray[300],
      '&:hover': {
        backgroundColor: colors.gray[50],
        borderColor: colors.primary[300],
      }
    };
  };

  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        gap: "0.75rem",
      }}
    >
      <div>
        <h4
          style={{
            ...textStyles.label(colors.text.primary),
            marginBottom: "0.5rem",
            fontWeight: 600,
          }}
        >
          📊 Data Granularity
        </h4>
        <p
          style={{
            ...textStyles.caption(colors.text.secondary),
            margin: 0,
          }}
        >
          Choose how to aggregate cost data over time
        </p>
      </div>

      <div
        style={{
          display: "flex",
          flexDirection: isMobile ? "column" : "row",
          gap: "0.5rem",
          padding: "0.25rem",
          backgroundColor: colors.gray[50],
          borderRadius: "8px",
          border: `1px solid ${colors.gray[200]}`,
        }}
      >
        {granularityOptions.map((option) => {
          const isSelected = selectedGranularity === option.value;
          const status = getOptionStatus(option.value);
          
          return (
            <button
              key={option.value}
              onClick={() => onGranularityChange(option.value)}
              disabled={status === "disabled"}
              style={{
                flex: 1,
                padding: isMobile ? "0.75rem" : "0.5rem 1rem",
                border: "1px solid",
                borderRadius: "6px",
                cursor: status === "disabled" ? "not-allowed" : "pointer",
                transition: "all 0.2s ease",
                display: "flex",
                alignItems: "center",
                justifyContent: isMobile ? "flex-start" : "center",
                gap: "0.5rem",
                minHeight: "44px", // Touch-friendly
                backgroundColor: isSelected ? colors.primary[500] : colors.white,
                color: isSelected ? colors.white : colors.text.primary,
                borderColor: isSelected ? colors.primary[500] : colors.gray[300],
                ...(status === "warning" && {
                  backgroundColor: colors.gray[100],
                  color: colors.text.secondary,
                  opacity: 0.7,
                }),
              }}
              onMouseEnter={(e) => {
                if (!isSelected && status !== "warning") {
                  e.target.style.backgroundColor = colors.gray[50];
                  e.target.style.borderColor = colors.primary[300];
                }
              }}
              onMouseLeave={(e) => {
                if (!isSelected && status !== "warning") {
                  e.target.style.backgroundColor = colors.white;
                  e.target.style.borderColor = colors.gray[300];
                }
              }}
            >
              <span style={{ fontSize: "1rem" }}>{option.icon}</span>
              <div style={{ 
                display: "flex", 
                flexDirection: "column", 
                alignItems: isMobile ? "flex-start" : "center" 
              }}>
                <span
                  style={{
                    fontWeight: isSelected ? 600 : 500,
                    fontSize: "0.875rem",
                  }}
                >
                  {option.label}
                </span>
                {isMobile && (
                  <span
                    style={{
                      fontSize: "0.75rem",
                      opacity: 0.8,
                      marginTop: "0.125rem",
                    }}
                  >
                    {option.description}
                  </span>
                )}
              </div>
              {status === "warning" && (
                <span 
                  style={{ 
                    fontSize: "0.75rem", 
                    marginLeft: "0.25rem" 
                  }}
                  title="May not be optimal for selected date range"
                >
                  ⚠️
                </span>
              )}
            </button>
          );
        })}
      </div>
      
      {/* Help text for current selection */}
      <div
        style={{
          fontSize: "0.75rem",
          color: colors.text.secondary,
          fontStyle: "italic",
        }}
      >
        {selectedGranularity === "daily" && "📈 Best for short-term analysis (≤ 90 days)"}
        {selectedGranularity === "weekly" && "📊 Best for medium-term trends (2 weeks - 6 months)"}
        {selectedGranularity === "monthly" && "🗓️ Best for long-term patterns (≥ 2 months)"}
      </div>
    </div>
  );
};

export default GranularitySelector;