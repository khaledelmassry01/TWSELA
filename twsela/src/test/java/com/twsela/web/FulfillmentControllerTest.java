package com.twsela.web;

import com.twsela.service.ReceivingService;
import com.twsela.service.FulfillmentService;
import com.twsela.service.PickWaveService;
import com.twsela.web.dto.WarehouseFulfillmentDTO.*;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.security.AuthenticationHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FulfillmentController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(FulfillmentControllerTest.TestMethodSecurityConfig.class)
class FulfillmentControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;
    @MockBean private ReceivingService receivingService;
    @MockBean private FulfillmentService fulfillmentService;
    @MockBean private PickWaveService pickWaveService;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    @DisplayName("يجب جلب أوامر الاستلام")
    void getReceivingOrders_success() throws Exception {
        var order = new ReceivingOrderResponse(1L, 1L, 1L, "RO-001", "EXPECTED",
                LocalDate.now(), null, null, 10, 0, "ملاحظات", Instant.now());
        when(receivingService.getByWarehouse(1L)).thenReturn(List.of(order));

        mockMvc.perform(get("/api/warehouse/1/receiving")
                        .with(user("admin").roles("WAREHOUSE_MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].referenceNumber").value("RO-001"));
    }

    @Test
    @DisplayName("يجب إنشاء أمر استلام")
    void createReceiving_success() throws Exception {
        var resp = new ReceivingOrderResponse(1L, 1L, 1L, "RO-002", "EXPECTED",
                LocalDate.now(), null, null, 5, 0, null, Instant.now());
        when(receivingService.create(any())).thenReturn(resp);

        mockMvc.perform(post("/api/warehouse/receiving")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"warehouseId\":1,\"merchantId\":1,\"referenceNumber\":\"RO-002\",\"totalExpectedItems\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("يجب جلب أوامر التنفيذ")
    void getFulfillmentOrders_success() throws Exception {
        var order = new FulfillmentOrderResponse(1L, 1L, 1L, 1L, "FO-001", "PENDING",
                "STANDARD", null, null, null, null, null, Instant.now());
        when(fulfillmentService.getByWarehouse(1L)).thenReturn(List.of(order));

        mockMvc.perform(get("/api/warehouse/1/fulfillment")
                        .with(user("admin").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].orderNumber").value("FO-001"));
    }

    @Test
    @DisplayName("يجب إنشاء أمر تنفيذ")
    void createFulfillment_success() throws Exception {
        var resp = new FulfillmentOrderResponse(1L, 1L, null, 1L, "FO-002", "PENDING",
                "STANDARD", null, null, null, null, null, Instant.now());
        when(fulfillmentService.create(any())).thenReturn(resp);

        mockMvc.perform(post("/api/warehouse/fulfillment")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"warehouseId\":1,\"orderNumber\":\"FO-002\",\"merchantId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("يجب جلب موجات الالتقاط")
    void getPickWaves_success() throws Exception {
        var wave = new PickWaveResponse(1L, 1L, "PW-001", "CREATED", "SINGLE_ORDER",
                5, 0, null, null, null, Instant.now());
        when(pickWaveService.getByWarehouse(1L)).thenReturn(List.of(wave));

        mockMvc.perform(get("/api/warehouse/1/pick-waves")
                        .with(user("admin").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].waveNumber").value("PW-001"));
    }

    @Test
    @DisplayName("يجب رفض الوصول لغير المصرح لهم")
    void getFulfillment_forbidden() throws Exception {
        mockMvc.perform(get("/api/warehouse/1/fulfillment")
                        .with(user("merchant").roles("MERCHANT")))
                .andExpect(status().isForbidden());
    }
}
