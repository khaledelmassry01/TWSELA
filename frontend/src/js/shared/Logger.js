/**
 * Logger — مركزي للتسجيل في كافة أنحاء Frontend
 *
 * يوفر طبقة تجريد فوق console.* مع التحكم في مستوى السجل.
 * في بيئة الإنتاج يتم كتم الرسائل DEBUG و INFO تلقائياً.
 *
 * @example
 *   import { Logger } from '../shared/Logger.js';
 *   const log = Logger.getLogger('AuthService');
 *   log.debug('Starting login flow');
 *   log.info('User authenticated', { userId: 123 });
 *   log.warn('Token will expire soon');
 *   log.error('Login failed', error);
 */

const LOG_LEVELS = Object.freeze({
    DEBUG: 0,
    INFO: 1,
    WARN: 2,
    ERROR: 3,
    SILENT: 4,
});

// In production, suppress DEBUG and INFO
const IS_PROD =
    typeof window !== 'undefined' &&
    window.location?.hostname !== 'localhost' &&
    window.location?.hostname !== '127.0.0.1';

let globalLevel = IS_PROD ? LOG_LEVELS.WARN : LOG_LEVELS.DEBUG;

/**
 * Set the minimum log level globally.
 * @param {'DEBUG'|'INFO'|'WARN'|'ERROR'|'SILENT'} level
 */
function setLevel(level) {
    if (LOG_LEVELS[level] === undefined) {
        // eslint-disable-next-line no-console
        console.warn(`[Logger] Unknown level "${level}". Using WARN.`);
        globalLevel = LOG_LEVELS.WARN;
        return;
    }
    globalLevel = LOG_LEVELS[level];
}

/**
 * Create a scoped logger instance.
 * @param {string} tag — Module or component name prefixed to every message.
 * @returns {{ debug: Function, info: Function, warn: Function, error: Function }}
 */
function getLogger(tag = '') {
    const prefix = tag ? `[${tag}]` : '';

    return {
        debug(...args) {
            if (globalLevel <= LOG_LEVELS.DEBUG) {
                // eslint-disable-next-line no-console
                console.debug(prefix, ...args);
            }
        },
        info(...args) {
            if (globalLevel <= LOG_LEVELS.INFO) {
                // eslint-disable-next-line no-console
                console.info(prefix, ...args);
            }
        },
        warn(...args) {
            if (globalLevel <= LOG_LEVELS.WARN) {
                // eslint-disable-next-line no-console
                console.warn(prefix, ...args);
            }
        },
        error(...args) {
            if (globalLevel <= LOG_LEVELS.ERROR) {
                // eslint-disable-next-line no-console
                console.error(prefix, ...args);
            }
        },
    };
}

export const Logger = Object.freeze({
    getLogger,
    setLevel,
    LOG_LEVELS,
});
