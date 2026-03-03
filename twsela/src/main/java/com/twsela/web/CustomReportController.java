package com.twsela.web;

import com.twsela.domain.*;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.CustomReportService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.ReportingAnalyticsDTO.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public class CustomReportController {

    private final CustomReportService customReportService;
    private final AuthenticationHelper authenticationHelper;

    public CustomReportController(CustomReportService customReportService,
                                  AuthenticationHelper authenticationHelper) {
        this.customReportService = customReportService;
        this.authenticationHelper = authenticationHelper;
    }

    // ── Custom Reports ──

    @GetMapping("/custom")
    public ResponseEntity<ApiResponse<List<CustomReport>>> getAllReports(
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(customReportService.getAllReports(tenantId)));
    }

    @GetMapping("/custom/{id}")
    public ResponseEntity<ApiResponse<CustomReport>> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(customReportService.getReportById(id)));
    }

    @PostMapping("/custom")
    public ResponseEntity<ApiResponse<CustomReport>> createReport(
            @Valid @RequestBody CreateCustomReportRequest request,
            @RequestParam(required = false) Long tenantId,
            Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(
                customReportService.createReport(request, userId, tenantId), "Custom report created"));
    }

    @PutMapping("/custom/{id}")
    public ResponseEntity<ApiResponse<CustomReport>> updateReport(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomReportRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                customReportService.updateReport(id, request), "Custom report updated"));
    }

    @DeleteMapping("/custom/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable Long id) {
        customReportService.deleteReport(id);
        return ResponseEntity.ok(ApiResponse.ok("Custom report deleted"));
    }

    // ── Report Schedules ──

    @GetMapping("/schedules")
    public ResponseEntity<ApiResponse<List<ReportSchedule>>> getSchedules(
            @RequestParam Long customReportId) {
        return ResponseEntity.ok(ApiResponse.ok(customReportService.getSchedulesByReport(customReportId)));
    }

    @PostMapping("/schedules")
    public ResponseEntity<ApiResponse<ReportSchedule>> createSchedule(
            @Valid @RequestBody CreateReportScheduleRequest request,
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(
                customReportService.createSchedule(request, tenantId), "Report schedule created"));
    }

    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable Long id) {
        customReportService.deleteSchedule(id);
        return ResponseEntity.ok(ApiResponse.ok("Report schedule deleted"));
    }

    // ── Report Executions ──

    @GetMapping("/executions")
    public ResponseEntity<ApiResponse<List<ReportExecution>>> getExecutions(
            @RequestParam Long customReportId) {
        return ResponseEntity.ok(ApiResponse.ok(customReportService.getExecutionsByReport(customReportId)));
    }

    @PostMapping("/executions")
    public ResponseEntity<ApiResponse<ReportExecution>> createExecution(
            @Valid @RequestBody CreateReportExecutionRequest request,
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(
                customReportService.createExecution(request, tenantId), "Report execution started"));
    }

    // ── Saved Filters ──

    @GetMapping("/filters")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MERCHANT')")
    public ResponseEntity<ApiResponse<List<SavedFilter>>> getFilters(Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(customReportService.getFiltersByUser(userId)));
    }

    @PostMapping("/filters")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MERCHANT')")
    public ResponseEntity<ApiResponse<SavedFilter>> createFilter(
            @Valid @RequestBody CreateSavedFilterRequest request,
            @RequestParam(required = false) Long tenantId,
            Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(
                customReportService.createFilter(request, userId, tenantId), "Saved filter created"));
    }

    @DeleteMapping("/filters/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MERCHANT')")
    public ResponseEntity<ApiResponse<Void>> deleteFilter(@PathVariable Long id) {
        customReportService.deleteFilter(id);
        return ResponseEntity.ok(ApiResponse.ok("Saved filter deleted"));
    }
}
