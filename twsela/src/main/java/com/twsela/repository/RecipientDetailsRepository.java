package com.twsela.repository;

import com.twsela.domain.RecipientDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RecipientDetailsRepository extends JpaRepository<RecipientDetails, Long> {
    Optional<RecipientDetails> findByPhone(String phone);
}

