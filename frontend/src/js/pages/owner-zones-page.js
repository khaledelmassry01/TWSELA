import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('owner-zones-page');

/**
 * Twsela CMS - Owner Zones Page Initialization
 * Handles page-specific initialization and legacy functions
 * Follows DRY principle with clean separation of concerns
 */

let currentZoneId = null;

// Initialize zones page
document.addEventListener('DOMContentLoaded', function() {
    // Ø§Ù†ØªØ¸Ø§Ø± ØªØ­Ù…ÙŠÙ„ app.js Ø£ÙˆÙ„Ø§Ù‹ Ù„ØªØ¬Ù†Ø¨ Ø§Ù„ØªØ¶Ø§Ø±Ø¨
    waitForAppInitialization();
});

async function waitForAppInitialization() {
    let attempts = 0;
    const maxAttempts = 50; // 5 seconds max
    
    while (attempts < maxAttempts) {
        // ÙØ­Øµ ÙˆØ¬ÙˆØ¯ app.js
        if (window.twselaApp && window.twselaApp.isInitialized) {
            log.debug('âœ… App.js is initialized, proceeding with zones page');
            await initializeZonesPage();
            return;
        }
        
        // ÙØ­Øµ ÙˆØ¬ÙˆØ¯ Ø§Ù„Ø®Ø¯Ù…Ø§Øª Ø§Ù„Ù…Ø·Ù„ÙˆØ¨Ø©
        if (window.authService && window.apiService) {
            log.debug('âœ… Services are available, proceeding with zones page');
            await initializeZonesPage();
            return;
        }
        
        await new Promise(resolve => setTimeout(resolve, 100));
        attempts++;
    }
    
    log.warn('âš ï¸ App.js not initialized after timeout, proceeding anyway');
    await initializeZonesPage();
}

async function initializeZonesPage() {
    try {
        // ÙØ­Øµ Ø§Ù„Ù…ØµØ§Ø¯Ù‚Ø© Ø£ÙˆÙ„Ø§Ù‹
        if (!await checkAuthentication()) {
            return;
        }
        
        // Load zones data
        await loadZonesData();
        
        // Initialize charts
        initializeCharts();
        
        // Setup event listeners
        setupEventListeners();
        
        log.debug('âœ… Zones page initialized successfully');
    } catch (error) {
        log.error('âŒ Error initializing zones page:', error);
        showNotification('Ø®Ø·Ø£ ÙÙŠ ØªØ­Ù…ÙŠÙ„ ØµÙØ­Ø© Ø§Ù„Ù…Ù†Ø§Ø·Ù‚', 'error');
    }
}

/**
 * ÙØ­Øµ Ø§Ù„Ù…ØµØ§Ø¯Ù‚Ø© Ù‚Ø¨Ù„ ØªØ­Ù…ÙŠÙ„ Ø§Ù„ØµÙØ­Ø©
 */
