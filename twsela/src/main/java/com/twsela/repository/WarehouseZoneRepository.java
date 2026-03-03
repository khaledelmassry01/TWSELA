package com.twsela.repository;

import com.twsela.domain.WarehouseZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseZoneRepository extends JpaRepository<WarehouseZone, Long> {
    List<WarehouseZone> findByWarehouseIdAndActiveTrue(Long warehouseId);
    List<WarehouseZone> findByWarehouseId(Long warehouseId);
    Optional<WarehouseZone> findByWarehouseIdAndCode(Long warehouseId, String code);
    boolean existsByWarehouseIdAndCode(Long warehouseId, String code);
}
