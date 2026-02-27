package com.twsela.web;

import com.twsela.service.BackupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/backup")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
@Tag(name = "Backup", description = "إدارة النسخ الاحتياطية للنظام")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @Operation(summary = "إنشاء نسخة احتياطية", description = "إنشاء نسخة احتياطية من قاعدة البيانات")
    @ApiResponse(responseCode = "200", description = "تم إنشاء النسخة")
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createBackup() {
        boolean success = backupService.createBackup();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("timestamp", java.time.Instant.now());
        
        if (success) {
            response.put("message", "Backup created successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Failed to create backup");
            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(summary = "استعادة نسخة احتياطية", description = "استعادة نسخة احتياطية من ملف")
    @ApiResponse(responseCode = "200", description = "تم الاستعادة")
    @PostMapping("/restore")
    public ResponseEntity<Map<String, Object>> restoreBackup(@RequestParam String backupFilePath) {
        boolean success = backupService.restoreBackup(backupFilePath);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("backupFilePath", backupFilePath);
        response.put("timestamp", java.time.Instant.now());
        
        if (success) {
            response.put("message", "Backup restored successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Failed to restore backup");
            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(summary = "حالة النسخ الاحتياطي", description = "عرض حالة عمليات النسخ الاحتياطي")
    @ApiResponse(responseCode = "200", description = "تم بنجاح")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getBackupStatus() {
        String status = backupService.getBackupStatus();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("status", status);
        response.put("timestamp", java.time.Instant.now());
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "اختبار خدمة النسخ الاحتياطي", description = "اختبار الاتصال بخدمة النسخ الاحتياطي")
    @ApiResponse(responseCode = "200", description = "تم الاختبار")
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testBackupService() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Backup Service Test");
        response.put("status", "Available");
        response.put("timestamp", java.time.Instant.now());
        response.put("endpoints", new String[]{
            "POST /api/backup/create",
            "POST /api/backup/restore",
            "GET /api/backup/status"
        });
        
        return ResponseEntity.ok(response);
    }
}
