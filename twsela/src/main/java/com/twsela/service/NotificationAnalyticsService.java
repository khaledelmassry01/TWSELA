package com.twsela.service;

import com.twsela.domain.NotificationChannel;
import com.twsela.domain.NotificationDeliveryLog.DeliveryStatus;
import com.twsela.repository.NotificationDeliveryLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Analytics and reporting for notification delivery performance.
 */
@Service
@Transactional(readOnly = true)
public class NotificationAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(NotificationAnalyticsService.class);

    private final NotificationDeliveryLogRepository deliveryLogRepository;

    public NotificationAnalyticsService(NotificationDeliveryLogRepository deliveryLogRepository) {
        this.deliveryLogRepository = deliveryLogRepository;
    }

    /**
     * Get delivery statistics for a date range broken down by channel.
     */
    public Map<String, Object> getDeliveryStats(Instant from, Instant to) {
        List<Object[]> rawData = deliveryLogRepository.getDeliveryStatsByChannelAndStatus(from, to);

        Map<String, Map<String, Long>> channelBreakdown = new LinkedHashMap<>();
        long totalSent = 0, totalDelivered = 0, totalFailed = 0, totalBounced = 0;

        for (Object[] row : rawData) {
            String channel = row[0].toString();
            String status = row[1].toString();
            long count = ((Number) row[2]).longValue();

            channelBreakdown.computeIfAbsent(channel, k -> new LinkedHashMap<>()).put(status, count);

            switch (status) {
                case "SENT": totalSent += count; break;
                case "DELIVERED": totalDelivered += count; break;
                case "FAILED": totalFailed += count; break;
                case "BOUNCED": totalBounced += count; break;
            }
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalSent", totalSent);
        stats.put("totalDelivered", totalDelivered);
        stats.put("totalFailed", totalFailed);
        stats.put("totalBounced", totalBounced);
        stats.put("channelBreakdown", channelBreakdown);
        return stats;
    }

    /**
     * Get email open rate for a date range.
     */
    public double getOpenRate(Instant from, Instant to) {
        long sent = deliveryLogRepository.countByChannelAndStatusAndSentAtBetween(
                NotificationChannel.EMAIL, DeliveryStatus.SENT, from, to);
        long delivered = deliveryLogRepository.countByChannelAndStatusAndSentAtBetween(
                NotificationChannel.EMAIL, DeliveryStatus.DELIVERED, from, to);

        if (sent + delivered == 0) return 0.0;
        return (double) delivered / (sent + delivered) * 100;
    }

    /**
     * Get performance metrics per channel.
     */
    public Map<String, Object> getChannelPerformance(Instant from, Instant to) {
        Map<String, Object> performance = new LinkedHashMap<>();

        for (NotificationChannel channel : NotificationChannel.values()) {
            long sent = deliveryLogRepository.countByChannelAndStatusAndSentAtBetween(
                    channel, DeliveryStatus.SENT, from, to);
            long delivered = deliveryLogRepository.countByChannelAndStatusAndSentAtBetween(
                    channel, DeliveryStatus.DELIVERED, from, to);
            long failed = deliveryLogRepository.countByChannelAndStatusAndSentAtBetween(
                    channel, DeliveryStatus.FAILED, from, to);

            long total = sent + delivered + failed;
            double successRate = total > 0 ? (double)(sent + delivered) / total * 100 : 0;

            Map<String, Object> channelStats = new LinkedHashMap<>();
            channelStats.put("total", total);
            channelStats.put("sent", sent);
            channelStats.put("delivered", delivered);
            channelStats.put("failed", failed);
            channelStats.put("successRate", Math.round(successRate * 100.0) / 100.0);

            performance.put(channel.name(), channelStats);
        }

        return performance;
    }
}
