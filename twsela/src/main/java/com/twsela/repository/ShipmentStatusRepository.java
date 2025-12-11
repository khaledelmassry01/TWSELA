package com.twsela.repository;

import com.twsela.domain.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipmentStatusRepository extends JpaRepository<ShipmentStatus, Long> {
    
    Optional<ShipmentStatus> findByName(String name);
    
    boolean existsByName(String name);
}
