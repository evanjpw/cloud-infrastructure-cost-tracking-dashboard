import React, { useState, useEffect } from "react";
import { colors, getCardStyle, getInputStyle, getButtonStyle } from "../styles/colors";
import { textStyles } from "../styles/typography";

const DateRangePicker = ({ 
  startDate, 
  endDate, 
  onDateRangeChange, 
  granularity = "daily",
  isMobile = false 
}) => {
  const [showCustomRange, setShowCustomRange] = useState(false);
  const [selectedPreset, setSelectedPreset] = useState("custom");

  // Date preset options matching cloud console patterns
  const datePresets = [
    {
      key: "last7days",
      label: "Last 7 days",
      icon: "📅",
      getDates: () => {
        const end = new Date();
        const start = new Date();
        start.setDate(end.getDate() - 7);
        return { start: formatDate(start), end: formatDate(end) };
      }
    },
    {
      key: "last30days", 
      label: "Last 30 days",
      icon: "🗓️",
      getDates: () => {
        const end = new Date();
        const start = new Date();
        start.setDate(end.getDate() - 30);
        return { start: formatDate(start), end: formatDate(end) };
      }
    },
    {
      key: "last90days",
      label: "Last 90 days", 
      icon: "📊",
      getDates: () => {
        const end = new Date();
        const start = new Date();
        start.setDate(end.getDate() - 90);
        return { start: formatDate(start), end: formatDate(end) };
      }
    },
    {
      key: "mtd",
      label: "Month to date",
      icon: "📈",
      getDates: () => {
        const end = new Date();
        const start = new Date(end.getFullYear(), end.getMonth(), 1);
        return { start: formatDate(start), end: formatDate(end) };
      }
    },
    {
      key: "lastmonth",
      label: "Last month",
      icon: "🗃️", 
      getDates: () => {
        const end = new Date();
        end.setDate(0); // Last day of previous month
        const start = new Date(end.getFullYear(), end.getMonth(), 1);
        return { start: formatDate(start), end: formatDate(end) };
      }
    },
    {
      key: "ytd",
      label: "Year to date",
      icon: "📋",
      getDates: () => {
        const end = new Date();
        const start = new Date(end.getFullYear(), 0, 1);
        return { start: formatDate(start), end: formatDate(end) };
      }
    },
    {
      key: "custom",
      label: "Custom range",
      icon: "⚙️",
      getDates: () => ({ start: startDate, end: endDate })
    }
  ];

  // Format date for input fields
  const formatDate = (date) => {
    return date.toISOString().split('T')[0];
  };

  // Detect current preset based on start/end dates
  useEffect(() => {
    const currentRange = { start: startDate, end: endDate };
    
    for (const preset of datePresets) {
      if (preset.key === "custom") continue;
      
      const presetRange = preset.getDates();
      if (presetRange.start === currentRange.start && presetRange.end === currentRange.end) {
        setSelectedPreset(preset.key);
        setShowCustomRange(false);
        return;
      }
    }
    
    setSelectedPreset("custom");
    setShowCustomRange(true);
  }, [startDate, endDate]);

  const handlePresetSelect = (presetKey) => {
    const preset = datePresets.find(p => p.key === presetKey);
    if (!preset) return;

    setSelectedPreset(presetKey);
    
    if (presetKey === "custom") {
      setShowCustomRange(true);
    } else {
      setShowCustomRange(false);
      const { start, end } = preset.getDates();
      onDateRangeChange(start, end);
    }
  };

  const handleCustomDateSubmit = (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const newStartDate = formData.get("startDate");
    const newEndDate = formData.get("endDate");

    if (newStartDate && newEndDate) {
      onDateRangeChange(newStartDate, newEndDate);
    }
  };

  // Get recommended granularity for each preset
  const getRecommendedGranularity = (presetKey) => {
    const preset = datePresets.find(p => p.key === presetKey);
    if (!preset) return null;
    
    const { start, end } = preset.getDates();
    const startDate = new Date(start);
    const endDate = new Date(end);
    const daysDiff = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24));
    
    if (daysDiff <= 30) return "daily";
    if (daysDiff <= 180) return "weekly";
    return "monthly";
  };

  return (
    <div
      style={{
        ...getCardStyle(),
        padding: isMobile ? "1rem" : "1.5rem",
        marginBottom: "1.5rem",
      }}
    >
      <div style={{ marginBottom: "1.5rem" }}>
        <h4
          style={{
            ...textStyles.cardTitle(colors.text.primary),
            marginBottom: "0.5rem",
            display: "flex",
            alignItems: "center",
            gap: "0.5rem",
          }}
        >
          📅 Date Range
        </h4>
        <p
          style={{
            ...textStyles.caption(colors.text.secondary),
            margin: 0,
          }}
        >
          Select the time period for cost analysis
        </p>
      </div>

      {/* Preset Buttons */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: isMobile 
            ? "1fr 1fr" 
            : "repeat(auto-fit, minmax(140px, 1fr))",
          gap: "0.5rem",
          marginBottom: "1rem",
        }}
      >
        {datePresets.map((preset) => {
          const isSelected = selectedPreset === preset.key;
          const recommendedGranularity = getRecommendedGranularity(preset.key);
          const matchesCurrentGranularity = recommendedGranularity === granularity;
          
          return (
            <button
              key={preset.key}
              onClick={() => handlePresetSelect(preset.key)}
              style={{
                padding: "0.75rem 0.5rem",
                border: "1px solid",
                borderRadius: "6px",
                cursor: "pointer",
                transition: "all 0.2s ease",
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                gap: "0.25rem",
                minHeight: "60px",
                backgroundColor: isSelected ? colors.primary[500] : colors.white,
                color: isSelected ? colors.white : colors.text.primary,
                borderColor: isSelected ? colors.primary[500] : colors.gray[300],
                fontSize: "0.8rem",
                fontWeight: isSelected ? 600 : 500,
              }}
              onMouseEnter={(e) => {
                if (!isSelected) {
                  e.target.style.backgroundColor = colors.gray[50];
                  e.target.style.borderColor = colors.primary[300];
                }
              }}
              onMouseLeave={(e) => {
                if (!isSelected) {
                  e.target.style.backgroundColor = colors.white;
                  e.target.style.borderColor = colors.gray[300];
                }
              }}
            >
              <span style={{ fontSize: "1.2rem" }}>{preset.icon}</span>
              <span style={{ textAlign: "center", lineHeight: "1.2" }}>
                {preset.label}
              </span>
              {/* Granularity hint */}
              {!isSelected && recommendedGranularity && (
                <span
                  style={{
                    fontSize: "0.65rem",
                    opacity: 0.7,
                    color: matchesCurrentGranularity ? colors.success : colors.text.secondary,
                  }}
                >
                  {matchesCurrentGranularity ? "✓" : ""} {recommendedGranularity}
                </span>
              )}
            </button>
          );
        })}
      </div>

      {/* Custom Date Range Form */}
      {showCustomRange && (
        <form
          onSubmit={handleCustomDateSubmit}
          style={{
            padding: "1rem",
            backgroundColor: colors.gray[50],
            borderRadius: "8px",
            border: `1px solid ${colors.gray[200]}`,
          }}
        >
          <h5
            style={{
              ...textStyles.label(colors.text.primary),
              marginBottom: "1rem",
              fontWeight: 600,
            }}
          >
            🎯 Custom Date Range
          </h5>
          
          <div
            style={{
              display: "flex",
              flexDirection: isMobile ? "column" : "row",
              gap: "1rem",
              alignItems: isMobile ? "stretch" : "flex-end",
            }}
          >
            <div style={{ flex: 1 }}>
              <label
                style={{
                  ...textStyles.label(colors.text.primary),
                  display: "block",
                  marginBottom: "0.25rem",
                }}
              >
                Start Date:
              </label>
              <input
                type="date"
                name="startDate"
                defaultValue={startDate}
                style={{
                  ...getInputStyle(),
                  width: "100%",
                }}
              />
            </div>
            
            <div style={{ flex: 1 }}>
              <label
                style={{
                  ...textStyles.label(colors.text.primary),
                  display: "block",
                  marginBottom: "0.25rem",
                }}
              >
                End Date:
              </label>
              <input
                type="date"
                name="endDate"
                defaultValue={endDate}
                style={{
                  ...getInputStyle(),
                  width: "100%",
                }}
              />
            </div>
            
            <button
              type="submit"
              style={{
                ...getButtonStyle("primary"),
                minHeight: "44px",
                padding: "0 1.5rem",
                whiteSpace: "nowrap",
              }}
            >
              📊 Apply Range
            </button>
          </div>
        </form>
      )}

      {/* Current Selection Summary */}
      <div
        style={{
          marginTop: "1rem",
          padding: "0.75rem",
          backgroundColor: colors.primary[50],
          borderRadius: "6px",
          border: `1px solid ${colors.primary[200]}`,
        }}
      >
        <p
          style={{
            ...textStyles.caption(colors.primary[700]),
            margin: 0,
            fontWeight: 500,
          }}
        >
          📈 <strong>Active Range:</strong> {startDate} to {endDate}
          {selectedPreset !== "custom" && (
            <span style={{ marginLeft: "0.5rem", opacity: 0.8 }}>
              ({datePresets.find(p => p.key === selectedPreset)?.label})
            </span>
          )}
        </p>
      </div>
    </div>
  );
};

export default DateRangePicker;