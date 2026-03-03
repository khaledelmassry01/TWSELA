package com.twsela.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Business Intelligence dashboard — orchestrates all analytics services
 * into cohesive summaries and reports.
 */
@Service
@Transactional(readOnly = true)
public class BIDashboardService {

    private static final Logger log = LoggerFactory.getLogger(BIDashboardService.class);

    private final RevenueAnalyticsService revenueService;
    private final OperationsAnalyticsService operationsService;
    private final CourierAnalyticsService courierService;
    private final MerchantAnalyticsService merchantService;

    public BIDashboardService(RevenueAnalyticsService revenueService,
                               OperationsAnalyticsService operationsService,
                               CourierAnalyticsService courierService,
                               MerchantAnalyticsService merchantService) {
        this.revenueService = revenueService;
        this.operationsService = operationsService;
        this.courierService = courierService;
        this.merchantService = merchantService;
    }

    /**
     * Executive summary — high-level KPIs for the BI dashboard header.
     */
    public Map<String, Object> getExecutiveSummary(Instant from, Instant to) {
        Map<String, Object> summary = new LinkedHashMap<>();

        summary.put("totalRevenue", revenueService.getTotalRevenue(from, to));
        summary.put("profitMargin", revenueService.getProfitMargin(from, to));
        summary.put("avgShipmentValue", revenueService.getAverageShipmentValue(from, to));
        summary.put("firstAttemptRate", operationsService.getFirstAttemptRate(from, to));
        summary.put("avgDeliveryTimeHours", operationsService.getAverageDeliveryTime(from, to));
        summary.put("slaCompliance", operationsService.getSlaComplianceRate(from, to));
        summary.put("returnRate", operationsService.getReturnRate(from, to));
        summary.put("throughputPerDay", operationsService.getThroughput(from, to));
        summary.put("courierUtilization", courierService.getUtilizationRate(from, to));
        summary.put("activeMerchants", merchantService.getActiveMerchantCount(from, to));
        summary.put("merchantGrowthRate", merchantService.getGrowthRate(from, to));

        return summary;
    }

    /**
     * Full revenue analytics report.
     */
    public Map<String, Object> getRevenueAnalytics(Instant from, Instant to, int topMerchants) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalRevenue", revenueService.getTotalRevenue(from, to));
        report.put("profitMargin", revenueService.getProfitMargin(from, to));
        report.put("costPerDelivery", revenueService.getCostPerDelivery(from, to));
        report.put("avgShipmentValue", revenueService.getAverageShipmentValue(from, to));
        report.put("byZone", revenueService.getRevenueByZone(from, to));
        report.put("byMerchant", revenueService.getRevenueByMerchant(from, to, topMerchants));
        return report;
    }

    /**
     * Full operations analytics report.
     */
    public Map<String, Object> getOperationsAnalytics(Instant from, Instant to) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("firstAttemptRate", operationsService.getFirstAttemptRate(from, to));
        report.put("avgDeliveryTimeHours", operationsService.getAverageDeliveryTime(from, to));
        report.put("slaCompliance", operationsService.getSlaComplianceRate(from, to));
        report.put("returnRate", operationsService.getReturnRate(from, to));
        report.put("returnReasons", operationsService.getReturnReasonBreakdown(from, to));
        report.put("peakHours", operationsService.getPeakHours(from, to));
        report.put("throughputPerDay", operationsService.getThroughput(from, to));
        report.put("bottlenecks", operationsService.getBottleneckAnalysis(from, to));
        return report;
    }

    /**
     * Courier analytics report.
     */
    public Map<String, Object> getCourierAnalytics(Instant from, Instant to, int top) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("utilization", courierService.getUtilizationRate(from, to));
        report.put("shipmentsPerDay", courierService.getShipmentsPerDay(from, to));
        report.put("performanceDistribution", courierService.getPerformanceDistribution(from, to));
        report.put("leaderboard", courierService.getLeaderboard(from, to, top));
        report.put("earningsDistribution", courierService.getEarningsDistribution(from, to));
        return report;
    }

    /**
     * Merchant analytics report.
     */
    public Map<String, Object> getMerchantAnalytics(Instant from, Instant to, int top) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("retentionRate", merchantService.getRetentionRate(from, to));
        report.put("growthRate", merchantService.getGrowthRate(from, to));
        report.put("activeMerchants", merchantService.getActiveMerchantCount(from, to));
        report.put("topByVolume", merchantService.getTopMerchants(from, to, "volume", top));
        report.put("topByRevenue", merchantService.getTopMerchants(from, to, "revenue", top));
        report.put("churnRisk", merchantService.getChurnRisk(from, to));
        return report;
    }
}
