package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.domain.PickupSchedule.PickupStatus;
import com.twsela.domain.PickupSchedule.TimeSlot;
import com.twsela.repository.PickupScheduleRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PickupScheduleServiceTest {

    @Mock private PickupScheduleRepository pickupRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private PickupScheduleService pickupScheduleService;

    private User merchant;
    private User courierUser;
    private PickupSchedule pickup;

    @BeforeEach
    void setUp() {
        Role merchantRole = new Role("MERCHANT");
        merchantRole.setId(3L);
        merchant = new User();
        merchant.setId(10L);
        merchant.setName("تاجر اختبار");
        merchant.setRole(merchantRole);

        Role courierRole = new Role("COURIER");
        courierRole.setId(2L);
        courierUser = new User();
        courierUser.setId(20L);
        courierUser.setName("مندوب اختبار");
        courierUser.setRole(courierRole);

        pickup = new PickupSchedule();
        pickup.setId(1L);
        pickup.setMerchant(merchant);
        pickup.setPickupDate(LocalDate.now().plusDays(1));
        pickup.setTimeSlot(TimeSlot.MORNING_9_12);
        pickup.setAddress("القاهرة - مصر الجديدة");
        pickup.setEstimatedShipments(5);
        pickup.setStatus(PickupStatus.SCHEDULED);
    }

    @Nested
    @DisplayName("schedulePickup — جدولة موعد استلام")
    class SchedulePickupTests {

        @Test
        @DisplayName("يجب إنشاء موعد استلام جديد بنجاح")
        void schedulePickup_success() {
            when(userRepository.findById(10L)).thenReturn(Optional.of(merchant));
            when(pickupRepository.save(any(PickupSchedule.class))).thenAnswer(inv -> {
                PickupSchedule p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });

            LocalDate futureDate = LocalDate.now().plusDays(1);
            PickupSchedule result = pickupScheduleService.schedulePickup(
                    10L, futureDate, TimeSlot.MORNING_9_12,
                    "القاهرة", 30.0, 31.0, 5, "ملاحظات");

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(PickupStatus.SCHEDULED);
            assertThat(result.getMerchant().getId()).isEqualTo(10L);
            assertThat(result.getEstimatedShipments()).isEqualTo(5);
        }

        @Test
        @DisplayName("يجب رفض تاريخ في الماضي")
        void schedulePickup_pastDate() {
            when(userRepository.findById(10L)).thenReturn(Optional.of(merchant));

            LocalDate pastDate = LocalDate.now().minusDays(1);
            assertThatThrownBy(() -> pickupScheduleService.schedulePickup(
                    10L, pastDate, TimeSlot.MORNING_9_12,
                    "القاهرة", 30.0, 31.0, 5, null))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("المستقبل");
        }

        @Test
        @DisplayName("يجب رفض عدد شحنات صفر أو سالب")
        void schedulePickup_invalidShipmentCount() {
            when(userRepository.findById(10L)).thenReturn(Optional.of(merchant));

            LocalDate futureDate = LocalDate.now().plusDays(1);
            assertThatThrownBy(() -> pickupScheduleService.schedulePickup(
                    10L, futureDate, TimeSlot.AFTERNOON_12_3,
                    "القاهرة", 30.0, 31.0, 0, null))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("أكبر من صفر");
        }
    }

    @Nested
    @DisplayName("assignCourier — تعيين مندوب للاستلام")
    class AssignCourierTests {

        @Test
        @DisplayName("يجب تعيين مندوب بنجاح")
        void assignCourier_success() {
            when(pickupRepository.findById(1L)).thenReturn(Optional.of(pickup));
            when(userRepository.findById(20L)).thenReturn(Optional.of(courierUser));
            when(pickupRepository.save(any(PickupSchedule.class))).thenAnswer(inv -> inv.getArgument(0));

            PickupSchedule result = pickupScheduleService.assignCourier(1L, 20L);

            assertThat(result.getAssignedCourier().getId()).isEqualTo(20L);
            assertThat(result.getStatus()).isEqualTo(PickupStatus.ASSIGNED);
        }

        @Test
        @DisplayName("يجب رفض التعيين إذا لم يكن الموعد مجدولاً")
        void assignCourier_wrongStatus() {
            pickup.setStatus(PickupStatus.IN_PROGRESS);
            when(pickupRepository.findById(1L)).thenReturn(Optional.of(pickup));

            assertThatThrownBy(() -> pickupScheduleService.assignCourier(1L, 20L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("لا يمكن تعيين مندوب");
        }
    }

    @Nested
    @DisplayName("startPickup — بدء الاستلام")
    class StartPickupTests {

        @Test
        @DisplayName("يجب بدء الاستلام بنجاح من حالة ASSIGNED")
        void startPickup_success() {
            pickup.setStatus(PickupStatus.ASSIGNED);
            when(pickupRepository.findById(1L)).thenReturn(Optional.of(pickup));
            when(pickupRepository.save(any(PickupSchedule.class))).thenAnswer(inv -> inv.getArgument(0));

            PickupSchedule result = pickupScheduleService.startPickup(1L);

            assertThat(result.getStatus()).isEqualTo(PickupStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("يجب رفض بدء الاستلام من حالة غير ASSIGNED")
        void startPickup_wrongStatus() {
            pickup.setStatus(PickupStatus.SCHEDULED);
            when(pickupRepository.findById(1L)).thenReturn(Optional.of(pickup));

            assertThatThrownBy(() -> pickupScheduleService.startPickup(1L))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    @Nested
    @DisplayName("completePickup — إتمام الاستلام")
    class CompletePickupTests {

        @Test
        @DisplayName("يجب إتمام الاستلام بنجاح من حالة IN_PROGRESS")
        void completePickup_success() {
            pickup.setStatus(PickupStatus.IN_PROGRESS);
            when(pickupRepository.findById(1L)).thenReturn(Optional.of(pickup));
            when(pickupRepository.save(any(PickupSchedule.class))).thenAnswer(inv -> inv.getArgument(0));

            PickupSchedule result = pickupScheduleService.completePickup(1L);

            assertThat(result.getStatus()).isEqualTo(PickupStatus.COMPLETED);
            assertThat(result.getCompletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("cancelPickup — إلغاء الاستلام")
    class CancelPickupTests {

        @Test
        @DisplayName("يجب إلغاء الاستلام بنجاح")
        void cancelPickup_success() {
            when(pickupRepository.findById(1L)).thenReturn(Optional.of(pickup));
            when(pickupRepository.save(any(PickupSchedule.class))).thenAnswer(inv -> inv.getArgument(0));

            PickupSchedule result = pickupScheduleService.cancelPickup(1L);

            assertThat(result.getStatus()).isEqualTo(PickupStatus.CANCELLED);
        }

        @Test
        @DisplayName("يجب رفض إلغاء استلام مكتمل")
        void cancelPickup_completed() {
            pickup.setStatus(PickupStatus.COMPLETED);
            when(pickupRepository.findById(1L)).thenReturn(Optional.of(pickup));

            assertThatThrownBy(() -> pickupScheduleService.cancelPickup(1L))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    @Nested
    @DisplayName("queries — استعلامات الجدولة")
    class QueryTests {

        @Test
        @DisplayName("يجب إرجاع مواعيد التاجر")
        void getMerchantPickups() {
            when(pickupRepository.findByMerchantIdOrderByPickupDateDesc(eq(10L), any()))
                    .thenReturn(new PageImpl<>(List.of(pickup)));

            Page<PickupSchedule> result = pickupScheduleService.getMerchantPickups(10L, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("يجب إرجاع المواعيد المتأخرة")
        void getOverduePickups() {
            when(pickupRepository.findOverdue(any(LocalDate.class))).thenReturn(List.of(pickup));

            List<PickupSchedule> result = pickupScheduleService.getOverduePickups();

            assertThat(result).hasSize(1);
        }
    }
}
