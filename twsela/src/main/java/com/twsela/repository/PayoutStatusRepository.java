package com.twsela.repository;

import com.twsela.domain.PayoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PayoutStatusRepository extends JpaRepository<PayoutStatus, Long> {
    Optional<PayoutStatus> findByName(String name);
}
