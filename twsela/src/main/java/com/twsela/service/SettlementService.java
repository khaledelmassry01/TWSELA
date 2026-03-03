package com.twsela.service;

import com.twsela.domain.PaymentIntent;
import com.twsela.domain.SettlementBatch;
import com.twsela.domain.SettlementItem;
import com.twsela.domain.User;
import com.twsela.repository.PaymentIntentRepository;
import com.twsela.repository.SettlementBatchRepository;
import com.twsela.repository.SettlementItemRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * خدمة التسويات المالية — إنشاء دفعات التسوية وحساب الصافي ومعالجة التحويلات.
 */
@Service
@Transactional
public class SettlementService {

    private static final Logger log = LoggerFactory.getLogger(SettlementService.class);
    private static final BigDecimal DEFAULT_FEE_PERCENTAGE = new BigDecimal("0.025"); // 2.5%

    private final SettlementBatchRepository settlementBatchRepository;
    private final SettlementItemRepository settlementItemRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    private final UserRepository userRepository;

    public SettlementService(SettlementBatchRepository settlementBatchRepository,
                             SettlementItemRepository settlementItemRepository,
                             PaymentIntentRepository paymentIntentRepository,
                             UserRepository userRepository) {
        this.settlementBatchRepository = settlementBatchRepository;
        this.settlementItemRepository = settlementItemRepository;
        this.paymentIntentRepository = paymentIntentRepository;
        this.userRepository = userRepository;
    }

    /**
     * إنشاء دفعة تسوية جديدة لفترة زمنية.
     */
    public SettlementBatch generateBatch(SettlementBatch.SettlementPeriod period,
                                          LocalDate startDate, LocalDate endDate,
                                          Long generatedByUserId) {
        // Check for duplicate
        settlementBatchRepository.findByPeriodAndStartDate(period, startDate)
                .ifPresent(existing -> {
                    throw new BusinessRuleException("توجد تسوية بالفعل لنفس الفترة: " + existing.getSettlementNumber());
                });

        User generatedBy = userRepository.findById(generatedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", generatedByUserId));

        SettlementBatch batch = new SettlementBatch();
        batch.setSettlementNumber("STL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        batch.setPeriod(period);
        batch.setStartDate(startDate);
        batch.setEndDate(endDate);
        batch.setStatus(SettlementBatch.BatchStatus.DRAFT);
        batch.setGeneratedBy(generatedBy);
        batch.setTotalTransactions(0);
        batch.setTotalAmount(BigDecimal.ZERO);
        batch.setTotalFees(BigDecimal.ZERO);
        batch.setNetAmount(BigDecimal.ZERO);

        SettlementBatch saved = settlementBatchRepository.save(batch);
        log.info("Settlement batch {} created — period={}, dates={} to {}", saved.getSettlementNumber(), period, startDate, endDate);
        return saved;
    }

    /**
     * إضافة بند تسوية لدفعة.
     */
    public SettlementItem addItem(Long batchId, Long shipmentId, Long merchantId,
                                   BigDecimal amount, SettlementItem.ItemType type) {
        SettlementBatch batch = getBatchById(batchId);

        if (batch.getStatus() != SettlementBatch.BatchStatus.DRAFT) {
            throw new BusinessRuleException("لا يمكن إضافة بنود لتسوية بحالة: " + batch.getStatus());
        }

        BigDecimal fee = calculateFee(amount, type);
        BigDecimal netAmount = amount.subtract(fee);

        SettlementItem item = new SettlementItem();
        item.setBatch(batch);
        item.setAmount(amount);
        item.setFee(fee);
        item.setNetAmount(netAmount);
        item.setType(type);

        SettlementItem saved = settlementItemRepository.save(item);

        // Update batch totals
        recalculateBatchTotals(batch);

        log.info("Settlement item {} added to batch {} — type={}, amount={}, fee={}", saved.getId(), batchId, type, amount, fee);
        return saved;
    }

    /**
     * معالجة دفعة تسوية (تحويل المبالغ).
     */
    public SettlementBatch processBatch(Long batchId) {
        SettlementBatch batch = getBatchById(batchId);

        if (batch.getStatus() != SettlementBatch.BatchStatus.DRAFT && batch.getStatus() != SettlementBatch.BatchStatus.PENDING) {
            throw new BusinessRuleException("لا يمكن معالجة تسوية بحالة: " + batch.getStatus());
        }

        batch.setStatus(SettlementBatch.BatchStatus.PROCESSING);
        settlementBatchRepository.save(batch);

        try {
            // In production: initiate bank transfers for each merchant
            batch.setStatus(SettlementBatch.BatchStatus.COMPLETED);
            batch.setProcessedAt(Instant.now());
            batch.setUpdatedAt(Instant.now());
            log.info("Settlement batch {} processed successfully — net={}", batch.getSettlementNumber(), batch.getNetAmount());
        } catch (Exception e) {
            batch.setStatus(SettlementBatch.BatchStatus.FAILED);
            batch.setNotes("Processing failed: " + e.getMessage());
            batch.setUpdatedAt(Instant.now());
            log.error("Settlement batch {} processing failed: {}", batch.getSettlementNumber(), e.getMessage());
        }

        return settlementBatchRepository.save(batch);
    }

    @Transactional(readOnly = true)
    public SettlementBatch getBatchById(Long batchId) {
        return settlementBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("SettlementBatch", "id", batchId));
    }

    @Transactional(readOnly = true)
    public List<SettlementBatch> getBatchesByStatus(SettlementBatch.BatchStatus status) {
        return settlementBatchRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public List<SettlementItem> getBatchItems(Long batchId) {
        return settlementItemRepository.findByBatchId(batchId);
    }

    @Transactional(readOnly = true)
    public List<SettlementBatch> getBatchesByDateRange(LocalDate start, LocalDate end) {
        return settlementBatchRepository.findByDateRange(start, end);
    }

    // ── Internal helpers ──

    private BigDecimal calculateFee(BigDecimal amount, SettlementItem.ItemType type) {
        if (type == SettlementItem.ItemType.REFUND || type == SettlementItem.ItemType.ADJUSTMENT) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(DEFAULT_FEE_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);
    }

    private void recalculateBatchTotals(SettlementBatch batch) {
        List<SettlementItem> items = settlementItemRepository.findByBatchId(batch.getId());
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalFees = BigDecimal.ZERO;
        BigDecimal netAmount = BigDecimal.ZERO;

        for (SettlementItem item : items) {
            totalAmount = totalAmount.add(item.getAmount());
            totalFees = totalFees.add(item.getFee());
            netAmount = netAmount.add(item.getNetAmount());
        }

        batch.setTotalTransactions(items.size());
        batch.setTotalAmount(totalAmount);
        batch.setTotalFees(totalFees);
        batch.setNetAmount(netAmount);
        batch.setUpdatedAt(Instant.now());
        settlementBatchRepository.save(batch);
    }
}
