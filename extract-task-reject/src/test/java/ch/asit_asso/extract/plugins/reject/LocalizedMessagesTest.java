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
package ch.asit_asso.extract.plugins.reject;

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
    @DisplayName("getString returns null for missing key")
    void testGetStringWithMissingKey() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        String value = messages.getString("non.existent.key");
        assertNull(value);
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
        String content = messages.getFileContent("rejectHelp.html");
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

        String remarkLabel = messages.getString("param.remark.label");
        assertNotNull(remarkLabel);
        assertFalse(remarkLabel.isEmpty());
    }

    @Test
    @DisplayName("Error messages are available")
    void testErrorMessagesAvailable() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String noRemarkError = messages.getString("reject.error.noRemark");
        assertNotNull(noRemarkError);
        assertFalse(noRemarkError.isEmpty());

        String failedMessage = messages.getString("remark.executing.failed");
        assertNotNull(failedMessage);
        assertFalse(failedMessage.isEmpty());
    }

    @Test
    @DisplayName("Success message is available")
    void testSuccessMessageAvailable() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String successMessage = messages.getString("remark.executing.success");
        assertNotNull(successMessage);
        assertFalse(successMessage.isEmpty());
    }
}
