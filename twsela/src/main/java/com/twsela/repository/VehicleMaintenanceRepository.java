package com.twsela.repository;

import com.twsela.domain.VehicleMaintenance;
import com.twsela.domain.VehicleMaintenance.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VehicleMaintenanceRepository extends JpaRepository<VehicleMaintenance, Long> {

    List<VehicleMaintenance> findByVehicleIdOrderByScheduledDateDesc(Long vehicleId);

    List<VehicleMaintenance> findByStatus(MaintenanceStatus status);

    @Query("SELECT m FROM VehicleMaintenance m WHERE m.status = 'SCHEDULED' AND m.scheduledDate <= :date")
    List<VehicleMaintenance> findDueForService(@Param("date") LocalDate date);

    @Query("SELECT m FROM VehicleMaintenance m WHERE m.vehicle.id = :vehicleId AND m.status IN ('SCHEDULED', 'IN_PROGRESS')")
    List<VehicleMaintenance> findPendingByVehicle(@Param("vehicleId") Long vehicleId);
}
