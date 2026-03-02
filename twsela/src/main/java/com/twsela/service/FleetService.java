package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.domain.Vehicle.VehicleStatus;
import com.twsela.domain.Vehicle.VehicleType;
import com.twsela.domain.VehicleAssignment.AssignmentStatus;
import com.twsela.domain.VehicleMaintenance.MaintenanceStatus;
import com.twsela.repository.*;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.DuplicateResourceException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for fleet management operations: vehicles, assignments, maintenance, and fuel.
 */
@Service
@Transactional
public class FleetService {

    private static final Logger log = LoggerFactory.getLogger(FleetService.class);

    private final VehicleRepository vehicleRepository;
    private final VehicleAssignmentRepository assignmentRepository;
    private final VehicleMaintenanceRepository maintenanceRepository;
    private final FuelLogRepository fuelLogRepository;
    private final UserRepository userRepository;

    public FleetService(VehicleRepository vehicleRepository,
                        VehicleAssignmentRepository assignmentRepository,
                        VehicleMaintenanceRepository maintenanceRepository,
                        FuelLogRepository fuelLogRepository,
                        UserRepository userRepository) {
        this.vehicleRepository = vehicleRepository;
        this.assignmentRepository = assignmentRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.fuelLogRepository = fuelLogRepository;
        this.userRepository = userRepository;
    }

    // ══════════════════════════════════════════════════════════
    // Vehicle CRUD
    // ══════════════════════════════════════════════════════════

    public Vehicle createVehicle(Vehicle vehicle) {
        if (vehicleRepository.existsByPlateNumber(vehicle.getPlateNumber())) {
            throw new DuplicateResourceException("المركبة برقم اللوحة موجودة بالفعل: " + vehicle.getPlateNumber());
        }
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        log.info("Vehicle created: {}", vehicle.getPlateNumber());
        return vehicleRepository.save(vehicle);
    }

