package com.twsela.web;

import com.twsela.domain.SystemAuditLog;
import com.twsela.service.AuditService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
@Tag(name = "Audit", description = "سجلات المراجعة وتتبع العمليات")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @Operation(summary = "سجل المراجعة", description = "عرض سجلات المراجعة مع فلترة حسب التاريخ")
    @ApiResponse(responseCode = "200", description = "تم بنجاح")
    @GetMapping("/logs")
    public ResponseEntity<Map<String, Object>> getAuditLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long userId) {
        
        List<SystemAuditLog> logs;
            
            if (startDate != null && endDate != null) {
                Instant start = startDate.toInstant(ZoneOffset.UTC);
                Instant end = endDate.toInstant(ZoneOffset.UTC);
                
                if (action != null) {
                    logs = auditService.getAuditLogsByDateRange(start, end)
                        .stream()
                        .filter(log -> log.getActionType().equals(action))
                        .toList();
                } else {
                    logs = auditService.getAuditLogsByDateRange(start, end);
                }
            } else if (userId != null) {
                logs = auditService.getUserAuditLogs(userId);
            } else if (action != null) {
                logs = auditService.getAuditLogsByDateRange(
                    Instant.now().minusSeconds(86400), // Last 24 hours
                    Instant.now()
                ).stream()
                .filter(log -> log.getActionType().equals(action))
                .toList();
            } else {
                logs = auditService.getAuditLogsByDateRange(
                    Instant.now().minusSeconds(86400), // Last 24 hours
                    Instant.now()
                );
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("logs", logs);
            response.put("count", logs.size());
            
            return ResponseEntity.ok(response);
    }

    @Operation(summary = "سجل المراجعة لكيان", description = "عرض سجلات المراجعة لكيان معين")
    @ApiResponse(responseCode = "200", description = "تم بنجاح")
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<Map<String, Object>> getEntityAuditLogs(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        
        List<SystemAuditLog> logs = auditService.getEntityAuditLogs(entityType, entityId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("logs", logs);
        response.put("count", logs.size());
        response.put("entityType", entityType);
        response.put("entityId", entityId);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "سجل المراجعة لمستخدم", description = "عرض سجلات المراجعة لمستخدم معين")
    @ApiResponse(responseCode = "200", description = "تم بنجاح")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserAuditLogs(@PathVariable Long userId) {
        List<SystemAuditLog> logs = auditService.getUserAuditLogs(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("logs", logs);
        response.put("count", logs.size());
        response.put("userId", userId);
        
        return ResponseEntity.ok(response);
    }
}
