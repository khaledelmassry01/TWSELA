package com.twsela.web.dto;

import java.math.BigDecimal;

public class ReportDTO {
    private long totalShipments;
    private long deliveredShipments;
    private BigDecimal totalRevenue;
    private BigDecimal totalEarnings;
    private boolean success = true;

    public ReportDTO() {}

    public long getTotalShipments() { return totalShipments; }
    public void setTotalShipments(long totalShipments) { this.totalShipments = totalShipments; }
    public long getDeliveredShipments() { return deliveredShipments; }
    public void setDeliveredShipments(long deliveredShipments) { this.deliveredShipments = deliveredShipments; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public BigDecimal getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(BigDecimal totalEarnings) { this.totalEarnings = totalEarnings; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
