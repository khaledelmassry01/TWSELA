package com.twsela.repository;

import com.twsela.domain.ReturnShipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnShipmentRepository extends JpaRepository<ReturnShipment, Long> {
    
    List<ReturnShipment> findByOriginalShipmentIdOrderByCreatedAtDesc(Long originalShipmentId);
    
    List<ReturnShipment> findByReturnShipmentIdOrderByCreatedAtDesc(Long returnShipmentId);
    
    @Query("SELECT r FROM ReturnShipment r WHERE r.originalShipment.id = :originalId AND r.returnShipment.id = :returnId")
    Optional<ReturnShipment> findByOriginalShipmentIdAndReturnShipmentId(@Param("originalId") Long originalId, @Param("returnId") Long returnId);
    
    List<ReturnShipment> findByReasonContainingIgnoreCaseOrderByCreatedAtDesc(String reason);

    List<ReturnShipment> findByStatusOrderByCreatedAtDesc(ReturnShipment.ReturnStatusEnum status);

    @Query("SELECT r FROM ReturnShipment r WHERE r.originalShipment.merchant.id = :merchantId ORDER BY r.createdAt DESC")
    List<ReturnShipment> findByMerchantId(@Param("merchantId") Long merchantId);

    @Query("SELECT r FROM ReturnShipment r WHERE r.assignedCourier.id = :courierId ORDER BY r.createdAt DESC")
    List<ReturnShipment> findByAssignedCourierId(@Param("courierId") Long courierId);

    boolean existsByOriginalShipmentIdAndStatusNot(Long originalShipmentId, ReturnShipment.ReturnStatusEnum status);
}
