package com.twsela.repository;

import com.twsela.domain.DeliveryProof;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryProofRepository extends JpaRepository<DeliveryProof, Long> {

    Optional<DeliveryProof> findByShipmentId(Long shipmentId);

    boolean existsByShipmentId(Long shipmentId);
}
