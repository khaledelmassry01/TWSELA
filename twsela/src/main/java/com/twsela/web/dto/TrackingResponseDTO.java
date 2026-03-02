package com.twsela.web.dto;

import java.time.Instant;
import java.util.List;

/**
 * DTO for public shipment tracking response.
 * Includes status timeline, courier location when in transit, and ETA.
 */
public class TrackingResponseDTO {

    private String trackingNumber;
    private String currentStatus;
    private String courierName;
    private LocationDTO lastCourierLocation;
    private Long estimatedMinutesToDelivery;
    private String podType;
    private List<StatusTimelineEntry> statusTimeline;

    public TrackingResponseDTO() {}

    // ── Getters & Setters ──────────────────────────────────────

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public String getCourierName() { return courierName; }
    public void setCourierName(String courierName) { this.courierName = courierName; }

    public LocationDTO getLastCourierLocation() { return lastCourierLocation; }
    public void setLastCourierLocation(LocationDTO lastCourierLocation) { this.lastCourierLocation = lastCourierLocation; }

    public Long getEstimatedMinutesToDelivery() { return estimatedMinutesToDelivery; }
    public void setEstimatedMinutesToDelivery(Long estimatedMinutesToDelivery) { this.estimatedMinutesToDelivery = estimatedMinutesToDelivery; }

    public String getPodType() { return podType; }
    public void setPodType(String podType) { this.podType = podType; }

    public List<StatusTimelineEntry> getStatusTimeline() { return statusTimeline; }
    public void setStatusTimeline(List<StatusTimelineEntry> statusTimeline) { this.statusTimeline = statusTimeline; }

    // ── Nested timeline entry ──────────────────────────────────

    public static class StatusTimelineEntry {
        private String status;
        private String notes;
        private Instant timestamp;

        public StatusTimelineEntry() {}

        public StatusTimelineEntry(String status, String notes, Instant timestamp) {
            this.status = status;
            this.notes = notes;
            this.timestamp = timestamp;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    }
}
