package com.twsela.web;

import com.twsela.domain.*;
import com.twsela.domain.Vehicle.VehicleStatus;
import com.twsela.domain.Vehicle.VehicleType;
import com.twsela.service.FleetService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.FleetDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for fleet management operations.
 */
@RestController
@RequestMapping("/api/fleet")
@Tag(name = "Fleet", description = "إدارة أسطول المركبات")
public class FleetController {

    private final FleetService fleetService;

    public FleetController(FleetService fleetService) {
        this.fleetService = fleetService;
    }

    // ── Vehicles ────────────────────────────────────────────

    @Operation(summary = "إضافة مركبة", description = "إضافة مركبة جديدة للأسطول")
    @PostMapping("/vehicles")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(
            @Valid @RequestBody CreateVehicleRequest request) {
        Vehicle vehicle = new Vehicle();
        vehicle.setPlateNumber(request.plateNumber());
        vehicle.setVehicleType(request.vehicleType());
        vehicle.setMake(request.make());
        vehicle.setModel(request.model());
        vehicle.setModelYear(request.modelYear());
        vehicle.setColor(request.color());
        vehicle.setInsuranceExpiry(request.insuranceExpiry());
        vehicle.setLicenseExpiry(request.licenseExpiry());

        Vehicle saved = fleetService.createVehicle(vehicle);
        return ResponseEntity.ok(ApiResponse.ok(toVehicleResponse(saved), "تمت إضافة المركبة بنجاح"));
    }

    @Operation(summary = "جميع المركبات")
    @GetMapping("/vehicles")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAllVehicles(
            @RequestParam(required = false) VehicleStatus status) {
        List<Vehicle> vehicles = status != null
                ? fleetService.getVehiclesByStatus(status)
                : fleetService.getAllVehicles();
        List<VehicleResponse> response = vehicles.stream().map(this::toVehicleResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(response, "تم جلب المركبات بنجاح"));
    }

    @Operation(summary = "تفاصيل مركبة")
    @GetMapping("/vehicles/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(toVehicleResponse(fleetService.getVehicle(id)), "تم جلب المركبة"));
    }

    @Operation(summary = "المركبات المتاحة")
    @GetMapping("/vehicles/available")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAvailableVehicles(
            @RequestParam(required = false) VehicleType type) {
        List<Vehicle> vehicles = fleetService.getAvailableVehicles(type);
        return ResponseEntity.ok(ApiResponse.ok(vehicles.stream().map(this::toVehicleResponse).toList(), "المركبات المتاحة"));
    }

