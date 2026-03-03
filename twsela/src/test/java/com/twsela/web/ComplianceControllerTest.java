package com.twsela.web;

import com.twsela.domain.ComplianceReport;
import com.twsela.domain.ComplianceRule;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.service.ComplianceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ComplianceController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(ComplianceControllerTest.TestMethodSecurityConfig.class)
@DisplayName("اختبارات وحدة تحكم الامتثال")
class ComplianceControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;
    @MockBean private ComplianceService complianceService;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/compliance/rules — قواعد الامتثال")
    void getRules_success() throws Exception {
        ComplianceRule rule = new ComplianceRule();
        rule.setId(1L);
        rule.setName("ENFORCE_HTTPS");
        when(complianceService.getAllRules()).thenReturn(List.of(rule));

        mockMvc.perform(get("/api/compliance/rules")
                        .with(user("01099999999").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/compliance/check — تشغيل فحص")
    void runCheck_success() throws Exception {
        ComplianceReport report = new ComplianceReport();
        report.setId(1L);
        report.setTotalRules(10);
        report.setPassedRules(9);
        report.setFailedRules(1);
        report.setWarningRules(0);
        report.setReportDate(LocalDate.now());
        report.setStatus(ComplianceReport.ReportStatus.FINAL);

        when(authHelper.getCurrentUserId(any())).thenReturn(1L);
        when(complianceService.runComplianceCheck(1L)).thenReturn(report);

        mockMvc.perform(post("/api/compliance/check")
                        .with(user("01099999999").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/compliance/status — حالة الامتثال")
    void getStatus_success() throws Exception {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("totalRules", 10);
        status.put("passed", 8L);
        status.put("failed", 1L);
        status.put("warnings", 1L);
        status.put("complianceScore", 80L);
        when(complianceService.getComplianceStatus()).thenReturn(status);

        mockMvc.perform(get("/api/compliance/status")
                        .with(user("01099999999").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.complianceScore").value(80));
    }

    @Test
    @DisplayName("GET /api/compliance/rules — مستخدم غير مصرح — 403")
    void getRules_forbidden() throws Exception {
        mockMvc.perform(get("/api/compliance/rules")
                        .with(user("01012345678").roles("COURIER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
