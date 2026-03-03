package com.twsela.service;

import com.twsela.domain.Shipment;
import com.twsela.domain.ShipmentStatus;
import com.twsela.repository.DeliveryAttemptRepository;
import com.twsela.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationsAnalyticsServiceTest {

    @Mock private ShipmentRepository shipmentRepository;
    @Mock private DeliveryAttemptRepository deliveryAttemptRepository;

    @InjectMocks
    private OperationsAnalyticsService operationsAnalyticsService;

    private Instant from;
    private Instant to;

    @BeforeEach
    void setUp() {
        from = Instant.now().minus(30, ChronoUnit.DAYS);
        to = Instant.now();
    }

    @Nested
    @DisplayName("getReturnRate — معدل الإرجاع")
    class ReturnRateTests {

        @Test
        @DisplayName("يجب حساب معدل الإرجاع بشكل صحيح")
        void getReturnRate_normal() {
            when(shipmentRepository.countByCreatedAtBetween(from, to)).thenReturn(100L);
            when(shipmentRepository.countByStatusNameAndCreatedAtBetween("RETURNED", from, to)).thenReturn(5L);

            double rate = operationsAnalyticsService.getReturnRate(from, to);

            assertThat(rate).isEqualTo(5.0);
        }

        @Test
        @DisplayName("يجب إرجاع صفر بدون شحنات")
        void getReturnRate_noShipments() {
            when(shipmentRepository.countByCreatedAtBetween(from, to)).thenReturn(0L);

            double rate = operationsAnalyticsService.getReturnRate(from, to);

            assertThat(rate).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("getThroughput — الإنتاجية اليومية")
    class ThroughputTests {

        @Test
        @DisplayName("يجب حساب الإنتاجية بشكل صحيح")
        void getThroughput_normal() {
            when(shipmentRepository.countByCreatedAtBetween(from, to)).thenReturn(300L);

            double throughput = operationsAnalyticsService.getThroughput(from, to);

            assertThat(throughput).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("getReturnReasonBreakdown — تحليل أسباب الإرجاع")
    class ReturnReasonTests {

        @Test
        @DisplayName("يجب عرض توزيع أسباب الإرجاع")
        void getReturnReasonBreakdown_normal() {
            List<Object[]> reasons = new ArrayList<>();
            reasons.add(new Object[]{"CUSTOMER_ABSENT", 15L});
            reasons.add(new Object[]{"WRONG_ADDRESS", 8L});
            when(deliveryAttemptRepository.countFailuresByReason(from, to)).thenReturn(reasons);

            Map<String, Long> breakdown = operationsAnalyticsService.getReturnReasonBreakdown(from, to);

            assertThat(breakdown).containsEntry("CUSTOMER_ABSENT", 15L);
            assertThat(breakdown).containsEntry("WRONG_ADDRESS", 8L);
        }
    }

    @Nested
    @DisplayName("getPeakHours — ساعات الذروة")
    class PeakHoursTests {

        @Test
        @DisplayName("يجب إرجاع 24 ساعة بدون شحنات")
        void getPeakHours_empty() {
            when(shipmentRepository.findByCreatedAtBetween(from, to)).thenReturn(Collections.emptyList());

            List<Map<String, Object>> hours = operationsAnalyticsService.getPeakHours(from, to);

            assertThat(hours).hasSize(24);
        }
    }

    @Nested
    @DisplayName("getBottleneckAnalysis — تحليل الاختناقات")
    class BottleneckTests {

        @Test
        @DisplayName("يجب عرض عدد الشحنات العالقة لكل حالة")
        void getBottleneckAnalysis_normal() {
            when(shipmentRepository.countByStatusNameAndCreatedAtBetween(eq("CREATED"), eq(from), eq(to))).thenReturn(10L);
            when(shipmentRepository.countByStatusNameAndCreatedAtBetween(eq("PICKED_UP"), eq(from), eq(to))).thenReturn(5L);
            when(shipmentRepository.countByStatusNameAndCreatedAtBetween(eq("IN_TRANSIT"), eq(from), eq(to))).thenReturn(20L);
            when(shipmentRepository.countByStatusNameAndCreatedAtBetween(eq("OUT_FOR_DELIVERY"), eq(from), eq(to))).thenReturn(3L);

            List<Map<String, Object>> bottlenecks = operationsAnalyticsService.getBottleneckAnalysis(from, to);

            assertThat(bottlenecks).hasSize(4);
            // Should be sorted descending by stuckCount
            assertThat((Long) bottlenecks.get(0).get("stuckCount")).isEqualTo(20L);
        }
    }

    @Nested
    @DisplayName("getAverageDeliveryTime — متوسط وقت التوصيل")
    class AvgDeliveryTimeTests {

        @Test
        @DisplayName("يجب إرجاع صفر بدون شحنات مسلّمة")
        void getAverageDeliveryTime_empty() {
            when(shipmentRepository.findByCreatedAtBetween(from, to)).thenReturn(Collections.emptyList());

            double avg = operationsAnalyticsService.getAverageDeliveryTime(from, to);

            assertThat(avg).isEqualTo(0.0);
        }
    }
}
