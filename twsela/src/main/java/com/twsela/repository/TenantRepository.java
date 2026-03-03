package com.twsela.repository;

import com.twsela.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findBySlug(String slug);

    Optional<Tenant> findByDomain(String domain);

    List<Tenant> findByStatus(Tenant.TenantStatus status);

    List<Tenant> findByPlan(Tenant.TenantPlan plan);

    Optional<Tenant> findByTenantId(String tenantId);

    boolean existsBySlug(String slug);

    boolean existsByDomain(String domain);
}
