package com.twsela.repository;

import com.twsela.domain.CarrierSelectionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CarrierSelectionRuleRepository extends JpaRepository<CarrierSelectionRule, Long> {
    List<CarrierSelectionRule> findByIsActiveTrueOrderByPriorityAsc();
    List<CarrierSelectionRule> findByZoneIdAndIsActiveTrueOrderByPriorityAsc(Long zoneId);
    List<CarrierSelectionRule> findByTenantIdAndIsActiveTrueOrderByPriorityAsc(Long tenantId);
}
