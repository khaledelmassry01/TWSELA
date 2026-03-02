package com.twsela.repository;

import com.twsela.domain.Vehicle;
import com.twsela.domain.Vehicle.VehicleStatus;
import com.twsela.domain.Vehicle.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByPlateNumber(String plateNumber);

    List<Vehicle> findByStatus(VehicleStatus status);

    List<Vehicle> findByVehicleType(VehicleType vehicleType);

    List<Vehicle> findByStatusAndVehicleType(VehicleStatus status, VehicleType vehicleType);

    boolean existsByPlateNumber(String plateNumber);
}
