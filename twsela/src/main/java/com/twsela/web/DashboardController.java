package com.twsela.web;

import com.twsela.domain.*;
import static com.twsela.domain.ShipmentStatusConstants.*;
import com.twsela.repository.*;
import com.twsela.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Unified Dashboard Controller for all user roles
 * Replaces role-specific dashboard endpoints with generic ones that filter by user role
 */
@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MERCHANT') or hasRole('COURIER') or hasRole('WAREHOUSE_MANAGER')")
@Tag(name = "Dashboard", description = "لوحة التحكم والإحصائيات")
@Transactional(readOnly = true)
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private ShipmentRepository shipmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ShipmentStatusRepository shipmentStatusRepository;
    
    @Autowired
    private FinancialService financialService;

    @Operation(
        summary = "الحصول على ملخص لوحة التحكم",
        description = "الحصول على ملخص شامل للوحة التحكم حسب دور المستخدم"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "تم الحصول على الملخص بنجاح",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "Owner Dashboard Summary",
                    value = """
                    {
                        "success": true,
                        "totalShipments": 150,
                        "todayShipments": 25,
                        "totalRevenue": 50000.00,
                        "activeUsers": 45,
                        "recentActivity": [...],
                        "timestamp": "2024-01-15T10:30:00Z",
                        "userRole": "OWNER"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "غير مصرح - لا توجد صلاحية للوصول"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "خطأ في الخادم"
        )
    })
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            String role = currentUser.getRole().getName();
            log.debug("Dashboard summary requested by {} ({})", currentUser.getName(), role);
            
            Map<String, Object> summary = new HashMap<>();
            
            switch (role) {
                case "OWNER":
                    summary = getOwnerDashboardSummary(currentUser);
                    break;
                case "ADMIN":
                    summary = getAdminDashboardSummary(currentUser);
                    break;
                case "MERCHANT":
                    summary = getMerchantDashboardSummary(currentUser);
                    break;
                case "COURIER":
                    summary = getCourierDashboardSummary(currentUser);
                    break;
                case "WAREHOUSE_MANAGER":
                    summary = getWarehouseDashboardSummary(currentUser);
                    break;
                default:
                    return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "غير مصرح بالوصول لهذا الدور",
                        "timestamp", java.time.Instant.now()
                    ));
            }
            
            // Add success flag and timestamp
            summary.put("success", true);
            summary.put("timestamp", java.time.Instant.now());
            summary.put("userRole", role);
            
            log.debug("Returning summary for role: {}", role);
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Error in getDashboardSummary", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "حدث خطأ في تحميل بيانات لوحة التحكم");
            errorResponse.put("timestamp", java.time.Instant.now());
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    private Map<String, Object> getOwnerDashboardSummary(User owner) {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            log.debug("Building owner dashboard summary");
            
            long totalShipments = shipmentRepository.count();
            summary.put("totalShipments", totalShipments);
            
            Instant todayStart = getTodayStart();
            Instant todayEnd = getTodayEnd();
            long todayShipments = shipmentRepository.countByCreatedAtBetween(todayStart, todayEnd);
            summary.put("todayShipments", todayShipments);
            
            BigDecimal totalRevenue = shipmentRepository.sumDeliveryFeeByStatusName(DELIVERED);
            summary.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
            
            long activeUsers = userRepository.count();
            summary.put("activeUsers", activeUsers);
            
            List<Shipment> recentActivity = shipmentRepository.findTop10ByOrderByUpdatedAtDesc();
            summary.put("recentActivity", recentActivity);
            
            log.debug("Owner dashboard: {} shipments, {} today, revenue={}", totalShipments, todayShipments, totalRevenue);
            
        } catch (Exception e) {
            log.error("Error in getOwnerDashboardSummary", e);
            summary.put("error", "Failed to load dashboard data: " + e.getMessage());
        }
        
        return summary;
    }

    private Map<String, Object> getAdminDashboardSummary(User admin) {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            long totalShipments = shipmentRepository.count();
            summary.put("totalShipments", totalShipments);
            
            Instant todayStart = getTodayStart();
            Instant todayEnd = getTodayEnd();
            long todayShipments = shipmentRepository.countByCreatedAtBetween(todayStart, todayEnd);
            summary.put("todayShipments", todayShipments);
            
            long activeUsers = userRepository.count();
            summary.put("activeUsers", activeUsers);
            
            List<Shipment> recentActivity = shipmentRepository.findTop10ByOrderByUpdatedAtDesc();
            summary.put("recentActivity", recentActivity);
            
        } catch (Exception e) {
            log.error("Error in getAdminDashboardSummary", e);
            summary.put("error", "Failed to load dashboard data: " + e.getMessage());
        }
        
        return summary;
    }

    private Map<String, Object> getMerchantDashboardSummary(User merchant) {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            Long mid = merchant.getId();
            
            long totalShipments = shipmentRepository.countByMerchantId(mid);
            summary.put("totalShipments", totalShipments);
            
            Instant todayStart = getTodayStart();
            Instant todayEnd = getTodayEnd();
            long todayShipments = shipmentRepository.countByMerchantIdAndCreatedAtBetween(mid, todayStart, todayEnd);
            summary.put("todayShipments", todayShipments);
            
            long deliveredShipments = shipmentRepository.countByMerchantIdAndStatusName(mid, DELIVERED);
            summary.put("deliveredShipments", deliveredShipments);
            
            BigDecimal totalRevenue = shipmentRepository.sumDeliveryFeeByMerchantIdAndStatusName(mid, DELIVERED);
            summary.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
            
            List<Shipment> recentActivity = shipmentRepository.findTop10ByMerchantIdOrderByUpdatedAtDesc(mid);
            summary.put("recentActivity", recentActivity);
            
        } catch (Exception e) {
            log.error("Error in getMerchantDashboardSummary", e);
            summary.put("error", "Failed to load dashboard data: " + e.getMessage());
        }
        
        return summary;
    }

    private Map<String, Object> getCourierDashboardSummary(User courier) {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            Long cid = courier.getId();
            
            long totalShipments = shipmentRepository.countByCourierId(cid);
            summary.put("totalShipments", totalShipments);
            
            Instant todayStart = getTodayStart();
            Instant todayEnd = getTodayEnd();
            long todayShipments = shipmentRepository.countByCourierIdAndCreatedAtBetween(cid, todayStart, todayEnd);
            summary.put("todayShipments", todayShipments);
            
            long deliveredShipments = shipmentRepository.countByCourierIdAndStatusName(cid, DELIVERED);
            summary.put("deliveredShipments", deliveredShipments);
            
            BigDecimal totalEarnings = financialService.calculateCourierEarnings(
                cid, 
                java.time.LocalDate.now().minusDays(30), 
                java.time.LocalDate.now()
            );
            summary.put("totalEarnings", totalEarnings);
            
            List<Shipment> recentActivity = shipmentRepository.findTop10ByCourierIdOrderByUpdatedAtDesc(cid);
            summary.put("recentActivity", recentActivity);
            
        } catch (Exception e) {
            log.error("Error in getCourierDashboardSummary", e);
            summary.put("error", "Failed to load dashboard data: " + e.getMessage());
        }
        
        return summary;
    }

    private Map<String, Object> getWarehouseDashboardSummary(User warehouseManager) {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            Instant todayStart = getTodayStart();
            Instant todayEnd = getTodayEnd();
            
            long receivedToday = shipmentRepository.countByCreatedAtBetween(todayStart, todayEnd);
            summary.put("receivedToday", receivedToday);
            
            long dispatchedToday = shipmentRepository.countByStatusName(READY_FOR_DISPATCH);
            summary.put("dispatchedToday", dispatchedToday);
            
            long currentInventory = shipmentRepository.countByStatusName(RECEIVED_AT_HUB);
            summary.put("currentInventory", currentInventory);
            
            long pendingReturns = shipmentRepository.countByStatusName(PENDING_RETURN);
            summary.put("pendingReturns", pendingReturns);
            
        } catch (Exception e) {
            log.error("Error in getWarehouseDashboardSummary", e);
            summary.put("error", "Failed to load dashboard data: " + e.getMessage());
        }
        
        return summary;
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        String phone = authentication.getName();
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Instant getTodayStart() {
        return Instant.now().atZone(java.time.ZoneId.systemDefault())
            .withHour(0).withMinute(0).withSecond(0).toInstant();
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long totalShipments = shipmentRepository.count();
            long totalUsers = userRepository.count();
            
            long activeShipments = shipmentRepository.countByStatusName(IN_TRANSIT)
                + shipmentRepository.countByStatusName(OUT_FOR_DELIVERY)
                + shipmentRepository.countByStatusName(ASSIGNED_TO_COURIER);
            
            long deliveredShipments = shipmentRepository.countByStatusName(DELIVERED);
            
            double deliveryRate = totalShipments > 0 ? (double) deliveredShipments / totalShipments * 100 : 0;
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalShipments", totalShipments);
            stats.put("activeShipments", activeShipments);
            stats.put("deliveredShipments", deliveredShipments);
            stats.put("totalUsers", totalUsers);
            stats.put("deliveryRate", Math.round(deliveryRate * 100.0) / 100.0);
            
            response.put("success", true);
            response.put("statistics", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in getStatistics", e);
            response.put("success", false);
            response.put("message", "حدث خطأ في تحميل الإحصائيات: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Instant todayStart = getTodayStart();
            Instant todayEnd = getTodayEnd();
            Instant weekStart = Instant.now().atZone(java.time.ZoneId.systemDefault())
                .minusDays(7).withHour(0).withMinute(0).withSecond(0).toInstant();
            Instant monthStart = Instant.now().atZone(java.time.ZoneId.systemDefault())
                .minusDays(30).withHour(0).withMinute(0).withSecond(0).toInstant();
            
            Map<String, Object> dashboardStats = new HashMap<>();
            
            // Today's statistics
            Map<String, Object> todayStats = new HashMap<>();
            long todayShipments = shipmentRepository.countByCreatedAtBetween(todayStart, todayEnd);
            todayStats.put("shipments", todayShipments);
            todayStats.put("deliveries", shipmentRepository.countByStatusName(DELIVERED));
            todayStats.put("pending", shipmentRepository.countByStatusName(PENDING));
            BigDecimal todayRevenue = shipmentRepository.sumDeliveryFeeByStatusName(DELIVERED);
            todayStats.put("revenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);
            
            // This week's statistics
            Map<String, Object> weekStats = new HashMap<>();
            long weekShipments = shipmentRepository.countByCreatedAtBetween(weekStart, todayEnd);
            weekStats.put("shipments", weekShipments);
            weekStats.put("deliveries", shipmentRepository.countByStatusName(DELIVERED));
            weekStats.put("pending", shipmentRepository.countByStatusName(PENDING));
            weekStats.put("revenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);
            
            // This month's statistics
            Map<String, Object> monthStats = new HashMap<>();
            long monthShipments = shipmentRepository.countByCreatedAtBetween(monthStart, todayEnd);
            monthStats.put("shipments", monthShipments);
            monthStats.put("deliveries", shipmentRepository.countByStatusName(DELIVERED));
            monthStats.put("pending", shipmentRepository.countByStatusName(PENDING));
            monthStats.put("revenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);
            
            dashboardStats.put("today", todayStats);
            dashboardStats.put("week", weekStats);
            dashboardStats.put("month", monthStats);
            
            response.put("success", true);
            response.put("dashboard", dashboardStats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in getDashboardStatistics", e);
            response.put("success", false);
            response.put("message", "حدث خطأ في تحميل إحصائيات لوحة التحكم: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/revenue-chart")
    public ResponseEntity<Map<String, Object>> getRevenueChart(Authentication authentication) {
        Map<String, Object> chartData = new HashMap<>();
        try {
            java.time.ZoneId zone = java.time.ZoneId.systemDefault();
            java.time.LocalDate today = java.time.LocalDate.now();
            String[] labels = new String[7];
            BigDecimal[] data = new BigDecimal[7];
            for (int i = 6; i >= 0; i--) {
                java.time.LocalDate day = today.minusDays(i);
                labels[6 - i] = day.toString();
                // Approximate: use total delivered revenue for now
                data[6 - i] = BigDecimal.ZERO;
            }
            BigDecimal totalRevenue = shipmentRepository.sumDeliveryFeeByStatusName(DELIVERED);
            chartData.put("labels", labels);
            chartData.put("data", data);
            chartData.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
            chartData.put("success", true);
        } catch (Exception e) {
            log.error("Error in getRevenueChart", e);
            chartData.put("success", false);
            chartData.put("message", e.getMessage());
        }
        return ResponseEntity.ok(chartData);
    }

    @GetMapping("/shipments-chart")
    public ResponseEntity<Map<String, Object>> getShipmentsChart(Authentication authentication) {
        Map<String, Object> chartData = new HashMap<>();
        try {
            java.time.ZoneId zone = java.time.ZoneId.systemDefault();
            java.time.LocalDate today = java.time.LocalDate.now();
            String[] labels = new String[7];
            Long[] data = new Long[7];
            for (int i = 6; i >= 0; i--) {
                java.time.LocalDate day = today.minusDays(i);
                labels[6 - i] = day.toString();
                Instant dayStart = day.atStartOfDay(zone).toInstant();
                Instant dayEnd = day.plusDays(1).atStartOfDay(zone).toInstant();
                data[6 - i] = shipmentRepository.countByCreatedAtBetween(dayStart, dayEnd);
            }
            chartData.put("labels", labels);
            chartData.put("data", data);
            chartData.put("success", true);
        } catch (Exception e) {
            log.error("Error in getShipmentsChart", e);
            chartData.put("success", false);
            chartData.put("message", e.getMessage());
        }
        return ResponseEntity.ok(chartData);
    }

    private Instant getTodayEnd() {
        return Instant.now().atZone(java.time.ZoneId.systemDefault())
            .withHour(23).withMinute(59).withSecond(59).toInstant();
    }
}
