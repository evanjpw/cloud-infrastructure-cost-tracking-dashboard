import React from "react";
import { colors, getCardStyle } from "../styles/colors";
import { textStyles } from "../styles/typography";

const CostBreakdownChart = ({ data = [] }) => {
  const totalCost = data.reduce((sum, item) => sum + (item.totalCost || 0), 0);

  const formatCost = (cost) => {
    return typeof cost === "number" ? cost.toFixed(2) : "0.00";
  };

  const getPercentage = (cost) => {
    if (totalCost === 0) return 0;
    return ((cost / totalCost) * 100).toFixed(1);
  };

  return (
    <div
      style={{
        ...getCardStyle(),
        padding: "1.5rem",
        marginBottom: "2rem",
      }}
    >
      <h5
        style={{
          ...textStyles.cardTitle(colors.text.primary),
          marginBottom: "1rem",
        }}
      >
        Cost Breakdown Chart
      </h5>
      {data.length === 0 ? (
        <p
          style={{
            ...textStyles.body(colors.text.secondary),
            fontStyle: "italic",
          }}
        >
          [No data available for chart visualization]
        </p>
      ) : (
        <div>
          <div style={{ marginBottom: "1rem" }}>
            <span style={textStyles.data(colors.text.primary)}>
              <strong>Total Cost: ${formatCost(totalCost)}</strong>
            </span>
          </div>

          {/* Simple horizontal bar chart */}
          <div
            style={{ display: "flex", flexDirection: "column", gap: "0.5rem" }}
          >
            {data.map((item, index) => {
              const percentage = getPercentage(item.totalCost);
              const color = colors.chart[index % colors.chart.length];

              return (
                <div
                  key={index}
                  style={{ display: "flex", alignItems: "center", gap: "1rem" }}
                >
                  <div
                    style={{
                      minWidth: "100px",
                      ...textStyles.bodySmall(colors.text.primary),
                    }}
                  >
                    {item.service || "Unknown"}
                    {item.note && (
                      <span
                        style={{
                          fontSize: "0.7em",
                          color: colors.warning,
                          marginLeft: "0.3rem",
                        }}
                      >
                        ⚠️
                      </span>
                    )}
                  </div>
                  <div
                    style={{
                      flex: 1,
                      height: "20px",
                      backgroundColor: colors.background.tertiary,
                      borderRadius: "4px",
                      overflow: "hidden",
                      position: "relative",
                    }}
                  >
                    <div
                      style={{
                        width: `${percentage}%`,
                        height: "100%",
                        backgroundColor: color,
                        transition: "width 0.3s ease",
                      }}
                    />
                  </div>
                  <div
                    style={{
                      minWidth: "80px",
                      textAlign: "right",
                      ...textStyles.data(colors.text.primary),
                      fontSize: "0.8rem",
                    }}
                  >
                    ${formatCost(item.totalCost)} ({percentage}%)
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};

export default CostBreakdownChart;
