package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.domain.MerchantSubscription.BillingCycle;
import com.twsela.domain.MerchantSubscription.SubscriptionStatus;
import com.twsela.domain.SubscriptionPlan.PlanName;
import com.twsela.repository.MerchantSubscriptionRepository;
import com.twsela.repository.SubscriptionPlanRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.DuplicateResourceException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Manages merchant subscription lifecycle:
 * TRIAL → ACTIVE → PAST_DUE → EXPIRED → CANCELLED
 */
@Service
@Transactional
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);
    private static final int TRIAL_DAYS = 14;
    private static final List<SubscriptionStatus> ACTIVE_STATUSES =
            List.of(SubscriptionStatus.TRIAL, SubscriptionStatus.ACTIVE);

    private final MerchantSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final UserRepository userRepository;

    public SubscriptionService(MerchantSubscriptionRepository subscriptionRepository,
                               SubscriptionPlanRepository planRepository,
                               UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get all active plans.
     */
    @Transactional(readOnly = true)
    public List<SubscriptionPlan> getActivePlans() {
        return planRepository.findByActiveTrueOrderBySortOrderAsc();
    }

    /**
     * Subscribe a merchant to a plan. Starts with TRIAL for 14 days.
     */
    public MerchantSubscription subscribe(Long merchantId, PlanName planName, BillingCycle cycle) {
        // Check no active subscription exists
        boolean hasActive = subscriptionRepository.existsByMerchantIdAndStatusIn(merchantId, ACTIVE_STATUSES);
        if (hasActive) {
            throw new DuplicateResourceException("التاجر لديه اشتراك نشط بالفعل");
        }

        User merchant = userRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", merchantId));

        SubscriptionPlan plan = planRepository.findByName(planName)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "name", planName));

        Instant now = Instant.now();
        MerchantSubscription subscription = new MerchantSubscription(merchant, plan);
        subscription.setBillingCycle(cycle);
        subscription.setStatus(SubscriptionStatus.TRIAL);
        subscription.setCurrentPeriodStart(now);
        subscription.setTrialEndsAt(now.plus(TRIAL_DAYS, ChronoUnit.DAYS));
        subscription.setCurrentPeriodEnd(now.plus(TRIAL_DAYS, ChronoUnit.DAYS));

        log.info("Merchant {} subscribed to plan {} (trial)", merchantId, planName);
        return subscriptionRepository.save(subscription);
    }

    /**
     * Get active subscription for a merchant.
     */
    @Transactional(readOnly = true)
    public MerchantSubscription getActiveSubscription(Long merchantId) {
        return subscriptionRepository.findByMerchantIdAndStatusIn(merchantId, ACTIVE_STATUSES)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantSubscription", "merchantId", merchantId));
    }

    /**
     * Get subscription by merchant (any status).
     */
    @Transactional(readOnly = true)
    public MerchantSubscription getSubscriptionByMerchant(Long merchantId) {
        return subscriptionRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantSubscription", "merchantId", merchantId));
    }

    /**
     * Activate a subscription after successful payment.
     */
    public MerchantSubscription activate(Long subscriptionId) {
        MerchantSubscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantSubscription", "id", subscriptionId));

        Instant now = Instant.now();
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setCurrentPeriodStart(now);
        sub.setCurrentPeriodEnd(calculatePeriodEnd(now, sub.getBillingCycle()));
        sub.setUpdatedAt(now);

        log.info("Subscription {} activated for merchant {}", subscriptionId, sub.getMerchant().getId());
        return subscriptionRepository.save(sub);
    }

    /**
     * Upgrade a merchant's subscription to a higher plan.
     */
    public MerchantSubscription upgradePlan(Long merchantId, PlanName newPlanName) {
        MerchantSubscription sub = getActiveSubscription(merchantId);
        SubscriptionPlan newPlan = planRepository.findByName(newPlanName)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "name", newPlanName));

        if (newPlan.getSortOrder() <= sub.getPlan().getSortOrder()) {
            throw new BusinessRuleException("لا يمكن الترقية إلى خطة أقل أو مساوية");
        }

        sub.setPlan(newPlan);
        sub.setUpdatedAt(Instant.now());
        log.info("Merchant {} upgraded to plan {}", merchantId, newPlanName);
        return subscriptionRepository.save(sub);
    }

    /**
     * Downgrade a merchant's subscription to a lower plan (effective next period).
     */
    public MerchantSubscription downgradePlan(Long merchantId, PlanName newPlanName) {
        MerchantSubscription sub = getActiveSubscription(merchantId);
        SubscriptionPlan newPlan = planRepository.findByName(newPlanName)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "name", newPlanName));

        if (newPlan.getSortOrder() >= sub.getPlan().getSortOrder()) {
            throw new BusinessRuleException("لا يمكن التنزيل إلى خطة أعلى أو مساوية");
        }

        sub.setPlan(newPlan);
        sub.setUpdatedAt(Instant.now());
        log.info("Merchant {} downgraded to plan {}", merchantId, newPlanName);
        return subscriptionRepository.save(sub);
    }

    /**
     * Cancel a merchant's subscription.
     */
    public MerchantSubscription cancelSubscription(Long merchantId) {
        MerchantSubscription sub = getActiveSubscription(merchantId);
        sub.setStatus(SubscriptionStatus.CANCELLED);
        sub.setCancelledAt(Instant.now());
        sub.setAutoRenew(false);
        sub.setUpdatedAt(Instant.now());
        log.info("Merchant {} cancelled subscription", merchantId);
        return subscriptionRepository.save(sub);
    }

    /**
     * Renew subscription for the next period.
     */
    public MerchantSubscription renew(Long subscriptionId) {
        MerchantSubscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantSubscription", "id", subscriptionId));

        if (sub.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new BusinessRuleException("لا يمكن تجديد اشتراك ملغي");
        }

        Instant now = Instant.now();
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setCurrentPeriodStart(now);
        sub.setCurrentPeriodEnd(calculatePeriodEnd(now, sub.getBillingCycle()));
        sub.setUpdatedAt(now);

        log.info("Subscription {} renewed", subscriptionId);
        return subscriptionRepository.save(sub);
    }

    /**
     * Check if a merchant is within their usage limit.
     */
    @Transactional(readOnly = true)
    public boolean isWithinUsageLimit(Long merchantId, int currentUsage) {
        try {
            MerchantSubscription sub = getActiveSubscription(merchantId);
            int limit = sub.getPlan().getMaxShipmentsPerMonth();
            return limit <= 0 || currentUsage < limit; // 0 or negative = unlimited
        } catch (ResourceNotFoundException e) {
            // No active subscription — use FREE plan default
            return currentUsage < 50;
        }
    }

    /**
     * Process expired subscriptions — runs daily at 2:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void processExpiredSubscriptions() {
        Instant now = Instant.now();

        // 1. Expire ACTIVE subscriptions past their period end
        List<MerchantSubscription> expired = subscriptionRepository.findExpired(
                now, List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAST_DUE));
        for (MerchantSubscription sub : expired) {
            if (sub.isAutoRenew()) {
                sub.setStatus(SubscriptionStatus.PAST_DUE);
                log.info("Subscription {} marked PAST_DUE (auto-renew pending)", sub.getId());
            } else {
                sub.setStatus(SubscriptionStatus.EXPIRED);
                log.info("Subscription {} expired (no auto-renew)", sub.getId());
            }
            sub.setUpdatedAt(now);
            subscriptionRepository.save(sub);
        }

        // 2. Expire trials
        List<MerchantSubscription> expiredTrials = subscriptionRepository.findExpiredTrials(now);
        for (MerchantSubscription sub : expiredTrials) {
            sub.setStatus(SubscriptionStatus.EXPIRED);
            sub.setUpdatedAt(now);
            subscriptionRepository.save(sub);
            log.info("Trial subscription {} expired for merchant {}", sub.getId(), sub.getMerchant().getId());
        }
    }

    private Instant calculatePeriodEnd(Instant start, BillingCycle cycle) {
        return switch (cycle) {
            case MONTHLY -> start.plus(30, ChronoUnit.DAYS);
            case ANNUAL -> start.plus(365, ChronoUnit.DAYS);
        };
    }
}
