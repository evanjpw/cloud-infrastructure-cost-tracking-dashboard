import { colors } from "../styles/colors";
import { typography } from "../styles/typography";

// Chart.js default configuration to match our design system
export const chartDefaults = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: "bottom",
      align: "start",
      labels: {
        usePointStyle: true,
        padding: 16,
        font: {
          family: typography.fontFamily.primary,
          size: 12,
          weight: typography.fontWeight.medium,
        },
        color: colors.text.primary,
      },
    },
    tooltip: {
      backgroundColor: colors.gray[900],
      titleColor: "#ffffff",
      bodyColor: colors.gray[100],
      borderColor: colors.gray[700],
      borderWidth: 1,
      padding: 12,
      cornerRadius: 6,
      displayColors: true,
      titleFont: {
        family: typography.fontFamily.primary,
        size: 14,
        weight: typography.fontWeight.semibold,
      },
      bodyFont: {
        family: typography.fontFamily.primary,
        size: 13,
        weight: typography.fontWeight.normal,
      },
    },
  },
  interaction: {
    mode: "index",
    intersect: false,
  },
};

// Professional color palette for charts
export const chartColors = {
  // Primary palette - AWS/Azure inspired
  primary: [
    colors.primary[500], // Blue
    colors.warning, // Orange
    colors.success, // Green
    "#9c27b0", // Purple
    "#009688", // Teal
    "#607d8b", // Blue gray
    colors.error, // Red
    "#795548", // Brown
  ],
  // Gradients for more sophisticated charts
  gradients: {
    blue: ["rgba(33, 150, 243, 0.8)", "rgba(33, 150, 243, 0.2)"],
    green: ["rgba(76, 175, 80, 0.8)", "rgba(76, 175, 80, 0.2)"],
    orange: ["rgba(255, 152, 0, 0.8)", "rgba(255, 152, 0, 0.2)"],
  },
};

// Grid and axes configuration
export const axesConfig = {
  grid: {
    color: colors.gray[200],
    borderColor: colors.gray[300],
    drawBorder: true,
    drawOnChartArea: true,
    drawTicks: false,
  },
  ticks: {
    color: colors.text.secondary,
    font: {
      family: typography.fontFamily.primary,
      size: 11,
      weight: typography.fontWeight.normal,
    },
    padding: 8,
  },
  title: {
    color: colors.text.primary,
    font: {
      family: typography.fontFamily.primary,
      size: 13,
      weight: typography.fontWeight.semibold,
    },
    padding: { top: 10, bottom: 10 },
  },
};

// Animation configuration for professional feel
export const animationConfig = {
  duration: 750,
  easing: "easeInOutQuart",
};

// Helper function to create gradient
export const createGradient = (ctx, area, colorStart, colorEnd) => {
  const gradient = ctx.createLinearGradient(0, area.bottom, 0, area.top);
  gradient.addColorStop(0, colorEnd);
  gradient.addColorStop(1, colorStart);
  return gradient;
};

// Format currency for tooltips and labels
export const formatCurrency = (value) => {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    minimumFractionDigits: 0,
    maximumFractionDigits: value >= 1000 ? 0 : 2,
  }).format(value);
};

// Format large numbers with K, M, B suffixes
export const formatLargeNumber = (value) => {
  if (value >= 1000000000) {
    return `$${(value / 1000000000).toFixed(1)}B`;
  }
  if (value >= 1000000) {
    return `$${(value / 1000000).toFixed(1)}M`;
  }
  if (value >= 1000) {
    return `$${(value / 1000).toFixed(1)}K`;
  }
  return formatCurrency(value);
};

// Common chart options by type
export const chartOptions = {
  line: {
    ...chartDefaults,
    elements: {
      line: {
        tension: 0.3,
        borderWidth: 2,
      },
      point: {
        radius: 3,
        hoverRadius: 5,
        backgroundColor: "#ffffff",
        hoverBackgroundColor: "#ffffff",
      },
    },
    scales: {
      x: {
        ...axesConfig,
      },
      y: {
        ...axesConfig,
        beginAtZero: true,
        ticks: {
          ...axesConfig.ticks,
          callback: function (value) {
            return formatLargeNumber(value);
          },
        },
      },
    },
  },
  pie: {
    ...chartDefaults,
    cutout: "0%", // Use 50% for doughnut
    plugins: {
      ...chartDefaults.plugins,
      legend: {
        ...chartDefaults.plugins.legend,
        position: "right",
      },
    },
  },
  bar: {
    ...chartDefaults,
    scales: {
      x: {
        ...axesConfig,
        stacked: false,
      },
      y: {
        ...axesConfig,
        stacked: false,
        beginAtZero: true,
        ticks: {
          ...axesConfig.ticks,
          callback: function (value) {
            return formatLargeNumber(value);
          },
        },
      },
    },
  },
};