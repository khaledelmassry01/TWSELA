package com.twsela.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.*;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.service.CustomReportService;
import com.twsela.web.dto.ReportingAnalyticsDTO.*;
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

@WebMvcTest(controllers = CustomReportController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(CustomReportControllerTest.TestMethodSecurityConfig.class)
class CustomReportControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomReportService customReportService;
    @MockBean
    private AuthenticationHelper authenticationHelper;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenBlacklistService tokenBlacklistService;
    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void getAllReports_asOwner_returns200() throws Exception {
        when(customReportService.getAllReports(any())).thenReturn(List.of());
        mockMvc.perform(get("/api/reports/custom")
                        .with(user("admin").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createReport_asOwner_returns200() throws Exception {
        when(authenticationHelper.getCurrentUserId(any())).thenReturn(1L);
        CustomReport report = new CustomReport();
        report.setId(1L);
        report.setName("Sales Report");
        report.setReportType("SALES");
        when(customReportService.createReport(any(), any(), any())).thenReturn(report);

        CreateCustomReportRequest request = new CreateCustomReportRequest(
                "Sales Report", "Monthly sales", "SALES", "{}", "[]", "{}", false);
        mockMvc.perform(post("/api/reports/custom")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Sales Report"));
    }

    @Test
    void deleteReport_asOwner_returns200() throws Exception {
        mockMvc.perform(delete("/api/reports/custom/1")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createSchedule_asAdmin_returns200() throws Exception {
        ReportSchedule schedule = new ReportSchedule();
        schedule.setId(1L);
        schedule.setCronExpression("0 0 * * *");
        when(customReportService.createSchedule(any(), any())).thenReturn(schedule);

        CreateReportScheduleRequest request = new CreateReportScheduleRequest(
                1L, "0 0 * * *", "PDF", "user@test.com", true);
        mockMvc.perform(post("/api/reports/schedules")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cronExpression").value("0 0 * * *"));
    }

    @Test
    void createFilter_asMerchant_returns200() throws Exception {
        when(authenticationHelper.getCurrentUserId(any())).thenReturn(5L);
        SavedFilter filter = new SavedFilter();
        filter.setId(1L);
        filter.setName("My Filter");
        filter.setEntityType("SHIPMENT");
        when(customReportService.createFilter(any(), any(), any())).thenReturn(filter);

        CreateSavedFilterRequest request = new CreateSavedFilterRequest(
                "My Filter", "SHIPMENT", "{}", false);
        mockMvc.perform(post("/api/reports/filters")
                        .with(user("merchant").roles("MERCHANT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("My Filter"));
    }
}
