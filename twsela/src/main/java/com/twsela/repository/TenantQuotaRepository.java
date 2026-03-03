package com.twsela.repository;

import com.twsela.domain.TenantQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantQuotaRepository extends JpaRepository<TenantQuota, Long> {

    Optional<TenantQuota> findByTenantIdAndQuotaType(Long tenantId, TenantQuota.QuotaType quotaType);

    List<TenantQuota> findByTenantId(Long tenantId);

    @Query("SELECT q FROM TenantQuota q WHERE q.tenant.id = :tenantId AND q.currentValue >= q.maxValue")
    List<TenantQuota> findExceeded(@org.springframework.data.repository.query.Param("tenantId") Long tenantId);
}
