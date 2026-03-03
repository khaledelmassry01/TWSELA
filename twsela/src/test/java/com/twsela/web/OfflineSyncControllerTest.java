package com.twsela.web;

import com.twsela.service.OfflineSyncService;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.security.AuthenticationHelper;
import com.twsela.web.dto.OfflineMobileDTO.*;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OfflineSyncController.class, properties = {
    "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
    "app.security.jwt.expiration-ms=3600000"
})
@Import(OfflineSyncControllerTest.TestMethodSecurityConfig.class)
class OfflineSyncControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;

    @MockBean private OfflineSyncService syncService;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private AuthenticationHelper authenticationHelper;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    void enqueue_shouldReturnCreated() throws Exception {
        var response = new OfflineQueueResponse(1L, 10L, "CREATE_SHIPMENT", "{}",
                5, "PENDING", null, null, null, 1L, LocalDateTime.now());
        when(syncService.enqueue(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/offline/queue")
                        .with(user("courier").roles("COURIER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"userId":10,"operationType":"CREATE_SHIPMENT","payload":"{}"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.operationType").value("CREATE_SHIPMENT"));
    }

    @Test
    void startSession_shouldReturnCreated() throws Exception {
        var response = new SyncSessionResponse(1L, 10L, "device-1", LocalDateTime.now(),
                null, 0, 0, "IN_PROGRESS", 1L, LocalDateTime.now());
        when(syncService.startSession(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/offline/sync/sessions")
                        .with(user("courier").roles("COURIER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"userId":10,"deviceId":"device-1"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    void enqueue_forbidden_forMerchant() throws Exception {
        mockMvc.perform(post("/api/offline/queue")
                        .with(user("merchant").roles("MERCHANT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"userId":10,"operationType":"CREATE_SHIPMENT","payload":"{}"}
                        """))
                .andExpect(status().isForbidden());
    }
}
