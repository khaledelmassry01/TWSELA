package com.twsela.repository;

import com.twsela.domain.TenantAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantAuditLogRepository extends JpaRepository<TenantAuditLog, Long> {

    List<TenantAuditLog> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<TenantAuditLog> findByTenantIdAndAction(Long tenantId, String action);
}
