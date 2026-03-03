package com.twsela.repository;

import com.twsela.domain.ArchivePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchivePolicyRepository extends JpaRepository<ArchivePolicy, Long> {
    List<ArchivePolicy> findByIsActiveTrue();
    List<ArchivePolicy> findByEntityType(String entityType);
    List<ArchivePolicy> findByTenantId(Long tenantId);
}
