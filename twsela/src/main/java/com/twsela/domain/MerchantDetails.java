package com.twsela.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "merchant_details", indexes = {
    @Index(name = "idx_merchant_business_name", columnList = "business_name")
})
public class MerchantDetails {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @Column(name = "business_name", nullable = false, length = 255)
    private String businessName;

    @Column(name = "pickup_address", nullable = false, columnDefinition = "TEXT")
    private String pickupAddress;


    @Column(name = "bank_account_details", columnDefinition = "TEXT")
    @JsonIgnore
    private String bankAccountDetails;

    // Constructors
    public MerchantDetails() {}

    public MerchantDetails(User user, String businessName, String pickupAddress) {
        this.user = user;
        this.businessName = businessName;
        this.pickupAddress = pickupAddress;
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    public String getBankAccountDetails() { return bankAccountDetails; }
    public void setBankAccountDetails(String bankAccountDetails) { this.bankAccountDetails = bankAccountDetails; }

    @Override
    public String toString() {
        return "MerchantDetails{" +
                "userId=" + userId +
                ", businessName='" + businessName + '\'' +
                ", pickupAddress='" + pickupAddress + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MerchantDetails)) return false;
        MerchantDetails that = (MerchantDetails) o;
        return userId != null && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}


