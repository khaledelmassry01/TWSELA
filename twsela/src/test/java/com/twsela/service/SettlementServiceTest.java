package com.twsela.service;

import com.twsela.domain.PaymentIntent;
import com.twsela.domain.Shipment;
import com.twsela.domain.SettlementBatch;
import com.twsela.domain.SettlementItem;
import com.twsela.domain.User;
import com.twsela.repository.PaymentIntentRepository;
import com.twsela.repository.SettlementBatchRepository;
import com.twsela.repository.SettlementItemRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock private SettlementBatchRepository settlementBatchRepository;
    @Mock private SettlementItemRepository settlementItemRepository;
    @Mock private PaymentIntentRepository paymentIntentRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private SettlementService settlementService;

    private User generator;
    private SettlementBatch draftBatch;

    @BeforeEach
    void setUp() {
        generator = new User();
        generator.setId(1L);
        generator.setName("مسؤول التسويات");

        draftBatch = new SettlementBatch();
        draftBatch.setId(10L);
        draftBatch.setSettlementNumber("STL-20240101-001");
        draftBatch.setPeriod(SettlementBatch.SettlementPeriod.DAILY);
        draftBatch.setStartDate(LocalDate.of(2024, 1, 1));
        draftBatch.setEndDate(LocalDate.of(2024, 1, 1));
        draftBatch.setStatus(SettlementBatch.BatchStatus.DRAFT);
        draftBatch.setTotalTransactions(0);
        draftBatch.setTotalAmount(BigDecimal.ZERO);
        draftBatch.setTotalFees(BigDecimal.ZERO);
        draftBatch.setNetAmount(BigDecimal.ZERO);
        draftBatch.setGeneratedBy(generator);
    }

    @Test
    @DisplayName("generateBatch() creates a new settlement batch")
    void generateBatch_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(generator));
        when(settlementBatchRepository.findByPeriodAndStartDate(
                SettlementBatch.SettlementPeriod.DAILY, LocalDate.of(2024, 1, 1)))
                .thenReturn(Optional.empty());
        when(settlementBatchRepository.save(any(SettlementBatch.class))).thenAnswer(inv -> {
            SettlementBatch b = inv.getArgument(0);
            b.setId(10L);
            return b;
        });

        SettlementBatch result = settlementService.generateBatch(
                SettlementBatch.SettlementPeriod.DAILY,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 1),
                1L);

        assertNotNull(result);
        assertEquals(SettlementBatch.BatchStatus.DRAFT, result.getStatus());
        assertTrue(result.getSettlementNumber().startsWith("STL-"));
        verify(settlementBatchRepository).save(any(SettlementBatch.class));
    }

    @Test
    @DisplayName("generateBatch() throws for duplicate period and date")
    void generateBatch_duplicate() {
        when(settlementBatchRepository.findByPeriodAndStartDate(
                SettlementBatch.SettlementPeriod.DAILY, LocalDate.of(2024, 1, 1)))
                .thenReturn(Optional.of(draftBatch));

        assertThrows(BusinessRuleException.class,
                () -> settlementService.generateBatch(
                        SettlementBatch.SettlementPeriod.DAILY,
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 1, 1),
                        1L));
    }

    @Test
    @DisplayName("addItem() calculates 2.5% fee for COD type")
    void addItem_codWithFee() {
        when(settlementBatchRepository.findById(10L)).thenReturn(Optional.of(draftBatch));
        when(settlementItemRepository.save(any(SettlementItem.class))).thenAnswer(inv -> {
            SettlementItem item = inv.getArgument(0);
            item.setId(1L);
            return item;
        });

        SettlementItem result = settlementService.addItem(10L, null, null,
                new BigDecimal("1000.00"), SettlementItem.ItemType.COD);

        assertNotNull(result);
        assertEquals(new BigDecimal("1000.00"), result.getAmount());
        // Fee should be 2.5% of 1000 = 25.00
        assertEquals(0, new BigDecimal("25.00").compareTo(result.getFee()));
        // Net = 1000 - 25 = 975
        assertEquals(0, new BigDecimal("975.00").compareTo(result.getNetAmount()));
    }

    @Test
    @DisplayName("addItem() applies zero fee for REFUND type")
    void addItem_refundNoFee() {
        when(settlementBatchRepository.findById(10L)).thenReturn(Optional.of(draftBatch));
        when(settlementItemRepository.save(any(SettlementItem.class))).thenAnswer(inv -> {
            SettlementItem item = inv.getArgument(0);
            item.setId(2L);
            return item;
        });

        SettlementItem result = settlementService.addItem(10L, null, null,
                new BigDecimal("300.00"), SettlementItem.ItemType.REFUND);

        assertNotNull(result);
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getFee()));
        assertEquals(0, new BigDecimal("300.00").compareTo(result.getNetAmount()));
    }

    @Test
    @DisplayName("processBatch() processes a pending batch to COMPLETED")
    void processBatch_success() {
        draftBatch.setStatus(SettlementBatch.BatchStatus.PENDING);
        when(settlementBatchRepository.findById(10L)).thenReturn(Optional.of(draftBatch));
        when(settlementBatchRepository.save(any(SettlementBatch.class))).thenAnswer(inv -> inv.getArgument(0));

        SettlementBatch result = settlementService.processBatch(10L);

        assertEquals(SettlementBatch.BatchStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getProcessedAt());
    }

    @Test
    @DisplayName("processBatch() throws for completed batch")
    void processBatch_wrongStatus() {
        draftBatch.setStatus(SettlementBatch.BatchStatus.COMPLETED);
        when(settlementBatchRepository.findById(10L)).thenReturn(Optional.of(draftBatch));

        assertThrows(BusinessRuleException.class, () -> settlementService.processBatch(10L));
    }
}
