package com.twsela.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTOs for analytics and advanced reporting.
 */
public class AnalyticsDTO {

    // ── Revenue by period ───────────────────────────────────────

    public static class RevenueReport {
        private BigDecimal totalRevenue;
        private BigDecimal totalDeliveryFees;
        private BigDecimal totalCodCollected;
        private long totalShipments;
        private long deliveredShipments;
        private double deliveryRate;
        private List<PeriodRevenue> breakdown;

        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
        public BigDecimal getTotalDeliveryFees() { return totalDeliveryFees; }
        public void setTotalDeliveryFees(BigDecimal totalDeliveryFees) { this.totalDeliveryFees = totalDeliveryFees; }
        public BigDecimal getTotalCodCollected() { return totalCodCollected; }
        public void setTotalCodCollected(BigDecimal totalCodCollected) { this.totalCodCollected = totalCodCollected; }
        public long getTotalShipments() { return totalShipments; }
        public void setTotalShipments(long totalShipments) { this.totalShipments = totalShipments; }
        public long getDeliveredShipments() { return deliveredShipments; }
        public void setDeliveredShipments(long deliveredShipments) { this.deliveredShipments = deliveredShipments; }
        public double getDeliveryRate() { return deliveryRate; }
        public void setDeliveryRate(double deliveryRate) { this.deliveryRate = deliveryRate; }
        public List<PeriodRevenue> getBreakdown() { return breakdown; }
        public void setBreakdown(List<PeriodRevenue> breakdown) { this.breakdown = breakdown; }
    }

    public static class PeriodRevenue {
        private String period;
        private BigDecimal revenue;
        private long shipmentCount;

        public PeriodRevenue() {}
        public PeriodRevenue(String period, BigDecimal revenue, long shipmentCount) {
            this.period = period;
            this.revenue = revenue;
            this.shipmentCount = shipmentCount;
        }

        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
        public long getShipmentCount() { return shipmentCount; }
        public void setShipmentCount(long shipmentCount) { this.shipmentCount = shipmentCount; }
    }

    // ── Status distribution ─────────────────────────────────────

    public static class StatusDistribution {
        private String status;
        private long count;
        private double percentage;

        public StatusDistribution() {}
        public StatusDistribution(String status, long count, double percentage) {
            this.status = status;
            this.count = count;
            this.percentage = percentage;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }

    // ── Courier performance ─────────────────────────────────────

    public static class CourierPerformance {
        private Long courierId;
        private String courierName;
        private long totalDeliveries;
        private long failedDeliveries;
        private double successRate;
        private BigDecimal totalEarnings;

        public Long getCourierId() { return courierId; }
        public void setCourierId(Long courierId) { this.courierId = courierId; }
        public String getCourierName() { return courierName; }
        public void setCourierName(String courierName) { this.courierName = courierName; }
        public long getTotalDeliveries() { return totalDeliveries; }
        public void setTotalDeliveries(long totalDeliveries) { this.totalDeliveries = totalDeliveries; }
        public long getFailedDeliveries() { return failedDeliveries; }
        public void setFailedDeliveries(long failedDeliveries) { this.failedDeliveries = failedDeliveries; }
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        public BigDecimal getTotalEarnings() { return totalEarnings; }
        public void setTotalEarnings(BigDecimal totalEarnings) { this.totalEarnings = totalEarnings; }
    }

    // ── Top merchant ────────────────────────────────────────────

    public static class TopMerchant {
        private Long merchantId;
        private String merchantName;
        private long shipmentCount;
        private BigDecimal revenue;

        public Long getMerchantId() { return merchantId; }
        public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
        public String getMerchantName() { return merchantName; }
        public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
        public long getShipmentCount() { return shipmentCount; }
        public void setShipmentCount(long shipmentCount) { this.shipmentCount = shipmentCount; }
        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    }
}
