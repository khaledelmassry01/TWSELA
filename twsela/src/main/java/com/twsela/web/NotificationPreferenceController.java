package com.twsela.web;

import com.twsela.domain.*;
import com.twsela.domain.NotificationPreference.DigestMode;
import com.twsela.repository.NotificationPreferenceRepository;
import com.twsela.repository.UserRepository;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.PushNotificationService;
import com.twsela.web.dto.AdvancedNotificationDTO.*;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

/**
 * REST controller for notification preferences and device token management.
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification Preferences", description = "إدارة تفضيلات الإشعارات وأجهزة الدفع")
public class NotificationPreferenceController {

    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final PushNotificationService pushService;
    private final AuthenticationHelper authHelper;

    public NotificationPreferenceController(NotificationPreferenceRepository preferenceRepository,
                                             UserRepository userRepository,
                                             PushNotificationService pushService,
                                             AuthenticationHelper authHelper) {
        this.preferenceRepository = preferenceRepository;
        this.userRepository = userRepository;
        this.pushService = pushService;
        this.authHelper = authHelper;
    }

    @GetMapping("/preferences")
    @Operation(summary = "الحصول على تفضيلات الإشعارات")
    public ResponseEntity<ApiResponse<PreferenceResponse>> getPreferences(Authentication auth) {
        Long userId = authHelper.getCurrentUserId(auth);
        NotificationPreference pref = preferenceRepository.findByUserId(userId)
                .orElse(null);

        if (pref == null) {
            return ResponseEntity.ok(ApiResponse.ok(new PreferenceResponse(
                    null, userId, Map.of(), null, null, DigestMode.NONE, null, null)));
        }

        return ResponseEntity.ok(ApiResponse.ok(toPreferenceResponse(pref)));
    }

    @PutMapping("/preferences")
    @Operation(summary = "تعديل تفضيلات الإشعارات")
    public ResponseEntity<ApiResponse<PreferenceResponse>> updatePreferences(
            @Valid @RequestBody PreferenceRequest request, Authentication auth) {
        Long userId = authHelper.getCurrentUserId(auth);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        NotificationPreference pref = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    NotificationPreference np = new NotificationPreference();
                    np.setUser(user);
                    return np;
                });

        // Convert map to JSON
        if (request.enabledChannels() != null) {
            pref.setEnabledChannelsJson(mapToJson(request.enabledChannels()));
        }
        if (request.quietHoursStart() != null) pref.setQuietHoursStart(request.quietHoursStart());
        if (request.quietHoursEnd() != null) pref.setQuietHoursEnd(request.quietHoursEnd());
        if (request.digestMode() != null) pref.setDigestMode(request.digestMode());
        pref.setUpdatedAt(Instant.now());

        pref = preferenceRepository.save(pref);
        return ResponseEntity.ok(ApiResponse.ok(toPreferenceResponse(pref), "تم تحديث التفضيلات"));
    }

    @PutMapping("/preferences/pause")
    @Operation(summary = "إيقاف مؤقت للإشعارات")
    public ResponseEntity<ApiResponse<PreferenceResponse>> pauseNotifications(
            @Valid @RequestBody PauseRequest request, Authentication auth) {
        Long userId = authHelper.getCurrentUserId(auth);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        NotificationPreference pref = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    NotificationPreference np = new NotificationPreference();
                    np.setUser(user);
                    return np;
                });

        pref.setPausedUntil(request.pauseUntil());
        pref.setUpdatedAt(Instant.now());
        pref = preferenceRepository.save(pref);

        return ResponseEntity.ok(ApiResponse.ok(toPreferenceResponse(pref), "تم إيقاف الإشعارات مؤقتاً"));
    }

    @PostMapping("/devices")
    @Operation(summary = "تسجيل جهاز للإشعارات الفورية")
    public ResponseEntity<ApiResponse<DeviceTokenResponse>> registerDevice(
            @Valid @RequestBody RegisterDeviceRequest request, Authentication auth) {
        Long userId = authHelper.getCurrentUserId(auth);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        DeviceToken dt = pushService.registerToken(userId, request.token(), request.platform(), user);
        return ResponseEntity.ok(ApiResponse.ok(
                new DeviceTokenResponse(dt.getId(), dt.getToken(), dt.getPlatform(), dt.isActive(), dt.getLastUsedAt()),
                "تم تسجيل الجهاز"));
    }

    @DeleteMapping("/devices/{token}")
    @Operation(summary = "إلغاء تسجيل جهاز")
    public ResponseEntity<ApiResponse<Void>> unregisterDevice(@PathVariable String token, Authentication auth) {
        Long userId = authHelper.getCurrentUserId(auth);
        pushService.unregisterToken(userId, token);
        return ResponseEntity.ok(ApiResponse.ok(null, "تم إلغاء تسجيل الجهاز"));
    }

    // ── Mappers ────────────────────────────────────────

    private PreferenceResponse toPreferenceResponse(NotificationPreference pref) {
        Map<String, List<String>> channels = parseChannelsJson(pref.getEnabledChannelsJson());
        return new PreferenceResponse(
                pref.getId(),
                pref.getUser().getId(),
                channels,
                pref.getQuietHoursStart(),
                pref.getQuietHoursEnd(),
                pref.getDigestMode(),
                pref.getPausedUntil(),
                pref.getUpdatedAt()
        );
    }

    private String mapToJson(Map<String, List<String>> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":[");
            for (int i = 0; i < entry.getValue().size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(entry.getValue().get(i)).append("\"");
            }
            sb.append("]");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<String>> parseChannelsJson(String json) {
        if (json == null || json.isBlank() || json.equals("{}")) {
            return Map.of();
        }
        // Lightweight JSON parsing
        Map<String, List<String>> result = new LinkedHashMap<>();
        try {
            String content = json.trim();
            if (content.startsWith("{")) content = content.substring(1);
            if (content.endsWith("}")) content = content.substring(0, content.length() - 1);

            // Split by ], to get key:[values] pairs
            String[] pairs = content.split("],");
            for (String pair : pairs) {
                pair = pair.trim();
                if (pair.isEmpty()) continue;
                int colonIdx = pair.indexOf(":");
                if (colonIdx == -1) continue;
                String key = pair.substring(0, colonIdx).trim().replace("\"", "");
                String arrayPart = pair.substring(colonIdx + 1).trim();
                arrayPart = arrayPart.replace("[", "").replace("]", "");
                List<String> values = new ArrayList<>();
                for (String v : arrayPart.split(",")) {
                    String val = v.trim().replace("\"", "");
                    if (!val.isEmpty()) values.add(val);
                }
                result.put(key, values);
            }
        } catch (Exception e) {
            return Map.of();
        }
        return result;
    }
}
