package com.twsela.repository;

import com.twsela.domain.CashMovementLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface CashMovementLedgerRepository extends JpaRepository<CashMovementLedger, Long> {
    
    List<CashMovementLedger> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<CashMovementLedger> findByTransactionTypeOrderByCreatedAtDesc(CashMovementLedger.TransactionType transactionType);
    
    List<CashMovementLedger> findByStatusOrderByCreatedAtDesc(CashMovementLedger.TransactionStatus status);
    
    @Query("SELECT c FROM CashMovementLedger c WHERE c.user.id = :userId AND c.transactionType = :transactionType ORDER BY c.createdAt DESC")
    List<CashMovementLedger> findByUserIdAndTransactionType(@Param("userId") Long userId, @Param("transactionType") CashMovementLedger.TransactionType transactionType);
    
    @Query("SELECT SUM(c.amount) FROM CashMovementLedger c WHERE c.user.id = :userId AND c.transactionType = :transactionType AND c.status = :status")
    BigDecimal sumAmountByUserIdAndTransactionTypeAndStatus(@Param("userId") Long userId, @Param("transactionType") CashMovementLedger.TransactionType transactionType, @Param("status") CashMovementLedger.TransactionStatus status);
    
    @Query("SELECT c FROM CashMovementLedger c WHERE c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    List<CashMovementLedger> findByCreatedAtBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
}
