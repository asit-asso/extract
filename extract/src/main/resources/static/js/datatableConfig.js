/**
 * DataTables configuration utilities
 *
 * This module provides configuration helpers for DataTables with i18n support.
 * Configuration data is injected by the Thymeleaf template via EXTRACT_CONFIG global object.
 */

/**
 * Gets the base DataTables configuration properties with i18n support
 *
 * @returns {Object} DataTables configuration object
 */
function getDataTableBaseProperties() {
    // Check if configuration has been injected by the template
    if (typeof EXTRACT_CONFIG === 'undefined' || !EXTRACT_CONFIG) {
        console.error('EXTRACT_CONFIG is not defined. DataTables i18n will not work properly.');
        return getDefaultDataTableProperties();
    }

    var languageCode = EXTRACT_CONFIG.language || 'fr';

    // Map language codes to DataTables i18n file names
    var languageFileMap = {
        'fr': 'fr-FR',
        'de': 'de-DE',
        'en': 'en-GB'
    };

    var languageFile = languageFileMap[languageCode] || 'fr-FR';
    var languageUrl = EXTRACT_CONFIG.datatables.i18nPath || '/lib/datatables.net-plugins/i18n/';

    return {
        "language": {
            "url": languageUrl + languageFile + '.json'
        },
        "pagingType": "simple_numbers",
        "info": false,
        "lengthChange": false,
        "layout": {
            "topEnd": null
        }
    };
}

/**
 * Fallback configuration when EXTRACT_CONFIG is not available
 * @private
 */
function getDefaultDataTableProperties() {
    return {
        "language": {
            "url": '/lib/datatables.net-plugins/i18n/fr-FR.json'
        },
        "pagingType": "simple_numbers",
        "info": false,
        "lengthChange": false,
        "layout": {
            "topEnd": null
        }
    };
}
