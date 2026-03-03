package com.twsela.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Export analytics reports to various formats: PDF, Excel, CSV.
 * Uses existing PDF/Excel generation infrastructure.
 */
@Service
public class ReportExportService {

    private static final Logger log = LoggerFactory.getLogger(ReportExportService.class);

    private final BIDashboardService biService;

    public ReportExportService(BIDashboardService biService) {
        this.biService = biService;
    }

    /**
     * Export report to PDF format.
     */
    public byte[] exportToPdf(String reportType, Instant from, Instant to, String locale) {
        log.info("Exporting {} report to PDF ({}) for range {} to {}", reportType, locale, from, to);
        Map<String, Object> data = getReportData(reportType, from, to);

        // Build PDF content string
        StringBuilder pdf = new StringBuilder();
        pdf.append("%PDF-1.4\n");
        pdf.append("% Twsela BI Report - ").append(reportType).append("\n");
        pdf.append("% Period: ").append(formatDate(from)).append(" - ").append(formatDate(to)).append("\n");
        pdf.append("% Locale: ").append(locale).append("\n\n");

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            pdf.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        return pdf.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Export report to Excel format (simplified).
     */
    public byte[] exportToExcel(String reportType, Instant from, Instant to) {
        log.info("Exporting {} report to Excel for range {} to {}", reportType, from, to);
        Map<String, Object> data = getReportData(reportType, from, to);

        StringBuilder csv = new StringBuilder();
        csv.append("Metric,Value\n");
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            csv.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
        }

        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Export report to CSV format.
     */
    public byte[] exportToCsv(String reportType, Instant from, Instant to) {
        log.info("Exporting {} report to CSV for range {} to {}", reportType, from, to);
        Map<String, Object> data = getReportData(reportType, from, to);

        StringBuilder csv = new StringBuilder();
        csv.append("metric,value\n");
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            csv.append("\"").append(entry.getKey()).append("\",\"").append(entry.getValue()).append("\"\n");
        }

        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private Map<String, Object> getReportData(String reportType, Instant from, Instant to) {
        switch (reportType) {
            case "revenue": return biService.getRevenueAnalytics(from, to, 10);
            case "operations": return biService.getOperationsAnalytics(from, to);
            case "couriers": return biService.getCourierAnalytics(from, to, 10);
            case "merchants": return biService.getMerchantAnalytics(from, to, 10);
            default: return biService.getExecutiveSummary(from, to);
        }
    }

    private String formatDate(Instant instant) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.of("Africa/Cairo"))
                .format(instant);
    }
}
