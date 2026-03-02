package com.twsela.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AWB (Air Waybill) Service
 * Generates unique tracking numbers in format: TWS-YYYYMMDD-NNNNNN
 */
@Service
public class AwbService {

    private static final Logger log = LoggerFactory.getLogger(AwbService.class);
    private static final String PREFIX = "TWS";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final AtomicLong counter = new AtomicLong(System.currentTimeMillis() % 1_000_000);

    /**
     * Generate a unique AWB tracking number
     * Format: TWS-YYYYMMDD-NNNNNN
     */
    public String generateAwb() {
        String datePart = LocalDate.now().format(DATE_FMT);
        long seq = counter.incrementAndGet() % 1_000_000;
        String awb = String.format("%s-%s-%06d", PREFIX, datePart, seq);
        log.debug("Generated AWB: {}", awb);
        return awb;
    }

    /**
     * Validate AWB format
     */
    public boolean isValidAwb(String awb) {
        if (awb == null) return false;
        return awb.matches("^TWS-\\d{8}-\\d{6}$");
    }

    /**
     * Extract date from AWB
     */
    public LocalDate extractDate(String awb) {
        if (!isValidAwb(awb)) {
            throw new IllegalArgumentException("Invalid AWB format: " + awb);
        }
        String datePart = awb.substring(4, 12);
        return LocalDate.parse(datePart, DATE_FMT);
    }
}
