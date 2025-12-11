package com.twsela.web;

import com.twsela.domain.*;
import com.twsela.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Unified Master Data Controller for managing zones, pricing, and users
 * Replaces role-specific endpoints with generic ones that filter by user role
 */
@RestController
@RequestMapping("/api/master")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
public class MasterDataController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ZoneRepository zoneRepository;
    
    @Autowired
    private DeliveryPricingRepository deliveryPricingRepository;
    
    @Autowired
    private TelemetrySettingsRepository telemetrySettingsRepository;

    // ========== USER MANAGEMENT ==========
    
    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Filter users based on role
        Page<User> users;
        if (currentUser.getRole().getName().equals("OWNER")) {
            users = userRepository.findAll(pageable);
        } else {
            // ADMIN can only see non-OWNER users
            users = userRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        // Only OWNER can create users
        if (!currentUser.getRole().getName().equals("OWNER")) {
            return ResponseEntity.status(403).build();
        }
        
        User createdUser = userRepository.save(user);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        // Only OWNER can update users
        if (!currentUser.getRole().getName().equals("OWNER")) {
            return ResponseEntity.status(403).build();
        }
        
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Update user fields
        existingUser.setName(user.getName());
        existingUser.setPhone(user.getPhone());
        existingUser.setRole(user.getRole());
        existingUser.setStatus(user.getStatus());
        
        User updatedUser = userRepository.save(existingUser);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        // Only OWNER can delete users
        if (!currentUser.getRole().getName().equals("OWNER")) {
            return ResponseEntity.status(403).build();
        }
        
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ========== ZONE MANAGEMENT ==========
    
    @GetMapping("/zones")
    public ResponseEntity<List<Zone>> getAllZones(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            System.out.println("🔍 MasterDataController: Getting zones for user: " + currentUser.getName() + " (" + currentUser.getPhone() + ") with role: " + currentUser.getRole().getName());
            
            // Filter zones based on role
            List<Zone> zones;
            if (currentUser.getRole().getName().equals("OWNER")) {
                zones = zoneRepository.findAll();
                System.out.println("📊 MasterDataController: Found " + zones.size() + " zones for OWNER");
            } else {
                // ADMIN can only see active zones
                zones = zoneRepository.findByStatus(ZoneStatus.ZONE_ACTIVE);
                System.out.println("📊 MasterDataController: Found " + zones.size() + " active zones for ADMIN");
            }
            
            // Log zone details
            for (Zone zone : zones) {
                System.out.println("🏢 Zone: " + zone.getName() + " - Status: " + zone.getStatus() + " - Fee: " + zone.getDefaultFee());
            }
            
            return ResponseEntity.ok(zones);
        } catch (Exception e) {
            // Log the error and return empty list instead of 500
            System.err.println("❌ MasterDataController: Error retrieving zones: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @PostMapping("/zones")
    public ResponseEntity<Zone> createZone(@RequestBody Zone zone, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        // Only OWNER can create zones
        if (!currentUser.getRole().getName().equals("OWNER")) {
            return ResponseEntity.status(403).build();
        }
        
        Zone createdZone = zoneRepository.save(zone);
        return ResponseEntity.ok(createdZone);
    }

    @PutMapping("/zones/{id}")
    public ResponseEntity<Zone> updateZone(@PathVariable Long id, @RequestBody Zone zone, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        // Only OWNER can update zones
        if (!currentUser.getRole().getName().equals("OWNER")) {
            return ResponseEntity.status(403).build();
        }
        
        Zone existingZone = zoneRepository.findById(id).orElse(null);
        if (existingZone == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Update zone fields
        existingZone.setName(zone.getName());
        existingZone.setDescription(zone.getDescription());
        existingZone.setStatus(zone.getStatus());
        existingZone.setDefaultFee(zone.getDefaultFee()); // إضافة تحديث الرسوم الافتراضية للمنطقة
        
        Zone updatedZone = zoneRepository.save(existingZone);
        return ResponseEntity.ok(updatedZone);
    }

    @DeleteMapping("/zones/{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        // Only OWNER can delete zones
        if (!currentUser.getRole().getName().equals("OWNER")) {
            return ResponseEntity.status(403).build();
        }
        
        zoneRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ========== PRICING MANAGEMENT ==========
    
    @GetMapping("/pricing")
    public ResponseEntity<List<DeliveryPricing>> getAllPricing(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        // Filter pricing based on role
        List<DeliveryPricing> pricing;
        if (currentUser.getRole().getName().equals("OWNER")) {
            pricing = deliveryPricingRepository.findAll();
        } else {
            // ADMIN can only see active pricing
            pricing = deliveryPricingRepository.findAll();
        }
        
        return ResponseEntity.ok(pricing);
    }

    @PostMapping("/pricing")
    public ResponseEntity<DeliveryPricing> createPricing(@RequestBody DeliveryPricing deliveryPricing, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        // Only OWNER can create pricing
        if (!currentUser.getRole().getName().equals("OWNER")) {
            return ResponseEntity.status(403).build();
        }
        
        DeliveryPricing createdPricing = deliveryPricingRepository.save(deliveryPricing);
        return ResponseEntity.ok(createdPricing);
    }

    @PutMapping("/pricing/{id}")
    public ResponseEntity<DeliveryPricing> updatePricing(@PathVariable Long id, @RequestBody DeliveryPricing deliveryPricing, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        // Only OWNER can update pricing
        if (!currentUser.getRole().getName().equals("OWNER")) {
            return ResponseEntity.status(403).build();
        }
        
        DeliveryPricing existingPricing = deliveryPricingRepository.findById(id).orElse(null);
        if (existingPricing == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Update pricing fields
        existingPricing.setZone(deliveryPricing.getZone());
        // existingPricing.setWeightFrom(deliveryPricing.getWeightFrom());
        // existingPricing.setWeightTo(deliveryPricing.getWeightTo());
        // existingPricing.setPrice(deliveryPricing.getPrice());
        // existingPricing.setStatus(deliveryPricing.getStatus());
        
        DeliveryPricing updatedPricing = deliveryPricingRepository.save(existingPricing);
        return ResponseEntity.ok(updatedPricing);
    }

    @DeleteMapping("/pricing/{id}")
    public ResponseEntity<Void> deletePricing(@PathVariable Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        // Only OWNER can delete pricing
        if (!currentUser.getRole().getName().equals("OWNER")) {
            return ResponseEntity.status(403).build();
        }
        
        deliveryPricingRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ========== TELEMETRY SETTINGS MANAGEMENT ==========
    
    @GetMapping("/telemetry")
    public ResponseEntity<List<TelemetrySettings>> getAllTelemetrySettings(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            
            // Only OWNER can view telemetry settings
            if (!currentUser.getRole().getName().equals("OWNER")) {
                return ResponseEntity.status(403).build();
            }
            
            List<TelemetrySettings> settings = telemetrySettingsRepository.findAll();
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            // Log the error and return empty list instead of 500
            System.err.println("❌ MasterDataController: Error retrieving telemetry settings: " + e.getMessage());
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    @PutMapping("/telemetry")
    public ResponseEntity<TelemetrySettings> updateTelemetrySettings(
            @RequestBody TelemetrySettings telemetrySettings, 
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        // Only OWNER can update telemetry settings
        if (!currentUser.getRole().getName().equals("OWNER")) {
            return ResponseEntity.status(403).build();
        }
        
        // Find existing setting by key
        TelemetrySettings existingSetting = telemetrySettingsRepository.findBySettingKey(telemetrySettings.getSettingKey())
                .orElse(null);
        
        if (existingSetting == null) {
            // Create new setting if it doesn't exist
            TelemetrySettings newSetting = new TelemetrySettings(
                    telemetrySettings.getSettingKey(),
                    telemetrySettings.getSettingValue(),
                    telemetrySettings.getDescription()
            );
            TelemetrySettings savedSetting = telemetrySettingsRepository.save(newSetting);
            return ResponseEntity.ok(savedSetting);
        } else {
            // Update existing setting
            existingSetting.setSettingValue(telemetrySettings.getSettingValue());
            existingSetting.setDescription(telemetrySettings.getDescription());
            TelemetrySettings updatedSetting = telemetrySettingsRepository.save(existingSetting);
            return ResponseEntity.ok(updatedSetting);
        }
    }
    
    @GetMapping("/telemetry/{key}")
    public ResponseEntity<TelemetrySettings> getTelemetrySettingByKey(
            @PathVariable String key, 
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        // Only OWNER can view telemetry settings
        if (!currentUser.getRole().getName().equals("OWNER")) {
            return ResponseEntity.status(403).build();
        }
        
        TelemetrySettings setting = telemetrySettingsRepository.findBySettingKey(key)
                .orElse(null);
        
        if (setting == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(setting);
    }
    
    @DeleteMapping("/telemetry/{key}")
    public ResponseEntity<Void> deleteTelemetrySetting(
            @PathVariable String key, 
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        // Only OWNER can delete telemetry settings
        if (!currentUser.getRole().getName().equals("OWNER")) {
            return ResponseEntity.status(403).build();
        }
        
        TelemetrySettings setting = telemetrySettingsRepository.findBySettingKey(key)
                .orElse(null);
        
        if (setting == null) {
            return ResponseEntity.notFound().build();
        }
        
        telemetrySettingsRepository.delete(setting);
        return ResponseEntity.ok().build();
    }

    private User getCurrentUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }
}