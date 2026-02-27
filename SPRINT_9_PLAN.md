# Sprint 9 — Frontend Overhaul
**المهام: 30 | حزم العمل: 4**

---

## WP-1: API Path Alignment (Frontend ↔ Backend)

### T-9.01-T-9.06: إصلاح مسارات API في api_service.js
- مطابقة كل endpoint مع Backend الفعلي
- إزالة endpoints بدون backend (stub graceful)
- Shipment paths: `/api/shipments/list` consistency

### T-9.07-T-9.10: apiService method validation
- التأكد أن كل page script يستدعي methods موجودة
- إضافة methods مفقودة في api_service.js

## WP-2: Error Handling & Loading States

### T-9.11-T-9.14: Unified error handling
- Error toast component مشترك
- Network error overlay
- 401 redirect → login
- 403 forbidden page

### T-9.15-T-9.18: Loading states
- Skeleton loading لكل dashboard
- Button loading states
- Table loading indicators
- Empty state components

## WP-3: Page Quality

### T-9.19: owner-dashboard — real data integration
### T-9.20: courier-dashboard — real data integration
### T-9.21: merchant-dashboard — real data integration
### T-9.22: warehouse-dashboard — real data integration
### T-9.23: owner-reports pages — real chart data
### T-9.24: owner-payouts — real financial data
### T-9.25: owner-pricing — CRUD integration

## WP-4: Accessibility & Polish

### T-9.26: RTL consistency audit
- كل الصفحات dir="rtl" + font-family consistency

### T-9.27: aria attributes
- Labels, roles, live regions

### T-9.28: Keyboard navigation
- Focus management, tab order

### T-9.29: Mobile responsiveness audit
### T-9.30: Console error cleanup

---

## معايير القبول
- [ ] صفر TypeError/ReferenceError في console
- [ ] كل استدعاء API يتعامل مع الأخطاء
- [ ] Loading state لكل عملية async
- [ ] RTL صحيح في كل الصفحات
- [ ] BUILD SUCCESS
