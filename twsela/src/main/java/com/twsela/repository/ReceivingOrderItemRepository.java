package com.twsela.repository;

import com.twsela.domain.ReceivingOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceivingOrderItemRepository extends JpaRepository<ReceivingOrderItem, Long> {
    List<ReceivingOrderItem> findByReceivingOrderId(Long receivingOrderId);
}
