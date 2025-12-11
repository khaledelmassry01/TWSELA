package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class FinancialService {

    private final PayoutRepository payoutRepository;
    private final PayoutItemRepository payoutItemRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final PayoutStatusRepository payoutStatusRepository;

    public FinancialService(PayoutRepository payoutRepository, PayoutItemRepository payoutItemRepository,
                          ShipmentRepository shipmentRepository, UserRepository userRepository,
                          PayoutStatusRepository payoutStatusRepository) {
        this.payoutRepository = payoutRepository;
        this.payoutItemRepository = payoutItemRepository;
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
        this.payoutStatusRepository = payoutStatusRepository;
    }

    public Payout createCourierPayout(Long courierId, LocalDate startDate, LocalDate endDate) {
        User courier = userRepository.findById(courierId).orElseThrow();
        
        // Get all delivered shipments for the courier in the period
        List<Shipment> deliveredShipments = shipmentRepository.findByCourierIdAndStatusNameAndCashReconciledFalse(
            courierId, "DELIVERED");
        
        // Calculate total earnings (70% of delivery fees)
        BigDecimal totalEarnings = deliveredShipments.stream()
            .map(shipment -> shipment.getDeliveryFee().multiply(BigDecimal.valueOf(0.70)))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Create payout
        PayoutStatus pendingStatus = payoutStatusRepository.findByName("PENDING").orElseThrow();
        Payout payout = new Payout(courier, Payout.PayoutType.COURIER_SETTLEMENT, pendingStatus, 
                                 startDate, endDate, totalEarnings);
        payout.setDescription("Courier settlement for period " + startDate + " to " + endDate);
        
        payout = payoutRepository.save(payout);
        
        // Create payout items for each shipment
        for (Shipment shipment : deliveredShipments) {
            BigDecimal courierEarning = shipment.getDeliveryFee().multiply(BigDecimal.valueOf(0.70));
            PayoutItem item = new PayoutItem(payout, PayoutItem.SourceType.SHIPMENT, 
                                          shipment.getId(), courierEarning, 
                                          "Delivery fee for shipment " + shipment.getTrackingNumber());
            payoutItemRepository.save(item);
            
            // Link shipment to payout
            shipment.setPayout(payout);
            shipmentRepository.save(shipment);
        }
        
        return payout;
    }

    public Payout createMerchantPayout(Long merchantId, LocalDate startDate, LocalDate endDate) {
        User merchant = userRepository.findById(merchantId).orElseThrow();
        
        // Get all delivered shipments for the merchant in the period
        List<Shipment> deliveredShipments = shipmentRepository.findByMerchantIdAndStatusNameAndPayoutIsNull(
            merchantId, "DELIVERED");
        
        // Calculate total delivery fees collected
        BigDecimal totalFees = deliveredShipments.stream()
            .map(Shipment::getDeliveryFee)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Create payout
        PayoutStatus pendingStatus = payoutStatusRepository.findByName("PENDING").orElseThrow();
        Payout payout = new Payout(merchant, Payout.PayoutType.MERCHANT_PAYOUT, pendingStatus, 
                                 startDate, endDate, totalFees);
        payout.setDescription("Merchant payout for period " + startDate + " to " + endDate);
        
        payout = payoutRepository.save(payout);
        
        // Create payout items for each shipment
        for (Shipment shipment : deliveredShipments) {
            PayoutItem item = new PayoutItem(payout, PayoutItem.SourceType.SHIPMENT, 
                                          shipment.getId(), shipment.getDeliveryFee(), 
                                          "Delivery fee for shipment " + shipment.getTrackingNumber());
            payoutItemRepository.save(item);
            
            // Link shipment to payout
            shipment.setPayout(payout);
            shipmentRepository.save(shipment);
        }
        
        return payout;
    }

    public List<Payout> getPayoutsForUser(Long userId) {
        return payoutRepository.findByUserIdOrderByPayoutPeriodEndDesc(userId);
    }

    public Payout updatePayoutStatus(Long payoutId, String statusName) {
        Payout payout = payoutRepository.findById(payoutId).orElseThrow();
        PayoutStatus status = payoutStatusRepository.findByName(statusName).orElseThrow();
        
        payout.setStatus(status);
        
        if ("COMPLETED".equals(statusName)) {
            payout.setPaidAt(java.time.Instant.now());
        }
        
        return payoutRepository.save(payout);
    }

    public BigDecimal calculateTotalRevenue(LocalDate startDate, LocalDate endDate) {
        List<Shipment> shipments = shipmentRepository.findByCreatedAtBetween(
            startDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant(),
            endDate.plusDays(1).atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()
        );
        
        return shipments.stream()
            .filter(shipment -> "DELIVERED".equals(shipment.getStatus().getName()))
            .map(Shipment::getDeliveryFee)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateCourierEarnings(Long courierId, LocalDate startDate, LocalDate endDate) {
        List<Shipment> shipments = shipmentRepository.findByCourierIdAndStatusNameAndCashReconciledFalse(
            courierId, "DELIVERED");
        
        return shipments.stream()
            .map(shipment -> shipment.getDeliveryFee().multiply(BigDecimal.valueOf(0.70)))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Payout> getPendingPayouts() {
        PayoutStatus pendingStatus = payoutStatusRepository.findByName("PENDING").orElseThrow();
        return payoutRepository.findByStatus(pendingStatus);
    }

    public Payout getPayoutById(Long payoutId) {
        return payoutRepository.findById(payoutId).orElseThrow();
    }

    public List<PayoutItem> getPayoutItems(Long payoutId) {
        return payoutItemRepository.findByPayoutId(payoutId);
    }
}

