package com.twsela.web;

import com.twsela.service.OfflineSyncService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.OfflineMobileDTO.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offline")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'COURIER')")
public class OfflineSyncController {

    private final OfflineSyncService syncService;

    public OfflineSyncController(OfflineSyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/queue")
    public ResponseEntity<ApiResponse<OfflineQueueResponse>> enqueue(
            @Valid @RequestBody CreateOfflineQueueRequest request,
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(syncService.enqueue(request, tenantId)));
    }

    @GetMapping("/queue/user/{userId}")
    public ResponseEntity<ApiResponse<List<OfflineQueueResponse>>> getPendingByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(syncService.getPendingByUser(userId)));
    }

    @PostMapping("/sync/sessions")
    public ResponseEntity<ApiResponse<SyncSessionResponse>> startSession(
            @Valid @RequestBody StartSyncSessionRequest request,
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(syncService.startSession(request, tenantId)));
    }

    @GetMapping("/sync/sessions/{id}")
    public ResponseEntity<ApiResponse<SyncSessionResponse>> getSession(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(syncService.getSessionById(id)));
    }

    @GetMapping("/sync/sessions/{sessionId}/conflicts")
    public ResponseEntity<ApiResponse<List<SyncConflictResponse>>> getConflicts(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.ok(syncService.getConflictsBySession(sessionId)));
    }

    @PatchMapping("/sync/conflicts/{id}/resolve")
    public ResponseEntity<ApiResponse<SyncConflictResponse>> resolveConflict(
            @PathVariable Long id, @Valid @RequestBody ResolveSyncConflictRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(syncService.resolveConflict(id, request)));
    }
}
