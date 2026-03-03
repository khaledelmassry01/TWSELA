package com.twsela.web;

import com.twsela.service.BIDashboardService;
import com.twsela.service.KPISnapshotService;
import com.twsela.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * REST controller for BI dashboard analytics endpoints.
 */
@RestController
@RequestMapping("/api/bi-analytics")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
@Tag(name = "BI Dashboard", description = "لوحة تحليلات الأعمال")
public class BIDashboardController {

    private static final ZoneId CAIRO = ZoneId.of("Africa/Cairo");

    private final BIDashboardService biService;
    private final KPISnapshotService kpiService;

    public BIDashboardController(BIDashboardService biService,
                                  KPISnapshotService kpiService) {
        this.biService = biService;
        this.kpiService = kpiService;
    }

    // ══════════════════════════════════════════════════════════
    // Executive Summary
    // ══════════════════════════════════════════════════════════

    @GetMapping("/summary")
    @Operation(summary = "ملخص تنفيذي — مؤشرات الأداء الرئيسية")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExecutiveSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        Map<String, Object> summary = biService.getExecutiveSummary(from, to);
        return ResponseEntity.ok(ApiResponse.ok(summary, "تم جلب الملخص التنفيذي"));
    }

    // ══════════════════════════════════════════════════════════
    // Revenue Analytics
    // ══════════════════════════════════════════════════════════

    @GetMapping("/revenue")
    @Operation(summary = "تحليلات الإيرادات — حسب المنطقة والتاجر")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRevenueAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "10") int top) {
        Map<String, Object> report = biService.getRevenueAnalytics(from, to, top);
        return ResponseEntity.ok(ApiResponse.ok(report, "تم جلب تحليلات الإيرادات"));
    }

    // ══════════════════════════════════════════════════════════
    // Operations Analytics
    // ══════════════════════════════════════════════════════════

    @GetMapping("/operations")
    @Operation(summary = "تحليلات العمليات — الأداء والتوصيل")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOperationsAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        Map<String, Object> report = biService.getOperationsAnalytics(from, to);
        return ResponseEntity.ok(ApiResponse.ok(report, "تم جلب تحليلات العمليات"));
    }

    // ══════════════════════════════════════════════════════════
    // Courier Analytics
    // ══════════════════════════════════════════════════════════

    @GetMapping("/couriers")
    @Operation(summary = "تحليلات المناديب — الاستغلال والأداء")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCourierAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "10") int top) {
        Map<String, Object> report = biService.getCourierAnalytics(from, to, top);
        return ResponseEntity.ok(ApiResponse.ok(report, "تم جلب تحليلات المناديب"));
    }

    // ══════════════════════════════════════════════════════════
    // Merchant Analytics
    // ══════════════════════════════════════════════════════════

    @GetMapping("/merchants")
    @Operation(summary = "تحليلات التجار — الاحتفاظ والنمو")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMerchantAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "10") int top) {
        Map<String, Object> report = biService.getMerchantAnalytics(from, to, top);
        return ResponseEntity.ok(ApiResponse.ok(report, "تم جلب تحليلات التجار"));
    }

    // ══════════════════════════════════════════════════════════
    // KPI Trends
    // ══════════════════════════════════════════════════════════

    @GetMapping("/kpi/trends")
    @Operation(summary = "اتجاهات مؤشرات الأداء — بيانات يومية")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getKPITrends(
            @RequestParam String metric,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<Map<String, Object>> trend = kpiService.getTrend(metric, from, to);
        return ResponseEntity.ok(ApiResponse.ok(trend, "تم جلب اتجاهات المؤشرات"));
    }
}
