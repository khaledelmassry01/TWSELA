package com.twsela.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public final class RecipientExperienceDTO {
    private RecipientExperienceDTO() {}

    // ── Recipient Profile ──
    public record CreateRecipientProfileRequest(
            @NotBlank String phone,
            String name,
            String email,
            String preferredLanguage,
            String preferredTimeSlot,
            String deliveryInstructions) {}

    public record RecipientProfileResponse(
            Long id, String phone, String name, String email,
            String preferredLanguage, Long defaultAddressId,
            String preferredTimeSlot, String deliveryInstructions,
            Integer totalDeliveries, Instant createdAt) {}

    // ── Recipient Address ──
    public record CreateRecipientAddressRequest(
            @NotNull Long recipientProfileId,
            String label,
            @NotBlank String addressLine1,
            String addressLine2,
            String city, String district, String postalCode,
            Double latitude, Double longitude,
            Boolean isDefault, String notes) {}

    public record RecipientAddressResponse(
            Long id, Long recipientProfileId, String label,
            String addressLine1, String addressLine2,
            String city, String district, String postalCode,
            Double latitude, Double longitude,
            Boolean isDefault, String notes, Instant createdAt) {}

    // ── Delivery Preference ──
    public record CreateDeliveryPreferenceRequest(
            @NotNull Long recipientProfileId,
            Boolean preferSafePlace, String safePlaceDescription,
            Boolean allowNeighborDelivery, Boolean requireSignature,
            Boolean requireOtp, Boolean preferContactless,
            Boolean smsBeforeDelivery, Integer smsMinutesBefore) {}

    public record DeliveryPreferenceResponse(
            Long id, Long recipientProfileId,
            Boolean preferSafePlace, String safePlaceDescription,
            Boolean allowNeighborDelivery, Boolean requireSignature,
            Boolean requireOtp, Boolean preferContactless,
            Boolean smsBeforeDelivery, Integer smsMinutesBefore, Instant updatedAt) {}

    // ── Delivery Time Slot ──
    public record CreateDeliveryTimeSlotRequest(
            Long zoneId,
            @NotNull Integer dayOfWeek,
            @NotNull LocalTime startTime,
            @NotNull LocalTime endTime,
            Integer maxCapacity,
            BigDecimal surchargeAmount,
            String displayNameAr) {}

    public record DeliveryTimeSlotResponse(
            Long id, Long zoneId, Integer dayOfWeek,
            LocalTime startTime, LocalTime endTime,
            Integer maxCapacity, Integer currentBookings,
            Boolean isActive, BigDecimal surchargeAmount,
            String displayNameAr, Instant createdAt) {}

    // ── Delivery Booking ──
    public record CreateDeliveryBookingRequest(
            Long shipmentId,
            @NotNull Long deliveryTimeSlotId,
            Long recipientProfileId,
            @NotNull LocalDate selectedDate) {}

    public record DeliveryBookingResponse(
            Long id, Long shipmentId, Long deliveryTimeSlotId,
            Long recipientProfileId, LocalDate selectedDate,
            String status, Long rescheduledFromId,
            String rescheduledReason, Instant bookedAt, Instant createdAt) {}

    // ── Delivery Redirect ──
    public record CreateDeliveryRedirectRequest(
            @NotNull Long shipmentId,
            Long recipientProfileId,
            @NotBlank String redirectType,
            Long newAddressId,
            LocalDate holdUntilDate,
            String neighborName, String neighborPhone,
            String reason) {}

    public record DeliveryRedirectResponse(
            Long id, Long shipmentId, Long recipientProfileId,
            String redirectType, Long newAddressId,
            LocalDate holdUntilDate, String neighborName,
            String neighborPhone, String status, String reason,
            Instant requestedAt, Instant processedAt, Long processedById,
            Instant createdAt) {}

    // ── Satisfaction Survey ──
    public record CreateSatisfactionSurveyRequest(
            Long shipmentId,
            Long recipientProfileId,
            @NotNull @Min(1) @Max(5) Integer overallRating,
            Integer deliverySpeedRating,
            Integer courierBehaviorRating,
            Integer packagingRating,
            String comment,
            Boolean wouldRecommend,
            String feedbackTags) {}

    public record SatisfactionSurveyResponse(
            Long id, Long shipmentId, Long recipientProfileId,
            Integer overallRating, Integer deliverySpeedRating,
            Integer courierBehaviorRating, Integer packagingRating,
            String comment, Boolean wouldRecommend,
            String feedbackTags, Instant submittedAt) {}
}
