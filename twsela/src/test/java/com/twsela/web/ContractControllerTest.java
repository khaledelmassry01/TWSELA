package com.twsela.web;

import com.twsela.domain.Contract;
import com.twsela.domain.Contract.ContractStatus;
import com.twsela.domain.Contract.ContractType;
import com.twsela.domain.User;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.service.ContractService;
import com.twsela.service.CustomPricingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ContractController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(ContractControllerTest.TestMethodSecurityConfig.class)
class ContractControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;

    @MockBean private ContractService contractService;
    @MockBean private CustomPricingService pricingService;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private UserDetailsService userDetailsService;

    private Contract createTestContract() {
        User party = new User();
        party.setId(1L);
        party.setName("تاجر تجريبي");
        party.setPhone("01012345678");

        Contract c = new Contract("TWS-CTR-ABCD1234", ContractType.MERCHANT_AGREEMENT,
                party, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
        c.setId(1L);
        c.setCreatedBy(party);
        c.setCreatedAt(Instant.now());
        return c;
    }

    @Test
    @DisplayName("يجب عرض جميع العقود للمسؤول")
    void getAllContracts_admin() throws Exception {
        Contract contract = createTestContract();
        when(contractService.getAllContracts()).thenReturn(List.of(contract));
        when(pricingService.getPricingRules(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/contracts")
                        .with(user("admin").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].contractNumber").value("TWS-CTR-ABCD1234"));
    }

    @Test
    @DisplayName("يجب رفض الوصول لغير المسؤولين")
    void getAllContracts_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/contracts")
                        .with(user("merchant").roles("MERCHANT")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("يجب عرض عقودي كتاجر")
    void getMyContracts_merchant() throws Exception {
        Contract contract = createTestContract();
        when(authHelper.getCurrentUserId(any(Authentication.class))).thenReturn(1L);
        when(contractService.getContractsByParty(1L)).thenReturn(List.of(contract));
        when(pricingService.getPricingRules(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/contracts/my")
                        .with(user("merchant").roles("MERCHANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].contractNumber").value("TWS-CTR-ABCD1234"));
    }

    @Test
    @DisplayName("يجب توقيع العقد بنجاح")
    void signContract_success() throws Exception {
        Contract contract = createTestContract();
        contract.setStatus(ContractStatus.ACTIVE);
        contract.setSignedAt(Instant.now());
        when(contractService.signContract(eq(1L), eq("123456"))).thenReturn(contract);
        when(pricingService.getPricingRules(1L)).thenReturn(List.of());

        mockMvc.perform(post("/api/contracts/1/sign")
                        .with(user("merchant").roles("MERCHANT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"otp\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
