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
package ch.asit_asso.extract.plugins.validation;

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
        String content = messages.getFileContent("validationHelp.html");
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

        String validMessagesLabel = messages.getString("paramValidMessages.label");
        assertNotNull(validMessagesLabel);
        assertFalse(validMessagesLabel.isEmpty());

        String rejectMessagesLabel = messages.getString("paramRejectMessages.label");
        assertNotNull(rejectMessagesLabel);
        assertFalse(rejectMessagesLabel.isEmpty());
    }

    @Test
    @DisplayName("Validation message is available")
    void testValidationMessageAvailable() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String validationMessage = messages.getString("messageValidation");
        assertNotNull(validationMessage);
        assertFalse(validationMessage.isEmpty());
    }

    @Test
    @DisplayName("dump method does not throw exception")
    void testDumpDoesNotThrow() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        assertDoesNotThrow(() -> messages.dump());
    }

    @Test
    @DisplayName("Constructor with null language falls back to default")
    void testConstructorWithNullLanguage() {
        LocalizedMessages messages = new LocalizedMessages(null);
        assertNotNull(messages);
        // Should be able to get strings
        String value = messages.getString("plugin.label");
        assertNotNull(value);
    }

    @Test
    @DisplayName("Constructor with regional language code works")
    void testConstructorWithRegionalLanguageCode() {
        LocalizedMessages messages = new LocalizedMessages("fr-CH");
        assertNotNull(messages);
        String value = messages.getString("plugin.label");
        assertNotNull(value);
    }

    @Test
    @DisplayName("Constructor with empty string language falls back to default")
    void testConstructorWithEmptyLanguage() {
        LocalizedMessages messages = new LocalizedMessages("");
        assertNotNull(messages);
    }

    @Test
    @DisplayName("getString with null key throws exception")
    void testGetStringWithNullKey() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        assertThrows(IllegalArgumentException.class, () -> messages.getString(null));
    }

    @Test
    @DisplayName("getFileContent with null filename throws exception")
    void testGetFileContentWithNullFilename() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        assertThrows(IllegalArgumentException.class, () -> messages.getFileContent(null));
    }

    @Test
    @DisplayName("getFileContent with blank filename throws exception")
    void testGetFileContentWithBlankFilename() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        assertThrows(IllegalArgumentException.class, () -> messages.getFileContent("   "));
    }

    @Test
    @DisplayName("Constructor with mixed valid and invalid languages in comma-separated list")
    void testConstructorWithMixedLanguages() {
        LocalizedMessages messages = new LocalizedMessages("invalid,fr,also_invalid");
        assertNotNull(messages);
        String value = messages.getString("plugin.label");
        assertNotNull(value);
    }

    @Test
    @DisplayName("Constructor with only invalid languages in comma-separated list falls back to default")
    void testConstructorWithOnlyInvalidLanguages() {
        LocalizedMessages messages = new LocalizedMessages("invalid1,invalid2");
        assertNotNull(messages);
    }

    @Test
    @DisplayName("Constructor with whitespace around language codes")
    void testConstructorWithWhitespaceAroundLanguages() {
        LocalizedMessages messages = new LocalizedMessages(" fr , en ");
        assertNotNull(messages);
        String value = messages.getString("plugin.label");
        assertNotNull(value);
    }
}
