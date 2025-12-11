package com.twsela.web;

import com.twsela.domain.SystemAuditLog;
import com.twsela.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @GetMapping("/logs")
    public ResponseEntity<Map<String, Object>> getAuditLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long userId) {
        
        try {
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
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "حدث خطأ أثناء تحميل سجلات التدقيق: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<Map<String, Object>> getEntityAuditLogs(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        
        try {
            List<SystemAuditLog> logs = auditService.getEntityAuditLogs(entityType, entityId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("logs", logs);
            response.put("count", logs.size());
            response.put("entityType", entityType);
            response.put("entityId", entityId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "حدث خطأ أثناء تحميل سجلات الكيان: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserAuditLogs(@PathVariable Long userId) {
        try {
            List<SystemAuditLog> logs = auditService.getUserAuditLogs(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("logs", logs);
            response.put("count", logs.size());
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "حدث خطأ أثناء تحميل سجلات المستخدم: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
