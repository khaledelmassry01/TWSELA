package com.twsela.web;

import com.twsela.domain.DeadLetterEvent;
import com.twsela.service.DeadLetterService;
import com.twsela.security.AuthenticationHelper;
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
 * متحكم الأحداث الميتة.
 */
@RestController
@RequestMapping("/api/events/dead-letter")
@Tag(name = "Dead Letter Events", description = "إدارة الأحداث الفاشلة")
public class DeadLetterController {

    private final DeadLetterService deadLetterService;
    private final AuthenticationHelper authenticationHelper;

    public DeadLetterController(DeadLetterService deadLetterService,
                                 AuthenticationHelper authenticationHelper) {
        this.deadLetterService = deadLetterService;
        this.authenticationHelper = authenticationHelper;
    }

    @GetMapping
    @Operation(summary = "الأحداث الفاشلة غير المحلولة")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<DeadLetterEvent>>> getUnresolved() {
        List<DeadLetterEvent> events = deadLetterService.getUnresolved();
        return ResponseEntity.ok(ApiResponse.ok(events));
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "إعادة محاولة حدث فاشل")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DeadLetterEvent>> retry(@PathVariable Long id) {
        DeadLetterEvent retried = deadLetterService.retry(id);
        return ResponseEntity.ok(ApiResponse.ok(retried, "تمت إعادة المحاولة"));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "حل حدث فاشل")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DeadLetterEvent>> resolve(
            @PathVariable Long id, Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        DeadLetterEvent resolved = deadLetterService.resolve(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(resolved, "تم حل الحدث"));
    }

    @GetMapping("/stats")
    @Operation(summary = "إحصائيات الأحداث الفاشلة")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = deadLetterService.getStats();
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
