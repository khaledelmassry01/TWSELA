package com.twsela.web;

import com.twsela.service.RecipientProfileService;
import com.twsela.service.DeliverySchedulingService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.RecipientExperienceDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Recipient", description = "إدارة ملفات المستلمين")
public class RecipientController {

    private final RecipientProfileService profileService;
    private final DeliverySchedulingService schedulingService;

    public RecipientController(RecipientProfileService profileService,
                                DeliverySchedulingService schedulingService) {
        this.profileService = profileService;
        this.schedulingService = schedulingService;
    }

    // ── Profiles ──
    @GetMapping("/api/recipients/{id}")
    @Operation(summary = "جلب ملف المستلم")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MERCHANT','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.getById(id), "تم جلب ملف المستلم"));
    }

    @GetMapping("/api/recipients/phone/{phone}")
    @Operation(summary = "جلب ملف المستلم بالهاتف")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MERCHANT','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> getByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.getByPhone(phone), "تم جلب ملف المستلم"));
    }

    @PostMapping("/api/recipients")
    @Operation(summary = "إنشاء ملف مستلم")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MERCHANT')")
    public ResponseEntity<ApiResponse<?>> createProfile(@Valid @RequestBody CreateRecipientProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.create(req), "تم إنشاء ملف المستلم"));
    }

    @PutMapping("/api/recipients/{id}")
    @Operation(summary = "تحديث ملف المستلم")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MERCHANT')")
    public ResponseEntity<ApiResponse<?>> updateProfile(@PathVariable Long id,
                                                         @Valid @RequestBody CreateRecipientProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.update(id, req), "تم تحديث ملف المستلم"));
    }

    // ── Addresses ──
    @GetMapping("/api/recipients/{profileId}/addresses")
    @Operation(summary = "جلب عناوين المستلم")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MERCHANT','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> getAddresses(@PathVariable Long profileId) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.getAddresses(profileId), "تم جلب العناوين"));
    }

    @PostMapping("/api/recipients/addresses")
    @Operation(summary = "إضافة عنوان للمستلم")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MERCHANT')")
    public ResponseEntity<ApiResponse<?>> createAddress(@Valid @RequestBody CreateRecipientAddressRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.createAddress(req), "تم إضافة العنوان"));
    }

    // ── Preferences ──
    @GetMapping("/api/recipients/{profileId}/preferences")
    @Operation(summary = "جلب تفضيلات التوصيل")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MERCHANT','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> getPreferences(@PathVariable Long profileId) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.getPreferences(profileId), "تم جلب التفضيلات"));
    }

    @PostMapping("/api/recipients/preferences")
    @Operation(summary = "حفظ تفضيلات التوصيل")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MERCHANT')")
    public ResponseEntity<ApiResponse<?>> savePreferences(@Valid @RequestBody CreateDeliveryPreferenceRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.savePreferences(req), "تم حفظ التفضيلات"));
    }

    // ── Time Slots ──
    @GetMapping("/api/delivery/slots/zone/{zoneId}")
    @Operation(summary = "جلب فترات التوصيل المتاحة")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MERCHANT','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> getSlots(@PathVariable Long zoneId) {
        return ResponseEntity.ok(ApiResponse.ok(schedulingService.getSlotsByZone(zoneId), "تم جلب الفترات"));
    }

    @PostMapping("/api/delivery/slots")
    @Operation(summary = "إنشاء فترة توصيل")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<?>> createSlot(@Valid @RequestBody CreateDeliveryTimeSlotRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(schedulingService.createSlot(req), "تم إنشاء الفترة"));
    }

    // ── Bookings ──
    @GetMapping("/api/delivery/bookings/shipment/{shipmentId}")
    @Operation(summary = "جلب حجوزات الشحنة")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MERCHANT','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> getBookings(@PathVariable Long shipmentId) {
        return ResponseEntity.ok(ApiResponse.ok(schedulingService.getBookingsByShipment(shipmentId), "تم جلب الحجوزات"));
    }

    @PostMapping("/api/delivery/bookings")
    @Operation(summary = "إنشاء حجز توصيل")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MERCHANT')")
    public ResponseEntity<ApiResponse<?>> createBooking(@Valid @RequestBody CreateDeliveryBookingRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(schedulingService.createBooking(req), "تم إنشاء الحجز"));
    }
}
