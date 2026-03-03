package com.twsela.web;

import com.twsela.service.ReportExportService;
import com.twsela.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * REST controller for exporting analytics reports in PDF, Excel, or CSV format.
 */
@RestController
@RequestMapping("/api/reports/export")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
@Tag(name = "Report Export", description = "تصدير التقارير بصيغ متعددة")
public class ReportExportController {

    private final ReportExportService exportService;

    public ReportExportController(ReportExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/{reportType}")
    @Operation(summary = "تصدير تقرير بصيغة PDF أو Excel أو CSV")
    public ResponseEntity<byte[]> exportReport(
            @PathVariable String reportType,
            @RequestParam(defaultValue = "pdf") String format,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "ar") String locale) {

        byte[] data;
        String contentType;
        String extension;

        switch (format.toLowerCase()) {
            case "excel":
            case "xlsx":
                data = exportService.exportToExcel(reportType, from, to);
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                extension = "xlsx";
                break;
            case "csv":
                data = exportService.exportToCsv(reportType, from, to);
                contentType = "text/csv; charset=UTF-8";
                extension = "csv";
                break;
            case "pdf":
            default:
                data = exportService.exportToPdf(reportType, from, to, locale);
                contentType = "application/pdf";
                extension = "pdf";
                break;
        }

        String filename = String.format("twsela_%s_%s.%s",
                reportType,
                DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.of("Africa/Cairo")).format(from),
                extension);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(data);
    }
}
