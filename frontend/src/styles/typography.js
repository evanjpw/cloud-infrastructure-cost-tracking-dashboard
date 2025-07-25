// Professional Typography System
// Inspired by AWS Cost Explorer, Azure Cost Management, and modern web design

export const typography = {
  // Font families
  fontFamily: {
    // Primary font stack - clean, modern, professional
    primary:
      '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
    // Monospace for code/data
    mono: '"SF Mono", "Monaco", "Inconsolata", "Roboto Mono", "Source Code Pro", monospace',
  },

  // Font weights
  fontWeight: {
    light: 300,
    normal: 400,
    medium: 500,
    semibold: 600,
    bold: 700,
  },

  // Font sizes with proper scale
  fontSize: {
    xs: "0.75rem", // 12px
    sm: "0.875rem", // 14px
    base: "1rem", // 16px
    lg: "1.125rem", // 18px
    xl: "1.25rem", // 20px
    "2xl": "1.5rem", // 24px
    "3xl": "1.875rem", // 30px
    "4xl": "2.25rem", // 36px
  },

  // Line heights for optimal readability
  lineHeight: {
    tight: 1.25,
    normal: 1.5,
    relaxed: 1.75,
  },

  // Letter spacing
  letterSpacing: {
    tight: "-0.025em",
    normal: "0",
    wide: "0.025em",
  },
};

// Typography component styles for consistent usage
export const getTypographyStyle = (variant) => {
  const variants = {
    // Page titles
    h1: {
      fontSize: typography.fontSize["3xl"],
      fontWeight: typography.fontWeight.bold,
      lineHeight: typography.lineHeight.tight,
      letterSpacing: typography.letterSpacing.tight,
      fontFamily: typography.fontFamily.primary,
    },

    // Section headers
    h2: {
      fontSize: typography.fontSize["2xl"],
      fontWeight: typography.fontWeight.semibold,
      lineHeight: typography.lineHeight.tight,
      letterSpacing: typography.letterSpacing.tight,
      fontFamily: typography.fontFamily.primary,
    },

    // Subsection headers
    h3: {
      fontSize: typography.fontSize.xl,
      fontWeight: typography.fontWeight.semibold,
      lineHeight: typography.lineHeight.tight,
      fontFamily: typography.fontFamily.primary,
    },

    // Card titles, component headers
    h4: {
      fontSize: typography.fontSize.lg,
      fontWeight: typography.fontWeight.semibold,
      lineHeight: typography.lineHeight.normal,
      fontFamily: typography.fontFamily.primary,
    },

    // Subheadings
    h5: {
      fontSize: typography.fontSize.base,
      fontWeight: typography.fontWeight.semibold,
      lineHeight: typography.lineHeight.normal,
      fontFamily: typography.fontFamily.primary,
    },

    // Small headers
    h6: {
      fontSize: typography.fontSize.sm,
      fontWeight: typography.fontWeight.semibold,
      lineHeight: typography.lineHeight.normal,
      fontFamily: typography.fontFamily.primary,
      textTransform: "uppercase",
      letterSpacing: typography.letterSpacing.wide,
    },

    // Body text
    body: {
      fontSize: typography.fontSize.base,
      fontWeight: typography.fontWeight.normal,
      lineHeight: typography.lineHeight.normal,
      fontFamily: typography.fontFamily.primary,
    },

    // Large body text
    bodyLarge: {
      fontSize: typography.fontSize.lg,
      fontWeight: typography.fontWeight.normal,
      lineHeight: typography.lineHeight.normal,
      fontFamily: typography.fontFamily.primary,
    },

    // Small body text
    bodySmall: {
      fontSize: typography.fontSize.sm,
      fontWeight: typography.fontWeight.normal,
      lineHeight: typography.lineHeight.normal,
      fontFamily: typography.fontFamily.primary,
    },

    // Caption text
    caption: {
      fontSize: typography.fontSize.xs,
      fontWeight: typography.fontWeight.normal,
      lineHeight: typography.lineHeight.normal,
      fontFamily: typography.fontFamily.primary,
    },

    // Labels for forms
    label: {
      fontSize: typography.fontSize.sm,
      fontWeight: typography.fontWeight.medium,
      lineHeight: typography.lineHeight.normal,
      fontFamily: typography.fontFamily.primary,
    },

    // Button text
    button: {
      fontSize: typography.fontSize.sm,
      fontWeight: typography.fontWeight.medium,
      lineHeight: typography.lineHeight.normal,
      fontFamily: typography.fontFamily.primary,
      letterSpacing: typography.letterSpacing.wide,
    },

    // Data/numbers
    data: {
      fontSize: typography.fontSize.base,
      fontWeight: typography.fontWeight.medium,
      lineHeight: typography.lineHeight.normal,
      fontFamily: typography.fontFamily.mono,
    },

    // Large data displays
    dataLarge: {
      fontSize: typography.fontSize.xl,
      fontWeight: typography.fontWeight.semibold,
      lineHeight: typography.lineHeight.tight,
      fontFamily: typography.fontFamily.mono,
    },

    // Navigation items
    nav: {
      fontSize: typography.fontSize.sm,
      fontWeight: typography.fontWeight.medium,
      lineHeight: typography.lineHeight.normal,
      fontFamily: typography.fontFamily.primary,
    },
  };

  return variants[variant] || variants.body;
};

// Helper function to get consistent text styles
export const getTextStyle = (variant, color) => ({
  ...getTypographyStyle(variant),
  color,
  margin: 0, // Reset default margins
});

// Global font loading CSS (to be added to index.css)
export const globalFontCSS = `
  /* Import Inter font for better readability */
  @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');

  /* Use Inter as primary font with system fallbacks */
  * {
    font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  }

  /* Improve font rendering */
  body {
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-rendering: optimizeLegibility;
  }

  /* Reset default margins and improve line height */
  h1, h2, h3, h4, h5, h6, p {
    margin: 0;
    line-height: 1.5;
  }
`;

// Predefined text styles for common use cases
export const textStyles = {
  pageTitle: (color) => getTextStyle("h1", color),
  sectionTitle: (color) => getTextStyle("h2", color),
  cardTitle: (color) => getTextStyle("h4", color),
  label: (color) => getTextStyle("label", color),
  body: (color) => getTextStyle("body", color),
  bodySmall: (color) => getTextStyle("bodySmall", color),
  caption: (color) => getTextStyle("caption", color),
  button: (color) => getTextStyle("button", color),
  data: (color) => getTextStyle("data", color),
  nav: (color) => getTextStyle("nav", color),
};
