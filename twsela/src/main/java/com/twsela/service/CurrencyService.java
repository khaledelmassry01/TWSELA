package com.twsela.service;

import com.twsela.domain.ExchangeRate;
import com.twsela.web.exception.ResourceNotFoundException;
import com.twsela.repository.CurrencyRepository;
import com.twsela.repository.ExchangeRateRepository;
import com.twsela.web.dto.CountryDTO.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CurrencyService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyService.class);

    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    public CurrencyService(CurrencyRepository currencyRepository,
                           ExchangeRateRepository exchangeRateRepository) {
        this.currencyRepository = currencyRepository;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @Transactional(readOnly = true)
    public List<CurrencyResponse> getAllActiveCurrencies() {
        return currencyRepository.findByActiveTrue().stream()
                .map(c -> new CurrencyResponse(c.getId(), c.getCode(), c.getNameEn(),
                        c.getNameAr(), c.getSymbol(), c.getDecimalPlaces(), c.isActive()))
                .toList();
    }

    /**
     * Convert amount from one currency to another.
     */
    @Transactional(readOnly = true)
    public ConvertResponse convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return new ConvertResponse(amount, fromCurrency, amount, toCurrency, BigDecimal.ONE);
        }

        BigDecimal rate = getLatestRate(fromCurrency.toUpperCase(), toCurrency.toUpperCase());
        BigDecimal converted = amount.multiply(rate).setScale(6, RoundingMode.HALF_UP);

        return new ConvertResponse(amount, fromCurrency.toUpperCase(),
                converted, toCurrency.toUpperCase(), rate);
    }

    @Transactional(readOnly = true)
    public ExchangeRateResponse getExchangeRate(String baseCurrency, String targetCurrency) {
        ExchangeRate er = exchangeRateRepository
                .findLatestRate(baseCurrency.toUpperCase(), targetCurrency.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "سعر الصرف غير موجود: " + baseCurrency + " → " + targetCurrency));
        return toResponse(er);
    }

    public ExchangeRateResponse updateRate(UpdateExchangeRateRequest req) {
        ExchangeRate er = new ExchangeRate();
        er.setBaseCurrency(req.baseCurrency().toUpperCase());
        er.setTargetCurrency(req.targetCurrency().toUpperCase());
        er.setRate(req.rate());
        er.setEffectiveDate(LocalDate.now());
        er.setSource(ExchangeRate.RateSource.MANUAL);
        er = exchangeRateRepository.save(er);
        log.info("Updated exchange rate: {} → {} = {}", req.baseCurrency(), req.targetCurrency(), req.rate());
        return toResponse(er);
    }

    private BigDecimal getLatestRate(String base, String target) {
        return exchangeRateRepository.findLatestRate(base, target)
                .map(ExchangeRate::getRate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "سعر الصرف غير موجود: " + base + " → " + target));
    }

    private ExchangeRateResponse toResponse(ExchangeRate er) {
        return new ExchangeRateResponse(er.getId(), er.getBaseCurrency(),
                er.getTargetCurrency(), er.getRate(), er.getEffectiveDate(),
                er.getSource().name());
    }
}
