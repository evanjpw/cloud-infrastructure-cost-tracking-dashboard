// Professional Blue/Gray Color Scheme
// Inspired by AWS Cost Explorer, Azure Cost Management, and GCP Billing

export const colors = {
  // Primary Blue (AWS-inspired)
  primary: {
    50: "#e3f2fd",
    100: "#bbdefb",
    300: "#64b5f6",
    500: "#2196f3", // Main brand blue
    600: "#1976d2",
    700: "#1565c0",
    800: "#0d47a1",
    900: "#0a3d91",
  },

  // Secondary Gray (Professional neutrals)
  gray: {
    50: "#fafafa",
    100: "#f5f5f5",
    200: "#eeeeee",
    300: "#e0e0e0",
    400: "#bdbdbd",
    500: "#9e9e9e",
    600: "#757575",
    700: "#616161",
    800: "#424242",
    900: "#212121",
  },

  // Sidebar Dark Theme (AWS-style)
  sidebar: {
    background: "#232f3e",
    backgroundHover: "#2c3e50",
    backgroundActive: "#3a4c66",
    text: "#ffffff",
    textSecondary: "#8492a6",
    border: "#3a4c66",
  },

  // Status Colors
  success: "#4caf50",
  warning: "#ff9800",
  error: "#f44336",
  info: "#2196f3",

  // Background Colors
  background: {
    primary: "#ffffff",
    secondary: "#f8f9fa",
    tertiary: "#f5f5f5",
  },

  // Text Colors
  text: {
    primary: "#212529",
    secondary: "#6c757d",
    tertiary: "#9e9e9e",
    inverse: "#ffffff",
  },

  // Border Colors
  border: {
    light: "#dee2e6",
    medium: "#ced4da",
    dark: "#adb5bd",
  },

  // Chart Colors (Professional palette)
  chart: [
    "#2196f3", // Primary blue
    "#4caf50", // Success green
    "#ff9800", // Warning orange
    "#f44336", // Error red
    "#9c27b0", // Purple
    "#607d8b", // Blue gray
    "#795548", // Brown
    "#009688", // Teal
  ],
};

// Helper functions for common color combinations
export const getCardStyle = () => ({
  backgroundColor: colors.background.primary,
  border: `1px solid ${colors.border.light}`,
  borderRadius: "8px",
  boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
});

export const getInputStyle = () => ({
  border: `1px solid ${colors.border.medium}`,
  borderRadius: "4px",
  padding: "0.5rem",
  fontSize: "0.9rem",
  color: colors.text.primary,
});

export const getButtonStyle = (variant = "primary") => {
  const styles = {
    primary: {
      backgroundColor: colors.primary[500],
      color: colors.text.inverse,
      border: "none",
    },
    secondary: {
      backgroundColor: colors.background.primary,
      color: colors.primary[500],
      border: `1px solid ${colors.primary[500]}`,
    },
    outline: {
      backgroundColor: "transparent",
      color: colors.primary[500],
      border: `1px solid ${colors.border.medium}`,
    },
  };

  return {
    ...styles[variant],
    borderRadius: "4px",
    padding: "0.5rem 1rem",
    fontSize: "0.9rem",
    cursor: "pointer",
    fontWeight: "500",
    transition: "all 0.2s ease",
  };
};
