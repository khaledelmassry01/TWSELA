package com.twsela.service;

import com.twsela.domain.*;
import static com.twsela.domain.ShipmentStatusConstants.*;
import com.twsela.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ShipmentService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentService.class);

    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;
    private final CourierZoneRepository courierZoneRepository;
    private final ShipmentStatusHistoryRepository shipmentStatusHistoryRepository;
    private final ShipmentStatusRepository shipmentStatusRepository;
    private final DeliveryPricingRepository deliveryPricingRepository;
    private final RecipientDetailsRepository recipientDetailsRepository;
    private final ShipmentManifestRepository shipmentManifestRepository;
    private final TelemetrySettingsRepository telemetrySettingsRepository;
    private final CourierLocationHistoryRepository courierLocationHistoryRepository;
    private final ReturnShipmentRepository returnShipmentRepository;

    public ShipmentService(ShipmentRepository shipmentRepository, UserRepository userRepository, 
                          ZoneRepository zoneRepository, CourierZoneRepository courierZoneRepository,
                          ShipmentStatusHistoryRepository shipmentStatusHistoryRepository, 
                          ShipmentStatusRepository shipmentStatusRepository,
                          DeliveryPricingRepository deliveryPricingRepository,
                          RecipientDetailsRepository recipientDetailsRepository,
                          ShipmentManifestRepository shipmentManifestRepository,
                          TelemetrySettingsRepository telemetrySettingsRepository,
                          CourierLocationHistoryRepository courierLocationHistoryRepository,
                          ReturnShipmentRepository returnShipmentRepository) {
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
        this.zoneRepository = zoneRepository;
        this.courierZoneRepository = courierZoneRepository;
        this.shipmentStatusHistoryRepository = shipmentStatusHistoryRepository;
        this.shipmentStatusRepository = shipmentStatusRepository;
        this.deliveryPricingRepository = deliveryPricingRepository;
        this.recipientDetailsRepository = recipientDetailsRepository;
        this.shipmentManifestRepository = shipmentManifestRepository;
        this.telemetrySettingsRepository = telemetrySettingsRepository;
        this.courierLocationHistoryRepository = courierLocationHistoryRepository;
        this.returnShipmentRepository = returnShipmentRepository;
    }

    // Legacy method - redirects to unified method
    public Shipment createShipment(Long merchantId, Long zoneId, Shipment shipment) {
        return createShipment(shipment, merchantId, zoneId);
    }

    public Shipment createShipmentFromExcel(Long merchantId, Shipment shipment) {
        User merchant = userRepository.findById(merchantId).orElseThrow();
        
        // Find zone by name
        Zone zone = zoneRepository.findByNameIgnoreCase(shipment.getZone().getName()).orElseThrow();
        
        // Create or find recipient details
        RecipientDetails recipientDetails = createOrFindRecipientDetails(
            shipment.getRecipientDetails().getPhone(),
            shipment.getRecipientDetails().getName(),
            shipment.getRecipientDetails().getAddress()
        );
        
        shipment.setMerchant(merchant);
        shipment.setZone(zone);
        shipment.setRecipientDetails(recipientDetails);
        
        // Calculate delivery fee automatically
        calculateDeliveryFee(merchantId, zone.getId(), shipment);
        
        if (shipment.getTrackingNumber() == null || shipment.getTrackingNumber().isBlank()) {
            shipment.setTrackingNumber(generateTrackingNumber());
        }
        
        // Set default values
        if (shipment.getItemValue() == null) {
            shipment.setItemValue(BigDecimal.ZERO);
        }
        if (shipment.getCodAmount() == null) {
            shipment.setCodAmount(BigDecimal.ZERO);
        }
        if (shipment.getSourceType() == null) {
            shipment.setSourceType(Shipment.SourceType.MERCHANT);
        }
        if (shipment.getShippingFeePaidBy() == null) {
            shipment.setShippingFeePaidBy(Shipment.ShippingFeePaidBy.MERCHANT);
        }
        
        // Set initial status
        ShipmentStatus pendingStatus = shipmentStatusRepository.findByName(PENDING_APPROVAL)
            .orElseThrow(() -> new RuntimeException("PENDING_APPROVAL status not found"));
        shipment.setStatus(pendingStatus);
        
        Shipment savedShipment = shipmentRepository.save(shipment);
        
        // Create status history
        createStatusHistory(savedShipment, pendingStatus, "Shipment created from Excel");
        
        return savedShipment;
    }

    public Shipment findByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber).orElse(null);
    }
    
    public Optional<ShipmentStatus> getStatusByName(String name) {
        return shipmentStatusRepository.findByName(name);
    }

    public List<Shipment> findByTrackingNumbers(List<String> trackingNumbers) {
        return shipmentRepository.findByTrackingNumberIn(trackingNumbers);
    }

    private RecipientDetails createOrFindRecipientDetails(String phone, String name, String address) {
        return recipientDetailsRepository.findByPhone(phone)
            .orElseGet(() -> {
                RecipientDetails details = new RecipientDetails(phone, name, address);
                return recipientDetailsRepository.save(details);
            });
    }

    private void calculateDeliveryFee(Long merchantId, Long zoneId, Shipment shipment) {
        BigDecimal deliveryFee = null;
        
        // التسلسل الهرمي للتسعير (Pricing Hierarchy):
        // (أ) Merchant-Zone Price ➡️ (ب) Zone Default Fee ➡️ (ج) System Default Fee
        
        // (أ) أولوية أولى: البحث عن تسعير خاص للتاجر والمنطقة
        var merchantZonePricing = deliveryPricingRepository.findByMerchantIdAndZoneId(merchantId, zoneId);
        if (merchantZonePricing.isPresent() && merchantZonePricing.get().getIsActive()) {
            deliveryFee = merchantZonePricing.get().getDeliveryFee();
        } else {
            // (ب) أولوية ثانية: استخدام الرسوم الافتراضية للمنطقة
            Zone zone = zoneRepository.findById(zoneId).orElse(null);
            if (zone != null && zone.getDefaultFee() != null) {
                deliveryFee = zone.getDefaultFee();
            } else {
                // (ج) أولوية ثالثة: استخدام الرسوم الافتراضية للنظام
                var systemDefaultFee = telemetrySettingsRepository.findBySettingKey("DEFAULT_DELIVERY_FEE");
                if (systemDefaultFee.isPresent()) {
                    try {
                        deliveryFee = new BigDecimal(systemDefaultFee.get().getSettingValue());
                    } catch (NumberFormatException e) {
                        deliveryFee = BigDecimal.valueOf(50.00); // قيمة احتياطية
                    }
                } else {
                    deliveryFee = BigDecimal.valueOf(50.00); // قيمة افتراضية نهائية
                }
            }
        }
        
        // تعيين الرسوم المحسوبة للشحنة
        shipment.setDeliveryFee(deliveryFee);
    }

    private void createStatusHistory(Shipment shipment, ShipmentStatus status, String notes) {
        ShipmentStatusHistory history = new ShipmentStatusHistory(shipment, status, notes);
        shipmentStatusHistoryRepository.save(history);
    }

    public List<Shipment> getManifestForCourier(Long courierId) {
        return shipmentRepository.findByCourierId(courierId);
    }

    public List<Shipment> assignShipmentsToCourier(Long courierId, int count) {
        User courier = userRepository.findById(courierId).orElseThrow(() -> new RuntimeException("Courier not found"));
        
        // Find shipments without courier assignment
        List<Shipment> allShipments = shipmentRepository.findAll();
        List<Shipment> unassignedShipments = allShipments.stream()
                .filter(shipment -> shipment.getManifest() == null)
                .toList();
        
        // Take only the requested count
        List<Shipment> shipmentsToAssign = unassignedShipments.stream()
                .limit(count)
                .toList();
        
        // Create manifest for courier
        ShipmentManifest manifest = new ShipmentManifest(courier, generateManifestNumber());
        manifest = shipmentManifestRepository.save(manifest);
        
        // Assign manifest to shipments
        ShipmentStatus assignedStatus = shipmentStatusRepository.findByName(ASSIGNED_TO_COURIER).orElseThrow();
        for (Shipment shipment : shipmentsToAssign) {
            shipment.setManifest(manifest);
            shipment.setStatus(assignedStatus);
            createStatusHistory(shipment, assignedStatus, "Assigned to courier");
        }
        
        return shipmentRepository.saveAll(shipmentsToAssign);
    }

    // Unified status update method
    public Shipment updateStatus(String trackingNumber, ShipmentStatus status, String reason) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + trackingNumber));
        
        // Handle failed delivery scenarios
        if (FAILED_ATTEMPT.equals(status.getName()) && reason != null) {
            ShipmentStatus specificStatus = determineSpecificFailedStatus(reason);
            shipment.setStatus(specificStatus);
        } else {
            shipment.setStatus(status);
        }
        
        shipment.setUpdatedAt(Instant.now());
        
        // Set delivered timestamp if status is DELIVERED
        if (DELIVERED.equals(status.getName())) {
            shipment.setDeliveredAt(Instant.now());
        }
        
        // Create status history entry
        String historyNote = reason != null ? "Reason: " + reason : "Status updated";
        createStatusHistory(shipment, shipment.getStatus(), historyNote);
        
        return shipmentRepository.save(shipment);
    }
    
    // Overloaded method for backward compatibility
    public Shipment updateStatus(String trackingNumber, ShipmentStatus status) {
        return updateStatus(trackingNumber, status, null);
    }
    
    // Legacy method - redirects to unified method
    public Shipment updateStatusWithReason(String trackingNumber, String statusName, String reason) {
        ShipmentStatus status = shipmentStatusRepository.findByName(statusName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid status: " + statusName));
        return updateStatus(trackingNumber, status, reason);
    }

    public List<Shipment> getPendingShipmentsForCourier(Long courierId) {
        // Get courier's assigned zones
        List<CourierZone> courierZones = courierZoneRepository.findByCourierId(courierId);
        List<Long> zoneIds = courierZones.stream()
                .map(cz -> cz.getId().getZoneId())
                .toList();
        
        if (zoneIds.isEmpty()) {
            return List.of(); // No zones assigned to courier
        }
        
        // Find shipments with APPROVED status in courier's zones
        ShipmentStatus approvedStatus = shipmentStatusRepository.findByName(APPROVED).orElseThrow();
        return shipmentRepository.findByStatusAndZoneIdIn(approvedStatus, zoneIds);
    }

    @Transactional
    public void dispatchShipmentsToCourier(Long courierId, List<Long> shipmentIds) {
        User courier = userRepository.findById(courierId)
                .orElseThrow(() -> new RuntimeException("Courier not found"));
        
        // Create manifest for courier
        ShipmentManifest manifest = new ShipmentManifest(courier, generateManifestNumber());
        manifest = shipmentManifestRepository.save(manifest);
        
        for (Long shipmentId : shipmentIds) {
            Shipment shipment = shipmentRepository.findById(shipmentId)
                    .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));
            
            // Verify shipment status is APPROVED
            if (!shipment.getStatus().getName().equals(APPROVED)) {
                throw new RuntimeException("Shipment " + shipment.getTrackingNumber() + " is not in APPROVED status");
            }
            
            // Update shipment
            shipment.setManifest(manifest);
            ShipmentStatus assignedStatus = shipmentStatusRepository.findByName(ASSIGNED_TO_COURIER).orElseThrow();
            shipment.setStatus(assignedStatus);
            shipment.setUpdatedAt(Instant.now());
            
            // Create status history record
            createStatusHistory(shipment, assignedStatus, "Dispatched by owner");
            
            shipmentRepository.save(shipment);
        }
    }

    public List<Shipment> getRecentActivity(int limit) {
        return shipmentRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt"))
                .stream()
                .limit(limit)
                .toList();
    }

    private String generateTrackingNumber() {
        return "TWS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String generateManifestNumber() {
        return "MAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public Optional<Shipment> getShipmentById(Long id) {
        return shipmentRepository.findById(id);
    }

    public Shipment updateShipment(Long id, Shipment shipmentDetails) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found with id: " + id));
        
        // Update fields
        if (shipmentDetails.getRecipientDetails() != null) {
            RecipientDetails recipientDetails = shipment.getRecipientDetails();
            if (shipmentDetails.getRecipientDetails().getName() != null) {
                recipientDetails.setName(shipmentDetails.getRecipientDetails().getName());
            }
            if (shipmentDetails.getRecipientDetails().getPhone() != null) {
                recipientDetails.setPhone(shipmentDetails.getRecipientDetails().getPhone());
            }
            if (shipmentDetails.getRecipientDetails().getAddress() != null) {
                recipientDetails.setAddress(shipmentDetails.getRecipientDetails().getAddress());
            }
        }
        
        if (shipmentDetails.getCodAmount() != null) {
            shipment.setCodAmount(shipmentDetails.getCodAmount());
        }
        if (shipmentDetails.getDeliveryFee() != null) {
            shipment.setDeliveryFee(shipmentDetails.getDeliveryFee());
        }
        if (shipmentDetails.getPodType() != null) {
            shipment.setPodType(shipmentDetails.getPodType());
        }
        if (shipmentDetails.getRecipientNotes() != null) {
            shipment.setRecipientNotes(shipmentDetails.getRecipientNotes());
        }
        
        shipment.setUpdatedAt(Instant.now());
        return shipmentRepository.save(shipment);
    }

    public void deleteShipment(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found with id: " + id));
        
        // Delete associated status history first
        shipmentStatusHistoryRepository.deleteByShipment(shipment);
        
        // Delete the shipment
        shipmentRepository.delete(shipment);
    }

    public Shipment uploadPodImage(String trackingNumber, String imagePath) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + trackingNumber));
        
        // Update POD information
        shipment.setPodType(Shipment.PodType.PHOTO);
        shipment.setPodData(imagePath);
        ShipmentStatus deliveredStatus = shipmentStatusRepository.findByName(DELIVERED).orElseThrow();
        shipment.setStatus(deliveredStatus);
        shipment.setDeliveredAt(Instant.now());
        shipment.setUpdatedAt(Instant.now());
        
        // Create status history entry
        createStatusHistory(shipment, deliveredStatus, "Delivered with photo POD");
        
        return shipmentRepository.save(shipment);
    }

    private ShipmentStatus determineSpecificFailedStatus(String reason) {
        String lowerReason = reason.toLowerCase();
        
        if (lowerReason.contains("reschedule") || lowerReason.contains("postpone") || 
            lowerReason.contains("later") || lowerReason.contains("tomorrow")) {
            return shipmentStatusRepository.findByName(POSTPONED).orElseThrow();
        } else if (lowerReason.contains("address") || lowerReason.contains("phone") || 
                   lowerReason.contains("update") || lowerReason.contains("wrong")) {
            return shipmentStatusRepository.findByName(PENDING_UPDATE).orElseThrow();
        } else if (lowerReason.contains("return") || lowerReason.contains("refuse") || 
                   lowerReason.contains("reject") || lowerReason.contains("back")) {
            return shipmentStatusRepository.findByName(PENDING_RETURN).orElseThrow();
        } else {
            // Default to POSTPONED for unknown reasons
            return shipmentStatusRepository.findByName(POSTPONED).orElseThrow();
        }
    }

    // Get all shipments with pagination and filtering
    public Map<String, Object> getAllShipments(int page, int size, String status, String search) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Shipment> shipments;
            
            if (status != null && !status.isEmpty()) {
                ShipmentStatus shipmentStatus = shipmentStatusRepository.findByName(status).orElse(null);
                if (shipmentStatus != null) {
                    shipments = shipmentRepository.findByStatus(shipmentStatus, pageable);
                } else {
                    shipments = shipmentRepository.findAll(pageable);
                }
            } else if (search != null && !search.isEmpty()) {
                shipments = shipmentRepository.findByTrackingNumberContainingIgnoreCaseOrRecipientNameContainingIgnoreCaseOrRecipientPhoneContainingIgnoreCase(
                    search, search, search, pageable);
            } else {
                shipments = shipmentRepository.findAll(pageable);
            }
            
            return Map.of(
                "content", shipments.getContent(),
                "totalElements", shipments.getTotalElements(),
                "totalPages", shipments.getTotalPages(),
                "currentPage", shipments.getNumber(),
                "size", shipments.getSize(),
                "first", shipments.isFirst(),
                "last", shipments.isLast()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch shipments: " + e.getMessage(), e);
        }
    }

    // Find shipment by ID
    public Shipment findById(Long id) {
        return shipmentRepository.findById(id).orElse(null);
    }

    // Unified create shipment method
    public Shipment createShipment(Shipment shipment, Long merchantId, Long zoneId) {
        try {
            // Set merchant and zone if provided
            if (merchantId != null) {
                User merchant = userRepository.findById(merchantId).orElseThrow(() -> new RuntimeException("Merchant not found"));
                shipment.setMerchant(merchant);
            }
            
            if (zoneId != null) {
                Zone zone = zoneRepository.findById(zoneId).orElseThrow(() -> new RuntimeException("Zone not found"));
                shipment.setZone(zone);
            }
            
            // Create or find recipient details if provided
            if (shipment.getRecipientDetails() != null) {
                RecipientDetails recipientDetails = createOrFindRecipientDetails(
                    shipment.getRecipientDetails().getPhone(),
                    shipment.getRecipientDetails().getName(),
                    shipment.getRecipientDetails().getAddress()
                );
                shipment.setRecipientDetails(recipientDetails);
            }
            
            // Calculate delivery fee if merchant and zone are provided
            if (merchantId != null && zoneId != null) {
                calculateDeliveryFee(merchantId, zoneId, shipment);
            }
            
            // Generate tracking number if not provided
            if (shipment.getTrackingNumber() == null || shipment.getTrackingNumber().isEmpty()) {
                shipment.setTrackingNumber(generateTrackingNumber());
            }
            
            // Set default values
            if (shipment.getItemValue() == null) {
                shipment.setItemValue(BigDecimal.ZERO);
            }
            if (shipment.getCodAmount() == null) {
                shipment.setCodAmount(BigDecimal.ZERO);
            }
            if (shipment.getSourceType() == null) {
                shipment.setSourceType(Shipment.SourceType.MERCHANT);
            }
            if (shipment.getShippingFeePaidBy() == null) {
                shipment.setShippingFeePaidBy(Shipment.ShippingFeePaidBy.MERCHANT);
            }
            
            // Set initial status
            ShipmentStatus pendingStatus = shipmentStatusRepository.findByName(PENDING_APPROVAL)
                .orElse(shipmentStatusRepository.findByName(PENDING)
                    .orElseThrow(() -> new RuntimeException("PENDING status not found")));
            shipment.setStatus(pendingStatus);
            
            // Set timestamps
            shipment.setCreatedAt(Instant.now());
            shipment.setUpdatedAt(Instant.now());
            
            Shipment savedShipment = shipmentRepository.save(shipment);
            
            // Create status history
            createStatusHistory(savedShipment, pendingStatus, "Shipment created");
            
            return savedShipment;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create shipment: " + e.getMessage(), e);
        }
    }
    
    // Overloaded method for backward compatibility
    public Shipment createShipment(Shipment shipment) {
        return createShipment(shipment, null, null);
    }

    // ===== SHIPMENT STATUS MANAGEMENT (merged from ShipmentStatusService) =====
    
    public List<ShipmentStatus> getAllStatuses() {
        return shipmentStatusRepository.findAll();
    }
    
    public Optional<ShipmentStatus> getStatusById(Long id) {
        return shipmentStatusRepository.findById(id);
    }
    
    public ShipmentStatus createStatus(String name, String description) {
        if (shipmentStatusRepository.existsByName(name)) {
            throw new IllegalArgumentException("Status with name '" + name + "' already exists");
        }
        
        ShipmentStatus status = new ShipmentStatus(name, description);
        return shipmentStatusRepository.save(status);
    }
    
    public ShipmentStatus updateStatus(Long id, String name, String description) {
        ShipmentStatus status = shipmentStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Status not found with id: " + id));
        
        if (!status.getName().equals(name) && shipmentStatusRepository.existsByName(name)) {
            throw new IllegalArgumentException("Status with name '" + name + "' already exists");
        }
        
        status.setName(name);
        status.setDescription(description);
        return shipmentStatusRepository.save(status);
    }
    
    public void deleteStatus(Long id) {
        if (!shipmentStatusRepository.existsById(id)) {
            throw new IllegalArgumentException("Status not found with id: " + id);
        }
        
        shipmentStatusRepository.deleteById(id);
    }
    
    public boolean statusExistsByName(String name) {
        return shipmentStatusRepository.existsByName(name);
    }
    
    // ===== RETURN TO ORIGIN (RTO) FUNCTIONALITY =====
    
    /**
     * Create a return shipment for the given original shipment
     * This method handles the complete RTO process including:
     * 1. Updating original shipment status
     * 2. Creating new return shipment
     * 3. Recording the relationship in return_shipments table
     */
    @Transactional
    public Shipment createReturnShipment(Shipment originalShipment, String reason) {
        try {
            // 1. Update original shipment status to RETURNED_TO_ORIGIN
            ShipmentStatus returnedStatus = shipmentStatusRepository.findByName(RETURNED_TO_ORIGIN)
                    .orElseThrow(() -> new RuntimeException("RETURNED_TO_ORIGIN status not found"));
            
            originalShipment.setStatus(returnedStatus);
            originalShipment.setUpdatedAt(Instant.now());
            shipmentRepository.save(originalShipment);
            
            // Create status history for original shipment
            createStatusHistory(originalShipment, returnedStatus, "Return requested: " + reason);
            
            // 2. Create new return shipment
            Shipment returnShipment = new Shipment();
            returnShipment.setTrackingNumber(generateTrackingNumber());
            returnShipment.setMerchant(originalShipment.getMerchant());
            returnShipment.setZone(originalShipment.getZone());
            returnShipment.setRecipientDetails(originalShipment.getRecipientDetails());
            returnShipment.setItemValue(originalShipment.getItemValue());
            returnShipment.setCodAmount(originalShipment.getCodAmount());
            returnShipment.setDeliveryFee(originalShipment.getDeliveryFee());
            returnShipment.setSourceType(Shipment.SourceType.MERCHANT);
            returnShipment.setShippingFeePaidBy(Shipment.ShippingFeePaidBy.MERCHANT);
            returnShipment.setCashReconciled(false);
            
            // Set initial status for return shipment
            ShipmentStatus pendingStatus = shipmentStatusRepository.findByName(PENDING_APPROVAL)
                    .orElse(shipmentStatusRepository.findByName(PENDING)
                            .orElseThrow(() -> new RuntimeException("PENDING status not found")));
            returnShipment.setStatus(pendingStatus);
            
            // Set timestamps
            returnShipment.setCreatedAt(Instant.now());
            returnShipment.setUpdatedAt(Instant.now());
            
            Shipment savedReturnShipment = shipmentRepository.save(returnShipment);
            
            // Create status history for return shipment
            createStatusHistory(savedReturnShipment, pendingStatus, "Return shipment created for: " + originalShipment.getTrackingNumber());
            
            // 3. Record the relationship in return_shipments table
            ReturnShipment returnRecord = new ReturnShipment(originalShipment, savedReturnShipment, reason);
            returnShipmentRepository.save(returnRecord);
            
            return savedReturnShipment;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create return shipment: " + e.getMessage(), e);
        }
    }
    
    // ===== COURIER LOCATION UPDATE =====
    
    /**
     * Update courier location (Web-only)
     * This method records the courier's current location for tracking purposes
     */
    @Transactional
    public void updateCourierLocation(Long courierId, Double latitude, Double longitude) {
        try {
            // Verify courier exists
            User courier = userRepository.findById(courierId)
                    .orElseThrow(() -> new RuntimeException("Courier not found"));
            
            if (!courier.getRole().getName().equals("COURIER")) {
                throw new RuntimeException("User is not a courier");
            }
            
            // Create location history entry
            CourierLocationHistory locationHistory = new CourierLocationHistory();
            locationHistory.setCourier(courier);
            locationHistory.setLatitude(BigDecimal.valueOf(latitude));
            locationHistory.setLongitude(BigDecimal.valueOf(longitude));
            locationHistory.setTimestamp(Instant.now());
            
            // Save location history
            courierLocationHistoryRepository.save(locationHistory);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to update courier location: " + e.getMessage(), e);
        }
    }

    // ===== EXTRACTED BUSINESS LOGIC FROM ShipmentController =====
    // TODO: Wire ShipmentController to delegate to these methods

    /**
     * Calculate delivery fee based on zone, weight, and priority.
     * Extracted from ShipmentController — uses priority multiplier on zone default fee.
     *
     * @param zone     the delivery zone (contains defaultFee)
     * @param weight   package weight (reserved for future weight-based pricing)
     * @param priority one of "EXPRESS", "STANDARD", "ECONOMY"
     * @return calculated delivery fee
     */
    public BigDecimal calculateDeliveryFeeByPriority(Zone zone, BigDecimal weight, String priority) {
        BigDecimal defaultFee = zone.getDefaultFee() != null ? zone.getDefaultFee() : new BigDecimal("50.00");
        BigDecimal baseFee = defaultFee;

        BigDecimal multiplier = BigDecimal.ONE;
        switch (priority != null ? priority : "STANDARD") {
            case "EXPRESS":
                multiplier = new BigDecimal("1.5");
                break;
            case "ECONOMY":
                multiplier = new BigDecimal("0.8");
                break;
            case "STANDARD":
            default:
                multiplier = BigDecimal.ONE;
                break;
        }

        return baseFee.multiply(multiplier);
    }

    /**
     * Check whether a shipment is eligible for Return-To-Origin (RTO).
     * A shipment cannot be returned if it is already DELIVERED, CANCELLED, or RETURNED_TO_ORIGIN.
     *
     * @param shipment the shipment to check
     * @return true if the shipment may be returned
     */
    public boolean isEligibleForReturn(Shipment shipment) {
        String statusName = shipment.getStatus().getName();
        return !statusName.equals(DELIVERED)
            && !statusName.equals(CANCELLED)
            && !statusName.equals(RETURNED_TO_ORIGIN);
    }

    /**
     * Receive shipments at warehouse from merchant by tracking numbers.
     * Extracted from ShipmentController warehouse/receive endpoint.
     *
     * @param trackingNumbers list of tracking numbers to receive
     * @return map with updatedShipments count and any errors
     */
    @Transactional
    public Map<String, Object> receiveShipmentsAtWarehouse(List<String> trackingNumbers) {
        List<Shipment> updatedShipments = new java.util.ArrayList<>();
        List<String> errors = new java.util.ArrayList<>();

        ShipmentStatus receivedStatus = shipmentStatusRepository.findByName(RECEIVED_AT_HUB)
                .orElseThrow(() -> new RuntimeException("RECEIVED_AT_HUB status not found"));

        for (String trackingNumber : trackingNumbers) {
            Optional<Shipment> shipmentOpt = shipmentRepository.findByTrackingNumber(trackingNumber);
            if (shipmentOpt.isPresent()) {
                Shipment shipment = shipmentOpt.get();
                shipment.setStatus(receivedStatus);
                shipment.setUpdatedAt(Instant.now());
                shipmentRepository.save(shipment);
                createStatusHistory(shipment, receivedStatus, "Received at warehouse by manager");
                updatedShipments.add(shipment);
            } else {
                errors.add("Shipment not found: " + trackingNumber);
            }
        }

        return Map.of(
            "updatedShipments", updatedShipments.size(),
            "errors", errors
        );
    }

    /**
     * Dispatch shipments from warehouse to a courier.
     * Extracted from ShipmentController warehouse/dispatch endpoint.
     *
     * @param courierId   ID of the courier to dispatch to
     * @param shipmentIds list of shipment IDs to dispatch
     * @return map with results including count and errors
     */
    @Transactional
    public Map<String, Object> dispatchShipmentsToCourierFromWarehouse(Long courierId, List<Long> shipmentIds) {
        User courier = userRepository.findById(courierId)
                .orElseThrow(() -> new RuntimeException("Courier not found"));

        if (!courier.getRole().getName().equals("COURIER")) {
            throw new IllegalArgumentException("User is not a courier");
        }

        List<Shipment> updatedShipments = new java.util.ArrayList<>();
        List<String> errors = new java.util.ArrayList<>();

        ShipmentStatus assignedStatus = shipmentStatusRepository.findByName(ASSIGNED_TO_COURIER)
                .orElseThrow(() -> new RuntimeException("ASSIGNED_TO_COURIER status not found"));

        for (Long shipmentId : shipmentIds) {
            Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
            if (shipmentOpt.isPresent()) {
                Shipment shipment = shipmentOpt.get();
                String statusName = shipment.getStatus().getName();
                if (!statusName.equals(RECEIVED_AT_HUB) && !statusName.equals(RETURNED_TO_HUB)) {
                    errors.add("Shipment " + shipment.getTrackingNumber() + " is not in warehouse");
                    continue;
                }
                shipment.setStatus(assignedStatus);
                shipment.setUpdatedAt(Instant.now());
                shipmentRepository.save(shipment);
                createStatusHistory(shipment, assignedStatus, "Dispatched to courier: " + courier.getName());
                updatedShipments.add(shipment);
            } else {
                errors.add("Shipment not found: " + shipmentId);
            }
        }

        return Map.of(
            "updatedShipments", updatedShipments.size(),
            "courier", courier.getName(),
            "errors", errors
        );
    }

    /**
     * Reconcile shipments with a courier at end-of-day.
     * Extracted from ShipmentController warehouse/reconcile endpoint.
     *
     * @param courierId                  the courier ID
     * @param cashConfirmedShipmentIds   IDs of shipments whose cash is confirmed
     * @param returnedShipmentIds        IDs of shipments returned to hub
     * @return map with processed counts and errors
     */
    @Transactional
    public Map<String, Object> reconcileWithCourier(Long courierId,
                                                     List<Long> cashConfirmedShipmentIds,
                                                     List<Long> returnedShipmentIds) {
        User courier = userRepository.findById(courierId)
                .orElseThrow(() -> new RuntimeException("Courier not found"));

        List<Shipment> processedShipments = new java.util.ArrayList<>();
        List<String> errors = new java.util.ArrayList<>();

        // Process cash confirmed shipments
        for (Long shipmentId : cashConfirmedShipmentIds) {
            Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
            if (shipmentOpt.isPresent()) {
                Shipment shipment = shipmentOpt.get();
                if (shipment.getCourier() == null || !shipment.getCourier().getId().equals(courierId)) {
                    errors.add("Shipment " + shipment.getTrackingNumber() + " does not belong to this courier");
                    continue;
                }
                createStatusHistory(shipment, shipment.getStatus(), "Cash reconciliation confirmed by warehouse manager");
                processedShipments.add(shipment);
            } else {
                errors.add("Shipment not found: " + shipmentId);
            }
        }

        // Process returned shipments
        ShipmentStatus returnedStatus = shipmentStatusRepository.findByName(RETURNED_TO_HUB)
                .orElseThrow(() -> new RuntimeException("RETURNED_TO_HUB status not found"));

        for (Long shipmentId : returnedShipmentIds) {
            Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
            if (shipmentOpt.isPresent()) {
                Shipment shipment = shipmentOpt.get();
                if (shipment.getCourier() == null || !shipment.getCourier().getId().equals(courierId)) {
                    errors.add("Shipment " + shipment.getTrackingNumber() + " does not belong to this courier");
                    continue;
                }
                shipment.setStatus(returnedStatus);
                shipment.setUpdatedAt(Instant.now());
                shipmentRepository.save(shipment);
                createStatusHistory(shipment, returnedStatus, "Returned to warehouse from courier: " + courier.getName());
                processedShipments.add(shipment);
            } else {
                errors.add("Shipment not found: " + shipmentId);
            }
        }

        return Map.of(
            "processedShipments", processedShipments.size(),
            "cashConfirmed", cashConfirmedShipmentIds.size(),
            "returned", returnedShipmentIds.size(),
            "courier", courier.getName(),
            "errors", errors
        );
    }
}