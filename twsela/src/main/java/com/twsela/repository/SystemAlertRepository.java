package com.twsela.repository;

import com.twsela.domain.SystemAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemAlertRepository extends JpaRepository<SystemAlert, Long> {
    List<SystemAlert> findByAlertType(String alertType);
    List<SystemAlert> findBySeverity(String severity);
    List<SystemAlert> findByAcknowledgedFalse();
    List<SystemAlert> findByTenantId(Long tenantId);
}
