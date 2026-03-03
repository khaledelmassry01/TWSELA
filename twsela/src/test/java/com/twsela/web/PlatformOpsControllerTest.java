package com.twsela.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.*;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.service.PlatformOpsService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PlatformOpsController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(PlatformOpsControllerTest.TestMethodSecurityConfig.class)
class PlatformOpsControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlatformOpsService platformOpsService;
    @MockBean
    private AuthenticationHelper authenticationHelper;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenBlacklistService tokenBlacklistService;
    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void getAllMetrics_asOwner_returns200() throws Exception {
        when(platformOpsService.getAllMetrics()).thenReturn(List.of());
        mockMvc.perform(get("/api/platform/metrics")
                        .with(user("admin").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createMetric_asOwner_returns200() throws Exception {
        PlatformMetric metric = new PlatformMetric();
        metric.setId(1L);
        metric.setMetricName("cpu_usage");
        metric.setMetricValue(75.5);
        metric.setMetricType("GAUGE");
        when(platformOpsService.createMetric(any())).thenReturn(metric);

        CreatePlatformMetricRequest request = new CreatePlatformMetricRequest(
                "cpu_usage", 75.5, "GAUGE", "{\"host\":\"server1\"}");
        mockMvc.perform(post("/api/platform/metrics")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metricName").value("cpu_usage"));
    }

    @Test
    void createAlert_asAdmin_returns200() throws Exception {
        SystemAlert alert = new SystemAlert();
        alert.setId(1L);
        alert.setAlertType("HIGH_LATENCY");
        alert.setSeverity("WARNING");
        alert.setMessage("Response time exceeds threshold");
        when(platformOpsService.createAlert(any(), any())).thenReturn(alert);

        CreateSystemAlertRequest request = new CreateSystemAlertRequest(
                "HIGH_LATENCY", "WARNING", "Response time exceeds threshold", "API_GATEWAY");
        mockMvc.perform(post("/api/platform/alerts")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alertType").value("HIGH_LATENCY"));
    }

    @Test
    void acknowledgeAlert_asOwner_returns200() throws Exception {
        when(authenticationHelper.getCurrentUserId(any())).thenReturn(1L);
        SystemAlert alert = new SystemAlert();
        alert.setId(1L);
        alert.setAcknowledged(true);
        when(platformOpsService.acknowledgeAlert(any(), any())).thenReturn(alert);

        mockMvc.perform(patch("/api/platform/alerts/1/acknowledge")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.acknowledged").value(true));
    }

    @Test
    void createMaintenanceWindow_asOwner_returns200() throws Exception {
        when(authenticationHelper.getCurrentUserId(any())).thenReturn(1L);
        MaintenanceWindow window = new MaintenanceWindow();
        window.setId(1L);
        window.setTitle("DB Upgrade");
        window.setStatus("SCHEDULED");
        when(platformOpsService.createMaintenanceWindow(any(), any(), any())).thenReturn(window);

        CreateMaintenanceWindowRequest request = new CreateMaintenanceWindowRequest(
                "DB Upgrade", "Database version upgrade",
                LocalDateTime.of(2025, 1, 15, 2, 0),
                LocalDateTime.of(2025, 1, 15, 4, 0),
                "database,api");
        mockMvc.perform(post("/api/platform/maintenance")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("DB Upgrade"));
    }
}
