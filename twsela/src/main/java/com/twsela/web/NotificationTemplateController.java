package com.twsela.web;

import com.twsela.domain.NotificationTemplate;
import com.twsela.domain.NotificationType;
import com.twsela.repository.NotificationTemplateRepository;
import com.twsela.service.NotificationAnalyticsService;
import com.twsela.service.NotificationDispatcher;
import com.twsela.web.dto.AdvancedNotificationDTO.*;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for managing notification templates and viewing analytics (admin only).
 */
@RestController
@RequestMapping("/api/admin/notifications")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
@Tag(name = "Notification Templates (Admin)", description = "إدارة قوالب الإشعارات والتحليلات")
public class NotificationTemplateController {

    private final NotificationTemplateRepository templateRepository;
    private final NotificationDispatcher dispatcher;
    private final NotificationAnalyticsService analyticsService;

    public NotificationTemplateController(NotificationTemplateRepository templateRepository,
                                           NotificationDispatcher dispatcher,
                                           NotificationAnalyticsService analyticsService) {
        this.templateRepository = templateRepository;
        this.dispatcher = dispatcher;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/templates")
    @Operation(summary = "عرض جميع قوالب الإشعارات")
    public ResponseEntity<ApiResponse<List<TemplateResponse>>> getAllTemplates() {
        List<TemplateResponse> templates = templateRepository.findAll().stream()
                .map(this::toTemplateResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(templates));
    }

    @GetMapping("/templates/{eventType}")
    @Operation(summary = "عرض قوالب حدث محدد")
    public ResponseEntity<ApiResponse<List<TemplateResponse>>> getTemplatesByEvent(
            @PathVariable NotificationType eventType) {
        List<TemplateResponse> templates = templateRepository.findByEventType(eventType).stream()
                .map(this::toTemplateResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(templates));
    }

    @PutMapping("/templates/{id}")
    @Operation(summary = "تعديل قالب إشعار")
    public ResponseEntity<ApiResponse<TemplateResponse>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTemplateRequest request) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationTemplate", "id", id));

        if (request.subjectTemplate() != null) template.setSubjectTemplate(request.subjectTemplate());
        if (request.bodyTemplateAr() != null) template.setBodyTemplateAr(request.bodyTemplateAr());
        if (request.bodyTemplateEn() != null) template.setBodyTemplateEn(request.bodyTemplateEn());
        if (request.active() != null) template.setActive(request.active());
        template.setUpdatedAt(Instant.now());

        template = templateRepository.save(template);
        return ResponseEntity.ok(ApiResponse.ok(toTemplateResponse(template), "تم تحديث القالب"));
    }

    @PostMapping("/templates/{id}/test")
    @Operation(summary = "إرسال إشعار تجريبي باستخدام قالب")
    public ResponseEntity<ApiResponse<String>> testTemplate(
            @PathVariable Long id,
            @Valid @RequestBody TestNotificationRequest request) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationTemplate", "id", id));

        Map<String, String> vars = request.templateVars() != null ? request.templateVars() : Map.of();
        dispatcher.dispatch(request.recipientUserId(), template.getEventType(), vars);

        return ResponseEntity.ok(ApiResponse.ok("تم إرسال الإشعار التجريبي بنجاح"));
    }

    @GetMapping("/analytics")
    @Operation(summary = "إحصائيات تسليم الإشعارات")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalytics(
            @RequestParam(defaultValue = "7") int days) {
        Instant from = Instant.now().minus(days, ChronoUnit.DAYS);
        Instant to = Instant.now();

        Map<String, Object> stats = analyticsService.getDeliveryStats(from, to);
        stats.put("emailOpenRate", analyticsService.getOpenRate(from, to));
        stats.put("channelPerformance", analyticsService.getChannelPerformance(from, to));

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    // ── Mapper ─────────────────────────────────────────

    private TemplateResponse toTemplateResponse(NotificationTemplate t) {
        return new TemplateResponse(
                t.getId(), t.getEventType(), t.getChannel(),
                t.getSubjectTemplate(), t.getBodyTemplateAr(), t.getBodyTemplateEn(),
                t.isActive(), t.getUpdatedAt()
        );
    }
}
