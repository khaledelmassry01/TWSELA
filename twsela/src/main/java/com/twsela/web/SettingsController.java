package com.twsela.web;

import com.twsela.domain.SystemSetting;
import com.twsela.domain.User;
import com.twsela.repository.SystemSettingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Settings Controller — persists user preferences in system_settings table.
 */
@RestController
@RequestMapping("/api/settings")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MERCHANT', 'COURIER', 'WAREHOUSE_MANAGER')")
@Tag(name = "Settings", description = "إدارة الإعدادات وتفضيلات المستخدم")
public class SettingsController {

    private static final Logger log = LoggerFactory.getLogger(SettingsController.class);

    private static final Map<String, Object> DEFAULTS = Map.ofEntries(
        Map.entry("language", "ar"),
        Map.entry("timezone", "Asia/Riyadh"),
        Map.entry("dateFormat", "dd/MM/yyyy"),
        Map.entry("timeFormat", "24h"),
        Map.entry("currency", "EGP"),
        Map.entry("darkMode", "false"),
        Map.entry("themeColor", "blue"),
        Map.entry("fontSize", "16"),
        Map.entry("emailNotifications", "true"),
        Map.entry("smsNotifications", "false"),
        Map.entry("pushNotifications", "true")
    );

    private final SystemSettingRepository settingRepository;

    public SettingsController(SystemSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @Operation(summary = "عرض الإعدادات", description = "الحصول على إعدادات المستخدم")
    @ApiResponse(responseCode = "200", description = "تم بنجاح")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getSettings(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        // Start with defaults
        Map<String, Object> settings = new HashMap<>(DEFAULTS);

        // Override with user-specific saved values
        List<SystemSetting> saved = settingRepository.findByUserId(currentUser.getId());
        for (SystemSetting s : saved) {
            settings.put(s.getSettingKey(), s.getSettingValue());
        }

        return ResponseEntity.ok(settings);
    }

    @Operation(summary = "حفظ الإعدادات", description = "حفظ إعدادات المستخدم")
    @ApiResponse(responseCode = "200", description = "تم الحفظ")
    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> saveSettings(@RequestBody Map<String, Object> settings, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Long userId = currentUser.getId();

        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            SystemSetting setting = settingRepository.findByUserIdAndSettingKey(userId, entry.getKey())
                .orElse(new SystemSetting(currentUser, entry.getKey(), null));
            setting.setSettingValue(String.valueOf(entry.getValue()));
            setting.setUpdatedAt(Instant.now());
            settingRepository.save(setting);
        }

        log.info("Settings saved for user {}", userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "تم حفظ الإعدادات بنجاح");
        response.put("settings", settings);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "إعادة تعيين الإعدادات", description = "إعادة الإعدادات للقيم الافتراضية")
    @ApiResponse(responseCode = "200", description = "تم إعادة التعيين")
    @PostMapping("/reset")
    @Transactional
    public ResponseEntity<Map<String, Object>> resetSettings(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        settingRepository.deleteByUserId(currentUser.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "تم إعادة تعيين الإعدادات بنجاح");
        response.put("settings", DEFAULTS);

        return ResponseEntity.ok(response);
    }
}
