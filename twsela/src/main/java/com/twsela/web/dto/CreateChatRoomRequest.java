package com.twsela.web.dto;

import jakarta.validation.constraints.NotNull;

/**
 * طلب إنشاء غرفة محادثة.
 */
public class CreateChatRoomRequest {

    @NotNull(message = "shipmentId is required")
    private Long shipmentId;

    @NotNull(message = "roomType is required")
    private String roomType;

    private String participants;

    public CreateChatRoomRequest() {}

    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public String getParticipants() { return participants; }
    public void setParticipants(String participants) { this.participants = participants; }
}
