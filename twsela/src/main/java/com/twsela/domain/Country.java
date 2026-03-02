package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a country supported by the system.
 */
@Entity
@Table(name = "countries", indexes = {
    @Index(name = "idx_country_active", columnList = "is_active")
})
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 2)
    private String code; // ISO 3166-1 alpha-2

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "name_ar", nullable = false, length = 100)
    private String nameAr;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode; // ISO 4217

    @Column(name = "phone_prefix", length = 5)
    private String phonePrefix;

    @Column(name = "address_format", length = 255)
    private String addressFormat; // template: {street}, {city}, {state}, {zip}

    @Column(name = "time_zone", length = 50)
    private String timeZone;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "default_payment_gateway", length = 30)
    private String defaultPaymentGateway;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Country() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public String getNameAr() { return nameAr; }
    public void setNameAr(String nameAr) { this.nameAr = nameAr; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public String getPhonePrefix() { return phonePrefix; }
    public void setPhonePrefix(String phonePrefix) { this.phonePrefix = phonePrefix; }
    public String getAddressFormat() { return addressFormat; }
    public void setAddressFormat(String addressFormat) { this.addressFormat = addressFormat; }
    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getDefaultPaymentGateway() { return defaultPaymentGateway; }
    public void setDefaultPaymentGateway(String defaultPaymentGateway) { this.defaultPaymentGateway = defaultPaymentGateway; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Country that = (Country) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
