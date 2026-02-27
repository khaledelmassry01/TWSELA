package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "warehouses", indexes = {
    @Index(name = "idx_warehouse_name", columnList = "name")
})
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Warehouse name is required")
    @Size(max = 100, message = "Warehouse name must not exceed 100 characters")
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Warehouse address is required")
    private String address;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 8)
    private BigDecimal longitude;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WarehouseInventory> inventory = new HashSet<>();

    // Constructors
    public Warehouse() {}

    public Warehouse(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public Warehouse(String name, String address, BigDecimal latitude, BigDecimal longitude) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Set<WarehouseInventory> getInventory() {
        return inventory;
    }

    public void setInventory(Set<WarehouseInventory> inventory) {
        this.inventory = inventory;
    }

    // Helper methods
    public void addInventory(WarehouseInventory inventoryItem) {
        inventory.add(inventoryItem);
        inventoryItem.setWarehouse(this);
    }

    public void removeInventory(WarehouseInventory inventoryItem) {
        inventory.remove(inventoryItem);
        inventoryItem.setWarehouse(null);
    }

    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }

    @Override
    public String toString() {
        return "Warehouse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Warehouse)) return false;
        Warehouse warehouse = (Warehouse) o;
        return id != null && id.equals(warehouse.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
