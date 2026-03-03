package com.twsela.service;

import com.twsela.repository.ShipmentRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevenueAnalyticsServiceTest {

    @Mock private ShipmentRepository shipmentRepository;

    @InjectMocks
    private RevenueAnalyticsService revenueAnalyticsService;

    private Instant from;
    private Instant to;

    @BeforeEach
    void setUp() {
        from = Instant.now().minus(30, ChronoUnit.DAYS);
        to = Instant.now();
    }

    @Nested
    @DisplayName("getTotalRevenue — إجمالي الإيرادات")
    class TotalRevenueTests {

        @Test
        @DisplayName("يجب حساب إجمالي الإيرادات من الشحنات المسلّمة")
        void getTotalRevenue_returnsSum() {
            when(shipmentRepository.sumDeliveryFeeByStatusNameAndCreatedAtBetween("DELIVERED", from, to))
                    .thenReturn(new BigDecimal("5000.00"));

            BigDecimal result = revenueAnalyticsService.getTotalRevenue(from, to);

            assertThat(result).isEqualByComparingTo(new BigDecimal("5000.00"));
        }

        @Test
        @DisplayName("يجب إرجاع صفر بدون شحنات")
        void getTotalRevenue_noShipments_returnsNull() {
            when(shipmentRepository.sumDeliveryFeeByStatusNameAndCreatedAtBetween("DELIVERED", from, to))
                    .thenReturn(null);

            BigDecimal result = revenueAnalyticsService.getTotalRevenue(from, to);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("getProfitMargin — هامش الربح")
    class ProfitMarginTests {

        @Test
        @DisplayName("يجب حساب هامش الربح 40% عند إيراد إيجابي")
        void getProfitMargin_positiveRevenue() {
            when(shipmentRepository.sumDeliveryFeeByStatusNameAndCreatedAtBetween("DELIVERED", from, to))
                    .thenReturn(new BigDecimal("10000.00"));

            double margin = revenueAnalyticsService.getProfitMargin(from, to);

            assertThat(margin).isEqualTo(40.0); // 40% margin (100% - 60% cost)
        }

        @Test
        @DisplayName("يجب إرجاع صفر عند إيراد صفري")
        void getProfitMargin_zeroRevenue() {
            when(shipmentRepository.sumDeliveryFeeByStatusNameAndCreatedAtBetween("DELIVERED", from, to))
                    .thenReturn(BigDecimal.ZERO);

            double margin = revenueAnalyticsService.getProfitMargin(from, to);

            assertThat(margin).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("getCostPerDelivery — تكلفة التوصيل الواحد")
    class CostPerDeliveryTests {

        @Test
        @DisplayName("يجب حساب التكلفة لكل توصيلة")
        void getCostPerDelivery_returnsAvg() {
            when(shipmentRepository.sumDeliveryFeeByStatusNameAndCreatedAtBetween("DELIVERED", from, to))
                    .thenReturn(new BigDecimal("10000.00"));
            when(shipmentRepository.countByStatusNameAndCreatedAtBetween("DELIVERED", from, to))
                    .thenReturn(100L);

            BigDecimal cost = revenueAnalyticsService.getCostPerDelivery(from, to);

            // 10000 * 0.60 / 100 = 60.00
            assertThat(cost).isEqualByComparingTo(new BigDecimal("60.00"));
        }

        @Test
        @DisplayName("يجب إرجاع صفر بدون توصيلات")
        void getCostPerDelivery_noDeliveries() {
            when(shipmentRepository.sumDeliveryFeeByStatusNameAndCreatedAtBetween("DELIVERED", from, to))
                    .thenReturn(new BigDecimal("5000.00"));
            when(shipmentRepository.countByStatusNameAndCreatedAtBetween("DELIVERED", from, to))
                    .thenReturn(0L);

            BigDecimal cost = revenueAnalyticsService.getCostPerDelivery(from, to);

            assertThat(cost).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("getAverageShipmentValue — متوسط قيمة الشحنة")
    class AvgShipmentValueTests {

        @Test
        @DisplayName("يجب حساب المتوسط بشكل صحيح")
        void getAverageShipmentValue_normal() {
            when(shipmentRepository.sumDeliveryFeeByStatusNameAndCreatedAtBetween("DELIVERED", from, to))
                    .thenReturn(new BigDecimal("5000.00"));
            when(shipmentRepository.countByStatusNameAndCreatedAtBetween("DELIVERED", from, to))
                    .thenReturn(50L);

            BigDecimal avg = revenueAnalyticsService.getAverageShipmentValue(from, to);

            assertThat(avg).isEqualByComparingTo(new BigDecimal("100.00"));
        }
    }

    @Nested
    @DisplayName("getRevenueByZone — الإيرادات حسب المنطقة")
    class RevenueByZoneTests {

        @Test
        @DisplayName("يجب إرجاع قائمة فارغة بدون شحنات")
        void getRevenueByZone_empty() {
            when(shipmentRepository.findByCreatedAtBetween(from, to))
                    .thenReturn(Collections.emptyList());

            List<Map<String, Object>> result = revenueAnalyticsService.getRevenueByZone(from, to);

            assertThat(result).isEmpty();
        }
    }
}
