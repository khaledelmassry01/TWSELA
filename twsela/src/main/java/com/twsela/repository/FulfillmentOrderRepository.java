package com.twsela.repository;

import com.twsela.domain.FulfillmentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FulfillmentOrderRepository extends JpaRepository<FulfillmentOrder, Long> {
    List<FulfillmentOrder> findByWarehouseIdOrderByCreatedAtDesc(Long warehouseId);
    List<FulfillmentOrder> findByStatusOrderByCreatedAtDesc(String status);
    Optional<FulfillmentOrder> findByOrderNumber(String orderNumber);
    List<FulfillmentOrder> findByAssignedPickerIdAndStatus(Long pickerId, String status);
}