async function checkAuthentication() {
    try {
        // ÙØ­Øµ ÙˆØ¬ÙˆØ¯ Ø®Ø¯Ù…Ø© Ø§Ù„Ù…ØµØ§Ø¯Ù‚Ø©
        if (!window.authService) {
            log.error('âŒ AuthService not available');
            window.location.href = '/login.html';
            return false;
        }
        
        // ÙØ­Øµ Ø§Ù„ØªÙˆÙƒÙ† Ø§Ù„Ù…Ø­Ù„ÙŠ Ø£ÙˆÙ„Ø§Ù‹ Ù„ØªØ¬Ù†Ø¨ Ø§Ø³ØªØ¯Ø¹Ø§Ø¡ auth/me ØºÙŠØ± Ø§Ù„Ø¶Ø±ÙˆØ±ÙŠ
        const token = window.authService.getToken();
        if (!token) {
            log.warn('âš ï¸ No authentication token found');
            window.location.href = '/login.html';
            return false;
        }
        
        // ÙØ­Øµ Ø¨ÙŠØ§Ù†Ø§Øª Ø§Ù„Ù…Ø³ØªØ®Ø¯Ù… Ø§Ù„Ù…Ø­Ù„ÙŠØ© Ø£ÙˆÙ„Ø§Ù‹
        const user = window.authService.getCurrentUser();
        if (user && user.role) {
            // ÙØ­Øµ Ø§Ù„ØµÙ„Ø§Ø­ÙŠØ§Øª Ù…Ù† Ø§Ù„Ø¨ÙŠØ§Ù†Ø§Øª Ø§Ù„Ù…Ø­Ù„ÙŠØ©
            if (!['OWNER', 'ADMIN'].includes(user.role)) {
                log.warn('âš ï¸ User does not have permission to access zones page');
                window.location.href = '/login.html';
                return false;
            }
            log.debug('âœ… Authentication verified from local data');
            return true;
        }
        
        // ÙØ­Øµ Ø¥Ø°Ø§ ÙƒØ§Ù† app.js Ù‚Ø¯ Ù‚Ø§Ù… Ø¨ÙØ­Øµ Ø§Ù„Ù…ØµØ§Ø¯Ù‚Ø© Ø¨Ø§Ù„ÙØ¹Ù„
        if (window.twselaApp && window.twselaApp.isInitialized) {
            log.debug('âœ… App.js has already verified authentication');
            return true;
        }
        
        // ÙØ­Øµ Ø¥Ø°Ø§ ÙƒØ§Ù† Ù‡Ù†Ø§Ùƒ ÙØ­Øµ Ù…ØµØ§Ø¯Ù‚Ø© Ø¬Ø§Ø±ÙŠ Ø¨Ø§Ù„ÙØ¹Ù„
        if (window.authCheckInProgress) {
            log.debug('â³ Auth check already in progress, waiting...');
            // Ø§Ù†ØªØ¸Ø§Ø± Ø§Ù†ØªÙ‡Ø§Ø¡ Ø§Ù„ÙØ­Øµ Ø§Ù„Ø­Ø§Ù„ÙŠ
            let attempts = 0;
            while (window.authCheckInProgress && attempts < 50) {
                await new Promise(resolve => setTimeout(resolve, 100));
                attempts++;
            }
            
            // ÙØ­Øµ Ø§Ù„Ù†ØªÙŠØ¬Ø© Ø¨Ø¹Ø¯ Ø§Ù„Ø§Ù†ØªØ¸Ø§Ø±
            const finalUser = window.authService.getCurrentUser();
            if (finalUser && finalUser.role && ['OWNER', 'ADMIN'].includes(finalUser.role)) {
                log.debug('âœ… Authentication verified after waiting');
                return true;
            }
        }
        
        // ØªØ¹ÙŠÙŠÙ† Ø¹Ù„Ø§Ù…Ø© Ø£Ù† ÙØ­Øµ Ø§Ù„Ù…ØµØ§Ø¯Ù‚Ø© Ø¬Ø§Ø±ÙŠ
        window.authCheckInProgress = true;
        
        try {
            // ÙÙ‚Ø· Ø¥Ø°Ø§ Ù„Ù… ØªÙƒÙ† Ø§Ù„Ø¨ÙŠØ§Ù†Ø§Øª Ø§Ù„Ù…Ø­Ù„ÙŠØ© Ù…ØªØ§Ø­Ø©ØŒ Ø§Ø³ØªØ¯Ø¹Ø§Ø¡ auth/me
            log.debug('ðŸ”„ Verifying authentication with server...');
            const isValid = await window.authService.checkAuthStatus();
            if (!isValid) {
                log.warn('âš ï¸ User not authenticated, redirecting to login');
                window.location.href = '/login.html';
                return false;
            }
            
            // ÙØ­Øµ Ø§Ù„ØµÙ„Ø§Ø­ÙŠØ§Øª Ø¨Ø¹Ø¯ Ø§Ù„ØªØ­Ù‚Ù‚ Ù…Ù† Ø§Ù„Ø®Ø§Ø¯Ù…
            const updatedUser = window.authService.getCurrentUser();
            if (!updatedUser || !['OWNER', 'ADMIN'].includes(updatedUser.role)) {
                log.warn('âš ï¸ User does not have permission to access zones page');
                window.location.href = '/login.html';
                return false;
            }
            
            log.debug('âœ… Authentication verified successfully');
            return true;
        } finally {
            // Ø¥Ø²Ø§Ù„Ø© Ø¹Ù„Ø§Ù…Ø© ÙØ­Øµ Ø§Ù„Ù…ØµØ§Ø¯Ù‚Ø©
            window.authCheckInProgress = false;
        }
    } catch (error) {
        window.authCheckInProgress = false;
        log.error('âŒ Authentication check failed:', error);
        window.location.href = '/login.html';
        return false;
    }
}

