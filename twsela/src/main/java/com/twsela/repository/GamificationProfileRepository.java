package com.twsela.repository;

import com.twsela.domain.GamificationProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GamificationProfileRepository extends JpaRepository<GamificationProfile, Long> {

    Optional<GamificationProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    List<GamificationProfile> findByTierOrderByTotalXpDesc(String tier);

    List<GamificationProfile> findByTenantIdOrderByTotalXpDesc(Long tenantId);

    List<GamificationProfile> findTop10ByTenantIdOrderByTotalXpDesc(Long tenantId);
}
