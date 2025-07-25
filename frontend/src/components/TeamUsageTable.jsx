import React from "react";
import { colors, getCardStyle } from "../styles/colors";
import { textStyles } from "../styles/typography";

const TeamUsageTable = ({ data = [] }) => {
  const formatCost = (cost) => {
    return typeof cost === "number" ? cost.toFixed(2) : "0.00";
  };

  return (
    <div style={{ ...getCardStyle(), padding: "1.5rem" }}>
      <h5
        style={{
          ...textStyles.cardTitle(colors.text.primary),
          marginBottom: "1rem",
        }}
      >
        Team Usage Table
      </h5>
      {data.length === 0 ? (
        <p
          style={{
            ...textStyles.body(colors.text.secondary),
            fontStyle: "italic",
          }}
        >
          No cost data available
        </p>
      ) : (
        <table style={{ width: "100%", borderCollapse: "collapse" }}>
          <thead>
            <tr>
              <th
                style={{
                  borderBottom: `1px solid ${colors.border.medium}`,
                  textAlign: "left",
                  padding: "0.75rem",
                  backgroundColor: colors.background.secondary,
                  ...textStyles.label(colors.text.primary),
                }}
              >
                Service
              </th>
              <th
                style={{
                  borderBottom: `1px solid ${colors.border.medium}`,
                  textAlign: "right",
                  padding: "0.75rem",
                  backgroundColor: colors.background.secondary,
                  ...textStyles.label(colors.text.primary),
                }}
              >
                Total Cost ($)
              </th>
            </tr>
          </thead>
          <tbody>
            {data.map((item, index) => (
              <tr key={index}>
                <td
                  style={{
                    padding: "0.75rem",
                    borderBottom: `1px solid ${colors.border.light}`,
                    ...textStyles.body(colors.text.primary),
                  }}
                >
                  {item.service || "Unknown Service"}
                  {item.note && (
                    <span
                      style={{
                        fontSize: "0.8em",
                        color: colors.warning,
                        marginLeft: "0.5rem",
                      }}
                    >
                      ⚠️
                    </span>
                  )}
                </td>
                <td
                  style={{
                    padding: "0.75rem",
                    borderBottom: `1px solid ${colors.border.light}`,
                    textAlign: "right",
                    ...textStyles.data(colors.text.primary),
                  }}
                >
                  {formatCost(item.totalCost)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default TeamUsageTable;
