package com.twsela.web.dto;

import com.twsela.domain.SupportTicket.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

/**
 * DTOs for support ticket module.
 */
public class SupportDTO {

    public record CreateTicketRequest(
            @NotBlank(message = "الموضوع مطلوب") String subject,
            @NotBlank(message = "الوصف مطلوب") String description,
            @NotNull(message = "الأولوية مطلوبة") TicketPriority priority,
            @NotNull(message = "التصنيف مطلوب") TicketCategory category,
            Long shipmentId
    ) {}

    public record AddMessageRequest(
            @NotBlank(message = "الرسالة مطلوبة") String content,
            boolean internal
    ) {}

    public record TicketResponse(
            Long id, String ticketNumber, String subject, String description,
            TicketPriority priority, TicketStatus status, TicketCategory category,
            Long reporterId, String reporterName,
            Long assigneeId, String assigneeName,
            Long shipmentId,
            Instant firstResponseAt, Instant resolvedAt,
            boolean slaBreached,
            int messageCount, Instant createdAt, Instant updatedAt
    ) {}

    public record MessageResponse(
            Long id, Long senderId, String senderName,
            String content, boolean internal, Instant createdAt
    ) {}

    public record ArticleRequest(
            @NotBlank(message = "العنوان مطلوب") String title,
            @NotBlank(message = "المحتوى مطلوب") String content,
            @NotNull(message = "التصنيف مطلوب") TicketCategory category
    ) {}

    public record ArticleResponse(
            Long id, String title, String content, TicketCategory category,
            boolean published, int viewCount, String authorName, Instant createdAt
    ) {}

    private SupportDTO() {}
}
