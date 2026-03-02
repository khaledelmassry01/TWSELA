package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.domain.VehicleAssignment.AssignmentStatus;
import com.twsela.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("محرك التوزيع الذكي - SmartAssignmentService")
class SmartAssignmentServiceTest {

    @Mock private AssignmentRuleRepository ruleRepository;
    @Mock private AssignmentScoreRepository scoreRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private CourierLocationHistoryRepository locationRepository;
    @Mock private CourierZoneRepository courierZoneRepository;
    @Mock private CourierRatingRepository ratingRepository;
    @Mock private VehicleAssignmentRepository vehicleAssignmentRepository;

    @InjectMocks private SmartAssignmentService service;

    private Shipment shipment;
    private Zone zone;

    @BeforeEach
    void setUp() {
        zone = new Zone();
        zone.setId(1L);
        zone.setName("القاهرة");

        shipment = new Shipment();
        shipment.setId(100L);
        shipment.setZone(zone);
        shipment.setDeliveryLatitude(BigDecimal.valueOf(30.0444));
        shipment.setDeliveryLongitude(BigDecimal.valueOf(31.2357));
    }

    // ── Distance Score ──────────────────────────────────────

    @Nested
    @DisplayName("حساب التقييم")
    class ScoreCalculation {

        @Test
        @DisplayName("يجب حساب درجة المسافة — قريب = درجة عالية")
        void distanceScoreCloseLocation() {
            CourierLocationHistory loc = new CourierLocationHistory();
            loc.setLatitude(BigDecimal.valueOf(30.05));
            loc.setLongitude(BigDecimal.valueOf(31.24));
            when(locationRepository.findByCourierIdOrderByTimestampDesc(1L))
                    .thenReturn(List.of(loc));

            double score = service.calculateDistanceScore(1L, shipment, 50.0);

            assertThat(score).isGreaterThan(0.9); // very close
        }

        @Test
        @DisplayName("يجب حساب درجة المسافة — بعيد = درجة منخفضة")
        void distanceScoreFarLocation() {
            CourierLocationHistory loc = new CourierLocationHistory();
            loc.setLatitude(BigDecimal.valueOf(31.2));  // ~130km away
            loc.setLongitude(BigDecimal.valueOf(32.3));
            when(locationRepository.findByCourierIdOrderByTimestampDesc(1L))
                    .thenReturn(List.of(loc));

            double score = service.calculateDistanceScore(1L, shipment, 50.0);

            assertThat(score).isEqualTo(0.0); // beyond max distance
        }

        @Test
        @DisplayName("يجب حساب درجة الحمل — مندوب فارغ = درجة كاملة")
        void loadScoreEmpty() {
            when(shipmentRepository.countByCourierIdAndStatusName(1L, "IN_TRANSIT")).thenReturn(0L);

            double score = service.calculateLoadScore(1L, 30);

            assertThat(score).isEqualTo(1.0);
        }

        @Test
        @DisplayName("يجب حساب درجة الحمل — مندوب ممتلئ = صفر")
        void loadScoreFull() {
            when(shipmentRepository.countByCourierIdAndStatusName(1L, "IN_TRANSIT")).thenReturn(30L);

            double score = service.calculateLoadScore(1L, 30);

            assertThat(score).isEqualTo(0.0);
        }

        @Test
        @DisplayName("يجب حساب درجة التقييم — تقييم عالي")
        void ratingScoreHigh() {
            when(ratingRepository.getAverageRatingByCourierId(1L)).thenReturn(4.5);

            double score = service.calculateRatingScore(1L, 3.0);

            assertThat(score).isEqualTo(0.9); // 4.5/5
        }

        @Test
        @DisplayName("يجب حساب درجة المنطقة — تطابق")
        void zoneScoreMatch() {
            CourierZone cz = new CourierZone(1L, 1L);
            when(courierZoneRepository.findByCourierIdAndZoneId(1L, 1L))
                    .thenReturn(List.of(cz));

            double score = service.calculateZoneScore(1L, shipment);

            assertThat(score).isEqualTo(1.0);
        }

        @Test
        @DisplayName("يجب حساب درجة المنطقة — عدم تطابق")
        void zoneScoreNoMatch() {
            when(courierZoneRepository.findByCourierIdAndZoneId(1L, 1L))
                    .thenReturn(Collections.emptyList());

            double score = service.calculateZoneScore(1L, shipment);

            assertThat(score).isEqualTo(0.0);
        }

        @Test
        @DisplayName("يجب حساب درجة المركبة — مركبة نشطة")
        void vehicleScoreActive() {
            when(vehicleAssignmentRepository.existsByCourierIdAndStatus(1L, AssignmentStatus.ACTIVE))
                    .thenReturn(true);

            double score = service.calculateVehicleScore(1L);

            assertThat(score).isEqualTo(1.0);
        }
    }

    // ── Find Best Courier ───────────────────────────────────

    @Nested
    @DisplayName("إيجاد أفضل مندوب")
    @MockitoSettings(strictness = Strictness.LENIENT)
    class FindBestCourier {

        @Test
        @DisplayName("يجب إرجاع فارغ إذا لا يوجد مرشحون")
        void emptyWhenNoCandidates() {
            var result = service.findBestCourier(shipment, Collections.emptyList());
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("يجب استبعاد مندوب بتقييم أقل من الحد")
        void filterByMinRating() {
            AssignmentRule rule = new AssignmentRule("MIN_RATING", "4.0", "");
            when(ruleRepository.findByRuleKey("MIN_RATING")).thenReturn(Optional.of(rule));
            when(ruleRepository.findByRuleKey("MAX_LOAD_PER_COURIER")).thenReturn(Optional.empty());
            when(ruleRepository.findByRuleKey("MAX_DISTANCE_KM")).thenReturn(Optional.empty());
            when(ruleRepository.findByRuleKey("REQUIRE_ZONE_MATCH")).thenReturn(Optional.empty());

            when(ratingRepository.getAverageRatingByCourierId(1L)).thenReturn(3.5);

            var result = service.findBestCourier(shipment, List.of(1L));
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("يجب استبعاد مندوب تجاوز الحد الأقصى للحمل")
        void filterByMaxLoad() {
            when(ruleRepository.findByRuleKey("MIN_RATING")).thenReturn(Optional.empty());
            when(ruleRepository.findByRuleKey("MAX_LOAD_PER_COURIER")).thenReturn(Optional.empty());
            when(ruleRepository.findByRuleKey("MAX_DISTANCE_KM")).thenReturn(Optional.empty());
            when(ruleRepository.findByRuleKey("REQUIRE_ZONE_MATCH")).thenReturn(Optional.empty());

            when(ratingRepository.getAverageRatingByCourierId(1L)).thenReturn(4.5);
            when(shipmentRepository.countByCourierIdAndStatusName(1L, "IN_TRANSIT")).thenReturn(30L);

            var result = service.findBestCourier(shipment, List.of(1L));
            assertThat(result).isEmpty();
        }
    }

    // ── Haversine ───────────────────────────────────────────

    @Test
    @DisplayName("دالة هافرسين — المسافة بين القاهرة والإسكندرية ~180 كم")
    void haversineKnownDistance() {
        double distance = SmartAssignmentService.haversineKm(
                30.0444, 31.2357,  // Cairo
                31.2001, 29.9187); // Alexandria
        assertThat(distance).isBetween(170.0, 190.0);
    }
}
