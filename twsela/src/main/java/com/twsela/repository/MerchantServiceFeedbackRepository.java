package com.twsela.repository;

import com.twsela.domain.MerchantServiceFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantServiceFeedbackRepository extends JpaRepository<MerchantServiceFeedback, Long> {
    
    List<MerchantServiceFeedback> findByMerchantIdOrderByCreatedAtDesc(Long merchantId);
    
    List<MerchantServiceFeedback> findByCourierIdOrderByCreatedAtDesc(Long courierId);
    
    @Query("SELECT m FROM MerchantServiceFeedback m WHERE m.relatedShipment.id = :shipmentId AND m.courier.id = :courierId")
    List<MerchantServiceFeedback> findByRelatedShipmentIdAndCourierId(@Param("shipmentId") Long shipmentId, @Param("courierId") Long courierId);
    
    @Query("SELECT AVG(m.rating) FROM MerchantServiceFeedback m WHERE m.merchant.id = :merchantId")
    Double findAverageRatingByMerchantId(@Param("merchantId") Long merchantId);
}
