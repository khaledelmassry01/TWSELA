package com.twsela.repository;

import com.twsela.domain.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    Page<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM WalletTransaction t WHERE t.wallet.id = :walletId AND t.type = :type")
    BigDecimal sumByWalletIdAndType(@Param("walletId") Long walletId,
                                    @Param("type") WalletTransaction.TransactionType type);

    boolean existsByWalletIdAndReferenceIdAndReason(Long walletId, Long referenceId,
                                                     WalletTransaction.TransactionReason reason);
}
