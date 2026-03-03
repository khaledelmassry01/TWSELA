package com.twsela.web;

import com.twsela.domain.DomainEvent;
import com.twsela.domain.EventSubscription;
import com.twsela.repository.EventSubscriptionRepository;
import com.twsela.service.EventPublisher;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.EventDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * متحكم الأحداث والاشتراكات.
 */
@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "إدارة أحداث النظام والاشتراكات")
public class EventController {

    private final EventPublisher eventPublisher;
    private final EventSubscriptionRepository eventSubscriptionRepository;

    public EventController(EventPublisher eventPublisher,
                            EventSubscriptionRepository eventSubscriptionRepository) {
        this.eventPublisher = eventPublisher;
        this.eventSubscriptionRepository = eventSubscriptionRepository;
    }

    @GetMapping
    @Operation(summary = "قائمة الأحداث")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<DomainEvent>>> getEvents(
            @RequestParam(required = false) String eventType) {
        List<DomainEvent> events;
        if (eventType != null && !eventType.isBlank()) {
            events = eventPublisher.getEventsByType(eventType);
        } else {
            events = eventPublisher.getEventsByType("*");
        }
        return ResponseEntity.ok(ApiResponse.ok(events));
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "تفاصيل حدث")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DomainEvent>> getEvent(@PathVariable String eventId) {
        Optional<DomainEvent> event = eventPublisher.getEventByEventId(eventId);
        return event.map(e -> ResponseEntity.ok(ApiResponse.ok(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/subscriptions")
    @Operation(summary = "الاشتراكات النشطة")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<EventSubscription>>> getSubscriptions() {
        List<EventSubscription> subs = eventSubscriptionRepository.findActiveSubscriptions();
        return ResponseEntity.ok(ApiResponse.ok(subs));
    }

    @PostMapping("/subscriptions")
    @Operation(summary = "إنشاء اشتراك")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EventSubscription>> createSubscription(
            @Valid @RequestBody EventDTO.SubscriptionRequest request) {
        EventSubscription sub = new EventSubscription();
        sub.setSubscriberName(request.getSubscriberName());
        sub.setEventType(request.getEventType());
        sub.setHandlerClass(request.getHandlerClass());
        sub.setFilterExpression(request.getFilterExpression());
        sub.setRetryPolicy(request.getRetryPolicy());
        sub.setActive(true);
        EventSubscription saved = eventSubscriptionRepository.save(sub);
        return ResponseEntity.ok(ApiResponse.ok(saved, "تم إنشاء الاشتراك بنجاح"));
    }

    @PutMapping("/subscriptions/{id}")
    @Operation(summary = "تعديل اشتراك")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EventSubscription>> updateSubscription(
            @PathVariable Long id,
            @Valid @RequestBody EventDTO.SubscriptionRequest request) {
        EventSubscription sub = eventSubscriptionRepository.findById(id)
                .orElseThrow(() -> new com.twsela.web.exception.ResourceNotFoundException(
                        "EventSubscription", "id", id));
        sub.setSubscriberName(request.getSubscriberName());
        sub.setEventType(request.getEventType());
        sub.setHandlerClass(request.getHandlerClass());
        sub.setFilterExpression(request.getFilterExpression());
        sub.setRetryPolicy(request.getRetryPolicy());
        EventSubscription updated = eventSubscriptionRepository.save(sub);
        return ResponseEntity.ok(ApiResponse.ok(updated, "تم تعديل الاشتراك بنجاح"));
    }
}
