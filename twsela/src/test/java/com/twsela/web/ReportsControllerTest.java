package com.twsela.web;

import com.twsela.domain.*;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.UserRepository;
import com.twsela.service.FinancialService;
import com.twsela.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = ReportsController.class,
    properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
    }
)
class ReportsControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean ShipmentRepository shipmentRepository;
    @MockBean UserRepository userRepository;
    @MockBean FinancialService financialService;
    @MockBean UserDetailsService userDetailsService;
    @MockBean JwtService jwtService;

    private Authentication createAuth(String role) {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setPhone("0501234567");
        Role r = new Role(role);
        r.setId(1L);
        user.setRole(r);
        UserStatus status = new UserStatus("ACTIVE");
        status.setId(1L);
        user.setStatus(status);
        return new UsernamePasswordAuthenticationToken(user, null,
            List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    @Test
    void getShipmentReport_asOwner() throws Exception {
        when(shipmentRepository.countByCreatedAtBetweenInstant(any(), any())).thenReturn(100L);
        when(shipmentRepository.countByStatusNameAndCreatedAtBetween(eq("DELIVERED"), any(), any())).thenReturn(80L);
        when(shipmentRepository.sumDeliveryFeeByStatusNameAndCreatedAtBetween(eq("DELIVERED"), any(), any())).thenReturn(new BigDecimal("5000"));

        mockMvc.perform(get("/api/reports/shipments")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31")
                .with(authentication(createAuth("OWNER")))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalShipments").value(100))
            .andExpect(jsonPath("$.deliveredShipments").value(80));
    }

    @Test
    void getCourierReport_asOwner() throws Exception {
        when(userRepository.findByRoleName("COURIER")).thenReturn(List.of());

        mockMvc.perform(get("/api/reports/couriers")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31")
                .with(authentication(createAuth("OWNER")))
                .with(csrf()))
            .andExpect(status().isOk());
    }

    @Test
    void getMerchantReport_asOwner() throws Exception {
        when(userRepository.findByRoleName("MERCHANT")).thenReturn(List.of());

        mockMvc.perform(get("/api/reports/merchants")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31")
                .with(authentication(createAuth("OWNER")))
                .with(csrf()))
            .andExpect(status().isOk());
    }

    @Test
    void getWarehouseReport_asOwner() throws Exception {
        when(shipmentRepository.countByStatusNameAndCreatedAtBetween(anyString(), any(), any())).thenReturn(10L);

        mockMvc.perform(get("/api/reports/warehouse")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31")
                .with(authentication(createAuth("OWNER")))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.receivedShipments").exists());
    }

    @Test
    void getDashboardReport_asOwner() throws Exception {
        when(shipmentRepository.count()).thenReturn(200L);
        when(shipmentRepository.countByCreatedAtBetweenInstant(any(), any())).thenReturn(5L);
        when(shipmentRepository.sumDeliveryFeeByStatusName("DELIVERED")).thenReturn(new BigDecimal("10000"));
        when(userRepository.countActiveUsers()).thenReturn(50L);

        mockMvc.perform(get("/api/reports/dashboard")
                .with(authentication(createAuth("OWNER")))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.totalShipments").value(200));
    }

    @Test
    void getCourierReport_asMerchant_forbidden() throws Exception {
        mockMvc.perform(get("/api/reports/couriers")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31")
                .with(authentication(createAuth("MERCHANT")))
                .with(csrf()))
            .andExpect(status().isForbidden());
    }
}
