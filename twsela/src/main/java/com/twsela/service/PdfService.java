package com.twsela.service;

import com.twsela.domain.Shipment;
import com.twsela.domain.User;
import com.google.zxing.WriterException;
import com.itextpdf.barcodes.Barcode128;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.layout.font.FontSet;
import com.itextpdf.layout.properties.BaseDirection;
import com.itextpdf.layout.properties.Property;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.Bidi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PdfService {

    private static final Logger log = LoggerFactory.getLogger(PdfService.class);

    private static final String FONT = "src/main/resources/fonts/NotoSansArabic-Regular.ttf";
    private static final String COMPANY_NAME = "توصيله";
    private static final String CUSTOMER_SERVICE_PHONE = "01234567890";

    public byte[] generateShipmentLabel(Shipment shipment) throws IOException, WriterException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfDocument pdf = new PdfDocument(new PdfWriter(baos))) {
            
            // Corrected Page Size: Half A4
            PageSize a4 = PageSize.A4;
            Rectangle pageSize = new Rectangle(a4.getWidth(), a4.getHeight() / 2);
            Document document = new Document(pdf, new PageSize(pageSize));
            document.setMargins(15, 15, 15, 15);

            // --- FONT AND TYPOGRAPHY SETUP ---
            FontSet fontSet = new FontSet();
            fontSet.addFont(FONT);
            FontProvider fontProvider = new FontProvider(fontSet);
            document.setFontProvider(fontProvider);
            document.setProperty(Property.FONT, new String[]{"Noto Sans Arabic"});
            // --- END OF SETUP ---

            addLabelContent(document, shipment);

            document.close();
            return baos.toByteArray();
        }
    }

    public byte[] generateBulkLabels(java.util.List<Shipment> shipments) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfDocument pdf = new PdfDocument(new PdfWriter(baos))) {
            // Use A4 for bulk printing
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(15, 15, 15, 15);

            // --- FONT AND TYPOGRAPHY SETUP ---
            FontSet fontSet = new FontSet();
            fontSet.addFont(FONT);
            FontProvider fontProvider = new FontProvider(fontSet);
            document.setFontProvider(fontProvider);
            document.setProperty(Property.FONT, new String[]{"Noto Sans Arabic"});
            // --- END OF SETUP ---

            for (int i = 0; i < shipments.size(); i++) {
                if (i > 0) {
                    document.add(new com.itextpdf.layout.element.AreaBreak());
                }
                addLabelContent(document, shipments.get(i));
            }

            document.close();
            return baos.toByteArray();
        }
    }

    private void addLabelContent(Document document, Shipment shipment) throws IOException, WriterException {
        // Use a master table to control the entire layout
        Table masterTable = new Table(1).setWidth(UnitValue.createPercentValue(100));

        // ** HEADER CELL **
        Cell headerCell = new Cell().setBorder(Border.NO_BORDER);
        Table headerContentTable = new Table(UnitValue.createPercentArray(new float[]{30, 70})).setWidth(UnitValue.createPercentValue(100));
        headerContentTable.addCell(createCell(COMPANY_NAME, 20, true).setTextAlignment(TextAlignment.LEFT).setVerticalAlignment(VerticalAlignment.MIDDLE));
        
        Barcode128 barcode = new Barcode128(document.getPdfDocument());
        barcode.setCode(shipment.getTrackingNumber());
        barcode.setCodeType(Barcode128.CODE128);
        PdfFormXObject barcodeXObject = barcode.createFormXObject(null, null, document.getPdfDocument());
        Image barcodeImage = new Image(barcodeXObject).setWidth(UnitValue.createPercentValue(100)).setHeight(35);
        Cell barcodeCellContent = createCell("", 1, false).add(barcodeImage).add(new Paragraph(shipment.getTrackingNumber()).setTextAlignment(TextAlignment.CENTER).setFontSize(10));
        headerContentTable.addCell(barcodeCellContent);
        headerCell.add(headerContentTable);
        masterTable.addCell(headerCell);

        // ** RECIPIENT CELL **
        Cell recipientCellContainer = new Cell().setBorder(Border.NO_BORDER).setPaddingTop(10);
        Table recipientTable = new Table(1).setWidth(UnitValue.createPercentValue(100)).setBorder(new SolidBorder(1));
        recipientTable.addCell(createCell("إلى المستلم", 8, true).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        Cell recipientInfoCell = createCell("", 10, false).setPadding(8);
        
        // Process Arabic text for recipient info
        Paragraph nameParagraph = new Paragraph(processArabicText(shipment.getRecipientName()));
        nameParagraph.setBold().setFontSize(12);
        nameParagraph.setTextAlignment(TextAlignment.RIGHT);
        nameParagraph.setBaseDirection(BaseDirection.RIGHT_TO_LEFT);
        recipientInfoCell.add(nameParagraph);
        
        Paragraph addressParagraph = new Paragraph(processArabicText(shipment.getRecipientAddress()));
        addressParagraph.setFontSize(10);
        addressParagraph.setTextAlignment(TextAlignment.RIGHT);
        addressParagraph.setBaseDirection(BaseDirection.RIGHT_TO_LEFT);
        recipientInfoCell.add(addressParagraph);
        
        if (shipment.getRecipientPhone() != null && !shipment.getRecipientPhone().trim().isEmpty()) {
            Paragraph phoneParagraph = new Paragraph(shipment.getRecipientPhone());
            phoneParagraph.setFontSize(10);
            phoneParagraph.setTextAlignment(TextAlignment.RIGHT);
            phoneParagraph.setBaseDirection(BaseDirection.RIGHT_TO_LEFT);
            recipientInfoCell.add(phoneParagraph);
        }
        
        recipientTable.addCell(recipientInfoCell);
        recipientCellContainer.add(recipientTable);
        masterTable.addCell(recipientCellContainer);

        // ** SENDER & DETAILS CELL **
        Cell detailsCellContainer = new Cell().setBorder(Border.NO_BORDER).setPaddingTop(5);
        Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).setWidth(UnitValue.createPercentValue(100));

                // Sender
        User merchant = shipment.getMerchant();
        Cell senderCell = createCell("", 9, false);
        
        Paragraph senderTitle = new Paragraph(processArabicText("من المرسل"));
        senderTitle.setBold().setFontSize(8);
        senderTitle.setTextAlignment(TextAlignment.RIGHT);
        senderTitle.setBaseDirection(BaseDirection.RIGHT_TO_LEFT);
        senderCell.add(senderTitle);
        
        if(merchant != null && merchant.getName() != null && !merchant.getName().trim().isEmpty()) {
            Paragraph merchantName = new Paragraph(processArabicText(merchant.getName()));
            merchantName.setTextAlignment(TextAlignment.RIGHT);
            merchantName.setBaseDirection(BaseDirection.RIGHT_TO_LEFT);
            senderCell.add(merchantName);
        }
        detailsTable.addCell(senderCell);

        // COD (Total Amount Required)
        Cell codCell = createCell("", 9, false).setTextAlignment(TextAlignment.CENTER);
        
        Paragraph codTitle = new Paragraph(processArabicText("المبلغ الإجمالي المطلوب"));
        codTitle.setBold().setFontSize(8);
        codTitle.setTextAlignment(TextAlignment.CENTER);
        codTitle.setBaseDirection(BaseDirection.RIGHT_TO_LEFT);
        codCell.add(codTitle);
        
        // Calculate total amount: COD + Delivery Fee
        java.math.BigDecimal totalAmount = shipment.getCodAmount().add(shipment.getDeliveryFee());
        Paragraph codAmount = new Paragraph(totalAmount.toString());
        codAmount.setBold().setFontSize(14);
        codAmount.setTextAlignment(TextAlignment.CENTER);
        codCell.add(codAmount);
        
        // Add breakdown details
        Paragraph breakdown = new Paragraph(processArabicText(""));
        breakdown.setFontSize(8);
        breakdown.setTextAlignment(TextAlignment.CENTER);
        breakdown.setBaseDirection(BaseDirection.RIGHT_TO_LEFT);
        codCell.add(breakdown);
        
        detailsTable.addCell(codCell);
        
        detailsCellContainer.add(detailsTable);
        masterTable.addCell(detailsCellContainer);

        // Add the master table to the document
        document.add(masterTable);

        // ** FOOTER **
        Paragraph footer = new Paragraph(processArabicText("خدمة العملاء:") + CUSTOMER_SERVICE_PHONE)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(8)
                        .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setFixedPosition(15, 15, document.getPdfDocument().getDefaultPageSize().getWidth() - 30);
        document.add(footer);
    }

    // Helper method to create styled cells with correct RTL handling
    private Cell createCell(String text, float fontSize, boolean isBold) {
        // Process Arabic text to ensure proper RTL rendering
        String processedText = processArabicText(text);
        
        Paragraph p = new Paragraph(processedText).setFontSize(fontSize);
        p.setTextAlignment(TextAlignment.RIGHT); // Force right alignment
        p.setBaseDirection(BaseDirection.RIGHT_TO_LEFT); // Force RTL direction
        if (isBold) {
            p.setBold();
        }
        Cell cell = new Cell().add(p);
        cell.setPadding(2);
        cell.setBorder(Border.NO_BORDER);
        return cell;
    }

    /**
     * Processes Arabic text to ensure proper shaping and RTL rendering
     * @param text The input text
     * @return Properly shaped Arabic text
     */
    private String processArabicText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        // Clean the text more aggressively - remove all problematic characters
        String cleanText = text.trim()
            .replaceAll("[\\u0000-\\u001F\\u007F-\\u009F]", "") // Control characters
            .replaceAll("[\\u2000-\\u200F]", "") // General punctuation
            .replaceAll("[\\u2028-\\u202F]", "") // Line/paragraph separators
            .replaceAll("[\\u205F-\\u206F]", "") // Mathematical operators
            .replaceAll("[\\uFEFF]", "") // Zero width no-break space
            .replaceAll("[\\uFFFD]", "") // Replacement character
            .replaceAll("\\s+", " ") // Multiple spaces to single space
            .trim();
        
        // If text becomes empty after cleaning, return empty string
        if (cleanText.isEmpty()) {
            return "";
        }
        
        try {
            // Step 1: Shape Arabic letters (connect them properly)
            ArabicShaping shaper = new ArabicShaping(ArabicShaping.LETTERS_SHAPE | ArabicShaping.DIGITS_EN2AN);
            String shapedText = shaper.shape(cleanText);

            // Step 2: Handle RTL reordering
            Bidi bidi = new Bidi(shapedText, Bidi.RTL);
            bidi.setReorderingMode(Bidi.REORDER_DEFAULT);
            String reorderedText = bidi.writeReordered(Bidi.DO_MIRRORING);
            
            return reorderedText;
        } catch (Exception e) {
            e.printStackTrace();
            return cleanText; // Fallback to cleaned text
        }
    }
}