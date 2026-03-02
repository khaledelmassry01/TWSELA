package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.domain.SupportTicket.*;
import com.twsela.repository.*;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("خدمة تذاكر الدعم - SupportTicketService")
class SupportTicketServiceTest {

    @Mock private SupportTicketRepository ticketRepository;
    @Mock private SlaPolicyRepository slaPolicyRepository;
    @Mock private KnowledgeArticleRepository articleRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private SupportTicketService service;

    private User reporter;
    private User agent;
    private SupportTicket ticket;
    private SlaPolicy slaPolicy;

    @BeforeEach
    void setUp() {
        reporter = new User();
        reporter.setId(1L);
        reporter.setName("أحمد العميل");

        agent = new User();
        agent.setId(2L);
        agent.setName("سارة الدعم");

        ticket = new SupportTicket();
        ticket.setId(100L);
        ticket.setTicketNumber("TWS-T-AB12CD34");
        ticket.setSubject("مشكلة في الشحنة");
        ticket.setDescription("الشحنة لم تصل");
        ticket.setPriority(TicketPriority.HIGH);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCategory(TicketCategory.SHIPMENT);
        ticket.setReporter(reporter);
        ticket.setMessages(new ArrayList<>());
        ticket.setCreatedAt(Instant.now());

        slaPolicy = new SlaPolicy();
        slaPolicy.setId(1L);
        slaPolicy.setPriority(TicketPriority.HIGH);
        slaPolicy.setFirstResponseHours(4);
        slaPolicy.setResolutionHours(24);
        slaPolicy.setActive(true);
    }

    // ── Ticket Creation ─────────────────────────────────────

    @Nested
    @DisplayName("إنشاء تذكرة")
    class CreateTicket {

        @Test
        @DisplayName("يجب إنشاء تذكرة جديدة بنجاح")
        void shouldCreateTicketSuccessfully() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(reporter));
            when(ticketRepository.save(any(SupportTicket.class))).thenAnswer(inv -> {
                SupportTicket t = inv.getArgument(0);
                t.setId(100L);
                return t;
            });

            SupportTicket result = service.createTicket(
                    1L, "مشكلة", "تفاصيل المشكلة",
                    TicketPriority.HIGH, TicketCategory.SHIPMENT, null);

