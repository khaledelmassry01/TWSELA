package com.twsela.web;

import com.twsela.domain.*;
import com.twsela.service.SystemHealthService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.PlatformOpsDTO.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public class SystemHealthController {

    private final SystemHealthService systemHealthService;

    public SystemHealthController(SystemHealthService systemHealthService) {
        this.systemHealthService = systemHealthService;
    }

    // ── Health Checks ──

    @GetMapping("/health-checks")
    public ResponseEntity<ApiResponse<List<SystemHealthCheck>>> getAllHealthChecks() {
        return ResponseEntity.ok(ApiResponse.ok(systemHealthService.getAllHealthChecks()));
    }

    @GetMapping("/health-checks/component/{component}")
    public ResponseEntity<ApiResponse<List<SystemHealthCheck>>> getByComponent(@PathVariable String component) {
        return ResponseEntity.ok(ApiResponse.ok(systemHealthService.getHealthChecksByComponent(component)));
    }

    @PostMapping("/health-checks")
    public ResponseEntity<ApiResponse<SystemHealthCheck>> createHealthCheck(
            @Valid @RequestBody CreateHealthCheckRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                systemHealthService.createHealthCheck(request), "Health check recorded"));
    }

    // ── Archive Policies ──

    @GetMapping("/archive-policies")
    public ResponseEntity<ApiResponse<List<ArchivePolicy>>> getAllArchivePolicies(
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(systemHealthService.getAllArchivePolicies(tenantId)));
    }

    @GetMapping("/archive-policies/{id}")
    public ResponseEntity<ApiResponse<ArchivePolicy>> getArchivePolicyById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(systemHealthService.getArchivePolicyById(id)));
    }

    @PostMapping("/archive-policies")
    public ResponseEntity<ApiResponse<ArchivePolicy>> createArchivePolicy(
            @Valid @RequestBody CreateArchivePolicyRequest request,
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(
                systemHealthService.createArchivePolicy(request, tenantId), "Archive policy created"));
    }

    @DeleteMapping("/archive-policies/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArchivePolicy(@PathVariable Long id) {
        systemHealthService.deleteArchivePolicy(id);
        return ResponseEntity.ok(ApiResponse.ok("Archive policy deleted"));
    }

    // ── Archived Records ──

    @GetMapping("/archive-policies/{policyId}/records")
    public ResponseEntity<ApiResponse<List<ArchivedRecord>>> getArchivedRecords(@PathVariable Long policyId) {
        return ResponseEntity.ok(ApiResponse.ok(systemHealthService.getArchivedRecordsByPolicy(policyId)));
    }

    // ── Cleanup Tasks ──

    @GetMapping("/cleanup-tasks")
    public ResponseEntity<ApiResponse<List<CleanupTask>>> getAllCleanupTasks(
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(systemHealthService.getAllCleanupTasks(tenantId)));
    }

    @GetMapping("/cleanup-tasks/{id}")
    public ResponseEntity<ApiResponse<CleanupTask>> getCleanupTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(systemHealthService.getCleanupTaskById(id)));
    }

    @PostMapping("/cleanup-tasks")
    public ResponseEntity<ApiResponse<CleanupTask>> createCleanupTask(
            @Valid @RequestBody CreateCleanupTaskRequest request,
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(
                systemHealthService.createCleanupTask(request, tenantId), "Cleanup task created"));
    }

    @DeleteMapping("/cleanup-tasks/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCleanupTask(@PathVariable Long id) {
        systemHealthService.deleteCleanupTask(id);
        return ResponseEntity.ok(ApiResponse.ok("Cleanup task deleted"));
    }
}
