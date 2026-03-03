package com.twsela.service;

import com.twsela.domain.PaymentTransaction.PaymentGatewayType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FawryGatewayTest {

    private final FawryGateway gateway = new FawryGateway();

    @Test
    @DisplayName("getGatewayType() returns FAWRY")
    void getGatewayType() {
        assertEquals(PaymentGatewayType.FAWRY, gateway.getGatewayType());
    }

    @Test
    @DisplayName("charge() returns reference number")
    void charge_success() {
        String ref = gateway.charge(new BigDecimal("300.00"), "EGP", Map.of("shipmentId", "20"));
        assertNotNull(ref);
        assertTrue(ref.startsWith("FWR-"));
    }

    @Test
    @DisplayName("refund() returns refund reference")
    void refund_success() {
        String ref = gateway.refund("FWR-12345", new BigDecimal("100.00"));
        assertNotNull(ref);
        assertTrue(ref.startsWith("FAWRY-REFUND-"));
    }

    @Test
    @DisplayName("verifyWebhook() rejects null signature")
    void verifyWebhook_nullSignature() {
        assertFalse(gateway.verifyWebhook("{}", null));
    }

    @Test
    @DisplayName("getPaymentStatus() returns status")
    void getPaymentStatus_success() {
        String status = gateway.getPaymentStatus("FWR-12345");
        assertNotNull(status);
        assertEquals("PAID", status);
    }
}
