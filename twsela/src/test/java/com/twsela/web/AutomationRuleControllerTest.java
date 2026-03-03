package com.twsela.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.AutomationRule;
import com.twsela.domain.User;
import com.twsela.domain.WorkflowDefinition;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.service.AutomationRuleService;
import com.twsela.web.dto.AutomationRuleDto;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AutomationRuleController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(AutomationRuleControllerTest.TestMethodSecurityConfig.class)
class AutomationRuleControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AutomationRuleService automationRuleService;
    @MockBean
    private AuthenticationHelper authenticationHelper;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenBlacklistService tokenBlacklistService;
    @MockBean
    private UserDetailsService userDetailsService;

    private User currentUser;
    private AutomationRule rule;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setPhone("01000000000");
        currentUser.setTenantId(1L);

        rule = new AutomationRule();
        rule.setId(1L);
        rule.setName("إرسال إشعار");
        rule.setTriggerEvent(WorkflowDefinition.TriggerEvent.SHIPMENT_CREATED);
        rule.setActionType(AutomationRule.ActionType.SEND_NOTIFICATION);
    }

    @Test
    @DisplayName("POST /api/automation-rules — إنشاء قاعدة")
    void create() throws Exception {
        when(authenticationHelper.getCurrentUser(any())).thenReturn(currentUser);
        when(automationRuleService.create(any())).thenReturn(rule);

        AutomationRuleDto dto = new AutomationRuleDto();
        dto.setName("إرسال إشعار");
        dto.setTriggerEvent(WorkflowDefinition.TriggerEvent.SHIPMENT_CREATED);
        dto.setActionType(AutomationRule.ActionType.SEND_NOTIFICATION);

        mockMvc.perform(post("/api/automation-rules")
                        .with(user("01000000000").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("إرسال إشعار"));
    }

    @Test
    @DisplayName("GET /api/automation-rules — جلب قواعد المستأجر")
    void getAll() throws Exception {
        when(authenticationHelper.getCurrentUser(any())).thenReturn(currentUser);
        when(automationRuleService.findByTenantId(1L)).thenReturn(List.of(rule));

        mockMvc.perform(get("/api/automation-rules")
                        .with(user("01000000000").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("إرسال إشعار"));
    }

    @Test
    @DisplayName("PATCH /api/automation-rules/{id}/activate — تفعيل")
    void activate() throws Exception {
        when(automationRuleService.activate(1L)).thenReturn(rule);
        mockMvc.perform(patch("/api/automation-rules/1/activate")
                        .with(user("01000000000").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("رفض وصول COURIER")
    void accessDenied_courier() throws Exception {
        mockMvc.perform(get("/api/automation-rules")
                        .with(user("01000000000").roles("COURIER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/automation-rules/{id} — حذف قاعدة")
    void deleteRule() throws Exception {
        mockMvc.perform(delete("/api/automation-rules/1")
                        .with(user("01000000000").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
