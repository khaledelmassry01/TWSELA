package com.twsela.web;

import com.twsela.service.AnalyticsService;
import com.twsela.web.dto.AnalyticsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Advanced analytics endpoints for the owner/admin dashboard.
 */
@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
@Tag(name = "Analytics", description = "التحليلات المتقدمة")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Operation(summary = "تقرير الإيرادات حسب الفترة")
    @GetMapping("/revenue")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<AnalyticsDTO.RevenueReport>> getRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        AnalyticsDTO.RevenueReport report = analyticsService.getRevenueByPeriod(startDate, endDate);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(report));
    }

    @Operation(summary = "توزيع حالات الشحنات")
    @GetMapping("/status-distribution")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<List<AnalyticsDTO.StatusDistribution>>> getStatusDistribution(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AnalyticsDTO.StatusDistribution> result = analyticsService.getStatusDistribution(startDate, endDate);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(result));
    }

    @Operation(summary = "ترتيب أداء المناديب")
    @GetMapping("/courier-ranking")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<List<AnalyticsDTO.CourierPerformance>>> getCourierRanking(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        List<AnalyticsDTO.CourierPerformance> result = analyticsService.getCourierPerformanceRanking(startDate, endDate, limit);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(result));
    }

    @Operation(summary = "أفضل التجار")
    @GetMapping("/top-merchants")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<List<AnalyticsDTO.TopMerchant>>> getTopMerchants(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        List<AnalyticsDTO.TopMerchant> result = analyticsService.getTopMerchants(startDate, endDate, limit);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(result));
    }
}
