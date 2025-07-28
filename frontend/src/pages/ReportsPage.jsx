import React from "react";
import { colors, getCardStyle } from "../styles/colors";
import { textStyles } from "../styles/typography";

const ReportsPage = () => {
  return (
    <div style={{ maxWidth: "1200px", margin: "0 auto" }}>
      {/* Page Header */}
      <div style={{ marginBottom: "2rem" }}>
        <h2 style={textStyles.pageTitle(colors.text.primary)}>
          Reports & Analytics
        </h2>
        <p style={textStyles.body(colors.text.secondary)}>
          Generate, schedule, and export comprehensive cost reports
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
        <div style={{ fontSize: "4rem", marginBottom: "1rem" }}>📄</div>
        <h3 style={textStyles.cardTitle(colors.text.primary)}>
          Reporting & Analytics
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
          <li>Custom report builder</li>
          <li>Scheduled report delivery</li>
          <li>Executive dashboards</li>
          <li>PDF, Excel, CSV exports</li>
          <li>Cost attribution reports</li>
          <li>Chargeback/showback reports</li>
        </ul>
        <div
          style={{
            marginTop: "2rem",
            padding: "1rem",
            backgroundColor: colors.info + "20",
            borderRadius: "6px",
            border: `1px solid ${colors.info}`,
          }}
        >
          <p style={{ ...textStyles.caption(colors.text.primary), margin: 0 }}>
            📈 <strong>Phase 2.3:</strong> Advanced reporting features planned for Q4 2025
          </p>
        </div>
      </div>
    </div>
  );
};

export default ReportsPage;