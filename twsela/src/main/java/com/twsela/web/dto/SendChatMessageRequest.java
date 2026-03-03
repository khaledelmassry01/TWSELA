package com.twsela.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * طلب إرسال رسالة في غرفة محادثة.
 */
public class SendChatMessageRequest {

    @NotNull(message = "roomId is required")
    private Long roomId;

    @NotBlank(message = "content is required")
    private String content;

    private String messageType = "TEXT";

    public SendChatMessageRequest() {}

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
}
