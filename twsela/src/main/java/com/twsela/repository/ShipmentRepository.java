package com.twsela.repository;

import com.twsela.domain.Shipment;
import com.twsela.domain.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    
    Page<Shipment> findByStatus(ShipmentStatus status, Pageable pageable);
    List<Shipment> findByStatus(ShipmentStatus status);
    
    // Optimized queries with proper joins and indexes
    @Query("SELECT s FROM Shipment s JOIN FETCH s.manifest m WHERE m.courier.id = :courierId")
    List<Shipment> findByCourierId(@Param("courierId") Long courierId);
    
    @Query("SELECT s FROM Shipment s JOIN FETCH s.manifest m WHERE m.courier.id = :courierId")
    Page<Shipment> findByCourierId(@Param("courierId") Long courierId, Pageable pageable);
    
    // Optimized search query with proper indexing
    @Query("SELECT s FROM Shipment s LEFT JOIN FETCH s.recipientDetails rd WHERE " +
           "s.trackingNumber LIKE CONCAT('%', :trackingNumber, '%') OR " +
           "rd.name LIKE CONCAT('%', :recipientName, '%') OR " +
           "rd.phone LIKE CONCAT('%', :recipientPhone, '%')")
    Page<Shipment> searchShipments(@Param("trackingNumber") String trackingNumber, 
                                   @Param("recipientName") String recipientName, 
                                   @Param("recipientPhone") String recipientPhone, 
                                   Pageable pageable);
    
    // Keep the old method for backward compatibility
    Page<Shipment> findByTrackingNumberContainingIgnoreCaseOrRecipientNameContainingIgnoreCaseOrRecipientPhoneContainingIgnoreCase(
        String trackingNumber, String recipientName, String recipientPhone, Pageable pageable);
    
    @Query("SELECT s FROM Shipment s WHERE s.manifest IS NULL")
    List<Shipment> findByCourierIsNull();
    
    // Optimized queries with JOIN FETCH to avoid N+1 problems
    @Query("SELECT s FROM Shipment s JOIN FETCH s.manifest m WHERE m.courier.id = :courierId AND s.status IN :statuses")
    Page<Shipment> findByCourierIdAndStatusIn(@Param("courierId") Long courierId, @Param("statuses") List<ShipmentStatus> statuses, Pageable pageable);
    
    @Query("SELECT s FROM Shipment s JOIN FETCH s.manifest m WHERE m.courier.id = :courierId AND s.status IN :statuses")
    List<Shipment> findByCourierIdAndStatusIn(@Param("courierId") Long courierId, @Param("statuses") List<ShipmentStatus> statuses);
    
    List<Shipment> findByStatusAndZoneIdIn(ShipmentStatus status, List<Long> zoneIds);
    List<Shipment> findByTrackingNumberIn(List<String> trackingNumbers);
    List<Shipment> findByManifestId(Long manifestId);
    List<Shipment> findByPayoutId(Long payoutId);
    
    Page<Shipment> findByStatusIn(List<ShipmentStatus> statuses, Pageable pageable);
    
    long countByStatusAndUpdatedAtBetween(ShipmentStatus status, Instant start, Instant end);
    long countByStatusIn(List<ShipmentStatus> statuses);
    long countByStatus(ShipmentStatus status);
    long countByCreatedAtBetween(Instant start, Instant end);
    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.manifest.courier.id = :courierId")
    long countByCourierId(@Param("courierId") Long courierId);
    
    long countByMerchantId(Long merchantId);
    
    @Query("SELECT s FROM Shipment s WHERE s.manifest.courier.id = :courierId AND s.status IN :statuses AND s.updatedAt BETWEEN :start AND :end")
    List<Shipment> findByCourierIdAndStatusInAndUpdatedAtBetween(@Param("courierId") Long courierId, @Param("statuses") List<ShipmentStatus> statuses, @Param("start") Instant start, @Param("end") Instant end);
    
    // Merchant queries
    List<Shipment> findByMerchantId(Long merchantId);
    Page<Shipment> findByMerchantId(Long merchantId, Pageable pageable);
    List<Shipment> findByMerchantIdAndStatus(Long merchantId, ShipmentStatus status);
    
    // Financial queries
    @Query("SELECT s FROM Shipment s WHERE s.merchant.id = :merchantId AND s.status.name = :statusName AND s.payout IS NULL")
    List<Shipment> findByMerchantIdAndStatusNameAndPayoutIsNull(@Param("merchantId") Long merchantId, @Param("statusName") String statusName);
    
    List<Shipment> findByMerchantIdAndStatusAndPayoutIsNull(Long merchantId, ShipmentStatus status);
    
    @Query("SELECT s FROM Shipment s WHERE s.manifest.courier.id = :courierId AND s.status.name = :statusName AND s.cashReconciled = false")
    List<Shipment> findByCourierIdAndStatusNameAndCashReconciledFalse(@Param("courierId") Long courierId, @Param("statusName") String statusName);
    
    // Dashboard KPI queries
    List<Shipment> findByCreatedAtBetween(Instant start, Instant end);
    List<Shipment> findByStatusAndUpdatedAtBetween(ShipmentStatus status, Instant start, Instant end);
    
    @Query("SELECT s FROM Shipment s WHERE s.manifest.courier.id = :courierId AND s.updatedAt > :after")
    List<Shipment> findByCourierIdAndUpdatedAtAfter(@Param("courierId") Long courierId, @Param("after") Instant after);
    
    @Query("SELECT s FROM Shipment s WHERE s.recipientDetails.phone = :phone")
    List<Shipment> findByRecipientPhone(@Param("phone") String phone);
    
    @Query("SELECT s FROM Shipment s WHERE s.cashReconciled = false AND s.status.name = 'DELIVERED'")
    List<Shipment> findUnreconciledDeliveredShipments();
    
    @Query("SELECT ss FROM ShipmentStatus ss WHERE ss.name = :name")
    Optional<ShipmentStatus> findByStatusName(@Param("name") String name);
    
    Page<Shipment> findByMerchantIdAndStatus(Long merchantId, ShipmentStatus status, Pageable pageable);
}