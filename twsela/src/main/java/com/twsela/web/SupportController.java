package com.twsela.web;

import com.twsela.domain.*;
import com.twsela.domain.SupportTicket.*;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.SupportTicketService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.SupportDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for support tickets and knowledge base.
 */
@RestController
@RequestMapping("/api/support")
@Tag(name = "Support", description = "إدارة تذاكر الدعم وقاعدة المعرفة")
public class SupportController {

    private final SupportTicketService supportService;
    private final AuthenticationHelper authHelper;

    public SupportController(SupportTicketService supportService, AuthenticationHelper authHelper) {
        this.supportService = supportService;
        this.authHelper = authHelper;
    }

    // ── Tickets ─────────────────────────────────────────────

    @Operation(summary = "إنشاء تذكرة")
    @PostMapping("/tickets")
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(
            @Valid @RequestBody CreateTicketRequest request, Authentication auth) {
        Long userId = authHelper.getCurrentUserId(auth);
        SupportTicket ticket = supportService.createTicket(
                userId, request.subject(), request.description(),
                request.priority(), request.category(), request.shipmentId());
        return ResponseEntity.ok(ApiResponse.ok(toTicketResponse(ticket), "تم إنشاء التذكرة بنجاح"));
    }

    @Operation(summary = "تذاكري")
    @GetMapping("/tickets/my")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getMyTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        Long userId = authHelper.getCurrentUserId(auth);
        Page<SupportTicket> tickets = supportService.getTicketsByReporter(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(tickets.map(this::toTicketResponse), "تذاكري"));
    }

    @Operation(summary = "تفاصيل تذكرة")
    @GetMapping("/tickets/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicket(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(toTicketResponse(supportService.getTicket(id)), "تفاصيل التذكرة"));
    }

    @Operation(summary = "رسائل التذكرة")
    @GetMapping("/tickets/{id}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(@PathVariable Long id) {
        SupportTicket ticket = supportService.getTicket(id);
        List<MessageResponse> messages = ticket.getMessages().stream()
                .map(m -> new MessageResponse(
                        m.getId(), m.getSender().getId(), m.getSender().getName(),
                        m.getContent(), m.isInternal(), m.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(messages, "رسائل التذكرة"));
    }

    @Operation(summary = "إضافة رسالة")
    @PostMapping("/tickets/{id}/messages")
    public ResponseEntity<ApiResponse<TicketResponse>> addMessage(
            @PathVariable Long id,
            @Valid @RequestBody AddMessageRequest request,
            Authentication auth) {
        Long userId = authHelper.getCurrentUserId(auth);
        SupportTicket ticket = supportService.addMessage(id, userId, request.content(), request.internal());
        return ResponseEntity.ok(ApiResponse.ok(toTicketResponse(ticket), "تمت إضافة الرسالة"));
    }

    @Operation(summary = "تعيين تذكرة")
    @PutMapping("/tickets/{id}/assign/{assigneeId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TicketResponse>> assignTicket(
            @PathVariable Long id, @PathVariable Long assigneeId) {
        SupportTicket ticket = supportService.assignTicket(id, assigneeId);
        return ResponseEntity.ok(ApiResponse.ok(toTicketResponse(ticket), "تم تعيين التذكرة"));
    }

    @Operation(summary = "حل تذكرة")
    @PutMapping("/tickets/{id}/resolve")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TicketResponse>> resolveTicket(@PathVariable Long id) {
        SupportTicket ticket = supportService.resolveTicket(id);
        return ResponseEntity.ok(ApiResponse.ok(toTicketResponse(ticket), "تم حل التذكرة"));
    }

    @Operation(summary = "إغلاق تذكرة")
    @PutMapping("/tickets/{id}/close")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TicketResponse>> closeTicket(@PathVariable Long id) {
        SupportTicket ticket = supportService.closeTicket(id);
        return ResponseEntity.ok(ApiResponse.ok(toTicketResponse(ticket), "تم إغلاق التذكرة"));
    }

    @Operation(summary = "التذاكر حسب الحالة")
    @GetMapping("/tickets/admin")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getTicketsByStatus(
            @RequestParam TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SupportTicket> tickets = supportService.getTicketsByStatus(status, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(tickets.map(this::toTicketResponse), "التذاكر"));
    }

    // ── Knowledge Base ──────────────────────────────────────

    @Operation(summary = "إنشاء مقالة")
    @PostMapping("/articles")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ArticleResponse>> createArticle(
            @Valid @RequestBody ArticleRequest request, Authentication auth) {
        Long userId = authHelper.getCurrentUserId(auth);
        KnowledgeArticle article = supportService.createArticle(
                userId, request.title(), request.content(), request.category());
        return ResponseEntity.ok(ApiResponse.ok(toArticleResponse(article), "تم إنشاء المقالة"));
    }

    @Operation(summary = "نشر مقالة")
    @PutMapping("/articles/{id}/publish")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ArticleResponse>> publishArticle(@PathVariable Long id) {
        KnowledgeArticle article = supportService.publishArticle(id);
        return ResponseEntity.ok(ApiResponse.ok(toArticleResponse(article), "تم نشر المقالة"));
    }

    @Operation(summary = "المقالات المنشورة")
    @GetMapping("/articles")
    public ResponseEntity<ApiResponse<Page<ArticleResponse>>> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<KnowledgeArticle> articles = supportService.getPublishedArticles(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(articles.map(this::toArticleResponse), "المقالات"));
    }

    @Operation(summary = "بحث في المقالات")
    @GetMapping("/articles/search")
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> searchArticles(@RequestParam String q) {
        List<KnowledgeArticle> articles = supportService.searchArticles(q);
        return ResponseEntity.ok(ApiResponse.ok(articles.stream().map(this::toArticleResponse).toList(), "نتائج البحث"));
    }

    @Operation(summary = "عرض مقالة")
    @GetMapping("/articles/{id}")
    public ResponseEntity<ApiResponse<ArticleResponse>> viewArticle(@PathVariable Long id) {
        KnowledgeArticle article = supportService.viewArticle(id);
        return ResponseEntity.ok(ApiResponse.ok(toArticleResponse(article), "المقالة"));
    }

    // ── Mappers ─────────────────────────────────────────────

    private TicketResponse toTicketResponse(SupportTicket t) {
        return new TicketResponse(
                t.getId(), t.getTicketNumber(), t.getSubject(), t.getDescription(),
                t.getPriority(), t.getStatus(), t.getCategory(),
                t.getReporter().getId(), t.getReporter().getName(),
                t.getAssignee() != null ? t.getAssignee().getId() : null,
                t.getAssignee() != null ? t.getAssignee().getName() : null,
                t.getShipmentId(),
                t.getFirstResponseAt(), t.getResolvedAt(),
                supportService.isSlaBreached(t),
                t.getMessages().size(), t.getCreatedAt(), t.getUpdatedAt());
    }

    private ArticleResponse toArticleResponse(KnowledgeArticle a) {
        return new ArticleResponse(
                a.getId(), a.getTitle(), a.getContent(), a.getCategory(),
                a.isPublished(), a.getViewCount(),
                a.getAuthor() != null ? a.getAuthor().getName() : null,
                a.getCreatedAt());
    }
}
