package com.twsela.service;

import com.twsela.domain.TaxRule;
import com.twsela.web.exception.ResourceNotFoundException;
import com.twsela.repository.TaxRuleRepository;
import com.twsela.web.dto.CountryDTO.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("خدمة الضرائب")
class TaxServiceTest {

    @Mock TaxRuleRepository taxRuleRepository;
    @InjectMocks TaxService taxService;

    private TaxRule egyptVat() {
        TaxRule r = new TaxRule();
        r.setId(1L);
        r.setCountryCode("EG");
        r.setTaxType(TaxRule.TaxType.VAT);
        r.setRate(new BigDecimal("0.1400"));
        r.setValidFrom(LocalDate.of(2024, 1, 1));
        r.setActive(true);
        return r;
    }

    private TaxRule saudiVat() {
        TaxRule r = new TaxRule();
        r.setId(2L);
        r.setCountryCode("SA");
        r.setTaxType(TaxRule.TaxType.VAT);
        r.setRate(new BigDecimal("0.1500"));
        r.setValidFrom(LocalDate.of(2024, 1, 1));
        r.setActive(true);
        return r;
    }

    @Nested
    @DisplayName("حساب الضريبة")
    class CalculateTaxTests {

        @Test
        @DisplayName("حساب ضريبة مصر 14%")
        void calculateEgyptVat14() {
            when(taxRuleRepository.findApplicable(eq("EG"), any(LocalDate.class)))
                    .thenReturn(List.of(egyptVat()));

            TaxCalculationResponse res = taxService.calculateTax(new BigDecimal("100.00"), "EG");
            assertThat(res.taxRate()).isEqualByComparingTo(new BigDecimal("0.1400"));
            assertThat(res.taxAmount()).isEqualByComparingTo(new BigDecimal("14.00"));
            assertThat(res.totalAmount()).isEqualByComparingTo(new BigDecimal("114.00"));
        }

        @Test
        @DisplayName("حساب ضريبة السعودية 15%")
        void calculateSaudiVat15() {
            when(taxRuleRepository.findApplicable(eq("SA"), any(LocalDate.class)))
                    .thenReturn(List.of(saudiVat()));

            TaxCalculationResponse res = taxService.calculateTax(new BigDecimal("200.00"), "SA");
            assertThat(res.taxAmount()).isEqualByComparingTo(new BigDecimal("30.00"));
            assertThat(res.totalAmount()).isEqualByComparingTo(new BigDecimal("230.00"));
        }

        @Test
        @DisplayName("دولة بدون ضريبة ترجع صفر")
        void noTaxRulesReturnsZero() {
            when(taxRuleRepository.findApplicable(eq("XX"), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            TaxCalculationResponse res = taxService.calculateTax(new BigDecimal("100.00"), "XX");
            assertThat(res.taxAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(res.totalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        }
    }

    @Nested
    @DisplayName("إدارة القواعد الضريبية")
    class RuleManagementTests {

        @Test
        @DisplayName("جلب القواعد النشطة لدولة")
        void getApplicableRules() {
            when(taxRuleRepository.findByCountryCodeAndActiveTrue("EG"))
                    .thenReturn(List.of(egyptVat()));

            List<TaxRuleResponse> rules = taxService.getApplicableRules("EG");
            assertThat(rules).hasSize(1);
            assertThat(rules.get(0).taxType()).isEqualTo("VAT");
        }

        @Test
        @DisplayName("إنشاء قاعدة ضريبية جديدة")
        void createRule() {
            when(taxRuleRepository.save(any(TaxRule.class))).thenAnswer(inv -> {
                TaxRule t = inv.getArgument(0);
                t.setId(10L);
                return t;
            });

            CreateTaxRuleRequest req = new CreateTaxRuleRequest(
                    "AE", "VAT", new BigDecimal("0.0500"), null,
                    LocalDate.of(2024, 1, 1), null);

            TaxRuleResponse res = taxService.createRule(req);
            assertThat(res.countryCode()).isEqualTo("AE");
            assertThat(res.rate()).isEqualByComparingTo(new BigDecimal("0.0500"));
        }

        @Test
        @DisplayName("فحص الإعفاء")
        void isExemptWhenCategoryInList() {
            TaxRule rule = egyptVat();
            rule.setExemptCategories("[\"FOOD\",\"MEDICAL\"]");
            when(taxRuleRepository.findApplicable(eq("EG"), any(LocalDate.class)))
                    .thenReturn(List.of(rule));

            assertThat(taxService.isExempt("EG", "FOOD")).isTrue();
            assertThat(taxService.isExempt("EG", "ELECTRONICS")).isFalse();
        }

        @Test
        @DisplayName("تحديث قاعدة غير موجودة يرمي استثناء")
        void updateNonExistingThrows() {
            when(taxRuleRepository.findById(999L)).thenReturn(Optional.empty());
            CreateTaxRuleRequest req = new CreateTaxRuleRequest(
                    "EG", "VAT", new BigDecimal("0.15"), null,
                    LocalDate.of(2024, 1, 1), null);
            assertThatThrownBy(() -> taxService.updateRule(999L, req))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
