import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import KPICard from '../KPICard';

describe('KPICard', () => {
  const defaultProps = {
    title: 'Total Cost',
    value: '$12,345',
    subtitle: 'This month'
  };

  it('renders basic KPI card with required props', () => {
    render(<KPICard {...defaultProps} />);

    expect(screen.getByText('Total Cost')).toBeInTheDocument();
    expect(screen.getByText('$12,345')).toBeInTheDocument();
    expect(screen.getByText('This month')).toBeInTheDocument();
  });

  it('renders with icon when provided', () => {
    const TestIcon = () => <span data-testid="test-icon">💰</span>;
    
    render(<KPICard {...defaultProps} icon={<TestIcon />} />);

    expect(screen.getByTestId('test-icon')).toBeInTheDocument();
  });

  it('displays trend information when provided', () => {
    render(
      <KPICard 
        {...defaultProps} 
        trend="up"
        trendValue="+15%"
        trendLabel="vs last month"
      />
    );

    expect(screen.getByText('+15%')).toBeInTheDocument();
    expect(screen.getByText('vs last month')).toBeInTheDocument();
  });

  it('applies correct status colors', () => {
    const { rerender } = render(<KPICard {...defaultProps} status="positive" />);
    
    // Test positive status (success color)
    expect(screen.getByText('$12,345')).toHaveStyle('color: rgb(76, 175, 80)');

    // Test warning status
    rerender(<KPICard {...defaultProps} status="warning" />);
    expect(screen.getByText('$12,345')).toHaveStyle('color: rgb(255, 152, 0)');

    // Test critical status
    rerender(<KPICard {...defaultProps} status="critical" />);
    expect(screen.getByText('$12,345')).toHaveStyle('color: rgb(244, 67, 54)');
  });

  it('handles click events when clickable', () => {
    const mockOnClick = jest.fn();
    
    render(
      <KPICard 
        {...defaultProps} 
        onClick={mockOnClick}
        isClickable={true}
      />
    );

    const card = screen.getByText('Total Cost').closest('div');
    fireEvent.click(card);

    expect(mockOnClick).toHaveBeenCalledTimes(1);
  });

  it('does not trigger click when not clickable', () => {
    const mockOnClick = jest.fn();
    
    render(
      <KPICard 
        {...defaultProps} 
        onClick={mockOnClick}
        isClickable={false}
      />
    );

    const card = screen.getByText('Total Cost').closest('div');
    fireEvent.click(card);

    expect(mockOnClick).not.toHaveBeenCalled();
  });

  it('applies mobile styles when isMobile is true', () => {
    const { container } = render(<KPICard {...defaultProps} isMobile={true} />);

    // Find the root div with all styles
    const card = container.firstChild;
    expect(card).toHaveStyle('padding: 1rem');
  });

  it('renders different trend directions correctly', () => {
    const { rerender } = render(
      <KPICard 
        {...defaultProps} 
        trend="up"
        trendValue="+15%"
      />
    );

    // Test up trend (component uses emoji)
    expect(screen.getByText('📈')).toBeInTheDocument();

    // Test down trend
    rerender(
      <KPICard 
        {...defaultProps} 
        trend="down"
        trendValue="-5%"
      />
    );
    expect(screen.getByText('📉')).toBeInTheDocument();

    // Test default trend
    rerender(
      <KPICard 
        {...defaultProps} 
        trend="flat"
        trendValue="0%"
      />
    );
    expect(screen.getByText('➡️')).toBeInTheDocument();
  });

  it('handles long text values gracefully', () => {
    render(
      <KPICard 
        title="Very Long KPI Title That Should Wrap"
        value="$999,999,999.99"
        subtitle="This is a very long subtitle that should also wrap properly"
      />
    );

    expect(screen.getByText('Very Long KPI Title That Should Wrap')).toBeInTheDocument();
    expect(screen.getByText('$999,999,999.99')).toBeInTheDocument();
    expect(screen.getByText('This is a very long subtitle that should also wrap properly')).toBeInTheDocument();
  });

  it('handles missing optional props gracefully', () => {
    render(<KPICard title="Minimal Card" value="$100" />);

    expect(screen.getByText('Minimal Card')).toBeInTheDocument();
    expect(screen.getByText('$100')).toBeInTheDocument();
    expect(screen.queryByTestId('trend-indicator')).not.toBeInTheDocument();
  });

  it('applies hover styles for clickable cards', () => {
    const { container } = render(
      <KPICard 
        {...defaultProps} 
        isClickable={true}
        onClick={() => {}}
      />
    );

    const card = container.firstChild;
    expect(card).toHaveStyle('cursor: pointer');
  });

  it('handles special status values correctly', () => {
    const specialStatuses = [
      'good',
      'under-budget',
      'at-risk',
      'over-budget'
    ];

    specialStatuses.forEach(status => {
      const { rerender } = render(<KPICard {...defaultProps} status={status} />);
      
      // Just verify it renders without errors
      expect(screen.getByText('Total Cost')).toBeInTheDocument();
      
      rerender(<div />); // Clear between tests
    });
  });

  it('formats numbers and currency consistently', () => {
    const testCases = [
      { value: '$1,234.56', expected: '$1,234.56' },
      { value: '1234', expected: '1234' },
      { value: '$0.00', expected: '$0.00' }
    ];

    testCases.forEach(({ value, expected }) => {
      const { rerender } = render(<KPICard {...defaultProps} value={value} />);
      expect(screen.getByText(expected)).toBeInTheDocument();
      rerender(<div />);
    });
  });

  it('supports accessibility features', () => {
    const { container } = render(
      <KPICard 
        {...defaultProps} 
        isClickable={true}
        onClick={() => {}}
      />
    );

    const card = container.firstChild;
    
    // Clickable cards should have pointer cursor
    expect(card).toHaveStyle('cursor: pointer');
    
    // Should render without errors
    expect(card).toBeInTheDocument();
  });
});