package com.twsela.repository;

import com.twsela.domain.WebhookEvent;
import com.twsela.domain.WebhookEvent.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    Page<WebhookEvent> findBySubscriptionIdOrderByCreatedAtDesc(Long subscriptionId, Pageable pageable);

    List<WebhookEvent> findByStatusAndAttemptsLessThan(DeliveryStatus status, int maxAttempts);

    long countBySubscriptionIdAndStatus(Long subscriptionId, DeliveryStatus status);
}
