package com.twsela.service;

import com.twsela.domain.User;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourierAnalyticsServiceTest {

    @Mock private ShipmentRepository shipmentRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private CourierAnalyticsService courierAnalyticsService;

    private Instant from;
    private Instant to;
    private User courier1;
    private User courier2;

    @BeforeEach
    void setUp() {
        from = Instant.now().minus(30, ChronoUnit.DAYS);
        to = Instant.now();

        courier1 = new User();
        courier1.setId(1L);
        courier1.setName("مندوب ١");

        courier2 = new User();
        courier2.setId(2L);
        courier2.setName("مندوب ٢");
    }

    @Nested
    @DisplayName("getUtilizationRate — معدل الاستغلال")
    class UtilizationTests {

        @Test
        @DisplayName("يجب حساب معدل الاستغلال بشكل صحيح")
        void getUtilizationRate_normal() {
            when(userRepository.findByRoleName("COURIER")).thenReturn(List.of(courier1));
            when(shipmentRepository.countByCourierIdAndStatusNameAndCreatedAtBetween(
                    eq(1L), eq("DELIVERED"), eq(from), eq(to))).thenReturn(300L);

            double rate = courierAnalyticsService.getUtilizationRate(from, to);

            assertThat(rate).isGreaterThan(0);
        }

        @Test
        @DisplayName("يجب إرجاع صفر بدون مناديب")
        void getUtilizationRate_noCouriers() {
            when(userRepository.findByRoleName("COURIER")).thenReturn(Collections.emptyList());

            double rate = courierAnalyticsService.getUtilizationRate(from, to);

            assertThat(rate).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("getLeaderboard — ترتيب المناديب")
    class LeaderboardTests {

        @Test
        @DisplayName("يجب ترتيب المناديب حسب التوصيلات")
        void getLeaderboard_sorted() {
            when(userRepository.findByRoleName("COURIER")).thenReturn(List.of(courier1, courier2));
            when(shipmentRepository.countByCourierIdAndStatusNameAndCreatedAtBetween(1L, "DELIVERED", from, to)).thenReturn(50L);
            when(shipmentRepository.countByCourierIdAndCreatedAtBetween(1L, from, to)).thenReturn(60L);
            when(shipmentRepository.countByCourierIdAndStatusNameAndCreatedAtBetween(2L, "DELIVERED", from, to)).thenReturn(80L);
            when(shipmentRepository.countByCourierIdAndCreatedAtBetween(2L, from, to)).thenReturn(90L);

            List<Map<String, Object>> leaderboard = courierAnalyticsService.getLeaderboard(from, to, 10);

            assertThat(leaderboard).hasSize(2);
            assertThat(leaderboard.get(0).get("courierName")).isEqualTo("مندوب ٢"); // Higher delivered
            assertThat((Long) leaderboard.get(0).get("delivered")).isEqualTo(80L);
        }
    }

    @Nested
    @DisplayName("getShipmentsPerDay — الشحنات اليومية لكل مندوب")
    class ShipmentsPerDayTests {

        @Test
        @DisplayName("يجب حساب المعدل اليومي")
        void getShipmentsPerDay_normal() {
            when(userRepository.findByRoleName("COURIER")).thenReturn(List.of(courier1));
            when(shipmentRepository.countByCourierIdAndCreatedAtBetween(1L, from, to)).thenReturn(300L);

            double perDay = courierAnalyticsService.getShipmentsPerDay(from, to);

            assertThat(perDay).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("getPerformanceDistribution — توزيع الأداء")
    class PerformanceDistributionTests {

        @Test
        @DisplayName("يجب عرض توزيع أداء المناديب")
        void getPerformanceDistribution_normal() {
            when(userRepository.findByRoleName("COURIER")).thenReturn(List.of(courier1, courier2));
            when(shipmentRepository.countByCourierIdAndStatusNameAndCreatedAtBetween(1L, "DELIVERED", from, to)).thenReturn(50L);
            when(shipmentRepository.countByCourierIdAndStatusNameAndCreatedAtBetween(2L, "DELIVERED", from, to)).thenReturn(80L);

            Map<String, Object> dist = courierAnalyticsService.getPerformanceDistribution(from, to);

            assertThat(dist).containsKey("totalCouriers");
            assertThat((int) dist.get("totalCouriers")).isEqualTo(2);
            assertThat((long) dist.get("topPerformerDeliveries")).isEqualTo(80L);
        }
    }
}
