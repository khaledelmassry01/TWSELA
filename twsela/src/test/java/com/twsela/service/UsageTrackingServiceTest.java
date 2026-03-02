package com.twsela.service;

import com.twsela.domain.UsageTracking;
import com.twsela.repository.UsageTrackingRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsageTrackingServiceTest {

    @Mock private UsageTrackingRepository usageTrackingRepository;
    @Mock private SubscriptionService subscriptionService;

    @InjectMocks
    private UsageTrackingService usageTrackingService;

    private UsageTracking tracking;

    @BeforeEach
    void setUp() {
        tracking = new UsageTracking();
        tracking.setId(1L);
        tracking.setMerchantId(100L);
        tracking.setPeriod("2024-01");
        tracking.setShipmentsCreated(10);
        tracking.setApiCalls(50);
        tracking.setWebhookEvents(3);
    }

    @Test
    @DisplayName("getCurrentPeriod - يجب أن يعيد الفترة الحالية بتنسيق yyyy-MM")
    void getCurrentPeriod_shouldReturnFormattedPeriod() {
        String period = usageTrackingService.getCurrentPeriod();

        assertThat(period).matches("\\d{4}-\\d{2}");
    }

    @Test
    @DisplayName("trackShipmentCreation - تسجيل إنشاء شحنة")
    void trackShipmentCreation_shouldIncrementCount() {
        String period = usageTrackingService.getCurrentPeriod();
        when(usageTrackingRepository.findByMerchantIdAndPeriod(100L, period))
                .thenReturn(Optional.of(tracking));

        usageTrackingService.trackShipmentCreation(100L);

        verify(usageTrackingRepository).incrementShipments(eq(100L), eq(period), any());
    }

    @Test
    @DisplayName("trackShipmentCreation - إنشاء سجل جديد إذا لم يوجد")
    void trackShipmentCreation_shouldCreateRecordIfNotExists() {
        String period = usageTrackingService.getCurrentPeriod();
        when(usageTrackingRepository.findByMerchantIdAndPeriod(100L, period))
                .thenReturn(Optional.empty());
        when(usageTrackingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        usageTrackingService.trackShipmentCreation(100L);

        verify(usageTrackingRepository).save(any(UsageTracking.class));
        verify(usageTrackingRepository).incrementShipments(eq(100L), eq(period), any());
    }

    @Test
    @DisplayName("trackApiCall - تسجيل استدعاء API")
    void trackApiCall_shouldIncrementApiCalls() {
        String period = usageTrackingService.getCurrentPeriod();
        when(usageTrackingRepository.findByMerchantIdAndPeriod(100L, period))
                .thenReturn(Optional.of(tracking));

        usageTrackingService.trackApiCall(100L);

        verify(usageTrackingRepository).incrementApiCalls(eq(100L), eq(period), any());
    }

    @Test
    @DisplayName("isWithinShipmentLimit - ضمن الحد المسموح")
    void isWithinShipmentLimit_shouldReturnTrue() {
        String period = usageTrackingService.getCurrentPeriod();
        when(usageTrackingRepository.findByMerchantIdAndPeriod(100L, period))
                .thenReturn(Optional.of(tracking));
        when(subscriptionService.isWithinUsageLimit(100L, 10)).thenReturn(true);

        boolean result = usageTrackingService.isWithinShipmentLimit(100L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isWithinShipmentLimit - تجاوز الحد المسموح")
    void isWithinShipmentLimit_shouldReturnFalse() {
        String period = usageTrackingService.getCurrentPeriod();
        tracking.setShipmentsCreated(500);
        when(usageTrackingRepository.findByMerchantIdAndPeriod(100L, period))
                .thenReturn(Optional.of(tracking));
        when(subscriptionService.isWithinUsageLimit(100L, 500)).thenReturn(false);

        boolean result = usageTrackingService.isWithinShipmentLimit(100L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("getUsageSummary - جلب ملخص الاستخدام")
    void getUsageSummary_shouldReturnTracking() {
        when(usageTrackingRepository.findByMerchantIdAndPeriod(100L, "2024-01"))
                .thenReturn(Optional.of(tracking));

        UsageTracking result = usageTrackingService.getUsageSummary(100L, "2024-01");

        assertThat(result.getShipmentsCreated()).isEqualTo(10);
        assertThat(result.getApiCalls()).isEqualTo(50);
    }

    @Test
    @DisplayName("getUsageSummary - رمي خطأ عند عدم وجود بيانات")
    void getUsageSummary_shouldThrowNotFound() {
        when(usageTrackingRepository.findByMerchantIdAndPeriod(100L, "2024-01"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> usageTrackingService.getUsageSummary(100L, "2024-01"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getCurrentUsage - جلب الاستخدام الحالي (إنشاء سجل فارغ)")
    void getCurrentUsage_shouldReturnEmptyIfNotExists() {
        String period = usageTrackingService.getCurrentPeriod();
        when(usageTrackingRepository.findByMerchantIdAndPeriod(100L, period))
                .thenReturn(Optional.empty());

        UsageTracking result = usageTrackingService.getCurrentUsage(100L);

        assertThat(result.getMerchantId()).isEqualTo(100L);
        assertThat(result.getShipmentsCreated()).isEqualTo(0);
    }

    @Test
    @DisplayName("trackWebhookEvent - تسجيل حدث webhook")
    void trackWebhookEvent_shouldIncrementWebhookEvents() {
        String period = usageTrackingService.getCurrentPeriod();
        when(usageTrackingRepository.findByMerchantIdAndPeriod(100L, period))
                .thenReturn(Optional.of(tracking));
        when(usageTrackingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        usageTrackingService.trackWebhookEvent(100L);

        assertThat(tracking.getWebhookEvents()).isEqualTo(4);
        verify(usageTrackingRepository).save(tracking);
    }
}
