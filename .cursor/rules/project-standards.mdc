---
alwaysApply: true
---
## **2. Arabic Language Support (MANDATORY)**

### **Frontend Requirements:**
* All user-facing text must support Arabic (RTL) layout
* Use `dir="rtl"` attribute for Arabic content
* Include Arabic fonts: `NotoSansArabic-Regular.ttf` and `NotoSansArabic-Bold.ttf`
* Ensure proper text alignment and spacing for Arabic text
* All form labels, buttons, and messages must be in Arabic
* Date and number formatting must follow Arabic locale standards

### **Backend Requirements:**
* All API responses must include Arabic error messages
* Database content must support UTF-8 encoding for Arabic text
* Validation messages must be in Arabic
* Log messages should include Arabic context when applicable

---

## **3. Security Best Practices (CRITICAL)**

### **Authentication & Authorization:**
* All API endpoints must be protected with proper JWT authentication
* Role-based access control must be implemented for all user types (Owner, Merchant, Courier, Warehouse)
* Password hashing must use BCrypt with proper salt rounds
* Session management must be secure with proper expiration

### **Input Validation:**
* All user inputs must be validated on both frontend and backend
* SQL injection prevention through parameterized queries
* XSS prevention through proper output encoding
* CSRF protection for all state-changing operations

### **Data Protection:**
* Sensitive data must be encrypted at rest
* API keys and secrets must be stored securely (not in code)
* Personal information must be handled according to privacy regulations
* Audit logging for all sensitive operations

---

## **4. Database Consistency (MANDATORY)**

### **Entity Relationships:**
* All foreign key relationships must be properly defined
* Cascade operations must be explicitly defined
* Database constraints must be enforced at the application level
* All entities must have proper validation annotations

### **Data Integrity:**
* All monetary values must use `BigDecimal` for precision
* Date/time fields must use proper timezone handling
* Enum values must be consistent across the application
* Soft deletes must be implemented where appropriate

### **Migration Management:**
* All database changes must be versioned
* Migration scripts must be reversible
* Test data cleanup must be automated
* Database schema must be documented

---

## **5. API Design Standards (MANDATORY)**

### **RESTful Design:**
* All endpoints must follow REST conventions
* HTTP status codes must be used correctly
* Request/response formats must be consistent
* API versioning must be implemented

### **Response Format:**
* All API responses must follow the standard format:
```json
{
  "success": boolean,
  "message": string,
  "data": object,
  "errors": array
}
```

### **Error Handling:**
* All errors must be properly caught and handled
* Error messages must be user-friendly and in Arabic
* Stack traces must not be exposed in production
* Proper HTTP status codes must be returned

---

## **6. Frontend Architecture (MANDATORY)**

### **File Organization:**
* All HTML files must be in the root `frontend/` directory
* CSS files must be in `frontend/src/css/`
* JavaScript files must be in `frontend/src/js/`
* Page-specific JS files must be in `frontend/src/js/pages/`

### **Styling Standards:**
* Use Tailwind CSS for utility classes
* Custom CSS must be in dedicated files
* Responsive design must be implemented
* Dark mode support must be considered

### **JavaScript Standards:**
* Use ES6+ features
* Modular JavaScript with proper imports/exports
* Event handling must be properly managed
* DOM manipulation must be efficient

---

## **7. Performance Optimization (MANDATORY)**

### **Frontend Performance:**
* Images must be optimized and compressed
* CSS and JS files must be minified in production
* Lazy loading must be implemented for large content
* Caching strategies must be implemented

### **Backend Performance:**
* Database queries must be optimized
* Proper indexing must be implemented
* Connection pooling must be configured
* Caching must be implemented for frequently accessed data

---

## **8. Error Handling & Logging (MANDATORY)**

### **Error Handling:**
* All exceptions must be caught and handled gracefully
* User-friendly error messages must be displayed
* Error logging must be implemented
* Error recovery mechanisms must be in place

### **Logging Standards:**
* All important operations must be logged
* Log levels must be used appropriately
* Sensitive information must not be logged
* Log rotation must be configured

---

## **9. Testing Requirements (MANDATORY)**

### **Code Quality:**
* All new code must be tested
* Unit tests must be written for business logic
* Integration tests must be written for API endpoints
* Frontend functionality must be tested

### **Documentation:**
* All public APIs must be documented
* Code comments must be clear and helpful
* README files must be updated
* Change logs must be maintained

---

## **10. Deployment & Environment (MANDATORY)**

### **Environment Configuration:**
* All environment variables must be properly configured
* Different configurations for dev/staging/production
* Secrets must be managed securely
* Database connections must be properly configured

### **Build Process:**
* Automated build and deployment
* Proper versioning and tagging
* Rollback procedures must be in place
* Health checks must be implemented

---

## **Enforcement**

These rules are **MANDATORY** and must be followed for all future modifications to the Twsela project. Any violation of these rules must be corrected immediately. The rules ensure:

1. **Maintainability**: Clean, organized code that's easy to understand and modify
2. **Security**: Protection against common vulnerabilities and threats
3. **Performance**: Optimized code that provides good user experience
4. **Consistency**: Uniform approach across the entire project
5. **Reliability**: Robust error handling and proper testing

**Remember**: These rules are not suggestions - they are requirements that must be enforced for the success and longevity of the Twsela project.