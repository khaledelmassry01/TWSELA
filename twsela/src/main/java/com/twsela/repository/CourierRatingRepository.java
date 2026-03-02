package com.twsela.repository;

import com.twsela.domain.CourierRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourierRatingRepository extends JpaRepository<CourierRating, Long> {

    List<CourierRating> findByCourierIdOrderByCreatedAtDesc(Long courierId);

    Optional<CourierRating> findByShipmentId(Long shipmentId);

    @Query("SELECT AVG(r.rating) FROM CourierRating r WHERE r.courier.id = :courierId")
    Double getAverageRatingByCourierId(@Param("courierId") Long courierId);

    @Query("SELECT COUNT(r) FROM CourierRating r WHERE r.courier.id = :courierId")
    long countByCourierId(@Param("courierId") Long courierId);

    @Query("SELECT AVG(r.rating) FROM CourierRating r")
    Double getOverallAverageRating();
}
