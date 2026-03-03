package com.twsela.service;

import com.twsela.domain.PaymentIntent;
import com.twsela.domain.PaymentTransaction.PaymentGatewayType;
import com.twsela.domain.PaymentWebhookLog;
import com.twsela.repository.PaymentIntentRepository;
import com.twsela.repository.PaymentWebhookLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * معالج webhooks بوابات الدفع — يسجل ويتحقق ويحدّث حالة المعاملات.
 */
@Service
@Transactional
public class PaymentWebhookProcessor {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookProcessor.class);

    private final PaymentWebhookLogRepository webhookLogRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    private final PaymentGatewayFactory paymentGatewayFactory;

    public PaymentWebhookProcessor(PaymentWebhookLogRepository webhookLogRepository,
                                    PaymentIntentRepository paymentIntentRepository,
                                    PaymentGatewayFactory paymentGatewayFactory) {
        this.webhookLogRepository = webhookLogRepository;
        this.paymentIntentRepository = paymentIntentRepository;
        this.paymentGatewayFactory = paymentGatewayFactory;
    }

    /**
     * معالجة webhook وارد من بوابة دفع.
     *
     * @param provider اسم البوابة
     * @param eventType نوع الحدث (payment.success, payment.failed, etc.)
     * @param payload البيانات الخام
     * @param signature التوقيع
     * @param providerRef مرجع البوابة (nullable)
     * @return سجل الـ webhook
     */
    public PaymentWebhookLog processWebhook(String provider, String eventType, String payload,
                                             String signature, String providerRef) {
        // 1. Log the webhook
        PaymentWebhookLog webhookLog = new PaymentWebhookLog();
        webhookLog.setProvider(provider);
        webhookLog.setEventType(eventType);
        webhookLog.setPayload(payload);
        webhookLog.setSignature(signature);
        webhookLog = webhookLogRepository.save(webhookLog);

        log.info("Webhook received — provider={}, event={}, logId={}", provider, eventType, webhookLog.getId());

        // 2. Verify signature
        try {
            PaymentGatewayType gatewayType = PaymentGatewayType.valueOf(provider.toUpperCase());
            PaymentGateway gateway = paymentGatewayFactory.getGateway(gatewayType);
            boolean verified = gateway.verifyWebhook(payload, signature);
            webhookLog.setVerified(verified);

            if (!verified) {
                webhookLog.setError("Signature verification failed");
                webhookLog.setProcessed(true);
                webhookLog.setProcessedAt(Instant.now());
                webhookLogRepository.save(webhookLog);
                log.warn("Webhook {} signature verification failed — provider={}", webhookLog.getId(), provider);
                return webhookLog;
            }
        } catch (IllegalArgumentException e) {
            webhookLog.setError("Unknown provider: " + provider);
            webhookLog.setProcessed(true);
            webhookLog.setProcessedAt(Instant.now());
            webhookLogRepository.save(webhookLog);
            log.error("Webhook {} from unknown provider: {}", webhookLog.getId(), provider);
            return webhookLog;
        }

        // 3. Update payment intent if providerRef is provided
        if (providerRef != null && !providerRef.isBlank()) {
            try {
                paymentIntentRepository.findByProviderRef(providerRef).ifPresent(intent -> {
                    updateIntentFromWebhook(intent, eventType);
                    paymentIntentRepository.save(intent);
                    log.info("Payment intent {} updated from webhook — event={}, newStatus={}",
                            intent.getId(), eventType, intent.getStatus());
                });
            } catch (Exception e) {
                webhookLog.setError("Failed to update intent: " + e.getMessage());
                log.error("Webhook {} failed to update intent for ref={}: {}", webhookLog.getId(), providerRef, e.getMessage());
            }
        }

        // 4. Mark as processed
        webhookLog.setProcessed(true);
        webhookLog.setProcessedAt(Instant.now());
        return webhookLogRepository.save(webhookLog);
    }

    /**
     * إعادة معالجة webhooks غير المعالجة.
     */
    public int retryUnprocessed() {
        List<PaymentWebhookLog> unprocessed = webhookLogRepository.findByProcessedFalseOrderByCreatedAtAsc();
        int count = 0;
        for (PaymentWebhookLog wh : unprocessed) {
            try {
                processWebhook(wh.getProvider(), wh.getEventType(), wh.getPayload(), wh.getSignature(), null);
                count++;
            } catch (Exception e) {
                log.error("Retry failed for webhook {}: {}", wh.getId(), e.getMessage());
            }
        }
        log.info("Retried {} unprocessed webhooks", count);
        return count;
    }

    @Transactional(readOnly = true)
    public List<PaymentWebhookLog> getByProvider(String provider) {
        return webhookLogRepository.findByProviderOrderByCreatedAtDesc(provider);
    }

    // ── Internal ──

    private void updateIntentFromWebhook(PaymentIntent intent, String eventType) {
        String normalizedEvent = eventType.toLowerCase();
        if (normalizedEvent.contains("success") || normalizedEvent.contains("captured") || normalizedEvent.contains("paid")) {
            intent.setStatus(PaymentIntent.IntentStatus.SUCCEEDED);
            intent.setConfirmedAt(Instant.now());
        } else if (normalizedEvent.contains("fail") || normalizedEvent.contains("declined")) {
            intent.setStatus(PaymentIntent.IntentStatus.FAILED);
            intent.setFailedAt(Instant.now());
            intent.setFailureReason("Failed via webhook: " + eventType);
        } else if (normalizedEvent.contains("refund")) {
            intent.setStatus(PaymentIntent.IntentStatus.REFUNDED);
        }
        intent.setUpdatedAt(Instant.now());
    }
}
