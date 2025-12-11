package com.twsela.repository;

import com.twsela.domain.ShipmentManifest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentManifestRepository extends JpaRepository<ShipmentManifest, Long> {
    Optional<ShipmentManifest> findByManifestNumber(String manifestNumber);
    List<ShipmentManifest> findByCourierId(Long courierId);
    List<ShipmentManifest> findByCourierIdAndStatus(Long courierId, ShipmentManifest.ManifestStatus status);
}

