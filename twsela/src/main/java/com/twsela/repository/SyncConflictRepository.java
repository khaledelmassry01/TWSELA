package com.twsela.repository;

import com.twsela.domain.SyncConflict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SyncConflictRepository extends JpaRepository<SyncConflict, Long> {
    List<SyncConflict> findBySyncSessionId(Long syncSessionId);
    List<SyncConflict> findByResolutionIsNull();
}
