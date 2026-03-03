package com.twsela.service;

import com.twsela.domain.Contract;
import com.twsela.domain.Contract.ContractStatus;
import com.twsela.domain.Contract.ContractType;
import com.twsela.domain.User;
import com.twsela.repository.ContractRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("اختبارات خدمة العقود")
class ContractServiceTest {

    @Mock private ContractRepository contractRepository;
    @Mock private UserRepository userRepository;
    @Mock private OtpService otpService;

    @InjectMocks private ContractService contractService;

    private User party;
    private User creator;
    private Contract contract;

    @BeforeEach
    void setUp() {
        party = new User();
        party.setId(1L);
        party.setName("تاجر تجريبي");
        party.setPhone("01012345678");

        creator = new User();
        creator.setId(2L);
        creator.setName("مسؤول النظام");

        contract = new Contract("TWS-CTR-TEST0001", ContractType.MERCHANT_AGREEMENT,
                party, LocalDate.now(), LocalDate.now().plusMonths(12));
        contract.setId(1L);
        contract.setCreatedBy(creator);
    }

    @Nested
    @DisplayName("إنشاء العقود")
    class CreateContract {

        @Test
        @DisplayName("يجب إنشاء عقد جديد بنجاح")
        void createContract_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(party));
            when(userRepository.findById(2L)).thenReturn(Optional.of(creator));
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> {
                Contract c = inv.getArgument(0);
                c.setId(1L);
                return c;
            });

            Contract result = contractService.createContract(
                    ContractType.MERCHANT_AGREEMENT, 1L,
                    LocalDate.now(), LocalDate.now().plusMonths(6),
                    true, 30, "الشروط والأحكام", "ملاحظات", 2L);

            assertThat(result).isNotNull();
            assertThat(result.getContractNumber()).startsWith("TWS-CTR-");
            assertThat(result.getStatus()).isEqualTo(ContractStatus.DRAFT);
            assertThat(result.isAutoRenew()).isTrue();
            verify(contractRepository).save(any(Contract.class));
        }

        @Test
        @DisplayName("يجب رفض إنشاء عقد بتاريخ نهاية قبل البداية")
        void createContract_invalidDates() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(party));
            when(userRepository.findById(2L)).thenReturn(Optional.of(creator));

            assertThatThrownBy(() -> contractService.createContract(
                    ContractType.MERCHANT_AGREEMENT, 1L,
                    LocalDate.now().plusMonths(6), LocalDate.now(),
                    false, 30, null, null, 2L))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    @Nested
    @DisplayName("توقيع العقود")
    class SignContract {

        @Test
        @DisplayName("يجب إرسال العقد للتوقيع بنجاح")
        void sendForSignature_success() {
            contract.setStatus(ContractStatus.DRAFT);
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(otpService.generateOtp("01012345678")).thenReturn("123456");
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));

            Contract result = contractService.sendForSignature(1L);

            assertThat(result.getStatus()).isEqualTo(ContractStatus.PENDING_SIGNATURE);
            assertThat(result.getSignatureOtp()).isEqualTo("123456");
        }

        @Test
        @DisplayName("يجب توقيع العقد بنجاح عند التحقق من OTP")
        void signContract_success() {
            contract.setStatus(ContractStatus.PENDING_SIGNATURE);
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(otpService.verifyOtp("01012345678", "123456")).thenReturn(true);
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));

            Contract result = contractService.signContract(1L, "123456");

            assertThat(result.getStatus()).isEqualTo(ContractStatus.ACTIVE);
            assertThat(result.getSignedAt()).isNotNull();
            assertThat(result.getSignatureOtp()).isNull();
        }

        @Test
        @DisplayName("يجب رفض OTP غير صالح")
        void signContract_invalidOtp() {
            contract.setStatus(ContractStatus.PENDING_SIGNATURE);
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(otpService.verifyOtp("01012345678", "000000")).thenReturn(false);

            assertThatThrownBy(() -> contractService.signContract(1L, "000000"))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    @Nested
    @DisplayName("إنهاء وتجديد العقود")
    class TerminateAndRenew {

        @Test
        @DisplayName("يجب إنهاء العقد النشط بنجاح")
        void terminateContract_success() {
            contract.setStatus(ContractStatus.ACTIVE);
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));

            Contract result = contractService.terminateContract(1L, "انتهاء التعاون");

            assertThat(result.getStatus()).isEqualTo(ContractStatus.TERMINATED);
            assertThat(result.getNotes()).contains("إنهاء");
        }

        @Test
        @DisplayName("يجب تجديد العقد المنتهي بنجاح")
        void renewContract_success() {
            contract.setStatus(ContractStatus.EXPIRED);
            contract.setStartDate(LocalDate.of(2024, 1, 1));
            contract.setEndDate(LocalDate.of(2024, 12, 31));
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(contractRepository.save(any(Contract.class))).thenAnswer(inv -> inv.getArgument(0));

            Contract result = contractService.renewContract(1L);

            assertThat(result.getStatus()).isEqualTo(ContractStatus.ACTIVE);
            assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2024, 12, 31));
        }

        @Test
        @DisplayName("يجب رفض إنهاء عقد مسودة")
        void terminateContract_draftFails() {
            contract.setStatus(ContractStatus.DRAFT);
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

            assertThatThrownBy(() -> contractService.terminateContract(1L, "سبب"))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    @Test
    @DisplayName("يجب رفض البحث عن عقد غير موجود")
    void findById_notFound() {
        when(contractRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contractService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
