package com.twsela.service;

import com.twsela.domain.DomainEvent;
import com.twsela.domain.OutboxMessage;
import com.twsela.repository.DomainEventRepository;
import com.twsela.repository.OutboxMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * خدمة Outbox Poller — نشر الأحداث غير المنشورة بشكل دوري.
 */
@Service
@Transactional
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    private final OutboxMessageRepository outboxMessageRepository;
    private final DomainEventRepository domainEventRepository;

    public OutboxPoller(OutboxMessageRepository outboxMessageRepository,
                         DomainEventRepository domainEventRepository) {
        this.outboxMessageRepository = outboxMessageRepository;
        this.domainEventRepository = domainEventRepository;
    }

    /**
     * نشر الأحداث غير المنشورة — يعمل كل 5 ثوان.
     */
    @Scheduled(fixedDelay = 5000)
    public int pollAndPublish() {
        List<OutboxMessage> unpublished = outboxMessageRepository.findByPublishedFalseOrderByCreatedAtAsc();
        if (unpublished.isEmpty()) {
            return 0;
        }

        int published = 0;
        for (OutboxMessage message : unpublished) {
            try {
                // Mark corresponding DomainEvent as PUBLISHED
                List<DomainEvent> events = domainEventRepository
                        .findByAggregateTypeAndAggregateIdOrderByVersionAsc(
                                message.getAggregateType(), message.getAggregateId());

                for (DomainEvent event : events) {
                    if (event.getStatus() == DomainEvent.EventStatus.PENDING
                            && event.getEventType().equals(message.getEventType())) {
                        event.setStatus(DomainEvent.EventStatus.PUBLISHED);
                        event.setPublishedAt(Instant.now());
                        domainEventRepository.save(event);
                    }
                }

                message.setPublished(true);
                message.setPublishedAt(Instant.now());
                outboxMessageRepository.save(message);
                published++;
            } catch (Exception e) {
                log.error("Failed to publish outbox message {}: {}", message.getId(), e.getMessage());
            }
        }

        if (published > 0) {
            log.info("Published {} outbox messages", published);
        }
        return published;
    }
}
