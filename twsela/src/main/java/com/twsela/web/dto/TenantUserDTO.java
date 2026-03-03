package com.twsela.web.dto;

import com.twsela.domain.TenantInvitation;
import com.twsela.domain.TenantQuota;
import com.twsela.domain.TenantUser;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * DTOs الخاصة بمستخدمي المستأجر.
 */
public class TenantUserDTO {

    /**
     * طلب دعوة.
     */
    public static class InvitationRequest {
        @NotBlank(message = "رقم الهاتف مطلوب")
        private String phone;

        @NotNull(message = "الدور مطلوب")
        private TenantUser.TenantRole role;

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public TenantUser.TenantRole getRole() { return role; }
        public void setRole(TenantUser.TenantRole role) { this.role = role; }
    }

    /**
     * استجابة بيانات الدعوة.
     */
    public static class InvitationResponse {
        private Long id;
        private String phone;
        private String role;
        private String status;
        private String token;
        private Instant expiresAt;
        private Instant createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public Instant getExpiresAt() { return expiresAt; }
        public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
        public Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

        public static InvitationResponse from(TenantInvitation invitation) {
            InvitationResponse resp = new InvitationResponse();
            resp.setId(invitation.getId());
            resp.setPhone(invitation.getPhone());
            resp.setRole(invitation.getRole() != null ? invitation.getRole().name() : null);
            resp.setStatus(invitation.getStatus() != null ? invitation.getStatus().name() : null);
            resp.setToken(invitation.getToken());
            resp.setExpiresAt(invitation.getExpiresAt());
            resp.setCreatedAt(invitation.getCreatedAt());
            return resp;
        }
    }

    /**
     * استجابة بيانات مستخدم المستأجر.
     */
    public static class TenantUserResponse {
        private Long id;
        private Long userId;
        private String userName;
        private String userPhone;
        private String role;
        private boolean active;
        private Instant joinedAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getUserPhone() { return userPhone; }
        public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public Instant getJoinedAt() { return joinedAt; }
        public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }

        public static TenantUserResponse from(TenantUser tenantUser) {
            TenantUserResponse resp = new TenantUserResponse();
            resp.setId(tenantUser.getId());
            if (tenantUser.getUser() != null) {
                resp.setUserId(tenantUser.getUser().getId());
                resp.setUserName(tenantUser.getUser().getName());
                resp.setUserPhone(tenantUser.getUser().getPhone());
            }
            resp.setRole(tenantUser.getRole() != null ? tenantUser.getRole().name() : null);
            resp.setActive(tenantUser.isActive());
            resp.setJoinedAt(tenantUser.getJoinedAt());
            return resp;
        }
    }

    /**
     * طلب تغيير الدور.
     */
    public static class ChangeRoleRequest {
        @NotNull(message = "الدور الجديد مطلوب")
        private TenantUser.TenantRole role;

        public TenantUser.TenantRole getRole() { return role; }
        public void setRole(TenantUser.TenantRole role) { this.role = role; }
    }

    /**
     * استجابة بيانات الحصة.
     */
    public static class QuotaResponse {
        private Long id;
        private String quotaType;
        private long maxValue;
        private long currentValue;
        private String resetPeriod;
        private boolean exceeded;
        private Instant lastResetAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getQuotaType() { return quotaType; }
        public void setQuotaType(String quotaType) { this.quotaType = quotaType; }
        public long getMaxValue() { return maxValue; }
        public void setMaxValue(long maxValue) { this.maxValue = maxValue; }
        public long getCurrentValue() { return currentValue; }
        public void setCurrentValue(long currentValue) { this.currentValue = currentValue; }
        public String getResetPeriod() { return resetPeriod; }
        public void setResetPeriod(String resetPeriod) { this.resetPeriod = resetPeriod; }
        public boolean isExceeded() { return exceeded; }
        public void setExceeded(boolean exceeded) { this.exceeded = exceeded; }
        public Instant getLastResetAt() { return lastResetAt; }
        public void setLastResetAt(Instant lastResetAt) { this.lastResetAt = lastResetAt; }

        public static QuotaResponse from(TenantQuota quota) {
            QuotaResponse resp = new QuotaResponse();
            resp.setId(quota.getId());
            resp.setQuotaType(quota.getQuotaType() != null ? quota.getQuotaType().name() : null);
            resp.setMaxValue(quota.getMaxValue());
            resp.setCurrentValue(quota.getCurrentValue());
            resp.setResetPeriod(quota.getResetPeriod() != null ? quota.getResetPeriod().name() : null);
            resp.setExceeded(quota.isExceeded());
            resp.setLastResetAt(quota.getLastResetAt());
            return resp;
        }
    }

    private TenantUserDTO() {}
}
