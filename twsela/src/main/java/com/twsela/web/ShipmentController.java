package com.twsela.web;

import com.twsela.domain.Shipment;
import com.twsela.domain.ShipmentStatus;
import com.twsela.domain.ShipmentStatusHistory;
import com.twsela.domain.User;
import com.twsela.domain.RecipientDetails;
import com.twsela.domain.Zone;
import static com.twsela.domain.ShipmentStatusConstants.*;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.ShipmentStatusHistoryRepository;
import com.twsela.repository.UserRepository;
import com.twsela.repository.ZoneRepository;
import com.twsela.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.twsela.web.dto.CreateShipmentRequest;
import com.twsela.web.dto.LocationUpdateRequest;
import com.twsela.web.dto.ReconcileRequest;
import jakarta.validation.Valid;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/shipments")
@Tag(name = "Shipments", description = "إدارة الشحنات والتتبع")
public class ShipmentController {

    private static final Logger log = LoggerFactory.getLogger(ShipmentController.class);

    private final ShipmentRepository shipmentRepository;
    private final ShipmentStatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;
    private final ShipmentService shipmentService;
    private final ZoneRepository zoneRepository;

    public ShipmentController(ShipmentRepository shipmentRepository,
                              ShipmentStatusHistoryRepository statusHistoryRepository,
                              UserRepository userRepository,
                              ShipmentService shipmentService,
                              ZoneRepository zoneRepository) {
        this.shipmentRepository = shipmentRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.userRepository = userRepository;
        this.shipmentService = shipmentService;
        this.zoneRepository = zoneRepository;
    }

