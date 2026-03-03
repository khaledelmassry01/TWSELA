package com.twsela.service;

import com.twsela.domain.User;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Merchant analytics — retention, growth, churn prediction, top merchants.
 */
@Service
@Transactional(readOnly = true)
public class MerchantAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(MerchantAnalyticsService.class);

    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;

    public MerchantAnalyticsService(ShipmentRepository shipmentRepository,
                                     UserRepository userRepository) {
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retention rate: merchants who shipped in both current and previous period.
     */
    public double getRetentionRate(Instant from, Instant to) {
        Duration period = Duration.between(from, to);
        Instant prevFrom = from.minus(period);
        Instant prevTo = from;

        List<User> merchants = userRepository.findByRoleName("MERCHANT");
        if (merchants.isEmpty()) return 0.0;

        long activePrev = 0, activeBoth = 0;
        for (User merchant : merchants) {
            long prev = shipmentRepository.countByMerchantIdAndCreatedAtBetween(merchant.getId(), prevFrom, prevTo);
            long curr = shipmentRepository.countByMerchantIdAndCreatedAtBetween(merchant.getId(), from, to);
            if (prev > 0) activePrev++;
            if (prev > 0 && curr > 0) activeBoth++;
        }

        return activePrev > 0 ? (double) activeBoth / activePrev * 100 : 0.0;
    }

    /**
     * Top merchants by volume or revenue.
     */
    public List<Map<String, Object>> getTopMerchants(Instant from, Instant to, String metric, int top) {
        List<User> merchants = userRepository.findByRoleName("MERCHANT");
        List<Map<String, Object>> result = new ArrayList<>();

        for (User merchant : merchants) {
            long count = shipmentRepository.countByMerchantIdAndCreatedAtBetween(merchant.getId(), from, to);
            if (count == 0) continue;

            var revenue = shipmentRepository.sumDeliveryFeeByMerchantIdAndStatusNameAndCreatedAtBetween(
                    merchant.getId(), "DELIVERED", from, to);

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("merchantId", merchant.getId());
            entry.put("merchantName", merchant.getName());
            entry.put("shipments", count);
            entry.put("revenue", revenue);
            result.add(entry);
        }

        if ("revenue".equals(metric)) {
            result.sort((a, b) -> ((java.math.BigDecimal) b.get("revenue"))
                    .compareTo((java.math.BigDecimal) a.get("revenue")));
        } else {
            result.sort((a, b) -> Long.compare((Long) b.get("shipments"), (Long) a.get("shipments")));
        }

        return result.subList(0, Math.min(top, result.size()));
    }

    /**
     * Growth rate: current period shipments vs previous period.
     */
    public double getGrowthRate(Instant from, Instant to) {
        Duration period = Duration.between(from, to);
        Instant prevFrom = from.minus(period);

        long prev = shipmentRepository.countByCreatedAtBetween(prevFrom, from);
        long curr = shipmentRepository.countByCreatedAtBetween(from, to);

        if (prev == 0) return curr > 0 ? 100.0 : 0.0;
        return Math.round((double) (curr - prev) / prev * 100 * 100.0) / 100.0;
    }

    /**
     * Churn risk: merchants with significantly declining shipment volume.
     */
    public List<Map<String, Object>> getChurnRisk(Instant from, Instant to) {
        Duration period = Duration.between(from, to);
        Instant prevFrom = from.minus(period);

        List<User> merchants = userRepository.findByRoleName("MERCHANT");
        List<Map<String, Object>> atRisk = new ArrayList<>();

        for (User merchant : merchants) {
            long prev = shipmentRepository.countByMerchantIdAndCreatedAtBetween(merchant.getId(), prevFrom, from);
            long curr = shipmentRepository.countByMerchantIdAndCreatedAtBetween(merchant.getId(), from, to);

            if (prev > 0 && curr == 0) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("merchantId", merchant.getId());
                entry.put("merchantName", merchant.getName());
                entry.put("previousShipments", prev);
                entry.put("currentShipments", curr);
                entry.put("riskLevel", "HIGH");
                atRisk.add(entry);
            } else if (prev > 5 && curr < prev * 0.5) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("merchantId", merchant.getId());
                entry.put("merchantName", merchant.getName());
                entry.put("previousShipments", prev);
                entry.put("currentShipments", curr);
                entry.put("riskLevel", "MEDIUM");
                atRisk.add(entry);
            }
        }

        return atRisk;
    }

    /**
     * Active merchant count.
     */
    public long getActiveMerchantCount(Instant from, Instant to) {
        List<User> merchants = userRepository.findByRoleName("MERCHANT");
        return merchants.stream()
                .filter(m -> shipmentRepository.countByMerchantIdAndCreatedAtBetween(m.getId(), from, to) > 0)
                .count();
    }
}
