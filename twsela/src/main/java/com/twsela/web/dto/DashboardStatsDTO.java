package com.twsela.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class DashboardStatsDTO {
    private long totalShipments;
    private long todayShipments;
    private long deliveredShipments;
    private BigDecimal totalRevenue;
    private long activeUsers;
    private String role;
    private Instant timestamp;
    private boolean success = true;

    public DashboardStatsDTO() {}

    public long getTotalShipments() { return totalShipments; }
    public void setTotalShipments(long totalShipments) { this.totalShipments = totalShipments; }
    public long getTodayShipments() { return todayShipments; }
    public void setTodayShipments(long todayShipments) { this.todayShipments = todayShipments; }
    public long getDeliveredShipments() { return deliveredShipments; }
    public void setDeliveredShipments(long deliveredShipments) { this.deliveredShipments = deliveredShipments; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
