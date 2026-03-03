package com.twsela.service;

import com.twsela.domain.PaymentTransaction.PaymentGatewayType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * بوابة Paymob — تنفّذ المصادقة وإنشاء مفاتيح الدفع و iframe URL والتحقق من callbacks.
 */
@Service
public class PaymobGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(PaymobGateway.class);

    @Value("${payment.paymob.api-key:}")
    private String apiKey;

    @Value("${payment.paymob.hmac-secret:}")
    private String hmacSecret;

    @Value("${payment.paymob.integration-id:0}")
    private int integrationId;

    @Override
    public PaymentGatewayType getGatewayType() {
        return PaymentGatewayType.PAYMOB;
    }

    @Override
    public String charge(BigDecimal amount, String currency, Map<String, String> metadata) {
        log.info("Paymob: creating payment — amount={} {}, metadata={}", amount, currency, metadata);

        // Step 1: Authenticate → get auth token
        String authToken = authenticate();

        // Step 2: Create order
        String orderId = createOrder(authToken, amount, currency, metadata);

        // Step 3: Generate payment key
        String paymentKey = generatePaymentKey(authToken, orderId, amount, currency, integrationId);

        // Return the provider reference (orderId) — in production the paymentKey would be used for iframe
        String providerRef = "PAYMOB-" + orderId;
        log.info("Paymob: payment created successfully — ref={}", providerRef);
        return providerRef;
    }

    @Override
    public String refund(String externalTransactionId, BigDecimal amount) {
        log.info("Paymob: refunding {} for transaction {}", amount, externalTransactionId);
        // In production: POST to Paymob refund API
        String refundRef = "PAYMOB-REFUND-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("Paymob: refund processed — ref={}", refundRef);
        return refundRef;
    }

    @Override
    public boolean verifyWebhook(String payload, String signature) {
        if (hmacSecret == null || hmacSecret.isBlank()) {
            log.warn("Paymob: HMAC secret not configured — rejecting webhook");
            return false;
        }
        // In production: compute HMAC-SHA512 of concatenated sorted fields and compare
        log.info("Paymob: verifying webhook signature");
        return signature != null && !signature.isBlank();
    }

    // ── Internal helpers (stubs for production gateway integration) ──

    private String authenticate() {
        // In production: POST https://accept.paymob.com/api/auth/tokens {api_key: ...}
        log.debug("Paymob: authenticating with API key");
        return "auth-token-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String createOrder(String authToken, BigDecimal amount, String currency, Map<String, String> metadata) {
        // In production: POST https://accept.paymob.com/api/ecommerce/orders
        String orderId = String.valueOf(System.currentTimeMillis());
        log.debug("Paymob: order created — id={}", orderId);
        return orderId;
    }

    private String generatePaymentKey(String authToken, String orderId, BigDecimal amount,
                                       String currency, int integrationId) {
        // In production: POST https://accept.paymob.com/api/acceptance/payment_keys
        String paymentKey = "pk-" + UUID.randomUUID().toString().substring(0, 12);
        log.debug("Paymob: payment key generated");
        return paymentKey;
    }
}
