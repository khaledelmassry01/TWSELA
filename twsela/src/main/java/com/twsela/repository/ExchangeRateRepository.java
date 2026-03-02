package com.twsela.repository;

import com.twsela.domain.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findByBaseCurrencyAndTargetCurrencyAndEffectiveDate(
            String baseCurrency, String targetCurrency, LocalDate effectiveDate);

    @Query("SELECT er FROM ExchangeRate er WHERE er.baseCurrency = :base " +
           "AND er.targetCurrency = :target ORDER BY er.effectiveDate DESC LIMIT 1")
    Optional<ExchangeRate> findLatestRate(@Param("base") String baseCurrency,
                                          @Param("target") String targetCurrency);
}
