package com.twsela.repository;

import com.twsela.domain.EventSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventSubscriptionRepository extends JpaRepository<EventSubscription, Long> {

    List<EventSubscription> findByEventTypeAndActiveTrue(String eventType);

    Optional<EventSubscription> findBySubscriberName(String subscriberName);

    @Query("SELECT s FROM EventSubscription s WHERE s.active = true ORDER BY s.eventType")
    List<EventSubscription> findActiveSubscriptions();
}
