package com.twsela.web;

import com.twsela.domain.*;
import com.twsela.repository.*;
import com.twsela.service.FinancialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Unified Reports Controller for generating various reports.
 * All queries are pushed down to the database — no in-memory filtering.
 */
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MERCHANT') or hasRole('COURIER')")
@Tag(name = "Reports", description = "إنشاء التقارير والإحصائيات")
public class ReportsController {

    private static final Logger log = LoggerFactory.getLogger(ReportsController.class);

    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final FinancialService financialService;

    public ReportsController(ShipmentRepository shipmentRepository,
                             UserRepository userRepository,
                             FinancialService financialService) {
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
        this.financialService = financialService;
    }

    // ── /api/reports/shipments ───────────────────────────────────────────

    @Operation(summary = "تقرير الشحنات", description = "تقرير الشحنات حسب الفترة والدور")
    @ApiResponse(responseCode = "200", description = "تم بنجاح")
    @GetMapping("/shipments")
    public ResponseEntity<Map<String, Object>> getShipmentReport(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        String role = currentUser.getRole().getName();

        Map<String, Object> report;
        switch (role) {
            case "OWNER":
            case "ADMIN":
                report = getOwnerShipmentReport(startDate, endDate);
                break;
            case "MERCHANT":
                report = getMerchantShipmentReport(currentUser.getId(), startDate, endDate);
                break;
            case "COURIER":
                report = getCourierShipmentReport(currentUser.getId(), startDate, endDate);
                break;
            default:
                return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(report);
    }

    // ── /api/reports/couriers ────────────────────────────────────────────

    @Operation(summary = "تقرير المناديب", description = "تقرير أداء المناديب")
    @ApiResponse(responseCode = "200", description = "تم بنجاح")
    @GetMapping("/couriers")
    public ResponseEntity<List<Map<String, Object>>> getCourierReport(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        if (!isOwnerOrAdmin(currentUser)) {
            return ResponseEntity.status(403).build();
        }

        Instant start = convertToInstant(startDate);
        Instant end   = convertToInstantEnd(endDate);

        List<User> couriers = userRepository.findByRoleName("COURIER");
        List<Map<String, Object>> courierReport = new ArrayList<>(couriers.size());

        for (User courier : couriers) {
            Map<String, Object> data = new HashMap<>();
            data.put("courierId", courier.getId());
            data.put("courierName", courier.getName());

            // Single COUNT query instead of loading all shipments + stream filter
            long deliveredShipments = shipmentRepository.countByCourierIdAndStatusNameAndCreatedAtBetween(
                    courier.getId(), "DELIVERED", start, end);
            data.put("deliveredShipments", deliveredShipments);

            BigDecimal earnings = financialService.calculateCourierEarnings(courier.getId(), startDate, endDate);
            data.put("earnings", earnings);

            courierReport.add(data);
        }

        return ResponseEntity.ok(courierReport);
    }

    // ── /api/reports/merchants ───────────────────────────────────────────

    @Operation(summary = "تقرير التجار", description = "تقرير أداء التجار")
    @ApiResponse(responseCode = "200", description = "تم بنجاح")
    @GetMapping("/merchants")
    public ResponseEntity<List<Map<String, Object>>> getMerchantReport(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        if (!isOwnerOrAdmin(currentUser)) {
            return ResponseEntity.status(403).build();
        }

        Instant start = convertToInstant(startDate);
        Instant end   = convertToInstantEnd(endDate);

        List<User> merchants = userRepository.findByRoleName("MERCHANT");
        List<Map<String, Object>> merchantReport = new ArrayList<>(merchants.size());

        for (User merchant : merchants) {
            Map<String, Object> data = new HashMap<>();
            data.put("merchantId", merchant.getId());
            data.put("merchantName", merchant.getName());

            // 1 COUNT query for total (was: load all + stream)
            long totalShipments = shipmentRepository.countByMerchantIdAndCreatedAtBetween(
                    merchant.getId(), start, end);
            data.put("totalShipments", totalShipments);

            // 1 COUNT query for delivered (was: load all a second time + stream)
            long deliveredShipments = shipmentRepository.countByMerchantIdAndStatusNameAndCreatedAtBetween(
                    merchant.getId(), "DELIVERED", start, end);
            data.put("deliveredShipments", deliveredShipments);

            // 1 SUM query for revenue (was: load all a third time + stream + map + reduce)
            BigDecimal totalRevenue = shipmentRepository.sumDeliveryFeeByMerchantIdAndStatusNameAndCreatedAtBetween(
                    merchant.getId(), "DELIVERED", start, end);
            data.put("totalRevenue", totalRevenue);

            merchantReport.add(data);
        }

        return ResponseEntity.ok(merchantReport);
    }

    // ── /api/reports/warehouse ───────────────────────────────────────────

    @Operation(summary = "تقرير المستودع", description = "تقرير عمليات المستودع")
    @ApiResponse(responseCode = "200", description = "تم بنجاح")
    @GetMapping("/warehouse")
    public ResponseEntity<Map<String, Object>> getWarehouseReport(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        String role = currentUser.getRole().getName();
        if (!"OWNER".equals(role) && !"ADMIN".equals(role) && !"WAREHOUSE_MANAGER".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        Instant start = convertToInstant(startDate);
        Instant end   = convertToInstantEnd(endDate);

        Map<String, Object> report = new HashMap<>();

        // Actual status-specific counts with date range (was: count() × 3 with no filtering)
        long receivedShipments = shipmentRepository.countByStatusNameAndCreatedAtBetween("CREATED", start, end)
                + shipmentRepository.countByStatusNameAndCreatedAtBetween("PICKED_UP", start, end);
        report.put("receivedShipments", receivedShipments);

        long dispatchedShipments = shipmentRepository.countByStatusNameAndCreatedAtBetween("IN_TRANSIT", start, end)
                + shipmentRepository.countByStatusNameAndCreatedAtBetween("DELIVERED", start, end);
        report.put("dispatchedShipments", dispatchedShipments);

        long returnedShipments = shipmentRepository.countByStatusNameAndCreatedAtBetween("RETURNED", start, end);
        report.put("returnedShipments", returnedShipments);

        return ResponseEntity.ok(report);
    }

    // ── /api/reports/dashboard ───────────────────────────────────────────

    @Operation(summary = "تقرير لوحة التحكم", description = "ملخص لوحة التحكم")
    @ApiResponse(responseCode = "200", description = "تم بنجاح")
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardReport(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        String role = currentUser.getRole().getName();

        Map<String, Object> report = new HashMap<>();
        report.put("role", role);
        report.put("timestamp", Instant.now());

        Instant todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant todayEnd = LocalDate.now().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        switch (role) {
            case "OWNER":
            case "ADMIN":
                report.put("totalShipments", shipmentRepository.count());
                report.put("todayShipments", shipmentRepository.countByCreatedAtBetweenInstant(todayStart, todayEnd));
                report.put("totalRevenue", shipmentRepository.sumDeliveryFeeByStatusName("DELIVERED"));
                report.put("activeUsers", userRepository.countActiveUsers());
                break;
            case "MERCHANT":
                Long mid = currentUser.getId();
                report.put("totalShipments", shipmentRepository.countByMerchantId(mid));
                report.put("todayShipments", shipmentRepository.countByMerchantIdAndCreatedAtBetween(mid, todayStart, todayEnd));
                report.put("totalRevenue", shipmentRepository.sumDeliveryFeeByMerchantIdAndStatusName(mid, "DELIVERED"));
                break;
            case "COURIER":
                Long cid = currentUser.getId();
                report.put("totalShipments", shipmentRepository.countByCourierId(cid));
                report.put("todayShipments", shipmentRepository.countByCourierIdAndCreatedAtBetween(cid, todayStart, todayEnd));
                report.put("deliveredShipments", shipmentRepository.countByCourierIdAndStatusName(cid, "DELIVERED"));
                break;
        }

        report.put("success", true);
        return ResponseEntity.ok(report);
    }

    // ── Private helper methods ───────────────────────────────────────────

    private Map<String, Object> getOwnerShipmentReport(LocalDate startDate, LocalDate endDate) {
        Instant start = convertToInstant(startDate);
        Instant end   = convertToInstantEnd(endDate);

        Map<String, Object> report = new HashMap<>();
        report.put("totalShipments", shipmentRepository.countByCreatedAtBetweenInstant(start, end));
        report.put("deliveredShipments", shipmentRepository.countByStatusNameAndCreatedAtBetween("DELIVERED", start, end));
        report.put("totalRevenue", shipmentRepository.sumDeliveryFeeByStatusNameAndCreatedAtBetween("DELIVERED", start, end));
        return report;
    }

    private Map<String, Object> getMerchantShipmentReport(Long merchantId, LocalDate startDate, LocalDate endDate) {
        Instant start = convertToInstant(startDate);
        Instant end   = convertToInstantEnd(endDate);

        Map<String, Object> report = new HashMap<>();
        report.put("totalShipments", shipmentRepository.countByMerchantIdAndCreatedAtBetween(merchantId, start, end));
        report.put("deliveredShipments", shipmentRepository.countByMerchantIdAndStatusNameAndCreatedAtBetween(merchantId, "DELIVERED", start, end));
        report.put("totalRevenue", shipmentRepository.sumDeliveryFeeByMerchantIdAndStatusNameAndCreatedAtBetween(merchantId, "DELIVERED", start, end));
        return report;
    }

    private Map<String, Object> getCourierShipmentReport(Long courierId, LocalDate startDate, LocalDate endDate) {
        Instant start = convertToInstant(startDate);
        Instant end   = convertToInstantEnd(endDate);

        Map<String, Object> report = new HashMap<>();
        report.put("totalShipments", shipmentRepository.countByCourierIdAndCreatedAtBetween(courierId, start, end));
        report.put("deliveredShipments", shipmentRepository.countByCourierIdAndStatusNameAndCreatedAtBetween(courierId, "DELIVERED", start, end));
        report.put("totalEarnings", financialService.calculateCourierEarnings(courierId, startDate, endDate));
        return report;
    }

    private boolean isOwnerOrAdmin(User user) {
        String role = user.getRole().getName();
        return "OWNER".equals(role) || "ADMIN".equals(role);
    }

    private User getCurrentUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    private Instant convertToInstant(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private Instant convertToInstantEnd(LocalDate date) {
        return date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
    }
}
