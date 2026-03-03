package com.twsela.service;

import com.twsela.domain.DomainEvent;
import com.twsela.domain.OutboxMessage;
import com.twsela.repository.DomainEventRepository;
import com.twsela.repository.OutboxMessageRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private DomainEventRepository domainEventRepository;
    @Mock
    private OutboxMessageRepository outboxMessageRepository;

    @InjectMocks
    private EventPublisher eventPublisher;

    private DomainEvent sampleEvent;

    @BeforeEach
    void setUp() {
        sampleEvent = new DomainEvent();
        sampleEvent.setId(1L);
        sampleEvent.setEventId("uuid-123");
        sampleEvent.setEventType("SHIPMENT_STATUS_CHANGED");
        sampleEvent.setAggregateType("Shipment");
        sampleEvent.setAggregateId(10L);
        sampleEvent.setPayload("{\"status\":\"DELIVERED\"}");
        sampleEvent.setVersion(1);
        sampleEvent.setStatus(DomainEvent.EventStatus.PENDING);
    }

    @Test
    @DisplayName("نشر حدث جديد مع outbox")
    void publish_shouldCreateEventAndOutbox() {
        when(domainEventRepository.findMaxVersionByAggregate("Shipment", 10L)).thenReturn(null);
        when(domainEventRepository.save(any(DomainEvent.class))).thenReturn(sampleEvent);
        when(outboxMessageRepository.save(any(OutboxMessage.class))).thenReturn(new OutboxMessage());

        DomainEvent result = eventPublisher.publish("SHIPMENT_STATUS_CHANGED", "Shipment", 10L,
                "{\"status\":\"DELIVERED\"}", null);

        assertThat(result).isNotNull();
        assertThat(result.getEventType()).isEqualTo("SHIPMENT_STATUS_CHANGED");

        ArgumentCaptor<OutboxMessage> outboxCaptor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxMessageRepository).save(outboxCaptor.capture());
        assertThat(outboxCaptor.getValue().getEventType()).isEqualTo("SHIPMENT_STATUS_CHANGED");
        assertThat(outboxCaptor.getValue().isPublished()).isFalse();
    }

    @Test
    @DisplayName("نشر حدث مع زيادة الإصدار")
    void publish_shouldIncrementVersion() {
        when(domainEventRepository.findMaxVersionByAggregate("Shipment", 10L)).thenReturn(3);
        when(domainEventRepository.save(any(DomainEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(outboxMessageRepository.save(any(OutboxMessage.class))).thenReturn(new OutboxMessage());

        DomainEvent result = eventPublisher.publish("SHIPMENT_ASSIGNED", "Shipment", 10L, "{}", null);

        assertThat(result.getVersion()).isEqualTo(4);
    }

    @Test
    @DisplayName("جلب أحداث حسب الـ aggregate")
    void getEventsByAggregate_shouldReturnEvents() {
        when(domainEventRepository.findByAggregateTypeAndAggregateIdOrderByVersionAsc("Shipment", 10L))
                .thenReturn(List.of(sampleEvent));

        List<DomainEvent> result = eventPublisher.getEventsByAggregate("Shipment", 10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEventType()).isEqualTo("SHIPMENT_STATUS_CHANGED");
    }

    @Test
    @DisplayName("جلب حدث حسب المعرف")
    void getEventByEventId_shouldReturnEvent() {
        when(domainEventRepository.findByEventId("uuid-123")).thenReturn(Optional.of(sampleEvent));

        Optional<DomainEvent> result = eventPublisher.getEventByEventId("uuid-123");

        assertThat(result).isPresent();
        assertThat(result.get().getEventId()).isEqualTo("uuid-123");
    }

    @Test
    @DisplayName("جلب أحداث حسب النوع")
    void getEventsByType_shouldReturnEvents() {
        when(domainEventRepository.findByEventType("SHIPMENT_STATUS_CHANGED"))
                .thenReturn(List.of(sampleEvent));

        List<DomainEvent> result = eventPublisher.getEventsByType("SHIPMENT_STATUS_CHANGED");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("تحديث حالة الحدث إلى PUBLISHED")
    void updateStatus_toPublished_shouldSetPublishedAt() {
        when(domainEventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));
        when(domainEventRepository.save(any(DomainEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        DomainEvent result = eventPublisher.updateStatus(1L, DomainEvent.EventStatus.PUBLISHED);

        assertThat(result.getStatus()).isEqualTo(DomainEvent.EventStatus.PUBLISHED);
        assertThat(result.getPublishedAt()).isNotNull();
    }

    @Test
    @DisplayName("تحديث حالة الحدث إلى PROCESSED")
    void updateStatus_toProcessed_shouldSetProcessedAt() {
        when(domainEventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));
        when(domainEventRepository.save(any(DomainEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        DomainEvent result = eventPublisher.updateStatus(1L, DomainEvent.EventStatus.PROCESSED);

        assertThat(result.getStatus()).isEqualTo(DomainEvent.EventStatus.PROCESSED);
        assertThat(result.getProcessedAt()).isNotNull();
    }
}
