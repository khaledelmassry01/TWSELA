package com.twsela.service;

import com.twsela.domain.OptimizedRoute;
import com.twsela.domain.Shipment;
import com.twsela.repository.OptimizedRouteRepository;
import com.twsela.service.RouteOptimizationService.Waypoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("تحسين المسارات - RouteOptimizationService")
class RouteOptimizationServiceTest {

    @Mock private OptimizedRouteRepository routeRepository;

    @InjectMocks private RouteOptimizationService service;

    @Test
    @DisplayName("الجار الأقرب — يجب اختيار النقطة الأقرب أولاً")
    void nearestNeighborSelectsClosestFirst() {
        var waypoints = List.of(
                new Waypoint(1L, 30.1, 31.3),   // far
                new Waypoint(2L, 30.05, 31.24),  // close
                new Waypoint(3L, 30.2, 31.5)     // farthest
        );

        List<Waypoint> route = service.nearestNeighbor(waypoints, 30.04, 31.23);

        assertThat(route.get(0).getShipmentId()).isEqualTo(2L); // closest first
    }

    @Test
    @DisplayName("2-opt — يجب تحسين المسار")
    void twoOptImprovesRoute() {
        // Create a route that crosses itself (sub-optimal)
        var route = new ArrayList<>(List.of(
                new Waypoint(1L, 30.0, 31.0),
                new Waypoint(2L, 30.1, 31.2),
                new Waypoint(3L, 30.0, 31.2),
                new Waypoint(4L, 30.1, 31.0)
        ));

        double distBefore = service.calculateTotalDistance(route, 30.0, 31.0);
        List<Waypoint> improved = service.twoOptImprove(route);
        double distAfter = service.calculateTotalDistance(improved, 30.0, 31.0);

        assertThat(distAfter).isLessThanOrEqualTo(distBefore);
    }

    @Test
    @DisplayName("حساب المسافة الكلية — 3 نقاط")
    void calculateTotalDistance() {
        var route = List.of(
                new Waypoint(1L, 30.0444, 31.2357),
                new Waypoint(2L, 30.05, 31.24),
                new Waypoint(3L, 30.06, 31.25)
        );

        double total = service.calculateTotalDistance(route, 30.04, 31.23);

        assertThat(total).isGreaterThan(0);
    }

    @Test
    @DisplayName("تحسين مسار — قائمة فارغة")
    void optimizeRouteEmpty() {
        when(routeRepository.save(any(OptimizedRoute.class))).thenAnswer(inv -> {
            OptimizedRoute r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        OptimizedRoute result = service.optimizeRoute(1L, null, List.of(), 30.0, 31.0);

        assertThat(result.getWaypoints()).isEqualTo("[]");
        assertThat(result.getTotalDistanceKm()).isEqualTo(0);
    }

    @Test
    @DisplayName("تحسين مسار — شحنة واحدة")
    void optimizeRouteSingleShipment() {
        Shipment s = new Shipment();
        s.setId(10L);
        s.setDeliveryLatitude(BigDecimal.valueOf(30.05));
        s.setDeliveryLongitude(BigDecimal.valueOf(31.24));

        when(routeRepository.save(any(OptimizedRoute.class))).thenAnswer(inv -> {
            OptimizedRoute r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        OptimizedRoute result = service.optimizeRoute(1L, null, List.of(s), 30.04, 31.23);

        assertThat(result.getWaypoints()).contains("\"shipmentId\":10");
        assertThat(result.getTotalDistanceKm()).isGreaterThan(0);
    }

    @Test
    @DisplayName("تحسين مسار — شحنات متعددة")
    void optimizeRouteMultipleShipments() {
        List<Shipment> shipments = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Shipment s = new Shipment();
            s.setId((long) (i + 1));
            s.setDeliveryLatitude(BigDecimal.valueOf(30.0 + i * 0.02));
            s.setDeliveryLongitude(BigDecimal.valueOf(31.2 + i * 0.01));
            shipments.add(s);
        }

        when(routeRepository.save(any(OptimizedRoute.class))).thenAnswer(inv -> {
            OptimizedRoute r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        OptimizedRoute result = service.optimizeRoute(1L, 10L, shipments, 30.0, 31.2);

        assertThat(result.getWaypoints()).isNotEqualTo("[]");
        assertThat(result.getEstimatedDurationMinutes()).isGreaterThan(0);
        assertThat(result.getManifestId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("جلب المسار الحالي لمندوب")
    void getCurrentRoute() {
        OptimizedRoute route = new OptimizedRoute();
        route.setId(1L);
        route.setCourierId(5L);
        when(routeRepository.findTopByCourierIdOrderByOptimizedAtDesc(5L))
                .thenReturn(Optional.of(route));

        Optional<OptimizedRoute> result = service.getCurrentRoute(5L);

        assertThat(result).isPresent();
        assertThat(result.get().getCourierId()).isEqualTo(5L);
    }
}
