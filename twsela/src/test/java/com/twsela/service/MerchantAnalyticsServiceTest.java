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
class MerchantAnalyticsServiceTest {

    @Mock private ShipmentRepository shipmentRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private MerchantAnalyticsService merchantAnalyticsService;

    private Instant from;
    private Instant to;
    private Instant prevFrom;
    private User merchant1;
    private User merchant2;

    @BeforeEach
    void setUp() {
        from = Instant.now().minus(30, ChronoUnit.DAYS);
        to = Instant.now();
        prevFrom = from.minus(30, ChronoUnit.DAYS);

        merchant1 = new User();
        merchant1.setId(10L);
        merchant1.setName("تاجر ١");

        merchant2 = new User();
        merchant2.setId(20L);
        merchant2.setName("تاجر ٢");
    }

    @Nested
    @DisplayName("getRetentionRate — معدل الاحتفاظ")
    class RetentionRateTests {

        @Test
        @DisplayName("يجب حساب معدل الاحتفاظ بشكل صحيح")
        void getRetentionRate_normal() {
            when(userRepository.findByRoleName("MERCHANT")).thenReturn(List.of(merchant1, merchant2));
            // merchant1 active in both periods
            when(shipmentRepository.countByMerchantIdAndCreatedAtBetween(eq(10L), any(), eq(from))).thenReturn(10L);
            when(shipmentRepository.countByMerchantIdAndCreatedAtBetween(10L, from, to)).thenReturn(5L);
            // merchant2 active only in previous
            when(shipmentRepository.countByMerchantIdAndCreatedAtBetween(eq(20L), any(), eq(from))).thenReturn(8L);
            when(shipmentRepository.countByMerchantIdAndCreatedAtBetween(20L, from, to)).thenReturn(0L);

            double rate = merchantAnalyticsService.getRetentionRate(from, to);

            assertThat(rate).isEqualTo(50.0); // 1 of 2 retained
        }
    }

    @Nested
    @DisplayName("getGrowthRate — معدل النمو")
    class GrowthRateTests {

        @Test
        @DisplayName("يجب حساب معدل النمو بشكل صحيح")
        void getGrowthRate_positive() {
            when(shipmentRepository.countByCreatedAtBetween(any(), eq(from))).thenReturn(100L);
            when(shipmentRepository.countByCreatedAtBetween(from, to)).thenReturn(150L);

            double rate = merchantAnalyticsService.getGrowthRate(from, to);

            assertThat(rate).isEqualTo(50.0); // 50% growth
        }

        @Test
        @DisplayName("يجب إرجاع 100% عند بداية من صفر")
        void getGrowthRate_fromZero() {
            when(shipmentRepository.countByCreatedAtBetween(any(), eq(from))).thenReturn(0L);
            when(shipmentRepository.countByCreatedAtBetween(from, to)).thenReturn(50L);

            double rate = merchantAnalyticsService.getGrowthRate(from, to);

            assertThat(rate).isEqualTo(100.0);
        }
    }

    @Nested
    @DisplayName("getActiveMerchantCount — عدد التجار النشطين")
    class ActiveMerchantTests {

        @Test
        @DisplayName("يجب حساب عدد التجار النشطين")
        void getActiveMerchantCount_normal() {
            when(userRepository.findByRoleName("MERCHANT")).thenReturn(List.of(merchant1, merchant2));
            when(shipmentRepository.countByMerchantIdAndCreatedAtBetween(10L, from, to)).thenReturn(5L);
            when(shipmentRepository.countByMerchantIdAndCreatedAtBetween(20L, from, to)).thenReturn(0L);

            long count = merchantAnalyticsService.getActiveMerchantCount(from, to);

            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getChurnRisk — مخاطر فقد التجار")
    class ChurnRiskTests {

        @Test
        @DisplayName("يجب تحديد التجار المعرضين للخطر")
        void getChurnRisk_highRisk() {
            when(userRepository.findByRoleName("MERCHANT")).thenReturn(List.of(merchant1));
            when(shipmentRepository.countByMerchantIdAndCreatedAtBetween(eq(10L), any(), eq(from))).thenReturn(20L);
            when(shipmentRepository.countByMerchantIdAndCreatedAtBetween(10L, from, to)).thenReturn(0L);

            List<Map<String, Object>> risk = merchantAnalyticsService.getChurnRisk(from, to);

            assertThat(risk).hasSize(1);
            assertThat(risk.get(0).get("riskLevel")).isEqualTo("HIGH");
        }
    }
}
