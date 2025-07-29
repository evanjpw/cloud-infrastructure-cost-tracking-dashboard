package com.dashboard.service.interfaces;

import com.dashboard.model.report.Report;
import com.dashboard.model.report.ReportTemplate;
import com.dashboard.dto.report.GenerateReportRequest;
import com.dashboard.dto.report.ReportScheduleRequest;

import java.util.List;

/**
 * Service interface for report generation and management
 * Supports executive reports, cost analysis, and multi-format export
 */
public interface ReportService {
    
    /**
     * Generate a report based on configuration
     * @param request Report configuration including type, filters, and format
     * @return Generated report with data and metadata
     */
    Report generateReport(GenerateReportRequest request);
    
    /**
     * Get available report templates by type
     * @param reportType Optional filter for report type
     * @return List of report templates with configuration options
     */
    List<ReportTemplate> getReportTemplates(String reportType);
    
    /**
     * Get report by ID
     * @param reportId Report identifier
     * @return Report with full data and metadata
     */
    Report getReport(String reportId);
    
    /**
     * Get list of generated reports with filtering options
     * @param userId Optional user filter
     * @param reportType Optional type filter
     * @param limit Optional limit for results
     * @return List of reports with metadata
     */
    List<Report> getReports(String userId, String reportType, Integer limit);
    
    /**
     * Schedule recurring report generation
     * @param request Schedule configuration
     * @return Scheduled report configuration
     */
    Object scheduleReport(ReportScheduleRequest request);
    
    /**
     * Export report data in specified format
     * @param reportId Report identifier
     * @param format Export format (pdf, excel, csv, json)
     * @return Export result with download information
     */
    Object exportReport(String reportId, String format);
    
    /**
     * Get report analytics and usage statistics
     * @return Report generation statistics and trends
     */
    Object getReportAnalytics();
}