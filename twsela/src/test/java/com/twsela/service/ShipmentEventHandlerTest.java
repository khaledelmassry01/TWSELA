package com.twsela.service;

import com.twsela.domain.DomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ShipmentEventHandlerTest {

    private ShipmentEventHandler shipmentEventHandler;

    @BeforeEach
    void setUp() {
        shipmentEventHandler = new ShipmentEventHandler();
    }

    private DomainEvent createEvent(String eventType) {
        DomainEvent event = new DomainEvent();
        event.setId(1L);
        event.setEventId("uuid-shipment");
        event.setEventType(eventType);
        event.setAggregateType("Shipment");
        event.setAggregateId(10L);
        event.setPayload("{\"shipmentId\":10}");
        return event;
    }

    @Test
    @DisplayName("معالجة حدث تغيير حالة الشحنة")
    void handle_statusChanged_shouldNotThrow() {
        DomainEvent event = createEvent(ShipmentEventHandler.SHIPMENT_STATUS_CHANGED);
        assertThatCode(() -> shipmentEventHandler.handle(event)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("معالجة حدث تعيين شحنة")
    void handle_assigned_shouldNotThrow() {
        DomainEvent event = createEvent(ShipmentEventHandler.SHIPMENT_ASSIGNED);
        assertThatCode(() -> shipmentEventHandler.handle(event)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("معالجة حدث تسليم شحنة")
    void handle_delivered_shouldNotThrow() {
        DomainEvent event = createEvent(ShipmentEventHandler.SHIPMENT_DELIVERED);
        assertThatCode(() -> shipmentEventHandler.handle(event)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("معالجة حدث غير معروف — لا خطأ")
    void handle_unknownType_shouldNotThrow() {
        DomainEvent event = createEvent("UNKNOWN_EVENT");
        assertThatCode(() -> shipmentEventHandler.handle(event)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("الثوابت معرفة بشكل صحيح")
    void constants_shouldBeCorrect() {
        assertThat(ShipmentEventHandler.SHIPMENT_STATUS_CHANGED).isEqualTo("SHIPMENT_STATUS_CHANGED");
        assertThat(ShipmentEventHandler.SHIPMENT_ASSIGNED).isEqualTo("SHIPMENT_ASSIGNED");
        assertThat(ShipmentEventHandler.SHIPMENT_DELIVERED).isEqualTo("SHIPMENT_DELIVERED");
    }
}
