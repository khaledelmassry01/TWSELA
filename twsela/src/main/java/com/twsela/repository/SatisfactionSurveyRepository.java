package com.twsela.repository;

import com.twsela.domain.SatisfactionSurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SatisfactionSurveyRepository extends JpaRepository<SatisfactionSurvey, Long> {
    Optional<SatisfactionSurvey> findByShipmentId(Long shipmentId);
    List<SatisfactionSurvey> findByRecipientProfileIdOrderBySubmittedAtDesc(Long recipientProfileId);
    List<SatisfactionSurvey> findByOverallRatingGreaterThanEqual(Integer rating);
}
