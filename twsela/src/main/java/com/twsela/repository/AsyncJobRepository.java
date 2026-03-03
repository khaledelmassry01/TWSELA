package com.twsela.repository;

import com.twsela.domain.AsyncJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsyncJobRepository extends JpaRepository<AsyncJob, Long> {

    @Query("SELECT j FROM AsyncJob j WHERE j.status = :status AND j.scheduledAt <= :before ORDER BY j.priority ASC, j.createdAt ASC")
    List<AsyncJob> findByStatusAndScheduledAtBefore(@Param("status") AsyncJob.JobStatus status,
                                                     @Param("before") Instant before);

    List<AsyncJob> findByJobType(String jobType);

    List<AsyncJob> findByStatusIn(List<AsyncJob.JobStatus> statuses);

    @Query("SELECT j FROM AsyncJob j WHERE j.status = 'RUNNING'")
    List<AsyncJob> findRunningJobs();

    Optional<AsyncJob> findByJobId(String jobId);

    long countByStatus(AsyncJob.JobStatus status);
}
