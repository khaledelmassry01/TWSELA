package com.twsela.repository;

import com.twsela.domain.DocumentAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentAuditLogRepository extends JpaRepository<DocumentAuditLog, Long> {
    List<DocumentAuditLog> findByDocumentId(Long documentId);
    List<DocumentAuditLog> findByAction(String action);
    List<DocumentAuditLog> findByPerformedById(Long performedById);
}
