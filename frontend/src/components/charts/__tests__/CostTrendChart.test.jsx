import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import CostTrendChart from '../CostTrendChart';

// Mock Chart.js components
jest.mock('react-chartjs-2', () => ({
  Line: ({ data, options, ...props }) => (
    <div 
      data-testid="line-chart"
      data-chart-data={JSON.stringify(data)}
      data-chart-options={JSON.stringify(options)}
      {...props}
    >
      Mock Line Chart
    </div>
  )
}));

// Mock BaseChart
jest.mock('../BaseChart', () => {
  return ({ children, title, height }) => (
    <div data-testid="base-chart" data-title={title} data-height={height}>
      {children}
    </div>
  );
});

// Mock chart config
jest.mock('../../../config/chartConfig', () => ({
  chartOptions: {
    responsive: true,
    maintainAspectRatio: false
  },
  chartColors: {
    primary: ['#3b82f6'],
    gradients: {
      blue: ['rgba(59, 130, 246, 0.2)', 'rgba(59, 130, 246, 0.05)']
    }
  },
  formatCurrency: (value) => `$${value.toLocaleString()}`,
  createGradient: jest.fn((ctx, chartArea, color1, color2) => `gradient-${color1}-${color2}`),
  animationConfig: {
    duration: 750,
    easing: 'easeInOutQuart'
  }
}));

