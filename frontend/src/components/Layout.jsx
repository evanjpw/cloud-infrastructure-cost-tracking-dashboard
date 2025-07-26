import React, { useState, useEffect } from "react";
import { colors } from "../styles/colors";
import { textStyles } from "../styles/typography";

const Layout = ({ children }) => {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const checkIsMobile = () => {
      const mobile = window.innerWidth < 768;
      setIsMobile(mobile);
      // Auto-collapse sidebar on mobile, but don't create dependency loop
      if (mobile) {
        setSidebarCollapsed(true);
      }
    };

    checkIsMobile();
    window.addEventListener("resize", checkIsMobile);
    return () => window.removeEventListener("resize", checkIsMobile);
  }, []);

  const navigationItems = [
    { id: "dashboard", label: "Dashboard", icon: "📊", active: true },
    { id: "costs", label: "Cost Analysis", icon: "💰", active: false },
    { id: "budgets", label: "Budgets", icon: "🎯", active: false },
    { id: "reports", label: "Reports", icon: "📄", active: false },
    { id: "settings", label: "Settings", icon: "⚙️", active: false },
  ];

  const sidebarWidth = sidebarCollapsed ? "60px" : "240px";

  return (
    <div style={{ display: "flex", height: "100vh" }}>
      {/* Sidebar */}
      <div
        style={{
          width: sidebarWidth,
          backgroundColor: colors.sidebar.background,
          color: colors.sidebar.text,
          transition: "width 0.3s ease",
          overflow: "hidden",
          display: "flex",
          flexDirection: "column",
        }}
      >
        {/* Logo/Header */}
        <div
          style={{
            padding: "1rem",
            borderBottom: `1px solid ${colors.sidebar.border}`,
            display: "flex",
            alignItems: "center",
            justifyContent: sidebarCollapsed ? "center" : "space-between",
          }}
        >
          {!sidebarCollapsed && (
            <div>
              <div
                style={{
                  ...textStyles.cardTitle(colors.sidebar.text),
                  fontSize: "1.1rem",
                }}
              >
                Cloud Cost Dashboard
              </div>
              <div
                style={{
                  ...textStyles.caption(colors.sidebar.textSecondary),
                  marginTop: "4px",
                }}
              >
                Multi-Cloud Analytics
              </div>
            </div>
          )}
          <button
            onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
            style={{
              background: "none",
              border: "none",
              color: colors.sidebar.text,
              fontSize: "1.2rem",
              cursor: "pointer",
              padding: "4px",
            }}
          >
            {sidebarCollapsed ? "▶" : "◀"}
          </button>
        </div>

        {/* Navigation */}
        <nav style={{ flex: 1, padding: "1rem 0" }}>
          {navigationItems.map((item) => (
            <div
              key={item.id}
              style={{
                display: "flex",
                alignItems: "center",
                padding: "0.75rem 1rem",
                margin: "0.25rem 0.5rem",
                borderRadius: "4px",
                cursor: "pointer",
                backgroundColor: item.active
                  ? colors.sidebar.backgroundActive
                  : "transparent",
                transition: "background-color 0.2s ease",
                ...textStyles.nav(colors.sidebar.text),
              }}
              onMouseEnter={(e) => {
                if (!item.active) {
                  e.target.style.backgroundColor =
                    colors.sidebar.backgroundHover;
                }
              }}
              onMouseLeave={(e) => {
                if (!item.active) {
                  e.target.style.backgroundColor = "transparent";
                }
              }}
            >
              <span
                style={{
                  fontSize: "1.1rem",
                  marginRight: sidebarCollapsed ? "0" : "0.75rem",
                }}
              >
                {item.icon}
              </span>
              {!sidebarCollapsed && <span>{item.label}</span>}
            </div>
          ))}
        </nav>

        {/* User/Account Info */}
        {!sidebarCollapsed && (
          <div
            style={{
              padding: "1rem",
              borderTop: `1px solid ${colors.sidebar.border}`,
            }}
          >
            <div style={textStyles.caption(colors.sidebar.textSecondary)}>
              Demo Account
            </div>
            <div style={textStyles.caption(colors.sidebar.textSecondary)}>
              Cost Center: ENG001
            </div>
          </div>
        )}
      </div>

      {/* Main Content Area */}
      <div
        style={{
          flex: 1,
          display: "flex",
          flexDirection: "column",
          backgroundColor: colors.background.secondary,
          overflow: "hidden",
        }}
      >
        {/* Top Header Bar */}
        <header
          style={{
            backgroundColor: colors.background.primary,
            borderBottom: `1px solid ${colors.border.light}`,
            padding: isMobile ? "1rem" : "1rem 2rem",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
            flexWrap: "wrap",
            gap: "1rem",
          }}
        >
          <div>
            <h1 style={textStyles.sectionTitle(colors.text.primary)}>
              Cost Dashboard
            </h1>
            <p
              style={{
                ...textStyles.body(colors.text.secondary),
                marginTop: "4px",
              }}
            >
              Monitor and optimize your cloud infrastructure costs
            </p>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: "1rem" }}>
            <div style={textStyles.caption(colors.text.secondary)}>
              Last updated: {new Date().toLocaleTimeString()}
            </div>
            <div
              style={{
                width: "40px",
                height: "40px",
                borderRadius: "50%",
                backgroundColor: colors.primary[500],
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                color: colors.text.inverse,
                fontWeight: "bold",
              }}
            >
              U
            </div>
          </div>
        </header>

        {/* Main Content */}
        <main
          style={{
            flex: 1,
            padding: isMobile ? "1rem" : "2rem",
            overflow: "auto",
          }}
        >
          {children}
        </main>
      </div>
    </div>
  );
};

export default Layout;
