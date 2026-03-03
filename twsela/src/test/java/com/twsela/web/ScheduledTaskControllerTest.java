package com.twsela.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.ScheduledTask;
import com.twsela.domain.User;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.service.ScheduledTaskService;
import com.twsela.web.dto.ScheduledTaskDto;
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

@WebMvcTest(controllers = ScheduledTaskController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(ScheduledTaskControllerTest.TestMethodSecurityConfig.class)
class ScheduledTaskControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScheduledTaskService scheduledTaskService;
    @MockBean
    private AuthenticationHelper authenticationHelper;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenBlacklistService tokenBlacklistService;
    @MockBean
    private UserDetailsService userDetailsService;

    private User currentUser;
    private ScheduledTask task;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setPhone("01000000000");
        currentUser.setTenantId(1L);

        task = new ScheduledTask();
        task.setId(1L);
        task.setName("تقرير يومي");
        task.setTaskType(ScheduledTask.TaskType.GENERATE_REPORT);
        task.setCronExpression("0 0 6 * * ?");
    }

    @Test
    @DisplayName("POST /api/scheduled-tasks — إنشاء مهمة")
    void create() throws Exception {
        when(authenticationHelper.getCurrentUser(any())).thenReturn(currentUser);
        when(scheduledTaskService.create(any())).thenReturn(task);

        ScheduledTaskDto dto = new ScheduledTaskDto();
        dto.setName("تقرير يومي");
        dto.setTaskType(ScheduledTask.TaskType.GENERATE_REPORT);
        dto.setCronExpression("0 0 6 * * ?");

        mockMvc.perform(post("/api/scheduled-tasks")
                        .with(user("01000000000").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("تقرير يومي"));
    }

    @Test
    @DisplayName("GET /api/scheduled-tasks — جلب مهام المستأجر")
    void getAll() throws Exception {
        when(authenticationHelper.getCurrentUser(any())).thenReturn(currentUser);
        when(scheduledTaskService.findByTenantId(1L)).thenReturn(List.of(task));

        mockMvc.perform(get("/api/scheduled-tasks")
                        .with(user("01000000000").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("تقرير يومي"));
    }

    @Test
    @DisplayName("PATCH /api/scheduled-tasks/{id}/activate — تفعيل")
    void activate() throws Exception {
        when(scheduledTaskService.activate(1L)).thenReturn(task);
        mockMvc.perform(patch("/api/scheduled-tasks/1/activate")
                        .with(user("01000000000").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/scheduled-tasks/{id} — حذف مهمة")
    void deleteTask() throws Exception {
        mockMvc.perform(delete("/api/scheduled-tasks/1")
                        .with(user("01000000000").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("رفض وصول WAREHOUSE_MANAGER")
    void accessDenied_warehouse() throws Exception {
        mockMvc.perform(get("/api/scheduled-tasks")
                        .with(user("01000000000").roles("WAREHOUSE_MANAGER")))
                .andExpect(status().isForbidden());
    }
}
