package com.twsela.repository;

import com.twsela.domain.FraudBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FraudBlacklistRepository extends JpaRepository<FraudBlacklist, Long> {
    
    @Query("SELECT f FROM FraudBlacklist f WHERE f.entityType = :entityType AND f.entityValue = :entityValue AND f.isActive = true")
    Optional<FraudBlacklist> findByEntityTypeAndEntityValue(@Param("entityType") String entityType, @Param("entityValue") String entityValue);
    
    List<FraudBlacklist> findByEntityTypeAndIsActiveTrue(String entityType);
    
    boolean existsByEntityTypeAndEntityValueAndIsActiveTrue(String entityType, String entityValue);
}
