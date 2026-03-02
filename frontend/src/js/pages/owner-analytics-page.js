/**
 * owner-analytics-page.js — Advanced Analytics Dashboard (Sprint 20)
 * Uses /api/analytics/* endpoints for revenue trends, status distribution,
 * courier ranking, and top merchants.
 */
(function () {
    'use strict';

    var API_BASE = '/api';
    var dateInputs = {};
    var containers = {};

    function init() {
        dateInputs.start = document.getElementById('analyticsStartDate');
        dateInputs.end = document.getElementById('analyticsEndDate');
        containers.revenue = document.getElementById('revenueContainer');
        containers.statusDist = document.getElementById('statusDistContainer');
        containers.courierRank = document.getElementById('courierRankContainer');
        containers.topMerchants = document.getElementById('topMerchantsContainer');

        // Default dates: last 30 days
        var today = new Date();
        var thirtyAgo = new Date(today);
        thirtyAgo.setDate(today.getDate() - 30);

        if (dateInputs.start) dateInputs.start.value = formatDate(thirtyAgo);
        if (dateInputs.end) dateInputs.end.value = formatDate(today);

        var btn = document.getElementById('loadAnalyticsBtn');
        if (btn) btn.addEventListener('click', loadAllAnalytics);

        loadAllAnalytics();
    }

    function formatDate(d) {
        return d.toISOString().split('T')[0];
    }

    function getToken() {
        return localStorage.getItem('authToken') || localStorage.getItem('token') || '';
    }

    function esc(str) {
        if (window.Sanitizer && window.Sanitizer.escapeHtml) return window.Sanitizer.escapeHtml(str);
        if (str == null) return '';
        return String(str).replace(/[&<>"'\/]/g, function (c) {
            return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#x27;', '/': '&#x2F;' }[c];
        });
    }

    function fetchApi(path) {
        return fetch(API_BASE + path, {
            headers: { 'Authorization': 'Bearer ' + getToken(), 'Accept': 'application/json' }
        }).then(function (r) { return r.json(); });
    }

    // ── Load all analytics ──────────────────────────────────────

    function loadAllAnalytics() {
        var start = dateInputs.start ? dateInputs.start.value : '';
        var end = dateInputs.end ? dateInputs.end.value : '';
        if (!start || !end) return;

        var qs = '?startDate=' + start + '&endDate=' + end;

        loadRevenue(qs);
        loadStatusDistribution(qs);
        loadCourierRanking(qs);
        loadTopMerchants(qs);
    }

    // ── Revenue ─────────────────────────────────────────────────

    function loadRevenue(qs) {
        if (!containers.revenue) return;
        containers.revenue.innerHTML = '<div class="text-center py-3"><i class="fas fa-spinner fa-spin"></i></div>';
        fetchApi('/analytics/revenue' + qs).then(function (res) {
            if (!res.success || !res.data) { containers.revenue.innerHTML = '<p class="text-muted">لا توجد بيانات</p>'; return; }
            var d = res.data;
            var html = '<div class="row g-3">';
            html += card('إجمالي الشحنات', d.totalShipments, 'fas fa-box', 'primary');
            html += card('الشحنات المُسلمة', d.deliveredShipments, 'fas fa-check-circle', 'success');
            html += card('الإيرادات', formatMoney(d.totalRevenue), 'fas fa-coins', 'warning');
            html += card('نسبة التسليم', d.deliveryRate.toFixed(1) + '%', 'fas fa-chart-line', 'info');
            html += '</div>';

            if (d.breakdown && d.breakdown.length) {
                html += '<h6 class="mt-4 mb-2">التفصيل الشهري</h6>';
                html += '<div class="table-responsive"><table class="table table-sm table-striped">';
                html += '<thead><tr><th>الفترة</th><th>الإيرادات</th><th>عدد الشحنات</th></tr></thead><tbody>';
                d.breakdown.forEach(function (b) {
                    html += '<tr><td>' + esc(b.period) + '</td><td>' + formatMoney(b.revenue) + '</td><td>' + b.shipmentCount + '</td></tr>';
                });
                html += '</tbody></table></div>';
            }
            containers.revenue.innerHTML = html;
        }).catch(function () { containers.revenue.innerHTML = '<p class="text-danger">خطأ في تحميل الإيرادات</p>'; });
    }

    // ── Status distribution ─────────────────────────────────────

    function loadStatusDistribution(qs) {
        if (!containers.statusDist) return;
        containers.statusDist.innerHTML = '<div class="text-center py-3"><i class="fas fa-spinner fa-spin"></i></div>';
        fetchApi('/analytics/status-distribution' + qs).then(function (res) {
            if (!res.success || !res.data || !res.data.length) { containers.statusDist.innerHTML = '<p class="text-muted">لا توجد بيانات</p>'; return; }
            var html = '<div class="table-responsive"><table class="table table-sm"><thead><tr><th>الحالة</th><th>العدد</th><th>النسبة</th></tr></thead><tbody>';
            res.data.forEach(function (s) {
                html += '<tr><td>' + translateStatus(s.status) + '</td><td>' + s.count + '</td><td>';
                html += '<div class="progress" style="height:20px"><div class="progress-bar" style="width:' + s.percentage + '%">' + s.percentage.toFixed(1) + '%</div></div></td></tr>';
            });
            html += '</tbody></table></div>';
            containers.statusDist.innerHTML = html;
        }).catch(function () { containers.statusDist.innerHTML = '<p class="text-danger">خطأ</p>'; });
    }

    // ── Courier ranking ─────────────────────────────────────────

    function loadCourierRanking(qs) {
        if (!containers.courierRank) return;
        containers.courierRank.innerHTML = '<div class="text-center py-3"><i class="fas fa-spinner fa-spin"></i></div>';
        fetchApi('/analytics/courier-ranking' + qs + '&limit=10').then(function (res) {
            if (!res.success || !res.data || !res.data.length) { containers.courierRank.innerHTML = '<p class="text-muted">لا توجد بيانات</p>'; return; }
            var html = '<div class="table-responsive"><table class="table table-sm table-hover">';
            html += '<thead><tr><th>#</th><th>المندوب</th><th>التسليمات</th><th>نسبة النجاح</th><th>الأرباح</th></tr></thead><tbody>';
            res.data.forEach(function (c, i) {
                html += '<tr><td>' + (i + 1) + '</td><td>' + esc(c.courierName) + '</td><td>' + c.totalDeliveries + '</td>';
                html += '<td>' + c.successRate.toFixed(1) + '%</td><td>' + formatMoney(c.totalEarnings) + '</td></tr>';
            });
            html += '</tbody></table></div>';
            containers.courierRank.innerHTML = html;
        }).catch(function () { containers.courierRank.innerHTML = '<p class="text-danger">خطأ</p>'; });
    }

    // ── Top merchants ───────────────────────────────────────────

    function loadTopMerchants(qs) {
        if (!containers.topMerchants) return;
        containers.topMerchants.innerHTML = '<div class="text-center py-3"><i class="fas fa-spinner fa-spin"></i></div>';
        fetchApi('/analytics/top-merchants' + qs + '&limit=10').then(function (res) {
            if (!res.success || !res.data || !res.data.length) { containers.topMerchants.innerHTML = '<p class="text-muted">لا توجد بيانات</p>'; return; }
            var html = '<div class="table-responsive"><table class="table table-sm table-hover">';
            html += '<thead><tr><th>#</th><th>التاجر</th><th>الشحنات</th><th>الإيرادات</th></tr></thead><tbody>';
            res.data.forEach(function (m, i) {
                html += '<tr><td>' + (i + 1) + '</td><td>' + esc(m.merchantName) + '</td><td>' + m.shipmentCount + '</td>';
                html += '<td>' + formatMoney(m.revenue) + '</td></tr>';
            });
            html += '</tbody></table></div>';
            containers.topMerchants.innerHTML = html;
        }).catch(function () { containers.topMerchants.innerHTML = '<p class="text-danger">خطأ</p>'; });
    }

    // ── Helpers ──────────────────────────────────────────────────

    function card(title, value, icon, color) {
        return '<div class="col-md-3 col-6"><div class="card border-' + color + ' h-100"><div class="card-body text-center">' +
            '<i class="' + icon + ' fa-2x text-' + color + ' mb-2"></i>' +
            '<h6 class="card-title text-muted">' + esc(title) + '</h6>' +
            '<h4 class="mb-0">' + esc(String(value)) + '</h4></div></div></div>';
    }

    function formatMoney(val) {
        if (val == null) return '0 ج.م';
        return Number(val).toLocaleString('ar-EG') + ' ج.م';
    }

    var statusMap = {
        'PENDING': 'قيد الانتظار', 'CREATED': 'تم الإنشاء', 'PICKED_UP': 'تم الاستلام',
        'IN_TRANSIT': 'في الطريق', 'DELIVERED': 'تم التسليم', 'FAILED_DELIVERY': 'فشل التسليم',
        'RETURNED': 'مرتجع', 'CANCELLED': 'ملغي'
    };

    function translateStatus(s) { return statusMap[s] || s; }

    // Auto-init
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
