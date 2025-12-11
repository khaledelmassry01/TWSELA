package com.twsela.repository;

import com.twsela.domain.DeliveryPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPricingRepository extends JpaRepository<DeliveryPricing, Long> {
    
    @Query("SELECT dp FROM DeliveryPricing dp WHERE dp.merchant.id = :merchantId AND dp.zone.id = :zoneId")
    Optional<DeliveryPricing> findByMerchantIdAndZoneId(@Param("merchantId") Long merchantId, @Param("zoneId") Long zoneId);
    
    @Query("SELECT dp FROM DeliveryPricing dp WHERE dp.merchant.id = :merchantId")
    List<DeliveryPricing> findByMerchantId(@Param("merchantId") Long merchantId);
    
    @Query("SELECT dp FROM DeliveryPricing dp WHERE dp.zone.id = :zoneId")
    List<DeliveryPricing> findByZoneId(@Param("zoneId") Long zoneId);
    
    List<DeliveryPricing> findByIsActiveTrue();
    
    @Query("SELECT dp FROM DeliveryPricing dp WHERE dp.merchant.id = :merchantId AND dp.isActive = true")
    List<DeliveryPricing> findByMerchantIdAndIsActiveTrue(@Param("merchantId") Long merchantId);
    
    @Query("SELECT dp FROM DeliveryPricing dp WHERE dp.zone.id = :zoneId AND dp.isActive = true")
    List<DeliveryPricing> findByZoneIdAndIsActiveTrue(@Param("zoneId") Long zoneId);
    
    @Query("SELECT CASE WHEN COUNT(dp) > 0 THEN true ELSE false END FROM DeliveryPricing dp WHERE dp.merchant.id = :merchantId AND dp.zone.id = :zoneId")
    boolean existsByMerchantIdAndZoneId(@Param("merchantId") Long merchantId, @Param("zoneId") Long zoneId);
}