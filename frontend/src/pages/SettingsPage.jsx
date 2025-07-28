import React from "react";
import { colors, getCardStyle } from "../styles/colors";
import { textStyles } from "../styles/typography";

const SettingsPage = () => {
  return (
    <div style={{ maxWidth: "1200px", margin: "0 auto" }}>
      {/* Page Header */}
      <div style={{ marginBottom: "2rem" }}>
        <h2 style={textStyles.pageTitle(colors.text.primary)}>
          Settings & Configuration
        </h2>
        <p style={textStyles.body(colors.text.secondary)}>
          Manage user preferences, data sources, and system configuration
        </p>
      </div>

      {/* Demo Settings Card */}
      <div
        style={{
          ...getCardStyle(),
          padding: "2rem",
          marginBottom: "2rem",
        }}
      >
        <h3 style={textStyles.cardTitle(colors.text.primary)}>
          ⚙️ Demo Configuration
        </h3>
        <div style={{ display: "grid", gap: "1.5rem", marginTop: "1.5rem" }}>
          <div>
            <label style={textStyles.label(colors.text.primary)}>
              Default Currency
            </label>
            <select 
              style={{
                width: "200px",
                padding: "0.5rem",
                border: `1px solid ${colors.border.medium}`,
                borderRadius: "4px",
                marginTop: "0.5rem"
              }}
              defaultValue="USD"
            >
              <option value="USD">USD ($)</option>
              <option value="EUR">EUR (€)</option>
              <option value="GBP">GBP (£)</option>
            </select>
          </div>
          
          <div>
            <label style={textStyles.label(colors.text.primary)}>
              Default Date Range
            </label>
            <select 
              style={{
                width: "200px",
                padding: "0.5rem",
                border: `1px solid ${colors.border.medium}`,
                borderRadius: "4px",
                marginTop: "0.5rem"
              }}
              defaultValue="30days"
            >
              <option value="7days">Last 7 days</option>
              <option value="30days">Last 30 days</option>
              <option value="90days">Last 90 days</option>
            </select>
          </div>

          <div>
            <label style={textStyles.label(colors.text.primary)}>
              Theme Preference
            </label>
            <select 
              style={{
                width: "200px",
                padding: "0.5rem",
                border: `1px solid ${colors.border.medium}`,
                borderRadius: "4px",
                marginTop: "0.5rem"
              }}
              defaultValue="light"
            >
              <option value="light">Light</option>
              <option value="dark">Dark (Coming Soon)</option>
              <option value="auto">Auto (Coming Soon)</option>
            </select>
          </div>
        </div>
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
        <div style={{ fontSize: "4rem", marginBottom: "1rem" }}>🔧</div>
        <h3 style={textStyles.cardTitle(colors.text.primary)}>
          Advanced Settings
        </h3>
        <p style={textStyles.body(colors.text.secondary)}>
          Additional settings will include:
        </p>
        <ul style={{ 
          textAlign: "left", 
          maxWidth: "400px", 
          margin: "1.5rem auto",
          color: colors.text.secondary
        }}>
          <li>User profile management</li>
          <li>Team and role permissions</li>
          <li>Data source configuration</li>
          <li>Notification preferences</li>
          <li>API key management</li>
          <li>Export format defaults</li>
        </ul>
        <div
          style={{
            marginTop: "2rem",
            padding: "1rem",
            backgroundColor: colors.success + "20",
            borderRadius: "6px",
            border: `1px solid ${colors.success}`,
          }}
        >
          <p style={{ ...textStyles.caption(colors.text.primary), margin: 0 }}>
            🔐 <strong>Phase 3.1:</strong> Full authentication and user management coming in Q1 2026
          </p>
        </div>
      </div>
    </div>
  );
};

export default SettingsPage;