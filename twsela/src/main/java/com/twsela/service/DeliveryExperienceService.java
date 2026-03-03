package com.twsela.service;

import com.twsela.domain.DeliveryRedirect;
import com.twsela.domain.SatisfactionSurvey;
import com.twsela.repository.DeliveryRedirectRepository;
import com.twsela.repository.SatisfactionSurveyRepository;
import com.twsela.web.dto.RecipientExperienceDTO.*;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class DeliveryExperienceService {

    private final DeliveryRedirectRepository redirectRepo;
    private final SatisfactionSurveyRepository surveyRepo;

    public DeliveryExperienceService(DeliveryRedirectRepository redirectRepo,
                                      SatisfactionSurveyRepository surveyRepo) {
        this.redirectRepo = redirectRepo;
        this.surveyRepo = surveyRepo;
    }

    // ── Delivery Redirects ──
    @Transactional(readOnly = true)
    public List<DeliveryRedirectResponse> getRedirectsByShipment(Long shipmentId) {
        return redirectRepo.findByShipmentIdOrderByCreatedAtDesc(shipmentId).stream()
                .map(this::toRedirectResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DeliveryRedirectResponse> getPendingRedirects() {
        return redirectRepo.findByStatusOrderByCreatedAtDesc("REQUESTED").stream()
                .map(this::toRedirectResponse).toList();
    }

    public DeliveryRedirectResponse createRedirect(CreateDeliveryRedirectRequest req) {
        var r = new DeliveryRedirect();
        r.setShipmentId(req.shipmentId());
        r.setRecipientProfileId(req.recipientProfileId());
        r.setRedirectType(req.redirectType());
        r.setNewAddressId(req.newAddressId());
        r.setHoldUntilDate(req.holdUntilDate());
        r.setNeighborName(req.neighborName());
        r.setNeighborPhone(req.neighborPhone());
        r.setReason(req.reason());
        return toRedirectResponse(redirectRepo.save(r));
    }

    public DeliveryRedirectResponse processRedirect(Long id, String status, Long processedById) {
        var r = redirectRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("طلب إعادة التوجيه غير موجود"));
        r.setStatus(status);
        r.setProcessedAt(Instant.now());
        r.setProcessedById(processedById);
        return toRedirectResponse(redirectRepo.save(r));
    }

    // ── Satisfaction Surveys ──
    @Transactional(readOnly = true)
    public SatisfactionSurveyResponse getSurveyByShipment(Long shipmentId) {
        return toSurveyResponse(surveyRepo.findByShipmentId(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("الاستبيان غير موجود")));
    }

    @Transactional(readOnly = true)
    public List<SatisfactionSurveyResponse> getSurveysByRecipient(Long recipientProfileId) {
        return surveyRepo.findByRecipientProfileIdOrderBySubmittedAtDesc(recipientProfileId).stream()
                .map(this::toSurveyResponse).toList();
    }

    public SatisfactionSurveyResponse submitSurvey(CreateSatisfactionSurveyRequest req) {
        var s = new SatisfactionSurvey();
        s.setShipmentId(req.shipmentId());
        s.setRecipientProfileId(req.recipientProfileId());
        s.setOverallRating(req.overallRating());
        s.setDeliverySpeedRating(req.deliverySpeedRating());
        s.setCourierBehaviorRating(req.courierBehaviorRating());
        s.setPackagingRating(req.packagingRating());
        s.setComment(req.comment());
        s.setWouldRecommend(req.wouldRecommend());
        s.setFeedbackTags(req.feedbackTags());
        return toSurveyResponse(surveyRepo.save(s));
    }

    private DeliveryRedirectResponse toRedirectResponse(DeliveryRedirect r) {
        return new DeliveryRedirectResponse(r.getId(), r.getShipmentId(), r.getRecipientProfileId(),
                r.getRedirectType(), r.getNewAddressId(), r.getHoldUntilDate(),
                r.getNeighborName(), r.getNeighborPhone(), r.getStatus(), r.getReason(),
                r.getRequestedAt(), r.getProcessedAt(), r.getProcessedById(), r.getCreatedAt());
    }

    private SatisfactionSurveyResponse toSurveyResponse(SatisfactionSurvey s) {
        return new SatisfactionSurveyResponse(s.getId(), s.getShipmentId(), s.getRecipientProfileId(),
                s.getOverallRating(), s.getDeliverySpeedRating(), s.getCourierBehaviorRating(),
                s.getPackagingRating(), s.getComment(), s.getWouldRecommend(),
                s.getFeedbackTags(), s.getSubmittedAt());
    }
}
