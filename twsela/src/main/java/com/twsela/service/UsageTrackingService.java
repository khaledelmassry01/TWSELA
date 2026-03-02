package com.twsela.service;

import com.twsela.domain.UsageTracking;
import com.twsela.repository.UsageTrackingRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Tracks and enforces usage limits per merchant per period (month).
 */
@Service
@Transactional
public class UsageTrackingService {

    private static final Logger log = LoggerFactory.getLogger(UsageTrackingService.class);
    private static final DateTimeFormatter PERIOD_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneId.systemDefault());

    private final UsageTrackingRepository usageTrackingRepository;
    private final SubscriptionService subscriptionService;

    public UsageTrackingService(UsageTrackingRepository usageTrackingRepository,
                                SubscriptionService subscriptionService) {
        this.usageTrackingRepository = usageTrackingRepository;
        this.subscriptionService = subscriptionService;
    }

    /**
     * Get current period string (e.g., "2024-01").
     */
    public String getCurrentPeriod() {
        return PERIOD_FORMAT.format(Instant.now());
    }

    /**
     * Track a shipment creation event.
     */
    public void trackShipmentCreation(Long merchantId) {
        String period = getCurrentPeriod();
        ensureTrackingRecord(merchantId, period);
        usageTrackingRepository.incrementShipments(merchantId, period, Instant.now());
        log.debug("Tracked shipment creation for merchant {} in period {}", merchantId, period);
    }

    /**
     * Track an API call event.
     */
    public void trackApiCall(Long merchantId) {
        String period = getCurrentPeriod();
        ensureTrackingRecord(merchantId, period);
        usageTrackingRepository.incrementApiCalls(merchantId, period, Instant.now());
    }

    /**
     * Track a webhook event.
     */
    public void trackWebhookEvent(Long merchantId) {
        String period = getCurrentPeriod();
        UsageTracking tracking = getOrCreateTracking(merchantId, period);
        tracking.setWebhookEvents(tracking.getWebhookEvents() + 1);
        tracking.setLastUpdated(Instant.now());
        usageTrackingRepository.save(tracking);
    }

    /**
     * Check if a merchant is within their shipment limit for the current period.
     */
    @Transactional(readOnly = true)
    public boolean isWithinShipmentLimit(Long merchantId) {
        String period = getCurrentPeriod();
        int currentUsage = usageTrackingRepository.findByMerchantIdAndPeriod(merchantId, period)
                .map(UsageTracking::getShipmentsCreated)
                .orElse(0);
        return subscriptionService.isWithinUsageLimit(merchantId, currentUsage);
    }

    /**
     * Get usage summary for a merchant in a specific period.
     */
    @Transactional(readOnly = true)
    public UsageTracking getUsageSummary(Long merchantId, String period) {
        return usageTrackingRepository.findByMerchantIdAndPeriod(merchantId, period)
                .orElseThrow(() -> new ResourceNotFoundException("UsageTracking", "merchantId+period",
                        merchantId + "/" + period));
    }

    /**
     * Get usage for current period, returning empty record if none exists.
     */
    @Transactional(readOnly = true)
    public UsageTracking getCurrentUsage(Long merchantId) {
        String period = getCurrentPeriod();
        return usageTrackingRepository.findByMerchantIdAndPeriod(merchantId, period)
                .orElseGet(() -> {
                    UsageTracking empty = new UsageTracking();
                    empty.setMerchantId(merchantId);
                    empty.setPeriod(period);
                    return empty;
                });
    }

    private void ensureTrackingRecord(Long merchantId, String period) {
        if (usageTrackingRepository.findByMerchantIdAndPeriod(merchantId, period).isEmpty()) {
            UsageTracking tracking = new UsageTracking();
            tracking.setMerchantId(merchantId);
            tracking.setPeriod(period);
            tracking.setLastUpdated(Instant.now());
            usageTrackingRepository.save(tracking);
        }
    }

    private UsageTracking getOrCreateTracking(Long merchantId, String period) {
        return usageTrackingRepository.findByMerchantIdAndPeriod(merchantId, period)
                .orElseGet(() -> {
                    UsageTracking tracking = new UsageTracking();
                    tracking.setMerchantId(merchantId);
                    tracking.setPeriod(period);
                    tracking.setLastUpdated(Instant.now());
                    return usageTrackingRepository.save(tracking);
                });
    }
}
