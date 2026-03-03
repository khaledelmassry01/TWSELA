package com.twsela.repository;

import com.twsela.domain.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {

    List<LoyaltyTransaction> findByLoyaltyProgramIdOrderByCreatedAtDesc(Long loyaltyProgramId);

    List<LoyaltyTransaction> findByLoyaltyProgramIdAndTransactionType(Long loyaltyProgramId, String transactionType);

    List<LoyaltyTransaction> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
}
