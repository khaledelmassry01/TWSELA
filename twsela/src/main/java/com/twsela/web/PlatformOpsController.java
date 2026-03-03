package com.twsela.web;

import com.twsela.domain.*;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.PlatformOpsService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.PlatformOpsDTO.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/platform")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public class PlatformOpsController {

    private final PlatformOpsService platformOpsService;
    private final AuthenticationHelper authenticationHelper;

    public PlatformOpsController(PlatformOpsService platformOpsService,
                                 AuthenticationHelper authenticationHelper) {
        this.platformOpsService = platformOpsService;
        this.authenticationHelper = authenticationHelper;
    }

    // ── Platform Metrics ──

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<List<PlatformMetric>>> getAllMetrics() {
        return ResponseEntity.ok(ApiResponse.ok(platformOpsService.getAllMetrics()));
    }

    @GetMapping("/metrics/name/{name}")
    public ResponseEntity<ApiResponse<List<PlatformMetric>>> getMetricsByName(@PathVariable String name) {
        return ResponseEntity.ok(ApiResponse.ok(platformOpsService.getMetricsByName(name)));
    }

    @PostMapping("/metrics")
    public ResponseEntity<ApiResponse<PlatformMetric>> createMetric(
            @Valid @RequestBody CreatePlatformMetricRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                platformOpsService.createMetric(request), "Platform metric recorded"));
    }

    // ── System Alerts ──

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<SystemAlert>>> getAllAlerts(
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(platformOpsService.getAllAlerts(tenantId)));
    }

    @GetMapping("/alerts/unacknowledged")
    public ResponseEntity<ApiResponse<List<SystemAlert>>> getUnacknowledgedAlerts() {
        return ResponseEntity.ok(ApiResponse.ok(platformOpsService.getUnacknowledgedAlerts()));
    }

    @PostMapping("/alerts")
    public ResponseEntity<ApiResponse<SystemAlert>> createAlert(
            @Valid @RequestBody CreateSystemAlertRequest request,
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(
                platformOpsService.createAlert(request, tenantId), "System alert created"));
    }

    @PatchMapping("/alerts/{id}/acknowledge")
    public ResponseEntity<ApiResponse<SystemAlert>> acknowledgeAlert(
            @PathVariable Long id, Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(
                platformOpsService.acknowledgeAlert(id, userId), "Alert acknowledged"));
    }

    // ── Maintenance Windows ──

    @GetMapping("/maintenance")
    public ResponseEntity<ApiResponse<List<MaintenanceWindow>>> getAllMaintenanceWindows(
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(platformOpsService.getAllMaintenanceWindows(tenantId)));
    }

    @GetMapping("/maintenance/{id}")
    public ResponseEntity<ApiResponse<MaintenanceWindow>> getMaintenanceWindowById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(platformOpsService.getMaintenanceWindowById(id)));
    }

    @PostMapping("/maintenance")
    public ResponseEntity<ApiResponse<MaintenanceWindow>> createMaintenanceWindow(
            @Valid @RequestBody CreateMaintenanceWindowRequest request,
            @RequestParam(required = false) Long tenantId,
            Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(
                platformOpsService.createMaintenanceWindow(request, userId, tenantId), "Maintenance window created"));
    }

    @DeleteMapping("/maintenance/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMaintenanceWindow(@PathVariable Long id) {
        platformOpsService.deleteMaintenanceWindow(id);
        return ResponseEntity.ok(ApiResponse.ok("Maintenance window deleted"));
    }
}
