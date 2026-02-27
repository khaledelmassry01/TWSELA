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
        const manifestData = await apiService.getCourierManifest();
        updateManifestDisplay(manifestData);
    } catch (error) { console.error('Unhandled error:', error); }
}

function updateManifestDisplay(manifestData) {
    // Update manifest information
    const manifestIdElement = document.getElementById('manifestId');
    if (manifestIdElement) {
        manifestIdElement.textContent = manifestData.id || 'غير محدد';
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
    // Implementation for viewing shipment details
}

function updateShipmentStatus(shipmentId) {
    // Implementation for updating shipment status
}

function showNotification(message, type) {
    NotificationService.show(message, type);
}
