package com.twsela.service;

import com.twsela.domain.Shipment;
import com.twsela.repository.DeliveryAttemptRepository;
import com.twsela.repository.ShipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Operations analytics — delivery performance, SLA, returns, throughput.
 */
@Service
@Transactional(readOnly = true)
public class OperationsAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(OperationsAnalyticsService.class);

    private final ShipmentRepository shipmentRepository;
    private final DeliveryAttemptRepository deliveryAttemptRepository;

    public OperationsAnalyticsService(ShipmentRepository shipmentRepository,
                                       DeliveryAttemptRepository deliveryAttemptRepository) {
        this.shipmentRepository = shipmentRepository;
        this.deliveryAttemptRepository = deliveryAttemptRepository;
    }

    /**
     * Percentage of shipments delivered on first attempt.
     */
    public double getFirstAttemptRate(Instant from, Instant to) {
        long delivered = shipmentRepository.countByStatusNameAndCreatedAtBetween("DELIVERED", from, to);
        if (delivered == 0) return 0.0;

        // First-attempt = delivered shipments with 0 failed attempts
        var deliveredShipments = shipmentRepository.findByStatusAndUpdatedAtBetween(
                findStatusByName("DELIVERED"), from, to);
        long firstAttempt = deliveredShipments.stream()
                .filter(s -> deliveryAttemptRepository.countByShipmentId(s.getId()) == 0)
                .count();

        return (double) firstAttempt / delivered * 100;
    }

    /**
     * Average delivery time in hours (from creation to delivery).
     */
    public double getAverageDeliveryTime(Instant from, Instant to) {
        var delivered = shipmentRepository.findByCreatedAtBetween(from, to).stream()
                .filter(s -> s.getStatus() != null && "DELIVERED".equals(s.getStatus().getName()))
                .filter(s -> s.getUpdatedAt() != null && s.getCreatedAt() != null)
                .collect(Collectors.toList());

        if (delivered.isEmpty()) return 0.0;

        double totalHours = delivered.stream()
                .mapToDouble(s -> Duration.between(s.getCreatedAt(), s.getUpdatedAt()).toMinutes() / 60.0)
                .sum();

        return Math.round(totalHours / delivered.size() * 100.0) / 100.0;
    }

    /**
     * SLA compliance: shipments delivered within expected timeframe.
     * Default SLA: 48 hours from creation.
     */
    public double getSlaComplianceRate(Instant from, Instant to) {
        var delivered = shipmentRepository.findByCreatedAtBetween(from, to).stream()
                .filter(s -> s.getStatus() != null && "DELIVERED".equals(s.getStatus().getName()))
                .collect(Collectors.toList());

        if (delivered.isEmpty()) return 0.0;

        long withinSla = delivered.stream()
                .filter(s -> Duration.between(s.getCreatedAt(), s.getUpdatedAt()).toHours() <= 48)
                .count();

        return (double) withinSla / delivered.size() * 100;
    }

    /**
     * Return rate: returned shipments / total shipments.
     */
    public double getReturnRate(Instant from, Instant to) {
        long total = shipmentRepository.countByCreatedAtBetween(from, to);
        if (total == 0) return 0.0;
        long returned = shipmentRepository.countByStatusNameAndCreatedAtBetween("RETURNED", from, to);
        return (double) returned / total * 100;
    }

    /**
     * Return reason breakdown.
     */
    public Map<String, Long> getReturnReasonBreakdown(Instant from, Instant to) {
        List<Object[]> reasons = deliveryAttemptRepository.countFailuresByReason(from, to);
        Map<String, Long> breakdown = new LinkedHashMap<>();
        for (Object[] row : reasons) {
            breakdown.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        return breakdown;
    }

    /**
     * Peak hours analysis — which hours of the day have most deliveries.
     */
    public List<Map<String, Object>> getPeakHours(Instant from, Instant to) {
        var shipments = shipmentRepository.findByCreatedAtBetween(from, to);
        Map<Integer, Long> hourCounts = new TreeMap<>();
        for (int h = 0; h < 24; h++) hourCounts.put(h, 0L);

        for (var s : shipments) {
            int hour = s.getCreatedAt().atZone(java.time.ZoneId.of("Africa/Cairo")).getHour();
            hourCounts.merge(hour, 1L, Long::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        hourCounts.forEach((hour, count) -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("hour", hour);
            entry.put("shipments", count);
            result.add(entry);
        });
        return result;
    }

    /**
     * Daily throughput (shipments per day).
     */
    public double getThroughput(Instant from, Instant to) {
        long total = shipmentRepository.countByCreatedAtBetween(from, to);
        long days = Math.max(1, Duration.between(from, to).toDays());
        return Math.round((double) total / days * 100.0) / 100.0;
    }

    /**
     * Bottleneck analysis — shows which status has the most stuck shipments.
     */
    public List<Map<String, Object>> getBottleneckAnalysis(Instant from, Instant to) {
        String[] statuses = {"CREATED", "PICKED_UP", "IN_TRANSIT", "OUT_FOR_DELIVERY"};
        List<Map<String, Object>> bottlenecks = new ArrayList<>();

        for (String status : statuses) {
            long count = shipmentRepository.countByStatusNameAndCreatedAtBetween(status, from, to);
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("status", status);
            entry.put("stuckCount", count);
            bottlenecks.add(entry);
        }

        bottlenecks.sort((a, b) -> Long.compare(
                (Long) b.get("stuckCount"), (Long) a.get("stuckCount")));
        return bottlenecks;
    }

    private com.twsela.domain.ShipmentStatus findStatusByName(String name) {
        // Lightweight lookup — callers should cache
        return shipmentRepository.findByStatusName(name).orElse(null);
    }
}
