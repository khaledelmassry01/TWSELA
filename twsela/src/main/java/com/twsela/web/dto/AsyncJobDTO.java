package com.twsela.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTOs متعلقة بالمهام غير المتزامنة.
 */
public class AsyncJobDTO {

    /**
     * طلب إنشاء مهمة جديدة.
     */
    public static class AsyncJobRequest {
        @NotBlank(message = "نوع المهمة مطلوب")
        private String jobType;

        private String payload;
        private int priority = 5;
        private int maxRetries = 3;

        public String getJobType() { return jobType; }
        public void setJobType(String jobType) { this.jobType = jobType; }

        public String getPayload() { return payload; }
        public void setPayload(String payload) { this.payload = payload; }

        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }

        public int getMaxRetries() { return maxRetries; }
        public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    }

    private AsyncJobDTO() {}
}
