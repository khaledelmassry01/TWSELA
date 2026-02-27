/**
 * Twsela CMS - Centralized Configuration
 * Single source of truth for API base URL and environment settings.
 * All services should import from here instead of hardcoding values.
 */

const TwselaConfig = {
    /**
     * Get the API base URL based on the current environment.
     * In production, this should be updated to the production API domain.
     * @returns {string} API base URL
     */
    getApiBaseUrl() {
        // Check for a runtime override (e.g. set via environment at build time)
        if (window.__TWSELA_API_URL__) {
            return window.__TWSELA_API_URL__;
        }
        // Default for local development
        return 'http://localhost:8000';
    },

    /** Auth token key in localStorage */
    AUTH_TOKEN_KEY: 'authToken',

    /** User data key in localStorage */
    USER_DATA_KEY: 'userData',

    /** API request timeout in ms */
    REQUEST_TIMEOUT: 30000,

    /** Auth cache TTL in ms (5 minutes) */
    AUTH_CACHE_TTL: 5 * 60 * 1000
};

// Freeze to prevent accidental mutation
Object.freeze(TwselaConfig);

// Make globally available
window.TwselaConfig = TwselaConfig;
