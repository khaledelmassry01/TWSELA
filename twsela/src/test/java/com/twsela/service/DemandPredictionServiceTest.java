package com.twsela.service;

import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.ZoneRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("توقع الطلب - DemandPredictionService")
class DemandPredictionServiceTest {

    @Mock private ShipmentRepository shipmentRepository;
    @Mock private ZoneRepository zoneRepository;

    @InjectMocks private DemandPredictionService service;

    @Test
    @DisplayName("توقع الطلب اليومي — بيانات موجودة")
    void predictDailyDemandWithData() {
        when(zoneRepository.existsById(1L)).thenReturn(true);
        when(shipmentRepository.countByZoneIdAndCreatedAtBetween(eq(1L), any(), any()))
                .thenReturn(280L); // 280 in 28 days = avg 10/day

        int predicted = service.predictDailyDemand(1L, LocalDate.now().plusDays(1));

        assertThat(predicted).isGreaterThan(0);
    }

    @Test
    @DisplayName("توقع الطلب اليومي — بدون بيانات")
    void predictDailyDemandNoData() {
        when(zoneRepository.existsById(1L)).thenReturn(true);
        when(shipmentRepository.countByZoneIdAndCreatedAtBetween(eq(1L), any(), any()))
                .thenReturn(0L);

        int predicted = service.predictDailyDemand(1L, LocalDate.now());

        assertThat(predicted).isEqualTo(0);
    }

    @Test
    @DisplayName("احتياج المناديب — طلب عالي")
    void predictCourierNeedHighDemand() {
        when(zoneRepository.existsById(1L)).thenReturn(true);
        when(shipmentRepository.countByZoneIdAndCreatedAtBetween(eq(1L), any(), any()))
                .thenReturn(560L); // avg 20/day => 1 courier

        int couriers = service.predictCourierNeed(1L, LocalDate.now());

        assertThat(couriers).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("عامل يوم الأسبوع — بدون بيانات يرجع 1.0")
    void dayOfWeekFactorDefaultsToOne() {
        when(shipmentRepository.countByZoneIdAndCreatedAtBetween(eq(1L), any(), any()))
                .thenReturn(0L);

        double factor = service.getDayOfWeekFactor(1L, DayOfWeek.MONDAY);

        assertThat(factor).isEqualTo(1.0);
    }
}
