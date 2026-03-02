package com.twsela.web;

import com.twsela.domain.OptimizedRoute;
import com.twsela.domain.Shipment;
import com.twsela.domain.CourierLocationHistory;
import com.twsela.repository.CourierLocationHistoryRepository;
import com.twsela.repository.ShipmentRepository;
import com.twsela.service.RouteOptimizationService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.AssignmentDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for route optimization.
 */
@RestController
@RequestMapping("/api/routes")
@Tag(name = "Routes", description = "تحسين مسارات التوصيل")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public class RouteController {

    private final RouteOptimizationService routeService;
    private final ShipmentRepository shipmentRepository;
    private final CourierLocationHistoryRepository locationRepository;

    public RouteController(RouteOptimizationService routeService,
                            ShipmentRepository shipmentRepository,
                            CourierLocationHistoryRepository locationRepository) {
        this.routeService = routeService;
        this.shipmentRepository = shipmentRepository;
        this.locationRepository = locationRepository;
    }

    @Operation(summary = "تحسين مسار مندوب")
    @PostMapping("/optimize/{courierId}")
    public ResponseEntity<ApiResponse<OptimizedRouteResponse>> optimizeRoute(
            @PathVariable Long courierId,
            @RequestBody OptimizeRouteRequest request) {

        List<Shipment> shipments = shipmentRepository.findAllById(request.shipmentIds());

        // Get courier's latest location as start point
        double startLat = 30.0444; // Cairo default
        double startLng = 31.2357;
        List<CourierLocationHistory> locations = locationRepository
                .findByCourierIdOrderByTimestampDesc(courierId);
        if (!locations.isEmpty()) {
            startLat = locations.get(0).getLatitude().doubleValue();
            startLng = locations.get(0).getLongitude().doubleValue();
        }

        OptimizedRoute route = routeService.optimizeRoute(courierId, null, shipments, startLat, startLng);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(route), "تم تحسين المسار"));
    }

    @Operation(summary = "المسار الحالي لمندوب")
    @GetMapping("/{courierId}")
    public ResponseEntity<ApiResponse<OptimizedRouteResponse>> getCurrentRoute(
            @PathVariable Long courierId) {
        OptimizedRoute route = routeService.getCurrentRoute(courierId)
                .orElse(null);
        if (route == null) {
            return ResponseEntity.ok(ApiResponse.ok(null, "لا يوجد مسار حالي"));
        }
        return ResponseEntity.ok(ApiResponse.ok(toResponse(route), "المسار الحالي"));
    }

    private OptimizedRouteResponse toResponse(OptimizedRoute r) {
        return new OptimizedRouteResponse(
                r.getId(), r.getCourierId(), r.getManifestId(),
                r.getWaypoints(), r.getTotalDistanceKm(),
                r.getEstimatedDurationMinutes(), r.getOptimizedAt());
    }
}
