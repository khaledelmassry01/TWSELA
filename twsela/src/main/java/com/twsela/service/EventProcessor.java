package com.twsela.service;

import com.twsela.domain.DomainEvent;
import com.twsela.domain.EventSubscription;
import com.twsela.repository.DomainEventRepository;
import com.twsela.repository.EventSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * خدمة معالجة الأحداث — توجيه لكل مشترك مع إعادة المحاولة.
 */
@Service
@Transactional
public class EventProcessor {

    private static final Logger log = LoggerFactory.getLogger(EventProcessor.class);
    private static final int MAX_RETRIES = 3;

    private final DomainEventRepository domainEventRepository;
    private final EventSubscriptionRepository eventSubscriptionRepository;
    private final DeadLetterService deadLetterService;

    public EventProcessor(DomainEventRepository domainEventRepository,
                           EventSubscriptionRepository eventSubscriptionRepository,
                           DeadLetterService deadLetterService) {
        this.domainEventRepository = domainEventRepository;
        this.eventSubscriptionRepository = eventSubscriptionRepository;
        this.deadLetterService = deadLetterService;
    }

    /**
     * معالجة حدث — توجيه لجميع المشتركين النشطين.
     */
    public void processEvent(DomainEvent event) {
        List<EventSubscription> subscribers = eventSubscriptionRepository
                .findByEventTypeAndActiveTrue(event.getEventType());

        if (subscribers.isEmpty()) {
            log.debug("No active subscribers for event type: {}", event.getEventType());
            event.setStatus(DomainEvent.EventStatus.PROCESSED);
            event.setProcessedAt(Instant.now());
            domainEventRepository.save(event);
            return;
        }

        boolean allSucceeded = true;
        for (EventSubscription subscription : subscribers) {
            try {
                routeToSubscriber(event, subscription);
                subscription.setLastProcessedAt(Instant.now());
                eventSubscriptionRepository.save(subscription);
            } catch (Exception e) {
                allSucceeded = false;
                subscription.setFailureCount(subscription.getFailureCount() + 1);
                eventSubscriptionRepository.save(subscription);
                log.error("Failed to process event {} for subscriber {}: {}",
                        event.getEventId(), subscription.getSubscriberName(), e.getMessage());
            }
        }

        if (allSucceeded) {
            event.setStatus(DomainEvent.EventStatus.PROCESSED);
            event.setProcessedAt(Instant.now());
        } else {
            event.setStatus(DomainEvent.EventStatus.FAILED);
            deadLetterService.moveToDeadLetter(event, "One or more subscribers failed");
        }
        domainEventRepository.save(event);
    }

    /**
     * معالجة الأحداث المعلقة.
     */
    public int processPendingEvents() {
        List<DomainEvent> pending = domainEventRepository
                .findByStatusOrderByCreatedAtAsc(DomainEvent.EventStatus.PUBLISHED);
        int processed = 0;
        for (DomainEvent event : pending) {
            try {
                processEvent(event);
                processed++;
            } catch (Exception e) {
                log.error("Error processing event {}: {}", event.getEventId(), e.getMessage());
            }
        }
        log.info("Processed {} pending events", processed);
        return processed;
    }

    /**
     * توجيه الحدث للمشترك (محاكاة — يمكن أن يكون reflection/bean lookup في الإنتاج).
     */
    void routeToSubscriber(DomainEvent event, EventSubscription subscription) {
        log.info("Routing event {} to subscriber: {} (handler: {})",
                event.getEventId(), subscription.getSubscriberName(), subscription.getHandlerClass());
        // In production: use ApplicationContext.getBean() or reflection to invoke handlerClass
    }
}
