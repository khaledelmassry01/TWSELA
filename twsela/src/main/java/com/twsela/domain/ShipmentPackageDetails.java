package com.twsela.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "shipment_package_details")
public class ShipmentPackageDetails {

    @Id
    @Column(name = "shipment_id")
    private Long shipmentId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "shipment_id")
    @JsonBackReference
    private Shipment shipment;

    @Column(name = "package_type", length = 50)
    private String packageType;

    @Column(name = "weight_kg", precision = 6, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "length_cm", precision = 6, scale = 2)
    private BigDecimal lengthCm;

    @Column(name = "width_cm", precision = 6, scale = 2)
    private BigDecimal widthCm;

    @Column(name = "height_cm", precision = 6, scale = 2)
    private BigDecimal heightCm;

    // Constructors
    public ShipmentPackageDetails() {}

    public ShipmentPackageDetails(Shipment shipment, String packageType, BigDecimal weightKg, 
                                 BigDecimal lengthCm, BigDecimal widthCm, BigDecimal heightCm) {
        this.shipment = shipment;
        this.packageType = packageType;
        this.weightKg = weightKg;
        this.lengthCm = lengthCm;
        this.widthCm = widthCm;
        this.heightCm = heightCm;
    }

    // Getters and Setters
    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }
    public Shipment getShipment() { return shipment; }
    public void setShipment(Shipment shipment) { this.shipment = shipment; }
    public String getPackageType() { return packageType; }
    public void setPackageType(String packageType) { this.packageType = packageType; }
    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
    public BigDecimal getLengthCm() { return lengthCm; }
    public void setLengthCm(BigDecimal lengthCm) { this.lengthCm = lengthCm; }
    public BigDecimal getWidthCm() { return widthCm; }
    public void setWidthCm(BigDecimal widthCm) { this.widthCm = widthCm; }
    public BigDecimal getHeightCm() { return heightCm; }
    public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }

    @Override
    public String toString() {
        return "ShipmentPackageDetails{" +
                "shipmentId=" + shipmentId +
                ", packageType='" + packageType + '\'' +
                ", weightKg=" + weightKg +
                ", lengthCm=" + lengthCm +
                ", widthCm=" + widthCm +
                ", heightCm=" + heightCm +
                '}';
    }
}

