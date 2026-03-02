package com.twsela.web;

import com.twsela.domain.MerchantSubscription;
import com.twsela.domain.SubscriptionPlan;
import com.twsela.domain.UsageTracking;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.SubscriptionService;
import com.twsela.service.UsageTrackingService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.SubscriptionDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for subscription management.
 */
@RestController
@RequestMapping("/api/subscriptions")
@Tag(name = "Subscriptions", description = "إدارة الاشتراكات")
public class SubscriptionController {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionController.class);

    private final SubscriptionService subscriptionService;
    private final UsageTrackingService usageTrackingService;
    private final AuthenticationHelper authHelper;

    public SubscriptionController(SubscriptionService subscriptionService,
                                   UsageTrackingService usageTrackingService,
                                   AuthenticationHelper authHelper) {
        this.subscriptionService = subscriptionService;
        this.usageTrackingService = usageTrackingService;
        this.authHelper = authHelper;
    }

    /**
     * Get all available plans (public).
     */
    @Operation(summary = "خطط الاشتراك", description = "الحصول على جميع خطط الاشتراك المتاحة")
    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getPlans() {
        List<SubscriptionPlan> plans = subscriptionService.getActivePlans();
        List<PlanResponse> response = plans.stream().map(this::toPlanResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(response, "تم جلب الخطط بنجاح"));
    }

    /**
     * Subscribe to a plan.
     */
    @Operation(summary = "اشتراك جديد", description = "الاشتراك في خطة جديدة")
    @PostMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> subscribe(
            @Valid @RequestBody SubscribeRequest request,
            Authentication authentication) {
        Long merchantId = authHelper.getCurrentUserId(authentication);
        MerchantSubscription sub = subscriptionService.subscribe(
                merchantId, request.planName(), request.billingCycle());
        return ResponseEntity.ok(ApiResponse.ok(toSubscriptionResponse(sub), "تم الاشتراك بنجاح"));
    }

    /**
     * Get my subscription.
     */
    @Operation(summary = "اشتراكي", description = "الحصول على اشتراكي الحالي")
    @GetMapping("/my")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getMySubscription(Authentication authentication) {
        Long merchantId = authHelper.getCurrentUserId(authentication);
        MerchantSubscription sub = subscriptionService.getActiveSubscription(merchantId);
        return ResponseEntity.ok(ApiResponse.ok(toSubscriptionResponse(sub), "تم جلب الاشتراك بنجاح"));
    }

    /**
     * Upgrade plan.
     */
    @Operation(summary = "ترقية الخطة", description = "ترقية خطة الاشتراك")
    @PutMapping("/upgrade")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> upgrade(
            @Valid @RequestBody UpgradeRequest request,
            Authentication authentication) {
        Long merchantId = authHelper.getCurrentUserId(authentication);
        MerchantSubscription sub = subscriptionService.upgradePlan(merchantId, request.newPlanName());
        return ResponseEntity.ok(ApiResponse.ok(toSubscriptionResponse(sub), "تمت ترقية الخطة بنجاح"));
    }

    /**
     * Downgrade plan.
     */
    @Operation(summary = "تنزيل الخطة", description = "تنزيل خطة الاشتراك")
    @PutMapping("/downgrade")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> downgrade(
            @Valid @RequestBody UpgradeRequest request,
            Authentication authentication) {
        Long merchantId = authHelper.getCurrentUserId(authentication);
        MerchantSubscription sub = subscriptionService.downgradePlan(merchantId, request.newPlanName());
        return ResponseEntity.ok(ApiResponse.ok(toSubscriptionResponse(sub), "تم تنزيل الخطة بنجاح"));
    }

    /**
     * Cancel subscription.
     */
    @Operation(summary = "إلغاء الاشتراك", description = "إلغاء الاشتراك الحالي")
    @PutMapping("/cancel")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancel(Authentication authentication) {
        Long merchantId = authHelper.getCurrentUserId(authentication);
        MerchantSubscription sub = subscriptionService.cancelSubscription(merchantId);
        return ResponseEntity.ok(ApiResponse.ok(toSubscriptionResponse(sub), "تم إلغاء الاشتراك بنجاح"));
    }

    /**
     * Get current usage.
     */
    @Operation(summary = "استخدامي", description = "الحصول على مقدار الاستخدام الحالي")
    @GetMapping("/usage")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<UsageResponse>> getUsage(Authentication authentication) {
        Long merchantId = authHelper.getCurrentUserId(authentication);
        UsageTracking usage = usageTrackingService.getCurrentUsage(merchantId);

        int limit = 50; // default for free
        try {
            MerchantSubscription sub = subscriptionService.getActiveSubscription(merchantId);
            limit = sub.getPlan().getMaxShipmentsPerMonth();
        } catch (Exception ignored) {
            // Use default
        }

        double percentage = limit > 0 ? (usage.getShipmentsCreated() * 100.0 / limit) : 0;

        UsageResponse response = new UsageResponse(
                merchantId,
                usage.getPeriod(),
                usage.getShipmentsCreated(),
                usage.getApiCalls(),
                usage.getWebhookEvents(),
                limit,
                Math.min(percentage, 100.0)
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "تم جلب الاستخدام بنجاح"));
    }

    // ── Mappers ─────────────────────────────────────────────

    private PlanResponse toPlanResponse(SubscriptionPlan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getMonthlyPrice(),
                plan.getAnnualPrice(),
                plan.getMaxShipmentsPerMonth(),
                plan.getMaxWebhooks(),
                plan.getApiRateLimit(),
                plan.getFeatures(),
                plan.getSortOrder()
        );
    }

    private SubscriptionResponse toSubscriptionResponse(MerchantSubscription sub) {
        return new SubscriptionResponse(
                sub.getId(),
                sub.getMerchant().getId(),
                sub.getMerchant().getName(),
                toPlanResponse(sub.getPlan()),
                sub.getStatus(),
                sub.getBillingCycle(),
                sub.getCurrentPeriodStart(),
                sub.getCurrentPeriodEnd(),
                sub.getTrialEndsAt(),
                sub.isAutoRenew(),
                sub.getCreatedAt()
        );
    }
}
