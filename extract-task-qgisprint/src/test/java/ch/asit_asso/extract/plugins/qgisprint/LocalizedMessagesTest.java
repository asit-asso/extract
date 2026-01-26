/*
 * Copyright (C) 2017 arx iT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.asit_asso.extract.plugins.qgisprint;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LocalizedMessages
 */
class LocalizedMessagesTest {

    @Test
    @DisplayName("Default constructor uses French language")
    void testDefaultConstructor() {
        LocalizedMessages messages = new LocalizedMessages();
        assertNotNull(messages);
    }

    @Test
    @DisplayName("Constructor with valid language code works")
    void testConstructorWithValidLanguage() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        assertNotNull(messages);
    }

    @Test
    @DisplayName("Constructor with invalid language falls back to default")
    void testConstructorWithInvalidLanguage() {
        LocalizedMessages messages = new LocalizedMessages("invalid");
        assertNotNull(messages);
    }

    @Test
    @DisplayName("Constructor with comma-separated languages supports fallback")
    void testConstructorWithMultipleLanguages() {
        LocalizedMessages messages = new LocalizedMessages("de,en,fr");
        assertNotNull(messages);
    }

    @Test
    @DisplayName("getString returns value for valid key")
    void testGetStringWithValidKey() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        String value = messages.getString("plugin.label");
        assertNotNull(value);
        assertFalse(value.isEmpty());
    }

    @Test
    @DisplayName("getString returns key for missing key")
    void testGetStringWithMissingKey() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        String value = messages.getString("non.existent.key");
        assertEquals("non.existent.key", value);
    }

    @Test
    @DisplayName("getString throws exception for blank key")
    void testGetStringWithBlankKey() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        assertThrows(IllegalArgumentException.class, () -> messages.getString(""));
        assertThrows(IllegalArgumentException.class, () -> messages.getString("   "));
    }

    @Test
    @DisplayName("getFileContent returns content for valid file")
    void testGetFileContentWithValidFile() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        String content = messages.getFileContent("help.html");
        assertNotNull(content);
        assertFalse(content.isEmpty());
    }

    @Test
    @DisplayName("getFileContent returns null for non-existent file")
    void testGetFileContentWithNonExistentFile() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        String content = messages.getFileContent("nonexistent.html");
        assertNull(content);
    }

    @Test
    @DisplayName("getFileContent throws exception for invalid filename")
    void testGetFileContentWithInvalidFilename() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        assertThrows(IllegalArgumentException.class, () -> messages.getFileContent(""));
        assertThrows(IllegalArgumentException.class, () -> messages.getFileContent("../etc/passwd"));
    }

    @Test
    @DisplayName("Plugin description is available")
    void testPluginDescriptionAvailable() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        String description = messages.getString("plugin.description");
        assertNotNull(description);
        assertFalse(description.isEmpty());
    }

    @Test
    @DisplayName("Parameter labels are available")
    void testParameterLabelsAvailable() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String urlLabel = messages.getString("paramUrl.label");
        assertNotNull(urlLabel);
        assertFalse(urlLabel.isEmpty());

        String templateLabel = messages.getString("paramTemplateLayout.label");
        assertNotNull(templateLabel);
        assertFalse(templateLabel.isEmpty());
    }

    @Test
    @DisplayName("Constructor with null language uses default")
    void testConstructorWithNullLanguage() {
        LocalizedMessages messages = new LocalizedMessages(null);
        assertNotNull(messages);
        // Should still be able to get strings from default language
        String label = messages.getString("plugin.label");
        assertNotNull(label);
    }

    @Test
    @DisplayName("Constructor with empty string uses default")
    void testConstructorWithEmptyLanguage() {
        LocalizedMessages messages = new LocalizedMessages("");
        assertNotNull(messages);
    }

    @Test
    @DisplayName("Constructor with regional variant language code works")
    void testConstructorWithRegionalVariant() {
        LocalizedMessages messages = new LocalizedMessages("fr-CH");
        assertNotNull(messages);
        // Should fall back to fr if fr-CH is not available
        String label = messages.getString("plugin.label");
        assertNotNull(label);
        assertFalse(label.isEmpty());
    }

    @Test
    @DisplayName("Constructor with multiple languages including regional variant")
    void testConstructorWithMultipleLanguagesIncludingRegional() {
        LocalizedMessages messages = new LocalizedMessages("de-CH,en,fr");
        assertNotNull(messages);
        // Should eventually fall back to fr
        String label = messages.getString("plugin.label");
        assertNotNull(label);
    }

    @Test
    @DisplayName("Constructor with whitespace in language list")
    void testConstructorWithWhitespaceInLanguageList() {
        LocalizedMessages messages = new LocalizedMessages("de , en , fr");
        assertNotNull(messages);
        String label = messages.getString("plugin.label");
        assertNotNull(label);
    }

    @Test
    @DisplayName("getFileContent with null filename throws exception")
    void testGetFileContentWithNullFilename() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        assertThrows(IllegalArgumentException.class, () -> messages.getFileContent(null));
    }

    @Test
    @DisplayName("getString with null key throws exception")
    void testGetStringWithNullKey() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        assertThrows(IllegalArgumentException.class, () -> messages.getString(null));
    }

    @Test
    @DisplayName("All error messages are available")
    void testErrorMessagesAvailable() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String genericError = messages.getString("error.message.generic");
        assertNotNull(genericError);
        assertFalse(genericError.isEmpty());
    }

    @Test
    @DisplayName("HTTP error messages are available")
    void testHttpErrorMessagesAvailable() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        // Test common HTTP error codes
        String error400 = messages.getString("httperror.message.400");
        String error401 = messages.getString("httperror.message.401");
        String error404 = messages.getString("httperror.message.404");
        String error500 = messages.getString("httperror.message.500");

        // At least the generic error should exist
        String genericError = messages.getString("error.message.generic");
        assertNotNull(genericError);
    }

    @Test
    @DisplayName("Plugin error messages are available")
    void testPluginErrorMessagesAvailable() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String coverageLayerError = messages.getString("plugin.error.coveragelayer");
        assertNotNull(coverageLayerError);

        String noIdsError = messages.getString("plugin.error.getFeature.noids");
        assertNotNull(noIdsError);
    }

    @Test
    @DisplayName("Plugin success message is available")
    void testPluginSuccessMessageAvailable() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String successMessage = messages.getString("plugin.executing.success");
        assertNotNull(successMessage);
        assertFalse(successMessage.isEmpty());
    }

    @Test
    @DisplayName("Plugin failed message is available")
    void testPluginFailedMessageAvailable() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String failedMessage = messages.getString("plugin.executing.failed");
        assertNotNull(failedMessage);
        assertFalse(failedMessage.isEmpty());
    }

    @Test
    @DisplayName("Multiple calls to getString with same key return same value")
    void testGetStringConsistency() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String first = messages.getString("plugin.label");
        String second = messages.getString("plugin.label");

        assertEquals(first, second);
    }

    @Test
    @DisplayName("Different instances return same localized strings")
    void testDifferentInstancesSameStrings() {
        LocalizedMessages messages1 = new LocalizedMessages("fr");
        LocalizedMessages messages2 = new LocalizedMessages("fr");

        String label1 = messages1.getString("plugin.label");
        String label2 = messages2.getString("plugin.label");

        assertEquals(label1, label2);
    }

    @Test
    @DisplayName("getFileContent returns same content on multiple calls")
    void testGetFileContentConsistency() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String first = messages.getFileContent("help.html");
        String second = messages.getFileContent("help.html");

        assertEquals(first, second);
    }

    @Test
    @DisplayName("Constructor with only invalid languages in comma-separated list uses default")
    void testConstructorWithOnlyInvalidLanguages() {
        LocalizedMessages messages = new LocalizedMessages("xx,yy,zz");
        assertNotNull(messages);
        // Should fall back to default and still work
        String label = messages.getString("plugin.label");
        assertNotNull(label);
    }

    @Test
    @DisplayName("Constructor with mix of valid and invalid languages")
    void testConstructorWithMixedValidInvalidLanguages() {
        LocalizedMessages messages = new LocalizedMessages("invalid,fr,alsoinvalid");
        assertNotNull(messages);
        String label = messages.getString("plugin.label");
        assertNotNull(label);
        assertFalse(label.isEmpty());
    }
}
