package com.twsela.service;

import com.twsela.domain.Country;
import com.twsela.web.exception.DuplicateResourceException;
import com.twsela.web.exception.ResourceNotFoundException;
import com.twsela.repository.CountryRepository;
import com.twsela.web.dto.CountryDTO.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CountryService {

    private final CountryRepository countryRepository;

    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    @Transactional(readOnly = true)
    public List<CountryResponse> getAllActiveCountries() {
        return countryRepository.findByActiveTrue().stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CountryResponse getByCode(String code) {
        Country c = countryRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("الدولة غير موجودة: " + code));
        return toResponse(c);
    }

    public CountryResponse createCountry(CreateCountryRequest req) {
        if (countryRepository.existsByCode(req.code().toUpperCase())) {
            throw new DuplicateResourceException("الدولة موجودة بالفعل: " + req.code());
        }
        Country c = new Country();
        c.setCode(req.code().toUpperCase());
        c.setNameEn(req.nameEn());
        c.setNameAr(req.nameAr());
        c.setCurrencyCode(req.currencyCode().toUpperCase());
        c.setPhonePrefix(req.phonePrefix());
        c.setAddressFormat(req.addressFormat());
        c.setTimeZone(req.timeZone());
        c.setDefaultPaymentGateway(req.defaultPaymentGateway());
        c.setActive(true);
        return toResponse(countryRepository.save(c));
    }

    public CountryResponse updateCountry(String code, CreateCountryRequest req) {
        Country c = countryRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("الدولة غير موجودة: " + code));
        c.setNameEn(req.nameEn());
        c.setNameAr(req.nameAr());
        c.setCurrencyCode(req.currencyCode().toUpperCase());
        c.setPhonePrefix(req.phonePrefix());
        c.setAddressFormat(req.addressFormat());
        c.setTimeZone(req.timeZone());
        c.setDefaultPaymentGateway(req.defaultPaymentGateway());
        return toResponse(countryRepository.save(c));
    }

    public void toggleActive(String code, boolean active) {
        Country c = countryRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("الدولة غير موجودة: " + code));
        c.setActive(active);
        countryRepository.save(c);
    }

    private CountryResponse toResponse(Country c) {
        return new CountryResponse(
                c.getId(), c.getCode(), c.getNameEn(), c.getNameAr(),
                c.getCurrencyCode(), c.getPhonePrefix(), c.getAddressFormat(),
                c.getTimeZone(), c.isActive(), c.getDefaultPaymentGateway());
    }
}
