package com.twsela.web;

import com.twsela.service.DocumentTemplateService;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.security.AuthenticationHelper;
import com.twsela.web.dto.DocumentManagementDTO.*;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DocumentController.class, properties = {
    "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
    "app.security.jwt.expiration-ms=3600000"
})
@Import(DocumentControllerTest.TestMethodSecurityConfig.class)
class DocumentControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;

    @MockBean private DocumentTemplateService templateService;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private AuthenticationHelper authenticationHelper;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    void createTemplate_shouldReturnCreated() throws Exception {
        var response = new DocumentTemplateResponse(1L, "Invoice", "INVOICE", "PDF",
                null, 1, false, 1L, LocalDateTime.now());
        when(templateService.createTemplate(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/documents/templates")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"Invoice","type":"INVOICE"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Invoice"));
    }

    @Test
    void getTemplate_shouldReturnTemplate() throws Exception {
        var response = new DocumentTemplateResponse(1L, "Invoice", "INVOICE", "PDF",
                null, 1, false, 1L, LocalDateTime.now());
        when(templateService.getTemplateById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/documents/templates/1")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("INVOICE"));
    }

    @Test
    void generateDocument_shouldReturnGenerated() throws Exception {
        var response = new GeneratedDocumentResponse(1L, 1L, 100L, "INVOICE",
                "/docs/inv.pdf", 1024L, LocalDateTime.now(), null, 1L, LocalDateTime.now());
        when(templateService.generateDocument(any(), any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/documents/generate")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"templateId":1,"shipmentId":100,"documentType":"INVOICE"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentType").value("INVOICE"));
    }

    @Test
    void createBatch_shouldReturnBatch() throws Exception {
        var response = new DocumentBatchResponse(1L, "LABEL", "PENDING", 10, 0,
                1L, null, null, 1L, LocalDateTime.now());
        when(templateService.createBatch(any(), any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/documents/batches")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"batchType":"LABEL","totalDocuments":10}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.batchType").value("LABEL"));
    }

    @Test
    void getTemplate_forbidden_forMerchant() throws Exception {
        mockMvc.perform(get("/api/documents/templates/1")
                        .with(user("merchant").roles("MERCHANT")))
                .andExpect(status().isForbidden());
    }
}
