package com.twsela.repository;

import com.twsela.domain.Shipment;
import com.twsela.domain.ShipmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ShipmentStatusHistoryRepository extends JpaRepository<ShipmentStatusHistory, Long> {
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ShipmentStatusHistory s WHERE s.shipment = :shipment")
    void deleteByShipment(@Param("shipment") Shipment shipment);
}


