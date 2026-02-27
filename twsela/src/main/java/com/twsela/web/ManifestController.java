package com.twsela.web;

import com.twsela.domain.*;
import static com.twsela.domain.ShipmentStatusConstants.*;
import com.twsela.repository.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Unified Manifest Controller for managing shipment manifests
 * Replaces role-specific manifest endpoints with generic ones that filter by user role
 */
@RestController
@RequestMapping("/api/manifests")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('COURIER')")
public class ManifestController {

    @Autowired
    private ShipmentManifestRepository shipmentManifestRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ShipmentRepository shipmentRepository;

    @GetMapping
    public ResponseEntity<List<ShipmentManifest>> getAllManifests(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<ShipmentManifest> manifests;
        
        String role = currentUser.getRole().getName();
        
        switch (role) {
            case "OWNER":
            case "ADMIN":
                // OWNER and ADMIN can see all manifests
                manifests = shipmentManifestRepository.findAll();
                break;
            case "COURIER":
                // COURIER can only see their own manifests
                manifests = shipmentManifestRepository.findByCourierId(currentUser.getId());
                break;
            default:
                return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(manifests);
    }

    @PostMapping
    public ResponseEntity<ShipmentManifest> createManifest(@Valid @RequestBody CreateManifestRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        
        // Only OWNER and ADMIN can create manifests
        if (!currentUser.getRole().getName().equals("OWNER") && !currentUser.getRole().getName().equals("ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        
        // Verify courier exists
        User courier = userRepository.findById(request.courierId).orElse(null);
        if (courier == null || !courier.getRole().getName().equals("COURIER")) {
            return ResponseEntity.badRequest().build();
        }
        
        ShipmentManifest manifest = new ShipmentManifest();
        manifest.setCourier(courier);
        manifest.setStatus(ShipmentManifest.ManifestStatus.CREATED);
        manifest.setCreatedAt(java.time.Instant.now());
        
        ShipmentManifest createdManifest = shipmentManifestRepository.save(manifest);
        return ResponseEntity.ok(createdManifest);
    }

    @GetMapping("/{manifestId}")
    public ResponseEntity<ShipmentManifest> getManifestById(@PathVariable Long manifestId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        ShipmentManifest manifest = shipmentManifestRepository.findById(manifestId).orElse(null);
        
        if (manifest == null) {
            return ResponseEntity.notFound().build();
        }
        
        String role = currentUser.getRole().getName();
        
        // Check access permissions
        if (role.equals("COURIER") && !manifest.getCourier().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(manifest);
    }

    @PostMapping("/{manifestId}/shipments")
    public ResponseEntity<ShipmentManifest> assignShipmentsToManifest(
            @PathVariable Long manifestId,
            @RequestBody List<Long> shipmentIds,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        
        // Only OWNER and ADMIN can assign shipments to manifests
        if (!currentUser.getRole().getName().equals("OWNER") && !currentUser.getRole().getName().equals("ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        
        ShipmentManifest manifest = shipmentManifestRepository.findById(manifestId).orElse(null);
        if (manifest == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Assign shipments to manifest
        for (Long shipmentId : shipmentIds) {
            Shipment shipment = shipmentRepository.findById(shipmentId).orElse(null);
            if (shipment != null) {
                shipment.setManifest(manifest);
                shipmentRepository.save(shipment);
            }
        }
        
        // Refresh manifest
        manifest = shipmentManifestRepository.findById(manifestId).orElse(null);
        return ResponseEntity.ok(manifest);
    }

    @PutMapping("/{manifestId}/status")
    public ResponseEntity<ShipmentManifest> updateManifestStatus(
            @PathVariable Long manifestId,
            @RequestParam String status,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        ShipmentManifest manifest = shipmentManifestRepository.findById(manifestId).orElse(null);
        
        if (manifest == null) {
            return ResponseEntity.notFound().build();
        }
        
        String role = currentUser.getRole().getName();
        
        // Check access permissions
        if (role.equals("COURIER") && !manifest.getCourier().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        // Update status
        manifest.setStatus(ShipmentManifest.ManifestStatus.valueOf(status));
        manifest = shipmentManifestRepository.save(manifest);
        
        return ResponseEntity.ok(manifest);
    }
    
    /**
     * Assign shipments to manifest using tracking numbers
     * POST /api/manifests/{manifestId}/assign
     */
    @PostMapping("/{manifestId}/assign")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<?> assignShipmentsToManifestByTrackingNumbers(
            @PathVariable Long manifestId,
            @RequestBody Map<String, List<String>> request,
            Authentication authentication) {
        
        try {
            // Verify manifest exists
            ShipmentManifest manifest = shipmentManifestRepository.findById(manifestId)
                    .orElseThrow(() -> new RuntimeException("المانيفست غير موجود"));
            
            List<String> trackingNumbers = request.get("trackingNumbers");
            if (trackingNumbers == null || trackingNumbers.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "أرقام التتبع مطلوبة"
                ));
            }
            
            List<Shipment> assignedShipments = new java.util.ArrayList<>();
            List<String> errors = new java.util.ArrayList<>();
            
            for (String trackingNumber : trackingNumbers) {
                try {
                    Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                            .orElseThrow(() -> new RuntimeException("الشحنة غير موجودة: " + trackingNumber));
                    
                    // Check if shipment is eligible for assignment
                    if (shipment.getManifest() != null) {
                        errors.add("الشحنة " + trackingNumber + " مُعينة بالفعل لمانيفست آخر");
                        continue;
                    }
                    
                    // Check shipment status
                    if (!isEligibleForManifestAssignment(shipment)) {
                        errors.add("الشحنة " + trackingNumber + " غير مؤهلة للتعيين");
                        continue;
                    }
                    
                    // Assign shipment to manifest
                    shipment.setManifest(manifest);
                    shipment.setUpdatedAt(java.time.Instant.now());
                    shipmentRepository.save(shipment);
                    
                    assignedShipments.add(shipment);
                    
                } catch (Exception e) {
                    errors.add("خطأ في الشحنة " + trackingNumber + ": " + e.getMessage());
                }
            }
            
            // Update manifest status if shipments were assigned
            if (!assignedShipments.isEmpty()) {
                manifest.setStatus(ShipmentManifest.ManifestStatus.IN_PROGRESS);
                manifest.setAssignedAt(java.time.Instant.now());
                shipmentManifestRepository.save(manifest);
            }
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("message", "تم تعيين " + assignedShipments.size() + " شحنة للمانيفست");
            response.put("assignedShipments", assignedShipments.size());
            response.put("errors", errors);
            response.put("manifestId", manifestId);
            response.put("manifestNumber", manifest.getManifestNumber());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "حدث خطأ أثناء تعيين الشحنات: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    private boolean isEligibleForManifestAssignment(Shipment shipment) {
        String statusName = shipment.getStatus().getName();
        return statusName.equals(APPROVED) || 
               statusName.equals(RECEIVED_AT_HUB) || 
               statusName.equals(READY_FOR_DISPATCH);
    }

    private User getCurrentUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    public static class CreateManifestRequest {
        @NotNull(message = "معرف المندوب مطلوب")
        public Long courierId;
        
        public CreateManifestRequest() {}
        
        public CreateManifestRequest(Long courierId) {
            this.courierId = courierId;
        }
    }
}
