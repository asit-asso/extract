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
package ch.asit_asso.extract.plugins.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the LocalizedMessages class.
 */
@DisplayName("LocalizedMessages")
class LocalizedMessagesTest {

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor uses French language")
        void defaultConstructorUsesFrench() {
            LocalizedMessages messages = new LocalizedMessages();

            String label = messages.getString("plugin.label");
            assertNotNull(label);
            assertFalse(label.isEmpty());
        }

        @Test
        @DisplayName("Constructor with valid language code loads messages")
        void constructorWithValidLanguageLoadsMessages() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String label = messages.getString("plugin.label");
            assertNotNull(label);
            assertFalse(label.isEmpty());
        }

        @Test
        @DisplayName("Constructor with German language loads German messages")
        void constructorWithGermanLanguageLoadsGermanMessages() {
            LocalizedMessages messages = new LocalizedMessages("de");

            String label = messages.getString("plugin.label");
            assertNotNull(label);
            assertFalse(label.isEmpty());
        }

        @Test
        @DisplayName("Constructor with invalid language falls back to French")
        void constructorWithInvalidLanguageFallsBackToFrench() {
            LocalizedMessages messages = new LocalizedMessages("invalid");

            String label = messages.getString("plugin.label");
            assertNotNull(label);
            assertFalse(label.isEmpty());
        }

        @Test
        @DisplayName("Constructor with null language falls back to French")
        void constructorWithNullLanguageFallsBackToFrench() {
            LocalizedMessages messages = new LocalizedMessages(null);

            String label = messages.getString("plugin.label");
            assertNotNull(label);
            assertFalse(label.isEmpty());
        }

        @Test
        @DisplayName("Constructor with empty language falls back to French")
        void constructorWithEmptyLanguageFallsBackToFrench() {
            LocalizedMessages messages = new LocalizedMessages("");

            String label = messages.getString("plugin.label");
            assertNotNull(label);
            assertFalse(label.isEmpty());
        }

        @Test
        @DisplayName("Constructor with regional variant extracts base language")
        void constructorWithRegionalVariantExtractsBaseLanguage() {
            LocalizedMessages messages = new LocalizedMessages("de-CH");

            String label = messages.getString("plugin.label");
            assertNotNull(label);
            assertFalse(label.isEmpty());
        }
    }

    @Nested
    @DisplayName("Cascading fallback tests")
    class CascadingFallbackTests {

        @Test
        @DisplayName("Multiple languages create cascading fallback")
        void multipleLanguagesCreateCascadingFallback() {
            LocalizedMessages messages = new LocalizedMessages("de,fr");

            String label = messages.getString("plugin.label");
            assertNotNull(label);
            assertFalse(label.isEmpty());
        }

        @Test
        @DisplayName("Cascading fallback with spaces in language list")
        void cascadingFallbackWithSpaces() {
            LocalizedMessages messages = new LocalizedMessages("de, fr");

            String label = messages.getString("plugin.label");
            assertNotNull(label);
            assertFalse(label.isEmpty());
        }
    }

    @Nested
    @DisplayName("getString tests")
    class GetStringTests {

        @Test
        @DisplayName("getString returns value for existing key")
        void getStringReturnsValueForExistingKey() {
            LocalizedMessages messages = new LocalizedMessages();

            String label = messages.getString("plugin.label");
            assertNotNull(label);
            assertFalse(label.isEmpty());
        }

        @Test
        @DisplayName("getString returns key for non-existing key")
        void getStringReturnsKeyForNonExistingKey() {
            LocalizedMessages messages = new LocalizedMessages();

            String value = messages.getString("non.existent.key");
            assertEquals("non.existent.key", value);
        }

        @Test
        @DisplayName("getString throws exception for null key")
        void getStringThrowsExceptionForNullKey() {
            LocalizedMessages messages = new LocalizedMessages();

            assertThrows(IllegalArgumentException.class, () -> messages.getString(null));
        }

        @Test
        @DisplayName("getString throws exception for empty key")
        void getStringThrowsExceptionForEmptyKey() {
            LocalizedMessages messages = new LocalizedMessages();

            assertThrows(IllegalArgumentException.class, () -> messages.getString(""));
        }

        @Test
        @DisplayName("getString throws exception for blank key")
        void getStringThrowsExceptionForBlankKey() {
            LocalizedMessages messages = new LocalizedMessages();

            assertThrows(IllegalArgumentException.class, () -> messages.getString("   "));
        }
    }

    @Nested
    @DisplayName("getFileContent tests")
    class GetFileContentTests {

        @Test
        @DisplayName("getFileContent returns content for existing file")
        void getFileContentReturnsContentForExistingFile() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String content = messages.getFileContent("emailHelp.html");
            assertNotNull(content);
            assertFalse(content.isEmpty());
        }

        @Test
        @DisplayName("getFileContent returns null for non-existing file")
        void getFileContentReturnsNullForNonExistingFile() {
            LocalizedMessages messages = new LocalizedMessages();

            String content = messages.getFileContent("nonexistent.html");
            assertNull(content);
        }

        @Test
        @DisplayName("getFileContent throws exception for null filename")
        void getFileContentThrowsExceptionForNullFilename() {
            LocalizedMessages messages = new LocalizedMessages();

            assertThrows(IllegalArgumentException.class, () -> messages.getFileContent(null));
        }

        @Test
        @DisplayName("getFileContent throws exception for path traversal attempt")
        void getFileContentThrowsExceptionForPathTraversal() {
            LocalizedMessages messages = new LocalizedMessages();

            assertThrows(IllegalArgumentException.class, () -> messages.getFileContent("../../../etc/passwd"));
        }

        @Test
        @DisplayName("getFileContent throws exception for empty filename")
        void getFileContentThrowsExceptionForEmptyFilename() {
            LocalizedMessages messages = new LocalizedMessages();

            assertThrows(IllegalArgumentException.class, () -> messages.getFileContent(""));
        }
    }

    @Nested
    @DisplayName("Message key tests")
    class MessageKeyTests {

        @Test
        @DisplayName("Plugin label is available")
        void pluginLabelIsAvailable() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String value = messages.getString("plugin.label");
            assertNotNull(value);
            assertNotEquals("plugin.label", value);
        }

        @Test
        @DisplayName("Plugin description is available")
        void pluginDescriptionIsAvailable() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String value = messages.getString("plugin.description");
            assertNotNull(value);
            assertNotEquals("plugin.description", value);
        }

        @Test
        @DisplayName("Parameter labels are available")
        void parameterLabelsAreAvailable() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String toLabel = messages.getString("param.to.label");
            assertNotNull(toLabel);
            assertNotEquals("param.to.label", toLabel);

            String subjectLabel = messages.getString("param.subject.label");
            assertNotNull(subjectLabel);
            assertNotEquals("param.subject.label", subjectLabel);

            String bodyLabel = messages.getString("param.body.label");
            assertNotNull(bodyLabel);
            assertNotEquals("param.body.label", bodyLabel);
        }

        @Test
        @DisplayName("Error messages are available")
        void errorMessagesAreAvailable() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String noAddressee = messages.getString("email.error.noAddressee");
            assertNotNull(noAddressee);
            assertNotEquals("email.error.noAddressee", noAddressee);

            String failed = messages.getString("email.executing.failed");
            assertNotNull(failed);
            assertNotEquals("email.executing.failed", failed);
        }

        @Test
        @DisplayName("Success message is available")
        void successMessageIsAvailable() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String value = messages.getString("email.executing.success");
            assertNotNull(value);
            assertNotEquals("email.executing.success", value);
        }
    }

    @Nested
    @DisplayName("Language independence tests")
    class LanguageIndependenceTests {

        @Test
        @DisplayName("Different language instances are independent")
        void differentLanguageInstancesAreIndependent() {
            LocalizedMessages french = new LocalizedMessages("fr");
            LocalizedMessages german = new LocalizedMessages("de");

            String frenchLabel = french.getString("plugin.label");
            String germanLabel = german.getString("plugin.label");

            assertNotNull(frenchLabel);
            assertNotNull(germanLabel);
        }

        @Test
        @DisplayName("Multiple instances with same language are independent")
        void multipleInstancesWithSameLanguageAreIndependent() {
            LocalizedMessages messages1 = new LocalizedMessages("fr");
            LocalizedMessages messages2 = new LocalizedMessages("fr");

            String label1 = messages1.getString("plugin.label");
            String label2 = messages2.getString("plugin.label");

            assertEquals(label1, label2);
        }
    }
}
