package com.twsela.repository;

import com.twsela.domain.CourierZone;
import com.twsela.domain.CourierZone.Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourierZoneRepository extends JpaRepository<CourierZone, Id> {
    @Query("SELECT cz FROM CourierZone cz WHERE cz.id.courierId = :courierId")
    List<CourierZone> findByCourierId(@Param("courierId") Long courierId);
    
    @Query("SELECT cz FROM CourierZone cz WHERE cz.id.zoneId = :zoneId")
    List<CourierZone> findByZoneId(@Param("zoneId") Long zoneId);
    
    @Query("SELECT cz FROM CourierZone cz WHERE cz.id.courierId = :courierId AND cz.id.zoneId = :zoneId")
    List<CourierZone> findByCourierIdAndZoneId(@Param("courierId") Long courierId, @Param("zoneId") Long zoneId);
}