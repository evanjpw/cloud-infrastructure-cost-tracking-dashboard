// PDF Generation Utility
// Provides functions to generate PDF reports from cost data

export const generatePDFReport = async (reportConfig, costData) => {
  // In a real implementation, this would use a library like jsPDF or Puppeteer
  // For now, we'll simulate the PDF generation process
  
  console.log('Generating PDF report...', reportConfig);
  
  // Simulate PDF generation delay
  await new Promise(resolve => setTimeout(resolve, 2000));
  
  // Create a mock PDF blob
  const pdfContent = generatePDFContent(reportConfig, costData);
  const blob = new Blob([pdfContent], { type: 'application/pdf' });
  
  return {
    blob,
    filename: generateFilename(reportConfig, 'pdf'),
    size: blob.size,
    pages: calculatePages(reportConfig)
  };
};

export const generateExcelReport = async (reportConfig, costData) => {
  console.log('Generating Excel report...', reportConfig);
  
  // Simulate Excel generation delay
  await new Promise(resolve => setTimeout(resolve, 1500));
  
  // Create a mock Excel blob
  const excelContent = generateExcelContent(reportConfig, costData);
  const blob = new Blob([excelContent], { 
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
  });
  
  return {
    blob,
    filename: generateFilename(reportConfig, 'xlsx'),
    size: blob.size,
    sheets: ['Summary', 'Detailed Data', 'Charts']
  };
};

export const generateCSVReport = async (reportConfig, costData) => {
  console.log('Generating CSV report...', reportConfig);
  
  // Simulate CSV generation delay
  await new Promise(resolve => setTimeout(resolve, 500));
  
  const csvContent = generateCSVContent(reportConfig, costData);
  const blob = new Blob([csvContent], { type: 'text/csv' });
  
  return {
    blob,
    filename: generateFilename(reportConfig, 'csv'),
    size: blob.size,
    rows: costData.length + 1 // +1 for header
  };
};

export const generateJSONReport = async (reportConfig, costData) => {
  console.log('Generating JSON report...', reportConfig);
  
  // Simulate JSON generation delay
  await new Promise(resolve => setTimeout(resolve, 300));
  
  const jsonContent = generateJSONContent(reportConfig, costData);
  const blob = new Blob([jsonContent], { type: 'application/json' });
  
  return {
    blob,
    filename: generateFilename(reportConfig, 'json'),
    size: blob.size,
    records: costData.length
  };
};

const generatePDFContent = (reportConfig, costData) => {
  // This would be replaced with actual PDF generation using jsPDF or similar
  // For now, return a simple text representation
  
  const header = `%PDF-1.4
1 0 obj
<<
/Type /Catalog
/Pages 2 0 R
>>
endobj

2 0 obj
<<
/Type /Pages
/Kids [3 0 R]
/Count 1
>>
endobj

3 0 obj
<<
/Type /Page
/Parent 2 0 R
/MediaBox [0 0 612 792]
/Contents 4 0 R
/Resources <<
/Font <<
/F1 5 0 R
>>
>>
>>
endobj

4 0 obj
<<
/Length 200
>>
stream
BT
/F1 12 Tf
50 750 Td
(${reportConfig.title}) Tj
0 -20 Td
(Generated: ${new Date().toLocaleDateString()}) Tj
0 -30 Td
(Report Type: ${reportConfig.type}) Tj
0 -20 Td
(Time Range: ${reportConfig.timeRange}) Tj
0 -20 Td
(Total Records: ${costData.length}) Tj
ET
endstream
endobj

5 0 obj
<<
/Type /Font
/Subtype /Type1
/BaseFont /Helvetica
>>
endobj

xref
0 6
0000000000 65535 f 
0000000009 00000 n 
0000000058 00000 n 
0000000115 00000 n 
0000000274 00000 n 
0000000526 00000 n 
trailer
<<
/Size 6
/Root 1 0 R
>>
startxref
625
%%EOF`;

  return header;
};

const generateExcelContent = (reportConfig, costData) => {
  // This would be replaced with actual Excel generation using libraries like SheetJS
  // For now, return a simple representation
  
  const summary = {
    title: reportConfig.title,
    generatedAt: new Date().toISOString(),
    type: reportConfig.type,
    timeRange: reportConfig.timeRange,
    totalRecords: costData.length,
    totalCost: costData.reduce((sum, item) => sum + (item.totalCost || 0), 0)
  };
  
  return JSON.stringify({
    summary,
    data: costData.slice(0, 1000), // Limit for simulation
    metadata: {
      version: '1.0',
      format: 'Excel',
      sheets: ['Summary', 'Data', 'Charts']
    }
  });
};

