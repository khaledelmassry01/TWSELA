package com.twsela.web;

import com.twsela.domain.AsyncJob;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.service.AsyncJobService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AsyncJobController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(AsyncJobControllerTest.TestMethodSecurityConfig.class)
@DisplayName("اختبارات وحدة التحكم بالمهام")
class AsyncJobControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AsyncJobService asyncJobService;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    @DisplayName("POST /api/jobs — إنشاء مهمة جديدة")
    void createJob_shouldCreate() throws Exception {
        AsyncJob job = new AsyncJob();
        job.setId(1L);
        job.setJobId("job-uuid-new");
        job.setJobType("REPORT_GENERATION");
        job.setStatus(AsyncJob.JobStatus.QUEUED);

        when(asyncJobService.createJob(eq("REPORT_GENERATION"), anyString(), eq(5), eq(3)))
                .thenReturn(job);

        String body = "{\"jobType\":\"REPORT_GENERATION\",\"payload\":\"{}\",\"priority\":5,\"maxRetries\":3}";

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(user("01000000000").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jobType").value("REPORT_GENERATION"));
    }

    @Test
    @DisplayName("GET /api/jobs/{jobId} — تفاصيل مهمة")
    void getJob_shouldReturn() throws Exception {
        AsyncJob job = new AsyncJob();
        job.setId(1L);
        job.setJobId("job-uuid-detail");
        job.setJobType("BULK_SHIPMENT_PROCESS");
        job.setStatus(AsyncJob.JobStatus.RUNNING);

        when(asyncJobService.getByJobId("job-uuid-detail")).thenReturn(job);

        mockMvc.perform(get("/api/jobs/job-uuid-detail")
                        .with(user("01000000000").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jobId").value("job-uuid-detail"));
    }

    @Test
    @DisplayName("POST /api/jobs/{jobId}/cancel — إلغاء مهمة")
    void cancelJob_shouldCancel() throws Exception {
        AsyncJob job = new AsyncJob();
        job.setId(1L);
        job.setJobId("job-uuid-cancel");
        job.setJobType("REPORT_GENERATION");
        job.setStatus(AsyncJob.JobStatus.QUEUED);

        AsyncJob cancelled = new AsyncJob();
        cancelled.setId(1L);
        cancelled.setJobId("job-uuid-cancel");
        cancelled.setStatus(AsyncJob.JobStatus.CANCELLED);

        when(asyncJobService.getByJobId("job-uuid-cancel")).thenReturn(job);
        when(asyncJobService.cancelJob(1L)).thenReturn(cancelled);

        mockMvc.perform(post("/api/jobs/job-uuid-cancel/cancel")
                        .with(user("01000000000").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/jobs/stats — إحصائيات المهام")
    void getStats_shouldReturn() throws Exception {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("queued", 5L);
        stats.put("running", 2L);
        stats.put("completed", 10L);
        stats.put("total", 18L);

        when(asyncJobService.getStats()).thenReturn(stats);

        mockMvc.perform(get("/api/jobs/stats")
                        .with(user("01000000000").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.queued").value(5));
    }
}
