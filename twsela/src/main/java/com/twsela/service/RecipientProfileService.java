package com.twsela.service;

import com.twsela.domain.RecipientProfile;
import com.twsela.domain.RecipientAddress;
import com.twsela.domain.DeliveryPreference;
import com.twsela.repository.RecipientProfileRepository;
import com.twsela.repository.RecipientAddressRepository;
import com.twsela.repository.DeliveryPreferenceRepository;
import com.twsela.web.dto.RecipientExperienceDTO.*;
import com.twsela.web.exception.DuplicateResourceException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RecipientProfileService {

    private final RecipientProfileRepository profileRepo;
    private final RecipientAddressRepository addressRepo;
    private final DeliveryPreferenceRepository prefRepo;

    public RecipientProfileService(RecipientProfileRepository profileRepo,
                                    RecipientAddressRepository addressRepo,
                                    DeliveryPreferenceRepository prefRepo) {
        this.profileRepo = profileRepo;
        this.addressRepo = addressRepo;
        this.prefRepo = prefRepo;
    }

    @Transactional(readOnly = true)
    public RecipientProfileResponse getById(Long id) {
        return toResponse(profileRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ملف المستلم غير موجود")));
    }

    @Transactional(readOnly = true)
    public RecipientProfileResponse getByPhone(String phone) {
        return toResponse(profileRepo.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("ملف المستلم غير موجود")));
    }

    public RecipientProfileResponse create(CreateRecipientProfileRequest req) {
        if (profileRepo.existsByPhone(req.phone()))
            throw new DuplicateResourceException("رقم الهاتف مسجل بالفعل");
        var p = new RecipientProfile();
        p.setPhone(req.phone());
        p.setName(req.name());
        p.setEmail(req.email());
        if (req.preferredLanguage() != null) p.setPreferredLanguage(req.preferredLanguage());
        p.setPreferredTimeSlot(req.preferredTimeSlot());
        p.setDeliveryInstructions(req.deliveryInstructions());
        return toResponse(profileRepo.save(p));
    }

    public RecipientProfileResponse update(Long id, CreateRecipientProfileRequest req) {
        var p = profileRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ملف المستلم غير موجود"));
        p.setName(req.name());
        p.setEmail(req.email());
        if (req.preferredLanguage() != null) p.setPreferredLanguage(req.preferredLanguage());
        p.setPreferredTimeSlot(req.preferredTimeSlot());
        p.setDeliveryInstructions(req.deliveryInstructions());
        return toResponse(profileRepo.save(p));
    }

    // ── Addresses ──
    @Transactional(readOnly = true)
    public List<RecipientAddressResponse> getAddresses(Long profileId) {
        return addressRepo.findByRecipientProfileId(profileId).stream().map(this::toAddrResponse).toList();
    }

    public RecipientAddressResponse createAddress(CreateRecipientAddressRequest req) {
        var a = new RecipientAddress();
        a.setRecipientProfileId(req.recipientProfileId());
        a.setLabel(req.label() != null ? req.label() : "HOME");
        a.setAddressLine1(req.addressLine1());
        a.setAddressLine2(req.addressLine2());
        a.setCity(req.city());
        a.setDistrict(req.district());
        a.setPostalCode(req.postalCode());
        a.setLatitude(req.latitude());
        a.setLongitude(req.longitude());
        if (req.isDefault() != null) a.setIsDefault(req.isDefault());
        a.setNotes(req.notes());
        return toAddrResponse(addressRepo.save(a));
    }

    // ── Preferences ──
    @Transactional(readOnly = true)
    public DeliveryPreferenceResponse getPreferences(Long profileId) {
        return toPrefResponse(prefRepo.findByRecipientProfileId(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("تفضيلات التوصيل غير موجودة")));
    }

    public DeliveryPreferenceResponse savePreferences(CreateDeliveryPreferenceRequest req) {
        var pref = prefRepo.findByRecipientProfileId(req.recipientProfileId())
                .orElse(new DeliveryPreference());
        pref.setRecipientProfileId(req.recipientProfileId());
        if (req.preferSafePlace() != null) pref.setPreferSafePlace(req.preferSafePlace());
        pref.setSafePlaceDescription(req.safePlaceDescription());
        if (req.allowNeighborDelivery() != null) pref.setAllowNeighborDelivery(req.allowNeighborDelivery());
        if (req.requireSignature() != null) pref.setRequireSignature(req.requireSignature());
        if (req.requireOtp() != null) pref.setRequireOtp(req.requireOtp());
        if (req.preferContactless() != null) pref.setPreferContactless(req.preferContactless());
        if (req.smsBeforeDelivery() != null) pref.setSmsBeforeDelivery(req.smsBeforeDelivery());
        if (req.smsMinutesBefore() != null) pref.setSmsMinutesBefore(req.smsMinutesBefore());
        return toPrefResponse(prefRepo.save(pref));
    }

    private RecipientProfileResponse toResponse(RecipientProfile p) {
        return new RecipientProfileResponse(p.getId(), p.getPhone(), p.getName(), p.getEmail(),
                p.getPreferredLanguage(), p.getDefaultAddressId(), p.getPreferredTimeSlot(),
                p.getDeliveryInstructions(), p.getTotalDeliveries(), p.getCreatedAt());
    }

    private RecipientAddressResponse toAddrResponse(RecipientAddress a) {
        return new RecipientAddressResponse(a.getId(), a.getRecipientProfileId(), a.getLabel(),
                a.getAddressLine1(), a.getAddressLine2(), a.getCity(), a.getDistrict(),
                a.getPostalCode(), a.getLatitude(), a.getLongitude(), a.getIsDefault(),
                a.getNotes(), a.getCreatedAt());
    }

    private DeliveryPreferenceResponse toPrefResponse(DeliveryPreference p) {
        return new DeliveryPreferenceResponse(p.getId(), p.getRecipientProfileId(),
                p.getPreferSafePlace(), p.getSafePlaceDescription(), p.getAllowNeighborDelivery(),
                p.getRequireSignature(), p.getRequireOtp(), p.getPreferContactless(),
                p.getSmsBeforeDelivery(), p.getSmsMinutesBefore(), p.getUpdatedAt());
    }
}