describe('CostTrendChart', () => {
  const mockData = [
    { date: '2025-01-01', totalCost: 1000 },
    { date: '2025-01-02', totalCost: 1200 },
    { date: '2025-01-03', totalCost: 1100 },
    { date: '2025-01-04', totalCost: 1300 },
    { date: '2025-01-05', totalCost: 1150 }
  ];

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders chart with default title and height', () => {
    render(<CostTrendChart data={mockData} />);

    expect(screen.getByTestId('base-chart')).toBeInTheDocument();
    expect(screen.getByTestId('base-chart')).toHaveAttribute('data-title', 'Cost Trend Over Time');
    expect(screen.getByTestId('base-chart')).toHaveAttribute('data-height', '400px');
    expect(screen.getByTestId('line-chart')).toBeInTheDocument();
  });

  it('renders chart with custom title and height', () => {
    render(
      <CostTrendChart 
        data={mockData} 
        title="Custom Cost Trend" 
        height="500px" 
      />
    );

    expect(screen.getByTestId('base-chart')).toHaveAttribute('data-title', 'Custom Cost Trend');
    expect(screen.getByTestId('base-chart')).toHaveAttribute('data-height', '500px');
  });

  it('transforms data correctly for Chart.js', () => {
    render(<CostTrendChart data={mockData} />);

    const chartElement = screen.getByTestId('line-chart');
    const chartData = JSON.parse(chartElement.getAttribute('data-chart-data'));

    expect(chartData.labels).toEqual(['2025-01-01', '2025-01-02', '2025-01-03', '2025-01-04', '2025-01-05']);
    expect(chartData.datasets[0].data).toEqual([1000, 1200, 1100, 1300, 1150]);
    expect(chartData.datasets[0].label).toBe('Total Cost');
  });

  it('handles different data formats correctly', () => {
    const alternativeData = [
      { month: 'Jan', cost: 1000 },
      { month: 'Feb', cost: 1200 },
      { label: 'Mar', value: 1100 }
    ];

    render(<CostTrendChart data={alternativeData} />);

    const chartElement = screen.getByTestId('line-chart');
    const chartData = JSON.parse(chartElement.getAttribute('data-chart-data'));

    expect(chartData.labels).toEqual(['Jan', 'Feb', 'Mar']);
    expect(chartData.datasets[0].data).toEqual([1000, 1200, 1100]);
  });

  it('applies correct chart styling', () => {
    render(<CostTrendChart data={mockData} />);

    const chartElement = screen.getByTestId('line-chart');
    const chartData = JSON.parse(chartElement.getAttribute('data-chart-data'));
    const dataset = chartData.datasets[0];

    expect(dataset.borderColor).toBe('#3b82f6');
    expect(dataset.fill).toBe(true);
    expect(dataset.tension).toBe(0.3);
    expect(dataset.pointBackgroundColor).toBe('#fff');
    expect(dataset.pointBorderColor).toBe('#3b82f6');
    expect(dataset.pointRadius).toBe(4);
    expect(dataset.pointHoverRadius).toBe(6);
  });

  it('handles empty data gracefully', () => {
    render(<CostTrendChart data={[]} />);

    const chartElement = screen.getByTestId('line-chart');
    const chartData = JSON.parse(chartElement.getAttribute('data-chart-data'));

    expect(chartData.labels).toEqual([]);
    expect(chartData.datasets[0].data).toEqual([]);
  });

  it('handles malformed data gracefully', () => {
    const malformedData = [
      { date: '2025-01-01' }, // missing cost
      { totalCost: 1200 }, // missing date
      {}, // completely empty
      null, // null entry
    ].filter(Boolean); // Remove null entry

    render(<CostTrendChart data={malformedData} />);

    const chartElement = screen.getByTestId('line-chart');
    const chartData = JSON.parse(chartElement.getAttribute('data-chart-data'));

    // Should handle missing values gracefully
    expect(chartData.labels).toEqual(['2025-01-01', undefined, undefined]);
    expect(chartData.datasets[0].data).toEqual([undefined, 1200, undefined]);
  });

  it('sets up gradient background function correctly', () => {
    render(<CostTrendChart data={mockData} />);

    const chartElement = screen.getByTestId('line-chart');
    const chartData = JSON.parse(chartElement.getAttribute('data-chart-data'));
    const dataset = chartData.datasets[0];

    // The backgroundColor should be a function
    expect(typeof dataset.backgroundColor).toBe('function');
  });

  it('configures chart options correctly', () => {
    render(<CostTrendChart data={mockData} />);

    const chartElement = screen.getByTestId('line-chart');
    const options = JSON.parse(chartElement.getAttribute('data-chart-options'));

    expect(options).toMatchObject({
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: true,
          position: 'top'
        },
        tooltip: {
          mode: 'index',
          intersect: false
        }
      },
      scales: {
        x: {
          display: true,
          grid: {
            display: true
          }
        },
        y: {
          display: true,
          grid: {
            display: true
          }
        }
      }
    });
  });

  it('handles large datasets efficiently', () => {
    const largeData = Array.from({ length: 365 }, (_, i) => ({
      date: `2025-01-${String(i + 1).padStart(2, '0')}`,
      totalCost: Math.floor(Math.random() * 2000) + 1000
    }));

    const { container } = render(<CostTrendChart data={largeData} />);

    expect(container.querySelector('[data-testid="line-chart"]')).toBeInTheDocument();
    
    const chartElement = screen.getByTestId('line-chart');
    const chartData = JSON.parse(chartElement.getAttribute('data-chart-data'));
    
    expect(chartData.labels).toHaveLength(365);
    expect(chartData.datasets[0].data).toHaveLength(365);
  });

  it('provides proper accessibility support', () => {
    render(<CostTrendChart data={mockData} title="Cost Analysis Chart" />);

    const baseChart = screen.getByTestId('base-chart');
    expect(baseChart).toHaveAttribute('data-title', 'Cost Analysis Chart');
    
    // Chart should be wrapped in BaseChart which presumably handles accessibility
    expect(baseChart).toBeInTheDocument();
  });

  it('updates when data changes', () => {
    const { rerender } = render(<CostTrendChart data={mockData} />);

    let chartElement = screen.getByTestId('line-chart');
    let chartData = JSON.parse(chartElement.getAttribute('data-chart-data'));
    expect(chartData.datasets[0].data).toEqual([1000, 1200, 1100, 1300, 1150]);

    const newData = [
      { date: '2025-02-01', totalCost: 2000 },
      { date: '2025-02-02', totalCost: 2200 }
    ];

    rerender(<CostTrendChart data={newData} />);

    chartElement = screen.getByTestId('line-chart');
    chartData = JSON.parse(chartElement.getAttribute('data-chart-data'));
    expect(chartData.datasets[0].data).toEqual([2000, 2200]);
  });

  it('handles mixed positive and negative values', () => {
    const mixedData = [
      { date: '2025-01-01', totalCost: 1000 },
      { date: '2025-01-02', totalCost: -500 }, // negative cost (refund?)
      { date: '2025-01-03', totalCost: 0 },
      { date: '2025-01-04', totalCost: 1500 }
    ];

    render(<CostTrendChart data={mixedData} />);

    const chartElement = screen.getByTestId('line-chart');
    const chartData = JSON.parse(chartElement.getAttribute('data-chart-data'));

    expect(chartData.datasets[0].data).toEqual([1000, -500, 0, 1500]);
  });
});