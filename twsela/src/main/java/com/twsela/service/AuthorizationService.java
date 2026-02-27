package com.twsela.service;

import com.twsela.domain.User;
import static com.twsela.domain.ShipmentStatusConstants.*;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationService.class);

    private final UserRepository userRepository;
    private final ShipmentRepository shipmentRepository;

    public AuthorizationService(UserRepository userRepository, ShipmentRepository shipmentRepository) {
        this.userRepository = userRepository;
        this.shipmentRepository = shipmentRepository;
    }

    /**
     * Get the currently authenticated user
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        
        String phone = authentication.getName();
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new SecurityException("User not found"));
    }

    /**
     * Check if the current user can access a shipment
     */
    public boolean canAccessShipment(Long shipmentId) {
        User currentUser = getCurrentUser();
        String role = currentUser.getRole().getName();
        
        // Owner can access all shipments
        if ("OWNER".equals(role)) {
            return true;
        }
        
        // Check if shipment exists and belongs to the user
        return shipmentRepository.findById(shipmentId)
                .map(shipment -> {
                    // Merchant can only access their own shipments
                    if ("MERCHANT".equals(role)) {
                        return shipment.getMerchant().getId().equals(currentUser.getId());
                    }
                    // Courier can only access shipments assigned to them
                    if ("COURIER".equals(role)) {
                        return shipment.getCourier() != null && 
                               shipment.getCourier().getId().equals(currentUser.getId());
                    }
                    // Warehouse manager can access shipments in their warehouse
                    if ("WAREHOUSE_MANAGER".equals(role)) {
                        return true; // Warehouse managers can see all shipments
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Check if the current user can access another user's data
     */
    public boolean canAccessUser(Long userId) {
        User currentUser = getCurrentUser();
        String role = currentUser.getRole().getName();
        
        // Owner can access all users
        if ("OWNER".equals(role)) {
            return true;
        }
        
        // Users can only access their own data
        return currentUser.getId().equals(userId);
    }

    /**
     * Check if the current user can modify a shipment
     */
    public boolean canModifyShipment(Long shipmentId) {
        User currentUser = getCurrentUser();
        String role = currentUser.getRole().getName();
        
        // Owner can modify all shipments
        if ("OWNER".equals(role)) {
            return true;
        }
        
        // Check shipment ownership and status
        return shipmentRepository.findById(shipmentId)
                .map(shipment -> {
                    // Merchant can modify their own shipments if not yet assigned to courier
                    if ("MERCHANT".equals(role)) {
                        return shipment.getMerchant().getId().equals(currentUser.getId()) &&
                               (shipment.getCourier() == null || 
                                APPROVED.equals(shipment.getStatus().getName()) ||
                                RECEIVED_AT_HUB.equals(shipment.getStatus().getName()));
                    }
                    // Courier can modify shipments assigned to them
                    if ("COURIER".equals(role)) {
                        return shipment.getCourier() != null && 
                               shipment.getCourier().getId().equals(currentUser.getId());
                    }
                    // Warehouse manager can modify shipments in their warehouse
                    if ("WAREHOUSE_MANAGER".equals(role)) {
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Ensure the current user can access a shipment, throw exception if not
     */
    public void ensureCanAccessShipment(Long shipmentId) {
        if (!canAccessShipment(shipmentId)) {
            throw new SecurityException("Access denied: You cannot access this shipment");
        }
    }

    /**
     * Ensure the current user can modify a shipment, throw exception if not
     */
    public void ensureCanModifyShipment(Long shipmentId) {
        if (!canModifyShipment(shipmentId)) {
            throw new SecurityException("Access denied: You cannot modify this shipment");
        }
    }

    /**
     * Ensure the current user can access another user's data, throw exception if not
     */
    public void ensureCanAccessUser(Long userId) {
        if (!canAccessUser(userId)) {
            throw new SecurityException("Access denied: You cannot access this user's data");
        }
    }
}
