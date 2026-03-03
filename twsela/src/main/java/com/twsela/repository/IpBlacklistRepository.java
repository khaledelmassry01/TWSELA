package com.twsela.repository;

import com.twsela.domain.IpBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface IpBlacklistRepository extends JpaRepository<IpBlacklist, Long> {

    Optional<IpBlacklist> findByIpAddress(String ipAddress);

    List<IpBlacklist> findByPermanentTrue();

    @Query("SELECT i FROM IpBlacklist i WHERE i.permanent = true OR (i.expiresAt IS NOT NULL AND i.expiresAt > :now)")
    List<IpBlacklist> findNonExpired(@Param("now") Instant now);

    List<IpBlacklist> findByBlockedByIdOrderByBlockedAtDesc(Long blockedById);
}
