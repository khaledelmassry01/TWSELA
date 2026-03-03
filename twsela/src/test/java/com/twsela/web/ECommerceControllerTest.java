package com.twsela.web;

import com.twsela.domain.ECommerceConnection;
import com.twsela.domain.ECommerceConnection.ECommercePlatform;
import com.twsela.domain.User;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.service.ECommerceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ECommerceController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(ECommerceControllerTest.TestMethodSecurityConfig.class)
@DisplayName("اختبارات وحدة تحكم التجارة الإلكترونية")
class ECommerceControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;

    @MockBean private ECommerceService eCommerceService;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private UserDetailsService userDetailsService;

    private ECommerceConnection createTestConnection() {
        User merchant = new User();
        merchant.setId(1L);
        merchant.setPhone("01012345678");

        ECommerceConnection conn = new ECommerceConnection();
        conn.setId(1L);
        conn.setMerchant(merchant);
        conn.setPlatform(ECommercePlatform.SHOPIFY);
        conn.setStoreName("متجري");
        conn.setStoreUrl("https://mystore.shopify.com");
        conn.setActive(true);
        conn.setAutoCreateShipments(true);
        conn.setSyncErrors(0);
        conn.setCreatedAt(Instant.now());
        return conn;
    }

    @Test
    @DisplayName("يجب ربط متجر بنجاح")
    void connectStore() throws Exception {
        ECommerceConnection conn = createTestConnection();
        when(authHelper.getCurrentUserId(any(Authentication.class))).thenReturn(1L);
        when(eCommerceService.connectStore(eq(1L), eq(ECommercePlatform.SHOPIFY), anyString(),
                anyString(), anyString(), anyString(), any())).thenReturn(conn);

        mockMvc.perform(post("/api/integrations/connect")
                        .with(user("merchant").roles("MERCHANT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "platform": "SHOPIFY",
                                "storeUrl": "https://mystore.shopify.com",
                                "storeName": "متجري",
                                "accessToken": "sk-test",
                                "webhookSecret": "secret"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.storeName").value("متجري"));
    }

    @Test
    @DisplayName("يجب عرض اتصالات التاجر")
    void getMyConnections() throws Exception {
        when(authHelper.getCurrentUserId(any(Authentication.class))).thenReturn(1L);
        when(eCommerceService.getConnectionsByMerchant(1L)).thenReturn(List.of(createTestConnection()));

        mockMvc.perform(get("/api/integrations/connections")
                        .with(user("merchant").roles("MERCHANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].platform").value("SHOPIFY"));
    }

    @Test
    @DisplayName("يجب رفض الوصول لغير التجار")
    void connectStore_forbidden() throws Exception {
        mockMvc.perform(post("/api/integrations/connect")
                        .with(user("courier").roles("COURIER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"platform\": \"SHOPIFY\", \"storeUrl\": \"https://test.com\"}"))
                .andExpect(status().isForbidden());
    }
}
