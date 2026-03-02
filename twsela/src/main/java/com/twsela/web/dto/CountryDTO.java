package com.twsela.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTOs for multi-country & internationalization module.
 */
public final class CountryDTO {
    private CountryDTO() {}

    // ── Country ──
    public record CreateCountryRequest(
        @NotBlank @Size(min = 2, max = 2)  String code,
        @NotBlank String nameEn,
        @NotBlank String nameAr,
        @NotBlank @Size(min = 3, max = 3) String currencyCode,
        @NotBlank String phonePrefix,
        String addressFormat,
        @NotBlank String timeZone,
        String defaultPaymentGateway
    ) {}

    public record CountryResponse(
        Long id, String code, String nameEn, String nameAr,
        String currencyCode, String phonePrefix, String addressFormat,
        String timeZone, boolean active, String defaultPaymentGateway
    ) {}

    // ── Currency ──
    public record CurrencyResponse(
        Long id, String code, String nameEn, String nameAr,
        String symbol, int decimalPlaces, boolean active
    ) {}

    // ── Exchange Rate ──
    public record ExchangeRateResponse(
        Long id, String baseCurrency, String targetCurrency,
        BigDecimal rate, LocalDate effectiveDate, String source
    ) {}

    public record UpdateExchangeRateRequest(
        @NotBlank String baseCurrency,
        @NotBlank String targetCurrency,
        @NotNull @DecimalMin("0.000001") BigDecimal rate
    ) {}

    public record ConvertRequest(
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String fromCurrency,
        @NotBlank String toCurrency
    ) {}

    public record ConvertResponse(
        BigDecimal originalAmount, String fromCurrency,
        BigDecimal convertedAmount, String toCurrency,
        BigDecimal rate
    ) {}

    // ── Tax ──
    public record CreateTaxRuleRequest(
        @NotBlank String countryCode,
        @NotBlank String taxType,
        @NotNull @DecimalMin("0.0000") BigDecimal rate,
        String exemptCategories,
        @NotNull LocalDate validFrom,
        LocalDate validTo
    ) {}

    public record TaxRuleResponse(
        Long id, String countryCode, String taxType,
        BigDecimal rate, String exemptCategories,
        LocalDate validFrom, LocalDate validTo, boolean active
    ) {}

    public record TaxCalculationResponse(
        BigDecimal originalAmount, String countryCode,
        BigDecimal taxRate, BigDecimal taxAmount,
        BigDecimal totalAmount, String taxType
    ) {}

    // ── EInvoice ──
    public record EInvoiceResponse(
        Long id, Long invoiceId, String countryCode,
        String format, String serialNumber,
        String status, String submissionId,
        String submittedAt, String qrCode
    ) {}
}