const generateCSVContent = (reportConfig, costData) => {
  // Generate actual CSV content
  if (!costData || costData.length === 0) {
    return 'No data available';
  }
  
  // Get all unique keys from the data
  const allKeys = new Set();
  costData.forEach(item => {
    Object.keys(item).forEach(key => allKeys.add(key));
  });
  
  const headers = Array.from(allKeys);
  const csvRows = [headers.join(',')];
  
  costData.forEach(item => {
    const row = headers.map(header => {
      const value = item[header];
      if (value === null || value === undefined) return '';
      if (typeof value === 'string' && value.includes(',')) {
        return `"${value.replace(/"/g, '""')}"`;
      }
      return value;
    });
    csvRows.push(row.join(','));
  });
  
  // Add report metadata as comments
  const metadata = [
    `# Report: ${reportConfig.title}`,
    `# Generated: ${new Date().toISOString()}`,
    `# Type: ${reportConfig.type}`,
    `# Time Range: ${reportConfig.timeRange}`,
    `# Total Records: ${costData.length}`,
    ''
  ];
  
  return metadata.join('\n') + csvRows.join('\n');
};

const generateJSONContent = (reportConfig, costData) => {
  const report = {
    metadata: {
      title: reportConfig.title,
      description: reportConfig.description,
      type: reportConfig.type,
      generatedAt: new Date().toISOString(),
      timeRange: reportConfig.timeRange,
      groupBy: reportConfig.groupBy,
      format: 'json',
      version: '1.0'
    },
    summary: {
      totalRecords: costData.length,
      totalCost: costData.reduce((sum, item) => sum + (item.totalCost || 0), 0),
      dateRange: {
        start: reportConfig.customStartDate || getDateFromRange(reportConfig.timeRange).start,
        end: reportConfig.customEndDate || getDateFromRange(reportConfig.timeRange).end
      }
    },
    data: costData,
    aggregations: generateAggregations(costData, reportConfig.groupBy)
  };
  
  return JSON.stringify(report, null, 2);
};

const generateAggregations = (costData, groupBy) => {
  const aggregations = {};
  
  // Group by specified dimension
  const groups = {};
  costData.forEach(item => {
    const key = item[groupBy] || 'Unknown';
    if (!groups[key]) {
      groups[key] = {
        count: 0,
        totalCost: 0,
        items: []
      };
    }
    groups[key].count += 1;
    groups[key].totalCost += item.totalCost || 0;
    groups[key].items.push(item);
  });
  
  aggregations[groupBy] = groups;
  
  // Add summary statistics
  const costs = costData.map(item => item.totalCost || 0);
  aggregations.statistics = {
    total: costs.reduce((sum, cost) => sum + cost, 0),
    average: costs.length > 0 ? costs.reduce((sum, cost) => sum + cost, 0) / costs.length : 0,
    minimum: costs.length > 0 ? Math.min(...costs) : 0,
    maximum: costs.length > 0 ? Math.max(...costs) : 0,
    count: costs.length
  };
  
  return aggregations;
};

const generateFilename = (reportConfig, extension) => {
  const timestamp = new Date().toISOString().split('T')[0];
  const sanitizedTitle = reportConfig.title
    .replace(/[^a-zA-Z0-9\s]/g, '')
    .replace(/\s+/g, '_')
    .toLowerCase();
  
  return `${sanitizedTitle}_${timestamp}.${extension}`;
};

const calculatePages = (reportConfig) => {
  // Estimate number of pages based on report type and content
  const basePages = {
    cost_summary: 3,
    detailed_breakdown: 5,
    executive_summary: 2,
    budget_performance: 4,
    cost_optimization: 6,
    chargeback: 4
  };
  
  return basePages[reportConfig.type] || 3;
};

const getDateFromRange = (timeRange) => {
  const now = new Date();
  const start = new Date();
  
  switch (timeRange) {
    case 'last_7_days':
      start.setDate(now.getDate() - 7);
      break;
    case 'last_30_days':
      start.setDate(now.getDate() - 30);
      break;
    case 'last_90_days':
      start.setDate(now.getDate() - 90);
      break;
    case 'current_month':
      start.setDate(1);
      break;
    case 'last_month':
      start.setMonth(now.getMonth() - 1, 1);
      now.setDate(0); // Last day of previous month
      break;
    default:
      start.setDate(now.getDate() - 30);
  }
  
  return {
    start: start.toISOString().split('T')[0],
    end: now.toISOString().split('T')[0]
  };
};

// Utility function to download generated reports
export const downloadReport = (reportResult) => {
  const url = URL.createObjectURL(reportResult.blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = reportResult.filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
};

// Format file size for display
export const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes';
  
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

export default {
  generatePDFReport,
  generateExcelReport,
  generateCSVReport,
  generateJSONReport,
  downloadReport,
  formatFileSize
};