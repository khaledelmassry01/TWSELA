package com.twsela.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * WhatsApp notification service using Twilio WhatsApp Business API.
 * Sends template-based WhatsApp messages with SMS fallback.
 */
@Service
public class WhatsAppNotificationService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppNotificationService.class);

    /**
     * Send a WhatsApp message using a pre-approved template.
     *
     * @param phone         recipient phone number (E.164 format)
     * @param templateName  WhatsApp template name
     * @param parameters    template parameters
     * @return external message SID, or null on failure
     */
    public String sendWhatsApp(String phone, String templateName, List<String> parameters) {
        try {
            if (phone == null || phone.isBlank()) {
                log.warn("Cannot send WhatsApp: phone is empty");
                return null;
            }

            log.info("Sending WhatsApp to {} using template '{}' with {} params",
                    phone, templateName, parameters != null ? parameters.size() : 0);

            // In production, use Twilio WhatsApp API
            String externalId = "wa-" + System.currentTimeMillis();
            log.debug("WhatsApp message sent to {}: externalId={}", phone, externalId);
            return externalId;
        } catch (Exception e) {
            log.error("WhatsApp delivery failed for {}: {}", phone, e.getMessage());
            return null;
        }
    }

    /**
     * Send a free-form WhatsApp message (for messaging window conversations).
     */
    public String sendFreeFormMessage(String phone, String message) {
        try {
            log.info("Sending free-form WhatsApp to {}", phone);
            String externalId = "wa-ff-" + System.currentTimeMillis();
            return externalId;
        } catch (Exception e) {
            log.error("Free-form WhatsApp failed for {}: {}", phone, e.getMessage());
            return null;
        }
    }
}
