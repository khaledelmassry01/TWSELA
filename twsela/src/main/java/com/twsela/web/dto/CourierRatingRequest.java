package com.twsela.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for submitting a courier rating
 */
public class CourierRatingRequest {

    @NotNull(message = "معرّف الشحنة مطلوب")
    private Long shipmentId;

    @NotNull(message = "التقييم مطلوب")
    @Min(value = 1, message = "التقييم يجب أن يكون بين 1 و 5")
    @Max(value = 5, message = "التقييم يجب أن يكون بين 1 و 5")
    private Integer rating;

    private String comment;
    private String ratedByPhone;

    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getRatedByPhone() { return ratedByPhone; }
    public void setRatedByPhone(String ratedByPhone) { this.ratedByPhone = ratedByPhone; }
}
