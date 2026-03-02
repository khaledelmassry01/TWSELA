package com.twsela.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.User;
import com.twsela.domain.WebhookEvent;
import com.twsela.domain.WebhookEvent.DeliveryStatus;
import com.twsela.domain.WebhookSubscription;
import com.twsela.repository.WebhookEventRepository;
import com.twsela.repository.WebhookSubscriptionRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock private WebhookSubscriptionRepository subscriptionRepository;
    @Mock private WebhookEventRepository eventRepository;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private WebhookService webhookService;

    private User merchant;
    private WebhookSubscription subscription;

    @BeforeEach
    void setUp() {
        merchant = new User();
        merchant.setId(1L);
        merchant.setName("Test Merchant");

        subscription = new WebhookSubscription(merchant, "https://example.com/hook", "secret123", "SHIPMENT_CREATED,STATUS_CHANGED");
        subscription.setId(10L);
    }

    @Test
    @DisplayName("subscribe - إنشاء اشتراك بنجاح")
    void subscribe_success() {
        when(subscriptionRepository.save(any(WebhookSubscription.class))).thenAnswer(inv -> {
            WebhookSubscription s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });

        WebhookSubscription result = webhookService.subscribe(merchant, "https://example.com/hook",
                List.of("SHIPMENT_CREATED", "STATUS_CHANGED"));

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUrl()).isEqualTo("https://example.com/hook");
        verify(subscriptionRepository).save(any());
    }

    @Test
    @DisplayName("subscribe - URL فارغ")
    void subscribe_emptyUrl() {
        assertThatThrownBy(() -> webhookService.subscribe(merchant, "", List.of("SHIPMENT_CREATED")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("subscribe - لا أحداث")
    void subscribe_noEvents() {
        assertThatThrownBy(() -> webhookService.subscribe(merchant, "https://example.com/hook", List.of()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("unsubscribe - إلغاء الاشتراك بنجاح")
    void unsubscribe_success() {
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any())).thenReturn(subscription);

        webhookService.unsubscribe(10L, 1L);

        assertThat(subscription.isActive()).isFalse();
        verify(subscriptionRepository).save(subscription);
    }

    @Test
    @DisplayName("unsubscribe - تاجر آخر")
    void unsubscribe_wrongMerchant() {
        when(subscriptionRepository.findById(10L)).thenReturn(Optional.of(subscription));

        assertThatThrownBy(() -> webhookService.unsubscribe(10L, 999L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("getSubscriptions - قائمة الاشتراكات")
    void getSubscriptions_success() {
        when(subscriptionRepository.findByMerchantId(1L)).thenReturn(List.of(subscription));

        List<WebhookSubscription> result = webhookService.getSubscriptions(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getSubscription - غير موجود")
    void getSubscription_notFound() {
        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> webhookService.getSubscription(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getEvents - سجل الأحداث")
    void getEvents_success() {
        WebhookEvent evt = new WebhookEvent(subscription, "SHIPMENT_CREATED", "{}");
        evt.setId(1L);
        when(eventRepository.findBySubscriptionIdOrderByCreatedAtDesc(eq(10L), any()))
                .thenReturn(new PageImpl<>(List.of(evt)));

        var page = webhookService.getEvents(10L, PageRequest.of(0, 20));
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("retryFailed - إعادة محاولة الأحداث الفاشلة")
    void retryFailed_success() {
        WebhookEvent failedEvt = new WebhookEvent(subscription, "STATUS_CHANGED", "{\"test\":true}");
        failedEvt.setId(1L);
        failedEvt.setStatus(DeliveryStatus.FAILED);
        failedEvt.setAttempts(2);

        when(eventRepository.findByStatusAndAttemptsLessThan(DeliveryStatus.FAILED, 5))
                .thenReturn(List.of(failedEvt));
        when(eventRepository.save(any())).thenReturn(failedEvt);

        int retried = webhookService.retryFailed();
        assertThat(retried).isEqualTo(1);
        verify(eventRepository, atLeastOnce()).save(any());
    }
}
