package com.twsela.repository;

import com.twsela.domain.Zone;
import com.twsela.domain.ZoneStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    
    List<Zone> findByStatus(ZoneStatus status);
    
    Optional<Zone> findByNameAndStatus(String name, ZoneStatus status);
    
    Optional<Zone> findByNameIgnoreCase(String name);
    
    @Query("SELECT z FROM Zone z WHERE z.status = 'ZONE_ACTIVE' ORDER BY z.name")
    List<Zone> findAllActiveZonesOrdered();
    
    @Query("SELECT COUNT(z) FROM Zone z WHERE z.status = 'ZONE_ACTIVE'")
    long countActiveZones();
    
    boolean existsByNameAndStatus(String name, ZoneStatus status);
}