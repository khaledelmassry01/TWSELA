package com.twsela.service;

import com.twsela.domain.KPISnapshot;
import com.twsela.repository.KPISnapshotRepository;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Manages daily KPI snapshots — pre-computed metrics for fast dashboard loading.
 */
@Service
public class KPISnapshotService {

    private static final Logger log = LoggerFactory.getLogger(KPISnapshotService.class);
    private static final ZoneId CAIRO = ZoneId.of("Africa/Cairo");

    private final KPISnapshotRepository snapshotRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;

    public KPISnapshotService(KPISnapshotRepository snapshotRepository,
                               ShipmentRepository shipmentRepository,
                               UserRepository userRepository) {
        this.snapshotRepository = snapshotRepository;
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Capture daily KPI snapshot. Runs at midnight Cairo time.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Africa/Cairo")
    @Transactional
    public KPISnapshot captureSnapshot() {
        LocalDate yesterday = LocalDate.now(CAIRO).minusDays(1);
        return captureSnapshot(yesterday);
    }

    /**
     * Capture snapshot for a specific date.
     */
    @Transactional
    public KPISnapshot captureSnapshot(LocalDate date) {
        // Check if snapshot already exists
        Optional<KPISnapshot> existing = snapshotRepository.findBySnapshotDate(date);
        if (existing.isPresent()) {
            log.info("Snapshot already exists for {}, updating", date);
            snapshotRepository.delete(existing.get());
        }

        Instant dayStart = date.atStartOfDay(CAIRO).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(CAIRO).toInstant();

        KPISnapshot snapshot = new KPISnapshot(date);

        // Revenue
        BigDecimal revenue = shipmentRepository.sumDeliveryFeeByStatusNameAndCreatedAtBetween("DELIVERED", dayStart, dayEnd);
        snapshot.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);

        // Shipment counts
        long total = shipmentRepository.countByCreatedAtBetween(dayStart, dayEnd);
        long delivered = shipmentRepository.countByStatusNameAndCreatedAtBetween("DELIVERED", dayStart, dayEnd);
        long returned = shipmentRepository.countByStatusNameAndCreatedAtBetween("RETURNED", dayStart, dayEnd);
        snapshot.setTotalShipments((int) total);
        snapshot.setDeliveredShipments((int) delivered);
        snapshot.setReturnedShipments((int) returned);

        // First attempt rate (simplified: delivered / total for this snapshot)
        snapshot.setFirstAttemptRate(total > 0 ? (double) delivered / total * 100 : 0);

        // Average delivery time (simplified)
        snapshot.setAvgDeliveryHours(24.0); // Default placeholder

        // Active users
        var couriers = userRepository.findByRoleName("COURIER");
        long activeCouriers = couriers.stream()
                .filter(c -> shipmentRepository.countByCourierIdAndCreatedAtBetween(c.getId(), dayStart, dayEnd) > 0)
                .count();
        snapshot.setActiveCouriers((int) activeCouriers);

        var merchants = userRepository.findByRoleName("MERCHANT");
        long activeMerchants = merchants.stream()
                .filter(m -> shipmentRepository.countByMerchantIdAndCreatedAtBetween(m.getId(), dayStart, dayEnd) > 0)
                .count();
        snapshot.setActiveMerchants((int) activeMerchants);
        snapshot.setNewMerchants(0); // Would require tracking registration dates

        // SLA compliance (simplified)
        snapshot.setSlaComplianceRate(total > 0 ? (double) delivered / total * 100 : 0);

        snapshot = snapshotRepository.save(snapshot);
        log.info("KPI snapshot captured for {}: {} shipments, {} delivered, revenue={}",
                date, total, delivered, revenue);
        return snapshot;
    }

    /**
     * Get snapshot for a specific date.
     */
    @Transactional(readOnly = true)
    public Optional<KPISnapshot> getSnapshot(LocalDate date) {
        return snapshotRepository.findBySnapshotDate(date);
    }

    /**
     * Get snapshots for a date range.
     */
    @Transactional(readOnly = true)
    public List<KPISnapshot> getSnapshots(LocalDate from, LocalDate to) {
        return snapshotRepository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(from, to);
    }

    /**
     * Get trend data for a specific metric over a date range.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTrend(String metric, LocalDate from, LocalDate to) {
        List<KPISnapshot> snapshots = getSnapshots(from, to);
        List<Map<String, Object>> trend = new ArrayList<>();

        for (KPISnapshot s : snapshots) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", s.getSnapshotDate().toString());

            switch (metric) {
                case "revenue": point.put("value", s.getTotalRevenue()); break;
                case "shipments": point.put("value", s.getTotalShipments()); break;
                case "delivered": point.put("value", s.getDeliveredShipments()); break;
                case "returned": point.put("value", s.getReturnedShipments()); break;
                case "firstAttemptRate": point.put("value", s.getFirstAttemptRate()); break;
                case "slaCompliance": point.put("value", s.getSlaComplianceRate()); break;
                case "activeCouriers": point.put("value", s.getActiveCouriers()); break;
                case "activeMerchants": point.put("value", s.getActiveMerchants()); break;
                default: point.put("value", 0);
            }

            trend.add(point);
        }

        return trend;
    }
}
