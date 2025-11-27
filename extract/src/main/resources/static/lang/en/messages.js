/**
 * @file Creates an object containing the localized messages using by scripts.
 * @author Yves Grasset
 */

/**
 * Localized messages to be used by scripts.
 * This is the default (fallback) language.
 *
 * @type Object
 */
var LANG_MESSAGES = LANG_MESSAGES || {};

/**
 * Default (French) messages that serve as fallback for missing translations.
 */
var LANG_MESSAGES_EN = {
    "connectorsList" : {
        "deleteConfirm" : {
            "title" : "Delete a connector",
            "message" : "Are you sure you want to delete the connector \"{0}\" ?"
        }
    }
};


// Merge French messages into LANG_MESSAGES (provides default/fallback values)
// Using jQuery's deep extend to merge nested objects
if (typeof jQuery !== 'undefined') {
    jQuery.extend(true, LANG_MESSAGES, LANG_MESSAGES_EN);
} else {
    // Fallback if jQuery is not yet loaded (should not happen in normal usage)
    LANG_MESSAGES = LANG_MESSAGES_EN;
}

//var RULE_HELP_CONTENT = 'static/lang/fr/rulesHelp.html';

// Strings used only during development. What follows will be removed from production code
LANG_MESSAGES['development'] = {
    "notImplemented" : "Not yet developed",
    "notImplementedLong" : "This feature has not yet been developed."
};
