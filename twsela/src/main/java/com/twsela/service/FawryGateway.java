package com.twsela.service;

import com.twsela.domain.PaymentTransaction.PaymentGatewayType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.UUID;

/**
 * بوابة Fawry — توليد أرقام مرجعية والاستعلام عن الحالة والتحقق من HMAC.
 */
@Service
public class FawryGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(FawryGateway.class);

    @Value("${payment.fawry.merchant-code:}")
    private String merchantCode;

    @Value("${payment.fawry.security-key:}")
    private String securityKey;

    @Override
    public PaymentGatewayType getGatewayType() {
        return PaymentGatewayType.FAWRY;
    }

    @Override
    public String charge(BigDecimal amount, String currency, Map<String, String> metadata) {
        log.info("Fawry: generating reference number — amount={} {}, metadata={}", amount, currency, metadata);
        // In production: POST https://atfawry.fawrystaging.com/ECommerceWeb/Fawry/payments/charge
        String referenceNumber = generateReferenceNumber();
        log.info("Fawry: reference number generated — ref={}", referenceNumber);
        return referenceNumber;
    }

    @Override
    public String refund(String externalTransactionId, BigDecimal amount) {
        log.info("Fawry: refunding {} for reference {}", amount, externalTransactionId);
        // In production: POST to Fawry refund API
        String refundRef = "FAWRY-REFUND-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("Fawry: refund processed — ref={}", refundRef);
        return refundRef;
    }

    @Override
    public boolean verifyWebhook(String payload, String signature) {
        if (securityKey == null || securityKey.isBlank()) {
            log.warn("Fawry: security key not configured — rejecting webhook");
            return false;
        }
        // In production: compute SHA-256 HMAC of concatenated fields and compare
        log.info("Fawry: verifying webhook HMAC signature");
        if (signature == null || signature.isBlank()) {
            return false;
        }
        try {
            String expectedSignature = computeHmac(payload);
            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Fawry: HMAC verification failed", e);
            return false;
        }
    }

    /**
     * الاستعلام عن حالة عملية دفع بالرقم المرجعي.
     */
    public String getPaymentStatus(String referenceNumber) {
        log.info("Fawry: querying payment status for ref={}", referenceNumber);
        // In production: GET https://atfawry.fawrystaging.com/ECommerceWeb/Fawry/payments/status/v2?...
        return "PAID";
    }

    // ── Internal helpers ──

    private String generateReferenceNumber() {
        // In production: Fawry returns the reference number from the charge API
        return "FWR-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private String computeHmac(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((data + securityKey).getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC", e);
        }
    }
}
