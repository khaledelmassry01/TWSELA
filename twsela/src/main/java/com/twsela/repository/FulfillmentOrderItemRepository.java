package com.twsela.repository;

import com.twsela.domain.FulfillmentOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FulfillmentOrderItemRepository extends JpaRepository<FulfillmentOrderItem, Long> {
    List<FulfillmentOrderItem> findByFulfillmentOrderIdOrderByPickSequence(Long fulfillmentOrderId);
}