            assertThat(result.getTicketNumber()).startsWith("TWS-T-");
            assertThat(result.getStatus()).isEqualTo(TicketStatus.OPEN);
            assertThat(result.getReporter().getId()).isEqualTo(1L);
            verify(ticketRepository).save(any());
        }

        @Test
        @DisplayName("يجب رفض الإنشاء إذا المستخدم غير موجود")
        void shouldThrowWhenReporterNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createTicket(
                    99L, "test", "desc", TicketPriority.LOW, TicketCategory.OTHER, null))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── Ticket Assignment ───────────────────────────────────

    @Nested
    @DisplayName("تعيين تذكرة")
    class AssignTicket {

        @Test
        @DisplayName("يجب تعيين التذكرة بنجاح وتغيير الحالة")
        void shouldAssignTicketAndChangeStatus() {
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            when(userRepository.findById(2L)).thenReturn(Optional.of(agent));
            when(ticketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            SupportTicket result = service.assignTicket(100L, 2L);

            assertThat(result.getAssignee().getId()).isEqualTo(2L);
            assertThat(result.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("يجب رفض التعيين إذا المعين غير موجود")
        void shouldThrowWhenAssigneeNotFound() {
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.assignTicket(100L, 99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── Messages ────────────────────────────────────────────

    @Nested
    @DisplayName("رسائل التذكرة")
    class Messages {

        @Test
        @DisplayName("يجب إضافة رسالة وتتبع أول رد")
        void shouldAddMessageAndTrackFirstResponse() {
            ticket.setFirstResponseAt(null);
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            when(userRepository.findById(2L)).thenReturn(Optional.of(agent));
            when(ticketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            SupportTicket result = service.addMessage(100L, 2L, "سنقوم بالمتابعة", false);

            assertThat(result.getFirstResponseAt()).isNotNull();
            assertThat(result.getMessages()).hasSize(1);
        }

        @Test
        @DisplayName("يجب ألا يتم تتبع أول رد إذا كان المرسل هو المبلغ")
        void shouldNotTrackFirstResponseIfSenderIsReporter() {
            ticket.setFirstResponseAt(null);
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            when(userRepository.findById(1L)).thenReturn(Optional.of(reporter));
            when(ticketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            SupportTicket result = service.addMessage(100L, 1L, "تحديث", false);

            assertThat(result.getFirstResponseAt()).isNull();
        }
    }

    // ── Resolve / Close ─────────────────────────────────────

    @Nested
    @DisplayName("حل وإغلاق التذاكر")
    class ResolveClose {

        @Test
        @DisplayName("يجب حل التذكرة بنجاح")
        void shouldResolveTicket() {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            SupportTicket result = service.resolveTicket(100L);

            assertThat(result.getStatus()).isEqualTo(TicketStatus.RESOLVED);
            assertThat(result.getResolvedAt()).isNotNull();
        }

        @Test
        @DisplayName("يجب رفض حل تذكرة محلولة بالفعل")
        void shouldThrowWhenResolvingAlreadyResolved() {
            ticket.setStatus(TicketStatus.RESOLVED);
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> service.resolveTicket(100L))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("يجب إغلاق التذكرة بنجاح")
        void shouldCloseTicket() {
            when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            SupportTicket result = service.closeTicket(100L);

            assertThat(result.getStatus()).isEqualTo(TicketStatus.CLOSED);
            assertThat(result.getClosedAt()).isNotNull();
        }
    }

    // ── SLA ─────────────────────────────────────────────────

    @Nested
    @DisplayName("مراقبة مستوى الخدمة SLA")
    class SLA {

        @Test
        @DisplayName("يجب اكتشاف انتهاك SLA أول رد")
        void shouldDetectFirstResponseSlaBreach() {
            ticket.setCreatedAt(Instant.now().minus(5, ChronoUnit.HOURS));
            ticket.setFirstResponseAt(null);
            ticket.setResolvedAt(null);
            when(slaPolicyRepository.findByPriorityAndActiveTrue(TicketPriority.HIGH))
                    .thenReturn(Optional.of(slaPolicy));

            boolean breached = service.isSlaBreached(ticket);

            assertThat(breached).isTrue();
        }

        @Test
        @DisplayName("يجب ألا يكون هناك انتهاك إذا تم الرد في الوقت")
        void shouldNotBeBreachedWhenRespondedInTime() {
            ticket.setCreatedAt(Instant.now().minus(2, ChronoUnit.HOURS));
            ticket.setFirstResponseAt(Instant.now().minus(1, ChronoUnit.HOURS));
            ticket.setResolvedAt(null);
            when(slaPolicyRepository.findByPriorityAndActiveTrue(TicketPriority.HIGH))
                    .thenReturn(Optional.of(slaPolicy));

            boolean breached = service.isSlaBreached(ticket);

            assertThat(breached).isFalse();
        }

        @Test
        @DisplayName("يجب اكتشاف انتهاك SLA الحل")
        void shouldDetectResolutionSlaBreach() {
            ticket.setCreatedAt(Instant.now().minus(25, ChronoUnit.HOURS));
            ticket.setFirstResponseAt(Instant.now().minus(24, ChronoUnit.HOURS));
            ticket.setResolvedAt(null);
            when(slaPolicyRepository.findByPriorityAndActiveTrue(TicketPriority.HIGH))
                    .thenReturn(Optional.of(slaPolicy));

            boolean breached = service.isSlaBreached(ticket);

            assertThat(breached).isTrue();
        }
    }

    // ── Knowledge Base ──────────────────────────────────────

    @Nested
    @DisplayName("قاعدة المعرفة")
    class KnowledgeBase {

        @Test
        @DisplayName("يجب إنشاء مقالة جديدة غير منشورة")
        void shouldCreateUnpublishedArticle() {
            when(userRepository.findById(2L)).thenReturn(Optional.of(agent));
            when(articleRepository.save(any(KnowledgeArticle.class))).thenAnswer(inv -> {
                KnowledgeArticle a = inv.getArgument(0);
                a.setId(10L);
                return a;
            });

            KnowledgeArticle article = service.createArticle(
                    2L, "كيفية تتبع الشحنة", "المحتوى", TicketCategory.SHIPMENT);

            assertThat(article.isPublished()).isFalse();
            assertThat(article.getTitle()).isEqualTo("كيفية تتبع الشحنة");
        }

        @Test
        @DisplayName("يجب نشر المقالة بنجاح")
        void shouldPublishArticle() {
            KnowledgeArticle article = new KnowledgeArticle();
            article.setId(10L);
            article.setPublished(false);
            when(articleRepository.findById(10L)).thenReturn(Optional.of(article));
            when(articleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            KnowledgeArticle result = service.publishArticle(10L);

            assertThat(result.isPublished()).isTrue();
        }

        @Test
        @DisplayName("يجب زيادة عدد المشاهدات عند عرض المقالة")
        void shouldIncrementViewCount() {
            KnowledgeArticle article = new KnowledgeArticle();
            article.setId(10L);
            article.setViewCount(5);
            when(articleRepository.findById(10L)).thenReturn(Optional.of(article));
            when(articleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            KnowledgeArticle result = service.viewArticle(10L);

            assertThat(result.getViewCount()).isEqualTo(6);
        }
    }
}
