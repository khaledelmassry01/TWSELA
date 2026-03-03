package com.twsela.repository;

import com.twsela.domain.PartnerHandoff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PartnerHandoffRepository extends JpaRepository<PartnerHandoff, Long> {
    List<PartnerHandoff> findByShipmentId(Long shipmentId);
    List<PartnerHandoff> findByPartnerIdOrderByCreatedAtDesc(Long partnerId);
    List<PartnerHandoff> findByStatusAndTenantIdOrderByCreatedAtDesc(String status, Long tenantId);
}
