package com.twsela.repository;

import com.twsela.domain.ShipmentPackageDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ShipmentPackageDetailsRepository extends JpaRepository<ShipmentPackageDetails, Long> {
    Optional<ShipmentPackageDetails> findByShipmentId(Long shipmentId);
}

