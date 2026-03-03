package com.twsela.repository;

import com.twsela.domain.CustomsDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomsDocumentRepository extends JpaRepository<CustomsDocument, Long> {
    List<CustomsDocument> findByShipmentId(Long shipmentId);
    List<CustomsDocument> findByDocumentType(String documentType);
    List<CustomsDocument> findByTenantId(Long tenantId);
}
