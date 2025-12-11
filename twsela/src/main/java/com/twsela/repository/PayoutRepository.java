package com.twsela.repository;

import com.twsela.domain.Payout;
import com.twsela.domain.PayoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, Long> {
    List<Payout> findByUserId(Long userId);
    List<Payout> findByUserIdOrderByPayoutPeriodEndDesc(Long userId);
    List<Payout> findByPayoutTypeAndPayoutPeriodEndBetween(Payout.PayoutType payoutType, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT p FROM Payout p WHERE p.user.id = :userId AND p.payoutPeriodStart <= :date AND p.payoutPeriodEnd >= :date")
    List<Payout> findActivePayoutsForUser(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    List<Payout> findByStatus(PayoutStatus status);
    
    // Additional method for testing - find PayoutStatus by name
    @Query("SELECT ps FROM PayoutStatus ps WHERE ps.name = :statusName")
    Optional<PayoutStatus> findPayoutStatusByName(@Param("statusName") String statusName);
}