async function loadZonesData() {
    try {
        // ÙØ­Øµ ÙˆØ¬ÙˆØ¯ Ø®Ø¯Ù…Ø© API
        if (!window.apiService) {
            throw new Error('API service not available');
        }
        
        // ÙØ­Øµ Ø§Ù„Ù…ØµØ§Ø¯Ù‚Ø© Ù…Ø±Ø© Ø£Ø®Ø±Ù‰ Ù‚Ø¨Ù„ Ø§Ù„Ø·Ù„Ø¨
        if (!window.authService || !window.authService.isAuthenticated()) {
            throw new Error('User not authenticated');
        }
        
        log.debug('ðŸ”„ Loading zones data...');
        const response = await window.apiService.getZones();
        
        if (response.success) {
            updateZonesTable(response.data || []);
            log.debug('âœ… Zones data loaded successfully');
        } else {
            throw new Error(response.message || 'Failed to load zones data');
        }
    } catch (error) {
        log.error('âŒ Error loading zones data:', error);
        
        // Ø¥Ø°Ø§ ÙƒØ§Ù† Ø§Ù„Ø®Ø·Ø£ Ù…ØªØ¹Ù„Ù‚ Ø¨Ø§Ù„Ù…ØµØ§Ø¯Ù‚Ø©ØŒ ØªÙˆØ¬ÙŠÙ‡ Ù„ØµÙØ­Ø© ØªØ³Ø¬ÙŠÙ„ Ø§Ù„Ø¯Ø®ÙˆÙ„
        if (error.message.includes('authentication') || 
            error.message.includes('401') || 
            error.message.includes('not authenticated')) {
            log.warn('âš ï¸ Authentication error, redirecting to login');
            window.location.href = '/login.html';
            return;
        }
        
        // Ø¹Ø±Ø¶ Ø±Ø³Ø§Ù„Ø© Ø®Ø·Ø£ Ù„Ù„Ù…Ø³ØªØ®Ø¯Ù…
        showNotification('Ø®Ø·Ø£ ÙÙŠ ØªØ­Ù…ÙŠÙ„ Ø¨ÙŠØ§Ù†Ø§Øª Ø§Ù„Ù…Ù†Ø§Ø·Ù‚: ' + error.message, 'error');
        
        // Ø¹Ø±Ø¶ Ø¬Ø¯ÙˆÙ„ ÙØ§Ø±Øº Ù…Ø¹ Ø±Ø³Ø§Ù„Ø© Ø®Ø·Ø£
        updateZonesTable([]);
    }
}

function updateZonesTable(zones) {
    const tbody = document.querySelector('#zonesTable tbody');
    if (!tbody) {
        log.error('âŒ Zones table body not found');
        return;
    }

    tbody.innerHTML = '';
    
    if (!zones || zones.length === 0) {
        // Ø¹Ø±Ø¶ Ø±Ø³Ø§Ù„Ø© "Ù„Ø§ ØªÙˆØ¬Ø¯ Ø¨ÙŠØ§Ù†Ø§Øª"
        const emptyRow = document.createElement('tr');
        emptyRow.innerHTML = `
            <td colspan="5" class="text-center py-4">
                <div class="text-muted">
                    <i class="fas fa-info-circle me-2"></i>
                    Ù„Ø§ ØªÙˆØ¬Ø¯ Ù…Ù†Ø§Ø·Ù‚ Ù…ØªØ§Ø­Ø©
                </div>
            </td>
        `;
        tbody.appendChild(emptyRow);
        return;
    }
    
    zones.forEach(zone => {
        const row = createZoneRow(zone);
        tbody.appendChild(row);
    });
    
    log.debug(`âœ… Updated zones table with ${zones.length} zones`);
}

