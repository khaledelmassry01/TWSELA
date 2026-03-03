package com.twsela.repository;

import com.twsela.domain.GeneratedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneratedDocumentRepository extends JpaRepository<GeneratedDocument, Long> {
    List<GeneratedDocument> findByShipmentId(Long shipmentId);
    List<GeneratedDocument> findByTemplateId(Long templateId);
    List<GeneratedDocument> findByDocumentType(String documentType);
    List<GeneratedDocument> findByTenantId(Long tenantId);
}
