package com.twsela.service;

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
 * Revenue analytics — aggregates financial data across shipments, merchants, and zones.
 */
@Service
@Transactional(readOnly = true)
public class RevenueAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(RevenueAnalyticsService.class);

    private final ShipmentRepository shipmentRepository;

    public RevenueAnalyticsService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    /**
     * Total revenue (delivery fees from delivered shipments) for a date range.
     */
    public BigDecimal getTotalRevenue(Instant from, Instant to) {
        return shipmentRepository.sumDeliveryFeeByStatusNameAndCreatedAtBetween("DELIVERED", from, to);
    }

    /**
     * Revenue breakdown by zone.
     */
    public List<Map<String, Object>> getRevenueByZone(Instant from, Instant to) {
        // Aggregate shipments by zone
        var shipments = shipmentRepository.findByCreatedAtBetween(from, to);
        Map<String, BigDecimal> byZone = new LinkedHashMap<>();
        Map<String, Long> countByZone = new LinkedHashMap<>();

        for (var s : shipments) {
            String zoneName = s.getZone() != null ? s.getZone().getName() : "بدون منطقة";
            byZone.merge(zoneName, s.getDeliveryFee() != null ? s.getDeliveryFee() : BigDecimal.ZERO, BigDecimal::add);
            countByZone.merge(zoneName, 1L, Long::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        byZone.forEach((zone, revenue) -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("zone", zone);
            entry.put("revenue", revenue);
            entry.put("shipments", countByZone.getOrDefault(zone, 0L));
            result.add(entry);
        });

        result.sort((a, b) -> ((BigDecimal) b.get("revenue")).compareTo((BigDecimal) a.get("revenue")));
        return result;
    }

    /**
     * Revenue by top N merchants.
     */
    public List<Map<String, Object>> getRevenueByMerchant(Instant from, Instant to, int top) {
        var shipments = shipmentRepository.findByCreatedAtBetween(from, to);
        Map<Long, BigDecimal> byMerchant = new LinkedHashMap<>();
        Map<Long, String> merchantNames = new LinkedHashMap<>();
        Map<Long, Long> countByMerchant = new LinkedHashMap<>();

        for (var s : shipments) {
            if (s.getMerchant() != null) {
                Long merchantId = s.getMerchant().getId();
                byMerchant.merge(merchantId, s.getDeliveryFee() != null ? s.getDeliveryFee() : BigDecimal.ZERO, BigDecimal::add);
                merchantNames.putIfAbsent(merchantId, s.getMerchant().getName());
                countByMerchant.merge(merchantId, 1L, Long::sum);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        byMerchant.forEach((id, revenue) -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("merchantId", id);
            entry.put("merchantName", merchantNames.get(id));
            entry.put("revenue", revenue);
            entry.put("shipments", countByMerchant.getOrDefault(id, 0L));
            result.add(entry);
        });

        result.sort((a, b) -> ((BigDecimal) b.get("revenue")).compareTo((BigDecimal) a.get("revenue")));
        return result.subList(0, Math.min(top, result.size()));
    }

    /**
     * Profit margin: (revenue - estimated_cost) / revenue.
     * Estimated cost = 60% of revenue (configurable).
     */
    public double getProfitMargin(Instant from, Instant to) {
        BigDecimal revenue = getTotalRevenue(from, to);
        if (revenue.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        BigDecimal estimatedCost = revenue.multiply(BigDecimal.valueOf(0.60));
        BigDecimal profit = revenue.subtract(estimatedCost);
        return profit.divide(revenue, 4, RoundingMode.HALF_UP).doubleValue() * 100;
    }

    /**
     * Cost per delivery.
     */
    public BigDecimal getCostPerDelivery(Instant from, Instant to) {
        BigDecimal revenue = getTotalRevenue(from, to);
        long delivered = shipmentRepository.countByStatusNameAndCreatedAtBetween("DELIVERED", from, to);
        if (delivered == 0) return BigDecimal.ZERO;
        BigDecimal estimatedCost = revenue.multiply(BigDecimal.valueOf(0.60));
        return estimatedCost.divide(BigDecimal.valueOf(delivered), 2, RoundingMode.HALF_UP);
    }

    /**
     * Average shipment value (avg delivery fee).
     */
    public BigDecimal getAverageShipmentValue(Instant from, Instant to) {
        BigDecimal revenue = getTotalRevenue(from, to);
        long total = shipmentRepository.countByStatusNameAndCreatedAtBetween("DELIVERED", from, to);
        if (total == 0) return BigDecimal.ZERO;
        return revenue.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    /**
     * Month-over-month revenue comparison.
     */
    public Map<String, Object> getRevenueComparison(Instant period1From, Instant period1To,
                                                      Instant period2From, Instant period2To) {
        BigDecimal rev1 = getTotalRevenue(period1From, period1To);
        BigDecimal rev2 = getTotalRevenue(period2From, period2To);
        double changePercent = 0;
        if (rev1.compareTo(BigDecimal.ZERO) > 0) {
            changePercent = rev2.subtract(rev1)
                    .divide(rev1, 4, RoundingMode.HALF_UP)
                    .doubleValue() * 100;
        }

        Map<String, Object> comparison = new LinkedHashMap<>();
        comparison.put("period1Revenue", rev1);
        comparison.put("period2Revenue", rev2);
        comparison.put("changePercent", Math.round(changePercent * 100.0) / 100.0);
        comparison.put("trend", changePercent > 0 ? "UP" : changePercent < 0 ? "DOWN" : "STABLE");
        return comparison;
    }
}
