package com.twsela.repository;

import com.twsela.domain.CarrierRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CarrierRateRepository extends JpaRepository<CarrierRate, Long> {
    List<CarrierRate> findByCarrierId(Long carrierId);
    List<CarrierRate> findByCarrierZoneMappingId(Long mappingId);
}
