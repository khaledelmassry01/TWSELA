package com.twsela.repository;

import com.twsela.domain.StorageBin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StorageBinRepository extends JpaRepository<StorageBin, Long> {
    List<StorageBin> findByWarehouseZoneIdAndActiveTrue(Long warehouseZoneId);
    List<StorageBin> findByWarehouseZoneId(Long warehouseZoneId);
    Optional<StorageBin> findByBinCode(String binCode);
    List<StorageBin> findByWarehouseZoneIdAndOccupiedFalseAndActiveTrue(Long warehouseZoneId);
}
