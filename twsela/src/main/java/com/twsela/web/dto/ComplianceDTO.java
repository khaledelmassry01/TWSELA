package com.twsela.web.dto;

/**
 * DTOs متعلقة بالامتثال.
 */
public class ComplianceDTO {

    /**
     * طلب فحص امتثال.
     */
    public static class ComplianceCheckRequest {
        private String category;

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }

    private ComplianceDTO() {}
}
