package com.twsela.service;

import com.twsela.domain.EInvoice;
import com.twsela.domain.Invoice;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import com.twsela.repository.EInvoiceRepository;
import com.twsela.repository.InvoiceRepository;
import com.twsela.web.dto.CountryDTO.EInvoiceResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("خدمة الفوترة الإلكترونية")
class EInvoiceServiceTest {

    @Mock EInvoiceRepository eInvoiceRepository;
    @Mock InvoiceRepository invoiceRepository;
    @InjectMocks EInvoiceService eInvoiceService;

    private Invoice sampleInvoice() {
        Invoice inv = new Invoice();
        inv.setId(1L);
        inv.setInvoiceNumber("TWS-INV-001");
        inv.setTotalAmount(new BigDecimal("114.00"));
        inv.setTax(new BigDecimal("14.00"));
        return inv;
    }

    @Nested
    @DisplayName("إنشاء فاتورة إلكترونية")
    class GenerateTests {

        @Test
        @DisplayName("إنشاء فاتورة إلكترونية لمصر بنجاح")
        void generateForEgypt() {
            Invoice inv = sampleInvoice();
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));
            when(eInvoiceRepository.findByInvoiceId(1L)).thenReturn(Optional.empty());
            when(eInvoiceRepository.save(any(EInvoice.class))).thenAnswer(i -> {
                EInvoice e = i.getArgument(0);
                e.setId(1L);
                return e;
            });

            EInvoiceResponse res = eInvoiceService.generateEInvoice(1L, "EG");
            assertThat(res.format()).isEqualTo("ETA");
            assertThat(res.status()).isEqualTo("DRAFT");
            assertThat(res.countryCode()).isEqualTo("EG");
            assertThat(res.serialNumber()).startsWith("TWS-EG-");
        }

        @Test
        @DisplayName("إنشاء فاتورة للسعودية تستخدم ZATCA")
        void generateForSaudiUsesZATCA() {
            Invoice inv = sampleInvoice();
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));
            when(eInvoiceRepository.findByInvoiceId(1L)).thenReturn(Optional.empty());
            when(eInvoiceRepository.save(any(EInvoice.class))).thenAnswer(i -> {
                EInvoice e = i.getArgument(0);
                e.setId(2L);
                return e;
            });

            EInvoiceResponse res = eInvoiceService.generateEInvoice(1L, "SA");
            assertThat(res.format()).isEqualTo("ZATCA");
        }

        @Test
        @DisplayName("فاتورة موجودة بالفعل ترمي استثناء")
        void duplicateThrows() {
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(sampleInvoice()));
            when(eInvoiceRepository.findByInvoiceId(1L)).thenReturn(Optional.of(new EInvoice()));

            assertThatThrownBy(() -> eInvoiceService.generateEInvoice(1L, "EG"))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("فاتورة غير موجودة ترمي استثناء")
        void invoiceNotFoundThrows() {
            when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> eInvoiceService.generateEInvoice(99L, "EG"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("تقديم الفاتورة")
    class SubmitTests {

        @Test
        @DisplayName("تقديم فاتورة مسودة بنجاح")
        void submitDraft() {
            EInvoice ei = new EInvoice();
            ei.setId(1L);
            ei.setInvoice(sampleInvoice());
            ei.setStatus(EInvoice.EInvoiceStatus.DRAFT);
            ei.setCountryCode("EG");
            ei.setFormat(EInvoice.EInvoiceFormat.ETA);
            ei.setSerialNumber("TWS-EG-001");

            when(eInvoiceRepository.findById(1L)).thenReturn(Optional.of(ei));
            when(eInvoiceRepository.save(any(EInvoice.class))).thenAnswer(i -> i.getArgument(0));

            EInvoiceResponse res = eInvoiceService.submitToGovernment(1L);
            assertThat(res.status()).isEqualTo("ACCEPTED");
            assertThat(res.submissionId()).isNotNull();
        }

        @Test
        @DisplayName("تقديم فاتورة مقبولة بالفعل ترمي استثناء")
        void submitAcceptedThrows() {
            EInvoice ei = new EInvoice();
            ei.setId(1L);
            ei.setStatus(EInvoice.EInvoiceStatus.ACCEPTED);
            when(eInvoiceRepository.findById(1L)).thenReturn(Optional.of(ei));

            assertThatThrownBy(() -> eInvoiceService.submitToGovernment(1L))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    @Nested
    @DisplayName("أدوات داخلية")
    class InternalTests {

        @Test
        @DisplayName("بناء الحمولة والتوقيع")
        void buildPayloadAndSign() {
            Invoice inv = sampleInvoice();
            String payload = eInvoiceService.buildPayload(inv, "EG");
            assertThat(payload).contains("TWS-INV-001").contains("EG");

            String signed = eInvoiceService.signPayload(payload);
            assertThat(signed).isNotBlank();
            assertThat(signed).isNotEqualTo(payload);
        }
    }
}
