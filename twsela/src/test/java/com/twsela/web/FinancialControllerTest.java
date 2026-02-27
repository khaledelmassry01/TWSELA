package com.twsela.web;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FinancialController.class)
class FinancialControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private FinancialService financialService;
    @MockBean private PayoutRepository payoutRepository;
    @MockBean private UserRepository userRepository;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;

    private User ownerUser;
    private Payout samplePayout;
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

        PayoutStatus pendingPayoutStatus = new PayoutStatus();
        pendingPayoutStatus.setId(1L);
        pendingPayoutStatus.setName("PENDING");

        samplePayout = new Payout();
        samplePayout.setId(10L);
        samplePayout.setUser(ownerUser);
        samplePayout.setNetAmount(new BigDecimal("500.00"));
        samplePayout.setStatus(pendingPayoutStatus);

        // FinancialController.getCurrentUser does: (User) authentication.getPrincipal()
        ownerAuth = new UsernamePasswordAuthenticationToken(
                ownerUser, null, List.of(new SimpleGrantedAuthority("ROLE_OWNER")));
    }

    // ======== GET /api/financial/payouts ========

    @Test
    @DisplayName("GET /api/financial/payouts — يجب إرجاع جميع المدفوعات للمالك")
    void getAllPayouts_owner() throws Exception {
        when(payoutRepository.findAll()).thenReturn(List.of(samplePayout));

        mockMvc.perform(get("/api/financial/payouts").with(authentication(ownerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    @DisplayName("GET /api/financial/payouts — يجب إرجاع المدفوعات الخاصة بالتاجر فقط")
    void getAllPayouts_merchant() throws Exception {
        Role merchantRole = new Role("MERCHANT");
        merchantRole.setId(3L);
        User merchant = new User();
        merchant.setId(5L);
        merchant.setName("Merchant");
        merchant.setPhone("0509999999");
        merchant.setRole(merchantRole);

        Authentication merchantAuth = new UsernamePasswordAuthenticationToken(
                merchant, null, List.of(new SimpleGrantedAuthority("ROLE_MERCHANT")));

        when(financialService.getPayoutsForUser(5L)).thenReturn(List.of(samplePayout));

        mockMvc.perform(get("/api/financial/payouts").with(authentication(merchantAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ======== POST /api/financial/payouts ========

    @Test
    @DisplayName("POST /api/financial/payouts — يجب إنشاء مدفوعات جديدة")
    void createPayout_success() throws Exception {
        User courier = new User();
        courier.setId(5L);
        courier.setName("Courier");
        when(userRepository.findById(5L)).thenReturn(Optional.of(courier));
        when(financialService.createCourierPayout(eq(5L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(samplePayout);

        FinancialController.CreatePayoutRequest request =
                new FinancialController.CreatePayoutRequest(5L, "COURIER", LocalDate.now().minusDays(30), LocalDate.now());

        mockMvc.perform(post("/api/financial/payouts")
                        .with(authentication(ownerAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    // ======== GET /api/financial/payouts/{payoutId} ========

    @Test
    @DisplayName("GET /api/financial/payouts/{id} — يجب إرجاع تفاصيل مدفوعات")
    void getPayoutById_found() throws Exception {
        when(financialService.getPayoutById(10L)).thenReturn(samplePayout);

        mockMvc.perform(get("/api/financial/payouts/10").with(authentication(ownerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("GET /api/financial/payouts/{id} — يجب إرجاع 404 عند عدم وجود المدفوعات")
    void getPayoutById_notFound() throws Exception {
        when(financialService.getPayoutById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/financial/payouts/999").with(authentication(ownerAuth)))
                .andExpect(status().isNotFound());
    }

    // ======== PUT /api/financial/payouts/{payoutId}/status ========

    @Test
    @DisplayName("PUT /api/financial/payouts/{id}/status — يجب تحديث حالة المدفوعات")
    void updatePayoutStatus_success() throws Exception {
        PayoutStatus completedStatus = new PayoutStatus();
        completedStatus.setId(2L);
        completedStatus.setName("COMPLETED");
        samplePayout.setStatus(completedStatus);
        when(financialService.updatePayoutStatus(10L, "COMPLETED")).thenReturn(samplePayout);

        mockMvc.perform(put("/api/financial/payouts/10/status")
                        .with(authentication(ownerAuth))
                        .with(csrf())
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.name").value("COMPLETED"));
    }

    // ======== GET /api/financial/payouts/pending ========

    @Test
    @DisplayName("GET /api/financial/payouts/pending — يجب إرجاع المدفوعات المعلقة")
    void getPendingPayouts_success() throws Exception {
        when(financialService.getPendingPayouts()).thenReturn(List.of(samplePayout));

        mockMvc.perform(get("/api/financial/payouts/pending").with(authentication(ownerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status.name").value("PENDING"));
    }
}
