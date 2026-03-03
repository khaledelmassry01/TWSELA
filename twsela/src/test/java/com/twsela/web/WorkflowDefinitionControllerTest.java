package com.twsela.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.User;
import com.twsela.domain.WorkflowDefinition;
import com.twsela.domain.WorkflowStep;
import com.twsela.repository.TenantRepository;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.service.WorkflowDefinitionService;
import com.twsela.service.WorkflowStepService;
import com.twsela.web.dto.WorkflowDefinitionDto;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WorkflowDefinitionController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(WorkflowDefinitionControllerTest.TestMethodSecurityConfig.class)
class WorkflowDefinitionControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkflowDefinitionService workflowDefinitionService;
    @MockBean
    private WorkflowStepService workflowStepService;
    @MockBean
    private TenantRepository tenantRepository;
    @MockBean
    private AuthenticationHelper authenticationHelper;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenBlacklistService tokenBlacklistService;
    @MockBean
    private UserDetailsService userDetailsService;

    private User currentUser;
    private WorkflowDefinition definition;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setPhone("01000000000");
        currentUser.setTenantId(1L);

        definition = new WorkflowDefinition();
        definition.setId(1L);
        definition.setName("سلسلة شحنات");
        definition.setTriggerEvent(WorkflowDefinition.TriggerEvent.SHIPMENT_CREATED);
        definition.setPriority(5);
    }

    @Test
    @DisplayName("POST /api/workflows — إنشاء سلسلة عمل")
    void create() throws Exception {
        when(authenticationHelper.getCurrentUser(any())).thenReturn(currentUser);
        when(workflowDefinitionService.create(any())).thenReturn(definition);

        WorkflowDefinitionDto dto = new WorkflowDefinitionDto();
        dto.setName("سلسلة شحنات");
        dto.setTriggerEvent(WorkflowDefinition.TriggerEvent.SHIPMENT_CREATED);
        dto.setPriority(5);

        mockMvc.perform(post("/api/workflows")
                        .with(user("01000000000").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("سلسلة شحنات"));
    }

    @Test
    @DisplayName("GET /api/workflows — جلب سلاسل العمل")
    void getAll() throws Exception {
        when(authenticationHelper.getCurrentUser(any())).thenReturn(currentUser);
        when(workflowDefinitionService.findByTenantId(1L)).thenReturn(List.of(definition));

        mockMvc.perform(get("/api/workflows")
                        .with(user("01000000000").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("سلسلة شحنات"));
    }

    @Test
    @DisplayName("GET /api/workflows/{id} — جلب سلسلة عمل بالمعرّف")
    void getById() throws Exception {
        when(workflowDefinitionService.findById(1L)).thenReturn(definition);

        mockMvc.perform(get("/api/workflows/1")
                        .with(user("01000000000").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("PUT /api/workflows/{id} — تحديث سلسلة عمل")
    void updateDef() throws Exception {
        when(workflowDefinitionService.update(eq(1L), any())).thenReturn(definition);

        WorkflowDefinitionDto dto = new WorkflowDefinitionDto();
        dto.setName("سلسلة محدثة");
        dto.setTriggerEvent(WorkflowDefinition.TriggerEvent.STATUS_CHANGED);

        mockMvc.perform(put("/api/workflows/1")
                        .with(user("01000000000").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/workflows/{id}/activate — تفعيل")
    void activate() throws Exception {
        when(workflowDefinitionService.activate(1L)).thenReturn(definition);
        mockMvc.perform(patch("/api/workflows/1/activate")
                        .with(user("01000000000").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("رفض وصول MERCHANT")
    void accessDenied_merchant() throws Exception {
        mockMvc.perform(get("/api/workflows")
                        .with(user("01000000000").roles("MERCHANT")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/workflows/{id}/steps — جلب الخطوات")
    void getSteps() throws Exception {
        WorkflowStep step = new WorkflowStep();
        step.setId(1L);
        step.setStepOrder(1);
        step.setStepType(WorkflowStep.StepType.ACTION);
        when(workflowStepService.findByDefinitionId(1L)).thenReturn(List.of(step));

        mockMvc.perform(get("/api/workflows/1/steps")
                        .with(user("01000000000").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].stepOrder").value(1));
    }
}
