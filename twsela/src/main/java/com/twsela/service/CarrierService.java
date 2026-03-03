package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.repository.*;
import com.twsela.web.dto.MultiCarrierDTO.*;
import com.twsela.web.exception.DuplicateResourceException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CarrierService {

    private final CarrierRepository carrierRepository;
    private final CarrierZoneMappingRepository zoneMappingRepository;
    private final CarrierRateRepository rateRepository;
    private final CarrierShipmentRepository shipmentRepository;
    private final CarrierWebhookLogRepository webhookLogRepository;
    private final CarrierSelectionRuleRepository selectionRuleRepository;

    public CarrierService(CarrierRepository carrierRepository,
                          CarrierZoneMappingRepository zoneMappingRepository,
                          CarrierRateRepository rateRepository,
                          CarrierShipmentRepository shipmentRepository,
                          CarrierWebhookLogRepository webhookLogRepository,
                          CarrierSelectionRuleRepository selectionRuleRepository) {
        this.carrierRepository = carrierRepository;
        this.zoneMappingRepository = zoneMappingRepository;
        this.rateRepository = rateRepository;
        this.shipmentRepository = shipmentRepository;
        this.webhookLogRepository = webhookLogRepository;
        this.selectionRuleRepository = selectionRuleRepository;
    }

    // === Carriers ===

    public CarrierResponse createCarrier(CreateCarrierRequest request, Long tenantId) {
        if (carrierRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Carrier already exists with code: " + request.code());
        }
        Carrier c = new Carrier();
        c.setName(request.name());
        c.setCode(request.code());
        c.setType(request.type() != null ? request.type() : "INTERNATIONAL");
        c.setApiEndpoint(request.apiEndpoint());
        c.setApiKey(request.apiKey());
        c.setSupportedCountries(request.supportedCountries());
        c.setTenantId(tenantId);
        return toCarrierResponse(carrierRepository.save(c));
    }

    @Transactional(readOnly = true)
    public CarrierResponse getCarrierById(Long id) {
        return toCarrierResponse(carrierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier not found: " + id)));
    }

    @Transactional(readOnly = true)
    public List<CarrierResponse> getActiveCarriers(Long tenantId) {
        return carrierRepository.findByTenantIdAndStatusOrderByNameAsc(tenantId, "ACTIVE")
                .stream().map(this::toCarrierResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CarrierResponse> getAllCarriers(Long tenantId) {
        return carrierRepository.findByTenantIdOrderByNameAsc(tenantId)
                .stream().map(this::toCarrierResponse).toList();
    }

    // === Zone Mappings ===

    public CarrierZoneMappingResponse createZoneMapping(CreateCarrierZoneMappingRequest request) {
        CarrierZoneMapping m = new CarrierZoneMapping();
        m.setCarrierId(request.carrierId());
        m.setZoneId(request.zoneId());
        m.setCarrierZoneCode(request.carrierZoneCode());
        m.setDeliveryDays(request.deliveryDays() != null ? request.deliveryDays() : 3);
        return toZoneMappingResponse(zoneMappingRepository.save(m));
    }

    @Transactional(readOnly = true)
    public List<CarrierZoneMappingResponse> getMappingsByCarrier(Long carrierId) {
        return zoneMappingRepository.findByCarrierId(carrierId)
                .stream().map(this::toZoneMappingResponse).toList();
    }

    // === Rates ===

    public CarrierRateResponse createRate(CreateCarrierRateRequest request) {
        CarrierRate r = new CarrierRate();
        r.setCarrierId(request.carrierId());
        r.setCarrierZoneMappingId(request.carrierZoneMappingId());
        r.setMinWeight(request.minWeight());
        r.setMaxWeight(request.maxWeight());
        r.setBasePrice(request.basePrice());
        r.setPerKgPrice(request.perKgPrice());
        r.setCurrency(request.currency() != null ? request.currency() : "EGP");
        return toRateResponse(rateRepository.save(r));
    }

    @Transactional(readOnly = true)
    public List<CarrierRateResponse> getRatesByCarrier(Long carrierId) {
        return rateRepository.findByCarrierId(carrierId)
                .stream().map(this::toRateResponse).toList();
    }

    // === Carrier Shipments ===

    public CarrierShipmentResponse createCarrierShipment(CreateCarrierShipmentRequest request, Long tenantId) {
        CarrierShipment cs = new CarrierShipment();
        cs.setShipmentId(request.shipmentId());
        cs.setCarrierId(request.carrierId());
        cs.setExternalTrackingNumber(request.externalTrackingNumber());
        cs.setShippingCost(request.shippingCost());
        cs.setTenantId(tenantId);
        return toShipmentResponse(shipmentRepository.save(cs));
    }

    @Transactional(readOnly = true)
    public CarrierShipmentResponse getByShipmentId(Long shipmentId) {
        return toShipmentResponse(shipmentRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier shipment not found for shipment: " + shipmentId)));
    }

    // === Webhook Logs ===

    @Transactional(readOnly = true)
    public List<CarrierWebhookLogResponse> getUnprocessedLogs() {
        return webhookLogRepository.findByProcessedFalseOrderByCreatedAtAsc()
                .stream().map(this::toWebhookResponse).toList();
    }

    // === Selection Rules ===

    public CarrierSelectionRuleResponse createSelectionRule(CreateSelectionRuleRequest request, Long tenantId) {
        CarrierSelectionRule r = new CarrierSelectionRule();
        r.setPriority(request.priority());
        r.setZoneId(request.zoneId());
        r.setMinWeight(request.minWeight());
        r.setMaxWeight(request.maxWeight());
        r.setPreferredCarrierId(request.preferredCarrierId());
        r.setFallbackCarrierId(request.fallbackCarrierId());
        r.setCriteria(request.criteria());
        r.setTenantId(tenantId);
        return toRuleResponse(selectionRuleRepository.save(r));
    }

    @Transactional(readOnly = true)
    public List<CarrierSelectionRuleResponse> getActiveRules(Long tenantId) {
        return selectionRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(tenantId)
                .stream().map(this::toRuleResponse).toList();
    }

    // === Mappers ===

    private CarrierResponse toCarrierResponse(Carrier c) {
        return new CarrierResponse(c.getId(), c.getName(), c.getCode(), c.getType(),
                c.getApiEndpoint(), c.getStatus(), c.getSupportedCountries(), c.getTenantId(), c.getCreatedAt());
    }

    private CarrierZoneMappingResponse toZoneMappingResponse(CarrierZoneMapping m) {
        return new CarrierZoneMappingResponse(m.getId(), m.getCarrierId(), m.getZoneId(),
                m.getCarrierZoneCode(), m.getDeliveryDays(), m.getCreatedAt());
    }

    private CarrierRateResponse toRateResponse(CarrierRate r) {
        return new CarrierRateResponse(r.getId(), r.getCarrierId(), r.getCarrierZoneMappingId(),
                r.getMinWeight(), r.getMaxWeight(), r.getBasePrice(), r.getPerKgPrice(),
                r.getCurrency(), r.getCreatedAt());
    }

    private CarrierShipmentResponse toShipmentResponse(CarrierShipment cs) {
        return new CarrierShipmentResponse(cs.getId(), cs.getShipmentId(), cs.getCarrierId(),
                cs.getExternalTrackingNumber(), cs.getExternalStatus(), cs.getLabelUrl(),
                cs.getShippingCost(), cs.getTenantId(), cs.getCreatedAt());
    }

    private CarrierWebhookLogResponse toWebhookResponse(CarrierWebhookLog l) {
        return new CarrierWebhookLogResponse(l.getId(), l.getCarrierId(), l.getEventType(),
                l.getPayload(), l.getProcessed(), l.getError(), l.getCreatedAt());
    }

    private CarrierSelectionRuleResponse toRuleResponse(CarrierSelectionRule r) {
        return new CarrierSelectionRuleResponse(r.getId(), r.getPriority(), r.getZoneId(),
                r.getMinWeight(), r.getMaxWeight(), r.getPreferredCarrierId(), r.getFallbackCarrierId(),
                r.getCriteria(), r.getIsActive(), r.getTenantId(), r.getCreatedAt());
    }
}
