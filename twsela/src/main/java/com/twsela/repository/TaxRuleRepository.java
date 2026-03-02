package com.twsela.repository;

import com.twsela.domain.TaxRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaxRuleRepository extends JpaRepository<TaxRule, Long> {

    List<TaxRule> findByCountryCodeAndActiveTrue(String countryCode);

    @Query("SELECT tr FROM TaxRule tr WHERE tr.countryCode = :countryCode " +
           "AND tr.active = true " +
           "AND tr.validFrom <= :date " +
           "AND (tr.validTo IS NULL OR tr.validTo >= :date)")
    List<TaxRule> findApplicable(@Param("countryCode") String countryCode,
                                 @Param("date") LocalDate date);
}
