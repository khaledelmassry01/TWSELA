package com.twsela.service;

import com.twsela.domain.Country;
import com.twsela.web.exception.ResourceNotFoundException;
import com.twsela.repository.CountryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Localization utilities — formats addresses, phone numbers,
 * and currency values per country configuration.
 */
@Service
@Transactional(readOnly = true)
public class LocalizationService {

    private final CountryRepository countryRepository;

    public LocalizationService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    /**
     * Format phone number with country prefix.
     */
    public String formatPhone(String countryCode, String localNumber) {
        Country country = findCountry(countryCode);
        String cleaned = localNumber.replaceAll("[^0-9]", "");
        if (cleaned.startsWith("0")) {
            cleaned = cleaned.substring(1);
        }
        return country.getPhonePrefix() + cleaned;
    }

    /**
     * Format a monetary amount according to the country's currency.
     */
    public String formatCurrency(String countryCode, BigDecimal amount) {
        Country country = findCountry(countryCode);
        try {
            Currency currency = Currency.getInstance(country.getCurrencyCode());
            Locale locale = resolveLocale(countryCode);
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            fmt.setCurrency(currency);
            return fmt.format(amount);
        } catch (Exception e) {
            return amount.toPlainString() + " " + country.getCurrencyCode();
        }
    }

    /**
     * Format address based on country template.
     * Template placeholders: {line1}, {line2}, {city}, {state}, {postal}, {country}
     */
    public String formatAddress(String countryCode, String line1, String line2,
                                String city, String state, String postalCode) {
        Country country = findCountry(countryCode);
        String template = country.getAddressFormat();
        if (template == null || template.isBlank()) {
            template = "{line1}, {line2}, {city}, {state} {postal}, {country}";
        }
        return template
                .replace("{line1}", nullSafe(line1))
                .replace("{line2}", nullSafe(line2))
                .replace("{city}", nullSafe(city))
                .replace("{state}", nullSafe(state))
                .replace("{postal}", nullSafe(postalCode))
                .replace("{country}", country.getNameEn())
                .replaceAll(",\\s*,", ",")
                .replaceAll("^[,\\s]+|[,\\s]+$", "");
    }

    /**
     * Get the timezone string for a country.
     */
    public String getTimeZone(String countryCode) {
        return findCountry(countryCode).getTimeZone();
    }

    private Country findCountry(String countryCode) {
        return countryRepository.findByCode(countryCode.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("الدولة غير موجودة: " + countryCode));
    }

    private Locale resolveLocale(String countryCode) {
        return switch (countryCode.toUpperCase()) {
            case "EG" -> new Locale("ar", "EG");
            case "SA" -> new Locale("ar", "SA");
            case "AE" -> new Locale("ar", "AE");
            case "JO" -> new Locale("ar", "JO");
            default -> Locale.US;
        };
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
