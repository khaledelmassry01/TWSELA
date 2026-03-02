package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.domain.Vehicle.VehicleStatus;
import com.twsela.domain.Vehicle.VehicleType;
import com.twsela.domain.VehicleAssignment.AssignmentStatus;
import com.twsela.domain.VehicleMaintenance.MaintenanceStatus;
import com.twsela.domain.VehicleMaintenance.MaintenanceType;
import com.twsela.repository.*;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.DuplicateResourceException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FleetServiceTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private VehicleAssignmentRepository assignmentRepository;
    @Mock private VehicleMaintenanceRepository maintenanceRepository;
    @Mock private FuelLogRepository fuelLogRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private FleetService fleetService;

    private Vehicle vehicle;
    private User courier;

    @BeforeEach
    void setUp() {
        vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setPlateNumber("ABC-123");
        vehicle.setVehicleType(VehicleType.MOTORCYCLE);
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle.setCurrentMileage(10000);

        courier = new User();
        courier.setId(10L);
        courier.setName("Test Courier");
    }

    // ── Vehicle Tests ───────────────────────────────────────

    @Test
    @DisplayName("createVehicle - إضافة مركبة جديدة")
    void createVehicle_shouldCreate() {
        when(vehicleRepository.existsByPlateNumber("ABC-123")).thenReturn(false);
        when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Vehicle result = fleetService.createVehicle(vehicle);

        assertThat(result.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    @DisplayName("createVehicle - رفض لوحة مكررة")
    void createVehicle_shouldRejectDuplicate() {
        when(vehicleRepository.existsByPlateNumber("ABC-123")).thenReturn(true);

        assertThatThrownBy(() -> fleetService.createVehicle(vehicle))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("retireVehicle - إيقاف مركبة")
    void retireVehicle_shouldRetire() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Vehicle result = fleetService.retireVehicle(1L);

        assertThat(result.getStatus()).isEqualTo(VehicleStatus.RETIRED);
    }

    @Test
    @DisplayName("retireVehicle - رفض إيقاف مركبة مستخدمة")
    void retireVehicle_shouldRejectInUse() {
        vehicle.setStatus(VehicleStatus.IN_USE);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() -> fleetService.retireVehicle(1L))
                .isInstanceOf(BusinessRuleException.class);
    }

    // ── Assignment Tests ────────────────────────────────────

    @Test
    @DisplayName("assignVehicle - تعيين مركبة لمندوب")
    void assignVehicle_shouldAssign() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(assignmentRepository.existsByCourierIdAndStatus(10L, AssignmentStatus.ACTIVE)).thenReturn(false);
        when(userRepository.findById(10L)).thenReturn(Optional.of(courier));
        when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(assignmentRepository.save(any())).thenAnswer(inv -> {
            VehicleAssignment a = inv.getArgument(0);
            a.setId(100L);
            return a;
        });

        VehicleAssignment result = fleetService.assignVehicle(1L, 10L);

        assertThat(result.getStatus()).isEqualTo(AssignmentStatus.ACTIVE);
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.IN_USE);
    }

    @Test
    @DisplayName("assignVehicle - رفض مركبة غير متاحة")
    void assignVehicle_shouldRejectUnavailable() {
        vehicle.setStatus(VehicleStatus.IN_USE);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() -> fleetService.assignVehicle(1L, 10L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("assignVehicle - رفض مندوب لديه مركبة")
    void assignVehicle_shouldRejectCourierWithVehicle() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(assignmentRepository.existsByCourierIdAndStatus(10L, AssignmentStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> fleetService.assignVehicle(1L, 10L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("returnVehicle - إرجاع مركبة")
    void returnVehicle_shouldReturn() {
        VehicleAssignment assignment = new VehicleAssignment();
        assignment.setId(100L);
        assignment.setVehicle(vehicle);
        assignment.setCourier(courier);
        assignment.setStatus(AssignmentStatus.ACTIVE);

        when(assignmentRepository.findById(100L)).thenReturn(Optional.of(assignment));
        when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(assignmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VehicleAssignment result = fleetService.returnVehicle(100L, 12000);

        assertThat(result.getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
        assertThat(result.getEndMileage()).isEqualTo(12000);
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    // ── Maintenance Tests ───────────────────────────────────

    @Test
    @DisplayName("scheduleMaintenance - جدولة صيانة")
    void scheduleMaintenance_shouldSchedule() {
        VehicleMaintenance m = new VehicleMaintenance();
        m.setVehicle(vehicle);
        m.setMaintenanceType(MaintenanceType.OIL_CHANGE);
        m.setScheduledDate(LocalDate.now().plusDays(7));
        when(maintenanceRepository.save(any())).thenAnswer(inv -> {
            VehicleMaintenance saved = inv.getArgument(0);
            saved.setId(50L);
            return saved;
        });

        VehicleMaintenance result = fleetService.scheduleMaintenance(m);

        assertThat(result.getStatus()).isEqualTo(MaintenanceStatus.SCHEDULED);
    }

    @Test
    @DisplayName("completeMaintenance - إتمام صيانة")
    void completeMaintenance_shouldComplete() {
        vehicle.setStatus(VehicleStatus.MAINTENANCE);
        VehicleMaintenance m = new VehicleMaintenance();
        m.setId(50L);
        m.setVehicle(vehicle);
        m.setStatus(MaintenanceStatus.IN_PROGRESS);

        when(maintenanceRepository.findById(50L)).thenReturn(Optional.of(m));
        when(maintenanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VehicleMaintenance result = fleetService.completeMaintenance(50L, new BigDecimal("500.00"));

        assertThat(result.getStatus()).isEqualTo(MaintenanceStatus.COMPLETED);
        assertThat(result.getCost()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    // ── Fuel Tests ──────────────────────────────────────────

    @Test
    @DisplayName("addFuelLog - تسجيل تعبئة وقود")
    void addFuelLog_shouldAdd() {
        FuelLog fuelLog = new FuelLog();
        fuelLog.setVehicle(vehicle);
        fuelLog.setLiters(new BigDecimal("20.00"));
        fuelLog.setCostPerLiter(new BigDecimal("12.50"));
        fuelLog.setMileageAtFill(11000);
        fuelLog.setFuelDate(LocalDate.now());

        when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(fuelLogRepository.save(any())).thenAnswer(inv -> {
            FuelLog saved = inv.getArgument(0);
            saved.setId(30L);
            return saved;
        });

        FuelLog result = fleetService.addFuelLog(fuelLog);

        assertThat(result.getTotalCost()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(vehicle.getCurrentMileage()).isEqualTo(11000);
    }
}
