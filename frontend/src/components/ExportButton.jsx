import React, { useState } from 'react';
import { colors, getCardStyle } from '../styles/colors';
import { textStyles } from '../styles/typography';

const ExportButton = ({ 
  data, 
  filename = 'cost-data-export', 
  title = 'Export Data',
  icon = '📊',
  isMobile = false 
}) => {
  const [isExporting, setIsExporting] = useState(false);

  const formatDataForCSV = (data) => {
    if (!data || data.length === 0) {
      return 'No data to export';
    }

    // Get all unique keys from the data to create comprehensive headers
    const allKeys = new Set();
    data.forEach(item => {
      Object.keys(item).forEach(key => allKeys.add(key));
    });

    const headers = Array.from(allKeys).sort();
    
    // Create CSV content
    const csvContent = [
      // Header row
      headers.join(','),
      // Data rows
      ...data.map(item => 
        headers.map(header => {
          const value = item[header];
          if (value === null || value === undefined) {
            return '';
          }
          // Handle strings with commas or quotes
          if (typeof value === 'string' && (value.includes(',') || value.includes('"'))) {
            return `"${value.replace(/"/g, '""')}"`;
          }
          return value;
        }).join(',')
      )
    ].join('\n');

    return csvContent;
  };

  const generateTimestamp = () => {
    const now = new Date();
    return now.toISOString().slice(0, 19).replace(/[:-]/g, '').replace('T', '_');
  };

  const handleExport = async () => {
    setIsExporting(true);
    
    try {
      // Add a small delay to show the loading state
      await new Promise(resolve => setTimeout(resolve, 500));
      
      const csvContent = formatDataForCSV(data);
      const timestamp = generateTimestamp();
      const finalFilename = `${filename}_${timestamp}.csv`;
      
      // Create blob and download
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      const link = document.createElement('a');
      
      if (link.download !== undefined) {
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', finalFilename);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
      }
      
      console.log(`Exported ${data.length} records to ${finalFilename}`);
    } catch (error) {
      console.error('Export failed:', error);
      alert('Export failed. Please try again.');
    } finally {
      setIsExporting(false);
    }
  };

  const buttonStyle = {
    backgroundColor: isExporting ? colors.primary[100] : colors.primary[500],
    color: isExporting ? colors.primary[600] : colors.white,
    border: `1px solid ${colors.primary[500]}`,
    borderRadius: '6px',
    padding: isMobile ? '0.75rem 1rem' : '0.75rem 1.5rem',
    cursor: isExporting ? 'not-allowed' : 'pointer',
    fontSize: isMobile ? '0.875rem' : '0.9rem',
    fontWeight: '600',
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
    transition: 'all 0.2s ease',
    transform: isExporting ? 'scale(0.98)' : 'scale(1)',
    boxShadow: isExporting ? 'none' : '0 2px 4px rgba(33, 150, 243, 0.2)',
  };

  const hoverStyle = !isExporting ? {
    backgroundColor: colors.primary[600],
    transform: 'scale(1.02)',
    boxShadow: '0 4px 8px rgba(33, 150, 243, 0.3)',
  } : {};

  return (
    <button
      onClick={handleExport}
      disabled={isExporting || !data || data.length === 0}
      style={buttonStyle}
      onMouseEnter={(e) => {
        if (!isExporting) {
          Object.assign(e.target.style, hoverStyle);
        }
      }}
      onMouseLeave={(e) => {
        if (!isExporting) {
          Object.assign(e.target.style, buttonStyle);
        }
      }}
      title={`Export ${data?.length || 0} records to CSV`}
    >
      <span style={{ fontSize: '1.1em' }}>
        {isExporting ? '⏳' : icon}
      </span>
      <span>
        {isExporting ? 'Exporting...' : title}
      </span>
      {!isExporting && data && data.length > 0 && (
        <span style={{
          backgroundColor: colors.primary[700],
          color: colors.white,
          borderRadius: '12px',
          padding: '0.2rem 0.5rem',
          fontSize: '0.75rem',
          fontWeight: '600',
          minWidth: '20px',
          textAlign: 'center'
        }}>
          {data.length}
        </span>
      )}
    </button>
  );
};

export default ExportButton;