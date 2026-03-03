package com.twsela.service;

import com.twsela.domain.NotificationChannel;
import com.twsela.domain.NotificationTemplate;
import com.twsela.domain.NotificationType;
import com.twsela.repository.NotificationTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renders notification templates by substituting {{variable}} placeholders.
 */
@Service
public class TemplateEngine {

    private static final Logger log = LoggerFactory.getLogger(TemplateEngine.class);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)}}");

    private final NotificationTemplateRepository templateRepository;

    public TemplateEngine(NotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    /**
     * Render a template string by replacing {{var}} placeholders with values.
     */
    public String render(String template, Map<String, String> variables) {
        if (template == null || template.isBlank()) {
            return "";
        }
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String varName = matcher.group(1);
            String value = variables.getOrDefault(varName, "{{" + varName + "}}");
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Render a notification for a specific channel and locale.
     *
     * @return array of [subject, body] — subject may be null for non-email channels
     */
    public String[] renderForChannel(NotificationType eventType, NotificationChannel channel,
                                      String locale, Map<String, String> variables) {
        return templateRepository.findByEventTypeAndChannel(eventType, channel)
                .filter(NotificationTemplate::isActive)
                .map(template -> {
                    String body = "ar".equalsIgnoreCase(locale)
                            ? template.getBodyTemplateAr()
                            : (template.getBodyTemplateEn() != null ? template.getBodyTemplateEn() : template.getBodyTemplateAr());
                    String subject = template.getSubjectTemplate();
                    return new String[]{
                            subject != null ? render(subject, variables) : null,
                            render(body, variables)
                    };
                })
                .orElseGet(() -> {
                    log.warn("No active template found for event={} channel={}", eventType, channel);
                    return new String[]{null, null};
                });
    }
}
