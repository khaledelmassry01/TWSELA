package com.twsela.service;

import com.twsela.domain.PaymentTransaction.PaymentGatewayType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StripeGatewayTest {

    private final StripeGateway gateway = new StripeGateway();

    @Test
    @DisplayName("getGatewayType() returns STRIPE")
    void getGatewayType() {
        assertEquals(PaymentGatewayType.STRIPE, gateway.getGatewayType());
    }

    @Test
    @DisplayName("charge() returns payment intent ID")
    void charge_success() {
        String ref = gateway.charge(new BigDecimal("99.99"), "USD", Map.of("shipmentId", "10"));
        assertNotNull(ref);
        assertTrue(ref.startsWith("pi_"));
    }

    @Test
    @DisplayName("refund() returns refund ID")
    void refund_success() {
        String ref = gateway.refund("pi_abc123", new BigDecimal("50.00"));
        assertNotNull(ref);
        assertTrue(ref.startsWith("re_"));
    }

    @Test
    @DisplayName("verifyWebhook() rejects null signature")
    void verifyWebhook_nullSignature() {
        assertFalse(gateway.verifyWebhook("{}", null));
    }

    @Test
    @DisplayName("verifyWebhook() rejects signature without v1=")
    void verifyWebhook_invalidFormat() {
        assertFalse(gateway.verifyWebhook("{}", "invalid-format"));
    }

    @Test
    @DisplayName("verifyWebhook() rejects when webhook secret not configured")
    void verifyWebhook_noSecretConfigured() {
        // webhookSecret is blank by default
        assertFalse(gateway.verifyWebhook("{}", "t=123,v1=abc"));
    }
}