function createZoneRow(zone) {
    const row = document.createElement('tr');
    if (window.GlobalUIHandler) {
        row.innerHTML = window.GlobalUIHandler.createTableRow(zone, 'zone');
    } else {
        // Fallback if GlobalUIHandler is not available
        row.innerHTML = `
            <td>${escapeHtml(zone.name || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯')}</td>
            <td>${escapeHtml(zone.status || 'ØºÙŠØ± Ù…Ø­Ø¯Ø¯')}</td>
            <td>${zone.deliveryFee || '0'} Ø¬Ù†ÙŠÙ‡</td>
            <td>${zone.couriers || '0'}</td>
            <td>
                <button class="btn btn-sm btn-primary" onclick="editZone(${zone.id})">ØªØ¹Ø¯ÙŠÙ„</button>
                <button class="btn btn-sm btn-danger" onclick="deleteZone(${zone.id})">Ø­Ø°Ù</button>
            </td>
        `;
    }
    return row;
}

// Using unified DataUtils functions directly

function editZone(zoneId) {
    try {
        if (!zoneId) {
            throw new Error('Zone ID is required');
        }
        
        currentZoneId = zoneId;
        log.debug('ðŸ”„ Editing zone:', zoneId);
        
        // Use the handler's editZone method
        if (window.ownerZonesHandler) {
            window.ownerZonesHandler.editZone(zoneId);
        } else {
            // Fallback implementation
            const modal = document.getElementById('editZoneModal');
            if (modal) {
                const bootstrapModal = new bootstrap.Modal(modal);
                bootstrapModal.show();
            } else {
                throw new Error('Edit modal not found');
            }
        }
    } catch (error) {
        log.error('âŒ Error editing zone:', error);
        showNotification('Ø®Ø·Ø£ ÙÙŠ ØªØ­Ù…ÙŠÙ„ Ø¨ÙŠØ§Ù†Ø§Øª Ø§Ù„Ù…Ù†Ø·Ù‚Ø© Ù„Ù„ØªØ¹Ø¯ÙŠÙ„', 'error');
    }
}

function viewZone(zoneId) {
    try {
        if (!zoneId) {
            throw new Error('Zone ID is required');
        }
        
        log.debug('ðŸ”„ Viewing zone:', zoneId);
        
        // Use the handler's viewZone method
        if (window.ownerZonesHandler) {
            window.ownerZonesHandler.viewZone(zoneId);
        } else {
            // Fallback implementation
            showNotification('Ø¹Ø±Ø¶ ØªÙØ§ØµÙŠÙ„ Ø§Ù„Ù…Ù†Ø·Ù‚Ø© ØºÙŠØ± Ù…ØªØ§Ø­ Ø­Ø§Ù„ÙŠØ§Ù‹', 'info');
        }
    } catch (error) {
        log.error('âŒ Error viewing zone:', error);
        showNotification('Ø®Ø·Ø£ ÙÙŠ Ø¹Ø±Ø¶ ØªÙØ§ØµÙŠÙ„ Ø§Ù„Ù…Ù†Ø·Ù‚Ø©', 'error');
    }
}

function deleteZone(zoneId) {
    try {
        if (!zoneId) {
            throw new Error('Zone ID is required');
        }
        
        log.debug('ðŸ”„ Deleting zone:', zoneId);
        
        // Use the handler's deleteZone method
        if (window.ownerZonesHandler) {
            window.ownerZonesHandler.deleteZone(zoneId);
        } else {
            // Fallback implementation with confirmation
            if (confirm('Ù‡Ù„ Ø£Ù†Øª Ù…ØªØ£ÙƒØ¯ Ù…Ù† Ø­Ø°Ù Ù‡Ø°Ù‡ Ø§Ù„Ù…Ù†Ø·Ù‚Ø©ØŸ Ù„Ø§ ÙŠÙ…ÙƒÙ† Ø§Ù„ØªØ±Ø§Ø¬Ø¹ Ø¹Ù† Ù‡Ø°Ø§ Ø§Ù„Ø¥Ø¬Ø±Ø§Ø¡.')) {
                performDeleteZone(zoneId);
            }
        }
    } catch (error) {
        log.error('âŒ Error deleting zone:', error);
        showNotification('Ø®Ø·Ø£ ÙÙŠ Ø­Ø°Ù Ø§Ù„Ù…Ù†Ø·Ù‚Ø©', 'error');
    }
}

