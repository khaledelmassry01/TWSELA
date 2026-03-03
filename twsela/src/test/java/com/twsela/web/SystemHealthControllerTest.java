package com.twsela.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.*;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.service.SystemHealthService;
import com.twsela.web.dto.PlatformOpsDTO.*;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SystemHealthController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(SystemHealthControllerTest.TestMethodSecurityConfig.class)
class SystemHealthControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SystemHealthService systemHealthService;
    @MockBean
    private AuthenticationHelper authenticationHelper;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenBlacklistService tokenBlacklistService;
    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void getAllHealthChecks_asOwner_returns200() throws Exception {
        when(systemHealthService.getAllHealthChecks()).thenReturn(List.of());
        mockMvc.perform(get("/api/system/health-checks")
                        .with(user("admin").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createHealthCheck_asOwner_returns200() throws Exception {
        SystemHealthCheck check = new SystemHealthCheck();
        check.setId(1L);
        check.setComponent("DATABASE");
        check.setStatus("UP");
        check.setResponseTimeMs(45L);
        when(systemHealthService.createHealthCheck(any())).thenReturn(check);

        CreateHealthCheckRequest request = new CreateHealthCheckRequest("DATABASE", "UP", 45L, "All good");
        mockMvc.perform(post("/api/system/health-checks")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.component").value("DATABASE"));
    }

    @Test
    void createArchivePolicy_asAdmin_returns200() throws Exception {
        ArchivePolicy policy = new ArchivePolicy();
        policy.setId(1L);
        policy.setEntityType("SHIPMENT");
        policy.setRetentionDays(365);
        when(systemHealthService.createArchivePolicy(any(), any())).thenReturn(policy);

        CreateArchivePolicyRequest request = new CreateArchivePolicyRequest(
                "SHIPMENT", 365, "MOVE", true, true);
        mockMvc.perform(post("/api/system/archive-policies")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.entityType").value("SHIPMENT"));
    }

    @Test
    void createCleanupTask_asOwner_returns200() throws Exception {
        CleanupTask task = new CleanupTask();
        task.setId(1L);
        task.setName("Old Logs Cleanup");
        task.setTargetTable("audit_logs");
        when(systemHealthService.createCleanupTask(any(), any())).thenReturn(task);

        CreateCleanupTaskRequest request = new CreateCleanupTaskRequest(
                "Old Logs Cleanup", "audit_logs", "created_at < NOW() - INTERVAL 90 DAY",
                true, "0 0 * * 0", true);
        mockMvc.perform(post("/api/system/cleanup-tasks")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Old Logs Cleanup"));
    }

    @Test
    void deleteArchivePolicy_asOwner_returns200() throws Exception {
        mockMvc.perform(delete("/api/system/archive-policies/1")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
