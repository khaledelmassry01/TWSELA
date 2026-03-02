package com.twsela.web.dto;

import com.twsela.domain.Vehicle.VehicleStatus;
import com.twsela.domain.Vehicle.VehicleType;
import com.twsela.domain.VehicleMaintenance.MaintenanceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * DTOs for the fleet management module.
 */
public class FleetDTO {

    public record CreateVehicleRequest(
            @NotBlank(message = "رقم اللوحة مطلوب") String plateNumber,
            @NotNull(message = "نوع المركبة مطلوب") VehicleType vehicleType,
            String make, String model, Integer modelYear, String color,
            LocalDate insuranceExpiry, LocalDate licenseExpiry
    ) {}

    public record VehicleResponse(
            Long id, String plateNumber, VehicleType vehicleType,
            String make, String model, Integer modelYear, String color,
            VehicleStatus status, Integer currentMileage,
            LocalDate insuranceExpiry, LocalDate licenseExpiry, Instant createdAt
    ) {}

    public record AssignVehicleRequest(
            @NotNull(message = "معرف المركبة مطلوب") Long vehicleId,
            @NotNull(message = "معرف المندوب مطلوب") Long courierId
    ) {}

    public record ReturnVehicleRequest(
            @NotNull(message = "عداد المسافة عند الإرجاع مطلوب") Integer endMileage
    ) {}

    public record AssignmentResponse(
            Long id, Long vehicleId, String plateNumber,
            Long courierId, String courierName,
            String status, LocalDate assignedDate, LocalDate returnedDate,
            Integer startMileage, Integer endMileage, String notes
    ) {}

    public record ScheduleMaintenanceRequest(
            @NotNull(message = "معرف المركبة مطلوب") Long vehicleId,
            @NotNull(message = "نوع الصيانة مطلوب") MaintenanceType maintenanceType,
            @NotNull(message = "تاريخ الصيانة مطلوب") LocalDate scheduledDate,
            String description, String serviceProvider
    ) {}

    public record MaintenanceResponse(
            Long id, Long vehicleId, String plateNumber,
            MaintenanceType maintenanceType, String status,
            String description, LocalDate scheduledDate, LocalDate completedDate,
            BigDecimal cost, String serviceProvider
    ) {}

    public record AddFuelLogRequest(
            @NotNull(message = "معرف المركبة مطلوب") Long vehicleId,
            @NotNull(message = "تاريخ التعبئة مطلوب") LocalDate fuelDate,
            @NotNull(message = "عدد اللترات مطلوب") BigDecimal liters,
            @NotNull(message = "سعر اللتر مطلوب") BigDecimal costPerLiter,
            Integer mileageAtFill, String fuelStation
    ) {}

    public record FuelLogResponse(
            Long id, Long vehicleId, String plateNumber,
            LocalDate fuelDate, BigDecimal liters,
            BigDecimal costPerLiter, BigDecimal totalCost,
            Integer mileageAtFill, String fuelStation
    ) {}

    private FleetDTO() {}
}
