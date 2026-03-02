package com.twsela.web;

import com.twsela.security.JwtService;
import com.twsela.service.AnalyticsService;
import com.twsela.web.dto.AnalyticsDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AnalyticsController.class,
    properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000",
        "spring.profiles.active=test"
    }
)
@AutoConfigureMockMvc(addFilters = false)
class AnalyticsControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private AnalyticsService analyticsService;
    @MockBean private com.twsela.security.AuthenticationHelper authHelper;
    @MockBean private JwtService jwtService;
    @MockBean private com.twsela.security.TokenBlacklistService tokenBlacklistService;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/analytics/revenue — تقرير الإيرادات")
    void getRevenue_success() throws Exception {
        AnalyticsDTO.RevenueReport report = new AnalyticsDTO.RevenueReport();
        report.setTotalShipments(100);
        report.setDeliveredShipments(80);
        report.setTotalRevenue(new BigDecimal("5000.00"));
        report.setDeliveryRate(80);
        report.setBreakdown(List.of());

        when(analyticsService.getRevenueByPeriod(any(), any())).thenReturn(report);

        mockMvc.perform(get("/api/analytics/revenue")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalShipments").value(100))
                .andExpect(jsonPath("$.data.deliveryRate").value(80));
    }

    @Test
    @DisplayName("GET /api/analytics/status-distribution — توزيع الحالات")
    void getStatusDistribution_success() throws Exception {
        when(analyticsService.getStatusDistribution(any(), any()))
                .thenReturn(List.of(new AnalyticsDTO.StatusDistribution("DELIVERED", 80, 80.0)));

        mockMvc.perform(get("/api/analytics/status-distribution")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("DELIVERED"));
    }

    @Test
    @DisplayName("GET /api/analytics/courier-ranking — ترتيب المناديب")
    void getCourierRanking_success() throws Exception {
        AnalyticsDTO.CourierPerformance perf = new AnalyticsDTO.CourierPerformance();
        perf.setCourierId(1L);
        perf.setCourierName("Test Courier");
        perf.setTotalDeliveries(50);
        perf.setSuccessRate(90.0);

        when(analyticsService.getCourierPerformanceRanking(any(), any(), anyInt())).thenReturn(List.of(perf));

        mockMvc.perform(get("/api/analytics/courier-ranking")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].courierName").value("Test Courier"));
    }

    @Test
    @DisplayName("GET /api/analytics/top-merchants — أفضل التجار")
    void getTopMerchants_success() throws Exception {
        AnalyticsDTO.TopMerchant top = new AnalyticsDTO.TopMerchant();
        top.setMerchantId(2L);
        top.setMerchantName("Test Merchant");
        top.setShipmentCount(200);
        top.setRevenue(new BigDecimal("8000.00"));

        when(analyticsService.getTopMerchants(any(), any(), anyInt())).thenReturn(List.of(top));

        mockMvc.perform(get("/api/analytics/top-merchants")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].merchantName").value("Test Merchant"))
                .andExpect(jsonPath("$.data[0].revenue").value(8000.00));
    }
}