    @Operation(
        summary = "الحصول على جميع الشحنات",
        description = "الحصول على قائمة الشحنات مع إمكانية التصفح والترتيب حسب دور المستخدم"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "تم الحصول على الشحنات بنجاح",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                    {
                        "success": true,
                        "shipments": [...],
                        "totalElements": 100,
                        "totalPages": 5,
                        "currentPage": 0,
                        "size": 20,
                        "first": true,
                        "last": false,
                        "numberOfElements": 20
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
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MERCHANT')")
    public ResponseEntity<Map<String, Object>> getAllShipments(
            Authentication authentication,
            @Parameter(description = "رقم الصفحة", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "حجم الصفحة", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "حقل الترتيب", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "اتجاه الترتيب", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {
            User currentUser = getCurrentUser(authentication);
            
            // Create pageable with sorting
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Shipment> shipmentPage;
            
            // Filter shipments based on user role with pagination
            String role = currentUser.getRole().getName();
            switch (role) {
                case "OWNER":
                case "ADMIN":
                    // OWNER and ADMIN can see all shipments with pagination
                    shipmentPage = shipmentRepository.findAll(pageable);
                    break;
                case "MERCHANT":
                    // MERCHANT can only see their own shipments with pagination
                    shipmentPage = shipmentRepository.findByMerchantId(currentUser.getId(), pageable);
                    break;
                default:
                    return ResponseEntity.status(403).build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("shipments", shipmentPage.getContent());
            response.put("totalElements", shipmentPage.getTotalElements());
            response.put("totalPages", shipmentPage.getTotalPages());
            response.put("currentPage", shipmentPage.getNumber());
            response.put("size", shipmentPage.getSize());
            response.put("first", shipmentPage.isFirst());
            response.put("last", shipmentPage.isLast());
            response.put("numberOfElements", shipmentPage.getNumberOfElements());
            
            return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "الحصول على شحنة بالمعرف",
        description = "الحصول على تفاصيل شحنة محددة باستخدام معرف الشحنة"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "تم الحصول على الشحنة بنجاح",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "الشحنة غير موجودة"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "خطأ في الخادم"
        )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MERCHANT', 'COURIER', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<Map<String, Object>> getShipmentById(
        @Parameter(description = "معرف الشحنة", example = "1", required = true)
        @PathVariable Long id) {
            Shipment shipment = shipmentRepository.findById(id).orElse(null);
            
            if (shipment == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", ErrorMessages.SHIPMENT_NOT_FOUND);
                return ResponseEntity.status(404).body(error);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("shipment", shipment);
            
            return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MERCHANT', 'COURIER', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<Map<String, Object>> getShipmentsCount() {
            long count = shipmentRepository.count();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", count);
            
            return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "إنشاء شحنة جديدة",
        description = "إنشاء شحنة جديدة مع جميع التفاصيل المطلوبة"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "تم إنشاء الشحنة بنجاح",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                    {
                        "success": true,
                        "message": "تم إنشاء الشحنة بنجاح",
                        "data": {
                            "id": 1,
                            "trackingNumber": "TS123456789",
                            "recipientName": "أحمد محمد",
                            "recipientPhone": "+201234567890",
                            "recipientAddress": "شارع التحرير، القاهرة",
                            "status": "PENDING",
                            "createdAt": "2024-01-15T10:30:00Z"
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "بيانات غير صحيحة - الحقول المطلوبة مفقودة"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "خطأ في الخادم"
        )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MERCHANT')")
    public ResponseEntity<Map<String, Object>> createShipment(
            @Parameter(description = "بيانات الشحنة الجديدة", required = true)
            @Valid @RequestBody CreateShipmentRequest request,
            Authentication authentication) {
            User currentUser = getCurrentUser(authentication);
            
            // Create shipment object
            Shipment shipment = new Shipment();
            
            // Set recipient details
            RecipientDetails recipientDetails = new RecipientDetails();
            recipientDetails.setName(request.getRecipientName());
            recipientDetails.setPhone(request.getRecipientPhone());
            recipientDetails.setAddress(request.getRecipientAddress());
            
            // Set alternate phone if provided
            if (request.getAlternatePhone() != null) {
                recipientDetails.setAlternatePhone(request.getAlternatePhone());
            }
            
            shipment.setRecipientDetails(recipientDetails);
            
            // Note: package description and weight are not stored in Shipment entity
            // They are stored in recipient notes or handled separately
            
            // Set item value and COD amount
            BigDecimal itemValue = BigDecimal.ZERO;
            if (request.getItemValue() != null) {
                itemValue = request.getItemValue();
            }
            shipment.setItemValue(itemValue);
            
            BigDecimal codAmount = itemValue;
            if (request.getCodAmount() != null) {
                codAmount = request.getCodAmount();
            }
            shipment.setCodAmount(codAmount);
            
            // Set zone
            Long zoneId = request.getZoneId();
            Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.ZONE_NOT_FOUND + zoneId));
            shipment.setZone(zone);
            
            // Note: priority is not stored in Shipment entity
            // It's used for fee calculation only
            
            // Set shipping fee paid by
            String shippingFeePaidBy = request.getShippingFeePaidBy();
            shipment.setShippingFeePaidBy(Shipment.ShippingFeePaidBy.valueOf(shippingFeePaidBy));
            
            // Set special instructions in recipient notes if provided
            if (request.getSpecialInstructions() != null) {
                shipment.setRecipientNotes(request.getSpecialInstructions());
            }
            
            // Set merchant
            shipment.setMerchant(currentUser);
            
            // Calculate delivery fee using weight from request and priority
            BigDecimal weight = request.getPackageWeight();
            String priority = request.getPriority();
            BigDecimal deliveryFee = calculateDeliveryFee(zone, weight, priority);
            shipment.setDeliveryFee(deliveryFee);
            
            // Set initial status
            ShipmentStatus pendingStatus = shipmentService.getStatusByName("PENDING")
                .orElseThrow(() -> new RuntimeException(ErrorMessages.STATUS_NOT_FOUND + "PENDING"));
            shipment.setStatus(pendingStatus);
            
            // Generate tracking number
            String trackingNumber = generateTrackingNumber();
            shipment.setTrackingNumber(trackingNumber);
            
            // Save shipment
            Shipment savedShipment = shipmentService.createShipment(shipment);
            
            // Add status history
            ShipmentStatusHistory history = new ShipmentStatusHistory();
            history.setShipment(savedShipment);
            history.setStatus(pendingStatus);
            history.setNotes("تم إنشاء الشحنة بواسطة " + currentUser.getName());
            statusHistoryRepository.save(history);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", ErrorMessages.SHIPMENT_CREATED);
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", savedShipment.getId());
            data.put("trackingNumber", savedShipment.getTrackingNumber());
            data.put("recipientName", savedShipment.getRecipientDetails().getName());
            data.put("recipientPhone", savedShipment.getRecipientDetails().getPhone());
            data.put("recipientAddress", savedShipment.getRecipientDetails().getAddress());
            data.put("alternatePhone", savedShipment.getRecipientDetails().getAlternatePhone());
            data.put("packageDescription", request.getPackageDescription());
            data.put("packageWeight", weight);
            data.put("itemValue", savedShipment.getItemValue());
            data.put("codAmount", savedShipment.getCodAmount());
            data.put("zoneName", savedShipment.getZone().getName());
            data.put("priority", priority);
            data.put("shippingFeePaidBy", savedShipment.getShippingFeePaidBy().name());
            data.put("shippingFee", savedShipment.getDeliveryFee());
            data.put("specialInstructions", savedShipment.getRecipientNotes());
            data.put("status", savedShipment.getStatus().getName());
            data.put("createdAt", savedShipment.getCreatedAt());
            
            response.put("data", data);
            
            return ResponseEntity.ok(response);
            
    }

    // ===== WAREHOUSE OPERATIONS (merged from WarehouseController) =====
    
    /**
     * Receive shipments from merchant
     * POST /api/shipments/warehouse/receive
     * // TODO: Wire ShipmentController to delegate to ShipmentService.receiveShipmentsAtWarehouse()
     */
    @PostMapping("/warehouse/receive")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('OWNER')")
    public ResponseEntity<?> receiveShipments(@RequestBody Map<String, List<String>> request) {
            List<String> trackingNumbers = request.get("trackingNumbers");
            if (trackingNumbers == null || trackingNumbers.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", ErrorMessages.NO_TRACKING_NUMBERS_PROVIDED));
            }

            List<Shipment> updatedShipments = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (String trackingNumber : trackingNumbers) {
                Optional<Shipment> shipmentOpt = shipmentRepository.findByTrackingNumber(trackingNumber);
                if (shipmentOpt.isPresent()) {
                    Shipment shipment = shipmentOpt.get();
                    
                    // Update status to RECEIVED_AT_HUB
                    ShipmentStatus receivedStatus = shipmentService.getStatusByName(RECEIVED_AT_HUB)
                        .orElseThrow(() -> new RuntimeException(ErrorMessages.STATUS_NOT_FOUND + RECEIVED_AT_HUB));
                    shipment.setStatus(receivedStatus);
                    shipment.setUpdatedAt(Instant.now());
                    shipmentRepository.save(shipment);

                    // Add status history entry
                    ShipmentStatusHistory history = new ShipmentStatusHistory();
                    history.setShipment(shipment);
                    history.setStatus(receivedStatus);
                    history.setNotes("Received at warehouse by manager");
                    statusHistoryRepository.save(history);

                    updatedShipments.add(shipment);
                } else {
                    errors.add("Shipment not found: " + trackingNumber);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("updatedShipments", updatedShipments.size());
            response.put("errors", errors);
            response.put("message", "Successfully received " + updatedShipments.size() + " shipments");

            return ResponseEntity.ok(response);
    }

    /**
     * View warehouse inventory
     * GET /api/shipments/warehouse/inventory
     */
    @GetMapping("/warehouse/inventory")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('OWNER')")
    public ResponseEntity<?> getInventory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            // Get shipments with RECEIVED_AT_HUB or RETURNED_TO_HUB status
            ShipmentStatus receivedStatus = shipmentService.getStatusByName(RECEIVED_AT_HUB)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.STATUS_NOT_FOUND + RECEIVED_AT_HUB));
            ShipmentStatus returnedStatus = shipmentService.getStatusByName(RETURNED_TO_HUB)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.STATUS_NOT_FOUND + RETURNED_TO_HUB));
            List<ShipmentStatus> warehouseStatuses = List.of(receivedStatus, returnedStatus);
            Page<Shipment> shipments = shipmentRepository.findByStatusIn(warehouseStatuses, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("shipments", shipments.getContent());
            response.put("currentPage", shipments.getNumber());
            response.put("totalPages", shipments.getTotalPages());
            response.put("totalElements", shipments.getTotalElements());
            response.put("size", shipments.getSize());

            return ResponseEntity.ok(response);
    }

    /**
     * Dispatch shipments to courier
     * POST /api/shipments/warehouse/dispatch/{courierId}
     * // TODO: Wire ShipmentController to delegate to ShipmentService.dispatchShipmentsToCourierFromWarehouse()
     */
    @PostMapping("/warehouse/dispatch/{courierId}")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('OWNER')")
    public ResponseEntity<?> dispatchToCourier(
            @PathVariable Long courierId,
            @RequestBody Map<String, List<Long>> request) {
            List<Long> shipmentIds = request.get("shipmentIds");
            if (shipmentIds == null || shipmentIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", ErrorMessages.NO_SHIPMENT_IDS_PROVIDED));
            }

            // Verify courier exists
            Optional<User> courierOpt = userRepository.findById(courierId);
            if (!courierOpt.isPresent() || !courierOpt.get().getRole().getName().equals("COURIER")) {
                return ResponseEntity.badRequest().body(Map.of("error", ErrorMessages.INVALID_COURIER_ID));
            }

            User courier = courierOpt.get();
            List<Shipment> updatedShipments = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (Long shipmentId : shipmentIds) {
                Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
                if (shipmentOpt.isPresent()) {
                    Shipment shipment = shipmentOpt.get();
                    
                    // Check if shipment is in warehouse
                    if (!shipment.getStatus().getName().equals(RECEIVED_AT_HUB) && 
                        !shipment.getStatus().getName().equals(RETURNED_TO_HUB)) {
                        errors.add("Shipment " + shipment.getTrackingNumber() + " is not in warehouse");
                        continue;
                    }

                    ShipmentStatus assignedStatus = shipmentService.getStatusByName(ASSIGNED_TO_COURIER)
                        .orElseThrow(() -> new RuntimeException(ErrorMessages.STATUS_NOT_FOUND + ASSIGNED_TO_COURIER));
                    shipment.setStatus(assignedStatus);
                    shipment.setCourier(courier);
                    shipment.setUpdatedAt(Instant.now());
                    shipmentRepository.save(shipment);

                    // Add status history entry
                    ShipmentStatusHistory history = new ShipmentStatusHistory();
                    history.setShipment(shipment);
                    history.setStatus(assignedStatus);
                    history.setNotes("Dispatched to courier: " + courier.getName());
                    statusHistoryRepository.save(history);

                    updatedShipments.add(shipment);
                } else {
                    errors.add("Shipment not found: " + shipmentId);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("updatedShipments", updatedShipments.size());
            response.put("errors", errors);
            response.put("courier", courier.getName());
            response.put("message", "Successfully dispatched " + updatedShipments.size() + " shipments to " + courier.getName());

            return ResponseEntity.ok(response);
    }

    /**
     * Reconcile with courier (End of Day)
     * POST /api/shipments/warehouse/reconcile/courier/{courierId}
     * // TODO: Wire ShipmentController to delegate to ShipmentService.reconcileWithCourier()
     */
    @PostMapping("/warehouse/reconcile/courier/{courierId}")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('OWNER')")
    public ResponseEntity<?> reconcileWithCourier(
            @PathVariable Long courierId,
            @Valid @RequestBody ReconcileRequest request) {
            List<Long> cashConfirmedShipmentIds = request.getCash_confirmed_shipment_ids();
            List<Long> returnedShipmentIds = request.getReturned_shipment_ids();

            if (cashConfirmedShipmentIds == null) cashConfirmedShipmentIds = new ArrayList<>();
            if (returnedShipmentIds == null) returnedShipmentIds = new ArrayList<>();

            // Verify courier exists
            Optional<User> courierOpt = userRepository.findById(courierId);
            if (!courierOpt.isPresent() || !courierOpt.get().getRole().getName().equals("COURIER")) {
                return ResponseEntity.badRequest().body(Map.of("error", ErrorMessages.INVALID_COURIER_ID));
            }

            User courier = courierOpt.get();
            List<Shipment> processedShipments = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            // Process cash confirmed shipments
            for (Long shipmentId : cashConfirmedShipmentIds) {
                Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
                if (shipmentOpt.isPresent()) {
                    Shipment shipment = shipmentOpt.get();
                    
                    // Verify shipment belongs to this courier
                    if (shipment.getCourier() == null || !shipment.getCourier().getId().equals(courierId)) {
                        errors.add("Shipment " + shipment.getTrackingNumber() + " does not belong to this courier");
                        continue;
                    }

                    // Log cash reconciliation (for now, just add a note)
                    ShipmentStatusHistory history = new ShipmentStatusHistory();
                    history.setShipment(shipment);
                    history.setStatus(shipment.getStatus());
                    history.setNotes("Cash reconciliation confirmed by warehouse manager");
                    statusHistoryRepository.save(history);

                    processedShipments.add(shipment);
                } else {
                    errors.add("Shipment not found: " + shipmentId);
                }
            }

            // Process returned shipments
            for (Long shipmentId : returnedShipmentIds) {
                Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
                if (shipmentOpt.isPresent()) {
                    Shipment shipment = shipmentOpt.get();
                    
                    // Verify shipment belongs to this courier
                    if (shipment.getCourier() == null || !shipment.getCourier().getId().equals(courierId)) {
                        errors.add("Shipment " + shipment.getTrackingNumber() + " does not belong to this courier");
                        continue;
                    }

                    ShipmentStatus returnedStatus = shipmentService.getStatusByName(RETURNED_TO_HUB)
                        .orElseThrow(() -> new RuntimeException(ErrorMessages.STATUS_NOT_FOUND + RETURNED_TO_HUB));
                    shipment.setStatus(returnedStatus);
                    shipment.setUpdatedAt(Instant.now());
                    shipmentRepository.save(shipment);

                    // Add status history entry
                    ShipmentStatusHistory history = new ShipmentStatusHistory();
                    history.setShipment(shipment);
                    history.setStatus(returnedStatus);
                    history.setNotes("Returned to warehouse from courier: " + courier.getName());
                    statusHistoryRepository.save(history);

                    processedShipments.add(shipment);
                } else {
                    errors.add("Shipment not found: " + shipmentId);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("processedShipments", processedShipments.size());
            response.put("cashConfirmed", cashConfirmedShipmentIds.size());
            response.put("returned", returnedShipmentIds.size());
            response.put("errors", errors);
            response.put("courier", courier.getName());
            response.put("message", "Successfully reconciled with " + courier.getName());

            return ResponseEntity.ok(response);
    }

    /**
     * Get couriers for dispatch
     * GET /api/shipments/warehouse/couriers
     */
    @GetMapping("/warehouse/couriers")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('OWNER')")
    public ResponseEntity<?> getCouriers() {
            // Get all couriers using the existing method
            List<User> couriers = userRepository.findByRoleName("COURIER");
            
            // Filter only active couriers
            List<User> activeCouriers = couriers.stream()
                .filter(courier -> courier.isActive())
                .collect(Collectors.toList());
            
            List<Map<String, Object>> courierList = activeCouriers.stream()
                .map(courier -> {
                    Map<String, Object> courierData = new HashMap<>();
                    courierData.put("id", courier.getId());
                    courierData.put("name", courier.getName());
                    courierData.put("phone", courier.getPhone());
                    return courierData;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("couriers", courierList));
    }

    /**
     * Get courier's shipments for reconciliation
     * GET /api/shipments/warehouse/courier/{courierId}/shipments
     */
    @GetMapping("/warehouse/courier/{courierId}/shipments")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('OWNER')")
    public ResponseEntity<?> getCourierShipments(@PathVariable Long courierId) {
            // Verify courier exists
            Optional<User> courierOpt = userRepository.findById(courierId);
            if (!courierOpt.isPresent() || !courierOpt.get().getRole().getName().equals("COURIER")) {
                return ResponseEntity.badRequest().body(Map.of("error", ErrorMessages.INVALID_COURIER_ID));
            }

            // Get all shipments assigned to this courier (not just today's)
            ShipmentStatus assignedStatus = shipmentService.getStatusByName(ASSIGNED_TO_COURIER)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.STATUS_NOT_FOUND + ASSIGNED_TO_COURIER));
            ShipmentStatus outForDeliveryStatus = shipmentService.getStatusByName(OUT_FOR_DELIVERY)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.STATUS_NOT_FOUND + OUT_FOR_DELIVERY));
            ShipmentStatus deliveredStatus = shipmentService.getStatusByName(DELIVERED)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.STATUS_NOT_FOUND + DELIVERED));
            ShipmentStatus failedAttemptStatus = shipmentService.getStatusByName(FAILED_ATTEMPT)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.STATUS_NOT_FOUND + FAILED_ATTEMPT));
            ShipmentStatus returnedToHubStatus = shipmentService.getStatusByName(RETURNED_TO_HUB)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.STATUS_NOT_FOUND + RETURNED_TO_HUB));
            
            List<ShipmentStatus> relevantStatuses = List.of(
                assignedStatus,
                outForDeliveryStatus,
                deliveredStatus,
                failedAttemptStatus,
                returnedToHubStatus
            );

            // Get all shipments for this courier with relevant statuses
            List<Shipment> shipments = shipmentRepository.findByCourierIdAndStatusIn(courierId, relevantStatuses);

            Map<String, Object> response = new HashMap<>();
            response.put("courier", courierOpt.get().getName());
            response.put("shipments", shipments);
            response.put("total", shipments.size());

            return ResponseEntity.ok(response);
    }

    /**
     * Get warehouse statistics
     * GET /api/shipments/warehouse/stats
     */
    @GetMapping("/warehouse/stats")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('OWNER')")
    public ResponseEntity<?> getWarehouseStats() {
            Instant startOfDay = Instant.now().atZone(java.time.ZoneId.systemDefault()).withHour(0).withMinute(0).withSecond(0).toInstant();
            Instant endOfDay = Instant.now().atZone(java.time.ZoneId.systemDefault()).withHour(23).withMinute(59).withSecond(59).toInstant();

            // Count shipments received today
            ShipmentStatus receivedStatus = shipmentService.getStatusByName(RECEIVED_AT_HUB)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.STATUS_NOT_FOUND + RECEIVED_AT_HUB));
            long receivedToday = shipmentRepository.countByStatusAndUpdatedAtBetween(
                receivedStatus, startOfDay, endOfDay
            );

            // Count shipments dispatched today
            ShipmentStatus assignedStatus = shipmentService.getStatusByName(ASSIGNED_TO_COURIER)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.STATUS_NOT_FOUND + ASSIGNED_TO_COURIER));
            long dispatchedToday = shipmentRepository.countByStatusAndUpdatedAtBetween(
                assignedStatus, startOfDay, endOfDay
            );

            // Count current inventory
            ShipmentStatus returnedStatus = shipmentService.getStatusByName(RETURNED_TO_HUB)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.STATUS_NOT_FOUND + RETURNED_TO_HUB));
            long currentInventory = shipmentRepository.countByStatusIn(
                Arrays.asList(receivedStatus, returnedStatus)
            );

            // Count pending returns
            long pendingReturns = shipmentRepository.countByStatus(returnedStatus);

            Map<String, Object> stats = new HashMap<>();
            stats.put("receivedToday", receivedToday);
            stats.put("dispatchedToday", dispatchedToday);
            stats.put("currentInventory", currentInventory);
            stats.put("pendingReturns", pendingReturns);

            return ResponseEntity.ok(stats);
    }

    /**
     * Request return to origin (RTO) for a shipment
     * POST /api/shipments/{id}/return-request
     */
    @PostMapping("/{id}/return-request")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MERCHANT', 'COURIER', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<Map<String, Object>> requestReturnToOrigin(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
            String reason = request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", ErrorMessages.RETURN_REASON_REQUIRED);
                return ResponseEntity.badRequest().body(error);
            }

            // Find the original shipment
            Shipment originalShipment = shipmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException(ErrorMessages.SHIPMENT_NOT_FOUND));

            // Check if shipment is eligible for return
            if (!isEligibleForReturn(originalShipment)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", ErrorMessages.SHIPMENT_NOT_ELIGIBLE_FOR_RETURN);
                return ResponseEntity.badRequest().body(error);
            }

            // Create return shipment using the service
            Shipment returnShipment = shipmentService.createReturnShipment(originalShipment, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", ErrorMessages.RETURN_REQUEST_CREATED);
            response.put("originalShipmentId", originalShipment.getId());
            response.put("returnShipmentId", returnShipment.getId());
            response.put("returnTrackingNumber", returnShipment.getTrackingNumber());
            response.put("reason", reason);

            return ResponseEntity.ok(response);
    }

    /**
     * Update courier location (Web-only)
     * PUT /api/shipments/courier/location/update
     */
    @PutMapping("/courier/location/update")
    @PreAuthorize("hasRole('COURIER')")
    public ResponseEntity<Map<String, Object>> updateCourierLocation(
            @Valid @RequestBody LocationUpdateRequest request,
            Authentication authentication) {
            Double latitude = request.getLatitude();
            Double longitude = request.getLongitude();

            // Get current courier from security context
            User courier = getCurrentUser(authentication);
            Long courierId = courier.getId();

            // Update courier location using the service
            shipmentService.updateCourierLocation(courierId, latitude, longitude);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", ErrorMessages.LOCATION_UPDATED);
            response.put("latitude", latitude);
            response.put("longitude", longitude);
            response.put("timestamp", Instant.now());

            return ResponseEntity.ok(response);
    }

    private boolean isEligibleForReturn(Shipment shipment) {
        // Check if shipment is in a state that allows return
        String statusName = shipment.getStatus().getName();
        return !statusName.equals(DELIVERED) && 
               !statusName.equals(CANCELLED) && 
               !statusName.equals(RETURNED_TO_ORIGIN);
    }
    
    /**
     * Calculate delivery fee based on zone, weight, and priority.
     * // TODO: Delegate to ShipmentService.calculateDeliveryFeeByPriority()
     */
    private BigDecimal calculateDeliveryFee(Zone zone, BigDecimal weight, String priority) {
        // Get default fee for the zone
        BigDecimal defaultFee = zone.getDefaultFee() != null ? zone.getDefaultFee() : new BigDecimal("50.00");
        
        // For now, use default fee as base fee
        // In a real implementation, you would have more complex pricing logic
        BigDecimal baseFee = defaultFee;
        
        // Apply priority multiplier
        BigDecimal multiplier = BigDecimal.ONE;
        switch (priority) {
            case "EXPRESS":
                multiplier = new BigDecimal("1.5");
                break;
            case "STANDARD":
                multiplier = BigDecimal.ONE;
                break;
            case "ECONOMY":
                multiplier = new BigDecimal("0.8");
                break;
        }
        
        return baseFee.multiply(multiplier);
    }
    
    /**
     * Generate unique tracking number using SecureRandom.
     * // TODO: Delegate to ShipmentService.generateTrackingNumber()
     */
    private String generateTrackingNumber() {
        java.security.SecureRandom secureRandom = new java.security.SecureRandom();
        String prefix = "TS";
        long timestamp = System.currentTimeMillis();
        int random = secureRandom.nextInt(900000) + 100000; // 6 digits
        String trackingNumber = prefix + timestamp + random;
        
        // Ensure uniqueness by checking database
        while (shipmentRepository.findByTrackingNumber(trackingNumber).isPresent()) {
            random = secureRandom.nextInt(900000) + 100000;
            trackingNumber = prefix + timestamp + random;
        }
        
        return trackingNumber;
    }
    
    /**
     * Helper method to get current user from authentication
     */
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException(ErrorMessages.USER_NOT_AUTHENTICATED);
        }
        
        String phone = authentication.getName();
        return userRepository.findByPhone(phone)
            .orElseThrow(() -> new RuntimeException(ErrorMessages.USER_NOT_FOUND));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MERCHANT', 'COURIER')")
    public ResponseEntity<Map<String, Object>> getShipments(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Long courierId,
            @RequestParam(required = false) String deliveryDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
            User currentUser = getCurrentUser(authentication);
            String role = currentUser.getRole().getName();
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            
            Page<Shipment> shipmentPage;
            
            switch (role) {
                case "MERCHANT":
                    shipmentPage = shipmentRepository.findByMerchantId(currentUser.getId(), pageable);
                    break;
                case "COURIER":
                    shipmentPage = shipmentRepository.findByCourierId(currentUser.getId(), pageable);
                    break;
                default: // OWNER, ADMIN
                    if (courierId != null) {
                        shipmentPage = shipmentRepository.findByCourierId(courierId, pageable);
                    } else {
                        shipmentPage = shipmentRepository.findAll(pageable);
                    }
                    break;
            }
            
            response.put("success", true);
            response.put("data", shipmentPage.getContent());
            response.put("message", ErrorMessages.SHIPMENTS_RETRIEVED);
            response.put("count", shipmentPage.getTotalElements());
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", shipmentPage.getTotalPages());
            
            return ResponseEntity.ok(response);
    }
}