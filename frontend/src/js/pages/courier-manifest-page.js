import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('courier-manifest-page');

/**
 * Twsela CMS - Courier Manifest Page Initialization
 * Handles page-specific initialization and legacy functions
 * Follows DRY principle with clean separation of concerns
 */

// Initialize manifest page
document.addEventListener('DOMContentLoaded', function() {
    initializeManifestPage();
});

function initializeManifestPage() {
    // Load manifest data
    loadManifestData();
    
    // Setup event listeners
    setupEventListeners();
}

async function loadManifestData() {
    try {
        const manifestData = await window.apiService.getCourierManifest();
        updateManifestDisplay(manifestData);
    } catch (error) { log.error('Unhandled error:', error); }
}

function updateManifestDisplay(manifestData) {
    // Update manifest information
    const manifestIdElement = document.getElementById('manifestId');
    if (manifestIdElement) {
        manifestIdElement.textContent = manifestData.id || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯';
    }

    const manifestDateElement = document.getElementById('manifestDate');
    if (manifestDateElement) {
        manifestDateElement.textContent = SharedDataUtils.formatDate(manifestData.createdAt);
    }

    const totalShipmentsElement = document.getElementById('totalShipments');
    if (totalShipmentsElement) {
        totalShipmentsElement.textContent = manifestData.shipments?.length || 0;
    }

    // Update shipments table
    updateShipmentsTable(manifestData.shipments || []);
}

function updateShipmentsTable(shipments) {
    const tbody = document.querySelector('#shipmentsTable tbody');
    if (!tbody) return;

    tbody.innerHTML = '';
    
    shipments.forEach(shipment => {
        const row = createShipmentRow(shipment);
        tbody.appendChild(row);
    });
}

function createShipmentRow(shipment) {
    const row = document.createElement('tr');
    row.innerHTML = GlobalUIHandler.createTableRow(shipment, 'shipment');
    return row;
}

// Using unified DataUtils functions directly

function setupEventListeners() {
    // Setup event listeners for action buttons
    document.addEventListener('click', (e) => {
        if (e.target.closest('.action-btn')) {
            const button = e.target.closest('.action-btn');
            const shipmentId = parseInt(button.dataset.shipmentId);
            const action = button.classList.contains('view') ? 'view' : 'update';
            
            switch (action) {
                case 'view':
                    viewShipment(shipmentId);
                    break;
                case 'update':
                    updateShipmentStatus(shipmentId);
                    break;
            }
        }
    });
    
}

function viewShipment(shipmentId) {
    // Navigate to shipment details or show modal
    const shipment = document.querySelector(`[data-shipment-id="${shipmentId}"]`)?.closest('tr');
    if (!shipment) return;

    const cells = shipment.querySelectorAll('td');
    const trackingNumber = cells[0]?.textContent || '';

    Swal.fire({
        title: '\u062a\u0641\u0627\u0635\u064a\u0644 \u0627\u0644\u0634\u062d\u0646\u0629',
        html: `
            <div class="text-start" dir="rtl">
                <p><strong>\u0631\u0642\u0645 \u0627\u0644\u062a\u062a\u0628\u0639:</strong> ${escapeHtml(trackingNumber)}</p>
                <p><strong>\u0627\u0644\u0645\u0633\u062a\u0644\u0645:</strong> ${escapeHtml(cells[1]?.textContent || '')}</p>
                <p><strong>\u0627\u0644\u0639\u0646\u0648\u0627\u0646:</strong> ${escapeHtml(cells[2]?.textContent || '')}</p>
                <p><strong>\u0627\u0644\u062d\u0627\u0644\u0629:</strong> ${cells[3]?.innerHTML || ''}</p>
            </div>
        `,
        confirmButtonText: '\u0625\u063a\u0644\u0627\u0642',
        width: '500px'
    });
}

async function updateShipmentStatus(shipmentId) {
    const { value: newStatus } = await Swal.fire({
        title: '\u062a\u062d\u062f\u064a\u062b \u062d\u0627\u0644\u0629 \u0627\u0644\u0634\u062d\u0646\u0629',
        input: 'select',
        inputOptions: {
            'IN_TRANSIT': '\u0641\u064a \u0627\u0644\u0637\u0631\u064a\u0642',
            'OUT_FOR_DELIVERY': '\u062e\u0627\u0631\u062c \u0644\u0644\u062a\u0648\u0635\u064a\u0644',
            'DELIVERED': '\u062a\u0645 \u0627\u0644\u062a\u0633\u0644\u064a\u0645',
            'FAILED_DELIVERY': '\u0641\u0634\u0644 \u0627\u0644\u062a\u0648\u0635\u064a\u0644',
            'RETURNED': '\u0645\u0631\u062a\u062c\u0639'
        },
        inputPlaceholder: '\u0627\u062e\u062a\u0631 \u0627\u0644\u062d\u0627\u0644\u0629 \u0627\u0644\u062c\u062f\u064a\u062f\u0629',
        showCancelButton: true,
        confirmButtonText: '\u062a\u062d\u062f\u064a\u062b',
        cancelButtonText: '\u0625\u0644\u063a\u0627\u0621',
        inputValidator: (value) => {
            if (!value) return '\u064a\u0631\u062c\u0649 \u0627\u062e\u062a\u064a\u0627\u0631 \u0627\u0644\u062d\u0627\u0644\u0629';
        }
    });

    if (!newStatus) return;

    try {
        GlobalUIHandler.showLoading();
        await window.apiService.updateShipmentStatus(shipmentId, newStatus);
        NotificationService.success('\u062a\u0645 \u062a\u062d\u062f\u064a\u062b \u062d\u0627\u0644\u0629 \u0627\u0644\u0634\u062d\u0646\u0629');
        // Reload manifest data
        await loadManifestData();
    } catch (error) {
        log.error('Failed to update shipment status:', error);
        NotificationService.error('\u0641\u0634\u0644 \u062a\u062d\u062f\u064a\u062b \u0627\u0644\u062d\u0627\u0644\u0629');
    } finally {
        GlobalUIHandler.hideLoading();
    }
}

function showNotification(message, type) {
    NotificationService.show(message, type);
}
