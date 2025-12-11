package com.twsela.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "courier_details")
public class CourierDetails {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type")
    private VehicleType vehicleType;

    @Column(name = "license_plate_number", length = 50)
    private String licensePlateNumber;

    @Column(name = "onboarding_date")
    private LocalDate onboardingDate;

    public enum VehicleType {
        BICYCLE, CAR, MOTORCYCLE, VAN
    }

    // Constructors
    public CourierDetails() {}

    public CourierDetails(User user, VehicleType vehicleType, String licensePlateNumber) {
        this.user = user;
        this.vehicleType = vehicleType;
        this.licensePlateNumber = licensePlateNumber;
        this.onboardingDate = LocalDate.now();
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
    public String getLicensePlateNumber() { return licensePlateNumber; }
    public void setLicensePlateNumber(String licensePlateNumber) { this.licensePlateNumber = licensePlateNumber; }
    public LocalDate getOnboardingDate() { return onboardingDate; }
    public void setOnboardingDate(LocalDate onboardingDate) { this.onboardingDate = onboardingDate; }

    @Override
    public String toString() {
        return "CourierDetails{" +
                "userId=" + userId +
                ", vehicleType=" + vehicleType +
                ", licensePlateNumber='" + licensePlateNumber + '\'' +
                ", onboardingDate=" + onboardingDate +
                '}';
    }
}

