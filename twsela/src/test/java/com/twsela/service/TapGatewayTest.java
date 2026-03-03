package com.twsela.service;

import com.twsela.domain.PaymentTransaction.PaymentGatewayType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TapGatewayTest {

    private final TapGateway gateway = new TapGateway();

    @Test
    @DisplayName("getGatewayType() returns TAP")
    void getGatewayType() {
        assertEquals(PaymentGatewayType.TAP, gateway.getGatewayType());
    }

    @Test
    @DisplayName("charge() returns charge ID")
    void charge_success() {
        String ref = gateway.charge(new BigDecimal("200.00"), "SAR", Map.of("shipmentId", "5"));
        assertNotNull(ref);
        assertTrue(ref.startsWith("chg_"));
    }

    @Test
    @DisplayName("refund() returns refund ID")
    void refund_success() {
        String ref = gateway.refund("chg_abc123", new BigDecimal("100.00"));
        assertNotNull(ref);
        assertTrue(ref.startsWith("re_"));
    }

    @Test
    @DisplayName("verifyWebhook() rejects null signature")
    void verifyWebhook_nullSignature() {
        assertFalse(gateway.verifyWebhook("{}", null));
    }

    @Test
    @DisplayName("verifyWebhook() rejects when secret not configured")
    void verifyWebhook_noSecretConfigured() {
        assertFalse(gateway.verifyWebhook("{}", "valid-sig"));
    }
}
