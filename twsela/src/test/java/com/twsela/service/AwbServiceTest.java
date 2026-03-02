package com.twsela.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class AwbServiceTest {

    private final AwbService awbService = new AwbService();

    @Test
    @DisplayName("generateAwb() returns correct format TWS-YYYYMMDD-NNNNNN")
    void generateAwb_correctFormat() {
        String awb = awbService.generateAwb();
        assertNotNull(awb);
        assertTrue(awb.matches("^TWS-\\d{8}-\\d{6}$"), "AWB should match pattern: " + awb);
    }

    @Test
    @DisplayName("generateAwb() contains today's date")
    void generateAwb_containsTodaysDate() {
        String awb = awbService.generateAwb();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        assertTrue(awb.contains(today), "AWB should contain today's date");
    }

    @Test
    @DisplayName("generateAwb() generates unique AWBs")
    void generateAwb_uniqueness() {
        String awb1 = awbService.generateAwb();
        String awb2 = awbService.generateAwb();
        String awb3 = awbService.generateAwb();
        assertNotEquals(awb1, awb2);
        assertNotEquals(awb2, awb3);
    }

    @Test
    @DisplayName("isValidAwb() accepts valid format")
    void isValidAwb_validFormat() {
        assertTrue(awbService.isValidAwb("TWS-20250101-000001"));
        assertTrue(awbService.isValidAwb("TWS-20241231-999999"));
    }

    @Test
    @DisplayName("isValidAwb() rejects invalid formats")
    void isValidAwb_invalidFormats() {
        assertFalse(awbService.isValidAwb(null));
        assertFalse(awbService.isValidAwb(""));
        assertFalse(awbService.isValidAwb("ABC-20250101-000001"));
        assertFalse(awbService.isValidAwb("TWS-2025-000001"));
        assertFalse(awbService.isValidAwb("TWS-20250101-0001"));
    }

    @Test
    @DisplayName("extractDate() parses date correctly")
    void extractDate_parsesCorrectly() {
        LocalDate date = awbService.extractDate("TWS-20250115-000001");
        assertEquals(LocalDate.of(2025, 1, 15), date);
    }

    @Test
    @DisplayName("extractDate() throws on invalid AWB")
    void extractDate_throwsOnInvalid() {
        assertThrows(IllegalArgumentException.class, () -> awbService.extractDate("INVALID"));
    }

    @Test
    @DisplayName("roundtrip: generateAwb -> isValid -> extractDate")
    void roundtrip_generateValidateExtract() {
        String awb = awbService.generateAwb();
        assertTrue(awbService.isValidAwb(awb));
        LocalDate extracted = awbService.extractDate(awb);
        assertEquals(LocalDate.now(), extracted);
    }
}
