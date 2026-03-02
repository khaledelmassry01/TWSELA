package com.twsela.service;

import com.twsela.domain.ExchangeRate;
import com.twsela.repository.CurrencyRepository;
import com.twsela.repository.ExchangeRateRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import com.twsela.web.dto.CountryDTO.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("خدمة العملات وأسعار الصرف")
class CurrencyServiceTest {

    @Mock CurrencyRepository currencyRepository;
    @Mock ExchangeRateRepository exchangeRateRepository;
    @InjectMocks CurrencyService currencyService;

    @Nested
    @DisplayName("تحويل العملات")
    class ConvertTests {

        @Test
        @DisplayName("تحويل نفس العملة يرجع نفس المبلغ")
        void convertSameCurrency() {
            ConvertResponse res = currencyService.convert(
                    new BigDecimal("100.00"), "EGP", "EGP");
            assertThat(res.convertedAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(res.rate()).isEqualByComparingTo(BigDecimal.ONE);
        }

        @Test
        @DisplayName("تحويل بين عملتين مختلفتين")
        void convertDifferentCurrencies() {
            ExchangeRate rate = new ExchangeRate();
            rate.setBaseCurrency("USD");
            rate.setTargetCurrency("EGP");
            rate.setRate(new BigDecimal("49.500000"));
            rate.setEffectiveDate(LocalDate.now());
            rate.setSource(ExchangeRate.RateSource.MANUAL);

            when(exchangeRateRepository.findLatestRate("USD", "EGP"))
                    .thenReturn(Optional.of(rate));

            ConvertResponse res = currencyService.convert(
                    new BigDecimal("10.00"), "USD", "EGP");
            assertThat(res.convertedAmount()).isEqualByComparingTo(new BigDecimal("495.000000"));
        }

        @Test
        @DisplayName("تحويل بدون سعر صرف يرمي استثناء")
        void convertNoRateThrows() {
            when(exchangeRateRepository.findLatestRate("USD", "XYZ"))
                    .thenReturn(Optional.empty());
            assertThatThrownBy(() -> currencyService.convert(BigDecimal.TEN, "USD", "XYZ"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("سعر الصرف")
    class ExchangeRateTests {

        @Test
        @DisplayName("جلب سعر صرف موجود")
        void getExistingRate() {
            ExchangeRate er = new ExchangeRate();
            er.setId(1L);
            er.setBaseCurrency("USD");
            er.setTargetCurrency("SAR");
            er.setRate(new BigDecimal("3.750000"));
            er.setEffectiveDate(LocalDate.now());
            er.setSource(ExchangeRate.RateSource.MANUAL);

            when(exchangeRateRepository.findLatestRate("USD", "SAR"))
                    .thenReturn(Optional.of(er));

            ExchangeRateResponse res = currencyService.getExchangeRate("USD", "SAR");
            assertThat(res.rate()).isEqualByComparingTo(new BigDecimal("3.750000"));
        }

        @Test
        @DisplayName("تحديث سعر صرف يدوي")
        void updateManualRate() {
            when(exchangeRateRepository.save(any(ExchangeRate.class)))
                    .thenAnswer(inv -> {
                        ExchangeRate e = inv.getArgument(0);
                        e.setId(1L);
                        return e;
                    });

            UpdateExchangeRateRequest req = new UpdateExchangeRateRequest("USD", "EGP",
                    new BigDecimal("50.000000"));
            ExchangeRateResponse res = currencyService.updateRate(req);
            assertThat(res.source()).isEqualTo("MANUAL");
            assertThat(res.rate()).isEqualByComparingTo(new BigDecimal("50.000000"));
        }
    }
}
