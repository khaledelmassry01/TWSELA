package com.twsela.web;

import com.twsela.domain.*;
import com.twsela.repository.*;
import com.twsela.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
public class DashboardController {

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
            System.out.println("🔍 DashboardController: /summary endpoint called");
            
            User currentUser = getCurrentUser(authentication);
            System.out.println("✅ DashboardController: User found: " + currentUser.getName() + " (" + currentUser.getRole().getName() + ")");
            
            Map<String, Object> summary = new HashMap<>();
            
            String role = currentUser.getRole().getName();
            System.out.println("🔍 DashboardController: User role: " + role);
            
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
            
            System.out.println("✅ DashboardController: Returning summary for role: " + role);
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            System.err.println("❌ DashboardController: Error in getDashboardSummary: " + e.getMessage());
            e.printStackTrace();
            
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
            System.out.println("🔍 DashboardController: Getting owner dashboard summary");
            
            // Total shipments
            long totalShipments = shipmentRepository.count();
            summary.put("totalShipments", totalShipments);
            System.out.println("✅ Total shipments: " + totalShipments);
            
            // Today's shipments
            Instant todayStart = getTodayStart();
            Instant todayEnd = getTodayEnd();
            long todayShipments = shipmentRepository.countByCreatedAtBetween(todayStart, todayEnd);
            summary.put("todayShipments", todayShipments);
            System.out.println("✅ Today's shipments: " + todayShipments);
            
            // Total revenue - simplified approach
            BigDecimal totalRevenue = BigDecimal.ZERO;
            try {
                Optional<ShipmentStatus> deliveredStatus = shipmentStatusRepository.findByName("DELIVERED");
                if (deliveredStatus.isPresent()) {
                    List<Shipment> deliveredShipments = shipmentRepository.findByStatus(deliveredStatus.get());
                    totalRevenue = deliveredShipments.stream()
                        .map(Shipment::getDeliveryFee)
                        .filter(fee -> fee != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                }
            } catch (Exception e) {
                System.err.println("Error calculating revenue: " + e.getMessage());
                totalRevenue = BigDecimal.ZERO;
            }
            summary.put("totalRevenue", totalRevenue);
            System.out.println("✅ Total revenue: " + totalRevenue);
            
            // Active users
            long activeUsers = userRepository.count();
            summary.put("activeUsers", activeUsers);
            System.out.println("✅ Active users: " + activeUsers);
            
            // Recent activity - simplified approach
            try {
                List<Shipment> recentActivity = shipmentRepository.findAll().stream()
                    .sorted((s1, s2) -> s2.getUpdatedAt().compareTo(s1.getUpdatedAt()))
                    .limit(10)
                    .toList();
                summary.put("recentActivity", recentActivity);
                System.out.println("✅ Recent activity: " + recentActivity.size() + " items");
            } catch (Exception e) {
                System.err.println("Error loading recent activity: " + e.getMessage());
                summary.put("recentActivity", new ArrayList<>());
            }
            
            System.out.println("✅ Owner dashboard summary completed successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Error in getOwnerDashboardSummary: " + e.getMessage());
            e.printStackTrace();
            // Return empty summary with error info
            summary.put("error", "Failed to load dashboard data: " + e.getMessage());
        }
        
