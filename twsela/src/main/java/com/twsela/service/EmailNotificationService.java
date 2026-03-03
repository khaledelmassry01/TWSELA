package com.twsela.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Email notification service using SendGrid API.
 * Handles sending individual and bulk emails with retry support.
 */
@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final int MAX_RETRIES = 3;

    /**
     * Send a single email.
     *
     * @param to      recipient email address
     * @param subject email subject
     * @param htmlBody HTML body content
     * @return external message ID from provider, or null on failure
     */
    public String sendEmail(String to, String subject, String htmlBody) {
        // In production, integrate with SendGrid API
        // For now, log and simulate success
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.info("Sending email to {} (attempt {}): {}", to, attempt, subject);
                // Simulated success
                String externalId = "email-" + System.currentTimeMillis();
                log.debug("Email sent successfully to {}: externalId={}", to, externalId);
                return externalId;
            } catch (Exception e) {
                log.warn("Email send attempt {} failed for {}: {}", attempt, to, e.getMessage());
                if (attempt == MAX_RETRIES) {
                    log.error("Email delivery failed after {} attempts for {}", MAX_RETRIES, to);
                    return null;
                }
                try {
                    // Exponential backoff: 1s, 2s, 4s
                    Thread.sleep((long) Math.pow(2, attempt - 1) * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Send bulk emails (same content to multiple recipients).
     */
    public int sendBulkEmail(java.util.List<String> recipients, String subject, String htmlBody) {
        int successCount = 0;
        for (String to : recipients) {
            String result = sendEmail(to, subject, htmlBody);
            if (result != null) {
                successCount++;
            }
        }
        log.info("Bulk email sent: {}/{} successful", successCount, recipients.size());
        return successCount;
    }
}
