package com.twsela.repository;

import com.twsela.domain.FuelLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FuelLogRepository extends JpaRepository<FuelLog, Long> {

    List<FuelLog> findByVehicleIdOrderByFuelDateDesc(Long vehicleId);

    List<FuelLog> findByCourierIdOrderByFuelDateDesc(Long courierId);

    @Query("SELECT COALESCE(SUM(f.totalCost), 0) FROM FuelLog f WHERE f.vehicle.id = :vehicleId AND f.fuelDate BETWEEN :from AND :to")
    BigDecimal totalCostByVehicleAndPeriod(@Param("vehicleId") Long vehicleId,
                                            @Param("from") LocalDate from,
                                            @Param("to") LocalDate to);
}
