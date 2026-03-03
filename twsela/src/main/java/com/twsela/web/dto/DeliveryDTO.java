package com.twsela.web.dto;

import com.twsela.domain.DeliveryAttempt.FailureReason;
import com.twsela.domain.PickupSchedule.PickupStatus;
import com.twsela.domain.PickupSchedule.TimeSlot;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

/**
 * DTOs for delivery proof, delivery attempts, and pickup scheduling.
 */
public class DeliveryDTO {

    // ── Delivery Proof ──────────────────────────────────────

    public record SubmitProofRequest(
            String recipientName,
            Double latitude,
            Double longitude,
            String notes
    ) {}

    public record ProofResponse(
            Long id,
            Long shipmentId,
            String trackingNumber,
            String photoUrl,
            String signatureUrl,
            String recipientName,
            Double latitude,
            Double longitude,
            String notes,
            Instant deliveredAt,
            Long courierId,
            String courierName
    ) {}

    // ── Delivery Attempts ───────────────────────────────────

    public record RecordAttemptRequest(
            @NotNull(message = "سبب الفشل مطلوب") FailureReason failureReason,
            Double latitude,
            Double longitude,
            String notes
    ) {}

    public record AttemptResponse(
            Long id,
            Long shipmentId,
            int attemptNumber,
            String status,
            String failureReason,
            String photoUrl,
            Double latitude,
            Double longitude,
            String notes,
            Instant attemptedAt,
            LocalDate nextAttemptDate,
            Long courierId,
            String courierName
    ) {}

    public record FailureReportResponse(
            long totalFailedAttempts,
            Map<String, Long> failuresByReason
    ) {}

    // ── Pickup Scheduling ───────────────────────────────────

    public record SchedulePickupRequest(
            @NotNull(message = "تاريخ الاستلام مطلوب") LocalDate pickupDate,
            @NotNull(message = "الفترة الزمنية مطلوبة") TimeSlot timeSlot,
            @NotBlank(message = "العنوان مطلوب") String address,
            Double latitude,
            Double longitude,
            @Min(value = 1, message = "عدد الشحنات المتوقع يجب أن يكون 1 على الأقل") int estimatedShipments,
            String notes
    ) {}

    public record PickupResponse(
            Long id,
            Long merchantId,
            String merchantName,
            LocalDate pickupDate,
            TimeSlot timeSlot,
            String address,
            Double latitude,
            Double longitude,
            int estimatedShipments,
            String notes,
            PickupStatus status,
            Long assignedCourierId,
            String assignedCourierName,
            Instant completedAt,
            Instant createdAt
    ) {}

    private DeliveryDTO() {}
}
