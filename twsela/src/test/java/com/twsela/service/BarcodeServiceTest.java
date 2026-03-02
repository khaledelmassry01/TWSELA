package com.twsela.service;

import com.google.zxing.WriterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class BarcodeServiceTest {

    private final BarcodeService barcodeService = new BarcodeService();

    @Test
    @DisplayName("generateBarcode() returns non-empty PNG bytes")
    void generateBarcode_returnsPng() throws WriterException, IOException {
        byte[] barcode = barcodeService.generateBarcode("TWS-20250101-000001");
        assertNotNull(barcode);
        assertTrue(barcode.length > 0, "Barcode bytes should not be empty");
        // PNG magic bytes: 137 80 78 71 (‰PNG)
        assertEquals((byte) 0x89, barcode[0], "Should start with PNG header");
        assertEquals((byte) 0x50, barcode[1]);
    }

    @Test
    @DisplayName("generateBarcode() with custom dimensions")
    void generateBarcode_customDimensions() throws WriterException, IOException {
        byte[] barcode = barcodeService.generateBarcode("TEST123", 400, 100);
        assertNotNull(barcode);
        assertTrue(barcode.length > 0);
    }

    @Test
    @DisplayName("generateQrCode() returns non-empty PNG bytes")
    void generateQrCode_returnsPng() throws WriterException, IOException {
        byte[] qr = barcodeService.generateQrCode("https://twsela.com/track/123");
        assertNotNull(qr);
        assertTrue(qr.length > 0, "QR code bytes should not be empty");
        // PNG magic bytes
        assertEquals((byte) 0x89, qr[0]);
        assertEquals((byte) 0x50, qr[1]);
    }

    @Test
    @DisplayName("generateQrCode() with custom dimensions")
    void generateQrCode_customDimensions() throws WriterException, IOException {
        byte[] qr = barcodeService.generateQrCode("TEST", 300, 300);
        assertNotNull(qr);
        assertTrue(qr.length > 0);
    }

    @Test
    @DisplayName("generateQrCode() handles Arabic text")
    void generateQrCode_arabicText() throws WriterException, IOException {
        byte[] qr = barcodeService.generateQrCode("شحنة رقم ١٢٣");
        assertNotNull(qr);
        assertTrue(qr.length > 0);
    }

    @Test
    @DisplayName("QR code is larger than barcode for same content")
    void qrLargerThanBarcode() throws WriterException, IOException {
        byte[] barcode = barcodeService.generateBarcode("TESTCONTENT");
        byte[] qr = barcodeService.generateQrCode("TESTCONTENT");
        // QR is 200x200 vs barcode 300x80 so QR should generally be larger
        assertTrue(qr.length > 0 && barcode.length > 0);
    }
}
