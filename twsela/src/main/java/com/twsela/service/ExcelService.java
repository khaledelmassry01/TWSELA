package com.twsela.service;

import com.twsela.domain.Shipment;
import com.twsela.domain.User;
import com.twsela.repository.UserRepository;
import com.twsela.repository.ZoneRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
public class ExcelService {

    private static final Logger log = LoggerFactory.getLogger(ExcelService.class);

    private final ShipmentService shipmentService;
    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;

    public ExcelService(ShipmentService shipmentService, UserRepository userRepository, 
                       ZoneRepository zoneRepository) {
        this.shipmentService = shipmentService;
        this.userRepository = userRepository;
        this.zoneRepository = zoneRepository;
    }

    public Map<String, Object> processExcelFile(MultipartFile file, String userPhone) throws Exception {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        // Get merchant from authenticated user
        User merchant = userRepository.findByPhone(userPhone)
            .orElseThrow(() -> new RuntimeException("المستخدم غير موجود"));

        if (!merchant.getRole().getName().equals("MERCHANT") && !merchant.getRole().getName().equals("OWNER")) {
            throw new RuntimeException("غير مصرح لهذا المستخدم بإنشاء الشحنات");
        }

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Validate headers
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new RuntimeException("الملف فارغ أو لا يحتوي على رؤوس الأعمدة");
            }

            // Expected headers: Recipient Name, Phone, Address, Zone Name, COD Amount, Package Size, Notes
            String[] expectedHeaders = {"Recipient Name", "Phone", "Address", "Zone Name", "COD Amount", "Package Size", "Notes"};
            for (int i = 0; i < expectedHeaders.length; i++) {
                Cell cell = headerRow.getCell(i);
                if (cell == null || !expectedHeaders[i].equals(getCellValueAsString(cell))) {
                    throw new RuntimeException("رؤوس الأعمدة غير صحيحة. المتوقع: " + String.join(", ", expectedHeaders));
                }
            }

            // Process data rows
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                try {
                    Shipment shipment = createShipmentFromRow(row, merchant);
                    shipmentService.createShipmentFromExcel(merchant.getId(), shipment);
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    errors.add("الصف " + (rowIndex + 1) + ": " + e.getMessage());
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("errors", errors);
        return result;
    }

    private Shipment createShipmentFromRow(Row row, User merchant) throws Exception {
        Shipment shipment = new Shipment();

        // Recipient Name (required)
        String recipientName = getCellValueAsString(row.getCell(0));
        if (recipientName == null || recipientName.trim().isEmpty()) {
            throw new RuntimeException("اسم المستلم مطلوب");
        }
        shipment.setRecipientName(recipientName.trim());

        // Phone (required)
        String phone = getCellValueAsString(row.getCell(1));
        if (phone == null || phone.trim().isEmpty()) {
            throw new RuntimeException("رقم الهاتف مطلوب");
        }
        // Basic phone validation
        if (!phone.matches("^[0-9+\\-\\s()]+$")) {
            throw new RuntimeException("رقم الهاتف غير صحيح: " + phone);
        }
        shipment.setRecipientPhone(phone.trim());

        // Address (required)
        String address = getCellValueAsString(row.getCell(2));
        if (address == null || address.trim().isEmpty()) {
            throw new RuntimeException("العنوان مطلوب");
        }
        shipment.setRecipientAddress(address.trim());

        // Zone Name (required)
        String zoneName = getCellValueAsString(row.getCell(3));
        if (zoneName == null || zoneName.trim().isEmpty()) {
            throw new RuntimeException("اسم المنطقة مطلوب");
        }
        
        // Validate zone exists
        zoneRepository.findByNameIgnoreCase(zoneName.trim())
            .orElseThrow(() -> new RuntimeException("المنطقة غير موجودة: " + zoneName));
        
        // COD Amount (required)
        String codAmountStr = getCellValueAsString(row.getCell(4));
        if (codAmountStr == null || codAmountStr.trim().isEmpty()) {
            throw new RuntimeException("مبلغ الدفع عند الاستلام مطلوب");
        }
        try {
            BigDecimal codAmount = new BigDecimal(codAmountStr.trim());
            if (codAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("مبلغ الدفع عند الاستلام يجب أن يكون موجب");
            }
            shipment.setCodAmount(codAmount);
        } catch (NumberFormatException e) {
            throw new RuntimeException("مبلغ الدفع عند الاستلام غير صحيح: " + codAmountStr);
        }

        // Package Size (optional)
        String packageSizeStr = getCellValueAsString(row.getCell(5));
        if (packageSizeStr != null && !packageSizeStr.trim().isEmpty()) {
            try {
                BigDecimal packageSize = new BigDecimal(packageSizeStr.trim());
                if (packageSize.compareTo(BigDecimal.ZERO) < 0) {
                    throw new RuntimeException("حجم الطرد يجب أن يكون موجب");
                }
                // Store package size in recipient notes for now
                shipment.setRecipientNotes("حجم الطرد: " + packageSize + " كيلو");
            } catch (NumberFormatException e) {
                throw new RuntimeException("حجم الطرد غير صحيح: " + packageSizeStr);
            }
        }

        // Notes (optional)
        String notes = getCellValueAsString(row.getCell(6));
        if (notes != null && !notes.trim().isEmpty()) {
            String existingNotes = shipment.getRecipientNotes();
            if (existingNotes != null && !existingNotes.isEmpty()) {
                shipment.setRecipientNotes(existingNotes + " | ملاحظات: " + notes.trim());
            } else {
                shipment.setRecipientNotes("ملاحظات: " + notes.trim());
            }
        }

        return shipment;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    public byte[] generateTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Shipments Template");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Recipient Name", "Phone", "Address", "Zone Name", "COD Amount", "Package Size", "Notes"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // Add sample data
            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("أحمد محمد");
            sampleRow.createCell(1).setCellValue("01234567890");
            sampleRow.createCell(2).setCellValue("شارع التحرير، القاهرة");
            sampleRow.createCell(3).setCellValue("القاهرة");
            sampleRow.createCell(4).setCellValue("100.00");
            sampleRow.createCell(5).setCellValue("2.5");
            sampleRow.createCell(6).setCellValue("توصيل سريع");
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Convert to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
