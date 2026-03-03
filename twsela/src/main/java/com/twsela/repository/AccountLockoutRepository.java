package com.twsela.repository;

import com.twsela.domain.AccountLockout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountLockoutRepository extends JpaRepository<AccountLockout, Long> {

    @Query("SELECT a FROM AccountLockout a WHERE a.user.id = :userId AND a.lockoutEnd > :now AND a.unlockedAt IS NULL")
    Optional<AccountLockout> findActiveByUserId(@Param("userId") Long userId, @Param("now") Instant now);

    @Query("SELECT a FROM AccountLockout a WHERE a.lockoutEnd > :now AND a.unlockedAt IS NULL")
    List<AccountLockout> findActiveLockouts(@Param("now") Instant now);

    List<AccountLockout> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<AccountLockout> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
