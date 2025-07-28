import React from "react";
import { getCardStyle } from "../../styles/colors";
import { textStyles } from "../../styles/typography";

// Base wrapper component for all charts
const BaseChart = ({ title, subtitle, children, height = "400px", className = "" }) => {
  return (
    <div
      style={{
        ...getCardStyle(),
        padding: "1.5rem",
        marginBottom: "1.5rem",
        height: "auto",
      }}
      className={className}
    >
      {title && (
        <div style={{ marginBottom: "1.5rem" }}>
          <h3
            style={{
              ...textStyles.cardTitle("#212121"),
              marginBottom: subtitle ? "0.25rem" : "0",
            }}
          >
            {title}
          </h3>
          {subtitle && (
            <p
              style={{
                ...textStyles.caption("#757575"),
                margin: 0,
              }}
            >
              {subtitle}
            </p>
          )}
        </div>
      )}
      <div style={{ position: "relative", height }}>
        {children}
      </div>
    </div>
  );
};

export default BaseChart;