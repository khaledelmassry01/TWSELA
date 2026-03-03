package com.twsela.service;

import com.twsela.domain.DeadLetterEvent;
import com.twsela.domain.DomainEvent;
import com.twsela.domain.User;
import com.twsela.repository.DeadLetterEventRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * خدمة إدارة الأحداث الفاشلة (Dead Letter Queue).
 */
@Service
@Transactional
public class DeadLetterService {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterService.class);

    private final DeadLetterEventRepository deadLetterEventRepository;
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    public DeadLetterService(DeadLetterEventRepository deadLetterEventRepository,
                              UserRepository userRepository,
                              EventPublisher eventPublisher) {
        this.deadLetterEventRepository = deadLetterEventRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * نقل حدث فاشل إلى Dead Letter Queue.
     */
    public DeadLetterEvent moveToDeadLetter(DomainEvent event, String failureReason) {
        DeadLetterEvent dle = new DeadLetterEvent();
        dle.setOriginalEvent(event);
        dle.setFailureReason(failureReason);
        dle.setFailureCount(1);
        dle.setLastAttemptAt(Instant.now());
        dle.setNextRetryAt(Instant.now().plus(5, ChronoUnit.MINUTES));
        dle.setResolved(false);

        DeadLetterEvent saved = deadLetterEventRepository.save(dle);
        log.warn("Event {} moved to dead letter queue: {}", event.getEventId(), failureReason);
        return saved;
    }

    /**
     * إعادة محاولة حدث فاشل.
     */
    public DeadLetterEvent retry(Long deadLetterId) {
        DeadLetterEvent dle = deadLetterEventRepository.findById(deadLetterId)
                .orElseThrow(() -> new ResourceNotFoundException("DeadLetterEvent", "id", deadLetterId));

        dle.setFailureCount(dle.getFailureCount() + 1);
        dle.setLastAttemptAt(Instant.now());
        dle.setNextRetryAt(Instant.now().plus(10, ChronoUnit.MINUTES));

        // Re-publish the original event
        DomainEvent original = dle.getOriginalEvent();
        eventPublisher.updateStatus(original.getId(), DomainEvent.EventStatus.PENDING);

        log.info("Dead letter event {} retried — attempt #{}", deadLetterId, dle.getFailureCount());
        return deadLetterEventRepository.save(dle);
    }

    /**
     * تحديد حدث كمحلول.
     */
    public DeadLetterEvent resolve(Long deadLetterId, Long resolvedByUserId) {
        DeadLetterEvent dle = deadLetterEventRepository.findById(deadLetterId)
                .orElseThrow(() -> new ResourceNotFoundException("DeadLetterEvent", "id", deadLetterId));

        User resolver = userRepository.findById(resolvedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", resolvedByUserId));

        dle.setResolved(true);
        dle.setResolvedBy(resolver);
        dle.setResolvedAt(Instant.now());

        log.info("Dead letter event {} resolved by user {}", deadLetterId, resolvedByUserId);
        return deadLetterEventRepository.save(dle);
    }

    /**
     * الأحداث الفاشلة غير المحلولة.
     */
    @Transactional(readOnly = true)
    public List<DeadLetterEvent> getUnresolved() {
        return deadLetterEventRepository.findUnresolved();
    }

    /**
     * إحصائيات Dead Letter Queue.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("unresolvedCount", deadLetterEventRepository.countByResolved(false));
        stats.put("resolvedCount", deadLetterEventRepository.countByResolved(true));
        stats.put("totalCount", deadLetterEventRepository.count());
        return stats;
    }
}
