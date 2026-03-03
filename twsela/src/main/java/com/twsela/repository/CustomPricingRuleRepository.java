package com.twsela.repository;

import com.twsela.domain.CustomPricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomPricingRuleRepository extends JpaRepository<CustomPricingRule, Long> {

    List<CustomPricingRule> findByContractId(Long contractId);

    List<CustomPricingRule> findByContractIdAndActiveTrue(Long contractId);

    @Query("SELECT r FROM CustomPricingRule r WHERE r.contract.id = :contractId " +
           "AND r.active = true " +
           "AND (r.zoneFrom.id = :zoneFromId OR r.zoneFrom IS NULL) " +
           "AND (r.zoneTo.id = :zoneToId OR r.zoneTo IS NULL) " +
           "ORDER BY CASE WHEN r.zoneFrom IS NOT NULL AND r.zoneTo IS NOT NULL THEN 0 " +
           "WHEN r.zoneFrom IS NOT NULL OR r.zoneTo IS NOT NULL THEN 1 ELSE 2 END")
    List<CustomPricingRule> findActiveByContractIdAndZones(
            @Param("contractId") Long contractId,
            @Param("zoneFromId") Long zoneFromId,
            @Param("zoneToId") Long zoneToId);
}
