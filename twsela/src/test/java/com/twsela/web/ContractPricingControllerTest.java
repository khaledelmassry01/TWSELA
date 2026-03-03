package com.twsela.web;

import com.twsela.domain.Contract;
import com.twsela.domain.CustomPricingRule;
import com.twsela.domain.Zone;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.CustomPricingService;
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

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ContractPricingController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(ContractPricingControllerTest.TestMethodSecurityConfig.class)
class ContractPricingControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;

    @MockBean private CustomPricingService pricingService;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    @DisplayName("يجب عرض قواعد تسعير العقد للمسؤول")
    void getPricingRules_admin() throws Exception {
        Contract contract = new Contract();
        contract.setId(1L);
        Zone zoneFrom = new Zone();
        zoneFrom.setId(10L);
        zoneFrom.setName("القاهرة");
        Zone zoneTo = new Zone();
        zoneTo.setId(20L);
        zoneTo.setName("الإسكندرية");

        CustomPricingRule rule = new CustomPricingRule();
        rule.setId(1L);
        rule.setContract(contract);
        rule.setZoneFrom(zoneFrom);
        rule.setZoneTo(zoneTo);
        rule.setBasePrice(new BigDecimal("30.00"));
        rule.setPerKgPrice(new BigDecimal("3.00"));
        rule.setCodFeePercent(new BigDecimal("2.50"));
        rule.setMinimumCharge(new BigDecimal("20.00"));
        rule.setDiscountPercent(new BigDecimal("10.00"));
        rule.setMinMonthlyShipments(100);
        rule.setActive(true);

        when(pricingService.getPricingRules(1L)).thenReturn(List.of(rule));

        mockMvc.perform(get("/api/admin/contracts/1/pricing")
                        .with(user("admin").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].basePrice").value(30.00));
    }

    @Test
    @DisplayName("يجب رفض الوصول لغير المسؤولين")
    void getPricingRules_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/contracts/1/pricing")
                        .with(user("courier").roles("COURIER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("يجب حساب السعر للمستخدم المصرح")
    void calculatePrice_authenticated() throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("basePrice", new BigDecimal("25.00"));
        result.put("weightCharge", new BigDecimal("2.00"));
        result.put("codFee", BigDecimal.ZERO);
        result.put("discount", BigDecimal.ZERO);
        result.put("totalPrice", new BigDecimal("27.00"));
        result.put("source", "DEFAULT");

        when(pricingService.calculatePrice(eq(1L), isNull(), isNull(), eq(1.0), isNull()))
                .thenReturn(result);

        mockMvc.perform(get("/api/pricing/calculate")
                        .param("merchantId", "1")
                        .param("weightKg", "1.0")
                        .with(user("merchant").roles("MERCHANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.source").value("DEFAULT"));
    }

    @Test
    @DisplayName("يجب رفض حساب السعر بدون مصادقة")
    void calculatePrice_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/pricing/calculate")
                        .param("merchantId", "1"))
                .andExpect(status().isUnauthorized());
    }
}
