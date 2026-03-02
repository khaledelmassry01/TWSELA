package com.twsela.service;

import com.twsela.domain.User;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.dto.AnalyticsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock private ShipmentRepository shipmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private FinancialService financialService;

    @InjectMocks
    private AnalyticsService analyticsService;

    private LocalDate startDate;
    private LocalDate endDate;
    private User courier;
    private User merchant;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2024, 1, 1);
        endDate = LocalDate.of(2024, 1, 31);

        courier = new User();
        courier.setId(1L);
        courier.setName("Test Courier");

        merchant = new User();
        merchant.setId(2L);
        merchant.setName("Test Merchant");
    }

    @Test
    @DisplayName("getRevenueByPeriod - تقرير الإيرادات")
    void getRevenueByPeriod_success() {
        when(shipmentRepository.countByCreatedAtBetweenInstant(any(), any())).thenReturn(100L);
        when(shipmentRepository.countByStatusNameAndCreatedAtBetween(eq("DELIVERED"), any(), any())).thenReturn(80L);
        when(shipmentRepository.sumDeliveryFeeByStatusNameAndCreatedAtBetween(eq("DELIVERED"), any(), any()))
                .thenReturn(new BigDecimal("5000.00"));

        AnalyticsDTO.RevenueReport report = analyticsService.getRevenueByPeriod(startDate, endDate);

        assertThat(report.getTotalShipments()).isEqualTo(100);
        assertThat(report.getDeliveredShipments()).isEqualTo(80);
        assertThat(report.getDeliveryRate()).isEqualTo(80.0);
        assertThat(report.getBreakdown()).isNotNull();
    }

    @Test
    @DisplayName("getStatusDistribution - توزيع الحالات")
    void getStatusDistribution_success() {
        when(shipmentRepository.countByCreatedAtBetweenInstant(any(), any())).thenReturn(100L);
        when(shipmentRepository.countByStatusNameAndCreatedAtBetween(anyString(), any(), any())).thenReturn(0L);
        when(shipmentRepository.countByStatusNameAndCreatedAtBetween(eq("DELIVERED"), any(), any())).thenReturn(70L);
        when(shipmentRepository.countByStatusNameAndCreatedAtBetween(eq("PENDING"), any(), any())).thenReturn(20L);
        when(shipmentRepository.countByStatusNameAndCreatedAtBetween(eq("CANCELLED"), any(), any())).thenReturn(10L);

        List<AnalyticsDTO.StatusDistribution> result = analyticsService.getStatusDistribution(startDate, endDate);

        assertThat(result).isNotEmpty();
        assertThat(result.stream().mapToLong(AnalyticsDTO.StatusDistribution::getCount).sum())
                .isEqualTo(100);
    }

    @Test
    @DisplayName("getStatusDistribution - لا شحنات")
    void getStatusDistribution_empty() {
        when(shipmentRepository.countByCreatedAtBetweenInstant(any(), any())).thenReturn(0L);

        List<AnalyticsDTO.StatusDistribution> result = analyticsService.getStatusDistribution(startDate, endDate);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getCourierPerformanceRanking - ترتيب المناديب")
    void getCourierPerformanceRanking_success() {
        when(userRepository.findByRoleName("COURIER")).thenReturn(List.of(courier));
        when(shipmentRepository.countByCourierIdAndStatusNameAndCreatedAtBetween(eq(1L), eq("DELIVERED"), any(), any()))
                .thenReturn(50L);
        when(shipmentRepository.countByCourierIdAndStatusNameAndCreatedAtBetween(eq(1L), eq("FAILED_DELIVERY"), any(), any()))
                .thenReturn(5L);
        when(financialService.calculateCourierEarnings(eq(1L), any(), any())).thenReturn(new BigDecimal("1500.00"));

        List<AnalyticsDTO.CourierPerformance> result = analyticsService.getCourierPerformanceRanking(startDate, endDate, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTotalDeliveries()).isEqualTo(50);
        assertThat(result.get(0).getSuccessRate()).isGreaterThan(90);
    }

    @Test
    @DisplayName("getTopMerchants - أفضل التجار")
    void getTopMerchants_success() {
        when(userRepository.findByRoleName("MERCHANT")).thenReturn(List.of(merchant));
        when(shipmentRepository.countByMerchantIdAndCreatedAtBetween(eq(2L), any(), any())).thenReturn(200L);
        when(shipmentRepository.sumDeliveryFeeByMerchantIdAndStatusNameAndCreatedAtBetween(eq(2L), eq("DELIVERED"), any(), any()))
                .thenReturn(new BigDecimal("8000.00"));

        List<AnalyticsDTO.TopMerchant> result = analyticsService.getTopMerchants(startDate, endDate, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getShipmentCount()).isEqualTo(200);
        assertThat(result.get(0).getRevenue()).isEqualByComparingTo("8000.00");
    }
}
