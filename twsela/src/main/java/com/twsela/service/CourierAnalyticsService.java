package com.twsela.service;

import com.twsela.domain.User;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

/**
 * Courier performance analytics — utilization, leaderboard, earnings.
 */
@Service
@Transactional(readOnly = true)
public class CourierAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(CourierAnalyticsService.class);

    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;

    public CourierAnalyticsService(ShipmentRepository shipmentRepository,
                                    UserRepository userRepository) {
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Average utilization rate across all couriers.
     * Utilization = deliveries done / capacity (assumed 20 per day).
     */
    public double getUtilizationRate(Instant from, Instant to) {
        List<User> couriers = userRepository.findByRoleName("COURIER");
        if (couriers.isEmpty()) return 0.0;

        long totalDelivered = 0;
        for (User courier : couriers) {
            totalDelivered += shipmentRepository.countByCourierIdAndStatusNameAndCreatedAtBetween(
                    courier.getId(), "DELIVERED", from, to);
        }

        long days = Math.max(1, java.time.Duration.between(from, to).toDays());
        double capacity = couriers.size() * 20.0 * days; // 20 shipments/day capacity
        return Math.round(totalDelivered / capacity * 100 * 100.0) / 100.0;
    }

    /**
     * Performance distribution: top, average, bottom performers.
     */
    public Map<String, Object> getPerformanceDistribution(Instant from, Instant to) {
        List<User> couriers = userRepository.findByRoleName("COURIER");
        List<Long> deliveryCounts = new ArrayList<>();

        for (User courier : couriers) {
            long count = shipmentRepository.countByCourierIdAndStatusNameAndCreatedAtBetween(
                    courier.getId(), "DELIVERED", from, to);
            deliveryCounts.add(count);
        }

        deliveryCounts.sort(Collections.reverseOrder());

        Map<String, Object> distribution = new LinkedHashMap<>();
        distribution.put("totalCouriers", couriers.size());
        distribution.put("avgDeliveries", deliveryCounts.stream().mapToLong(Long::longValue).average().orElse(0));
        distribution.put("topPerformerDeliveries", deliveryCounts.isEmpty() ? 0 : deliveryCounts.get(0));
        distribution.put("bottomPerformerDeliveries", deliveryCounts.isEmpty() ? 0 : deliveryCounts.get(deliveryCounts.size() - 1));
        return distribution;
    }

    /**
     * Courier leaderboard — top N couriers by delivery count.
     */
    public List<Map<String, Object>> getLeaderboard(Instant from, Instant to, int top) {
        List<User> couriers = userRepository.findByRoleName("COURIER");
        List<Map<String, Object>> leaderboard = new ArrayList<>();

        for (User courier : couriers) {
            long delivered = shipmentRepository.countByCourierIdAndStatusNameAndCreatedAtBetween(
                    courier.getId(), "DELIVERED", from, to);
            long total = shipmentRepository.countByCourierIdAndCreatedAtBetween(
                    courier.getId(), from, to);
            double successRate = total > 0 ? (double) delivered / total * 100 : 0;

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("courierId", courier.getId());
            entry.put("courierName", courier.getName());
            entry.put("delivered", delivered);
            entry.put("total", total);
            entry.put("successRate", Math.round(successRate * 100.0) / 100.0);
            leaderboard.add(entry);
        }

        leaderboard.sort((a, b) -> Long.compare((Long) b.get("delivered"), (Long) a.get("delivered")));
        return leaderboard.subList(0, Math.min(top, leaderboard.size()));
    }

    /**
     * Earnings distribution across couriers.
     */
    public Map<String, Object> getEarningsDistribution(Instant from, Instant to) {
        List<User> couriers = userRepository.findByRoleName("COURIER");
        List<BigDecimal> earnings = new ArrayList<>();

        for (User courier : couriers) {
            BigDecimal fee = shipmentRepository.sumDeliveryFeeByMerchantIdAndStatusNameAndCreatedAtBetween(
                    courier.getId(), "DELIVERED", from, to);
            earnings.add(fee != null ? fee : BigDecimal.ZERO);
        }

        BigDecimal total = earnings.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avg = couriers.isEmpty() ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(couriers.size()), 2, RoundingMode.HALF_UP);

        Map<String, Object> dist = new LinkedHashMap<>();
        dist.put("totalEarnings", total);
        dist.put("averageEarning", avg);
        dist.put("courierCount", couriers.size());
        return dist;
    }

    /**
     * Average shipments per day per courier.
     */
    public double getShipmentsPerDay(Instant from, Instant to) {
        List<User> couriers = userRepository.findByRoleName("COURIER");
        if (couriers.isEmpty()) return 0.0;

        long totalShipments = 0;
        for (User c : couriers) {
            totalShipments += shipmentRepository.countByCourierIdAndCreatedAtBetween(c.getId(), from, to);
        }

        long days = Math.max(1, java.time.Duration.between(from, to).toDays());
        return Math.round((double) totalShipments / couriers.size() / days * 100.0) / 100.0;
    }
}
