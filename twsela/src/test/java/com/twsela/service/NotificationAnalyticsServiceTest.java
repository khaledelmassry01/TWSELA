package com.twsela.service;

import com.twsela.domain.NotificationChannel;
import com.twsela.domain.NotificationDeliveryLog.DeliveryStatus;
import com.twsela.repository.NotificationDeliveryLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationAnalyticsServiceTest {

    @Mock private NotificationDeliveryLogRepository deliveryLogRepository;

    @InjectMocks
    private NotificationAnalyticsService analyticsService;

    private final Instant from = Instant.now().minus(7, ChronoUnit.DAYS);
    private final Instant to = Instant.now();

    @Nested
    @DisplayName("getDeliveryStats — إحصائيات التسليم")
    class DeliveryStatsTests {

        @Test
        @DisplayName("يجب إرجاع إحصائيات مفصلة حسب القناة")
        void getDeliveryStats_withData() {
            Object[] emailSent = new Object[] { "EMAIL", "SENT", 50L };
            Object[] emailDelivered = new Object[] { "EMAIL", "DELIVERED", 45L };
            Object[] pushSent = new Object[] { "PUSH", "SENT", 30L };

            when(deliveryLogRepository.getDeliveryStatsByChannelAndStatus(from, to))
                    .thenReturn(List.of(emailSent, emailDelivered, pushSent));

            Map<String, Object> stats = analyticsService.getDeliveryStats(from, to);

            assertThat(stats.get("totalSent")).isEqualTo(80L);
            assertThat(stats.get("totalDelivered")).isEqualTo(45L);
            assertThat(stats).containsKey("channelBreakdown");
        }

        @Test
        @DisplayName("يجب إرجاع أصفار عند عدم وجود بيانات")
        void getDeliveryStats_noData() {
            when(deliveryLogRepository.getDeliveryStatsByChannelAndStatus(from, to))
                    .thenReturn(List.of());

            Map<String, Object> stats = analyticsService.getDeliveryStats(from, to);

            assertThat(stats.get("totalSent")).isEqualTo(0L);
            assertThat(stats.get("totalDelivered")).isEqualTo(0L);
            assertThat(stats.get("totalFailed")).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("getOpenRate — معدل فتح البريد الإلكتروني")
    class OpenRateTests {

        @Test
        @DisplayName("يجب حساب نسبة الفتح بشكل صحيح")
        void getOpenRate_calculated() {
            when(deliveryLogRepository.countByChannelAndStatusAndSentAtBetween(
                    NotificationChannel.EMAIL, DeliveryStatus.SENT, from, to)).thenReturn(60L);
            when(deliveryLogRepository.countByChannelAndStatusAndSentAtBetween(
                    NotificationChannel.EMAIL, DeliveryStatus.DELIVERED, from, to)).thenReturn(40L);

            double rate = analyticsService.getOpenRate(from, to);

            assertThat(rate).isEqualTo(40.0);
        }

        @Test
        @DisplayName("يجب إرجاع صفر عند عدم وجود بيانات")
        void getOpenRate_noData() {
            when(deliveryLogRepository.countByChannelAndStatusAndSentAtBetween(
                    any(), any(), any(), any())).thenReturn(0L);

            double rate = analyticsService.getOpenRate(from, to);

            assertThat(rate).isEqualTo(0.0);
        }
    }
}
