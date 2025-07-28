import React, { useRef } from "react";
import { Line } from "react-chartjs-2";
import BaseChart from "./BaseChart";
import {
  chartOptions,
  chartColors,
  formatCurrency,
  createGradient,
  animationConfig,
} from "../../config/chartConfig";

const CostTrendChart = ({ data, title = "Cost Trend Over Time", height = "400px" }) => {
  const chartRef = useRef(null);

  // Transform data for Chart.js
  const chartData = {
    labels: data.map((item) => item.date || item.month || item.label),
    datasets: [
      {
        label: "Total Cost",
        data: data.map((item) => item.totalCost || item.cost || item.value),
        borderColor: chartColors.primary[0],
        backgroundColor: (context) => {
          const chart = context.chart;
          const { ctx, chartArea } = chart;
          if (!chartArea) {
            return null;
          }
          return createGradient(
            ctx,
            chartArea,
            chartColors.gradients.blue[0],
            chartColors.gradients.blue[1]
          );
        },
        fill: true,
        tension: 0.3,
        pointBackgroundColor: "#fff",
        pointBorderColor: chartColors.primary[0],
        pointHoverBackgroundColor: chartColors.primary[0],
        pointHoverBorderColor: "#fff",
        pointRadius: 4,
        pointHoverRadius: 6,
        pointBorderWidth: 2,
        pointHoverBorderWidth: 3,
      },
    ],
  };

  const options = {
    ...chartOptions.line,
    animation: animationConfig,
    plugins: {
      ...chartOptions.line.plugins,
      tooltip: {
        ...chartOptions.line.plugins.tooltip,
        callbacks: {
          label: function (context) {
            return `Cost: ${formatCurrency(context.parsed.y)}`;
          },
        },
      },
      title: {
        display: false, // We use our own title
      },
    },
    scales: {
      ...chartOptions.line.scales,
      x: {
        ...chartOptions.line.scales.x,
        title: {
          ...chartOptions.line.scales.x.title,
          display: true,
          text: "Time Period",
        },
      },
      y: {
        ...chartOptions.line.scales.y,
        title: {
          ...chartOptions.line.scales.y.title,
          display: true,
          text: "Cost (USD)",
        },
      },
    },
  };

  return (
    <BaseChart
      title={title}
      subtitle={`Showing cost trends for ${data.length} periods`}
      height={height}
    >
      <Line ref={chartRef} data={chartData} options={options} />
    </BaseChart>
  );
};

export default CostTrendChart;