package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.domain.Wallet.WalletType;
import com.twsela.domain.WalletTransaction.TransactionReason;
import com.twsela.domain.WalletTransaction.TransactionType;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.UserRepository;
import com.twsela.repository.WalletRepository;
import com.twsela.repository.WalletTransactionRepository;
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
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock private WalletRepository walletRepository;
    @Mock private WalletTransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private ShipmentRepository shipmentRepository;

    @InjectMocks
    private WalletService walletService;

    private User merchant;
    private User courier;
    private Wallet merchantWallet;
    private Wallet courierWallet;

    @BeforeEach
    void setUp() {
        merchant = new User();
        merchant.setId(1L);
        merchant.setName("Test Merchant");

        courier = new User();
        courier.setId(2L);
        courier.setName("Test Courier");

        merchantWallet = new Wallet(merchant, WalletType.MERCHANT);
        merchantWallet.setId(10L);
        merchantWallet.setBalance(new BigDecimal("1000.00"));
        merchantWallet.setUpdatedAt(Instant.now());

        courierWallet = new Wallet(courier, WalletType.COURIER);
        courierWallet.setId(20L);
        courierWallet.setBalance(new BigDecimal("500.00"));
        courierWallet.setUpdatedAt(Instant.now());
    }

    @Test
    @DisplayName("getOrCreateWallet - الحصول على محفظة موجودة")
    void getOrCreateWallet_existingWallet() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(merchantWallet));
        Wallet result = walletService.getOrCreateWallet(1L, WalletType.MERCHANT);
        assertThat(result.getId()).isEqualTo(10L);
        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("getOrCreateWallet - إنشاء محفظة جديدة")
    void getOrCreateWallet_createsNew() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(merchant));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> {
            Wallet w = inv.getArgument(0);
            w.setId(99L);
            return w;
        });

        Wallet result = walletService.getOrCreateWallet(1L, WalletType.MERCHANT);
        assertThat(result.getId()).isEqualTo(99L);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    @DisplayName("getWalletByUserId - محفظة غير موجودة")
    void getWalletByUserId_notFound() {
        when(walletRepository.findByUserId(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> walletService.getWalletByUserId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("credit - إيداع مبلغ بنجاح")
    void credit_success() {
        when(walletRepository.findById(10L)).thenReturn(Optional.of(merchantWallet));
        when(walletRepository.save(any())).thenReturn(merchantWallet);
        when(transactionRepository.save(any(WalletTransaction.class))).thenAnswer(inv -> {
            WalletTransaction tx = inv.getArgument(0);
            tx.setId(1L);
            return tx;
        });

        WalletTransaction tx = walletService.credit(10L, new BigDecimal("200.00"),
                TransactionReason.COD_COLLECTED, 1L, "Test credit");

        assertThat(tx.getBalanceBefore()).isEqualByComparingTo("1000.00");
        assertThat(tx.getBalanceAfter()).isEqualByComparingTo("1200.00");
        assertThat(merchantWallet.getBalance()).isEqualByComparingTo("1200.00");
    }

    @Test
    @DisplayName("credit - مبلغ صفر أو سالب")
    void credit_invalidAmount() {
        assertThatThrownBy(() -> walletService.credit(10L, BigDecimal.ZERO,
                TransactionReason.COD_COLLECTED, null, "bad"))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> walletService.credit(10L, new BigDecimal("-5"),
                TransactionReason.COD_COLLECTED, null, "bad"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("debit - سحب مبلغ بنجاح")
    void debit_success() {
        when(walletRepository.findById(10L)).thenReturn(Optional.of(merchantWallet));
        when(walletRepository.save(any())).thenReturn(merchantWallet);
        when(transactionRepository.save(any(WalletTransaction.class))).thenAnswer(inv -> {
            WalletTransaction tx = inv.getArgument(0);
            tx.setId(2L);
            return tx;
        });

        WalletTransaction tx = walletService.debit(10L, new BigDecimal("300.00"),
                TransactionReason.WITHDRAWAL, null, "Test withdrawal");

        assertThat(tx.getBalanceBefore()).isEqualByComparingTo("1000.00");
        assertThat(tx.getBalanceAfter()).isEqualByComparingTo("700.00");
    }

    @Test
    @DisplayName("debit - رصيد غير كافٍ")
    void debit_insufficientBalance() {
        when(walletRepository.findById(10L)).thenReturn(Optional.of(merchantWallet));
        assertThatThrownBy(() -> walletService.debit(10L, new BigDecimal("5000.00"),
                TransactionReason.WITHDRAWAL, null, "too much"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("رصيد غير كافٍ");
    }

    @Test
    @DisplayName("getBalance - رصيد المحفظة")
    void getBalance_success() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(merchantWallet));
        BigDecimal balance = walletService.getBalance(1L);
        assertThat(balance).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("getTransactions - سجل المعاملات")
    void getTransactions_success() {
        WalletTransaction tx = new WalletTransaction(merchantWallet, TransactionType.CREDIT,
                new BigDecimal("100"), TransactionReason.COD_COLLECTED, 1L, "test");
        tx.setId(1L);
        Page<WalletTransaction> page = new PageImpl<>(List.of(tx));
        when(transactionRepository.findByWalletIdOrderByCreatedAtDesc(eq(10L), any(Pageable.class)))
                .thenReturn(page);

        Page<WalletTransaction> result = walletService.getTransactions(10L, PageRequest.of(0, 20));
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("settleShipment - تسوية شحنة COD")
    void settleShipment_codSettlement() {
        Shipment shipment = new Shipment();
        shipment.setId(100L);
        shipment.setTrackingNumber("TS100");
        shipment.setCodAmount(new BigDecimal("500.00"));
        shipment.setDeliveryFee(new BigDecimal("30.00"));
        shipment.setCourier(courier);
        shipment.setMerchant(merchant);

        when(shipmentRepository.findById(100L)).thenReturn(Optional.of(shipment));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(merchantWallet));
        when(walletRepository.findById(10L)).thenReturn(Optional.of(merchantWallet));
        when(walletRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(inv -> {
            WalletTransaction tx = inv.getArgument(0);
            tx.setId(99L);
            return tx;
        });
        when(transactionRepository.existsByWalletIdAndReferenceIdAndReason(anyLong(), anyLong(), any()))
                .thenReturn(false);

        walletService.settleShipment(100L);

        verify(transactionRepository, atLeast(1)).save(any(WalletTransaction.class));
    }

    @Test
    @DisplayName("settleShipment - تخطي تسوية مكررة (idempotent)")
    void settleShipment_alreadySettled() {
        Shipment shipment = new Shipment();
        shipment.setId(100L);
        shipment.setTrackingNumber("TS100");
        shipment.setCodAmount(new BigDecimal("500.00"));
        shipment.setDeliveryFee(new BigDecimal("30.00"));
        shipment.setCourier(courier);
        shipment.setMerchant(merchant);

        when(shipmentRepository.findById(100L)).thenReturn(Optional.of(shipment));
        when(walletRepository.findByUserId(anyLong())).thenReturn(Optional.of(courierWallet));
        when(transactionRepository.existsByWalletIdAndReferenceIdAndReason(anyLong(), anyLong(), any()))
                .thenReturn(true);

        walletService.settleShipment(100L);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAllWallets - قائمة المحافظ")
    void getAllWallets_success() {
        when(walletRepository.findAll()).thenReturn(List.of(merchantWallet, courierWallet));
        List<Wallet> wallets = walletService.getAllWallets();
        assertThat(wallets).hasSize(2);
    }
}
