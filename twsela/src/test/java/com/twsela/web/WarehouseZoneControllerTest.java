package com.twsela.web;

import com.twsela.service.WarehouseZoneService;
import com.twsela.service.StorageBinService;
import com.twsela.service.InventoryMovementService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WarehouseZoneController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(WarehouseZoneControllerTest.TestMethodSecurityConfig.class)
class WarehouseZoneControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;
    @MockBean private WarehouseZoneService zoneService;
    @MockBean private StorageBinService binService;
    @MockBean private InventoryMovementService movementService;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    @DisplayName("يجب جلب مناطق المستودع")
    void getZones_success() throws Exception {
        var zone = new WarehouseZoneResponse(1L, 1L, "المنطقة أ", "A", "PICKING", 100, 20, true, 1, Instant.now());
        when(zoneService.getByWarehouse(1L)).thenReturn(List.of(zone));

        mockMvc.perform(get("/api/warehouse/1/zones")
                        .with(user("admin").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("المنطقة أ"));
    }

    @Test
    @DisplayName("يجب إنشاء منطقة مستودع")
    void createZone_success() throws Exception {
        var resp = new WarehouseZoneResponse(1L, 1L, "Zone A", "A", "PICKING", 100, 0, true, 1, Instant.now());
        when(zoneService.create(any())).thenReturn(resp);

        mockMvc.perform(post("/api/warehouse/zones")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"warehouseId\":1,\"name\":\"Zone A\",\"code\":\"A\",\"zoneType\":\"PICKING\",\"capacity\":100,\"sortOrder\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("يجب رفض الوصول لغير المصرح لهم")
    void getZones_forbidden() throws Exception {
        mockMvc.perform(get("/api/warehouse/1/zones")
                        .with(user("courier").roles("COURIER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("يجب جلب حاويات المنطقة")
    void getBins_success() throws Exception {
        var bin = new StorageBinResponse(1L, 1L, "A-01-01", "A", "01", "01", null, "STANDARD", null, 100, 5, false, true, Instant.now());
        when(binService.getByZone(1L)).thenReturn(List.of(bin));

        mockMvc.perform(get("/api/warehouse/zones/1/bins")
                        .with(user("admin").roles("WAREHOUSE_MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].binCode").value("A-01-01"));
    }

    @Test
    @DisplayName("يجب إنشاء حاوية تخزين")
    void createBin_success() throws Exception {
        var resp = new StorageBinResponse(1L, 1L, "A-01-01", "A", "01", "01", null, "STANDARD", null, 100, 0, false, true, Instant.now());
        when(binService.create(any())).thenReturn(resp);

        mockMvc.perform(post("/api/warehouse/bins")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"warehouseZoneId\":1,\"binCode\":\"A-01-01\",\"aisle\":\"A\",\"rack\":\"01\",\"shelf\":\"01\",\"maxItems\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("يجب جلب حركات المخزون")
    void getMovements_success() throws Exception {
        var mov = new InventoryMovementResponse(1L, 1L, 1L, "SKU001", "IN", 10, "RECEIVING", 1L, 1L, "test", Instant.now());
        when(movementService.getByWarehouse(1L)).thenReturn(List.of(mov));

        mockMvc.perform(get("/api/warehouse/1/movements")
                        .with(user("admin").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].productSku").value("SKU001"));
    }
}
