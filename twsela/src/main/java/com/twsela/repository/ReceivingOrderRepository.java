package com.twsela.repository;

import com.twsela.domain.ReceivingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceivingOrderRepository extends JpaRepository<ReceivingOrder, Long> {
    List<ReceivingOrder> findByWarehouseIdOrderByCreatedAtDesc(Long warehouseId);
    List<ReceivingOrder> findByStatusOrderByCreatedAtDesc(String status);
    Optional<ReceivingOrder> findByReferenceNumber(String referenceNumber);
}
