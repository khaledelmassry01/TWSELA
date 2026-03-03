package com.twsela.web.dto;

import com.twsela.domain.Contract.ContractStatus;
import com.twsela.domain.Contract.ContractType;
import com.twsela.domain.ContractSlaTerms.SlaReviewPeriod;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * DTOs for Contract Management endpoints.
 */
public class ContractDTO {

    public record CreateContractRequest(
            @NotNull ContractType contractType,
            @NotNull Long partyId,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate,
            boolean autoRenew,
            int renewalNoticeDays,
            String termsDocument,
            String notes
    ) {}

    public record UpdateContractRequest(
            LocalDate startDate,
            LocalDate endDate,
            boolean autoRenew,
            int renewalNoticeDays,
            String termsDocument,
            String notes
    ) {}

    public record ContractResponse(
            Long id,
            String contractNumber,
            ContractType contractType,
            Long partyId,
            String partyName,
            LocalDate startDate,
            LocalDate endDate,
            ContractStatus status,
            boolean autoRenew,
            Instant signedAt,
            int pricingRulesCount,
            Instant createdAt
    ) {}

    public record SignContractRequest(
            @NotNull String otp
    ) {}

    public record TerminateContractRequest(
            @NotNull String reason
    ) {}

    public record CreatePricingRuleRequest(
            Long zoneFromId,
            Long zoneToId,
            String shipmentType,
            @NotNull BigDecimal basePrice,
            BigDecimal perKgPrice,
            BigDecimal codFeePercent,
            BigDecimal minimumCharge,
            BigDecimal discountPercent,
            int minMonthlyShipments
    ) {}

    public record PricingRuleResponse(
            Long id,
            Long contractId,
            Long zoneFromId,
            String zoneFromName,
            Long zoneToId,
            String zoneToName,
            String shipmentType,
            BigDecimal basePrice,
            BigDecimal perKgPrice,
            BigDecimal codFeePercent,
            BigDecimal minimumCharge,
            BigDecimal discountPercent,
            int minMonthlyShipments,
            boolean active
    ) {}

    public record PricingCalculationRequest(
            @NotNull Long merchantId,
            Long zoneFromId,
            Long zoneToId,
            double weightKg,
            BigDecimal codAmount
    ) {}

    public record SlaTermsRequest(
            double targetDeliveryRate,
            int maxDeliveryHours,
            BigDecimal latePenaltyPerShipment,
            BigDecimal lostPenaltyFixed,
            SlaReviewPeriod slaReviewPeriod
    ) {}

    public record SlaTermsResponse(
            Long id,
            Long contractId,
            double targetDeliveryRate,
            int maxDeliveryHours,
            BigDecimal latePenaltyPerShipment,
            BigDecimal lostPenaltyFixed,
            SlaReviewPeriod slaReviewPeriod
    ) {}
}
