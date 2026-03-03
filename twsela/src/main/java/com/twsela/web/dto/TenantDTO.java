package com.twsela.web.dto;

import com.twsela.domain.Tenant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * DTOs الخاصة بالمستأجر.
 */
public class TenantDTO {

    /**
     * طلب إنشاء مستأجر جديد.
     */
    public static class CreateTenantRequest {
        @NotBlank(message = "اسم المستأجر مطلوب")
        @Size(max = 100)
        private String name;

        @NotBlank(message = "الرابط المختصر مطلوب")
        @Size(max = 50)
        private String slug;

        @NotBlank(message = "اسم المسؤول مطلوب")
        private String contactName;

        @NotBlank(message = "رقم هاتف المسؤول مطلوب")
        private String contactPhone;

        private String contactEmail;
        private Tenant.TenantPlan plan = Tenant.TenantPlan.FREE;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getContactName() { return contactName; }
        public void setContactName(String contactName) { this.contactName = contactName; }
        public String getContactPhone() { return contactPhone; }
        public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
        public String getContactEmail() { return contactEmail; }
        public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
        public Tenant.TenantPlan getPlan() { return plan; }
        public void setPlan(Tenant.TenantPlan plan) { this.plan = plan; }
    }

    /**
     * طلب تحديث مستأجر.
     */
    public static class UpdateTenantRequest {
        private String name;
        private String contactName;
        private String contactPhone;
        private String contactEmail;
        private String domain;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getContactName() { return contactName; }
        public void setContactName(String contactName) { this.contactName = contactName; }
        public String getContactPhone() { return contactPhone; }
        public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
        public String getContactEmail() { return contactEmail; }
        public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }
    }

    /**
     * استجابة بيانات المستأجر.
     */
    public static class TenantResponse {
        private Long id;
        private String tenantId;
        private String name;
        private String slug;
        private String domain;
        private String status;
        private String plan;
        private String contactName;
        private String contactPhone;
        private String contactEmail;
        private Instant createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getPlan() { return plan; }
        public void setPlan(String plan) { this.plan = plan; }
        public String getContactName() { return contactName; }
        public void setContactName(String contactName) { this.contactName = contactName; }
        public String getContactPhone() { return contactPhone; }
        public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
        public String getContactEmail() { return contactEmail; }
        public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
        public Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

        public static TenantResponse from(Tenant tenant) {
            TenantResponse resp = new TenantResponse();
            resp.setId(tenant.getId());
            resp.setTenantId(tenant.getTenantId());
            resp.setName(tenant.getName());
            resp.setSlug(tenant.getSlug());
            resp.setDomain(tenant.getDomain());
            resp.setStatus(tenant.getStatus() != null ? tenant.getStatus().name() : null);
            resp.setPlan(tenant.getPlan() != null ? tenant.getPlan().name() : null);
            resp.setContactName(tenant.getContactName());
            resp.setContactPhone(tenant.getContactPhone());
            resp.setContactEmail(tenant.getContactEmail());
            resp.setCreatedAt(tenant.getCreatedAt());
            return resp;
        }
    }

    /**
     * ملخص المستأجر.
     */
    public static class TenantSummaryResponse {
        private Long id;
        private String name;
        private String slug;
        private String status;
        private String plan;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getPlan() { return plan; }
        public void setPlan(String plan) { this.plan = plan; }

        public static TenantSummaryResponse from(Tenant tenant) {
            TenantSummaryResponse resp = new TenantSummaryResponse();
            resp.setId(tenant.getId());
            resp.setName(tenant.getName());
            resp.setSlug(tenant.getSlug());
            resp.setStatus(tenant.getStatus() != null ? tenant.getStatus().name() : null);
            resp.setPlan(tenant.getPlan() != null ? tenant.getPlan().name() : null);
            return resp;
        }
    }

    private TenantDTO() {}
}
