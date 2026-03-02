/**
 * tracking-page.js
 * Public shipment tracking page — no login required.
 * Reads tracking number from URL param or user input, displays status timeline.
 */
(function () {
    'use strict';

    const API_BASE = window.API_BASE_URL || '';
    const trackingInput = document.getElementById('trackingInput');
    const trackBtn = document.getElementById('trackBtn');
    const errorMsg = document.getElementById('errorMsg');
    const resultsSection = document.getElementById('resultsSection');

    // Check URL for tracking number
    const urlParams = new URLSearchParams(window.location.search);
    const urlTracking = urlParams.get('tn') || urlParams.get('trackingNumber');
    if (urlTracking) {
        trackingInput.value = urlTracking;
        fetchTracking(urlTracking);
    }

    // Button click
    trackBtn.addEventListener('click', function () {
        const tn = trackingInput.value.trim();
        if (!tn) {
            showError('يرجى إدخال رقم التتبع');
            return;
        }
        fetchTracking(tn);
    });

    // Enter key
    trackingInput.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') {
            trackBtn.click();
        }
    });

    function fetchTracking(trackingNumber) {
        hideError();
        resultsSection.classList.add('d-none');

        fetch(API_BASE + '/api/public/tracking/' + encodeURIComponent(trackingNumber))
            .then(function (res) {
                if (!res.ok) {
                    if (res.status === 404) throw new Error('الشحنة غير موجودة');
                    throw new Error('حدث خطأ في الاتصال');
                }
                return res.json();
            })
            .then(function (json) {
                if (!json.success) {
                    showError(json.message || 'الشحنة غير موجودة');
                    return;
                }
                renderResults(json.data);
            })
            .catch(function (err) {
                showError(err.message || 'حدث خطأ غير متوقع');
            });
    }

    function renderResults(data) {
        resultsSection.classList.remove('d-none');

        document.getElementById('resultTrackingNumber').textContent = data.trackingNumber || '';
        document.getElementById('resultStatus').textContent = translateStatus(data.currentStatus);

        // ETA
        var etaSection = document.getElementById('etaSection');
        if (data.estimatedMinutesToDelivery) {
            etaSection.classList.remove('d-none');
            document.getElementById('resultETA').textContent = data.estimatedMinutesToDelivery + ' دقيقة';
        } else {
            etaSection.classList.add('d-none');
        }

        // Courier
        var courierSection = document.getElementById('courierSection');
        if (data.courierName) {
            courierSection.classList.remove('d-none');
            document.getElementById('resultCourierName').textContent = data.courierName;
        } else {
            courierSection.classList.add('d-none');
        }

        // Timeline
        var container = document.getElementById('timelineContainer');
        container.innerHTML = '';
        if (data.statusTimeline && data.statusTimeline.length > 0) {
            data.statusTimeline.forEach(function (entry, index) {
                var isLast = index === data.statusTimeline.length - 1;
                var div = document.createElement('div');
                div.className = 'timeline-entry' + (isLast ? ' timeline-entry-active' : '');

                var dot = document.createElement('div');
                dot.className = 'timeline-dot';
                div.appendChild(dot);

                var content = document.createElement('div');
                content.className = 'timeline-content';

                var statusEl = document.createElement('div');
                statusEl.className = 'timeline-status fw-bold';
                statusEl.textContent = translateStatus(entry.status);
                content.appendChild(statusEl);

                if (entry.notes) {
                    var notesEl = document.createElement('div');
                    notesEl.className = 'text-muted small';
                    notesEl.textContent = entry.notes;
                    content.appendChild(notesEl);
                }

                if (entry.timestamp) {
                    var timeEl = document.createElement('div');
                    timeEl.className = 'text-muted small';
                    timeEl.textContent = new Date(entry.timestamp).toLocaleString('ar-EG');
                    content.appendChild(timeEl);
                }

                div.appendChild(content);
                container.appendChild(div);
            });
        }
    }

    function translateStatus(status) {
        var map = {
            'PENDING': 'في الانتظار',
            'PENDING_APPROVAL': 'بانتظار الموافقة',
            'APPROVED': 'تمت الموافقة',
            'PICKED_UP': 'تم الاستلام',
            'RECEIVED_AT_HUB': 'وصلت المخزن',
            'READY_FOR_DISPATCH': 'جاهزة للتوزيع',
            'ASSIGNED_TO_COURIER': 'مُعيّنة لمندوب',
            'IN_TRANSIT': 'في الطريق',
            'OUT_FOR_DELIVERY': 'خرجت للتوصيل',
            'DELIVERED': 'تم التسليم',
            'PARTIALLY_DELIVERED': 'تسليم جزئي',
            'FAILED_DELIVERY': 'فشل التسليم',
            'FAILED_ATTEMPT': 'محاولة فاشلة',
            'POSTPONED': 'مؤجلة',
            'PENDING_UPDATE': 'بانتظار التحديث',
            'PENDING_RETURN': 'بانتظار الإرجاع',
            'RETURNED_TO_HUB': 'عادت للمخزن',
            'RETURNED_TO_ORIGIN': 'عادت للمصدر',
            'CANCELLED': 'ملغاة',
            'ON_HOLD': 'معلّقة',
            'RESCHEDULED': 'أعيدت جدولتها'
        };
        return map[status] || status || '';
    }

    function showError(msg) {
        errorMsg.textContent = msg;
        errorMsg.classList.remove('d-none');
    }

    function hideError() {
        errorMsg.classList.add('d-none');
    }
})();
