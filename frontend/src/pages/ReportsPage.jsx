import React, { useState, useEffect } from "react";
import { colors, getCardStyle } from "../styles/colors";
import { textStyles } from "../styles/typography";
import ReportBuilder from "../components/reports/ReportBuilder";
import ExecutiveDashboard from "../components/reports/ExecutiveDashboard";
import ReportViewer from "../components/reports/ReportViewer";
import { 
  generatePDFReport, 
  generateExcelReport, 
  generateCSVReport, 
  generateJSONReport,
  downloadReport,
  formatFileSize 
} from "../utils/pdfGenerator";

const ReportsPage = () => {
  const [activeTab, setActiveTab] = useState('builder'); // builder, executive, viewer
  const [costData, setCostData] = useState([]);
  const [budgets, setBudgets] = useState([]);
  const [teams, setTeams] = useState([]);
  const [services, setServices] = useState([]);
  const [generatedReport, setGeneratedReport] = useState(null);
  const [isGenerating, setIsGenerating] = useState(false);
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 768);
    };
    
    checkMobile();
    window.addEventListener('resize', checkMobile);
    fetchData();
    
    return () => window.removeEventListener('resize', checkMobile);
  }, []);

  const fetchData = async () => {
    try {
      // Fetch cost data
      const costResponse = await fetch('http://localhost:8080/api/cost-data');
      
      if (!costResponse.ok) {
        throw new Error(`HTTP error! status: ${costResponse.status}`);
      }
      
      const costData = await costResponse.json();
      setCostData(costData);

      // Extract unique teams and services
      const uniqueTeams = [...new Set(costData.map(item => item.team))].filter(Boolean);
      const uniqueServices = [...new Set(costData.map(item => item.service || item.serviceName))].filter(Boolean);
      
      setTeams(uniqueTeams.map(team => ({ value: team, label: team })));
      setServices(uniqueServices.map(service => ({ value: service, label: service })));

    } catch (error) {
      console.error('Failed to fetch data:', error);
      console.log('Using fallback demo data for reports...');
      
      // Use fallback demo data when backend is not available
      const fallbackCostData = [
        { id: 1, service: 'EC2', team: 'Development', totalCost: 2500, date: '2025-01-15', provider: 'AWS', region: 'us-east-1' },
        { id: 2, service: 'RDS', team: 'Development', totalCost: 1200, date: '2025-01-15', provider: 'AWS', region: 'us-east-1' },
        { id: 3, service: 'S3', team: 'Platform', totalCost: 450, date: '2025-01-15', provider: 'AWS', region: 'us-east-1' },
        { id: 4, service: 'Lambda', team: 'Platform', totalCost: 320, date: '2025-01-15', provider: 'AWS', region: 'us-east-1' },
        { id: 5, service: 'CloudFront', team: 'Frontend', totalCost: 180, date: '2025-01-15', provider: 'AWS', region: 'global' },
        { id: 6, service: 'EKS', team: 'DevOps', totalCost: 3200, date: '2025-01-15', provider: 'AWS', region: 'us-west-2' },
        { id: 7, service: 'Azure VM', team: 'Analytics', totalCost: 1800, date: '2025-01-15', provider: 'Azure', region: 'eastus' },
        { id: 8, service: 'Compute Engine', team: 'ML', totalCost: 2100, date: '2025-01-15', provider: 'GCP', region: 'us-central1' },
      ];
      
      setCostData(fallbackCostData);
      
      const uniqueTeams = [...new Set(fallbackCostData.map(item => item.team))];
      const uniqueServices = [...new Set(fallbackCostData.map(item => item.service))];
      
      setTeams(uniqueTeams.map(team => ({ value: team, label: team })));
      setServices(uniqueServices.map(service => ({ value: service, label: service })));
    }
    
    // Load budgets from localStorage (demo data) - this always works
    try {
      const savedBudgets = localStorage.getItem('budgets');
      if (savedBudgets) {
        setBudgets(JSON.parse(savedBudgets));
      } else {
        // Create sample budgets for demonstration
        const sampleBudgets = [
          {
            id: '1',
            name: 'Development Team Budget',
            amount: 15000,
            spent: 12500,
            scope: 'team',
            scopeValue: 'Development',
            period: 'monthly',
            alertThreshold: 80
          },
          {
            id: '2',
            name: 'Production Environment',
            amount: 25000,
            spent: 18700,
            scope: 'service',
            scopeValue: 'EC2',
            period: 'monthly',
            alertThreshold: 90
          }
        ];
        setBudgets(sampleBudgets);
        localStorage.setItem('budgets', JSON.stringify(sampleBudgets));
      }
    } catch (error) {
      console.error('Failed to load budgets:', error);
      setBudgets([]);
    }
  };

  const handleGenerateReport = async (reportConfig) => {
    setIsGenerating(true);
    
    try {
      // Filter cost data based on report configuration
      let filteredData = costData;
      
      if (reportConfig.teams.length > 0) {
        filteredData = filteredData.filter(item => reportConfig.teams.includes(item.team));
      }
      
      if (reportConfig.services.length > 0) {
        filteredData = filteredData.filter(item => 
          reportConfig.services.includes(item.service || item.serviceName)
        );
      }

      // Generate report based on format
      let reportResult;
      switch (reportConfig.format) {
        case 'pdf':
          reportResult = await generatePDFReport(reportConfig, filteredData);
          break;
        case 'excel':
          reportResult = await generateExcelReport(reportConfig, filteredData);
          break;
        case 'csv':
          reportResult = await generateCSVReport(reportConfig, filteredData);
          break;
        case 'json':
          reportResult = await generateJSONReport(reportConfig, filteredData);
          break;
        default:
          throw new Error(`Unsupported format: ${reportConfig.format}`);
      }

      // Create report object for viewer
      const report = {
        ...reportConfig,
        generatedAt: new Date().toISOString(),
        data: filteredData,
        size: formatFileSize(reportResult.size),
        downloadInfo: reportResult
      };

      setGeneratedReport(report);
      setActiveTab('viewer');

      // Auto-download if configured
      if (reportConfig.schedule === 'none') {
        downloadReport(reportResult);
      }

    } catch (error) {
      console.error('Report generation failed:', error);
      alert('Failed to generate report. Please try again.');
    } finally {
      setIsGenerating(false);
    }
  };

  const handleDownloadReport = (report) => {
    if (report.downloadInfo) {
      downloadReport(report.downloadInfo);
    }
  };

  const handleShareReport = (report) => {
    // In a real implementation, this would create a shareable link
    const shareData = {
      title: report.title,
      text: `${report.title} - Cost Report`,
      url: window.location.href
    };

    if (navigator.share) {
      navigator.share(shareData);
    } else {
      // Fallback: copy to clipboard
      navigator.clipboard.writeText(window.location.href);
      alert('Report link copied to clipboard!');
    }
  };

  const handleScheduleReport = (report) => {
    alert(`Report scheduling would be configured here. Report: "${report.title}" with ${report.schedule} frequency.`);
  };

  const tabs = [
    { id: 'builder', label: 'Report Builder', icon: '📝' },
    { id: 'executive', label: 'Executive Dashboard', icon: '👔' },
    { id: 'viewer', label: 'Report Viewer', icon: '📄' }
  ];

  return (
    <div style={{ maxWidth: "1400px", margin: "0 auto" }}>
      {/* Page Header */}
      <div style={{ marginBottom: "2rem" }}>
        <h2 style={textStyles.pageTitle(colors.text.primary)}>
          Reports & Analytics
        </h2>
        <p style={textStyles.body(colors.text.secondary)}>
          Generate, schedule, and export comprehensive cost reports
        </p>
      </div>

      {/* Tab Navigation */}
      <div style={{
        ...getCardStyle(),
        padding: '0',
        marginBottom: '2rem',
        overflow: 'hidden'
      }}>
        <div style={{
          display: 'flex',
          borderBottom: `1px solid ${colors.gray[200]}`
        }}>
          {tabs.map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              style={{
                padding: '1rem 1.5rem',
                backgroundColor: activeTab === tab.id ? colors.primary[50] : 'transparent',
                color: activeTab === tab.id ? colors.primary[600] : colors.text.secondary,
                border: 'none',
                borderBottom: activeTab === tab.id ? `3px solid ${colors.primary[500]}` : '3px solid transparent',
                fontSize: '0.95rem',
                fontWeight: activeTab === tab.id ? '600' : '500',
                cursor: 'pointer',
                transition: 'all 0.2s ease',
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
                minWidth: isMobile ? 'auto' : '140px',
                justifyContent: 'center'
              }}
            >
              <span style={{ fontSize: '1.1em' }}>{tab.icon}</span>
              {!isMobile && tab.label}
            </button>
          ))}
        </div>
      </div>

      {/* Tab Content */}
      {activeTab === 'builder' && (
        <ReportBuilder
          teams={teams}
          services={services}
          onGenerateReport={handleGenerateReport}
          isMobile={isMobile}
        />
      )}

      {activeTab === 'executive' && (
        <ExecutiveDashboard
          costData={costData}
          budgets={budgets}
          timeRange="last_30_days"
          isMobile={isMobile}
        />
      )}

      {activeTab === 'viewer' && (
        <ReportViewer
          report={generatedReport}
          onDownload={handleDownloadReport}
          onShare={handleShareReport}
          onSchedule={handleScheduleReport}
          isMobile={isMobile}
        />
      )}

      {/* Loading Overlay */}
      {isGenerating && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            ...getCardStyle(),
            padding: '2rem',
            textAlign: 'center',
            backgroundColor: colors.white,
            minWidth: '300px'
          }}>
            <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>⏳</div>
            <h3 style={{ ...textStyles.cardTitle(colors.text.primary), marginBottom: '1rem' }}>
              Generating Report...
            </h3>
            <p style={{ ...textStyles.body(colors.text.secondary), margin: 0 }}>
              Please wait while we prepare your report
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default ReportsPage;