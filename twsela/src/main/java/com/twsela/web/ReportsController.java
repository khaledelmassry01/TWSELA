package com.twsela.web;

import com.twsela.domain.*;
import com.twsela.repository.*;
import com.twsela.service.FinancialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Unified Reports Controller for generating various reports
 * Replaces role-specific report endpoints with generic ones that filter by user role
 */
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MERCHANT') or hasRole('COURIER')")
public class ReportsController {

    @Autowired
    private ShipmentRepository shipmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FinancialService financialService;

    @GetMapping("/shipments")
    public ResponseEntity<Map<String, Object>> getShipmentReport(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        Map<String, Object> report = new HashMap<>();
        
        String role = currentUser.getRole().getName();
        
        switch (role) {
            case "OWNER":
                report = getOwnerShipmentReport(startDate, endDate);
                break;
            case "ADMIN":
                report = getAdminShipmentReport(startDate, endDate);
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

    @GetMapping("/couriers")
    public ResponseEntity<List<Map<String, Object>>> getCourierReport(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        
        // Only OWNER and ADMIN can see courier reports
        if (!currentUser.getRole().getName().equals("OWNER") && !currentUser.getRole().getName().equals("ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        
        List<Map<String, Object>> courierReport = new ArrayList<>();
        
        // Get all couriers
        List<User> couriers = userRepository.findByRoleName("COURIER");
        
        for (User courier : couriers) {
            Map<String, Object> courierData = new HashMap<>();
            courierData.put("courierId", courier.getId());
            courierData.put("courierName", courier.getName());
            
            // Calculate earnings for the period
            BigDecimal earnings = financialService.calculateCourierEarnings(courier.getId(), startDate, endDate);
            courierData.put("earnings", earnings);
            
            // Count delivered shipments
            long deliveredShipments = shipmentRepository.findByCourierId(courier.getId()).stream()
                .filter(s -> "DELIVERED".equals(s.getStatus().getName()) &&
                           s.getCreatedAt().isAfter(convertToInstant(startDate)) &&
                           s.getCreatedAt().isBefore(convertToInstantEnd(endDate)))
                .count();
            courierData.put("deliveredShipments", deliveredShipments);
            
            courierReport.add(courierData);
        }
        
        return ResponseEntity.ok(courierReport);
    }

    @GetMapping("/merchants")
    public ResponseEntity<List<Map<String, Object>>> getMerchantReport(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        
        // Only OWNER and ADMIN can see merchant reports
        if (!currentUser.getRole().getName().equals("OWNER") && !currentUser.getRole().getName().equals("ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        
        List<Map<String, Object>> merchantReport = new ArrayList<>();
        
        // Get all merchants
        List<User> merchants = userRepository.findByRoleName("MERCHANT");
        
        for (User merchant : merchants) {
            Map<String, Object> merchantData = new HashMap<>();
            merchantData.put("merchantId", merchant.getId());
            merchantData.put("merchantName", merchant.getName());
            
            // Count total shipments
            long totalShipments = shipmentRepository.findByMerchantId(merchant.getId()).stream()
                .filter(s -> s.getCreatedAt().isAfter(convertToInstant(startDate)) &&
                           s.getCreatedAt().isBefore(convertToInstantEnd(endDate)))
                .count();
            merchantData.put("totalShipments", totalShipments);
            
            // Count delivered shipments
            long deliveredShipments = shipmentRepository.findByMerchantId(merchant.getId()).stream()
                .filter(s -> "DELIVERED".equals(s.getStatus().getName()) &&
                           s.getCreatedAt().isAfter(convertToInstant(startDate)) &&
                           s.getCreatedAt().isBefore(convertToInstantEnd(endDate)))
                .count();
            merchantData.put("deliveredShipments", deliveredShipments);
            
            // Calculate total revenue
            BigDecimal totalRevenue = shipmentRepository.findByMerchantId(merchant.getId()).stream()
                .filter(s -> "DELIVERED".equals(s.getStatus().getName()) &&
                           s.getCreatedAt().isAfter(convertToInstant(startDate)) &&
                           s.getCreatedAt().isBefore(convertToInstantEnd(endDate)))
                .map(Shipment::getDeliveryFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            merchantData.put("totalRevenue", totalRevenue);
            
            merchantReport.add(merchantData);
        }
        
        return ResponseEntity.ok(merchantReport);
    }

    @GetMapping("/warehouse")
    public ResponseEntity<Map<String, Object>> getWarehouseReport(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        
        // Only OWNER, ADMIN, and WAREHOUSE_MANAGER can see warehouse reports
        if (!currentUser.getRole().getName().equals("OWNER") && 
            !currentUser.getRole().getName().equals("ADMIN") && 
            !currentUser.getRole().getName().equals("WAREHOUSE_MANAGER")) {
            return ResponseEntity.status(403).build();
        }
        
        Map<String, Object> report = new HashMap<>();
        
        // Count shipments received
        long receivedShipments = shipmentRepository.count();
        report.put("receivedShipments", receivedShipments);
        
        // Count shipments dispatched
        long dispatchedShipments = shipmentRepository.count();
        report.put("dispatchedShipments", dispatchedShipments);
        
        // Count returned shipments
        long returnedShipments = shipmentRepository.count();
        report.put("returnedShipments", returnedShipments);
        
        return ResponseEntity.ok(report);
    }

    private Map<String, Object> getOwnerShipmentReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        
        List<Shipment> shipments = shipmentRepository.findAll().stream()
            .filter(s -> s.getCreatedAt().isAfter(convertToInstant(startDate)) &&
                       s.getCreatedAt().isBefore(convertToInstantEnd(endDate)))
            .toList();
        
        report.put("totalShipments", shipments.size());
        report.put("deliveredShipments", shipments.stream()
            .filter(s -> "DELIVERED".equals(s.getStatus().getName()))
            .count());
        report.put("totalRevenue", shipments.stream()
            .filter(s -> "DELIVERED".equals(s.getStatus().getName()))
            .map(Shipment::getDeliveryFee)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        return report;
    }

    private Map<String, Object> getAdminShipmentReport(LocalDate startDate, LocalDate endDate) {
        // Similar to owner report but may exclude sensitive financial data
        return getOwnerShipmentReport(startDate, endDate);
    }

    private Map<String, Object> getMerchantShipmentReport(Long merchantId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        
        List<Shipment> shipments = shipmentRepository.findByMerchantId(merchantId).stream()
            .filter(s -> s.getCreatedAt().isAfter(convertToInstant(startDate)) &&
                       s.getCreatedAt().isBefore(convertToInstantEnd(endDate)))
            .toList();
        
        report.put("totalShipments", shipments.size());
        report.put("deliveredShipments", shipments.stream()
            .filter(s -> "DELIVERED".equals(s.getStatus().getName()))
            .count());
        report.put("totalRevenue", shipments.stream()
            .filter(s -> "DELIVERED".equals(s.getStatus().getName()))
            .map(Shipment::getDeliveryFee)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        return report;
    }

    private Map<String, Object> getCourierShipmentReport(Long courierId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        
        List<Shipment> shipments = shipmentRepository.findByCourierId(courierId).stream()
            .filter(s -> s.getCreatedAt().isAfter(convertToInstant(startDate)) &&
                       s.getCreatedAt().isBefore(convertToInstantEnd(endDate)))
            .toList();
        
        report.put("totalShipments", shipments.size());
        report.put("deliveredShipments", shipments.stream()
            .filter(s -> "DELIVERED".equals(s.getStatus().getName()))
            .count());
        
        // Calculate earnings
        BigDecimal earnings = financialService.calculateCourierEarnings(courierId, startDate, endDate);
        report.put("totalEarnings", earnings);
        
        return report;
    }

    private User getCurrentUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    private java.time.Instant convertToInstant(LocalDate date) {
        return date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
    }

    private java.time.Instant convertToInstantEnd(LocalDate date) {
        return date.atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant();
    }
}
