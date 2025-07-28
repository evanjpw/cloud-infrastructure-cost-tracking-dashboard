import React from "react";
import { colors, getCardStyle } from "../styles/colors";
import { textStyles } from "../styles/typography";

const KPICard = ({ 
  title, 
  value, 
  subtitle, 
  icon, 
  trend, 
  trendValue, 
  trendLabel,
  status = "neutral",
  onClick,
  isClickable = false,
  isMobile = false 
}) => {
  const getStatusColor = (status) => {
    switch (status) {
      case "positive":
      case "good":
      case "under-budget":
        return colors.success;
      case "negative":
      case "warning":
      case "at-risk":
        return colors.warning;
      case "critical":
      case "over-budget":
        return colors.error;
      case "info":
        return colors.primary[500];
      default:
        return colors.text.primary;
    }
  };

  const getTrendColor = (trend) => {
    switch (trend) {
      case "up":
        return colors.error;
      case "down":
        return colors.success;
      default:
        return colors.text.secondary;
    }
  };

  const getTrendIcon = (trend) => {
    switch (trend) {
      case "up": return "📈";
      case "down": return "📉";
      default: return "➡️";
    }
  };

  const cardStyle = {
    ...getCardStyle(),
    padding: isMobile ? "1rem" : "1.5rem",
    cursor: isClickable ? "pointer" : "default",
    transition: "all 0.2s ease",
    background: colors.white,
    border: `1px solid ${colors.gray[200]}`,
    minHeight: isMobile ? "120px" : "140px",
    display: "flex",
    flexDirection: "column",
    justifyContent: "space-between",
    ...(isClickable && {
      '&:hover': {
        borderColor: colors.primary[300],
        transform: 'translateY(-2px)',
        boxShadow: `0 8px 25px ${colors.primary[100]}`,
      }
    })
  };

  return (
    <div
      style={cardStyle}
      onClick={isClickable ? onClick : undefined}
      onMouseEnter={isClickable ? (e) => {
        e.currentTarget.style.borderColor = colors.primary[300];
        e.currentTarget.style.transform = 'translateY(-2px)';
        e.currentTarget.style.boxShadow = `0 8px 25px ${colors.primary[100]}`;
      } : undefined}
      onMouseLeave={isClickable ? (e) => {
        e.currentTarget.style.borderColor = colors.gray[200];
        e.currentTarget.style.transform = 'translateY(0)';
        e.currentTarget.style.boxShadow = '0 2px 8px rgba(0, 0, 0, 0.1)';
      } : undefined}
    >
      {/* Header with icon and title */}
      <div style={{ 
        display: "flex", 
        alignItems: "flex-start", 
        justifyContent: "space-between",
        marginBottom: "0.75rem"
      }}>
        <div style={{ flex: 1 }}>
          <h4
            style={{
              ...textStyles.label(colors.text.secondary),
              margin: 0,
              fontSize: isMobile ? "0.75rem" : "0.875rem",
              fontWeight: 500,
              textTransform: "uppercase",
              letterSpacing: "0.5px"
            }}
          >
            {title}
          </h4>
        </div>
        {icon && (
          <span style={{ 
            fontSize: isMobile ? "1.25rem" : "1.5rem",
            opacity: 0.8
          }}>
            {icon}
          </span>
        )}
      </div>

      {/* Main value */}
      <div style={{ marginbottom: "0.5rem" }}>
        <div
          style={{
            fontSize: isMobile ? "1.5rem" : "2rem",
            fontWeight: 700,
            lineHeight: 1.2,
            color: getStatusColor(status),
            wordBreak: "break-word"
          }}
        >
          {value}
        </div>
        {subtitle && (
          <div
            style={{
              ...textStyles.caption(colors.text.secondary),
              marginTop: "0.25rem",
              fontSize: isMobile ? "0.7rem" : "0.75rem"
            }}
          >
            {subtitle}
          </div>
        )}
      </div>

      {/* Trend indicator */}
      {trend && trendValue && (
        <div style={{ 
          display: "flex", 
          alignItems: "center", 
          gap: "0.25rem",
          marginTop: "auto"
        }}>
          <span style={{ fontSize: "0.875rem" }}>
            {getTrendIcon(trend)}
          </span>
          <span
            style={{
              fontSize: isMobile ? "0.75rem" : "0.875rem",
              fontWeight: 600,
              color: getTrendColor(trend)
            }}
          >
            {trendValue}
          </span>
          {trendLabel && (
            <span
              style={{
                fontSize: isMobile ? "0.7rem" : "0.75rem",
                color: colors.text.secondary,
                opacity: 0.8
              }}
            >
              {trendLabel}
            </span>
          )}
        </div>
      )}
    </div>
  );
};

export default KPICard;