package com.twsela.web;

import com.twsela.service.BackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/backup")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
public class BackupController {

    @Autowired
    private BackupService backupService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createBackup() {
        try {
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
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Backup creation failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/restore")
    public ResponseEntity<Map<String, Object>> restoreBackup(@RequestParam String backupFilePath) {
        try {
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
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Backup restore failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getBackupStatus() {
        try {
            String status = backupService.getBackupStatus();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("status", status);
            response.put("timestamp", java.time.Instant.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to get backup status");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

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
