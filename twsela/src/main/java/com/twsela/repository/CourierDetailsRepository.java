package com.twsela.repository;

import com.twsela.domain.CourierDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CourierDetailsRepository extends JpaRepository<CourierDetails, Long> {
    Optional<CourierDetails> findByUserId(Long userId);
}