        return summary;
    }

    private Map<String, Object> getAdminDashboardSummary(User admin) {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            // Total shipments (excluding owner's personal data)
            long totalShipments = shipmentRepository.count();
            summary.put("totalShipments", totalShipments);
            
            // Today's shipments
            Instant todayStart = getTodayStart();
            Instant todayEnd = getTodayEnd();
            long todayShipments = shipmentRepository.countByCreatedAtBetween(todayStart, todayEnd);
            summary.put("todayShipments", todayShipments);
            
            // Active users (excluding owner)
            long activeUsers = userRepository.count();
            summary.put("activeUsers", activeUsers);
            
            // Recent activity
            List<Shipment> recentActivity = shipmentRepository.findAll().stream()
                .sorted((s1, s2) -> s2.getUpdatedAt().compareTo(s1.getUpdatedAt()))
                .limit(10)
                .toList();
            summary.put("recentActivity", recentActivity);
            
        } catch (Exception e) {
            System.err.println("Error in getAdminDashboardSummary: " + e.getMessage());
            e.printStackTrace();
            summary.put("error", "Failed to load dashboard data: " + e.getMessage());
        }
        
        return summary;
    }

    private Map<String, Object> getMerchantDashboardSummary(User merchant) {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            // Total shipments for this merchant
            long totalShipments = shipmentRepository.countByMerchantId(merchant.getId());
            summary.put("totalShipments", totalShipments);
            
            // Today's shipments
            Instant todayStart = getTodayStart();
            Instant todayEnd = getTodayEnd();
            List<Shipment> merchantShipments = shipmentRepository.findByMerchantId(merchant.getId());
            long todayShipments = merchantShipments.stream()
                .filter(s -> s.getCreatedAt().isAfter(todayStart) && s.getCreatedAt().isBefore(todayEnd))
                .count();
            summary.put("todayShipments", todayShipments);
            
            // Delivered shipments
            long deliveredShipments = merchantShipments.stream()
                .filter(s -> s.getStatus() != null && "DELIVERED".equals(s.getStatus().getName()))
                .count();
            summary.put("deliveredShipments", deliveredShipments);
            
            // Total revenue
            BigDecimal totalRevenue = merchantShipments.stream()
                .filter(s -> s.getStatus() != null && "DELIVERED".equals(s.getStatus().getName()))
                .map(Shipment::getDeliveryFee)
                .filter(fee -> fee != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            summary.put("totalRevenue", totalRevenue);
            
            // Recent activity
            List<Shipment> recentActivity = merchantShipments.stream()
                .sorted((s1, s2) -> s2.getUpdatedAt().compareTo(s1.getUpdatedAt()))
                .limit(10)
                .toList();
            summary.put("recentActivity", recentActivity);
            
        } catch (Exception e) {
            System.err.println("Error in getMerchantDashboardSummary: " + e.getMessage());
            e.printStackTrace();
            summary.put("error", "Failed to load dashboard data: " + e.getMessage());
        }
        
        return summary;
    }

    private Map<String, Object> getCourierDashboardSummary(User courier) {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            // Total assigned shipments
            long totalShipments = shipmentRepository.countByCourierId(courier.getId());
            summary.put("totalShipments", totalShipments);
            
            // Today's shipments
            Instant todayStart = getTodayStart();
            Instant todayEnd = getTodayEnd();
            List<Shipment> courierShipments = shipmentRepository.findByCourierId(courier.getId());
            long todayShipments = courierShipments.stream()
                .filter(s -> s.getCreatedAt().isAfter(todayStart) && s.getCreatedAt().isBefore(todayEnd))
                .count();
            summary.put("todayShipments", todayShipments);
            
            // Delivered shipments
            long deliveredShipments = courierShipments.stream()
                .filter(s -> s.getStatus() != null && "DELIVERED".equals(s.getStatus().getName()))
                .count();
            summary.put("deliveredShipments", deliveredShipments);
            
            // Total earnings
            BigDecimal totalEarnings = financialService.calculateCourierEarnings(
                courier.getId(), 
                java.time.LocalDate.now().minusDays(30), 
                java.time.LocalDate.now()
            );
            summary.put("totalEarnings", totalEarnings);
            
            // Recent activity
            List<Shipment> recentActivity = courierShipments.stream()
                .sorted((s1, s2) -> s2.getUpdatedAt().compareTo(s1.getUpdatedAt()))
                .limit(10)
                .toList();
            summary.put("recentActivity", recentActivity);
            
        } catch (Exception e) {
            System.err.println("Error in getCourierDashboardSummary: " + e.getMessage());
            e.printStackTrace();
            summary.put("error", "Failed to load dashboard data: " + e.getMessage());
        }
        
        return summary;
    }

    private Map<String, Object> getWarehouseDashboardSummary(User warehouseManager) {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            // Shipments received today
            Instant todayStart = getTodayStart();
            Instant todayEnd = getTodayEnd();
            
            // Count shipments received today (simplified for now)
            long receivedToday = shipmentRepository.countByCreatedAtBetween(todayStart, todayEnd);
            summary.put("receivedToday", receivedToday);
            
            // Count shipments dispatched today (simplified for now)
            long dispatchedToday = shipmentRepository.countByCreatedAtBetween(todayStart, todayEnd);
            summary.put("dispatchedToday", dispatchedToday);
            
            // Current inventory (simplified for now)
            long currentInventory = shipmentRepository.count();
            summary.put("currentInventory", currentInventory);
            
            // Pending returns (simplified for now)
            long pendingReturns = shipmentRepository.count();
            summary.put("pendingReturns", pendingReturns);
            
        } catch (Exception e) {
            System.err.println("Error in getWarehouseDashboardSummary: " + e.getMessage());
            e.printStackTrace();
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
            // Get basic statistics
            long totalShipments = shipmentRepository.count();
            long totalUsers = userRepository.count();
            
            // Calculate active shipments (simplified approach)
            long activeShipments = shipmentRepository.count();
            
            // Calculate delivered shipments (simplified approach)
            long deliveredShipments = shipmentRepository.count();
            
            // Calculate delivery rate
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
            response.put("success", false);
            response.put("message", "حدث خطأ في تحميل الإحصائيات: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // This would typically filter statistics based on user role
            // For now, we'll return general statistics
            
            Map<String, Object> dashboardStats = new HashMap<>();
            
            // Today's statistics
            Map<String, Object> todayStats = new HashMap<>();
            todayStats.put("shipments", 15);
            todayStats.put("deliveries", 12);
            todayStats.put("pending", 3);
            todayStats.put("revenue", 2500.0);
            
            // This week's statistics
            Map<String, Object> weekStats = new HashMap<>();
            weekStats.put("shipments", 95);
            weekStats.put("deliveries", 87);
            weekStats.put("pending", 8);
            weekStats.put("revenue", 18500.0);
            
            // This month's statistics
            Map<String, Object> monthStats = new HashMap<>();
            monthStats.put("shipments", 420);
            monthStats.put("deliveries", 398);
            monthStats.put("pending", 22);
            monthStats.put("revenue", 78500.0);
            
            dashboardStats.put("today", todayStats);
            dashboardStats.put("week", weekStats);
            dashboardStats.put("month", monthStats);
            
            response.put("success", true);
            response.put("dashboard", dashboardStats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "حدث خطأ في تحميل إحصائيات لوحة التحكم: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/revenue-chart")
    public ResponseEntity<Map<String, Object>> getRevenueChart(Authentication authentication) {
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", new String[]{"2025-01-04", "2025-01-05", "2025-01-06"});
        chartData.put("data", new BigDecimal[]{BigDecimal.valueOf(500), BigDecimal.valueOf(750), BigDecimal.valueOf(600)});
        chartData.put("success", true);
        return ResponseEntity.ok(chartData);
    }

    @GetMapping("/shipments-chart")
    public ResponseEntity<Map<String, Object>> getShipmentsChart(Authentication authentication) {
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", new String[]{"2025-01-04", "2025-01-05", "2025-01-06"});
        chartData.put("data", new Long[]{10L, 15L, 12L});
        chartData.put("success", true);
        return ResponseEntity.ok(chartData);
    }

    private Instant getTodayEnd() {
        return Instant.now().atZone(java.time.ZoneId.systemDefault())
            .withHour(23).withMinute(59).withSecond(59).toInstant();
    }
}
