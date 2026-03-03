package com.twsela.service;

import com.twsela.domain.KPISnapshot;
import com.twsela.repository.KPISnapshotRepository;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KPISnapshotServiceTest {

    @Mock private KPISnapshotRepository snapshotRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private KPISnapshotService kpiSnapshotService;

    private LocalDate today;
    private KPISnapshot snapshot;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
        snapshot = new KPISnapshot(today);
        snapshot.setId(1L);
        snapshot.setTotalRevenue(new BigDecimal("5000.00"));
        snapshot.setTotalShipments(100);
        snapshot.setDeliveredShipments(85);
        snapshot.setReturnedShipments(5);
    }

    @Nested
    @DisplayName("captureSnapshot — التقاط لقطة يومية")
    class CaptureSnapshotTests {

        @Test
        @DisplayName("يجب التقاط لقطة جديدة بنجاح")
        void captureSnapshot_createsNew() {
            when(snapshotRepository.findBySnapshotDate(any(LocalDate.class))).thenReturn(Optional.empty());
            when(shipmentRepository.sumDeliveryFeeByStatusNameAndCreatedAtBetween(eq("DELIVERED"), any(), any()))
                    .thenReturn(new BigDecimal("5000.00"));
            when(shipmentRepository.countByCreatedAtBetween(any(), any())).thenReturn(100L);
            when(shipmentRepository.countByStatusNameAndCreatedAtBetween(eq("DELIVERED"), any(), any())).thenReturn(85L);
            when(shipmentRepository.countByStatusNameAndCreatedAtBetween(eq("RETURNED"), any(), any())).thenReturn(5L);
            when(userRepository.findByRoleName("COURIER")).thenReturn(Collections.emptyList());
            when(userRepository.findByRoleName("MERCHANT")).thenReturn(Collections.emptyList());
            when(snapshotRepository.save(any(KPISnapshot.class))).thenAnswer(inv -> {
                KPISnapshot s = inv.getArgument(0);
                s.setId(1L);
                return s;
            });

            KPISnapshot result = kpiSnapshotService.captureSnapshot(today);

            assertThat(result).isNotNull();
            assertThat(result.getTotalRevenue()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertThat(result.getTotalShipments()).isEqualTo(100);
            verify(snapshotRepository).save(any(KPISnapshot.class));
        }
    }

    @Nested
    @DisplayName("getSnapshot — جلب لقطة بتاريخ محدد")
    class GetSnapshotTests {

        @Test
        @DisplayName("يجب إرجاع اللقطة إذا وُجدت")
        void getSnapshot_found() {
            when(snapshotRepository.findBySnapshotDate(today)).thenReturn(Optional.of(snapshot));

            Optional<KPISnapshot> result = kpiSnapshotService.getSnapshot(today);

            assertThat(result).isPresent();
            assertThat(result.get().getTotalRevenue()).isEqualByComparingTo(new BigDecimal("5000.00"));
        }
    }

    @Nested
    @DisplayName("getTrend — اتجاه مؤشر أداء")
    class GetTrendTests {

        @Test
        @DisplayName("يجب عرض بيانات اتجاه الإيرادات")
        void getTrend_revenue() {
            KPISnapshot s1 = new KPISnapshot(today.minusDays(2));
            s1.setTotalRevenue(new BigDecimal("3000.00"));
            KPISnapshot s2 = new KPISnapshot(today.minusDays(1));
            s2.setTotalRevenue(new BigDecimal("5000.00"));

            when(snapshotRepository.findBySnapshotDateBetweenOrderBySnapshotDateAsc(
                    today.minusDays(2), today)).thenReturn(List.of(s1, s2));

            List<Map<String, Object>> trend = kpiSnapshotService.getTrend("revenue", today.minusDays(2), today);

            assertThat(trend).hasSize(2);
            assertThat(trend.get(0).get("value")).isEqualTo(new BigDecimal("3000.00"));
            assertThat(trend.get(1).get("value")).isEqualTo(new BigDecimal("5000.00"));
        }
    }
}
