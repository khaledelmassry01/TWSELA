package com.twsela.web;

import com.twsela.domain.Shipment;
import com.twsela.domain.ShipmentStatusConstants;
import com.twsela.domain.User;
import com.twsela.repository.ShipmentRepository;
import com.twsela.service.CourierLocationService;
import com.twsela.web.dto.LocationDTO;
import com.twsela.web.dto.TrackingResponseDTO;
import com.twsela.web.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Enhanced public tracking controller.
 * Provides shipment tracking with status timeline, courier location, and ETA.
 */
@RestController
@RequestMapping("/api/public/tracking")
@Tag(name = "Public Tracking", description = "تتبع الشحنة العام — بدون تسجيل دخول")
public class PublicTrackingController {

    private static final Logger log = LoggerFactory.getLogger(PublicTrackingController.class);

    private final ShipmentRepository shipmentRepository;
    private final CourierLocationService locationService;

    public PublicTrackingController(ShipmentRepository shipmentRepository,
                                    CourierLocationService locationService) {
        this.shipmentRepository = shipmentRepository;
        this.locationService = locationService;
    }

    /**
     * Full tracking info: status timeline, courier location (if in transit), and ETA.
     */
    @Operation(summary = "تتبع الشحنة", description = "بيانات التتبع الكاملة مع الموقع وتقدير الوصول")
    @GetMapping("/{trackingNumber}")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<TrackingResponseDTO>> trackShipment(
            @PathVariable String trackingNumber) {
        if (trackingNumber == null || trackingNumber.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(com.twsela.web.dto.ApiResponse.error("رقم التتبع مطلوب"));
        }

        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "trackingNumber", trackingNumber));

        TrackingResponseDTO dto = buildTrackingResponse(shipment);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(dto));
    }

    /**
     * ETA-only endpoint for lightweight polling.
     */
    @Operation(summary = "تقدير وقت الوصول", description = "تقدير الوقت المتبقي لوصول الشحنة بالدقائق")
    @GetMapping("/{trackingNumber}/eta")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<Long>> getETA(
            @PathVariable String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "trackingNumber", trackingNumber));

        // Only calculate ETA for in-transit shipments
        String status = shipment.getStatus() != null ? shipment.getStatus().getName() : "";
        if (!isInTransit(status)) {
            return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(null, "الشحنة ليست في الطريق حالياً"));
        }

        Long eta = locationService.calculateETA(shipment);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(eta));
    }

    // ── Private helpers ────────────────────────────────────────

    private TrackingResponseDTO buildTrackingResponse(Shipment shipment) {
        TrackingResponseDTO dto = new TrackingResponseDTO();
        dto.setTrackingNumber(shipment.getTrackingNumber());
        dto.setCurrentStatus(shipment.getStatus() != null ? shipment.getStatus().getName() : null);

        // Status timeline — sorted ascending (oldest first)
        if (shipment.getStatusHistory() != null) {
            List<TrackingResponseDTO.StatusTimelineEntry> timeline = shipment.getStatusHistory().stream()
                    .sorted(Comparator.comparing(h -> h.getCreatedAt()))
                    .map(h -> new TrackingResponseDTO.StatusTimelineEntry(
                            h.getStatus() != null ? h.getStatus().getName() : null,
                            h.getNotes(),
                            h.getCreatedAt()
                    ))
                    .collect(Collectors.toList());
            dto.setStatusTimeline(timeline);
        }

        // Courier info + location only when in transit
        String status = dto.getCurrentStatus();
        if (status != null && isInTransit(status)) {
            User courier = shipment.getCourier();
            if (courier != null) {
                dto.setCourierName(courier.getName());
                Optional<LocationDTO> loc = locationService.getLastLocation(courier.getId());
                loc.ifPresent(dto::setLastCourierLocation);
            }
            // ETA
            dto.setEstimatedMinutesToDelivery(locationService.calculateETA(shipment));
        }

        // POD type
        if (shipment.getPodType() != null) {
            dto.setPodType(shipment.getPodType().name());
        }

        return dto;
    }

    private boolean isInTransit(String status) {
        return ShipmentStatusConstants.IN_TRANSIT.equals(status)
                || ShipmentStatusConstants.OUT_FOR_DELIVERY.equals(status)
                || ShipmentStatusConstants.ASSIGNED_TO_COURIER.equals(status);
    }
}
