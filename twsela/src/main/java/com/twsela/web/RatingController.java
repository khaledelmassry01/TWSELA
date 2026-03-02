package com.twsela.web;

import com.twsela.domain.CourierRating;
import com.twsela.domain.Shipment;
import com.twsela.domain.User;
import com.twsela.repository.CourierRatingRepository;
import com.twsela.repository.ShipmentRepository;
import com.twsela.security.AuthenticationHelper;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.CourierRatingDTO;
import com.twsela.web.dto.CourierRatingRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
@Tag(name = "Courier Ratings", description = "تقييمات المناديب")
public class RatingController {

    private static final Logger log = LoggerFactory.getLogger(RatingController.class);

    private final CourierRatingRepository ratingRepository;
    private final ShipmentRepository shipmentRepository;
    private final AuthenticationHelper authHelper;

    public RatingController(CourierRatingRepository ratingRepository,
                           ShipmentRepository shipmentRepository,
                           AuthenticationHelper authHelper) {
        this.ratingRepository = ratingRepository;
        this.shipmentRepository = shipmentRepository;
        this.authHelper = authHelper;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MERCHANT')")
    @Operation(summary = "إضافة تقييم لمندوب")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<CourierRatingDTO>> submitRating(
            @Valid @RequestBody CourierRatingRequest request,
            Authentication authentication) {
        Shipment shipment = shipmentRepository.findById(request.getShipmentId())
                .orElseThrow(() -> new com.twsela.web.exception.ResourceNotFoundException("الشحنة غير موجودة"));

        if (shipment.getManifest() == null || shipment.getManifest().getCourier() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("لم يتم تعيين مندوب لهذه الشحنة"));
        }

        // Check if already rated
        if (ratingRepository.findByShipmentId(request.getShipmentId()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("تم تقييم هذه الشحنة مسبقاً"));
        }

        User courier = shipment.getManifest().getCourier();
        CourierRating rating = new CourierRating(courier, shipment, request.getRating());
        rating.setComment(request.getComment());
        rating.setRatedByPhone(request.getRatedByPhone());
        rating = ratingRepository.save(rating);

        CourierRatingDTO dto = toDTO(rating);
        return ResponseEntity.ok(ApiResponse.ok(dto, "تم إضافة التقييم بنجاح"));
    }

    @GetMapping("/courier/{courierId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "عرض تقييمات مندوب")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<Map<String, Object>>> getCourierRatings(
            @PathVariable Long courierId) {
        List<CourierRating> ratings = ratingRepository.findByCourierIdOrderByCreatedAtDesc(courierId);
        Double avg = ratingRepository.getAverageRatingByCourierId(courierId);
        long count = ratingRepository.countByCourierId(courierId);

        List<CourierRatingDTO> dtos = ratings.stream().map(this::toDTO).toList();

        Map<String, Object> result = Map.of(
                "ratings", dtos,
                "averageRating", avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0,
                "totalRatings", count
        );

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/shipment/{shipmentId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MERCHANT', 'COURIER')")
    @Operation(summary = "عرض تقييم شحنة")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<CourierRatingDTO>> getShipmentRating(
            @PathVariable Long shipmentId) {
        return ratingRepository.findByShipmentId(shipmentId)
                .map(r -> ResponseEntity.ok(ApiResponse.ok(toDTO(r))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private CourierRatingDTO toDTO(CourierRating r) {
        return new CourierRatingDTO(
                r.getId(),
                r.getCourier().getId(),
                r.getCourier().getName(),
                r.getShipment().getId(),
                r.getShipment().getTrackingNumber(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt()
        );
    }
}