    @Transactional(readOnly = true)
    public Vehicle getVehicle(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));
    }

    @Transactional(readOnly = true)
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Vehicle> getVehiclesByStatus(VehicleStatus status) {
        return vehicleRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> getAvailableVehicles(VehicleType type) {
        if (type != null) {
            return vehicleRepository.findByStatusAndVehicleType(VehicleStatus.AVAILABLE, type);
        }
        return vehicleRepository.findByStatus(VehicleStatus.AVAILABLE);
    }

    public Vehicle updateVehicle(Long id, Vehicle updates) {
        Vehicle vehicle = getVehicle(id);
        if (updates.getMake() != null) vehicle.setMake(updates.getMake());
        if (updates.getModel() != null) vehicle.setModel(updates.getModel());
        if (updates.getModelYear() != null) vehicle.setModelYear(updates.getModelYear());
        if (updates.getColor() != null) vehicle.setColor(updates.getColor());
        if (updates.getCurrentMileage() != null) vehicle.setCurrentMileage(updates.getCurrentMileage());
        if (updates.getInsuranceExpiry() != null) vehicle.setInsuranceExpiry(updates.getInsuranceExpiry());
        if (updates.getLicenseExpiry() != null) vehicle.setLicenseExpiry(updates.getLicenseExpiry());
        vehicle.setUpdatedAt(Instant.now());
        return vehicleRepository.save(vehicle);
    }

    public Vehicle retireVehicle(Long id) {
        Vehicle vehicle = getVehicle(id);
        if (vehicle.getStatus() == VehicleStatus.IN_USE) {
            throw new BusinessRuleException("لا يمكن إيقاف مركبة مستخدمة حالياً");
        }
        vehicle.setStatus(VehicleStatus.RETIRED);
        vehicle.setUpdatedAt(Instant.now());
        log.info("Vehicle {} retired", vehicle.getPlateNumber());
        return vehicleRepository.save(vehicle);
    }

    // ══════════════════════════════════════════════════════════
    // Vehicle Assignment
    // ══════════════════════════════════════════════════════════

    public VehicleAssignment assignVehicle(Long vehicleId, Long courierId) {
        Vehicle vehicle = getVehicle(vehicleId);
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new BusinessRuleException("المركبة غير متاحة للتعيين");
        }

        boolean courierHasVehicle = assignmentRepository.existsByCourierIdAndStatus(
                courierId, AssignmentStatus.ACTIVE);
        if (courierHasVehicle) {
            throw new BusinessRuleException("المندوب لديه مركبة معينة بالفعل");
        }

        User courier = userRepository.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", courierId));

        VehicleAssignment assignment = new VehicleAssignment();
        assignment.setVehicle(vehicle);
        assignment.setCourier(courier);
        assignment.setStatus(AssignmentStatus.ACTIVE);
        assignment.setAssignedDate(LocalDate.now());
        assignment.setStartMileage(vehicle.getCurrentMileage());

        vehicle.setStatus(VehicleStatus.IN_USE);
        vehicle.setUpdatedAt(Instant.now());
        vehicleRepository.save(vehicle);

        log.info("Vehicle {} assigned to courier {}", vehicleId, courierId);
        return assignmentRepository.save(assignment);
    }

    public VehicleAssignment returnVehicle(Long assignmentId, Integer endMileage) {
        VehicleAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("VehicleAssignment", "id", assignmentId));

        if (assignment.getStatus() != AssignmentStatus.ACTIVE) {
            throw new BusinessRuleException("التعيين غير نشط");
        }

        assignment.setStatus(AssignmentStatus.COMPLETED);
        assignment.setReturnedDate(LocalDate.now());
        assignment.setEndMileage(endMileage);

        Vehicle vehicle = assignment.getVehicle();
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle.setCurrentMileage(endMileage);
        vehicle.setUpdatedAt(Instant.now());
        vehicleRepository.save(vehicle);

        log.info("Vehicle {} returned by courier {}", vehicle.getId(), assignment.getCourier().getId());
        return assignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public List<VehicleAssignment> getAssignmentsByVehicle(Long vehicleId) {
        return assignmentRepository.findByVehicleIdOrderByCreatedAtDesc(vehicleId);
    }

    @Transactional(readOnly = true)
    public List<VehicleAssignment> getAssignmentsByCourier(Long courierId) {
        return assignmentRepository.findByCourierIdOrderByCreatedAtDesc(courierId);
    }

    // ══════════════════════════════════════════════════════════
    // Maintenance
    // ══════════════════════════════════════════════════════════

    public VehicleMaintenance scheduleMaintenance(VehicleMaintenance maintenance) {
        maintenance.setStatus(MaintenanceStatus.SCHEDULED);
        log.info("Maintenance scheduled for vehicle {} on {}",
                maintenance.getVehicle().getId(), maintenance.getScheduledDate());
        return maintenanceRepository.save(maintenance);
    }

    public VehicleMaintenance completeMaintenance(Long maintenanceId, BigDecimal cost) {
        VehicleMaintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new ResourceNotFoundException("VehicleMaintenance", "id", maintenanceId));

        maintenance.setStatus(MaintenanceStatus.COMPLETED);
        maintenance.setCompletedDate(LocalDate.now());
        maintenance.setCost(cost);
        maintenance.setUpdatedAt(Instant.now());

        // Mark vehicle as available if it was in maintenance
        Vehicle vehicle = maintenance.getVehicle();
        if (vehicle.getStatus() == VehicleStatus.MAINTENANCE) {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            vehicle.setUpdatedAt(Instant.now());
            vehicleRepository.save(vehicle);
        }

        log.info("Maintenance {} completed for vehicle {}", maintenanceId, vehicle.getId());
        return maintenanceRepository.save(maintenance);
    }

    @Transactional(readOnly = true)
    public List<VehicleMaintenance> getMaintenanceByVehicle(Long vehicleId) {
        return maintenanceRepository.findByVehicleIdOrderByScheduledDateDesc(vehicleId);
    }

    @Transactional(readOnly = true)
    public List<VehicleMaintenance> getDueMaintenance() {
        return maintenanceRepository.findDueForService(LocalDate.now());
    }

    // ══════════════════════════════════════════════════════════
    // Fuel Logs
    // ══════════════════════════════════════════════════════════

    public FuelLog addFuelLog(FuelLog fuelLog) {
        fuelLog.setTotalCost(fuelLog.getLiters().multiply(fuelLog.getCostPerLiter()));

        if (fuelLog.getMileageAtFill() != null) {
            Vehicle vehicle = fuelLog.getVehicle();
            vehicle.setCurrentMileage(fuelLog.getMileageAtFill());
            vehicle.setUpdatedAt(Instant.now());
            vehicleRepository.save(vehicle);
        }

        log.info("Fuel log added for vehicle {}: {} liters",
                fuelLog.getVehicle().getId(), fuelLog.getLiters());
        return fuelLogRepository.save(fuelLog);
    }

    @Transactional(readOnly = true)
    public List<FuelLog> getFuelLogsByVehicle(Long vehicleId) {
        return fuelLogRepository.findByVehicleIdOrderByFuelDateDesc(vehicleId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getFuelCostForPeriod(Long vehicleId, LocalDate from, LocalDate to) {
        return fuelLogRepository.totalCostByVehicleAndPeriod(vehicleId, from, to);
    }
}
