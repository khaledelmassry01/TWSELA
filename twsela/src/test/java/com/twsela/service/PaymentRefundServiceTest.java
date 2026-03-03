package com.twsela.service;

import com.twsela.domain.PaymentIntent;
import com.twsela.domain.PaymentRefund;
import com.twsela.domain.PaymentTransaction.PaymentGatewayType;
import com.twsela.domain.User;
import com.twsela.repository.PaymentIntentRepository;
import com.twsela.repository.PaymentRefundRepository;
import com.twsela.repository.UserRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentRefundServiceTest {

    @Mock private PaymentRefundRepository paymentRefundRepository;
    @Mock private PaymentIntentRepository paymentIntentRepository;
    @Mock private UserRepository userRepository;
    @Mock private PaymentGatewayFactory paymentGatewayFactory;
    @Mock private PaymentGateway mockGateway;

    @InjectMocks private PaymentRefundService paymentRefundService;

    private PaymentIntent succeededIntent;
    private PaymentRefund pendingRefund;
    private User approver;

    @BeforeEach
    void setUp() {
        succeededIntent = new PaymentIntent();
        succeededIntent.setId(100L);
        succeededIntent.setAmount(new BigDecimal("500.00"));
        succeededIntent.setStatus(PaymentIntent.IntentStatus.SUCCEEDED);
        succeededIntent.setProvider(PaymentGatewayType.STRIPE);
        succeededIntent.setProviderRef("pi_test_123");

        pendingRefund = new PaymentRefund();
        pendingRefund.setId(50L);
        pendingRefund.setPaymentIntent(succeededIntent);
        pendingRefund.setAmount(new BigDecimal("200.00"));
        pendingRefund.setStatus(PaymentRefund.RefundStatus.PENDING);
        pendingRefund.setReason("عميل طلب استرداد");

        approver = new User();
        approver.setId(10L);
        approver.setName("مالك النظام");
    }

    @Test
    @DisplayName("createRefund() creates a refund for succeeded intent")
    void createRefund_success() {
        when(paymentIntentRepository.findById(100L)).thenReturn(Optional.of(succeededIntent));
        when(paymentRefundRepository.findByPaymentIntentId(100L)).thenReturn(List.of());
        when(paymentRefundRepository.save(any(PaymentRefund.class))).thenAnswer(inv -> {
            PaymentRefund r = inv.getArgument(0);
            r.setId(50L);
            return r;
        });

        PaymentRefund result = paymentRefundService.createRefund(100L, new BigDecimal("200.00"), "عميل طلب استرداد");

        assertNotNull(result);
        assertEquals(PaymentRefund.RefundStatus.PENDING, result.getStatus());
        assertEquals(new BigDecimal("200.00"), result.getAmount());
        verify(paymentRefundRepository).save(any(PaymentRefund.class));
    }

    @Test
    @DisplayName("createRefund() throws when intent not succeeded")
    void createRefund_notSucceeded() {
        succeededIntent.setStatus(PaymentIntent.IntentStatus.PENDING);
        when(paymentIntentRepository.findById(100L)).thenReturn(Optional.of(succeededIntent));

        assertThrows(BusinessRuleException.class,
                () -> paymentRefundService.createRefund(100L, new BigDecimal("200.00"), ""));
    }

    @Test
    @DisplayName("createRefund() throws when refund exceeds remaining amount")
    void createRefund_exceededAmount() {
        PaymentRefund existingRefund = new PaymentRefund();
        existingRefund.setAmount(new BigDecimal("400.00"));
        existingRefund.setStatus(PaymentRefund.RefundStatus.APPROVED);

        when(paymentIntentRepository.findById(100L)).thenReturn(Optional.of(succeededIntent));
        when(paymentRefundRepository.findByPaymentIntentId(100L)).thenReturn(List.of(existingRefund));

        assertThrows(BusinessRuleException.class,
                () -> paymentRefundService.createRefund(100L, new BigDecimal("200.00"), ""));
    }

    @Test
    @DisplayName("approveRefund() calls gateway and updates status")
    void approveRefund_success() {
        when(paymentRefundRepository.findById(50L)).thenReturn(Optional.of(pendingRefund));
        when(userRepository.findById(10L)).thenReturn(Optional.of(approver));
        when(paymentGatewayFactory.getGateway(PaymentGatewayType.STRIPE)).thenReturn(mockGateway);
        when(mockGateway.refund("pi_test_123", new BigDecimal("200.00"))).thenReturn("re_test_456");
        when(paymentRefundRepository.save(any(PaymentRefund.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentIntentRepository.save(any(PaymentIntent.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentRefund result = paymentRefundService.approveRefund(50L, 10L);

        assertEquals(PaymentRefund.RefundStatus.PROCESSED, result.getStatus());
        assertEquals("re_test_456", result.getProviderRef());
        assertNotNull(result.getApprovedAt());
        assertNotNull(result.getProcessedAt());
        assertEquals(approver, result.getApprovedBy());
    }

    @Test
    @DisplayName("rejectRefund() marks refund as rejected with reason")
    void rejectRefund_success() {
        when(paymentRefundRepository.findById(50L)).thenReturn(Optional.of(pendingRefund));
        when(paymentRefundRepository.save(any(PaymentRefund.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentRefund result = paymentRefundService.rejectRefund(50L, "لا يمكن الاسترداد بعد التوصيل");

        assertEquals(PaymentRefund.RefundStatus.REJECTED, result.getStatus());
        assertEquals("لا يمكن الاسترداد بعد التوصيل", result.getRejectedReason());
    }
}
