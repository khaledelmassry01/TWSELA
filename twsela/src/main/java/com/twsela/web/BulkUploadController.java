package com.twsela.web;

import com.twsela.domain.User;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.ExcelService;
import com.twsela.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/shipments/bulk")
@Tag(name = "Bulk Upload", description = "رفع الشحنات بالجملة")
public class BulkUploadController {

    private static final Logger log = LoggerFactory.getLogger(BulkUploadController.class);

    private final ExcelService excelService;
    private final AuthenticationHelper authHelper;

    public BulkUploadController(ExcelService excelService, AuthenticationHelper authHelper) {
        this.excelService = excelService;
        this.authHelper = authHelper;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MERCHANT')")
    @Operation(summary = "رفع شحنات بالجملة من ملف Excel")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<Map<String, Object>>> uploadBulk(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            User user = authHelper.getCurrentUser(authentication);

            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("الملف فارغ"));
            }

            String contentType = file.getContentType();
            if (contentType == null || (!contentType.contains("spreadsheet") && !contentType.contains("excel"))) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("يجب رفع ملف Excel (.xlsx)"));
            }

            Map<String, Object> result = excelService.processExcelFile(file, user.getPhone());
            return ResponseEntity.ok(ApiResponse.ok(result, "تم معالجة الملف بنجاح"));
        } catch (Exception e) {
            log.error("Bulk upload failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("فشل في معالجة الملف: " + e.getMessage()));
        }
    }

    @GetMapping("/template")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MERCHANT')")
    @Operation(summary = "تحميل قالب Excel للرفع بالجملة")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] template = excelService.generateTemplate();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=shipments_template.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(template);
        } catch (Exception e) {
            log.error("Template generation failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
