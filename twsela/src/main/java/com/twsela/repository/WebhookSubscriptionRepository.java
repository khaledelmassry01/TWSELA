package com.twsela.repository;

import com.twsela.domain.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, Long> {

    List<WebhookSubscription> findByMerchantIdAndActiveTrue(Long merchantId);

    List<WebhookSubscription> findByMerchantId(Long merchantId);

    @Query("SELECT ws FROM WebhookSubscription ws WHERE ws.active = true AND ws.events LIKE %:eventType%")
    List<WebhookSubscription> findActiveByEventType(String eventType);
}
