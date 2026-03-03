package com.twsela.repository;

import com.twsela.domain.PickWave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PickWaveRepository extends JpaRepository<PickWave, Long> {
    List<PickWave> findByWarehouseIdOrderByCreatedAtDesc(Long warehouseId);
    List<PickWave> findByStatusOrderByCreatedAtDesc(String status);
    Optional<PickWave> findByWaveNumber(String waveNumber);
    List<PickWave> findByAssignedPickerIdAndStatus(Long pickerId, String status);
}
