package com.twsela.repository;

import com.twsela.domain.MerchantSubscription;
import com.twsela.domain.MerchantSubscription.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantSubscriptionRepository extends JpaRepository<MerchantSubscription, Long> {

    Optional<MerchantSubscription> findByMerchantIdAndStatusIn(Long merchantId, List<SubscriptionStatus> statuses);

    Optional<MerchantSubscription> findByMerchantId(Long merchantId);

    List<MerchantSubscription> findByStatus(SubscriptionStatus status);

    @Query("SELECT ms FROM MerchantSubscription ms WHERE ms.currentPeriodEnd < :now AND ms.status IN :statuses")
    List<MerchantSubscription> findExpired(@Param("now") Instant now,
                                           @Param("statuses") List<SubscriptionStatus> statuses);

    @Query("SELECT ms FROM MerchantSubscription ms WHERE ms.status = 'TRIAL' AND ms.trialEndsAt < :now")
    List<MerchantSubscription> findExpiredTrials(@Param("now") Instant now);

    boolean existsByMerchantIdAndStatusIn(Long merchantId, List<SubscriptionStatus> activeStatuses);
}
