package com.twsela.repository;

import com.twsela.domain.DocumentBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentBatchRepository extends JpaRepository<DocumentBatch, Long> {
    List<DocumentBatch> findByStatus(String status);
    List<DocumentBatch> findByTenantId(Long tenantId);
    List<DocumentBatch> findByRequestedById(Long requestedById);
}
