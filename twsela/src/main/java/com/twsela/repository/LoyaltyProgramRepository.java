package com.twsela.repository;

import com.twsela.domain.LoyaltyProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyProgramRepository extends JpaRepository<LoyaltyProgram, Long> {

    Optional<LoyaltyProgram> findByMerchantId(Long merchantId);

    boolean existsByMerchantId(Long merchantId);

    List<LoyaltyProgram> findByTierOrderByLifetimePointsDesc(String tier);

    List<LoyaltyProgram> findByTenantId(Long tenantId);
}
