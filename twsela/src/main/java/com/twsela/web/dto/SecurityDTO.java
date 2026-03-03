package com.twsela.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTOs متعلقة بالأمان.
 */
public class SecurityDTO {

    /**
     * طلب فتح حساب مقفل.
     */
    public static class UnlockRequest {
        @NotNull(message = "معرف المستخدم مطلوب")
        private Long userId;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }

    /**
     * طلب حظر عنوان IP.
     */
    public static class IpBlockRequest {
        @NotBlank(message = "عنوان IP مطلوب")
        private String ipAddress;

        private String reason;
        private boolean permanent;

        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public boolean isPermanent() { return permanent; }
        public void setPermanent(boolean permanent) { this.permanent = permanent; }
    }

    private SecurityDTO() {}
}
