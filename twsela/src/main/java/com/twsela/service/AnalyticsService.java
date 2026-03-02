package com.twsela.service;

import com.twsela.domain.User;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.ShipmentStatusRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.dto.AnalyticsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides advanced analytics: revenue trends, status distribution,
 * courier performance ranking, top merchants.
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final FinancialService financialService;

    public AnalyticsService(ShipmentRepository shipmentRepository,
                            UserRepository userRepository,
                            FinancialService financialService) {
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
        this.financialService = financialService;
    }

    // ── Revenue by period (monthly breakdown) ───────────────────

    public AnalyticsDTO.RevenueReport getRevenueByPeriod(LocalDate startDate, LocalDate endDate) {
        Instant start = toInstant(startDate);
        Instant end = toInstantEnd(endDate);

        AnalyticsDTO.RevenueReport report = new AnalyticsDTO.RevenueReport();

        long total = shipmentRepository.countByCreatedAtBetweenInstant(start, end);
        long delivered = shipmentRepository.countByStatusNameAndCreatedAtBetween("DELIVERED", start, end);
        BigDecimal revenue = shipmentRepository.sumDeliveryFeeByStatusNameAndCreatedAtBetween("DELIVERED", start, end);

        report.setTotalShipments(total);
        report.setDeliveredShipments(delivered);
        report.setTotalDeliveryFees(revenue != null ? revenue : BigDecimal.ZERO);
        report.setTotalRevenue(report.getTotalDeliveryFees());
        report.setTotalCodCollected(BigDecimal.ZERO); // COD sum not yet in repo
        report.setDeliveryRate(total > 0 ? (double) delivered / total * 100 : 0);

        // Monthly breakdown
        List<AnalyticsDTO.PeriodRevenue> breakdown = new ArrayList<>();
        LocalDate current = startDate.withDayOfMonth(1);
        while (!current.isAfter(endDate)) {
            LocalDate monthEnd = current.plusMonths(1).minusDays(1);
            if (monthEnd.isAfter(endDate)) monthEnd = endDate;

            Instant mStart = toInstant(current);
            Instant mEnd = toInstantEnd(monthEnd);
            BigDecimal monthRevenue = shipmentRepository.sumDeliveryFeeByStatusNameAndCreatedAtBetween("DELIVERED", mStart, mEnd);
            long monthCount = shipmentRepository.countByStatusNameAndCreatedAtBetween("DELIVERED", mStart, mEnd);

            breakdown.add(new AnalyticsDTO.PeriodRevenue(
                    current.format(MONTH_FMT),
                    monthRevenue != null ? monthRevenue : BigDecimal.ZERO,
                    monthCount));

            current = current.plusMonths(1);
        }
        report.setBreakdown(breakdown);
        return report;
    }

    // ── Status distribution ─────────────────────────────────────

    public List<AnalyticsDTO.StatusDistribution> getStatusDistribution(LocalDate startDate, LocalDate endDate) {
        Instant start = toInstant(startDate);
        Instant end = toInstantEnd(endDate);

        long total = shipmentRepository.countByCreatedAtBetweenInstant(start, end);
        if (total == 0) return List.of();

        String[] statuses = {"PENDING", "CREATED", "PICKED_UP", "IN_TRANSIT", "DELIVERED",
                "FAILED_DELIVERY", "RETURNED", "CANCELLED"};

        List<AnalyticsDTO.StatusDistribution> result = new ArrayList<>();
        for (String status : statuses) {
            long count = shipmentRepository.countByStatusNameAndCreatedAtBetween(status, start, end);
            if (count > 0) {
                double pct = (double) count / total * 100;
                result.add(new AnalyticsDTO.StatusDistribution(status, count,
                        BigDecimal.valueOf(pct).setScale(1, RoundingMode.HALF_UP).doubleValue()));
            }
        }
        return result;
    }

    // ── Courier performance ranking ─────────────────────────────

    public List<AnalyticsDTO.CourierPerformance> getCourierPerformanceRanking(
            LocalDate startDate, LocalDate endDate, int limit) {
        Instant start = toInstant(startDate);
        Instant end = toInstantEnd(endDate);

        List<User> couriers = userRepository.findByRoleName("COURIER");

        return couriers.stream().map(courier -> {
            AnalyticsDTO.CourierPerformance perf = new AnalyticsDTO.CourierPerformance();
            perf.setCourierId(courier.getId());
            perf.setCourierName(courier.getName());

            long delivered = shipmentRepository.countByCourierIdAndStatusNameAndCreatedAtBetween(
                    courier.getId(), "DELIVERED", start, end);
            long failed = shipmentRepository.countByCourierIdAndStatusNameAndCreatedAtBetween(
                    courier.getId(), "FAILED_DELIVERY", start, end);
            long totalDeliveries = delivered + failed;

            perf.setTotalDeliveries(delivered);
            perf.setFailedDeliveries(failed);
            perf.setSuccessRate(totalDeliveries > 0 ? (double) delivered / totalDeliveries * 100 : 0);

            BigDecimal earnings = financialService.calculateCourierEarnings(courier.getId(), startDate, endDate);
            perf.setTotalEarnings(earnings != null ? earnings : BigDecimal.ZERO);

            return perf;
        })
        .sorted(Comparator.comparingLong(AnalyticsDTO.CourierPerformance::getTotalDeliveries).reversed())
        .limit(limit)
        .collect(Collectors.toList());
    }

    // ── Top merchants ───────────────────────────────────────────

    public List<AnalyticsDTO.TopMerchant> getTopMerchants(LocalDate startDate, LocalDate endDate, int limit) {
        Instant start = toInstant(startDate);
        Instant end = toInstantEnd(endDate);

        List<User> merchants = userRepository.findByRoleName("MERCHANT");

        return merchants.stream().map(merchant -> {
            AnalyticsDTO.TopMerchant top = new AnalyticsDTO.TopMerchant();
            top.setMerchantId(merchant.getId());
            top.setMerchantName(merchant.getName());
            top.setShipmentCount(shipmentRepository.countByMerchantIdAndCreatedAtBetween(
                    merchant.getId(), start, end));
            BigDecimal rev = shipmentRepository.sumDeliveryFeeByMerchantIdAndStatusNameAndCreatedAtBetween(
                    merchant.getId(), "DELIVERED", start, end);
            top.setRevenue(rev != null ? rev : BigDecimal.ZERO);
            return top;
        })
        .sorted(Comparator.comparingLong(AnalyticsDTO.TopMerchant::getShipmentCount).reversed())
        .limit(limit)
        .collect(Collectors.toList());
    }

    // ── Helpers ─────────────────────────────────────────────────

    private Instant toInstant(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private Instant toInstantEnd(LocalDate date) {
        return date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
    }
}
