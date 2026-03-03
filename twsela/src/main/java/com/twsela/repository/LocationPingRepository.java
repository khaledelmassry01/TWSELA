package com.twsela.repository;

import com.twsela.domain.LocationPing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationPingRepository extends JpaRepository<LocationPing, Long> {

    List<LocationPing> findByTrackingSessionIdOrderByTimestampDesc(Long trackingSessionId);

    long countByTrackingSessionId(Long trackingSessionId);

    List<LocationPing> findTop10ByTrackingSessionIdOrderByTimestampDesc(Long trackingSessionId);
}
