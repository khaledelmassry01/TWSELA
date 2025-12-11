package com.twsela.config;

import com.twsela.domain.*;
import com.twsela.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

//@Component  // Temporarily disabled for Swagger testing
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserStatusRepository userStatusRepository;
    
    @Autowired
    private ShipmentStatusRepository shipmentStatusRepository;
    
    @Autowired
    private PayoutStatusRepository payoutStatusRepository;
    
    @Autowired
    private ZoneRepository zoneRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private TelemetrySettingsRepository telemetrySettingsRepository;

    @Override
    public void run(String... args) throws Exception {
        try {
            System.out.println("🚀 Starting data initialization...");
            initializeRoles();
            initializeUserStatuses();
            initializeShipmentStatuses();
            initializePayoutStatuses();
            initializeZones();
            initializeTelemetrySettings();
            initializeUsers();
            System.out.println("✅ Data initialization completed successfully!");
            
            // Verify zones were created
            long zoneCount = zoneRepository.count();
            System.out.println("📊 Total zones in database: " + zoneCount);
            if (zoneCount > 0) {
                System.out.println("✅ Zones are available in database");
            } else {
                System.err.println("❌ No zones found in database!");
            }
        } catch (Exception e) {
            System.err.println("❌ DataInitializer: Critical error during initialization: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to prevent application startup with incomplete data
        }
    }

    private void initializeRoles() {
        if (roleRepository.count() == 0) {
            List<Role> roles = Arrays.asList(
                new Role("OWNER", "System Owner"),
                new Role("ADMIN", "System Administrator"),
                new Role("MERCHANT", "Merchant"),
                new Role("COURIER", "Courier"),
                new Role("WAREHOUSE_MANAGER", "Warehouse Manager")
            );
            roleRepository.saveAll(roles);
        }
    }

    private void initializeUserStatuses() {
        if (userStatusRepository.count() == 0) {
            List<UserStatus> statuses = Arrays.asList(
                new UserStatus("ACTIVE"),
                new UserStatus("INACTIVE"),
                new UserStatus("SUSPENDED"),
                new UserStatus("PENDING_VERIFICATION")
            );
            userStatusRepository.saveAll(statuses);
        }
    }

    private void initializeShipmentStatuses() {
        if (shipmentStatusRepository.count() == 0) {
            List<String> statusNames = Arrays.asList(
                    "PENDING", "PROCESSING", "OUT_FOR_DELIVERY", "DELIVERED",
                    "FAILED_DELIVERY", "RETURNED", "CANCELLED", "ON_HOLD"
            );
            for (String name : statusNames) {
                ShipmentStatus status = new ShipmentStatus();
                status.setName(name);
                shipmentStatusRepository.save(status);
            }
        }
    }

    private void initializePayoutStatuses() {
        if (payoutStatusRepository.count() == 0) {
            List<String> statusNames = Arrays.asList("PENDING", "PROCESSED", "FAILED", "CANCELLED");
            for (String name : statusNames) {
                PayoutStatus status = new PayoutStatus();
                status.setName(name);
                payoutStatusRepository.save(status);
            }
        }
    }

    private void initializeZones() {
        if (zoneRepository.count() == 0) {
            System.out.println("🏗️ Initializing zones...");
            List<String> zoneNames = Arrays.asList("CAIRO", "GIZA", "ALEXANDRIA", "SHARQIA", "DAKAHLEIA");
            for (String name : zoneNames) {
                Zone zone = new Zone();
                zone.setName(name);
                zone.setDescription(name + " zone");
                zone.setStatus(ZoneStatus.ZONE_ACTIVE);
                zone.setDefaultFee(new java.math.BigDecimal("50.00")); // إضافة الرسوم الافتراضية
                zone.setCenterLatitude(new java.math.BigDecimal("30.0444")); // إضافة إحداثيات افتراضية
                zone.setCenterLongitude(new java.math.BigDecimal("31.2357"));
                zoneRepository.save(zone);
                System.out.println("✅ Created zone: " + name);
            }
            System.out.println("✅ Zones initialization completed!");
        } else {
            System.out.println("ℹ️ Zones already exist, skipping initialization");
        }
    }

    private void initializeTelemetrySettings() {
        if (telemetrySettingsRepository.count() == 0) {
            List<TelemetrySettings> settings = Arrays.asList(
                new TelemetrySettings("DEFAULT_SYSTEM_FEE", "50.00", "الرسوم الافتراضية للنظام"),
                new TelemetrySettings("MAX_WEIGHT_PER_SHIPMENT", "50.0", "الحد الأقصى للوزن لكل شحنة"),
                new TelemetrySettings("DEFAULT_PRIORITY", "STANDARD", "الأولوية الافتراضية للشحنات"),
                new TelemetrySettings("SYSTEM_NAME", "Twsela", "اسم النظام"),
                new TelemetrySettings("SUPPORT_PHONE", "01023782584", "رقم هاتف الدعم الفني"),
                new TelemetrySettings("SMS_ENABLED", "true", "تفعيل خدمة الرسائل النصية"),
                new TelemetrySettings("EMAIL_ENABLED", "false", "تفعيل خدمة البريد الإلكتروني"),
                new TelemetrySettings("AUTO_ASSIGN_COURIER", "false", "التعيين التلقائي للعاملين")
            );
            telemetrySettingsRepository.saveAll(settings);
        }
    }

    private void initializeUsers() {
        if (userRepository.count() == 0) {
            try {
                // Get roles and statuses
                Role ownerRole = roleRepository.findByName("OWNER")
                    .orElseThrow(() -> new RuntimeException("Role 'OWNER' not found during initialization"));
                Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Role 'ADMIN' not found during initialization"));
                UserStatus activeStatus = userStatusRepository.findByName("ACTIVE")
                    .orElseThrow(() -> new RuntimeException("UserStatus 'ACTIVE' not found during initialization"));

            // Create Owner
            User owner = new User();
            owner.setName("System Owner");
            owner.setPhone("01023782584");
            owner.setPassword(passwordEncoder.encode("150620KkZz@#$"));
            owner.setRole(ownerRole);
            owner.setStatus(activeStatus);
            owner.setIsDeleted(false);
            owner.setCreatedAt(Instant.now());
            owner.setUpdatedAt(Instant.now());
            userRepository.save(owner);

            // Create Admin
            User admin = new User();
            admin.setName("System Administrator");
            admin.setPhone("01023782585");
            admin.setPassword(passwordEncoder.encode("150620KkZz@#$"));
            admin.setRole(adminRole);
            admin.setStatus(activeStatus);
            admin.setIsDeleted(false);
            admin.setCreatedAt(Instant.now());
            admin.setUpdatedAt(Instant.now());
            userRepository.save(admin);

            } catch (Exception e) {
                System.err.println("❌ DataInitializer: Error initializing users: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to initialize users: " + e.getMessage(), e);
            }
        }
    }
}
