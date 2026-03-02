package com.twsela.web;

import com.twsela.domain.Role;
import com.twsela.domain.User;
import com.twsela.domain.UserStatus;
import com.twsela.security.JwtService;
import com.twsela.service.ExcelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BulkUploadController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(BulkUploadControllerTest.TestMethodSecurityConfig.class)
class BulkUploadControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;

    @MockBean private ExcelService excelService;
    @MockBean private JwtService jwtService;
    @MockBean private com.twsela.security.TokenBlacklistService tokenBlacklistService;
    @MockBean private com.twsela.security.AuthenticationHelper authHelper;
    @MockBean private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        Mockito.when(authHelper.getCurrentUser(any(Authentication.class)))
                .thenAnswer(inv -> (User) ((Authentication) inv.getArgument(0)).getPrincipal());
    }

    private Authentication createAuth(String roleName) {
        Role role = new Role(roleName);
        role.setId(1L);
        UserStatus activeStatus = new UserStatus("ACTIVE");
        activeStatus.setId(1L);
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setPhone("0501234567");
        user.setRole(role);
        user.setStatus(activeStatus);
        user.setIsDeleted(false);
        return new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + roleName)));
    }

    @Test
    @DisplayName("POST /api/shipments/bulk — uploads Excel successfully")
    void uploadBulk_success() throws Exception {
        Map<String, Object> result = Map.of("total", 10, "success", 8, "failed", 2);
        when(excelService.processExcelFile(any(), eq("0501234567"))).thenReturn(result);

        MockMultipartFile file = new MockMultipartFile(
                "file", "shipments.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{1, 2, 3, 4});

        mockMvc.perform(multipart("/api/shipments/bulk")
                        .file(file)
                        .with(authentication(createAuth("MERCHANT")))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(10));
    }

    @Test
    @DisplayName("POST /api/shipments/bulk — rejects empty file")
    void uploadBulk_emptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]);

        mockMvc.perform(multipart("/api/shipments/bulk")
                        .file(file)
                        .with(authentication(createAuth("MERCHANT")))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/shipments/bulk — rejects non-Excel file")
    void uploadBulk_wrongContentType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "data.csv",
                "text/csv",
                "col1,col2\nval1,val2".getBytes());

        mockMvc.perform(multipart("/api/shipments/bulk")
                        .file(file)
                        .with(authentication(createAuth("MERCHANT")))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/shipments/bulk/template — downloads template")
    void downloadTemplate_success() throws Exception {
        when(excelService.generateTemplate()).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/api/shipments/bulk/template")
                        .with(authentication(createAuth("MERCHANT"))))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=shipments_template.xlsx"));
    }

    @Test
    @DisplayName("POST /api/shipments/bulk — handles processing error")
    void uploadBulk_processingError() throws Exception {
        when(excelService.processExcelFile(any(), anyString()))
                .thenThrow(new RuntimeException("Parse error"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "bad.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/shipments/bulk")
                        .file(file)
                        .with(authentication(createAuth("MERCHANT")))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
