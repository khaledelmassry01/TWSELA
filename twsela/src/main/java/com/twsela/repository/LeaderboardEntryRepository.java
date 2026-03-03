package com.twsela.repository;

import com.twsela.domain.LeaderboardEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderboardEntryRepository extends JpaRepository<LeaderboardEntry, Long> {

    List<LeaderboardEntry> findByPeriodAndPeriodKeyOrderByRankPositionAsc(String period, String periodKey);

    Optional<LeaderboardEntry> findByUserIdAndPeriodAndPeriodKey(Long userId, String period, String periodKey);

    List<LeaderboardEntry> findByTenantIdAndPeriodAndPeriodKeyOrderByRankPositionAsc(Long tenantId, String period, String periodKey);

    List<LeaderboardEntry> findTop10ByPeriodAndPeriodKeyOrderByScoreDesc(String period, String periodKey);
}
