import React from "react";
import { colors, getCardStyle } from "../styles/colors";
import { textStyles } from "../styles/typography";

const BudgetsPage = () => {
  return (
    <div style={{ maxWidth: "1200px", margin: "0 auto" }}>
      {/* Page Header */}
      <div style={{ marginBottom: "2rem" }}>
        <h2 style={textStyles.pageTitle(colors.text.primary)}>
          Budget Management
        </h2>
        <p style={textStyles.body(colors.text.secondary)}>
          Set, track, and manage budgets across teams, services, and time periods
        </p>
      </div>

      {/* Coming Soon Card */}
      <div
        style={{
          ...getCardStyle(),
          padding: "3rem",
          textAlign: "center",
          backgroundColor: colors.background.secondary,
        }}
      >
        <div style={{ fontSize: "4rem", marginBottom: "1rem" }}>🎯</div>
        <h3 style={textStyles.cardTitle(colors.text.primary)}>
          Budget Management System
        </h3>
        <p style={textStyles.body(colors.text.secondary)}>
          This page will include:
        </p>
        <ul style={{ 
          textAlign: "left", 
          maxWidth: "400px", 
          margin: "1.5rem auto",
          color: colors.text.secondary
        }}>
          <li>Budget creation and editing</li>
          <li>Spend vs budget tracking</li>
          <li>Budget alerts and notifications</li>
          <li>Forecasting and projections</li>
          <li>Budget allocation by team/service</li>
          <li>Historical budget performance</li>
        </ul>
        <div
          style={{
            marginTop: "2rem",
            padding: "1rem",
            backgroundColor: colors.warning + "20",
            borderRadius: "6px",
            border: `1px solid ${colors.warning}`,
          }}
        >
          <p style={{ ...textStyles.caption(colors.text.primary), margin: 0 }}>
            🚧 <strong>Phase 2.1:</strong> Budget management features planned for Q4 2025
          </p>
        </div>
      </div>
    </div>
  );
};

export default BudgetsPage;