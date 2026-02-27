package com.twsela.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.Objects;

@Entity
@Table(name = "recipient_details", indexes = {
    @Index(name = "idx_recipient_phone", columnList = "phone"),
    @Index(name = "idx_recipient_phone_address", columnList = "phone, name")
})
public class RecipientDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    private String phone;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Address is required")
    private String address;

    @Column(name = "alternate_phone", length = 50)
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Alternate phone number must be 10-15 digits")
    private String alternatePhone;

    @OneToMany(mappedBy = "recipientDetails", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private java.util.Set<Shipment> shipments = new java.util.HashSet<>();

    // Constructors
    public RecipientDetails() {}

    public RecipientDetails(String phone, String name, String address) {
        this.phone = phone;
        this.name = name;
        this.address = address;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getAlternatePhone() { return alternatePhone; }
    public void setAlternatePhone(String alternatePhone) { this.alternatePhone = alternatePhone; }
    public java.util.Set<Shipment> getShipments() { return shipments; }
    public void setShipments(java.util.Set<Shipment> shipments) { this.shipments = shipments; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecipientDetails)) return false;
        RecipientDetails that = (RecipientDetails) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "RecipientDetails{" +
                "id=" + id +
                ", phone='" + phone + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}

