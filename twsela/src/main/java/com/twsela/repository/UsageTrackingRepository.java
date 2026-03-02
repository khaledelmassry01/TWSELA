package com.twsela.repository;

import com.twsela.domain.UsageTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface UsageTrackingRepository extends JpaRepository<UsageTracking, Long> {

    Optional<UsageTracking> findByMerchantIdAndPeriod(Long merchantId, String period);

    @Modifying
    @Query("UPDATE UsageTracking u SET u.shipmentsCreated = u.shipmentsCreated + 1, u.lastUpdated = :now WHERE u.merchantId = :merchantId AND u.period = :period")
    int incrementShipments(@Param("merchantId") Long merchantId, @Param("period") String period, @Param("now") Instant now);

    @Modifying
    @Query("UPDATE UsageTracking u SET u.apiCalls = u.apiCalls + 1, u.lastUpdated = :now WHERE u.merchantId = :merchantId AND u.period = :period")
    int incrementApiCalls(@Param("merchantId") Long merchantId, @Param("period") String period, @Param("now") Instant now);
}
