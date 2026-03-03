package com.twsela.repository;

import com.twsela.domain.CarrierZoneMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CarrierZoneMappingRepository extends JpaRepository<CarrierZoneMapping, Long> {
    List<CarrierZoneMapping> findByCarrierId(Long carrierId);
    List<CarrierZoneMapping> findByZoneId(Long zoneId);
}