async function performDeleteZone(zoneId) {
    try {
        if (!window.apiService) {
            throw new Error('API service not available');
        }
        
        const response = await window.apiService.deleteZone(zoneId);
        if (response.success) {
            showNotification('ØªÙ… Ø­Ø°Ù Ø§Ù„Ù…Ù†Ø·Ù‚Ø© Ø¨Ù†Ø¬Ø§Ø­', 'success');
            await loadZonesData(); // Ø¥Ø¹Ø§Ø¯Ø© ØªØ­Ù…ÙŠÙ„ Ø§Ù„Ø¨ÙŠØ§Ù†Ø§Øª
        } else {
            throw new Error(response.message || 'Failed to delete zone');
        }
    } catch (error) {
        log.error('âŒ Error performing delete:', error);
        showNotification('Ø®Ø·Ø£ ÙÙŠ Ø­Ø°Ù Ø§Ù„Ù…Ù†Ø·Ù‚Ø©: ' + error.message, 'error');
    }
}

function initializeCharts() {
    // Initialize any charts if needed
}

function filterZones(searchTerm) {
    try {
        const rows = document.querySelectorAll('#zonesTable tbody tr');
        let visibleCount = 0;
        
        if (!searchTerm || searchTerm.trim() === '') {
            // Ø¥Ø¸Ù‡Ø§Ø± Ø¬Ù…ÙŠØ¹ Ø§Ù„ØµÙÙˆÙ Ø¥Ø°Ø§ ÙƒØ§Ù† Ø§Ù„Ø¨Ø­Ø« ÙØ§Ø±ØºØ§Ù‹
            rows.forEach(row => {
                row.style.display = '';
                visibleCount++;
            });
        } else {
            const searchLower = searchTerm.toLowerCase().trim();
            
            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                const matches = text.includes(searchLower);
                row.style.display = matches ? '' : 'none';
                
                if (matches) {
                    visibleCount++;
                }
            });
        }
        
        log.debug(`ðŸ” Filter applied: "${searchTerm}" - ${visibleCount} results`);
        
        // ØªØ­Ø¯ÙŠØ« Ù…Ø¹Ù„ÙˆÙ…Ø§Øª Ø§Ù„ØµÙØ­Ø© Ø¥Ø°Ø§ ÙƒØ§Ù†Øª Ù…ÙˆØ¬ÙˆØ¯Ø©
        const paginationInfo = document.querySelector('.pagination-info');
        if (paginationInfo) {
            if (searchTerm && searchTerm.trim() !== '') {
                paginationInfo.textContent = `Ø¹Ø±Ø¶ ${visibleCount} Ù†ØªÙŠØ¬Ø© Ù„Ù„Ø¨Ø­Ø« Ø¹Ù† "${searchTerm}"`;
            } else {
                paginationInfo.textContent = `Ø¹Ø±Ø¶ Ø¬Ù…ÙŠØ¹ Ø§Ù„Ù†ØªØ§Ø¦Ø¬ (${visibleCount})`;
            }
        }
        
    } catch (error) {
        log.error('âŒ Error filtering zones:', error);
        showNotification('Ø®Ø·Ø£ ÙÙŠ Ø§Ù„Ø¨Ø­Ø«', 'error');
    }
}

