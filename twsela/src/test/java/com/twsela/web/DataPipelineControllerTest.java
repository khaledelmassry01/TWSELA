package com.twsela.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.*;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.service.DataPipelineService;
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

@WebMvcTest(controllers = DataPipelineController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(DataPipelineControllerTest.TestMethodSecurityConfig.class)
class DataPipelineControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DataPipelineService dataPipelineService;
    @MockBean
    private AuthenticationHelper authenticationHelper;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenBlacklistService tokenBlacklistService;
    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void getAllExportJobs_asOwner_returns200() throws Exception {
        when(dataPipelineService.getAllExportJobs(any())).thenReturn(List.of());
        mockMvc.perform(get("/api/data/exports")
                        .with(user("admin").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createExportJob_asAdmin_returns200() throws Exception {
        when(authenticationHelper.getCurrentUserId(any())).thenReturn(1L);
        DataExportJob job = new DataExportJob();
        job.setId(1L);
        job.setEntityType("SHIPMENT");
        job.setFormat("CSV");
        when(dataPipelineService.createExportJob(any(), any(), any())).thenReturn(job);

        CreateDataExportJobRequest request = new CreateDataExportJobRequest("SHIPMENT", "{}", "CSV");
        mockMvc.perform(post("/api/data/exports")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.entityType").value("SHIPMENT"));
    }

    @Test
    void createPipelineConfig_asOwner_returns200() throws Exception {
        DataPipelineConfig config = new DataPipelineConfig();
        config.setId(1L);
        config.setName("Shipment Pipeline");
        config.setSourceType("MYSQL");
        config.setDestinationType("S3");
        when(dataPipelineService.createPipelineConfig(any(), any())).thenReturn(config);

        CreateDataPipelineConfigRequest request = new CreateDataPipelineConfigRequest(
                "Shipment Pipeline", "MYSQL", "{}", "{}", "S3", "{}", "0 0 * * *", true);
        mockMvc.perform(post("/api/data/pipelines")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Shipment Pipeline"));
    }

    @Test
    void createWidget_asOwner_returns200() throws Exception {
        when(authenticationHelper.getCurrentUserId(any())).thenReturn(1L);
        ReportWidget widget = new ReportWidget();
        widget.setId(1L);
        widget.setName("Sales Chart");
        widget.setReportType("SALES");
        widget.setChartType("BAR");
        when(dataPipelineService.createWidget(any(), any(), any())).thenReturn(widget);

        CreateReportWidgetRequest request = new CreateReportWidgetRequest(
                "Sales Chart", "SALES", "BAR", "{}", 0, "main-dashboard");
        mockMvc.perform(post("/api/data/widgets")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Sales Chart"));
    }

    @Test
    void deletePipelineConfig_asOwner_returns200() throws Exception {
        mockMvc.perform(delete("/api/data/pipelines/1")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
