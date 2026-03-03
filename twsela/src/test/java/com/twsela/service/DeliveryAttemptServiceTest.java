package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.domain.DeliveryAttempt.AttemptStatus;
import com.twsela.domain.DeliveryAttempt.FailureReason;
import com.twsela.repository.DeliveryAttemptRepository;
import com.twsela.repository.ShipmentRepository;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryAttemptServiceTest {

    @Mock private DeliveryAttemptRepository attemptRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private DeliveryAttemptService deliveryAttemptService;

    private Shipment shipment;
    private User courier;

    @BeforeEach
    void setUp() {
        shipment = new Shipment();
        shipment.setId(100L);
        shipment.setTrackingNumber("TS100001");

        Role courierRole = new Role("COURIER");
        courierRole.setId(2L);
        courier = new User();
        courier.setId(20L);
        courier.setName("مندوب اختبار");
        courier.setRole(courierRole);
    }

    @Nested
    @DisplayName("recordFailedAttempt — تسجيل محاولة تسليم فاشلة")
    class RecordFailedAttemptTests {

        @Test
        @DisplayName("يجب تسجيل المحاولة الأولى بنجاح وجدولة إعادة المحاولة")
        void recordFirstAttempt_success() {
            when(shipmentRepository.findById(100L)).thenReturn(Optional.of(shipment));
            when(userRepository.findById(20L)).thenReturn(Optional.of(courier));
            when(attemptRepository.countByShipmentId(100L)).thenReturn(0);
            when(attemptRepository.save(any(DeliveryAttempt.class))).thenAnswer(inv -> {
                DeliveryAttempt a = inv.getArgument(0);
                a.setId(1L);
                return a;
            });

            DeliveryAttempt result = deliveryAttemptService.recordFailedAttempt(
                    100L, FailureReason.CUSTOMER_ABSENT, 30.0, 31.0, "لم يكن متواجد", 20L);

            assertThat(result.getAttemptNumber()).isEqualTo(1);
            assertThat(result.getStatus()).isEqualTo(AttemptStatus.FAILED);
            assertThat(result.getFailureReason()).isEqualTo(FailureReason.CUSTOMER_ABSENT);
            assertThat(result.getNextAttemptDate()).isNotNull();
            verify(attemptRepository).save(any(DeliveryAttempt.class));
        }

        @Test
        @DisplayName("يجب عدم جدولة إعادة محاولة عند الوصول للحد الأقصى")
        void recordLastAttempt_noRetryScheduled() {
            when(shipmentRepository.findById(100L)).thenReturn(Optional.of(shipment));
            when(userRepository.findById(20L)).thenReturn(Optional.of(courier));
            when(attemptRepository.countByShipmentId(100L)).thenReturn(2); // 3rd attempt
            when(attemptRepository.save(any(DeliveryAttempt.class))).thenAnswer(inv -> {
                DeliveryAttempt a = inv.getArgument(0);
                a.setId(3L);
                return a;
            });

            DeliveryAttempt result = deliveryAttemptService.recordFailedAttempt(
                    100L, FailureReason.REFUSED, 30.0, 31.0, "رفض الاستلام", 20L);

            assertThat(result.getAttemptNumber()).isEqualTo(3);
            assertThat(result.getNextAttemptDate()).isNull();
        }

        @Test
        @DisplayName("يجب رفض المحاولة عند تجاوز الحد الأقصى (3)")
        void recordAttempt_maxExceeded() {
            when(shipmentRepository.findById(100L)).thenReturn(Optional.of(shipment));
            when(userRepository.findById(20L)).thenReturn(Optional.of(courier));
            when(attemptRepository.countByShipmentId(100L)).thenReturn(3);

            assertThatThrownBy(() -> deliveryAttemptService.recordFailedAttempt(
                    100L, FailureReason.WRONG_ADDRESS, 30.0, 31.0, null, 20L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("الحد الأقصى");
        }

        @Test
        @DisplayName("يجب رفض شحنة غير موجودة")
        void recordAttempt_shipmentNotFound() {
            when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deliveryAttemptService.recordFailedAttempt(
                    999L, FailureReason.PHONE_OFF, 30.0, 31.0, null, 20L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAttempts — استرجاع محاولات التسليم")
    class GetAttemptsTests {

        @Test
        @DisplayName("يجب إرجاع قائمة المحاولات مرتبة")
        void getAttempts_returnsList() {
            DeliveryAttempt attempt1 = new DeliveryAttempt();
            attempt1.setId(1L);
            attempt1.setAttemptNumber(1);
            DeliveryAttempt attempt2 = new DeliveryAttempt();
            attempt2.setId(2L);
            attempt2.setAttemptNumber(2);

            when(attemptRepository.findByShipmentIdOrderByAttemptNumberAsc(100L))
                    .thenReturn(List.of(attempt1, attempt2));

            List<DeliveryAttempt> result = deliveryAttemptService.getAttempts(100L);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getAttemptNumber()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("isMaxAttemptsReached — التحقق من الحد الأقصى")
    class MaxAttemptsTests {

        @Test
        @DisplayName("يجب إرجاع true عند الوصول للحد الأقصى")
        void maxReached_true() {
            when(attemptRepository.countByShipmentId(100L)).thenReturn(3);
            assertThat(deliveryAttemptService.isMaxAttemptsReached(100L)).isTrue();
        }

        @Test
        @DisplayName("يجب إرجاع false قبل الوصول للحد الأقصى")
        void maxReached_false() {
            when(attemptRepository.countByShipmentId(100L)).thenReturn(1);
            assertThat(deliveryAttemptService.isMaxAttemptsReached(100L)).isFalse();
        }
    }

    @Nested
    @DisplayName("scheduleRetry — جدولة إعادة المحاولة")
    class ScheduleRetryTests {

        @Test
        @DisplayName("يجب جدولة إعادة المحاولة بنجاح")
        void scheduleRetry_success() {
            DeliveryAttempt attempt = new DeliveryAttempt();
            attempt.setId(1L);
            when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
            when(attemptRepository.save(any(DeliveryAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

            LocalDate futureDate = LocalDate.now().plusDays(2);
            DeliveryAttempt result = deliveryAttemptService.scheduleRetry(1L, futureDate);

            assertThat(result.getNextAttemptDate()).isEqualTo(futureDate);
        }

        @Test
        @DisplayName("يجب رفض تاريخ في الماضي")
        void scheduleRetry_pastDate() {
            DeliveryAttempt attempt = new DeliveryAttempt();
            attempt.setId(1L);
            when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));

            LocalDate pastDate = LocalDate.now().minusDays(1);
            assertThatThrownBy(() -> deliveryAttemptService.scheduleRetry(1L, pastDate))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("المستقبل");
        }
    }

    @Nested
    @DisplayName("getFailureReport — تقرير أسباب الفشل")
    class FailureReportTests {

        @Test
        @DisplayName("يجب إنشاء تقرير بأسباب الفشل وإجماليها")
        void getFailureReport_success() {
            Instant from = Instant.now().minusSeconds(86400);
            Instant to = Instant.now();
            List<Object[]> mockData = List.of(
                    new Object[]{"CUSTOMER_ABSENT", 5L},
                    new Object[]{"WRONG_ADDRESS", 3L}
            );
            when(attemptRepository.countFailuresByReason(from, to)).thenReturn(mockData);

            var report = deliveryAttemptService.getFailureReport(from, to);

            assertThat(report).containsKey("totalFailedAttempts");
            assertThat(report).containsKey("failuresByReason");
            assertThat((long) report.get("totalFailedAttempts")).isEqualTo(8L);
        }
    }
}
