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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Wallet service: credit, debit, settlement upon delivery.
 *
 * Settlement logic when a COD shipment is delivered:
 *   1. Courier wallet  ← CREDIT (COD amount collected)
 *   2. Company wallet  ← CREDIT (commission = delivery fee)
 *   3. Merchant wallet ← CREDIT (COD amount − delivery fee)
 */
@Service
@Transactional
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ShipmentRepository shipmentRepository;

    public WalletService(WalletRepository walletRepository,
                         WalletTransactionRepository transactionRepository,
                         UserRepository userRepository,
                         ShipmentRepository shipmentRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.shipmentRepository = shipmentRepository;
    }

    /**
     * Get or create a wallet for a user.
     */
    public Wallet getOrCreateWallet(Long userId, WalletType type) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                    Wallet wallet = new Wallet(user, type);
                    log.info("Created new {} wallet for user {}", type, userId);
                    return walletRepository.save(wallet);
                });
    }

    /**
     * Get wallet for current user.
     */
    @Transactional(readOnly = true)
    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "userId", userId));
    }

    /**
     * Credit (add funds) to a wallet.
     */
    public WalletTransaction credit(Long walletId, BigDecimal amount, TransactionReason reason,
                                     Long referenceId, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("مبلغ الإيداع يجب أن يكون أكبر من صفر");
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", walletId));

        BigDecimal before = wallet.getBalance();
        BigDecimal after = before.add(amount);
        wallet.setBalance(after);
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);

        WalletTransaction tx = new WalletTransaction(wallet, TransactionType.CREDIT, amount, reason, referenceId, description);
        tx.setBalanceBefore(before);
        tx.setBalanceAfter(after);
        return transactionRepository.save(tx);
    }

    /**
     * Debit (withdraw funds) from a wallet.
     */
    public WalletTransaction debit(Long walletId, BigDecimal amount, TransactionReason reason,
                                    Long referenceId, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("مبلغ السحب يجب أن يكون أكبر من صفر");
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "id", walletId));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessRuleException("رصيد غير كافٍ — الرصيد الحالي: " + wallet.getBalance());
        }

        BigDecimal before = wallet.getBalance();
        BigDecimal after = before.subtract(amount);
        wallet.setBalance(after);
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);

        WalletTransaction tx = new WalletTransaction(wallet, TransactionType.DEBIT, amount, reason, referenceId, description);
        tx.setBalanceBefore(before);
        tx.setBalanceAfter(after);
        return transactionRepository.save(tx);
    }

    /**
     * Get wallet balance.
     */
    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "userId", userId));
        return wallet.getBalance();
    }

    /**
     * Get paginated transaction history for a wallet.
     */
    @Transactional(readOnly = true)
    public Page<WalletTransaction> getTransactions(Long walletId, Pageable pageable) {
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId, pageable);
    }

    /**
     * Settle a shipment upon delivery (COD flow).
     * Idempotent — ignores if already settled for this shipment.
     */
    public void settleShipment(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId));

        BigDecimal codAmount = shipment.getCodAmount() != null ? shipment.getCodAmount() : BigDecimal.ZERO;
        BigDecimal deliveryFee = shipment.getDeliveryFee() != null ? shipment.getDeliveryFee() : BigDecimal.ZERO;

        // Skip if no money involved
        if (codAmount.compareTo(BigDecimal.ZERO) == 0 && deliveryFee.compareTo(BigDecimal.ZERO) == 0) {
            log.debug("Skipping settlement for shipment {} — no COD/fee", shipmentId);
            return;
        }

        // 1. Courier wallet — credit COD collected
        User courier = shipment.getCourier();
        if (courier != null && codAmount.compareTo(BigDecimal.ZERO) > 0) {
            Wallet courierWallet = getOrCreateWallet(courier.getId(), WalletType.COURIER);
            // Idempotency check
            if (!transactionRepository.existsByWalletIdAndReferenceIdAndReason(
                    courierWallet.getId(), shipmentId, TransactionReason.COD_COLLECTED)) {
                credit(courierWallet.getId(), codAmount, TransactionReason.COD_COLLECTED,
                        shipmentId, "COD collected for shipment " + shipment.getTrackingNumber());
            }
        }

        // 2. Merchant wallet — credit (COD − delivery fee)
        User merchant = shipment.getMerchant();
        if (merchant != null) {
            BigDecimal merchantAmount = codAmount.subtract(deliveryFee);
            if (merchantAmount.compareTo(BigDecimal.ZERO) > 0) {
                Wallet merchantWallet = getOrCreateWallet(merchant.getId(), WalletType.MERCHANT);
                if (!transactionRepository.existsByWalletIdAndReferenceIdAndReason(
                        merchantWallet.getId(), shipmentId, TransactionReason.SETTLEMENT)) {
                    credit(merchantWallet.getId(), merchantAmount, TransactionReason.SETTLEMENT,
                            shipmentId, "Settlement for shipment " + shipment.getTrackingNumber());
                }
            }
        }

        log.info("Settled shipment {} — COD: {}, fee: {}", shipmentId, codAmount, deliveryFee);
    }

    /**
     * Get all wallets (admin).
     */
    @Transactional(readOnly = true)
    public java.util.List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }
}
