package com.twsela.repository;

import com.twsela.domain.CarrierShipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarrierShipmentRepository extends JpaRepository<CarrierShipment, Long> {
    Optional<CarrierShipment> findByShipmentId(Long shipmentId);
    Optional<CarrierShipment> findByExternalTrackingNumber(String trackingNumber);
    List<CarrierShipment> findByCarrierIdOrderByCreatedAtDesc(Long carrierId);
    List<CarrierShipment> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
}
