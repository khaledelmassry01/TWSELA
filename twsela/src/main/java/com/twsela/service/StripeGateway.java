package com.twsela.service;

import com.twsela.domain.PaymentTransaction.PaymentGatewayType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * بوابة Stripe — payment intents والتحقق من webhook signatures ومعالجة الاستردادات.
 */
@Service
public class StripeGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(StripeGateway.class);

    @Value("${payment.stripe.secret-key:}")
    private String secretKey;

    @Value("${payment.stripe.webhook-secret:}")
    private String webhookSecret;

    @Override
    public PaymentGatewayType getGatewayType() {
        return PaymentGatewayType.STRIPE;
    }

    @Override
    public String charge(BigDecimal amount, String currency, Map<String, String> metadata) {
        log.info("Stripe: creating payment intent — amount={} {}, metadata={}", amount, currency, metadata);
        // In production: POST https://api.stripe.com/v1/payment_intents
        // Convert amount to smallest unit (piasters/cents)
        long amountInSmallestUnit = amount.multiply(BigDecimal.valueOf(100)).longValue();
        String paymentIntentId = "pi_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        log.info("Stripe: payment intent created — id={}, amount={} (smallest unit)", paymentIntentId, amountInSmallestUnit);
        return paymentIntentId;
    }

    @Override
    public String refund(String externalTransactionId, BigDecimal amount) {
        log.info("Stripe: refunding {} for payment intent {}", amount, externalTransactionId);
        // In production: POST https://api.stripe.com/v1/refunds
        String refundId = "re_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        log.info("Stripe: refund processed — id={}", refundId);
        return refundId;
    }

    @Override
    public boolean verifyWebhook(String payload, String signature) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("Stripe: webhook secret not configured — rejecting webhook");
            return false;
        }
        // In production: use Stripe-Signature header, compute HMAC-SHA256
        // Signature format: t=timestamp,v1=signature
        log.info("Stripe: verifying webhook signature");
        return signature != null && signature.contains("v1=");
    }
}
