package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.domain.SupportTicket.*;
import com.twsela.repository.*;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service for support ticket management, SLA tracking, and knowledge base.
 */
@Service
@Transactional
public class SupportTicketService {

    private static final Logger log = LoggerFactory.getLogger(SupportTicketService.class);

    private final SupportTicketRepository ticketRepository;
    private final SlaPolicyRepository slaPolicyRepository;
    private final KnowledgeArticleRepository articleRepository;
    private final UserRepository userRepository;

    public SupportTicketService(SupportTicketRepository ticketRepository,
                                 SlaPolicyRepository slaPolicyRepository,
                                 KnowledgeArticleRepository articleRepository,
                                 UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.slaPolicyRepository = slaPolicyRepository;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
    }

    // ══════════════════════════════════════════════════════════
    // Ticket CRUD
    // ══════════════════════════════════════════════════════════

    public SupportTicket createTicket(Long reporterId, String subject, String description,
                                       TicketPriority priority, TicketCategory category,
                                       Long shipmentId) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", reporterId));

        SupportTicket ticket = new SupportTicket();
        ticket.setTicketNumber("TWS-T-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        ticket.setSubject(subject);
        ticket.setDescription(description);
        ticket.setPriority(priority);
        ticket.setCategory(category);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setReporter(reporter);
        ticket.setShipmentId(shipmentId);

        SupportTicket saved = ticketRepository.save(ticket);
        log.info("Support ticket {} created by user {}", saved.getTicketNumber(), reporterId);
        return saved;
    }

    @Transactional(readOnly = true)
    public SupportTicket getTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupportTicket", "id", id));
    }

    @Transactional(readOnly = true)
    public SupportTicket getTicketByNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("SupportTicket", "ticketNumber", ticketNumber));
    }

    @Transactional(readOnly = true)
    public Page<SupportTicket> getTicketsByReporter(Long reporterId, Pageable pageable) {
        return ticketRepository.findByReporterIdOrderByCreatedAtDesc(reporterId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<SupportTicket> getTicketsByAssignee(Long assigneeId, Pageable pageable) {
        return ticketRepository.findByAssigneeIdOrderByCreatedAtDesc(assigneeId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<SupportTicket> getTicketsByStatus(TicketStatus status, Pageable pageable) {
        return ticketRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    // ── Ticket Lifecycle ────────────────────────────────────

    public SupportTicket assignTicket(Long ticketId, Long assigneeId) {
        SupportTicket ticket = getTicket(ticketId);
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", assigneeId));

        ticket.setAssignee(assignee);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setUpdatedAt(Instant.now());
        log.info("Ticket {} assigned to user {}", ticket.getTicketNumber(), assigneeId);
        return ticketRepository.save(ticket);
    }

    public SupportTicket addMessage(Long ticketId, Long senderId, String content, boolean internal) {
        SupportTicket ticket = getTicket(ticketId);
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", senderId));

        TicketMessage message = new TicketMessage(ticket, sender, content, internal);
        ticket.getMessages().add(message);

        // Track first response SLA
        if (ticket.getFirstResponseAt() == null && !ticket.getReporter().getId().equals(senderId)) {
            ticket.setFirstResponseAt(Instant.now());
        }

        ticket.setUpdatedAt(Instant.now());
        return ticketRepository.save(ticket);
    }

    public SupportTicket resolveTicket(Long ticketId) {
        SupportTicket ticket = getTicket(ticketId);
        if (ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BusinessRuleException("التذكرة محلولة أو مغلقة بالفعل");
        }
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolvedAt(Instant.now());
        ticket.setUpdatedAt(Instant.now());
        log.info("Ticket {} resolved", ticket.getTicketNumber());
        return ticketRepository.save(ticket);
    }

    public SupportTicket closeTicket(Long ticketId) {
        SupportTicket ticket = getTicket(ticketId);
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(Instant.now());
        ticket.setUpdatedAt(Instant.now());
        log.info("Ticket {} closed", ticket.getTicketNumber());
        return ticketRepository.save(ticket);
    }

    public SupportTicket updatePriority(Long ticketId, TicketPriority newPriority) {
        SupportTicket ticket = getTicket(ticketId);
        ticket.setPriority(newPriority);
        ticket.setUpdatedAt(Instant.now());
        return ticketRepository.save(ticket);
    }

    // ══════════════════════════════════════════════════════════
    // SLA
    // ══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public SlaPolicy getSlaForPriority(TicketPriority priority) {
        return slaPolicyRepository.findByPriorityAndActiveTrue(priority)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean isSlaBreached(SupportTicket ticket) {
        SlaPolicy sla = getSlaForPriority(ticket.getPriority());
        if (sla == null) return false;

        Instant now = Instant.now();
        long hoursSinceCreation = java.time.Duration.between(ticket.getCreatedAt(), now).toHours();

        // Check first response SLA
        if (ticket.getFirstResponseAt() == null && hoursSinceCreation > sla.getFirstResponseHours()) {
            return true;
        }

        // Check resolution SLA
        if (ticket.getResolvedAt() == null && hoursSinceCreation > sla.getResolutionHours()) {
            return true;
        }

        return false;
    }

    // ══════════════════════════════════════════════════════════
    // Knowledge Base
    // ══════════════════════════════════════════════════════════

    public KnowledgeArticle createArticle(Long authorId, String title, String content,
                                           TicketCategory category) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));

        KnowledgeArticle article = new KnowledgeArticle();
        article.setTitle(title);
        article.setContent(content);
        article.setCategory(category);
        article.setAuthor(author);
        article.setPublished(false);

        log.info("Knowledge article '{}' created by user {}", title, authorId);
        return articleRepository.save(article);
    }

    public KnowledgeArticle publishArticle(Long articleId) {
        KnowledgeArticle article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("KnowledgeArticle", "id", articleId));
        article.setPublished(true);
        article.setUpdatedAt(Instant.now());
        return articleRepository.save(article);
    }

    @Transactional(readOnly = true)
    public Page<KnowledgeArticle> getPublishedArticles(Pageable pageable) {
        return articleRepository.findByPublishedTrueOrderByViewCountDesc(pageable);
    }

    @Transactional(readOnly = true)
    public List<KnowledgeArticle> searchArticles(String query) {
        return articleRepository.searchPublished(query);
    }

    public KnowledgeArticle viewArticle(Long articleId) {
        KnowledgeArticle article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("KnowledgeArticle", "id", articleId));
        article.setViewCount(article.getViewCount() + 1);
        return articleRepository.save(article);
    }
}
