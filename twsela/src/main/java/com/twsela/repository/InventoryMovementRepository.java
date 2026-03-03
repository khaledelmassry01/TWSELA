package com.twsela.repository;

import com.twsela.domain.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    List<InventoryMovement> findByWarehouseIdOrderByCreatedAtDesc(Long warehouseId);
    List<InventoryMovement> findByStorageBinIdOrderByCreatedAtDesc(Long storageBinId);
    List<InventoryMovement> findByProductSkuOrderByCreatedAtDesc(String productSku);
}
