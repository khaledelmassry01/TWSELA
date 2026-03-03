package com.twsela.web;

import com.twsela.service.DeliveryExperienceService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.RecipientExperienceDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Delivery Experience", description = "إدارة تجربة التوصيل")
public class DeliveryExperienceController {

    private final DeliveryExperienceService experienceService;

    public DeliveryExperienceController(DeliveryExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    // ── Redirects ──
    @GetMapping("/api/delivery/redirects/shipment/{shipmentId}")
    @Operation(summary = "جلب طلبات إعادة التوجيه")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MERCHANT','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> getRedirects(@PathVariable Long shipmentId) {
        return ResponseEntity.ok(ApiResponse.ok(experienceService.getRedirectsByShipment(shipmentId), "تم جلب الطلبات"));
    }

    @GetMapping("/api/delivery/redirects/pending")
    @Operation(summary = "جلب الطلبات المعلقة")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> getPendingRedirects() {
        return ResponseEntity.ok(ApiResponse.ok(experienceService.getPendingRedirects(), "تم جلب الطلبات المعلقة"));
    }

    @PostMapping("/api/delivery/redirects")
    @Operation(summary = "إنشاء طلب إعادة توجيه")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MERCHANT')")
    public ResponseEntity<ApiResponse<?>> createRedirect(@Valid @RequestBody CreateDeliveryRedirectRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(experienceService.createRedirect(req), "تم إنشاء طلب إعادة التوجيه"));
    }

    @PatchMapping("/api/delivery/redirects/{id}/process")
    @Operation(summary = "معالجة طلب إعادة التوجيه")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> processRedirect(@PathVariable Long id,
                                                           @RequestParam String status,
                                                           @RequestParam Long processedById) {
        return ResponseEntity.ok(ApiResponse.ok(experienceService.processRedirect(id, status, processedById), "تم معالجة الطلب"));
    }

    // ── Surveys ──
    @GetMapping("/api/delivery/surveys/shipment/{shipmentId}")
    @Operation(summary = "جلب استبيان الشحنة")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MERCHANT')")
    public ResponseEntity<ApiResponse<?>> getSurvey(@PathVariable Long shipmentId) {
        return ResponseEntity.ok(ApiResponse.ok(experienceService.getSurveyByShipment(shipmentId), "تم جلب الاستبيان"));
    }

    @PostMapping("/api/delivery/surveys")
    @Operation(summary = "تقديم استبيان رضا")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MERCHANT','COURIER')")
    public ResponseEntity<ApiResponse<?>> submitSurvey(@Valid @RequestBody CreateSatisfactionSurveyRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(experienceService.submitSurvey(req), "تم تقديم الاستبيان"));
    }
}
