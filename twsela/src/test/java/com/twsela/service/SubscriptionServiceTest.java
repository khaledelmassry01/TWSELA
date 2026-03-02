package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.domain.MerchantSubscription.BillingCycle;
import com.twsela.domain.MerchantSubscription.SubscriptionStatus;
import com.twsela.domain.SubscriptionPlan.PlanName;
import com.twsela.repository.MerchantSubscriptionRepository;
import com.twsela.repository.SubscriptionPlanRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.DuplicateResourceException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock private MerchantSubscriptionRepository subscriptionRepository;
    @Mock private SubscriptionPlanRepository planRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User merchant;
    private SubscriptionPlan freePlan;
    private SubscriptionPlan basicPlan;
    private SubscriptionPlan proPlan;
    private MerchantSubscription activeSubscription;

    @BeforeEach
    void setUp() {
        merchant = new User();
        merchant.setId(1L);
        merchant.setName("Test Merchant");

        freePlan = new SubscriptionPlan();
        freePlan.setId(1L);
        freePlan.setName(PlanName.FREE);
        freePlan.setMonthlyPrice(BigDecimal.ZERO);
        freePlan.setAnnualPrice(BigDecimal.ZERO);
        freePlan.setMaxShipmentsPerMonth(50);
        freePlan.setSortOrder(1);
        freePlan.setActive(true);

        basicPlan = new SubscriptionPlan();
        basicPlan.setId(2L);
        basicPlan.setName(PlanName.BASIC);
        basicPlan.setMonthlyPrice(new BigDecimal("199.00"));
        basicPlan.setAnnualPrice(new BigDecimal("1990.00"));
        basicPlan.setMaxShipmentsPerMonth(500);
        basicPlan.setSortOrder(2);
        basicPlan.setActive(true);

        proPlan = new SubscriptionPlan();
        proPlan.setId(3L);
        proPlan.setName(PlanName.PRO);
        proPlan.setMonthlyPrice(new BigDecimal("499.00"));
        proPlan.setAnnualPrice(new BigDecimal("4990.00"));
        proPlan.setMaxShipmentsPerMonth(5000);
        proPlan.setSortOrder(3);
        proPlan.setActive(true);

        activeSubscription = new MerchantSubscription(merchant, basicPlan);
        activeSubscription.setId(100L);
        activeSubscription.setStatus(SubscriptionStatus.ACTIVE);
        activeSubscription.setBillingCycle(BillingCycle.MONTHLY);
        activeSubscription.setCurrentPeriodStart(Instant.now());
        activeSubscription.setCurrentPeriodEnd(Instant.now().plus(30, ChronoUnit.DAYS));
    }

    @Test
    @DisplayName("getActivePlans - جلب الخطط النشطة")
    void getActivePlans_shouldReturnActivePlans() {
        when(planRepository.findByActiveTrueOrderBySortOrderAsc())
                .thenReturn(List.of(freePlan, basicPlan, proPlan));

        List<SubscriptionPlan> result = subscriptionService.getActivePlans();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getName()).isEqualTo(PlanName.FREE);
    }

    @Test
    @DisplayName("subscribe - إنشاء اشتراك جديد (تجريبي)")
    void subscribe_shouldCreateTrialSubscription() {
        when(subscriptionRepository.existsByMerchantIdAndStatusIn(eq(1L), anyList())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(merchant));
        when(planRepository.findByName(PlanName.BASIC)).thenReturn(Optional.of(basicPlan));
        when(subscriptionRepository.save(any())).thenAnswer(inv -> {
            MerchantSubscription sub = inv.getArgument(0);
            sub.setId(200L);
            return sub;
        });

        MerchantSubscription result = subscriptionService.subscribe(1L, PlanName.BASIC, BillingCycle.MONTHLY);

        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.TRIAL);
        assertThat(result.getTrialEndsAt()).isNotNull();
        assertThat(result.getBillingCycle()).isEqualTo(BillingCycle.MONTHLY);
        verify(subscriptionRepository).save(any());
    }

    @Test
    @DisplayName("subscribe - رفض اشتراك مكرر")
    void subscribe_shouldRejectDuplicate() {
        when(subscriptionRepository.existsByMerchantIdAndStatusIn(eq(1L), anyList())).thenReturn(true);

        assertThatThrownBy(() -> subscriptionService.subscribe(1L, PlanName.BASIC, BillingCycle.MONTHLY))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("getActiveSubscription - جلب الاشتراك النشط")
    void getActiveSubscription_shouldReturnSubscription() {
        when(subscriptionRepository.findByMerchantIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(Optional.of(activeSubscription));

        MerchantSubscription result = subscriptionService.getActiveSubscription(1L);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    @DisplayName("getActiveSubscription - رمي خطأ عند عدم وجود اشتراك")
    void getActiveSubscription_shouldThrowWhenNotFound() {
        when(subscriptionRepository.findByMerchantIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.getActiveSubscription(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("upgradePlan - ترقية خطة الاشتراك")
    void upgradePlan_shouldUpgrade() {
        when(subscriptionRepository.findByMerchantIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(Optional.of(activeSubscription));
        when(planRepository.findByName(PlanName.PRO)).thenReturn(Optional.of(proPlan));
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MerchantSubscription result = subscriptionService.upgradePlan(1L, PlanName.PRO);

        assertThat(result.getPlan().getName()).isEqualTo(PlanName.PRO);
    }

    @Test
    @DisplayName("upgradePlan - رفض ترقية إلى خطة أقل")
    void upgradePlan_shouldRejectDowngrade() {
        when(subscriptionRepository.findByMerchantIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(Optional.of(activeSubscription));
        when(planRepository.findByName(PlanName.FREE)).thenReturn(Optional.of(freePlan));

        assertThatThrownBy(() -> subscriptionService.upgradePlan(1L, PlanName.FREE))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("cancelSubscription - إلغاء الاشتراك")
    void cancelSubscription_shouldCancel() {
        when(subscriptionRepository.findByMerchantIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(Optional.of(activeSubscription));
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MerchantSubscription result = subscriptionService.cancelSubscription(1L);

        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        assertThat(result.getCancelledAt()).isNotNull();
        assertThat(result.isAutoRenew()).isFalse();
    }

    @Test
    @DisplayName("renew - تجديد الاشتراك")
    void renew_shouldRenewSubscription() {
        activeSubscription.setStatus(SubscriptionStatus.PAST_DUE);
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(activeSubscription));
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MerchantSubscription result = subscriptionService.renew(100L);

        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(result.getCurrentPeriodEnd()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("renew - رفض تجديد اشتراك ملغي")
    void renew_shouldRejectCancelledSubscription() {
        activeSubscription.setStatus(SubscriptionStatus.CANCELLED);
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(activeSubscription));

        assertThatThrownBy(() -> subscriptionService.renew(100L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("isWithinUsageLimit - ضمن الحد المسموح")
    void isWithinUsageLimit_shouldReturnTrue() {
        when(subscriptionRepository.findByMerchantIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(Optional.of(activeSubscription));

        boolean result = subscriptionService.isWithinUsageLimit(1L, 100);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isWithinUsageLimit - تجاوز الحد المسموح")
    void isWithinUsageLimit_shouldReturnFalse() {
        when(subscriptionRepository.findByMerchantIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(Optional.of(activeSubscription));

        boolean result = subscriptionService.isWithinUsageLimit(1L, 600);

        assertThat(result).isFalse();
    }
}
