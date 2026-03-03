package com.twsela.service;

import com.twsela.domain.Contract;
import com.twsela.domain.CustomPricingRule;
import com.twsela.domain.Zone;
import com.twsela.repository.ContractRepository;
import com.twsela.repository.CustomPricingRuleRepository;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.ZoneRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("اختبارات خدمة التسعير المخصص")
class CustomPricingServiceTest {

    @Mock private ContractRepository contractRepository;
    @Mock private CustomPricingRuleRepository pricingRuleRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private ZoneRepository zoneRepository;

    @InjectMocks private CustomPricingService pricingService;

    private Contract contract;
    private Zone zoneFrom;
    private Zone zoneTo;
    private CustomPricingRule rule;

    @BeforeEach
    void setUp() {
        contract = new Contract();
        contract.setId(1L);
        contract.setContractNumber("TWS-CTR-12345678");

        zoneFrom = new Zone();
        zoneFrom.setId(10L);
        zoneFrom.setName("القاهرة");

        zoneTo = new Zone();
        zoneTo.setId(20L);
        zoneTo.setName("الإسكندرية");

        rule = new CustomPricingRule();
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
    }

    @Nested
    @DisplayName("حساب السعر")
    class CalculatePrice {

        @Test
        @DisplayName("يجب حساب السعر من العقد عند وجود عقد نشط")
        void calculatePrice_contractPricing() {
            when(contractRepository.findActiveByPartyId(1L)).thenReturn(Optional.of(contract));
            when(pricingRuleRepository.findActiveByContractIdAndZones(eq(1L), eq(10L), eq(20L)))
                    .thenReturn(List.of(rule));
            when(shipmentRepository.countByMerchantIdAndCreatedAtBetween(eq(1L), any(Instant.class), any(Instant.class)))
                    .thenReturn(150L); // Over 100 threshold

            Map<String, Object> result = pricingService.calculatePrice(1L, 10L, 20L, 2.0, new BigDecimal("500.00"));

            assertThat(result.get("source")).isEqualTo("CONTRACT");
            assertThat(result.get("basePrice")).isEqualTo(new BigDecimal("30.00"));
            assertThat(result).containsKey("totalPrice");
        }

        @Test
        @DisplayName("يجب استخدام السعر الافتراضي عند عدم وجود عقد")
        void calculatePrice_defaultPricing() {
            when(contractRepository.findActiveByPartyId(99L)).thenReturn(Optional.empty());

            Map<String, Object> result = pricingService.calculatePrice(99L, null, null, 1.0, null);

            assertThat(result.get("source")).isEqualTo("DEFAULT");
            assertThat(result.get("basePrice")).isEqualTo(new BigDecimal("25.00"));
        }

        @Test
        @DisplayName("يجب تطبيق رسوم الدفع عند التسليم")
        void calculatePrice_withCod() {
            when(contractRepository.findActiveByPartyId(1L)).thenReturn(Optional.of(contract));
            when(pricingRuleRepository.findActiveByContractIdAndZones(eq(1L), eq(10L), eq(20L)))
                    .thenReturn(List.of(rule));
            when(shipmentRepository.countByMerchantIdAndCreatedAtBetween(eq(1L), any(Instant.class), any(Instant.class)))
                    .thenReturn(50L); // Below threshold, no discount

            Map<String, Object> result = pricingService.calculatePrice(1L, 10L, 20L, 1.0, new BigDecimal("1000.00"));

            assertThat(result.get("source")).isEqualTo("CONTRACT");
            BigDecimal codFee = (BigDecimal) result.get("codFee");
            assertThat(codFee).isEqualByComparingTo(new BigDecimal("25.00")); // 2.5% of 1000
        }
    }

    @Nested
    @DisplayName("إدارة قواعد التسعير")
    class ManageRules {

        @Test
        @DisplayName("يجب إضافة قاعدة تسعير جديدة")
        void addPricingRule_success() {
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(zoneRepository.findById(10L)).thenReturn(Optional.of(zoneFrom));
            when(zoneRepository.findById(20L)).thenReturn(Optional.of(zoneTo));
            when(pricingRuleRepository.save(any(CustomPricingRule.class))).thenAnswer(inv -> {
                CustomPricingRule r = inv.getArgument(0);
                r.setId(2L);
                return r;
            });

            CustomPricingRule result = pricingService.addPricingRule(
                    1L, 10L, 20L, "STANDARD",
                    new BigDecimal("35.00"), new BigDecimal("4.00"),
                    new BigDecimal("3.00"), new BigDecimal("25.00"),
                    new BigDecimal("5.00"), 50);

            assertThat(result.getId()).isEqualTo(2L);
            assertThat(result.getContract().getId()).isEqualTo(1L);
            verify(pricingRuleRepository).save(any(CustomPricingRule.class));
        }

        @Test
        @DisplayName("يجب تحديث قاعدة تسعير موجودة")
        void updatePricingRule_success() {
            when(pricingRuleRepository.findById(1L)).thenReturn(Optional.of(rule));
            when(pricingRuleRepository.save(any(CustomPricingRule.class))).thenAnswer(inv -> inv.getArgument(0));

            CustomPricingRule result = pricingService.updatePricingRule(
                    1L, new BigDecimal("40.00"), null, null, null, null, 200, true);

            assertThat(result.getBasePrice()).isEqualByComparingTo(new BigDecimal("40.00"));
            assertThat(result.getMinMonthlyShipments()).isEqualTo(200);
        }

        @Test
        @DisplayName("يجب رفض تحديث قاعدة غير موجودة")
        void updatePricingRule_notFound() {
            when(pricingRuleRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pricingService.updatePricingRule(
                    999L, null, null, null, null, null, 0, false))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("يجب استرجاع قواعد التسعير لعقد")
        void getPricingRules_success() {
            when(pricingRuleRepository.findByContractId(1L)).thenReturn(List.of(rule));

            List<CustomPricingRule> result = pricingService.getPricingRules(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getBasePrice()).isEqualByComparingTo(new BigDecimal("30.00"));
        }
    }
}
