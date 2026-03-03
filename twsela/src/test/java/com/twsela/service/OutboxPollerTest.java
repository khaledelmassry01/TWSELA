package com.twsela.service;

import com.twsela.domain.DomainEvent;
import com.twsela.domain.OutboxMessage;
import com.twsela.repository.DomainEventRepository;
import com.twsela.repository.OutboxMessageRepository;
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
class OutboxPollerTest {

    @Mock
    private OutboxMessageRepository outboxMessageRepository;
    @Mock
    private DomainEventRepository domainEventRepository;

    @InjectMocks
    private OutboxPoller outboxPoller;

    private OutboxMessage sampleMessage;
    private DomainEvent sampleEvent;

    @BeforeEach
    void setUp() {
        sampleMessage = new OutboxMessage();
        sampleMessage.setId(1L);
        sampleMessage.setAggregateType("Shipment");
        sampleMessage.setAggregateId(10L);
        sampleMessage.setEventType("SHIPMENT_STATUS_CHANGED");
        sampleMessage.setPayload("{\"status\":\"DELIVERED\"}");
        sampleMessage.setPublished(false);

        sampleEvent = new DomainEvent();
        sampleEvent.setId(1L);
        sampleEvent.setEventId("uuid-abc");
        sampleEvent.setEventType("SHIPMENT_STATUS_CHANGED");
        sampleEvent.setAggregateType("Shipment");
        sampleEvent.setAggregateId(10L);
        sampleEvent.setStatus(DomainEvent.EventStatus.PENDING);
    }

    @Test
    @DisplayName("نشر رسالة outbox غير منشورة")
    void pollAndPublish_shouldPublishMessages() {
        when(outboxMessageRepository.findByPublishedFalseOrderByCreatedAtAsc())
                .thenReturn(List.of(sampleMessage));
        when(domainEventRepository.findByAggregateTypeAndAggregateIdOrderByVersionAsc("Shipment", 10L))
                .thenReturn(List.of(sampleEvent));
        when(domainEventRepository.save(any(DomainEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(outboxMessageRepository.save(any(OutboxMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        int published = outboxPoller.pollAndPublish();

        assertThat(published).isEqualTo(1);
        assertThat(sampleEvent.getStatus()).isEqualTo(DomainEvent.EventStatus.PUBLISHED);
        assertThat(sampleMessage.isPublished()).isTrue();
    }

    @Test
    @DisplayName("لا توجد رسائل غير منشورة")
    void pollAndPublish_noMessages_shouldReturnZero() {
        when(outboxMessageRepository.findByPublishedFalseOrderByCreatedAtAsc())
                .thenReturn(Collections.emptyList());

        int published = outboxPoller.pollAndPublish();

        assertThat(published).isEqualTo(0);
    }

    @Test
    @DisplayName("تجاهل أحداث غير PENDING")
    void pollAndPublish_shouldSkipNonPendingEvents() {
        sampleEvent.setStatus(DomainEvent.EventStatus.PROCESSED);

        when(outboxMessageRepository.findByPublishedFalseOrderByCreatedAtAsc())
                .thenReturn(List.of(sampleMessage));
        when(domainEventRepository.findByAggregateTypeAndAggregateIdOrderByVersionAsc("Shipment", 10L))
                .thenReturn(List.of(sampleEvent));
        when(outboxMessageRepository.save(any(OutboxMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        int published = outboxPoller.pollAndPublish();

        assertThat(published).isEqualTo(1);
        assertThat(sampleEvent.getStatus()).isEqualTo(DomainEvent.EventStatus.PROCESSED); // unchanged
    }

    @Test
    @DisplayName("نشر عدة رسائل")
    void pollAndPublish_multipleMessages_shouldPublishAll() {
        OutboxMessage msg2 = new OutboxMessage();
        msg2.setId(2L);
        msg2.setAggregateType("Payment");
        msg2.setAggregateId(20L);
        msg2.setEventType("PAYMENT_RECEIVED");
        msg2.setPublished(false);

        DomainEvent event2 = new DomainEvent();
        event2.setId(2L);
        event2.setEventType("PAYMENT_RECEIVED");
        event2.setAggregateType("Payment");
        event2.setAggregateId(20L);
        event2.setStatus(DomainEvent.EventStatus.PENDING);

        when(outboxMessageRepository.findByPublishedFalseOrderByCreatedAtAsc())
                .thenReturn(List.of(sampleMessage, msg2));
        when(domainEventRepository.findByAggregateTypeAndAggregateIdOrderByVersionAsc("Shipment", 10L))
                .thenReturn(List.of(sampleEvent));
        when(domainEventRepository.findByAggregateTypeAndAggregateIdOrderByVersionAsc("Payment", 20L))
                .thenReturn(List.of(event2));
        when(domainEventRepository.save(any(DomainEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(outboxMessageRepository.save(any(OutboxMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        int published = outboxPoller.pollAndPublish();

        assertThat(published).isEqualTo(2);
    }

    @Test
    @DisplayName("خطأ في رسالة واحدة لا يوقف البقية")
    void pollAndPublish_errorInOne_shouldContinue() {
        OutboxMessage msg2 = new OutboxMessage();
        msg2.setId(2L);
        msg2.setAggregateType("Payment");
        msg2.setAggregateId(20L);
        msg2.setEventType("PAYMENT_RECEIVED");
        msg2.setPublished(false);

        DomainEvent event2 = new DomainEvent();
        event2.setId(2L);
        event2.setEventType("PAYMENT_RECEIVED");
        event2.setAggregateType("Payment");
        event2.setAggregateId(20L);
        event2.setStatus(DomainEvent.EventStatus.PENDING);

        when(outboxMessageRepository.findByPublishedFalseOrderByCreatedAtAsc())
                .thenReturn(List.of(sampleMessage, msg2));
        when(domainEventRepository.findByAggregateTypeAndAggregateIdOrderByVersionAsc("Shipment", 10L))
                .thenThrow(new RuntimeException("DB error"));
        when(domainEventRepository.findByAggregateTypeAndAggregateIdOrderByVersionAsc("Payment", 20L))
                .thenReturn(List.of(event2));
        when(domainEventRepository.save(any(DomainEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(outboxMessageRepository.save(any(OutboxMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        int published = outboxPoller.pollAndPublish();

        assertThat(published).isEqualTo(1);
    }
}
