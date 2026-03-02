package com.twsela.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Barcode & QR Code Generation Service
 * Uses ZXing library for generating Code128 barcodes and QR codes
 */
@Service
public class BarcodeService {

    private static final Logger log = LoggerFactory.getLogger(BarcodeService.class);

    private static final int DEFAULT_BARCODE_WIDTH = 300;
    private static final int DEFAULT_BARCODE_HEIGHT = 80;
    private static final int DEFAULT_QR_SIZE = 200;

    /**
     * Generate Code128 barcode as PNG bytes
     */
    public byte[] generateBarcode(String content) throws WriterException, IOException {
        return generateBarcode(content, DEFAULT_BARCODE_WIDTH, DEFAULT_BARCODE_HEIGHT);
    }

    /**
     * Generate Code128 barcode with custom dimensions
     */
    public byte[] generateBarcode(String content, int width, int height) throws WriterException, IOException {
        Code128Writer writer = new Code128Writer();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.CODE_128, width, height);
        return toBytes(matrix, "PNG");
    }

    /**
     * Generate QR code as PNG bytes
     */
    public byte[] generateQrCode(String content) throws WriterException, IOException {
        return generateQrCode(content, DEFAULT_QR_SIZE, DEFAULT_QR_SIZE);
    }

    /**
     * Generate QR code with custom dimensions
     */
    public byte[] generateQrCode(String content, int width, int height) throws WriterException, IOException {
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = Map.of(
                EncodeHintType.CHARACTER_SET, "UTF-8",
                EncodeHintType.MARGIN, 1
        );
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        return toBytes(matrix, "PNG");
    }

    private byte[] toBytes(BitMatrix matrix, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, format, baos);
        return baos.toByteArray();
    }
}
