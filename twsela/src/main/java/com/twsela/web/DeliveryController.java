package com.twsela.web;

import com.twsela.domain.DeliveryAttempt;
import com.twsela.domain.DeliveryProof;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.DeliveryAttemptService;
import com.twsela.service.DeliveryProofService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.DeliveryDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Controller for delivery proof and delivery attempts.
 */
@RestController
@RequestMapping("/api/delivery")
@Tag(name = "Delivery", description = "إثبات التسليم ومحاولات التسليم")
public class DeliveryController {

    private final DeliveryProofService proofService;
    private final DeliveryAttemptService attemptService;
    private final AuthenticationHelper authHelper;

    public DeliveryController(DeliveryProofService proofService,
                               DeliveryAttemptService attemptService,
                               AuthenticationHelper authHelper) {
        this.proofService = proofService;
        this.attemptService = attemptService;
        this.authHelper = authHelper;
    }

    // ══════════════════════════════════════════════════════════
    // Proof of Delivery
    // ══════════════════════════════════════════════════════════

    @Operation(summary = "رفع إثبات التسليم")
    @PostMapping(value = "/{shipmentId}/proof", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('COURIER')")
    public ResponseEntity<ApiResponse<ProofResponse>> submitProof(
            @PathVariable Long shipmentId,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            @RequestPart(value = "signature", required = false) MultipartFile signature,
            @RequestParam(required = false) String recipientName,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String notes,
            Authentication auth) {
        Long courierId = authHelper.getCurrentUserId(auth);
        DeliveryProof proof = proofService.submitProof(
                shipmentId, photo, signature, latitude, longitude, recipientName, notes, courierId);
        return ResponseEntity.ok(ApiResponse.ok(toProofResponse(proof), "تم رفع إثبات التسليم بنجاح"));
    }

    @Operation(summary = "عرض إثبات التسليم")
    @GetMapping("/{shipmentId}/proof")
    public ResponseEntity<ApiResponse<ProofResponse>> getProof(@PathVariable Long shipmentId) {
        DeliveryProof proof = proofService.getProof(shipmentId);
        return ResponseEntity.ok(ApiResponse.ok(toProofResponse(proof), "تفاصيل إثبات التسليم"));
    }

    // ══════════════════════════════════════════════════════════
    // Delivery Attempts
    // ══════════════════════════════════════════════════════════

    @Operation(summary = "تسجيل محاولة تسليم فاشلة")
    @PostMapping("/{shipmentId}/attempt")
    @PreAuthorize("hasRole('COURIER')")
    public ResponseEntity<ApiResponse<AttemptResponse>> recordAttempt(
            @PathVariable Long shipmentId,
            @Valid @RequestBody RecordAttemptRequest request,
            Authentication auth) {
        Long courierId = authHelper.getCurrentUserId(auth);
        DeliveryAttempt attempt = attemptService.recordFailedAttempt(
                shipmentId, request.failureReason(),
                request.latitude(), request.longitude(), request.notes(), courierId);
        return ResponseEntity.ok(ApiResponse.ok(toAttemptResponse(attempt), "تم تسجيل محاولة التسليم"));
    }

    @Operation(summary = "عرض محاولات التسليم لشحنة")
    @GetMapping("/{shipmentId}/attempts")
    public ResponseEntity<ApiResponse<List<AttemptResponse>>> getAttempts(@PathVariable Long shipmentId) {
        List<DeliveryAttempt> attempts = attemptService.getAttempts(shipmentId);
        List<AttemptResponse> responses = attempts.stream().map(this::toAttemptResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(responses, "محاولات التسليم"));
    }

    @Operation(summary = "تقرير أسباب فشل التسليم")
    @GetMapping("/admin/failures")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFailureReport(
            @RequestParam Instant from, @RequestParam Instant to) {
        Map<String, Object> report = attemptService.getFailureReport(from, to);
        return ResponseEntity.ok(ApiResponse.ok(report, "تقرير فشل التسليم"));
    }

    // ── Mappers ─────────────────────────────────────────────

    private ProofResponse toProofResponse(DeliveryProof p) {
        return new ProofResponse(
                p.getId(),
                p.getShipment().getId(),
                p.getShipment().getTrackingNumber(),
                p.getPhotoUrl(),
                p.getSignatureUrl(),
                p.getRecipientName(),
                p.getLatitude(),
                p.getLongitude(),
                p.getNotes(),
                p.getDeliveredAt(),
                p.getCapturedBy().getId(),
                p.getCapturedBy().getName()
        );
    }

    private AttemptResponse toAttemptResponse(DeliveryAttempt a) {
        return new AttemptResponse(
                a.getId(),
                a.getShipment().getId(),
                a.getAttemptNumber(),
                a.getStatus().name(),
                a.getFailureReason() != null ? a.getFailureReason().name() : null,
                a.getPhotoUrl(),
                a.getLatitude(),
                a.getLongitude(),
                a.getNotes(),
                a.getAttemptedAt(),
                a.getNextAttemptDate(),
                a.getCourier().getId(),
                a.getCourier().getName()
        );
    }
}
