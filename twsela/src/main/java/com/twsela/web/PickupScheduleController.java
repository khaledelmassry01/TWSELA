package com.twsela.web;

import com.twsela.domain.PickupSchedule;
import com.twsela.domain.PickupSchedule.PickupStatus;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.PickupScheduleService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.DeliveryDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for pickup scheduling operations.
 */
@RestController
@RequestMapping("/api/pickups")
@Tag(name = "Pickups", description = "جدولة مواعيد استلام الشحنات")
public class PickupScheduleController {

    private final PickupScheduleService pickupService;
    private final AuthenticationHelper authHelper;

    public PickupScheduleController(PickupScheduleService pickupService,
                                     AuthenticationHelper authHelper) {
        this.pickupService = pickupService;
        this.authHelper = authHelper;
    }

    @Operation(summary = "جدولة موعد استلام جديد")
    @PostMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<PickupResponse>> schedulePickup(
            @Valid @RequestBody SchedulePickupRequest request, Authentication auth) {
        Long merchantId = authHelper.getCurrentUserId(auth);
        PickupSchedule pickup = pickupService.schedulePickup(
                merchantId, request.pickupDate(), request.timeSlot(),
                request.address(), request.latitude(), request.longitude(),
                request.estimatedShipments(), request.notes());
        return ResponseEntity.ok(ApiResponse.ok(toResponse(pickup), "تم جدولة الاستلام بنجاح"));
    }

    @Operation(summary = "مواعيد الاستلام الخاصة بي")
    @GetMapping("/my")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<Page<PickupResponse>>> getMyPickups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        Long merchantId = authHelper.getCurrentUserId(auth);
        Page<PickupSchedule> pickups = pickupService.getMerchantPickups(merchantId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(pickups.map(this::toResponse), "مواعيد الاستلام"));
    }

    @Operation(summary = "مواعيد استلام اليوم للمندوب")
    @GetMapping("/today")
    @PreAuthorize("hasRole('COURIER')")
    public ResponseEntity<ApiResponse<List<PickupResponse>>> getCourierTodayPickups(Authentication auth) {
        Long courierId = authHelper.getCurrentUserId(auth);
        List<PickupSchedule> pickups = pickupService.getCourierPickups(courierId, LocalDate.now());
        List<PickupResponse> responses = pickups.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(responses, "مواعيد استلام اليوم"));
    }

    @Operation(summary = "تعيين مندوب لموعد استلام")
    @PutMapping("/{id}/assign/{courierId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PickupResponse>> assignCourier(
            @PathVariable Long id, @PathVariable Long courierId) {
        PickupSchedule pickup = pickupService.assignCourier(id, courierId);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(pickup), "تم تعيين المندوب"));
    }

    @Operation(summary = "بدء الاستلام")
    @PutMapping("/{id}/start")
    @PreAuthorize("hasRole('COURIER')")
    public ResponseEntity<ApiResponse<PickupResponse>> startPickup(@PathVariable Long id) {
        PickupSchedule pickup = pickupService.startPickup(id);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(pickup), "تم بدء الاستلام"));
    }

    @Operation(summary = "إتمام الاستلام")
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('COURIER')")
    public ResponseEntity<ApiResponse<PickupResponse>> completePickup(@PathVariable Long id) {
        PickupSchedule pickup = pickupService.completePickup(id);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(pickup), "تم إتمام الاستلام"));
    }

    @Operation(summary = "إلغاء موعد استلام")
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PickupResponse>> cancelPickup(@PathVariable Long id) {
        PickupSchedule pickup = pickupService.cancelPickup(id);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(pickup), "تم إلغاء الاستلام"));
    }

    @Operation(summary = "كل مواعيد الاستلام (الإدارة)")
    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<PickupResponse>>> getAllPickups(
            @RequestParam(required = false) PickupStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<PickupSchedule> pickups;
        if (status != null) {
            pickups = pickupService.getPickupsByStatus(status, PageRequest.of(page, size));
        } else {
            pickups = pickupService.getPickupsByStatus(PickupStatus.SCHEDULED, PageRequest.of(page, size));
        }
        return ResponseEntity.ok(ApiResponse.ok(pickups.map(this::toResponse), "مواعيد الاستلام"));
    }

    @Operation(summary = "المواعيد المتأخرة")
    @GetMapping("/admin/overdue")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PickupResponse>>> getOverduePickups() {
        List<PickupSchedule> pickups = pickupService.getOverduePickups();
        List<PickupResponse> responses = pickups.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(responses, "المواعيد المتأخرة"));
    }

    // ── Mapper ──────────────────────────────────────────────

    private PickupResponse toResponse(PickupSchedule p) {
        return new PickupResponse(
                p.getId(),
                p.getMerchant().getId(),
                p.getMerchant().getName(),
                p.getPickupDate(),
                p.getTimeSlot(),
                p.getAddress(),
                p.getLatitude(),
                p.getLongitude(),
                p.getEstimatedShipments(),
                p.getNotes(),
                p.getStatus(),
                p.getAssignedCourier() != null ? p.getAssignedCourier().getId() : null,
                p.getAssignedCourier() != null ? p.getAssignedCourier().getName() : null,
                p.getCompletedAt(),
                p.getCreatedAt()
        );
    }
}
