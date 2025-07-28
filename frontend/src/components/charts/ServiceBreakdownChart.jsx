import React from "react";
import { Pie } from "react-chartjs-2";
import BaseChart from "./BaseChart";
import {
  chartOptions,
  chartColors,
  formatCurrency,
  animationConfig,
} from "../../config/chartConfig";

const ServiceBreakdownChart = ({
  data,
  title = "Cost by Service",
  height = "400px",
  showAsDonut = false,
}) => {
  // Sort data by cost and take top 8 + "Others" if needed
  const sortedData = [...data].sort((a, b) => b.totalCost - a.totalCost);
  const topServices = sortedData.slice(0, 8);
  const otherServices = sortedData.slice(8);
  
  let finalData = topServices;
  if (otherServices.length > 0) {
    const othersCost = otherServices.reduce((sum, item) => sum + item.totalCost, 0);
    finalData = [
      ...topServices,
      { service: "Others", totalCost: othersCost, note: `${otherServices.length} services` }
    ];
  }

  const chartData = {
    labels: finalData.map((item) => item.service || item.name || item.label),
    datasets: [
      {
        data: finalData.map((item) => item.totalCost || item.cost || item.value),
        backgroundColor: chartColors.primary,
        borderColor: "#fff",
        borderWidth: 2,
        hoverBorderWidth: 3,
        hoverOffset: 8,
      },
    ],
  };

  const options = {
    ...chartOptions.pie,
    cutout: showAsDonut ? "50%" : "0%",
    animation: {
      ...animationConfig,
      animateRotate: true,
      animateScale: false,
    },
    plugins: {
      ...chartOptions.pie.plugins,
      tooltip: {
        ...chartOptions.pie.plugins.tooltip,
        callbacks: {
          label: function (context) {
            const label = context.label || "";
            const value = formatCurrency(context.parsed);
            const percentage = (
              (context.parsed / context.dataset.data.reduce((a, b) => a + b, 0)) *
              100
            ).toFixed(1);
            return [`${label}:`, `${value} (${percentage}%)`];
          },
        },
      },
      legend: {
        ...chartOptions.pie.plugins.legend,
        labels: {
          ...chartOptions.pie.plugins.legend.labels,
          generateLabels: function (chart) {
            const data = chart.data;
            if (data.labels.length && data.datasets.length) {
              const dataset = data.datasets[0];
              const total = dataset.data.reduce((a, b) => a + b, 0);
              
              return data.labels.map((label, i) => {
                const value = dataset.data[i];
                const percentage = ((value / total) * 100).toFixed(1);
                
                return {
                  text: `${label} (${percentage}%)`,
                  fillStyle: dataset.backgroundColor[i],
                  strokeStyle: dataset.borderColor,
                  lineWidth: dataset.borderWidth,
                  hidden: false,
                  index: i,
                };
              });
            }
            return [];
          },
        },
      },
    },
  };

  const totalCost = finalData.reduce((sum, item) => sum + (item.totalCost || 0), 0);

  return (
    <BaseChart
      title={title}
      subtitle={`Total: ${formatCurrency(totalCost)} across ${finalData.length} services`}
      height={height}
    >
      <Pie data={chartData} options={options} />
    </BaseChart>
  );
};

export default ServiceBreakdownChart;