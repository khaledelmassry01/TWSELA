package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.repository.DeliveryProofRepository;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;

/**
 * Service for managing delivery proofs (photos, signatures, GPS coordinates).
 */
@Service
@Transactional
public class DeliveryProofService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryProofService.class);

    private final DeliveryProofRepository proofRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public DeliveryProofService(DeliveryProofRepository proofRepository,
                                 ShipmentRepository shipmentRepository,
                                 UserRepository userRepository,
                                 FileStorageService fileStorageService) {
        this.proofRepository = proofRepository;
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Submit proof of delivery for a shipment.
     */
    public DeliveryProof submitProof(Long shipmentId, MultipartFile photo, MultipartFile signature,
                                      Double latitude, Double longitude,
                                      String recipientName, String notes, Long courierId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId));
        User courier = userRepository.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", courierId));

        if (proofRepository.existsByShipmentId(shipmentId)) {
            throw new BusinessRuleException("إثبات التسليم موجود مسبقاً لهذه الشحنة");
        }

        DeliveryProof proof = new DeliveryProof();
        proof.setShipment(shipment);
        proof.setLatitude(latitude);
        proof.setLongitude(longitude);
        proof.setRecipientName(recipientName);
        proof.setNotes(notes);
        proof.setDeliveredAt(Instant.now());
        proof.setCapturedBy(courier);

        // Store photo
        if (photo != null && !photo.isEmpty()) {
            try {
                String photoPath = fileStorageService.storeFile(photo, "delivery-photos");
                proof.setPhotoUrl(photoPath);
            } catch (IOException e) {
                log.warn("Failed to store delivery photo for shipment {}: {}", shipmentId, e.getMessage());
            }
        }

        // Store signature
        if (signature != null && !signature.isEmpty()) {
            try {
                String sigPath = fileStorageService.storeFile(signature, "delivery-signatures");
                proof.setSignatureUrl(sigPath);
            } catch (IOException e) {
                log.warn("Failed to store delivery signature for shipment {}: {}", shipmentId, e.getMessage());
            }
        }

        DeliveryProof saved = proofRepository.save(proof);
        log.info("Delivery proof submitted for shipment {} by courier {}", shipmentId, courierId);
        return saved;
    }

    /**
     * Get proof of delivery for a shipment.
     */
    @Transactional(readOnly = true)
    public DeliveryProof getProof(Long shipmentId) {
        return proofRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryProof", "shipmentId", shipmentId));
    }

    /**
     * Check if proof exists for a shipment.
     */
    @Transactional(readOnly = true)
    public boolean hasProof(Long shipmentId) {
        return proofRepository.existsByShipmentId(shipmentId);
    }

    /**
     * Validate that the delivery location is within a reasonable distance
     * from the expected delivery address.
     *
     * @return true if within acceptable range (~2 km)
     */
    public boolean validateProofLocation(DeliveryProof proof, double expectedLat, double expectedLng) {
        if (proof.getLatitude() == null || proof.getLongitude() == null) {
            return true; // No GPS data to validate
        }
        double distance = haversineDistance(
                proof.getLatitude(), proof.getLongitude(),
                expectedLat, expectedLng);
        return distance <= 2.0; // 2 km tolerance
    }

    /**
     * Calculate the distance in km between two GPS coordinates using the Haversine formula.
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
