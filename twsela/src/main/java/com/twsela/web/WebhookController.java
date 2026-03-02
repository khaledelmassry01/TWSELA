package com.twsela.web;

import com.twsela.domain.User;
import com.twsela.domain.WebhookEvent;
import com.twsela.domain.WebhookSubscription;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.WebhookService;
import com.twsela.web.dto.WebhookDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for webhook subscription management.
 */
@RestController
@RequestMapping("/api/webhooks")
@Tag(name = "Webhooks", description = "إدارة إشعارات Webhook")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final WebhookService webhookService;
    private final AuthenticationHelper authHelper;

    public WebhookController(WebhookService webhookService, AuthenticationHelper authHelper) {
        this.webhookService = webhookService;
        this.authHelper = authHelper;
    }

    /**
     * Create a new webhook subscription.
     */
    @Operation(summary = "إنشاء اشتراك webhook")
    @PostMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<WebhookDTO>> createSubscription(
            @Valid @RequestBody WebhookDTO.CreateWebhookRequest request,
            Authentication authentication) {
        User user = authHelper.getCurrentUser(authentication);
        WebhookSubscription sub = webhookService.subscribe(user, request.getUrl(), request.getEvents());
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(toDTO(sub), "تم إنشاء الاشتراك بنجاح"));
    }

    /**
     * List my webhook subscriptions.
     */
    @Operation(summary = "قائمة اشتراكاتي")
    @GetMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<List<WebhookDTO>>> getSubscriptions(
            Authentication authentication) {
        Long userId = authHelper.getCurrentUserId(authentication);
        List<WebhookDTO> dtos = webhookService.getSubscriptions(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(dtos));
    }

    /**
     * Get subscription details.
     */
    @Operation(summary = "تفاصيل الاشتراك")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<WebhookDTO>> getSubscription(@PathVariable Long id) {
        WebhookSubscription sub = webhookService.getSubscription(id);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(toDTO(sub)));
    }

    /**
     * Deactivate a webhook subscription.
     */
    @Operation(summary = "إلغاء الاشتراك")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<Void>> deleteSubscription(
            @PathVariable Long id, Authentication authentication) {
        Long userId = authHelper.getCurrentUserId(authentication);
        webhookService.unsubscribe(id, userId);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(null, "تم إلغاء الاشتراك"));
    }

    /**
     * Get events for a subscription.
     */
    @Operation(summary = "سجل أحداث الاشتراك")
    @GetMapping("/{id}/events")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<List<WebhookDTO.WebhookEventDTO>>> getEvents(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<WebhookEvent> events = webhookService.getEvents(id, PageRequest.of(page, size));
        List<WebhookDTO.WebhookEventDTO> dtos = events.getContent().stream()
                .map(this::toEventDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(dtos));
    }

    /**
     * Send a test event.
     */
    @Operation(summary = "إرسال حدث تجريبي")
    @PostMapping("/{id}/test")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<WebhookDTO.WebhookEventDTO>> sendTest(
            @PathVariable Long id) {
        WebhookEvent evt = webhookService.sendTestEvent(id);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(toEventDTO(evt), "تم إرسال حدث تجريبي"));
    }

    /**
     * Retry failed events (admin only).
     */
    @Operation(summary = "إعادة محاولة الأحداث الفاشلة")
    @PostMapping("/retry")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<Integer>> retryFailed() {
        int count = webhookService.retryFailed();
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(count, "تمت إعادة المحاولة لـ " + count + " حدث"));
    }

    // ── Mapping ─────────────────────────────────────────────────

    private WebhookDTO toDTO(WebhookSubscription sub) {
        WebhookDTO dto = new WebhookDTO();
        dto.setId(sub.getId());
        dto.setUrl(sub.getUrl());
        dto.setEvents(sub.getEvents() != null ? Arrays.asList(sub.getEvents().split(",")) : List.of());
        dto.setActive(sub.isActive());
        dto.setCreatedAt(sub.getCreatedAt());
        return dto;
    }

    private WebhookDTO.WebhookEventDTO toEventDTO(WebhookEvent evt) {
        WebhookDTO.WebhookEventDTO dto = new WebhookDTO.WebhookEventDTO();
        dto.setId(evt.getId());
        dto.setEventType(evt.getEventType());
        dto.setStatus(evt.getStatus() != null ? evt.getStatus().name() : null);
        dto.setAttempts(evt.getAttempts());
        dto.setResponseCode(evt.getResponseCode());
        dto.setCreatedAt(evt.getCreatedAt());
        dto.setLastAttemptAt(evt.getLastAttemptAt());
        return dto;
    }
}
