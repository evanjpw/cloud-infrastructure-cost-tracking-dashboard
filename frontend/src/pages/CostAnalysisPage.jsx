import React from "react";
import { colors, getCardStyle } from "../styles/colors";
import { textStyles } from "../styles/typography";

const CostAnalysisPage = () => {
  return (
    <div style={{ maxWidth: "1200px", margin: "0 auto" }}>
      {/* Page Header */}
      <div style={{ marginBottom: "2rem" }}>
        <h2 style={textStyles.pageTitle(colors.text.primary)}>
          Cost Analysis
        </h2>
        <p style={textStyles.body(colors.text.secondary)}>
          Deep dive into cost breakdowns with advanced filtering and analysis tools
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
        <div style={{ fontSize: "4rem", marginBottom: "1rem" }}>🔍</div>
        <h3 style={textStyles.cardTitle(colors.text.primary)}>
          Advanced Cost Analysis
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
          <li>Multi-dimensional cost breakdowns</li>
          <li>Service-by-service analysis</li>
          <li>Regional cost distribution</li>
          <li>Resource type filtering</li>
          <li>Cost trend comparisons</li>
          <li>Export capabilities</li>
        </ul>
        <div
          style={{
            marginTop: "2rem",
            padding: "1rem",
            backgroundColor: colors.primary[50],
            borderRadius: "6px",
            border: `1px solid ${colors.primary[200]}`,
          }}
        >
          <p style={{ ...textStyles.caption(colors.primary[700]), margin: 0 }}>
            📋 <strong>Phase 1.3 & 2.3:</strong> Advanced filtering and reporting features coming soon
          </p>
        </div>
      </div>
    </div>
  );
};

export default CostAnalysisPage;