function setupEventListeners() {
    try {
        log.debug('ðŸ”„ Setting up event listeners...');
        
        // Setup event listeners for action buttons
        document.addEventListener('click', (e) => {
            try {
                if (e.target.closest('.action-btn')) {
                    const button = e.target.closest('.action-btn');
                    const zoneId = parseInt(button.dataset.zoneId);
                    const action = button.dataset.action;
                    
                    log.debug(`ðŸ”„ Action button clicked: ${action} for zone ${zoneId}`);
                    
                    switch (action) {
                        case 'edit':
                            editZone(zoneId);
                            break;
                        case 'view':
                            viewZone(zoneId);
                            break;
                        case 'delete':
                            deleteZone(zoneId);
                            break;
                        default:
                            log.warn('âš ï¸ Unknown action:', action);
                    }
                }
            } catch (error) {
                log.error('âŒ Error handling action button click:', error);
                showNotification('Ø®Ø·Ø£ ÙÙŠ ØªÙ†ÙÙŠØ° Ø§Ù„Ø¥Ø¬Ø±Ø§Ø¡', 'error');
            }
        });

        // Setup search functionality
        const searchInput = document.querySelector('.search-input');
        if (searchInput) {
            searchInput.addEventListener('input', function(e) {
                try {
                    const searchTerm = e.target.value;
                    log.debug('ðŸ” Searching for:', searchTerm);
                    filterZones(searchTerm);
                } catch (error) {
                    log.error('âŒ Error in search:', error);
                }
            });
        }

        // Setup form submissions
        const addZoneBtn = document.getElementById('addZoneBtn');
        if (addZoneBtn) {
            addZoneBtn.addEventListener('click', function(e) {
                e.preventDefault();
                try {
                    addZone();
                } catch (error) {
                    log.error('âŒ Error adding zone:', error);
                    showNotification('Ø®Ø·Ø£ ÙÙŠ Ø¥Ø¶Ø§ÙØ© Ø§Ù„Ù…Ù†Ø·Ù‚Ø©', 'error');
                }
            });
        }

        const updateZoneBtn = document.getElementById('updateZoneBtn');
        if (updateZoneBtn) {
            updateZoneBtn.addEventListener('click', function(e) {
                e.preventDefault();
                try {
                    updateZone();
                } catch (error) {
                    log.error('âŒ Error updating zone:', error);
                    showNotification('Ø®Ø·Ø£ ÙÙŠ ØªØ­Ø¯ÙŠØ« Ø§Ù„Ù…Ù†Ø·Ù‚Ø©', 'error');
                }
            });
        }
        
        log.debug('âœ… Event listeners setup completed');
    } catch (error) {
        log.error('âŒ Error setting up event listeners:', error);
        showNotification('Ø®Ø·Ø£ ÙÙŠ Ø¥Ø¹Ø¯Ø§Ø¯ Ø§Ù„ØµÙØ­Ø©', 'error');
    }
}

async function addZone() {
    try {
        const form = document.getElementById('addZoneForm');
        if (!form) {
            throw new Error('Add zone form not found');
        }
        
        const formData = new FormData(form);
        const zoneData = {
            name: formData.get('zoneName'),
            code: formData.get('zoneCode'),
            deliveryFee: parseFloat(formData.get('deliveryFee')),
            status: formData.get('status'),
            description: formData.get('description'),
            latitude: parseFloat(formData.get('latitude')) || null,
            longitude: parseFloat(formData.get('longitude')) || null
        };
        
        // Ø§Ù„ØªØ­Ù‚Ù‚ Ù…Ù† ØµØ­Ø© Ø§Ù„Ø¨ÙŠØ§Ù†Ø§Øª
        if (!zoneData.name || !zoneData.code) {
            throw new Error('Ø§Ø³Ù… Ø§Ù„Ù…Ù†Ø·Ù‚Ø© ÙˆØ±Ù…Ø² Ø§Ù„Ù…Ù†Ø·Ù‚Ø© Ù…Ø·Ù„ÙˆØ¨Ø§Ù†');
        }
        
        log.debug('ðŸ”„ Adding new zone:', zoneData);
        
        if (!window.apiService) {
            throw new Error('API service not available');
        }
        
        const response = await window.apiService.createZone(zoneData);
        if (response.success) {
            showNotification('ØªÙ… Ø¥Ø¶Ø§ÙØ© Ø§Ù„Ù…Ù†Ø·Ù‚Ø© Ø¨Ù†Ø¬Ø§Ø­', 'success');
            
            // Ø¥ØºÙ„Ø§Ù‚ Ø§Ù„Ù†Ø§ÙØ°Ø© ÙˆØ¥Ø¹Ø§Ø¯Ø© ØªØ¹ÙŠÙŠÙ† Ø§Ù„Ù†Ù…ÙˆØ°Ø¬
            const modal = bootstrap.Modal.getInstance(document.getElementById('addZoneModal'));
            if (modal) {
                modal.hide();
            }
            form.reset();
            
            // Ø¥Ø¹Ø§Ø¯Ø© ØªØ­Ù…ÙŠÙ„ Ø§Ù„Ø¨ÙŠØ§Ù†Ø§Øª
            await loadZonesData();
        } else {
            throw new Error(response.message || 'Failed to create zone');
        }
    } catch (error) {
        log.error('âŒ Error adding zone:', error);
        showNotification('Ø®Ø·Ø£ ÙÙŠ Ø¥Ø¶Ø§ÙØ© Ø§Ù„Ù…Ù†Ø·Ù‚Ø©: ' + error.message, 'error');
    }
}

