package com.twsela.service;

import com.twsela.domain.PaymentIntent;
import com.twsela.domain.PaymentMethod;
import com.twsela.domain.PaymentTransaction.PaymentGatewayType;
import com.twsela.domain.Shipment;
import com.twsela.repository.PaymentIntentRepository;
import com.twsela.repository.PaymentMethodRepository;
import com.twsela.repository.ShipmentRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentIntentServiceTest {

    @Mock private PaymentIntentRepository paymentIntentRepository;
    @Mock private PaymentMethodRepository paymentMethodRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private PaymentGatewayFactory paymentGatewayFactory;
    @Mock private PaymentGateway mockGateway;

    @InjectMocks private PaymentIntentService paymentIntentService;

    private Shipment shipment;
    private PaymentIntent pendingIntent;

    @BeforeEach
    void setUp() {
        shipment = new Shipment();
        shipment.setId(1L);

        pendingIntent = new PaymentIntent();
        pendingIntent.setId(100L);
        pendingIntent.setShipment(shipment);
        pendingIntent.setAmount(new BigDecimal("250.00"));
        pendingIntent.setCurrency("EGP");
        pendingIntent.setStatus(PaymentIntent.IntentStatus.PENDING);
        pendingIntent.setProvider(PaymentGatewayType.PAYMOB);
        pendingIntent.setAttempts(0);
        pendingIntent.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
    }

    @Test
    @DisplayName("createIntent() creates a new payment intent")
    void createIntent_success() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(paymentIntentRepository.save(any(PaymentIntent.class))).thenAnswer(inv -> {
            PaymentIntent pi = inv.getArgument(0);
            pi.setId(100L);
            return pi;
        });

        PaymentIntent result = paymentIntentService.createIntent(1L, new BigDecimal("250.00"), "EGP", PaymentGatewayType.PAYMOB, null);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(PaymentIntent.IntentStatus.PENDING, result.getStatus());
        verify(paymentIntentRepository).save(any(PaymentIntent.class));
    }

    @Test
    @DisplayName("createIntent() throws when shipment not found")
    void createIntent_shipmentNotFound() {
        when(shipmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> paymentIntentService.createIntent(99L, BigDecimal.TEN, "EGP", PaymentGatewayType.PAYMOB, null));
    }

    @Test
    @DisplayName("confirmIntent() succeeds with gateway charge")
    void confirmIntent_success() {
        when(paymentIntentRepository.findById(100L)).thenReturn(Optional.of(pendingIntent));
        when(paymentGatewayFactory.getGateway(PaymentGatewayType.PAYMOB)).thenReturn(mockGateway);
        when(mockGateway.charge(any(), any(), any())).thenReturn("PAYMOB-REF-123");
        when(paymentIntentRepository.save(any(PaymentIntent.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentIntent result = paymentIntentService.confirmIntent(100L);

        assertEquals(PaymentIntent.IntentStatus.SUCCEEDED, result.getStatus());
        assertEquals("PAYMOB-REF-123", result.getProviderRef());
        assertNotNull(result.getConfirmedAt());
    }

    @Test
    @DisplayName("confirmIntent() marks as failed after max attempts")
    void confirmIntent_failsAfterMaxAttempts() {
        pendingIntent.setAttempts(2); // Already 2 attempts, next will be 3 (max)
        when(paymentIntentRepository.findById(100L)).thenReturn(Optional.of(pendingIntent));
        when(paymentGatewayFactory.getGateway(PaymentGatewayType.PAYMOB)).thenReturn(mockGateway);
        when(mockGateway.charge(any(), any(), any())).thenThrow(new RuntimeException("Gateway timeout"));
        when(paymentIntentRepository.save(any(PaymentIntent.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentIntent result = paymentIntentService.confirmIntent(100L);

        assertEquals(PaymentIntent.IntentStatus.FAILED, result.getStatus());
        assertNotNull(result.getFailureReason());
    }

    @Test
    @DisplayName("confirmIntent() throws for non-pending intent")
    void confirmIntent_nonPending() {
        pendingIntent.setStatus(PaymentIntent.IntentStatus.SUCCEEDED);
        when(paymentIntentRepository.findById(100L)).thenReturn(Optional.of(pendingIntent));

        assertThrows(BusinessRuleException.class, () -> paymentIntentService.confirmIntent(100L));
    }

    @Test
    @DisplayName("confirmIntent() throws for expired intent")
    void confirmIntent_expired() {
        pendingIntent.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        when(paymentIntentRepository.findById(100L)).thenReturn(Optional.of(pendingIntent));
        when(paymentIntentRepository.save(any(PaymentIntent.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(BusinessRuleException.class, () -> paymentIntentService.confirmIntent(100L));
    }

    @Test
    @DisplayName("cancelIntent() cancels a pending intent")
    void cancelIntent_success() {
        when(paymentIntentRepository.findById(100L)).thenReturn(Optional.of(pendingIntent));
        when(paymentIntentRepository.save(any(PaymentIntent.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentIntent result = paymentIntentService.cancelIntent(100L);

        assertEquals(PaymentIntent.IntentStatus.CANCELLED, result.getStatus());
    }

    @Test
    @DisplayName("cancelIntent() throws for non-pending intent")
    void cancelIntent_nonPending() {
        pendingIntent.setStatus(PaymentIntent.IntentStatus.SUCCEEDED);
        when(paymentIntentRepository.findById(100L)).thenReturn(Optional.of(pendingIntent));

        assertThrows(BusinessRuleException.class, () -> paymentIntentService.cancelIntent(100L));
    }
}
