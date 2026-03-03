package com.twsela.domain;

import com.twsela.domain.ECommerceConnection.ECommercePlatform;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * Represents an order received from an external e-commerce platform.
 */
@Entity
@Table(name = "ecommerce_orders")
public class ECommerceOrder {

    public enum OrderStatus {
        RECEIVED, SHIPMENT_CREATED, FULFILLED, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connection_id", nullable = false)
    private ECommerceConnection connection;

    @Column(name = "external_order_id", nullable = false, length = 100)
    private String externalOrderId;

    @Column(name = "external_order_number", length = 100)
    private String externalOrderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private ECommercePlatform platform;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status = OrderStatus.RECEIVED;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt = Instant.now();

    @Column(name = "processed_at")
    private Instant processedAt;

    // ── Getters / Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ECommerceConnection getConnection() { return connection; }
    public void setConnection(ECommerceConnection connection) { this.connection = connection; }

    public String getExternalOrderId() { return externalOrderId; }
    public void setExternalOrderId(String externalOrderId) { this.externalOrderId = externalOrderId; }

    public String getExternalOrderNumber() { return externalOrderNumber; }
    public void setExternalOrderNumber(String externalOrderNumber) { this.externalOrderNumber = externalOrderNumber; }

    public Shipment getShipment() { return shipment; }
    public void setShipment(Shipment shipment) { this.shipment = shipment; }

    public ECommercePlatform getPlatform() { return platform; }
    public void setPlatform(ECommercePlatform platform) { this.platform = platform; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public String getRawPayload() { return rawPayload; }
    public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }

    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
}
