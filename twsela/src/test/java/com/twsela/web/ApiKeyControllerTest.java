package com.twsela.web;

import com.twsela.domain.ApiKey;
import com.twsela.domain.User;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.service.ApiKeyService;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ApiKeyController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(ApiKeyControllerTest.TestMethodSecurityConfig.class)
@DisplayName("اختبارات وحدة تحكم مفاتيح API")
class ApiKeyControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;

    @MockBean private ApiKeyService apiKeyService;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private UserDetailsService userDetailsService;

    private ApiKey createTestApiKey() {
        User merchant = new User();
        merchant.setId(1L);
        merchant.setPhone("01012345678");

        ApiKey key = new ApiKey();
        key.setId(1L);
        key.setMerchant(merchant);
        key.setKeyValue("TWS-KEY-TEST123");
        key.setSecretHash("$2a$10$hash");
        key.setName("مفتاح اختبار");
        key.setScopes("shipments:read");
        key.setRateLimit(100);
        key.setActive(true);
        key.setRequestCount(0L);
        key.setCreatedAt(Instant.now());
        return key;
    }

    @Test
    @DisplayName("يجب إنشاء مفتاح API جديد")
    void createKey() throws Exception {
        ApiKey key = createTestApiKey();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("apiKey", key);
        result.put("secret", "plain-secret-123");

        when(authHelper.getCurrentUserId(any(Authentication.class))).thenReturn(1L);
        when(apiKeyService.generateApiKey(1L, "مفتاح جديد", "shipments:read")).thenReturn(result);

        mockMvc.perform(post("/api/developer/keys")
                        .with(user("merchant").roles("MERCHANT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"مفتاح جديد\", \"scopes\": \"shipments:read\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.keyValue").value("TWS-KEY-TEST123"));
    }

    @Test
    @DisplayName("يجب عرض مفاتيح التاجر")
    void getMyKeys() throws Exception {
        when(authHelper.getCurrentUserId(any(Authentication.class))).thenReturn(1L);
        when(apiKeyService.getKeysByMerchant(1L)).thenReturn(List.of(createTestApiKey()));

        mockMvc.perform(get("/api/developer/keys")
                        .with(user("merchant").roles("MERCHANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("مفتاح اختبار"));
    }

    @Test
    @DisplayName("يجب رفض الوصول لغير التجار")
    void createKey_forbidden() throws Exception {
        mockMvc.perform(post("/api/developer/keys")
                        .with(user("courier").roles("COURIER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"test\", \"scopes\": \"shipments:read\"}"))
                .andExpect(status().isForbidden());
    }
}
