package com.twsela.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class MultiCarrierDTO {

    private MultiCarrierDTO() {}

    // === Carrier ===
    public record CreateCarrierRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 30) String code,
            @Size(max = 20) String type,
            @Size(max = 500) String apiEndpoint,
            @Size(max = 500) String apiKey,
            String supportedCountries) {}

    public record CarrierResponse(Long id, String name, String code, String type,
            String apiEndpoint, String status, String supportedCountries, Long tenantId,
            LocalDateTime createdAt) {}

    // === Carrier Zone Mapping ===
    public record CreateCarrierZoneMappingRequest(
            @NotNull Long carrierId, @NotNull Long zoneId,
            @Size(max = 50) String carrierZoneCode, Integer deliveryDays) {}

    public record CarrierZoneMappingResponse(Long id, Long carrierId, Long zoneId,
            String carrierZoneCode, Integer deliveryDays, LocalDateTime createdAt) {}

    // === Carrier Rate ===
    public record CreateCarrierRateRequest(
            @NotNull Long carrierId, Long carrierZoneMappingId,
            @NotNull BigDecimal minWeight, @NotNull BigDecimal maxWeight,
            @NotNull BigDecimal basePrice, @NotNull BigDecimal perKgPrice,
            @Size(max = 3) String currency) {}

    public record CarrierRateResponse(Long id, Long carrierId, Long carrierZoneMappingId,
            BigDecimal minWeight, BigDecimal maxWeight, BigDecimal basePrice, BigDecimal perKgPrice,
            String currency, LocalDateTime createdAt) {}

    // === Carrier Shipment ===
    public record CreateCarrierShipmentRequest(
            @NotNull Long shipmentId, @NotNull Long carrierId,
            @Size(max = 100) String externalTrackingNumber, BigDecimal shippingCost) {}

    public record CarrierShipmentResponse(Long id, Long shipmentId, Long carrierId,
            String externalTrackingNumber, String externalStatus, String labelUrl,
            BigDecimal shippingCost, Long tenantId, LocalDateTime createdAt) {}

    // === Carrier Webhook Log ===
    public record CarrierWebhookLogResponse(Long id, Long carrierId, String eventType,
            String payload, Boolean processed, String error, LocalDateTime createdAt) {}

    // === Third Party Partner ===
    public record CreatePartnerRequest(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 20) String contactPhone,
            String serviceArea, BigDecimal commissionRate) {}

    public record ThirdPartyPartnerResponse(Long id, String name, String contactPhone,
            String serviceArea, BigDecimal commissionRate, String status, Long tenantId,
            LocalDateTime createdAt) {}

    // === Partner Handoff ===
    public record CreatePartnerHandoffRequest(
            @NotNull Long shipmentId, @NotNull Long partnerId,
            @Size(max = 100) String partnerTrackingNumber) {}

    public record PartnerHandoffResponse(Long id, Long shipmentId, Long partnerId,
            LocalDateTime handoffDate, String status, String partnerTrackingNumber,
            Long tenantId, LocalDateTime createdAt) {}

    // === Carrier Selection Rule ===
    public record CreateSelectionRuleRequest(
            @NotNull Integer priority, Long zoneId,
            BigDecimal minWeight, BigDecimal maxWeight,
            Long preferredCarrierId, Long fallbackCarrierId, String criteria) {}

    public record CarrierSelectionRuleResponse(Long id, Integer priority, Long zoneId,
            BigDecimal minWeight, BigDecimal maxWeight, Long preferredCarrierId,
            Long fallbackCarrierId, String criteria, Boolean isActive, Long tenantId,
            LocalDateTime createdAt) {}
}