async function updateZone() {
    try {
        if (!currentZoneId) {
            throw new Error('No zone selected for update');
        }
        
        const form = document.getElementById('editZoneForm');
        if (!form) {
            throw new Error('Edit zone form not found');
        }
        
        const formData = new FormData(form);
        const zoneData = {
            name: formData.get('zoneName'),
            code: formData.get('zoneCode'),
            deliveryFee: parseFloat(formData.get('deliveryFee')),
            status: formData.get('status'),
            description: formData.get('description'),
            latitude: parseFloat(formData.get('latitude')) || null,
            longitude: parseFloat(formData.get('longitude')) || null
        };
        
        // Ø§Ù„ØªØ­Ù‚Ù‚ Ù…Ù† ØµØ­Ø© Ø§Ù„Ø¨ÙŠØ§Ù†Ø§Øª
        if (!zoneData.name || !zoneData.code) {
            throw new Error('Ø§Ø³Ù… Ø§Ù„Ù…Ù†Ø·Ù‚Ø© ÙˆØ±Ù…Ø² Ø§Ù„Ù…Ù†Ø·Ù‚Ø© Ù…Ø·Ù„ÙˆØ¨Ø§Ù†');
        }
        
        log.debug('ðŸ”„ Updating zone:', currentZoneId, zoneData);
        
        if (!window.apiService) {
            throw new Error('API service not available');
        }
        
        const response = await window.apiService.updateZone(currentZoneId, zoneData);
        if (response.success) {
            showNotification('ØªÙ… ØªØ­Ø¯ÙŠØ« Ø§Ù„Ù…Ù†Ø·Ù‚Ø© Ø¨Ù†Ø¬Ø§Ø­', 'success');
            
            // Ø¥ØºÙ„Ø§Ù‚ Ø§Ù„Ù†Ø§ÙØ°Ø© ÙˆØ¥Ø¹Ø§Ø¯Ø© ØªØ¹ÙŠÙŠÙ† Ø§Ù„Ù†Ù…ÙˆØ°Ø¬
            const modal = bootstrap.Modal.getInstance(document.getElementById('editZoneModal'));
            if (modal) {
                modal.hide();
            }
            form.reset();
            currentZoneId = null;
            
            // Ø¥Ø¹Ø§Ø¯Ø© ØªØ­Ù…ÙŠÙ„ Ø§Ù„Ø¨ÙŠØ§Ù†Ø§Øª
            await loadZonesData();
        } else {
            throw new Error(response.message || 'Failed to update zone');
        }
    } catch (error) {
        log.error('âŒ Error updating zone:', error);
        showNotification('Ø®Ø·Ø£ ÙÙŠ ØªØ­Ø¯ÙŠØ« Ø§Ù„Ù…Ù†Ø·Ù‚Ø©: ' + error.message, 'error');
    }
}

function showNotification(message, type = 'info') {
    try {
        // Ù…Ø­Ø§ÙˆÙ„Ø© Ø§Ø³ØªØ®Ø¯Ø§Ù… Ø®Ø¯Ù…Ø© Ø§Ù„Ø¥Ø´Ø¹Ø§Ø±Ø§Øª Ø§Ù„Ù…ØªØ§Ø­Ø©
        if (window.notificationManager) {
            window.notificationManager.show({ message, type });
        } else if (window.NotificationService) {
            window.NotificationService.show(message, type);
        } else if (window.GlobalUIHandler && window.GlobalUIHandler.showNotification) {
            window.GlobalUIHandler.showNotification(message, type);
        } else {
            // Ø§Ø³ØªØ®Ø¯Ø§Ù… alert ÙƒØ¨Ø¯ÙŠÙ„
            log.debug(`ðŸ“¢ ${type.toUpperCase()}: ${message}`);
            alert(message);
        }
    } catch (error) {
        log.error('âŒ Error showing notification:', error);
        log.debug(`ðŸ“¢ ${type.toUpperCase()}: ${message}`);
    }
}
