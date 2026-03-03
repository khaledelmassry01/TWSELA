package com.twsela.service;

import com.twsela.domain.Contract;
import com.twsela.domain.ContractSlaTerms;
import com.twsela.domain.ContractSlaTerms.SlaReviewPeriod;
import com.twsela.domain.Shipment;
import com.twsela.domain.ShipmentStatus;
import com.twsela.domain.User;
import com.twsela.repository.ContractSlaTermsRepository;
import com.twsela.repository.ShipmentRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("اختبارات خدمة SLA العقود")
class ContractSlaServiceTest {

    @Mock private ContractSlaTermsRepository slaRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private ContractService contractService;

    @InjectMocks private ContractSlaService slaService;

    private Contract contract;
    private ContractSlaTerms slaTerms;
    private User party;

    @BeforeEach
    void setUp() {
        party = new User();
        party.setId(1L);
        party.setName("تاجر");

        contract = new Contract();
        contract.setId(1L);
        contract.setContractNumber("TWS-CTR-TEST0001");
        contract.setParty(party);

        slaTerms = new ContractSlaTerms();
        slaTerms.setId(1L);
        slaTerms.setContract(contract);
        slaTerms.setTargetDeliveryRate(0.95);
        slaTerms.setMaxDeliveryHours(48);
        slaTerms.setLatePenaltyPerShipment(new BigDecimal("5.00"));
        slaTerms.setLostPenaltyFixed(new BigDecimal("50.00"));
        slaTerms.setSlaReviewPeriod(SlaReviewPeriod.MONTHLY);
    }

    @Nested
    @DisplayName("حفظ واسترجاع شروط SLA")
    class SaveAndGet {

        @Test
        @DisplayName("يجب حفظ شروط SLA جديدة بنجاح")
        void saveSlaTerms_new() {
            when(contractService.findById(1L)).thenReturn(contract);
            when(slaRepository.findByContractId(1L)).thenReturn(Optional.empty());
            when(slaRepository.save(any(ContractSlaTerms.class))).thenAnswer(inv -> {
                ContractSlaTerms s = inv.getArgument(0);
                s.setId(1L);
                return s;
            });

            ContractSlaTerms result = slaService.saveSlaTerms(
                    1L, 0.95, 48,
                    new BigDecimal("5.00"), new BigDecimal("50.00"),
                    SlaReviewPeriod.MONTHLY);

            assertThat(result.getTargetDeliveryRate()).isEqualTo(0.95);
            assertThat(result.getMaxDeliveryHours()).isEqualTo(48);
            verify(slaRepository).save(any(ContractSlaTerms.class));
        }

        @Test
        @DisplayName("يجب استرجاع شروط SLA لعقد")
        void getSlaTerms_found() {
            when(slaRepository.findByContractId(1L)).thenReturn(Optional.of(slaTerms));

            Optional<ContractSlaTerms> result = slaService.getSlaTerms(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getTargetDeliveryRate()).isEqualTo(0.95);
        }
    }

    @Nested
    @DisplayName("التحقق من الالتزام")
    class Compliance {

        @Test
        @DisplayName("يجب إصدار تقرير التزام مع شحنات متوافقة")
        void checkCompliance_compliant() {
            Instant from = Instant.now().minus(30, ChronoUnit.DAYS);
            Instant to = Instant.now();

            when(contractService.findById(1L)).thenReturn(contract);
            when(slaRepository.findByContractId(1L)).thenReturn(Optional.of(slaTerms));
            when(shipmentRepository.countByMerchantIdAndCreatedAtBetween(1L, from, to)).thenReturn(100L);
            when(shipmentRepository.countByMerchantIdAndStatusNameAndCreatedAtBetween(1L, "DELIVERED", from, to)).thenReturn(97L);
            when(shipmentRepository.findByMerchantIdAndCreatedAtBetween(1L, from, to)).thenReturn(List.of());

            Map<String, Object> report = slaService.checkSlaCompliance(1L, from, to);

            assertThat(report.get("isCompliant")).isEqualTo(true);
            assertThat(report.get("totalShipments")).isEqualTo(100L);
            assertThat(report.get("deliveredShipments")).isEqualTo(97L);
        }

        @Test
        @DisplayName("يجب اكتشاف عدم الالتزام عندما يكون المعدل منخفض")
        void checkCompliance_nonCompliant() {
            Instant from = Instant.now().minus(30, ChronoUnit.DAYS);
            Instant to = Instant.now();

            when(contractService.findById(1L)).thenReturn(contract);
            when(slaRepository.findByContractId(1L)).thenReturn(Optional.of(slaTerms));
            when(shipmentRepository.countByMerchantIdAndCreatedAtBetween(1L, from, to)).thenReturn(100L);
            when(shipmentRepository.countByMerchantIdAndStatusNameAndCreatedAtBetween(1L, "DELIVERED", from, to)).thenReturn(85L);
            when(shipmentRepository.findByMerchantIdAndCreatedAtBetween(1L, from, to)).thenReturn(List.of());

            Map<String, Object> report = slaService.checkSlaCompliance(1L, from, to);

            assertThat(report.get("isCompliant")).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("حساب الغرامات")
    class Penalties {

        @Test
        @DisplayName("يجب حساب الغرامات بشكل صحيح")
        void calculatePenalties_success() {
            Instant from = Instant.now().minus(30, ChronoUnit.DAYS);
            Instant to = Instant.now();

            // Build late shipment (delivered after 72 hours, max is 48)
            ShipmentStatus deliveredStatus = new ShipmentStatus();
            deliveredStatus.setName("DELIVERED");
            Shipment lateShipment = new Shipment();
            lateShipment.setStatus(deliveredStatus);
            lateShipment.setCreatedAt(Instant.now().minus(72, ChronoUnit.HOURS));
            lateShipment.setUpdatedAt(Instant.now());

            when(contractService.findById(1L)).thenReturn(contract);
            when(slaRepository.findByContractId(1L)).thenReturn(Optional.of(slaTerms));
            when(shipmentRepository.countByMerchantIdAndCreatedAtBetween(1L, from, to)).thenReturn(50L);
            when(shipmentRepository.countByMerchantIdAndStatusNameAndCreatedAtBetween(1L, "DELIVERED", from, to)).thenReturn(45L);
            when(shipmentRepository.countByMerchantIdAndStatusNameAndCreatedAtBetween(1L, "RETURNED", from, to)).thenReturn(3L);
            when(shipmentRepository.findByMerchantIdAndCreatedAtBetween(1L, from, to)).thenReturn(List.of(lateShipment));

            Map<String, Object> penalties = slaService.calculatePenalties(1L, from, to);

            assertThat(penalties).containsKey("totalPenalties");
            assertThat(penalties.get("lateShipments")).isEqualTo(1L);
            assertThat(penalties.get("lostShipments")).isEqualTo(3L);
            BigDecimal total = (BigDecimal) penalties.get("totalPenalties");
            // 1 late * 5.00 + 3 lost * 50.00 = 155.00
            assertThat(total).isEqualByComparingTo(new BigDecimal("155.00"));
        }

        @Test
        @DisplayName("يجب رفض حساب غرامات لعقد بدون SLA")
        void calculatePenalties_noSla() {
            when(slaRepository.findByContractId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> slaService.calculatePenalties(99L, Instant.now(), Instant.now()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
