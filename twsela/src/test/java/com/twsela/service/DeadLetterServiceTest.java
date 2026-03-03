package com.twsela.service;

import com.twsela.domain.DeadLetterEvent;
import com.twsela.domain.DomainEvent;
import com.twsela.domain.User;
import com.twsela.repository.DeadLetterEventRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeadLetterServiceTest {

    @Mock
    private DeadLetterEventRepository deadLetterEventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private DeadLetterService deadLetterService;

    private DomainEvent sampleEvent;
    private DeadLetterEvent sampleDLE;
    private User resolverUser;

    @BeforeEach
    void setUp() {
        sampleEvent = new DomainEvent();
        sampleEvent.setId(1L);
        sampleEvent.setEventId("uuid-dle");
        sampleEvent.setEventType("SHIPMENT_STATUS_CHANGED");
        sampleEvent.setAggregateType("Shipment");
        sampleEvent.setAggregateId(10L);
        sampleEvent.setStatus(DomainEvent.EventStatus.FAILED);

        sampleDLE = new DeadLetterEvent();
        sampleDLE.setId(1L);
        sampleDLE.setOriginalEvent(sampleEvent);
        sampleDLE.setFailureReason("Processing failed");
        sampleDLE.setFailureCount(1);
        sampleDLE.setResolved(false);

        resolverUser = new User();
        resolverUser.setId(100L);
        resolverUser.setName("مدير النظام");
    }

    @Test
    @DisplayName("نقل حدث فاشل إلى DLQ")
    void moveToDeadLetter_shouldCreateEntry() {
        when(deadLetterEventRepository.save(any(DeadLetterEvent.class))).thenAnswer(inv -> {
            DeadLetterEvent dle = inv.getArgument(0);
            dle.setId(1L);
            return dle;
        });

        DeadLetterEvent result = deadLetterService.moveToDeadLetter(sampleEvent, "Subscriber failure");

        assertThat(result).isNotNull();
        assertThat(result.getOriginalEvent()).isEqualTo(sampleEvent);
        assertThat(result.getFailureReason()).isEqualTo("Subscriber failure");
        assertThat(result.isResolved()).isFalse();
    }

    @Test
    @DisplayName("إعادة محاولة حدث فاشل")
    void retry_shouldIncrementFailuresAndRepublish() {
        when(deadLetterEventRepository.findById(1L)).thenReturn(Optional.of(sampleDLE));
        when(deadLetterEventRepository.save(any(DeadLetterEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(eventPublisher.updateStatus(eq(1L), eq(DomainEvent.EventStatus.PENDING))).thenReturn(sampleEvent);

        DeadLetterEvent result = deadLetterService.retry(1L);

        assertThat(result.getFailureCount()).isEqualTo(2);
        verify(eventPublisher).updateStatus(1L, DomainEvent.EventStatus.PENDING);
    }

    @Test
    @DisplayName("حل حدث فاشل")
    void resolve_shouldMarkResolved() {
        when(deadLetterEventRepository.findById(1L)).thenReturn(Optional.of(sampleDLE));
        when(userRepository.findById(100L)).thenReturn(Optional.of(resolverUser));
        when(deadLetterEventRepository.save(any(DeadLetterEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        DeadLetterEvent result = deadLetterService.resolve(1L, 100L);

        assertThat(result.isResolved()).isTrue();
        assertThat(result.getResolvedBy()).isEqualTo(resolverUser);
        assertThat(result.getResolvedAt()).isNotNull();
    }

    @Test
    @DisplayName("الأحداث الفاشلة غير المحلولة")
    void getUnresolved_shouldReturnList() {
        when(deadLetterEventRepository.findUnresolved()).thenReturn(List.of(sampleDLE));

        List<DeadLetterEvent> result = deadLetterService.getUnresolved();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isResolved()).isFalse();
    }

    @Test
    @DisplayName("إحصائيات DLQ")
    void getStats_shouldReturnCounts() {
        when(deadLetterEventRepository.countByResolved(false)).thenReturn(5L);
        when(deadLetterEventRepository.countByResolved(true)).thenReturn(3L);
        when(deadLetterEventRepository.count()).thenReturn(8L);

        Map<String, Object> stats = deadLetterService.getStats();

        assertThat(stats.get("unresolvedCount")).isEqualTo(5L);
        assertThat(stats.get("resolvedCount")).isEqualTo(3L);
        assertThat(stats.get("totalCount")).isEqualTo(8L);
    }
}
