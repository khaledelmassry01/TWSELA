package com.twsela.web;

import com.twsela.domain.Shipment;
import com.twsela.repository.ShipmentRepository;
import com.twsela.service.AwbService;
import com.twsela.service.BarcodeService;
import com.twsela.service.FileUploadService;
import com.twsela.service.PdfService;
import com.twsela.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shipments")
@Tag(name = "Shipment Labels & POD", description = "بوليصات الشحن وإثبات التسليم")
public class LabelController {

    private static final Logger log = LoggerFactory.getLogger(LabelController.class);

    private final ShipmentRepository shipmentRepository;
    private final PdfService pdfService;
    private final BarcodeService barcodeService;
    private final AwbService awbService;
    private final FileUploadService fileUploadService;

    public LabelController(ShipmentRepository shipmentRepository,
                          PdfService pdfService,
                          BarcodeService barcodeService,
                          AwbService awbService,
                          FileUploadService fileUploadService) {
        this.shipmentRepository = shipmentRepository;
        this.pdfService = pdfService;
        this.barcodeService = barcodeService;
        this.awbService = awbService;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping("/{id}/label")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MERCHANT', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "تحميل بوليصة شحنة PDF")
    public ResponseEntity<byte[]> getShipmentLabel(@PathVariable Long id) {
        try {
            Shipment shipment = shipmentRepository.findById(id)
                    .orElseThrow(() -> new com.twsela.web.exception.ResourceNotFoundException("الشحنة غير موجودة"));

            byte[] pdf = pdfService.generateShipmentLabel(shipment);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=label_" + shipment.getTrackingNumber() + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            log.error("Label generation failed for shipment {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/labels/bulk")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MERCHANT', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "تحميل بوليصات متعددة PDF")
    public ResponseEntity<byte[]> getBulkLabels(@RequestBody List<Long> shipmentIds) {
        try {
            List<Shipment> shipments = shipmentRepository.findAllById(shipmentIds);
            if (shipments.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            byte[] pdf = pdfService.generateBulkLabels(shipments);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bulk_labels.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            log.error("Bulk label generation failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/barcode")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MERCHANT', 'COURIER', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "توليد باركود الشحنة")
    public ResponseEntity<byte[]> getBarcode(@PathVariable Long id) {
        try {
            Shipment shipment = shipmentRepository.findById(id)
                    .orElseThrow(() -> new com.twsela.web.exception.ResourceNotFoundException("الشحنة غير موجودة"));

            byte[] barcode = barcodeService.generateBarcode(shipment.getTrackingNumber());
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(barcode);
        } catch (Exception e) {
            log.error("Barcode generation failed for shipment {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/qrcode")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MERCHANT', 'COURIER', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "توليد QR كود الشحنة")
    public ResponseEntity<byte[]> getQrCode(@PathVariable Long id) {
        try {
            Shipment shipment = shipmentRepository.findById(id)
                    .orElseThrow(() -> new com.twsela.web.exception.ResourceNotFoundException("الشحنة غير موجودة"));

            byte[] qr = barcodeService.generateQrCode(shipment.getTrackingNumber());
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(qr);
        } catch (Exception e) {
            log.error("QR code generation failed for shipment {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/{id}/pod", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'COURIER')")
    @Operation(summary = "رفع إثبات التسليم (صورة)")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<Map<String, Object>>> uploadPod(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "podType", defaultValue = "PHOTO") String podType) {
        try {
            Shipment shipment = shipmentRepository.findById(id)
                    .orElseThrow(() -> new com.twsela.web.exception.ResourceNotFoundException("الشحنة غير موجودة"));

            String imageUrl = fileUploadService.uploadPodImage(file, shipment.getTrackingNumber());

            shipment.setPodData(imageUrl);
            try {
                shipment.setPodType(Shipment.PodType.valueOf(podType));
            } catch (IllegalArgumentException e) {
                shipment.setPodType(Shipment.PodType.PHOTO);
            }
            shipmentRepository.save(shipment);

            Map<String, Object> result = Map.of(
                    "shipmentId", shipment.getId(),
                    "trackingNumber", shipment.getTrackingNumber(),
                    "podUrl", imageUrl,
                    "podType", shipment.getPodType().name()
            );

            return ResponseEntity.ok(ApiResponse.ok(result, "تم رفع إثبات التسليم بنجاح"));
        } catch (Exception e) {
            log.error("POD upload failed for shipment {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("فشل في رفع إثبات التسليم: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/pod")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MERCHANT', 'COURIER', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "عرض إثبات التسليم")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<Map<String, Object>>> getPod(@PathVariable Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new com.twsela.web.exception.ResourceNotFoundException("الشحنة غير موجودة"));

        if (shipment.getPodData() == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> result = Map.of(
                "shipmentId", shipment.getId(),
                "trackingNumber", shipment.getTrackingNumber(),
                "podUrl", shipment.getPodData(),
                "podType", shipment.getPodType() != null ? shipment.getPodType().name() : "UNKNOWN"
        );

        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
