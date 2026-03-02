package com.twsela.service;

import com.twsela.domain.OptimizedRoute;
import com.twsela.domain.Shipment;
import com.twsela.repository.OptimizedRouteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Route optimization service using Nearest-Neighbor heuristic + 2-opt improvement.
 */
@Service
@Transactional
public class RouteOptimizationService {

    private static final Logger log = LoggerFactory.getLogger(RouteOptimizationService.class);
    private static final double AVG_SPEED_KMH = 30.0; // average delivery speed in city

    private final OptimizedRouteRepository routeRepository;

    public RouteOptimizationService(OptimizedRouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    /**
     * Waypoint data holder for route optimization.
     */
    public static class Waypoint {
        private final Long shipmentId;
        private final double lat;
        private final double lng;

        public Waypoint(Long shipmentId, double lat, double lng) {
            this.shipmentId = shipmentId;
            this.lat = lat;
            this.lng = lng;
        }

        public Long getShipmentId() { return shipmentId; }
        public double getLat() { return lat; }
        public double getLng() { return lng; }
    }

    /**
     * Optimize a route for a set of shipments using nearest-neighbor + 2-opt.
     */
    public OptimizedRoute optimizeRoute(Long courierId, Long manifestId, List<Shipment> shipments,
                                         double startLat, double startLng) {
        if (shipments.isEmpty()) {
            return createEmptyRoute(courierId, manifestId);
        }

        // Convert to waypoints
        List<Waypoint> waypoints = new ArrayList<>();
        for (Shipment s : shipments) {
            if (s.getDeliveryLatitude() != null && s.getDeliveryLongitude() != null) {
                waypoints.add(new Waypoint(s.getId(),
                        s.getDeliveryLatitude().doubleValue(),
                        s.getDeliveryLongitude().doubleValue()));
            }
        }

        if (waypoints.isEmpty()) {
            return createEmptyRoute(courierId, manifestId);
        }

        // Step 1: Nearest Neighbor heuristic
        List<Waypoint> route = nearestNeighbor(waypoints, startLat, startLng);

        // Step 2: 2-opt improvement
        route = twoOptImprove(route);

        // Step 3: Calculate metrics
        double totalDistance = calculateTotalDistance(route, startLat, startLng);
        int estimatedMinutes = (int) Math.ceil((totalDistance / AVG_SPEED_KMH) * 60);

        // Build waypoints JSON
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < route.size(); i++) {
            Waypoint w = route.get(i);
            if (i > 0) json.append(",");
            json.append(String.format(Locale.US,
                    "{\"shipmentId\":%d,\"lat\":%.8f,\"lng\":%.8f,\"order\":%d}",
                    w.getShipmentId(), w.getLat(), w.getLng(), i + 1));
        }
        json.append("]");

        OptimizedRoute optimized = new OptimizedRoute();
        optimized.setCourierId(courierId);
        optimized.setManifestId(manifestId);
        optimized.setWaypoints(json.toString());
        optimized.setTotalDistanceKm(Math.round(totalDistance * 100.0) / 100.0);
        optimized.setEstimatedDurationMinutes(estimatedMinutes);
        optimized.setOptimizedAt(Instant.now());

        log.info("Route optimized for courier {}: {} waypoints, {:.1f} km, ~{} min",
                courierId, route.size(), totalDistance, estimatedMinutes);
        return routeRepository.save(optimized);
    }

    /**
     * Get the current (latest) optimized route for a courier.
     */
    @Transactional(readOnly = true)
    public Optional<OptimizedRoute> getCurrentRoute(Long courierId) {
        return routeRepository.findTopByCourierIdOrderByOptimizedAtDesc(courierId);
    }

    /**
     * Get route for a specific manifest.
     */
    @Transactional(readOnly = true)
    public Optional<OptimizedRoute> getRouteForManifest(Long manifestId) {
        return routeRepository.findByManifestId(manifestId);
    }

    // ══════════════════════════════════════════════════════════
    // Nearest Neighbor Heuristic
    // ══════════════════════════════════════════════════════════

    List<Waypoint> nearestNeighbor(List<Waypoint> waypoints, double startLat, double startLng) {
        List<Waypoint> remaining = new ArrayList<>(waypoints);
        List<Waypoint> route = new ArrayList<>();
        double curLat = startLat;
        double curLng = startLng;

        while (!remaining.isEmpty()) {
            Waypoint nearest = null;
            double minDist = Double.MAX_VALUE;
            for (Waypoint w : remaining) {
                double d = SmartAssignmentService.haversineKm(curLat, curLng, w.getLat(), w.getLng());
                if (d < minDist) {
                    minDist = d;
                    nearest = w;
                }
            }
            route.add(nearest);
            curLat = nearest.getLat();
            curLng = nearest.getLng();
            remaining.remove(nearest);
        }
        return route;
    }

    // ══════════════════════════════════════════════════════════
    // 2-opt Improvement
    // ══════════════════════════════════════════════════════════

    List<Waypoint> twoOptImprove(List<Waypoint> route) {
        if (route.size() < 4) return route;

        boolean improved = true;
        List<Waypoint> best = new ArrayList<>(route);

        while (improved) {
            improved = false;
            for (int i = 0; i < best.size() - 1; i++) {
                for (int j = i + 2; j < best.size(); j++) {
                    double d1 = segmentDistance(best, i, i + 1) + segmentDistance(best, j, (j + 1) % best.size());
                    double d2 = segmentDistance(best, i, j) + segmentDistance(best, i + 1, (j + 1) % best.size());
                    if (d2 < d1 - 1e-10) {
                        // Reverse the segment between i+1 and j
                        Collections.reverse(best.subList(i + 1, j + 1));
                        improved = true;
                    }
                }
            }
        }
        return best;
    }

    private double segmentDistance(List<Waypoint> route, int i, int j) {
        Waypoint a = route.get(i % route.size());
        Waypoint b = route.get(j % route.size());
        return SmartAssignmentService.haversineKm(a.getLat(), a.getLng(), b.getLat(), b.getLng());
    }

    // ══════════════════════════════════════════════════════════
    // Distance Calculation
    // ══════════════════════════════════════════════════════════

    double calculateTotalDistance(List<Waypoint> route, double startLat, double startLng) {
        if (route.isEmpty()) return 0.0;

        double total = SmartAssignmentService.haversineKm(startLat, startLng,
                route.get(0).getLat(), route.get(0).getLng());
        for (int i = 0; i < route.size() - 1; i++) {
            total += SmartAssignmentService.haversineKm(
                    route.get(i).getLat(), route.get(i).getLng(),
                    route.get(i + 1).getLat(), route.get(i + 1).getLng());
        }
        return total;
    }

    private OptimizedRoute createEmptyRoute(Long courierId, Long manifestId) {
        OptimizedRoute r = new OptimizedRoute();
        r.setCourierId(courierId);
        r.setManifestId(manifestId);
        r.setWaypoints("[]");
        r.setTotalDistanceKm(0);
        r.setEstimatedDurationMinutes(0);
        return routeRepository.save(r);
    }
}
