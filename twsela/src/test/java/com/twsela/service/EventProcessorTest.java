package com.twsela.service;

import com.twsela.domain.DomainEvent;
import com.twsela.domain.EventSubscription;
import com.twsela.repository.DomainEventRepository;
import com.twsela.repository.EventSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventProcessorTest {

    @Mock
    private DomainEventRepository domainEventRepository;
    @Mock
    private EventSubscriptionRepository eventSubscriptionRepository;
    @Mock
    private DeadLetterService deadLetterService;

    @InjectMocks
    private EventProcessor eventProcessor;

    private DomainEvent sampleEvent;
    private EventSubscription sampleSub;

    @BeforeEach
    void setUp() {
        sampleEvent = new DomainEvent();
        sampleEvent.setId(1L);
        sampleEvent.setEventId("uuid-100");
        sampleEvent.setEventType("SHIPMENT_STATUS_CHANGED");
        sampleEvent.setAggregateType("Shipment");
        sampleEvent.setAggregateId(5L);
        sampleEvent.setStatus(DomainEvent.EventStatus.PUBLISHED);

        sampleSub = new EventSubscription();
        sampleSub.setId(1L);
        sampleSub.setSubscriberName("ShipmentEventHandler");
        sampleSub.setEventType("SHIPMENT_STATUS_CHANGED");
        sampleSub.setHandlerClass("com.twsela.service.ShipmentEventHandler");
        sampleSub.setActive(true);
    }

    @Test
    @DisplayName("معالجة حدث مع مشتركين نشطين")
    void processEvent_withSubscribers_shouldMarkProcessed() {
        when(eventSubscriptionRepository.findByEventTypeAndActiveTrue("SHIPMENT_STATUS_CHANGED"))
                .thenReturn(List.of(sampleSub));
        when(domainEventRepository.save(any(DomainEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(eventSubscriptionRepository.save(any(EventSubscription.class))).thenAnswer(inv -> inv.getArgument(0));

        eventProcessor.processEvent(sampleEvent);

        assertThat(sampleEvent.getStatus()).isEqualTo(DomainEvent.EventStatus.PROCESSED);
        assertThat(sampleEvent.getProcessedAt()).isNotNull();
        verify(deadLetterService, never()).moveToDeadLetter(any(), anyString());
    }

    @Test
    @DisplayName("معالجة حدث بدون مشتركين")
    void processEvent_noSubscribers_shouldMarkProcessed() {
        when(eventSubscriptionRepository.findByEventTypeAndActiveTrue("SHIPMENT_STATUS_CHANGED"))
                .thenReturn(Collections.emptyList());
        when(domainEventRepository.save(any(DomainEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        eventProcessor.processEvent(sampleEvent);

        assertThat(sampleEvent.getStatus()).isEqualTo(DomainEvent.EventStatus.PROCESSED);
    }

    @Test
    @DisplayName("فشل معالجة حدث — نقل للـ DLQ")
    void processEvent_subscriberFails_shouldMoveToDLQ() {
        EventProcessor spyProcessor = spy(eventProcessor);
        doThrow(new RuntimeException("Processing failed"))
                .when(spyProcessor).routeToSubscriber(any(DomainEvent.class), any(EventSubscription.class));

        when(eventSubscriptionRepository.findByEventTypeAndActiveTrue("SHIPMENT_STATUS_CHANGED"))
                .thenReturn(List.of(sampleSub));
        when(domainEventRepository.save(any(DomainEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(eventSubscriptionRepository.save(any(EventSubscription.class))).thenAnswer(inv -> inv.getArgument(0));

        spyProcessor.processEvent(sampleEvent);

        assertThat(sampleEvent.getStatus()).isEqualTo(DomainEvent.EventStatus.FAILED);
        verify(deadLetterService).moveToDeadLetter(eq(sampleEvent), anyString());
    }

    @Test
    @DisplayName("معالجة الأحداث المعلقة")
    void processPendingEvents_shouldProcessAll() {
        DomainEvent event2 = new DomainEvent();
        event2.setId(2L);
        event2.setEventId("uuid-200");
        event2.setEventType("PAYMENT_RECEIVED");
        event2.setAggregateType("Payment");
        event2.setAggregateId(7L);
        event2.setStatus(DomainEvent.EventStatus.PUBLISHED);

        when(domainEventRepository.findByStatusOrderByCreatedAtAsc(DomainEvent.EventStatus.PUBLISHED))
                .thenReturn(List.of(sampleEvent, event2));
        when(eventSubscriptionRepository.findByEventTypeAndActiveTrue(anyString()))
                .thenReturn(Collections.emptyList());
        when(domainEventRepository.save(any(DomainEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        int processed = eventProcessor.processPendingEvents();

        assertThat(processed).isEqualTo(2);
    }

    @Test
    @DisplayName("معالجة الأحداث المعلقة — لا توجد أحداث")
    void processPendingEvents_noPending_shouldReturnZero() {
        when(domainEventRepository.findByStatusOrderByCreatedAtAsc(DomainEvent.EventStatus.PUBLISHED))
                .thenReturn(Collections.emptyList());

        int processed = eventProcessor.processPendingEvents();

        assertThat(processed).isEqualTo(0);
    }

    @Test
    @DisplayName("تحديث عدد الفشل للمشترك عند فشل التوجيه")
    void processEvent_subscriberFails_shouldIncrementFailureCount() {
        EventProcessor spyProcessor = spy(eventProcessor);
        doThrow(new RuntimeException("fail"))
                .when(spyProcessor).routeToSubscriber(any(DomainEvent.class), any(EventSubscription.class));

        sampleSub.setFailureCount(2);
        when(eventSubscriptionRepository.findByEventTypeAndActiveTrue("SHIPMENT_STATUS_CHANGED"))
                .thenReturn(List.of(sampleSub));
        when(domainEventRepository.save(any(DomainEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(eventSubscriptionRepository.save(any(EventSubscription.class))).thenAnswer(inv -> inv.getArgument(0));

        spyProcessor.processEvent(sampleEvent);

        assertThat(sampleSub.getFailureCount()).isEqualTo(3);
    }
}
