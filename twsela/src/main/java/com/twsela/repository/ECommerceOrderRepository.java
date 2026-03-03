package com.twsela.repository;

import com.twsela.domain.ECommerceConnection.ECommercePlatform;
import com.twsela.domain.ECommerceOrder;
import com.twsela.domain.ECommerceOrder.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ECommerceOrderRepository extends JpaRepository<ECommerceOrder, Long> {

    Optional<ECommerceOrder> findByExternalOrderIdAndPlatform(String externalOrderId, ECommercePlatform platform);

    List<ECommerceOrder> findByConnectionId(Long connectionId);

    List<ECommerceOrder> findByStatus(OrderStatus status);

    long countByConnectionIdAndStatus(Long connectionId, OrderStatus status);
}
