package com.twsela.web.dto;

import com.twsela.domain.Invoice.InvoiceStatus;
import com.twsela.domain.MerchantSubscription.BillingCycle;
import com.twsela.domain.MerchantSubscription.SubscriptionStatus;
import com.twsela.domain.SubscriptionPlan.PlanName;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTOs for the subscription and billing module.
 */
public class SubscriptionDTO {

    // ── Plan DTOs ──────────────────────────────────────────────

    public record PlanResponse(
            Long id,
            PlanName name,
            BigDecimal monthlyPrice,
            BigDecimal annualPrice,
            int maxShipmentsPerMonth,
            int maxWebhooks,
            int apiRateLimit,
            String features,
            int sortOrder
    ) {}

    // ── Subscription DTOs ─────────────────────────────────────

    public record SubscribeRequest(
            @NotNull(message = "الخطة مطلوبة")
            PlanName planName,
            @NotNull(message = "دورة الفوترة مطلوبة")
            BillingCycle billingCycle
    ) {}

    public record UpgradeRequest(
            @NotNull(message = "الخطة الجديدة مطلوبة")
            PlanName newPlanName
    ) {}

    public record SubscriptionResponse(
            Long id,
            Long merchantId,
            String merchantName,
            PlanResponse plan,
            SubscriptionStatus status,
            BillingCycle billingCycle,
            Instant currentPeriodStart,
            Instant currentPeriodEnd,
            Instant trialEndsAt,
            boolean autoRenew,
            Instant createdAt
    ) {}

    // ── Usage DTOs ────────────────────────────────────────────

    public record UsageResponse(
            Long merchantId,
            String period,
            int shipmentsCreated,
            int apiCalls,
            int webhookEvents,
            int shipmentLimit,
            double usagePercentage
    ) {}

    // ── Invoice DTOs ──────────────────────────────────────────

    public record InvoiceResponse(
            Long id,
            String invoiceNumber,
            Long subscriptionId,
            BigDecimal amount,
            BigDecimal tax,
            BigDecimal totalAmount,
            InvoiceStatus status,
            Instant dueDate,
            Instant paidAt,
            String paymentGateway,
            List<InvoiceItemResponse> items,
            Instant createdAt
    ) {}

    public record InvoiceItemResponse(
            Long id,
            String description,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice
    ) {}

    // ── Payment DTOs ──────────────────────────────────────────

    public record PaymentRequest(
            @NotNull(message = "معرف الفاتورة مطلوب")
            Long invoiceId,
            @NotNull(message = "بوابة الدفع مطلوبة")
            String gateway,
            String currency
    ) {}

    private SubscriptionDTO() {
        // Utility class
    }
}