    @Operation(summary = "إيقاف مركبة")
    @PutMapping("/vehicles/{id}/retire")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<VehicleResponse>> retireVehicle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(toVehicleResponse(fleetService.retireVehicle(id)), "تم إيقاف المركبة"));
    }

    // ── Assignments ────────────────────────────────────────

    @Operation(summary = "تعيين مركبة لمندوب")
    @PostMapping("/assignments")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> assignVehicle(
            @Valid @RequestBody AssignVehicleRequest request) {
        VehicleAssignment assignment = fleetService.assignVehicle(request.vehicleId(), request.courierId());
        return ResponseEntity.ok(ApiResponse.ok(toAssignmentResponse(assignment), "تم تعيين المركبة بنجاح"));
    }

    @Operation(summary = "إرجاع مركبة")
    @PutMapping("/assignments/{id}/return")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'COURIER')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> returnVehicle(
            @PathVariable Long id,
            @Valid @RequestBody ReturnVehicleRequest request) {
        VehicleAssignment assignment = fleetService.returnVehicle(id, request.endMileage());
        return ResponseEntity.ok(ApiResponse.ok(toAssignmentResponse(assignment), "تم إرجاع المركبة بنجاح"));
    }

    // ── Maintenance ────────────────────────────────────────

    @Operation(summary = "جدولة صيانة")
    @PostMapping("/maintenance")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> scheduleMaintenance(
            @Valid @RequestBody ScheduleMaintenanceRequest request) {
        Vehicle vehicle = fleetService.getVehicle(request.vehicleId());
        VehicleMaintenance m = new VehicleMaintenance();
        m.setVehicle(vehicle);
        m.setMaintenanceType(request.maintenanceType());
        m.setScheduledDate(request.scheduledDate());
        m.setDescription(request.description());
        m.setServiceProvider(request.serviceProvider());

        VehicleMaintenance saved = fleetService.scheduleMaintenance(m);
        return ResponseEntity.ok(ApiResponse.ok(toMaintenanceResponse(saved), "تمت جدولة الصيانة"));
    }

    @Operation(summary = "إتمام صيانة")
    @PutMapping("/maintenance/{id}/complete")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> completeMaintenance(
            @PathVariable Long id, @RequestParam BigDecimal cost) {
        VehicleMaintenance m = fleetService.completeMaintenance(id, cost);
        return ResponseEntity.ok(ApiResponse.ok(toMaintenanceResponse(m), "تمت الصيانة بنجاح"));
    }

    @Operation(summary = "سجل صيانة المركبة")
    @GetMapping("/vehicles/{vehicleId}/maintenance")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<MaintenanceResponse>>> getVehicleMaintenance(@PathVariable Long vehicleId) {
        List<VehicleMaintenance> list = fleetService.getMaintenanceByVehicle(vehicleId);
        return ResponseEntity.ok(ApiResponse.ok(list.stream().map(this::toMaintenanceResponse).toList(), "سجل الصيانة"));
    }

    // ── Fuel ───────────────────────────────────────────────

    @Operation(summary = "تسجيل تعبئة وقود")
    @PostMapping("/fuel")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'COURIER')")
    public ResponseEntity<ApiResponse<FuelLogResponse>> addFuelLog(
            @Valid @RequestBody AddFuelLogRequest request) {
        Vehicle vehicle = fleetService.getVehicle(request.vehicleId());
        FuelLog fuelLog = new FuelLog();
        fuelLog.setVehicle(vehicle);
        fuelLog.setFuelDate(request.fuelDate());
        fuelLog.setLiters(request.liters());
        fuelLog.setCostPerLiter(request.costPerLiter());
        fuelLog.setMileageAtFill(request.mileageAtFill());
        fuelLog.setFuelStation(request.fuelStation());

        FuelLog saved = fleetService.addFuelLog(fuelLog);
        return ResponseEntity.ok(ApiResponse.ok(toFuelLogResponse(saved), "تم تسجيل التعبئة"));
    }

    @Operation(summary = "سجل وقود المركبة")
    @GetMapping("/vehicles/{vehicleId}/fuel")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<FuelLogResponse>>> getVehicleFuel(@PathVariable Long vehicleId) {
        List<FuelLog> logs = fleetService.getFuelLogsByVehicle(vehicleId);
        return ResponseEntity.ok(ApiResponse.ok(logs.stream().map(this::toFuelLogResponse).toList(), "سجل الوقود"));
    }

    // ── Mappers ────────────────────────────────────────────

    private VehicleResponse toVehicleResponse(Vehicle v) {
        return new VehicleResponse(v.getId(), v.getPlateNumber(), v.getVehicleType(),
                v.getMake(), v.getModel(), v.getModelYear(), v.getColor(),
                v.getStatus(), v.getCurrentMileage(),
                v.getInsuranceExpiry(), v.getLicenseExpiry(), v.getCreatedAt());
    }

    private AssignmentResponse toAssignmentResponse(VehicleAssignment a) {
        return new AssignmentResponse(a.getId(), a.getVehicle().getId(), a.getVehicle().getPlateNumber(),
                a.getCourier().getId(), a.getCourier().getName(),
                a.getStatus().name(), a.getAssignedDate(), a.getReturnedDate(),
                a.getStartMileage(), a.getEndMileage(), a.getNotes());
    }

    private MaintenanceResponse toMaintenanceResponse(VehicleMaintenance m) {
        return new MaintenanceResponse(m.getId(), m.getVehicle().getId(), m.getVehicle().getPlateNumber(),
                m.getMaintenanceType(), m.getStatus().name(),
                m.getDescription(), m.getScheduledDate(), m.getCompletedDate(),
                m.getCost(), m.getServiceProvider());
    }

    private FuelLogResponse toFuelLogResponse(FuelLog f) {
        return new FuelLogResponse(f.getId(), f.getVehicle().getId(), f.getVehicle().getPlateNumber(),
                f.getFuelDate(), f.getLiters(), f.getCostPerLiter(), f.getTotalCost(),
                f.getMileageAtFill(), f.getFuelStation());
    }
}
