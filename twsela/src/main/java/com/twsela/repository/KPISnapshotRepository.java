package com.twsela.repository;

import com.twsela.domain.KPISnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface KPISnapshotRepository extends JpaRepository<KPISnapshot, Long> {

    Optional<KPISnapshot> findBySnapshotDate(LocalDate date);

    List<KPISnapshot> findBySnapshotDateBetweenOrderBySnapshotDateAsc(LocalDate from, LocalDate to);

    @Query("SELECT k FROM KPISnapshot k ORDER BY k.snapshotDate DESC LIMIT 1")
    Optional<KPISnapshot> findLatest();
}
