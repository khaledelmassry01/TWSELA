package com.twsela.service;

import com.twsela.domain.DomainEvent;
import com.twsela.domain.OutboxMessage;
import com.twsela.repository.DomainEventRepository;
import com.twsela.repository.OutboxMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * خدمة نشر أحداث النطاق — Transactional Outbox Pattern.
 */
@Service
@Transactional
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final DomainEventRepository domainEventRepository;
    private final OutboxMessageRepository outboxMessageRepository;

    public EventPublisher(DomainEventRepository domainEventRepository,
                           OutboxMessageRepository outboxMessageRepository) {
        this.domainEventRepository = domainEventRepository;
        this.outboxMessageRepository = outboxMessageRepository;
    }

    /**
     * نشر حدث نطاق — يكتب في الـ DB مع رسالة outbox في نفس المعاملة.
     */
    public DomainEvent publish(String eventType, String aggregateType, Long aggregateId,
                                String payload, String metadata) {
        // Check idempotency — prevent duplicate events
        Integer maxVersion = domainEventRepository.findMaxVersionByAggregate(aggregateType, aggregateId);
        int nextVersion = (maxVersion != null) ? maxVersion + 1 : 1;

        DomainEvent event = new DomainEvent();
        event.setEventType(eventType);
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setPayload(payload);
        event.setMetadata(metadata);
        event.setVersion(nextVersion);
        event.setStatus(DomainEvent.EventStatus.PENDING);

        DomainEvent saved = domainEventRepository.save(event);

        // Write outbox message in same transaction
        OutboxMessage outbox = new OutboxMessage();
        outbox.setAggregateType(aggregateType);
        outbox.setAggregateId(aggregateId);
        outbox.setEventType(eventType);
        outbox.setPayload(payload);
        outbox.setPublished(false);
        outboxMessageRepository.save(outbox);

        log.info("Event published: type={}, aggregate={}:{}, version={}",
                eventType, aggregateType, aggregateId, nextVersion);
        return saved;
    }

    /**
     * جلب أحداث حسب الـ aggregate.
     */
    @Transactional(readOnly = true)
    public List<DomainEvent> getEventsByAggregate(String aggregateType, Long aggregateId) {
        return domainEventRepository.findByAggregateTypeAndAggregateIdOrderByVersionAsc(aggregateType, aggregateId);
    }

    /**
     * جلب حدث حسب المعرف.
     */
    @Transactional(readOnly = true)
    public Optional<DomainEvent> getEventByEventId(String eventId) {
        return domainEventRepository.findByEventId(eventId);
    }

    /**
     * جلب أحداث حسب النوع.
     */
    @Transactional(readOnly = true)
    public List<DomainEvent> getEventsByType(String eventType) {
        return domainEventRepository.findByEventType(eventType);
    }

    /**
     * تحديث حالة الحدث.
     */
    public DomainEvent updateStatus(Long eventId, DomainEvent.EventStatus status) {
        DomainEvent event = domainEventRepository.findById(eventId)
                .orElseThrow(() -> new com.twsela.web.exception.ResourceNotFoundException("DomainEvent", "id", eventId));
        event.setStatus(status);
        if (status == DomainEvent.EventStatus.PUBLISHED) {
            event.setPublishedAt(Instant.now());
        } else if (status == DomainEvent.EventStatus.PROCESSED) {
            event.setProcessedAt(Instant.now());
        }
        return domainEventRepository.save(event);
    }
}
