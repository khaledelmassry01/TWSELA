package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.domain.DeliveryAttempt.AttemptStatus;
import com.twsela.domain.DeliveryAttempt.FailureReason;
import com.twsela.repository.DeliveryAttemptRepository;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for recording delivery attempts and managing retries.
 */
@Service
@Transactional
public class DeliveryAttemptService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryAttemptService.class);
    private static final int MAX_ATTEMPTS = 3;

    private final DeliveryAttemptRepository attemptRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;

    public DeliveryAttemptService(DeliveryAttemptRepository attemptRepository,
                                   ShipmentRepository shipmentRepository,
                                   UserRepository userRepository) {
        this.attemptRepository = attemptRepository;
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Record a failed delivery attempt.
     */
    public DeliveryAttempt recordFailedAttempt(Long shipmentId, FailureReason reason,
                                                Double latitude, Double longitude,
                                                String notes, Long courierId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId));
        User courier = userRepository.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", courierId));

        int currentCount = attemptRepository.countByShipmentId(shipmentId);
        if (currentCount >= MAX_ATTEMPTS) {
            throw new BusinessRuleException("تم تجاوز الحد الأقصى لمحاولات التسليم (" + MAX_ATTEMPTS + ")");
        }

        DeliveryAttempt attempt = new DeliveryAttempt();
        attempt.setShipment(shipment);
        attempt.setAttemptNumber(currentCount + 1);
        attempt.setStatus(AttemptStatus.FAILED);
        attempt.setFailureReason(reason);
        attempt.setLatitude(latitude);
        attempt.setLongitude(longitude);
        attempt.setNotes(notes);
        attempt.setAttemptedAt(Instant.now());
        attempt.setCourier(courier);

        // Auto-schedule next attempt if not at max
        if (currentCount + 1 < MAX_ATTEMPTS) {
            attempt.setNextAttemptDate(LocalDate.now().plusDays(1));
        }

        DeliveryAttempt saved = attemptRepository.save(attempt);
        log.info("Delivery attempt #{} recorded for shipment {} — reason: {}",
                saved.getAttemptNumber(), shipmentId, reason);

        // If this was the last attempt, trigger auto-return logic
        if (currentCount + 1 >= MAX_ATTEMPTS) {
            log.info("Max attempts reached for shipment {}. Ready for return.", shipmentId);
        }

        return saved;
    }

    /**
     * Get all delivery attempts for a shipment.
     */
    @Transactional(readOnly = true)
    public List<DeliveryAttempt> getAttempts(Long shipmentId) {
        return attemptRepository.findByShipmentIdOrderByAttemptNumberAsc(shipmentId);
    }

    /**
     * Get the number of attempts for a shipment.
     */
    @Transactional(readOnly = true)
    public int getAttemptCount(Long shipmentId) {
        return attemptRepository.countByShipmentId(shipmentId);
    }

    /**
     * Check if max attempts have been reached for a shipment.
     */
    @Transactional(readOnly = true)
    public boolean isMaxAttemptsReached(Long shipmentId) {
        return attemptRepository.countByShipmentId(shipmentId) >= MAX_ATTEMPTS;
    }

    /**
     * Schedule a retry for the next attempt.
     */
    public DeliveryAttempt scheduleRetry(Long attemptId, LocalDate nextDate) {
        DeliveryAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryAttempt", "id", attemptId));

        if (nextDate.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("تاريخ إعادة المحاولة يجب أن يكون في المستقبل");
        }

        attempt.setNextAttemptDate(nextDate);
        log.info("Retry scheduled for attempt {} on {}", attemptId, nextDate);
        return attemptRepository.save(attempt);
    }

    /**
     * Get failure report summary for a date range.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getFailureReport(Instant from, Instant to) {
        List<Object[]> rawData = attemptRepository.countFailuresByReason(from, to);

        Map<String, Long> failuresByReason = new LinkedHashMap<>();
        long totalFailures = 0;
        for (Object[] row : rawData) {
            String reason = row[0] != null ? row[0].toString() : "UNKNOWN";
            long count = ((Number) row[1]).longValue();
            failuresByReason.put(reason, count);
            totalFailures += count;
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalFailedAttempts", totalFailures);
        report.put("failuresByReason", failuresByReason);
        return report;
    }
}
