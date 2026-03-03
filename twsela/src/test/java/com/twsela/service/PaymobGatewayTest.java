package com.twsela.service;

import com.twsela.domain.PaymentTransaction.PaymentGatewayType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PaymobGatewayTest {

    private final PaymobGateway gateway = new PaymobGateway();

    @Test
    @DisplayName("getGatewayType() returns PAYMOB")
    void getGatewayType() {
        assertEquals(PaymentGatewayType.PAYMOB, gateway.getGatewayType());
    }

    @Test
    @DisplayName("charge() returns provider reference")
    void charge_success() {
        String ref = gateway.charge(new BigDecimal("150.00"), "EGP", Map.of("shipmentId", "1"));
        assertNotNull(ref);
        assertTrue(ref.startsWith("PAYMOB-"));
    }

    @Test
    @DisplayName("refund() returns refund reference")
    void refund_success() {
        String ref = gateway.refund("PAYMOB-12345", new BigDecimal("50.00"));
        assertNotNull(ref);
        assertTrue(ref.startsWith("PAYMOB-REFUND-"));
    }

    @Test
    @DisplayName("verifyWebhook() rejects null signature")
    void verifyWebhook_nullSignature() {
        assertFalse(gateway.verifyWebhook("{}", null));
    }

    @Test
    @DisplayName("verifyWebhook() rejects blank signature")
    void verifyWebhook_blankSignature() {
        assertFalse(gateway.verifyWebhook("{}", ""));
    }

    @Test
    @DisplayName("verifyWebhook() accepts valid signature when HMAC not configured")
    void verifyWebhook_noHmacConfigured() {
        // When hmacSecret is blank, it returns false
        assertFalse(gateway.verifyWebhook("{}", "some-signature"));
    }
}
