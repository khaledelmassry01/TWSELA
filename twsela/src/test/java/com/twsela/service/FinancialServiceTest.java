package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialServiceTest {

    @Mock private PayoutRepository payoutRepository;
    @Mock private PayoutItemRepository payoutItemRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private PayoutStatusRepository payoutStatusRepository;

    @InjectMocks private FinancialService financialService;

    private User courier;
    private User merchant;
    private PayoutStatus pendingStatus;
    private PayoutStatus completedStatus;
    private Shipment deliveredShipment;

    @BeforeEach
    void setUp() {
        Role courierRole = new Role("COURIER");
        courierRole.setId(1L);
        courier = new User();
        courier.setId(10L);
        courier.setName("Test Courier");
        courier.setRole(courierRole);

        Role merchantRole = new Role("MERCHANT");
        merchantRole.setId(2L);
        merchant = new User();
        merchant.setId(20L);
        merchant.setName("Test Merchant");
        merchant.setRole(merchantRole);

        pendingStatus = new PayoutStatus("PENDING");
        pendingStatus.setId(1L);
        completedStatus = new PayoutStatus("COMPLETED");
        completedStatus.setId(2L);

        ShipmentStatus deliveredSt = new ShipmentStatus("DELIVERED", "تم التسليم");
        deliveredSt.setId(1L);
        RecipientDetails rd = new RecipientDetails("01012345678", "Test", "Cairo");

        deliveredShipment = new Shipment();
        deliveredShipment.setId(100L);
        deliveredShipment.setTrackingNumber("TWS-12345678");
        deliveredShipment.setMerchant(merchant);
        deliveredShipment.setStatus(deliveredSt);
        deliveredShipment.setDeliveryFee(new BigDecimal("100.00"));
        deliveredShipment.setItemValue(new BigDecimal("500.00"));
        deliveredShipment.setCodAmount(new BigDecimal("500.00"));
        deliveredShipment.setRecipientDetails(rd);
    }

    // ======== createCourierPayout ========

    @Test
    @DisplayName("createCourierPayout — يجب إنشاء دفعة سائق وحساب 70% من الرسوم")
    void createCourierPayout_success() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(courier));
        when(shipmentRepository.findByCourierIdAndStatusNameAndCashReconciledFalse(eq(10L), eq("DELIVERED")))
                .thenReturn(List.of(deliveredShipment));
        when(payoutStatusRepository.findByName("PENDING")).thenReturn(Optional.of(pendingStatus));
        when(payoutRepository.save(any(Payout.class))).thenAnswer(inv -> {
            Payout p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(payoutItemRepository.save(any(PayoutItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payout result = financialService.createCourierPayout(10L,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        // 70% of 100 = 70
        assertThat(result.getNetAmount()).isEqualByComparingTo(new BigDecimal("70.00"));
        assertThat(result.getPayoutType()).isEqualTo(Payout.PayoutType.COURIER_SETTLEMENT);
        verify(payoutItemRepository).save(any(PayoutItem.class));
    }

    // ======== createMerchantPayout ========

    @Test
    @DisplayName("createMerchantPayout — يجب إنشاء دفعة تاجر بنجاح")
    void createMerchantPayout_success() {
        when(userRepository.findById(20L)).thenReturn(Optional.of(merchant));
        when(shipmentRepository.findByMerchantIdAndStatusNameAndPayoutIsNull(eq(20L), eq("DELIVERED")))
                .thenReturn(List.of(deliveredShipment));
        when(payoutStatusRepository.findByName("PENDING")).thenReturn(Optional.of(pendingStatus));
        when(payoutRepository.save(any(Payout.class))).thenAnswer(inv -> {
            Payout p = inv.getArgument(0);
            p.setId(2L);
            return p;
        });
        when(payoutItemRepository.save(any(PayoutItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payout result = financialService.createMerchantPayout(20L,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        assertThat(result).isNotNull();
        assertThat(result.getNetAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.getPayoutType()).isEqualTo(Payout.PayoutType.MERCHANT_PAYOUT);
    }

    // ======== getPayoutsForUser ========

    @Test
    @DisplayName("getPayoutsForUser — يجب إرجاع قائمة الدفعات للمستخدم")
    void getPayoutsForUser_returnsList() {
        Payout payout = new Payout(courier, Payout.PayoutType.COURIER_SETTLEMENT, pendingStatus,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), new BigDecimal("70.00"));
        payout.setId(1L);

        when(payoutRepository.findByUserIdOrderByPayoutPeriodEndDesc(10L))
                .thenReturn(List.of(payout));

        List<Payout> result = financialService.getPayoutsForUser(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNetAmount()).isEqualByComparingTo(new BigDecimal("70.00"));
    }

    // ======== updatePayoutStatus ========

    @Test
    @DisplayName("updatePayoutStatus — يجب تحديث حالة الدفعة إلى COMPLETED وتعيين paidAt")
    void updatePayoutStatus_completed() {
        Payout payout = new Payout(courier, Payout.PayoutType.COURIER_SETTLEMENT, pendingStatus,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), new BigDecimal("70.00"));
        payout.setId(1L);

        when(payoutRepository.findById(1L)).thenReturn(Optional.of(payout));
        when(payoutStatusRepository.findByName("COMPLETED")).thenReturn(Optional.of(completedStatus));
        when(payoutRepository.save(any(Payout.class))).thenAnswer(inv -> inv.getArgument(0));

        Payout result = financialService.updatePayoutStatus(1L, "COMPLETED");

        assertThat(result.getStatus().getName()).isEqualTo("COMPLETED");
        assertThat(result.getPaidAt()).isNotNull();
    }

    // ======== getPendingPayouts ========

    @Test
    @DisplayName("getPendingPayouts — يجب إرجاع الدفعات المعلقة فقط")
    void getPendingPayouts_returnsPending() {
        when(payoutStatusRepository.findByName("PENDING")).thenReturn(Optional.of(pendingStatus));
        when(payoutRepository.findByStatus(pendingStatus)).thenReturn(List.of());

        List<Payout> result = financialService.getPendingPayouts();

        assertThat(result).isEmpty();
    }

    // ======== getPayoutById ========

    @Test
    @DisplayName("getPayoutById — يجب إرجاع الدفعة عند وجود المعرف")
    void getPayoutById_found() {
        Payout payout = new Payout(courier, Payout.PayoutType.COURIER_SETTLEMENT, pendingStatus,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), new BigDecimal("70.00"));
        payout.setId(1L);

        when(payoutRepository.findById(1L)).thenReturn(Optional.of(payout));

        Payout result = financialService.getPayoutById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getPayoutById — يجب رمي استثناء عند عدم وجود الدفعة")
    void getPayoutById_notFound() {
        when(payoutRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialService.getPayoutById(999L))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }

    // ======== getPayoutItems ========

    @Test
    @DisplayName("getPayoutItems — يجب إرجاع عناصر الدفعة")
    void getPayoutItems_returnsList() {
        when(payoutItemRepository.findByPayoutId(1L)).thenReturn(List.of());

        List<PayoutItem> result = financialService.getPayoutItems(1L);

        assertThat(result).isEmpty();
    }
}
