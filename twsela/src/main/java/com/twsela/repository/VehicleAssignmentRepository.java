package com.twsela.repository;

import com.twsela.domain.VehicleAssignment;
import com.twsela.domain.VehicleAssignment.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleAssignmentRepository extends JpaRepository<VehicleAssignment, Long> {

    List<VehicleAssignment> findByVehicleIdOrderByCreatedAtDesc(Long vehicleId);

    List<VehicleAssignment> findByCourierIdOrderByCreatedAtDesc(Long courierId);

    Optional<VehicleAssignment> findByCourierIdAndStatus(Long courierId, AssignmentStatus status);

    Optional<VehicleAssignment> findByVehicleIdAndStatus(Long vehicleId, AssignmentStatus status);

    boolean existsByVehicleIdAndStatus(Long vehicleId, AssignmentStatus status);

    boolean existsByCourierIdAndStatus(Long courierId, AssignmentStatus status);
}
