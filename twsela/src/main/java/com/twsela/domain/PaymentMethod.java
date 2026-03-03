package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * وسيلة دفع مسجلة للمستخدم (بطاقة / محفظة إلكترونية / حساب بنكي / فوري).
 */
@Entity
@Table(name = "payment_methods", indexes = {
        @Index(name = "idx_pm_user", columnList = "user_id"),
        @Index(name = "idx_pm_provider", columnList = "provider"),
        @Index(name = "idx_pm_tokenized", columnList = "tokenized_ref")
})
public class PaymentMethod {

    public enum PaymentType { CARD, WALLET, BANK_ACCOUNT, FAWRY }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PaymentType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private PaymentTransaction.PaymentGatewayType provider;

    @Column(name = "last4", length = 4)
    private String last4;

    @Column(name = "brand", length = 30)
    private String brand;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "tokenized_ref", length = 255)
    private String tokenizedRef;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    // ── Constructors ──
    public PaymentMethod() {}

    // ── Getters / Setters ──
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public PaymentType getType() { return type; }
    public void setType(PaymentType type) { this.type = type; }

    public PaymentTransaction.PaymentGatewayType getProvider() { return provider; }
    public void setProvider(PaymentTransaction.PaymentGatewayType provider) { this.provider = provider; }

    public String getLast4() { return last4; }
    public void setLast4(String last4) { this.last4 = last4; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public String getTokenizedRef() { return tokenizedRef; }
    public void setTokenizedRef(String tokenizedRef) { this.tokenizedRef = tokenizedRef; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // ── equals / hashCode ──
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentMethod that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
