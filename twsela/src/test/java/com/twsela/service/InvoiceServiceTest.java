package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.domain.Invoice.InvoiceStatus;
import com.twsela.domain.MerchantSubscription.BillingCycle;
import com.twsela.domain.MerchantSubscription.SubscriptionStatus;
import com.twsela.domain.SubscriptionPlan.PlanName;
import com.twsela.repository.InvoiceRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private SubscriptionService subscriptionService;

    @InjectMocks
    private InvoiceService invoiceService;

    private User merchant;
    private SubscriptionPlan basicPlan;
    private MerchantSubscription subscription;
    private Invoice pendingInvoice;

    @BeforeEach
    void setUp() {
        merchant = new User();
        merchant.setId(1L);
        merchant.setName("Test Merchant");

        basicPlan = new SubscriptionPlan();
        basicPlan.setId(2L);
        basicPlan.setName(PlanName.BASIC);
        basicPlan.setMonthlyPrice(new BigDecimal("199.00"));
        basicPlan.setAnnualPrice(new BigDecimal("1990.00"));

        subscription = new MerchantSubscription(merchant, basicPlan);
        subscription.setId(100L);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setBillingCycle(BillingCycle.MONTHLY);

        pendingInvoice = new Invoice();
        pendingInvoice.setId(50L);
        pendingInvoice.setInvoiceNumber("TWS-INV-12345678");
        pendingInvoice.setSubscription(subscription);
        pendingInvoice.setAmount(new BigDecimal("199.00"));
        pendingInvoice.setTax(new BigDecimal("27.86"));
        pendingInvoice.setTotalAmount(new BigDecimal("226.86"));
        pendingInvoice.setStatus(InvoiceStatus.PENDING);
        pendingInvoice.setDueDate(Instant.now().plus(7, ChronoUnit.DAYS));
    }

    @Test
    @DisplayName("generateInvoiceForSubscription - إنشاء فاتورة جديدة")
    void generateInvoice_shouldCreateInvoice() {
        when(invoiceRepository.existsBySubscriptionIdAndStatus(100L, InvoiceStatus.PENDING)).thenReturn(false);
        when(invoiceRepository.save(any())).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(60L);
            return i;
        });

        Invoice result = invoiceService.generateInvoiceForSubscription(subscription);

        assertThat(result.getInvoiceNumber()).startsWith("TWS-INV-");
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("199.00"));
        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.PENDING);
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("generateInvoiceForSubscription - رفض فاتورة مكررة")
    void generateInvoice_shouldRejectDuplicate() {
        when(invoiceRepository.existsBySubscriptionIdAndStatus(100L, InvoiceStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> invoiceService.generateInvoiceForSubscription(subscription))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("getInvoice - جلب فاتورة بالمعرف")
    void getInvoice_shouldReturnInvoice() {
        when(invoiceRepository.findById(50L)).thenReturn(Optional.of(pendingInvoice));

        Invoice result = invoiceService.getInvoice(50L);

        assertThat(result.getInvoiceNumber()).isEqualTo("TWS-INV-12345678");
    }

    @Test
    @DisplayName("getInvoice - رمي خطأ عند عدم وجود فاتورة")
    void getInvoice_shouldThrowNotFound() {
        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.getInvoice(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("markAsPaid - تعليم فاتورة كمدفوعة")
    void markAsPaid_shouldMarkInvoice() {
        when(invoiceRepository.findById(50L)).thenReturn(Optional.of(pendingInvoice));
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(subscriptionService.activate(100L)).thenReturn(subscription);

        Invoice result = invoiceService.markAsPaid(50L, "PAYMOB");

        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(result.getPaidAt()).isNotNull();
        assertThat(result.getPaymentGateway()).isEqualTo("PAYMOB");
        verify(subscriptionService).activate(100L);
    }

    @Test
    @DisplayName("markAsPaid - رفض دفع فاتورة مدفوعة")
    void markAsPaid_shouldRejectAlreadyPaid() {
        pendingInvoice.setStatus(InvoiceStatus.PAID);
        when(invoiceRepository.findById(50L)).thenReturn(Optional.of(pendingInvoice));

        assertThatThrownBy(() -> invoiceService.markAsPaid(50L, "PAYMOB"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("refundInvoice - استرداد فاتورة مدفوعة")
    void refundInvoice_shouldRefund() {
        pendingInvoice.setStatus(InvoiceStatus.PAID);
        when(invoiceRepository.findById(50L)).thenReturn(Optional.of(pendingInvoice));
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Invoice result = invoiceService.refundInvoice(50L);

        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.REFUNDED);
    }

    @Test
    @DisplayName("refundInvoice - رفض استرداد فاتورة غير مدفوعة")
    void refundInvoice_shouldRejectNonPaid() {
        when(invoiceRepository.findById(50L)).thenReturn(Optional.of(pendingInvoice));

        assertThatThrownBy(() -> invoiceService.refundInvoice(50L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("cancelInvoice - إلغاء فاتورة معلقة")
    void cancelInvoice_shouldCancel() {
        when(invoiceRepository.findById(50L)).thenReturn(Optional.of(pendingInvoice));
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Invoice result = invoiceService.cancelInvoice(50L);

        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
    }

    @Test
    @DisplayName("getInvoicesByMerchant - جلب فواتير التاجر")
    void getInvoicesByMerchant_shouldReturnPage() {
        Page<Invoice> page = new PageImpl<>(List.of(pendingInvoice));
        when(invoiceRepository.findBySubscriptionMerchantIdOrderByCreatedAtDesc(eq(1L), any()))
                .thenReturn(page);

        Page<Invoice> result = invoiceService.getInvoicesByMerchant(1L, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
    }
}
