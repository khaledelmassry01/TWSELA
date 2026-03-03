package com.twsela.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTOs متعلقة بالأحداث والاشتراكات.
 */
public class EventDTO {

    /**
     * طلب إنشاء اشتراك جديد.
     */
    public static class SubscriptionRequest {
        @NotBlank(message = "اسم المشترك مطلوب")
        private String subscriberName;

        @NotBlank(message = "نوع الحدث مطلوب")
        private String eventType;

        @NotBlank(message = "فئة المعالج مطلوبة")
        private String handlerClass;

        private String filterExpression;
        private String retryPolicy;

        public String getSubscriberName() { return subscriberName; }
        public void setSubscriberName(String subscriberName) { this.subscriberName = subscriberName; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public String getHandlerClass() { return handlerClass; }
        public void setHandlerClass(String handlerClass) { this.handlerClass = handlerClass; }

        public String getFilterExpression() { return filterExpression; }
        public void setFilterExpression(String filterExpression) { this.filterExpression = filterExpression; }

        public String getRetryPolicy() { return retryPolicy; }
        public void setRetryPolicy(String retryPolicy) { this.retryPolicy = retryPolicy; }
    }

    private EventDTO() {}
}
