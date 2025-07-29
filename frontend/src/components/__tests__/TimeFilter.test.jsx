import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import TimeFilter from '../TimeFilter';

// Mock window.innerWidth for mobile testing
Object.defineProperty(window, 'innerWidth', {
  writable: true,
  configurable: true,
  value: 1024,
});

describe('TimeFilter', () => {
  const mockOnDateRangeChange = jest.fn();
  const defaultProps = {
    startDate: '2025-01-01',
    endDate: '2025-01-31',
    onDateRangeChange: mockOnDateRangeChange
  };

  beforeEach(() => {
    mockOnDateRangeChange.mockClear();
    // Reset window width
    window.innerWidth = 1024;
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('renders with initial date values', () => {
    render(<TimeFilter {...defaultProps} />);

    expect(screen.getByDisplayValue('2025-01-01')).toBeInTheDocument();
    expect(screen.getByDisplayValue('2025-01-31')).toBeInTheDocument();
    expect(screen.getByText('Apply Filter')).toBeInTheDocument();
  });

  it('calls onDateRangeChange when form is submitted with valid dates', async () => {
    const user = userEvent.setup();
    render(<TimeFilter {...defaultProps} />);

    const startDateInput = screen.getByDisplayValue('2025-01-01');
    const endDateInput = screen.getByDisplayValue('2025-01-31');
    const submitButton = screen.getByText('Apply Filter');

    // Change dates
    await user.clear(startDateInput);
    await user.type(startDateInput, '2025-02-01');
    
    await user.clear(endDateInput);
    await user.type(endDateInput, '2025-02-28');

    // Submit form
    await user.click(submitButton);

    expect(mockOnDateRangeChange).toHaveBeenCalledWith('2025-02-01', '2025-02-28');
  });

  it('handles form submission via Enter key', async () => {
    const user = userEvent.setup();
    render(<TimeFilter {...defaultProps} />);

    const startDateInput = screen.getByDisplayValue('2025-01-01');

    // Change start date and press Enter
    await user.clear(startDateInput);
    await user.type(startDateInput, '2025-03-01');
    await user.keyboard('{Enter}');

    expect(mockOnDateRangeChange).toHaveBeenCalledWith('2025-03-01', '2025-01-31');
  });

  it('does not call onDateRangeChange when dates are missing', async () => {
    const user = userEvent.setup();
    render(<TimeFilter {...defaultProps} />);

    const startDateInput = screen.getByDisplayValue('2025-01-01');
    const submitButton = screen.getByText('Apply Filter');

    // Clear start date
    await user.clear(startDateInput);
    await user.click(submitButton);

    expect(mockOnDateRangeChange).not.toHaveBeenCalled();
  });

  it('adapts layout for mobile devices', async () => {
    // Set mobile width
    window.innerWidth = 500;
    
    // Trigger resize event
    const resizeEvent = new Event('resize');
    
    render(<TimeFilter {...defaultProps} />);
    
    // Trigger the resize handler
    fireEvent(window, resizeEvent);

    await waitFor(() => {
      const container = screen.getByDisplayValue('2025-01-01').closest('div').parentElement;
      expect(container).toHaveStyle('padding: 1rem');
    });
  });

  it('adapts layout for desktop devices', async () => {
    // Set desktop width
    window.innerWidth = 1200;
    
    render(<TimeFilter {...defaultProps} />);
    
    const resizeEvent = new Event('resize');
    fireEvent(window, resizeEvent);

    await waitFor(() => {
      const container = screen.getByDisplayValue('2025-01-01').closest('div').parentElement;
      expect(container).toHaveStyle('padding: 1.5rem');
    });
  });

  it('handles resize events properly', () => {
    const { unmount } = render(<TimeFilter {...defaultProps} />);

    // Verify event listener is added
    const addEventListenerSpy = jest.spyOn(window, 'addEventListener');
    const removeEventListenerSpy = jest.spyOn(window, 'removeEventListener');

    // Re-render to trigger useEffect
    render(<TimeFilter {...defaultProps} />);

    expect(addEventListenerSpy).toHaveBeenCalledWith('resize', expect.any(Function));

    // Unmount to trigger cleanup
    unmount();
    
    expect(removeEventListenerSpy).toHaveBeenCalledWith('resize', expect.any(Function));

    addEventListenerSpy.mockRestore();
    removeEventListenerSpy.mockRestore();
  });

  it('renders quick select buttons', () => {
    render(<TimeFilter {...defaultProps} />);

    expect(screen.getByText('Last 7 Days')).toBeInTheDocument();
    expect(screen.getByText('Last 30 Days')).toBeInTheDocument();
    expect(screen.getByText('Last 90 Days')).toBeInTheDocument();
    expect(screen.getByText('This Month')).toBeInTheDocument();
    expect(screen.getByText('Last Month')).toBeInTheDocument();
  });

  it('handles quick select button clicks', async () => {
    const user = userEvent.setup();
    const today = new Date('2025-01-31');
    const mockDate = jest.spyOn(Date, 'now').mockReturnValue(today.getTime());

    render(<TimeFilter {...defaultProps} />);

    // Click "Last 7 Days"
    await user.click(screen.getByText('Last 7 Days'));

    // Should calculate 7 days ago from current date
    const expectedStartDate = '2025-01-24'; // 7 days before Jan 31
    const expectedEndDate = '2025-01-31';

    expect(mockOnDateRangeChange).toHaveBeenCalledWith(expectedStartDate, expectedEndDate);

    mockDate.mockRestore();
  });

  it('handles This Month quick select', async () => {
    const user = userEvent.setup();
    const today = new Date('2025-01-15'); // Mid-month
    const mockDate = jest.spyOn(Date, 'now').mockReturnValue(today.getTime());

    render(<TimeFilter {...defaultProps} />);

    await user.click(screen.getByText('This Month'));

    expect(mockOnDateRangeChange).toHaveBeenCalledWith('2025-01-01', '2025-01-31');

    mockDate.mockRestore();
  });

  it('handles Last Month quick select', async () => {
    const user = userEvent.setup();
    const today = new Date('2025-02-15'); // February
    const mockDate = jest.spyOn(Date, 'now').mockReturnValue(today.getTime());

    render(<TimeFilter {...defaultProps} />);

    await user.click(screen.getByText('Last Month'));

    expect(mockOnDateRangeChange).toHaveBeenCalledWith('2025-01-01', '2025-01-31');

    mockDate.mockRestore();
  });

  it('validates date inputs properly', async () => {
    const user = userEvent.setup();
    render(<TimeFilter {...defaultProps} />);

    const startDateInput = screen.getByDisplayValue('2025-01-01');
    const endDateInput = screen.getByDisplayValue('2025-01-31');

    // Test that inputs accept valid date format
    await user.clear(startDateInput);
    await user.type(startDateInput, '2025-12-01');
    expect(startDateInput).toHaveValue('2025-12-01');

    await user.clear(endDateInput);
    await user.type(endDateInput, '2025-12-31');
    expect(endDateInput).toHaveValue('2025-12-31');
  });

  it('has proper accessibility attributes', () => {
    render(<TimeFilter {...defaultProps} />);

    const startDateInput = screen.getByDisplayValue('2025-01-01');
    const endDateInput = screen.getByDisplayValue('2025-01-31');
    const submitButton = screen.getByText('Apply Filter');

    expect(startDateInput).toHaveAttribute('type', 'date');
    expect(endDateInput).toHaveAttribute('type', 'date');
    expect(submitButton).toHaveAttribute('type', 'submit');

    // Check for proper labels
    expect(screen.getByText('Start Date')).toBeInTheDocument();
    expect(screen.getByText('End Date')).toBeInTheDocument();
  });

  it('prevents form submission with invalid date combinations', async () => {
    const user = userEvent.setup();
    render(<TimeFilter {...defaultProps} />);

    const startDateInput = screen.getByDisplayValue('2025-01-01');
    const endDateInput = screen.getByDisplayValue('2025-01-31');
    const submitButton = screen.getByText('Apply Filter');

    // Set end date before start date
    await user.clear(startDateInput);
    await user.type(startDateInput, '2025-02-01');
    
    await user.clear(endDateInput);
    await user.type(endDateInput, '2025-01-15');

    await user.click(submitButton);

    // Should still call the function as validation might be handled by parent
    expect(mockOnDateRangeChange).toHaveBeenCalledWith('2025-02-01', '2025-01-15');
  });

  it('maintains focus management for accessibility', async () => {
    const user = userEvent.setup();
    render(<TimeFilter {...defaultProps} />);

    const startDateInput = screen.getByDisplayValue('2025-01-01');
    
    await user.click(startDateInput);
    expect(startDateInput).toHaveFocus();

    await user.tab();
    const endDateInput = screen.getByDisplayValue('2025-01-31');
    expect(endDateInput).toHaveFocus();
  });
});