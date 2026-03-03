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
 * بوابة Tap Payments — إنشاء charges وإدارة sources والتحقق من webhooks.
 */
@Service
public class TapGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(TapGateway.class);

    @Value("${payment.tap.secret-key:}")
    private String secretKey;

    @Override
    public PaymentGatewayType getGatewayType() {
        return PaymentGatewayType.TAP;
    }

    @Override
    public String charge(BigDecimal amount, String currency, Map<String, String> metadata) {
        log.info("Tap: creating charge — amount={} {}, metadata={}", amount, currency, metadata);
        // In production: POST https://api.tap.company/v2/charges
        String chargeId = "chg_" + UUID.randomUUID().toString().substring(0, 12);
        log.info("Tap: charge created — id={}", chargeId);
        return chargeId;
    }

    @Override
    public String refund(String externalTransactionId, BigDecimal amount) {
        log.info("Tap: refunding {} for charge {}", amount, externalTransactionId);
        // In production: POST https://api.tap.company/v2/refunds
        String refundId = "re_" + UUID.randomUUID().toString().substring(0, 12);
        log.info("Tap: refund processed — id={}", refundId);
        return refundId;
    }

    @Override
    public boolean verifyWebhook(String payload, String signature) {
        if (secretKey == null || secretKey.isBlank()) {
            log.warn("Tap: secret key not configured — rejecting webhook");
            return false;
        }
        // In production: compute HMAC-SHA256 and compare
        log.info("Tap: verifying webhook signature");
        return signature != null && !signature.isBlank();
    }
}
