package com.twsela.repository;

import com.twsela.domain.DeliveryRedirect;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeliveryRedirectRepository extends JpaRepository<DeliveryRedirect, Long> {
    List<DeliveryRedirect> findByShipmentIdOrderByCreatedAtDesc(Long shipmentId);
    List<DeliveryRedirect> findByStatusOrderByCreatedAtDesc(String status);
}
