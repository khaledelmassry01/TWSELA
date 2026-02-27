package com.twsela.web;

import com.twsela.domain.*;
import com.twsela.repository.*;
import com.twsela.security.JwtService;
import com.twsela.service.FinancialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private ShipmentRepository shipmentRepository;
    @MockBean private UserRepository userRepository;
    @MockBean private ShipmentStatusRepository shipmentStatusRepository;
    @MockBean private FinancialService financialService;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;

    private User ownerUser;
    private Authentication ownerAuth;

    @BeforeEach
    void setUp() {
        Role ownerRole = new Role("OWNER");
        ownerRole.setId(1L);
        ownerUser = new User();
        ownerUser.setId(1L);
        ownerUser.setName("Owner");
        ownerUser.setPhone("0501234567");
        ownerUser.setRole(ownerRole);

        ownerAuth = new UsernamePasswordAuthenticationToken(
                "0501234567", null, List.of(new SimpleGrantedAuthority("ROLE_OWNER")));

        when(userRepository.findByPhone("0501234567")).thenReturn(Optional.of(ownerUser));
    }

    // ======== GET /api/dashboard/summary ========

    @Test
    @DisplayName("GET /api/dashboard/summary — يجب إرجاع ملخص لوحة التحكم للمالك")
    void getDashboardSummary_owner() throws Exception {
        when(shipmentRepository.count()).thenReturn(50L);
        when(shipmentRepository.countByCreatedAtBetween(any(Instant.class), any(Instant.class))).thenReturn(5L);
        when(shipmentRepository.sumDeliveryFeeByStatusName(anyString())).thenReturn(new BigDecimal("1000.00"));
        when(userRepository.count()).thenReturn(10L);
        when(shipmentRepository.findTop10ByOrderByUpdatedAtDesc()).thenReturn(List.of());

        mockMvc.perform(get("/api/dashboard/summary").with(authentication(ownerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.userRole").value("OWNER"))
                .andExpect(jsonPath("$.totalShipments").value(50));
    }

    // ======== GET /api/dashboard/statistics ========

    @Test
    @DisplayName("GET /api/dashboard/statistics — يجب إرجاع الإحصائيات العامة")
    void getStatistics_success() throws Exception {
        when(shipmentRepository.count()).thenReturn(100L);
        when(userRepository.count()).thenReturn(20L);
        when(shipmentRepository.countByStatusName(anyString())).thenReturn(5L);

        mockMvc.perform(get("/api/dashboard/statistics").with(authentication(ownerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statistics.totalShipments").value(100));
    }

    // ======== Unauthenticated ========

    @Test
    @DisplayName("GET /api/dashboard/summary — يجب إرجاع خطأ بدون مصادقة")
    void getDashboardSummary_noAuth_fails() throws Exception {
        // No authentication provided — getCurrentUser will throw
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().is4xxClientError());
    }

    // ======== Merchant role ========

    @Test
    @DisplayName("GET /api/dashboard/summary — يجب إرجاع ملخص لتاجر")
    void getDashboardSummary_merchant() throws Exception {
        Role merchantRole = new Role("MERCHANT");
        merchantRole.setId(3L);
        User merchant = new User();
        merchant.setId(2L);
        merchant.setName("Merchant");
        merchant.setPhone("0509999999");
        merchant.setRole(merchantRole);

        Authentication merchantAuth = new UsernamePasswordAuthenticationToken(
                "0509999999", null, List.of(new SimpleGrantedAuthority("ROLE_MERCHANT")));
        when(userRepository.findByPhone("0509999999")).thenReturn(Optional.of(merchant));
        when(shipmentRepository.countByMerchantId(2L)).thenReturn(15L);
        when(shipmentRepository.countByMerchantIdAndCreatedAtBetween(eq(2L), any(Instant.class), any(Instant.class)))
                .thenReturn(3L);
        when(shipmentRepository.countByMerchantIdAndStatusName(eq(2L), anyString())).thenReturn(1L);
        when(shipmentRepository.sumDeliveryFeeByMerchantIdAndStatusName(eq(2L), anyString()))
                .thenReturn(new BigDecimal("200.00"));
        when(shipmentRepository.findTop10ByMerchantIdOrderByUpdatedAtDesc(2L)).thenReturn(List.of());

        mockMvc.perform(get("/api/dashboard/summary").with(authentication(merchantAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.userRole").value("MERCHANT"));
    }

    // ======== Courier role ========

    @Test
    @DisplayName("GET /api/dashboard/summary — يجب إرجاع ملخص لمندوب")
    void getDashboardSummary_courier() throws Exception {
        Role courierRole = new Role("COURIER");
        courierRole.setId(4L);
        User courier = new User();
        courier.setId(3L);
        courier.setName("Courier");
        courier.setPhone("0508888888");
        courier.setRole(courierRole);

        Authentication courierAuth = new UsernamePasswordAuthenticationToken(
                "0508888888", null, List.of(new SimpleGrantedAuthority("ROLE_COURIER")));
        when(userRepository.findByPhone("0508888888")).thenReturn(Optional.of(courier));
        when(shipmentRepository.countByCourierId(3L)).thenReturn(20L);
        when(shipmentRepository.countByCourierIdAndCreatedAtBetween(eq(3L), any(Instant.class), any(Instant.class)))
                .thenReturn(4L);
        when(shipmentRepository.countByCourierIdAndStatusName(eq(3L), anyString())).thenReturn(2L);
        when(financialService.calculateCourierEarnings(eq(3L), any(java.time.LocalDate.class), any(java.time.LocalDate.class)))
                .thenReturn(new BigDecimal("300.00"));
        when(shipmentRepository.findTop10ByCourierIdOrderByUpdatedAtDesc(3L)).thenReturn(List.of());

        mockMvc.perform(get("/api/dashboard/summary").with(authentication(courierAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.userRole").value("COURIER"));
    }
}
