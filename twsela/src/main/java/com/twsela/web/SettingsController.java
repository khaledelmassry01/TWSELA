package com.twsela.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Settings Controller for managing user preferences and application settings
 */
@RestController
@RequestMapping("/api/settings")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MERCHANT', 'COURIER', 'WAREHOUSE_MANAGER')")
public class SettingsController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSettings(Authentication authentication) {
        Map<String, Object> settings = new HashMap<>();
        
        // Default settings
        settings.put("language", "ar");
        settings.put("timezone", "Asia/Riyadh");
        settings.put("dateFormat", "dd/MM/yyyy");
        settings.put("timeFormat", "24h");
        settings.put("currency", "EGP");
        settings.put("darkMode", false);
        settings.put("themeColor", "blue");
        settings.put("fontSize", "16");
        settings.put("emailNotifications", true);
        settings.put("smsNotifications", false);
        settings.put("pushNotifications", true);
        
        return ResponseEntity.ok(settings);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> saveSettings(@RequestBody Map<String, Object> settings, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // In a real application, you would save these settings to the database
            // For now, we'll just return success
            
            response.put("success", true);
            response.put("message", "تم حفظ الإعدادات بنجاح");
            response.put("settings", settings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "فشل في حفظ الإعدادات: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetSettings(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Reset to default settings
            Map<String, Object> defaultSettings = new HashMap<>();
            defaultSettings.put("language", "ar");
            defaultSettings.put("timezone", "Asia/Riyadh");
            defaultSettings.put("darkMode", false);
            defaultSettings.put("themeColor", "blue");
            defaultSettings.put("fontSize", "16");
            
            response.put("success", true);
            response.put("message", "تم إعادة تعيين الإعدادات بنجاح");
            response.put("settings", defaultSettings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "فشل في إعادة تعيين الإعدادات: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
