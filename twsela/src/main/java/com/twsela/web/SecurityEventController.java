package com.twsela.web;

import com.twsela.domain.AccountLockout;
import com.twsela.domain.SecurityEvent;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.AccountLockoutService;
import com.twsela.service.SecurityAuditService;
import com.twsela.service.SecurityEventService;
import com.twsela.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * متحكم الأحداث الأمنية وإدارة الحسابات المقفلة.
 */
@RestController
@RequestMapping("/api/security")
@Tag(name = "Security Events", description = "إدارة الأحداث الأمنية وقفل الحسابات")
public class SecurityEventController {

    private final SecurityEventService securityEventService;
    private final AccountLockoutService accountLockoutService;
    private final SecurityAuditService securityAuditService;
    private final AuthenticationHelper authHelper;

    public SecurityEventController(SecurityEventService securityEventService,
                                    AccountLockoutService accountLockoutService,
                                    SecurityAuditService securityAuditService,
                                    AuthenticationHelper authHelper) {
        this.securityEventService = securityEventService;
        this.accountLockoutService = accountLockoutService;
        this.securityAuditService = securityAuditService;
        this.authHelper = authHelper;
    }

    @GetMapping("/events")
    @Operation(summary = "قائمة الأحداث الأمنية")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<SecurityEvent>>> getEvents(
            @RequestParam(required = false) Long userId) {
        List<SecurityEvent> events;
        if (userId != null) {
            events = securityEventService.getEventsByUser(userId);
        } else {
            events = securityEventService.getActiveThreats();
        }
        return ResponseEntity.ok(ApiResponse.ok(events));
    }

    @GetMapping("/events/summary")
    @Operation(summary = "ملخص الأحداث الأمنية")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEventSummary() {
        Map<String, Object> summary = securityEventService.getEventSummary();
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @GetMapping("/events/threats")
    @Operation(summary = "التهديدات النشطة")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<SecurityEvent>>> getThreats() {
        List<SecurityEvent> threats = securityEventService.getActiveThreats();
        return ResponseEntity.ok(ApiResponse.ok(threats));
    }

    @GetMapping("/lockouts")
    @Operation(summary = "الحسابات المقفلة")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<AccountLockout>>> getLockouts() {
        List<AccountLockout> lockouts = accountLockoutService.getActiveLockouts();
        return ResponseEntity.ok(ApiResponse.ok(lockouts));
    }

    @PostMapping("/lockouts/{userId}/unlock")
    @Operation(summary = "فتح حساب مقفل")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AccountLockout>> unlockAccount(
            @PathVariable Long userId, Authentication authentication) {
        Long unlockedById = authHelper.getCurrentUserId(authentication);
        AccountLockout lockout = accountLockoutService.manualUnlock(userId, unlockedById);
        return ResponseEntity.ok(ApiResponse.ok(lockout, "تم فتح الحساب بنجاح"));
    }

    @GetMapping("/audit")
    @Operation(summary = "تقرير التدقيق الأمني")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSecurityAudit() {
        Map<String, Object> audit = securityAuditService.generateSecurityAudit();
        return ResponseEntity.ok(ApiResponse.ok(audit));
    }
}
