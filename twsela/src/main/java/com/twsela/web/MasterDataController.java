package com.twsela.web;

import com.twsela.domain.*;
import com.twsela.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Unified Master Data Controller for managing zones, pricing, and users
 * Replaces role-specific endpoints with generic ones that filter by user role
 */
@RestController
@RequestMapping("/api/master")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
@Tag(name = "Master Data", description = "إدارة البيانات الرئيسية: المناطق والتسعير والمستخدمين")
public class MasterDataController {

    private static final Logger log = LoggerFactory.getLogger(MasterDataController.class);

    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;
    private final DeliveryPricingRepository deliveryPricingRepository;
    private final TelemetrySettingsRepository telemetrySettingsRepository;
    private final PasswordEncoder passwordEncoder;

    public MasterDataController(UserRepository userRepository,
                                ZoneRepository zoneRepository,
                                DeliveryPricingRepository deliveryPricingRepository,
                                TelemetrySettingsRepository telemetrySettingsRepository,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.zoneRepository = zoneRepository;
        this.deliveryPricingRepository = deliveryPricingRepository;
        this.telemetrySettingsRepository = telemetrySettingsRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
            users = userRepository.findAllNonDeleted(pageable);
        } else {
            // ADMIN can only see non-OWNER users
            users = userRepository.findAllExcludingOwners(pageable);
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
        
        // Hash password before saving
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
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
        
        // Soft delete — mark as deleted instead of removing from database
        User userToDelete = userRepository.findById(id).orElse(null);
        if (userToDelete == null) {
            return ResponseEntity.notFound().build();
        }
        userToDelete.setIsDeleted(true);
        userToDelete.setDeletedAt(java.time.Instant.now());
        userRepository.save(userToDelete);
        return ResponseEntity.ok().build();
    }

    // ========== ZONE MANAGEMENT ==========
    
    @GetMapping("/zones")
    @Cacheable(value = "zones", key = "T(String).valueOf(#authentication.name)")
    public ResponseEntity<List<Zone>> getAllZones(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            log.debug("Getting zones for user: {} ({})", currentUser.getName(), currentUser.getRole().getName());
            
            // Filter zones based on role
            List<Zone> zones;
            if (currentUser.getRole().getName().equals("OWNER")) {
                zones = zoneRepository.findAll();
                log.debug("Found {} zones for OWNER", zones.size());
            } else {
                // ADMIN can only see active zones
                zones = zoneRepository.findByStatus(ZoneStatus.ZONE_ACTIVE);
                log.debug("Found {} active zones for ADMIN", zones.size());
            }
            
            return ResponseEntity.ok(zones);
        } catch (Exception e) {
            log.error("Error retrieving zones", e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @PostMapping("/zones")
    @CacheEvict(value = "zones", allEntries = true)
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
    @CacheEvict(value = "zones", allEntries = true)
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
    @CacheEvict(value = "zones", allEntries = true)
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
    @Cacheable(value = "pricing", key = "'all'")
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
    @CacheEvict(value = "pricing", allEntries = true)
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
    @CacheEvict(value = "pricing", allEntries = true)
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
    @CacheEvict(value = "pricing", allEntries = true)
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
            log.error("Error retrieving telemetry settings", e);
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