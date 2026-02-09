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
package ch.asit_asso.extract.plugins.fmedesktop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LocalizedMessages class.
 */
@DisplayName("LocalizedMessages")
public class LocalizedMessagesTest {

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor uses French language")
        void defaultConstructorUsesFrench() {
            LocalizedMessages messages = new LocalizedMessages();

            Locale locale = messages.getLocale();

            assertEquals("fr", locale.getLanguage());
        }

        @Test
        @DisplayName("Constructor with valid language code")
        void constructorWithValidLanguageCode() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            Locale locale = messages.getLocale();

            assertEquals("fr", locale.getLanguage());
        }

        @Test
        @DisplayName("Constructor with null language uses default")
        void constructorWithNullLanguageUsesDefault() {
            LocalizedMessages messages = new LocalizedMessages(null);

            Locale locale = messages.getLocale();

            assertEquals("fr", locale.getLanguage());
        }

        @Test
        @DisplayName("Constructor with invalid language uses default")
        void constructorWithInvalidLanguageUsesDefault() {
            LocalizedMessages messages = new LocalizedMessages("invalid");

            Locale locale = messages.getLocale();

            assertEquals("fr", locale.getLanguage());
        }

        @Test
        @DisplayName("Constructor with empty language uses default")
        void constructorWithEmptyLanguageUsesDefault() {
            LocalizedMessages messages = new LocalizedMessages("");

            Locale locale = messages.getLocale();

            assertEquals("fr", locale.getLanguage());
        }

        @Test
        @DisplayName("Constructor with comma-separated languages creates fallback chain")
        void constructorWithCommaSeparatedLanguages() {
            LocalizedMessages messages = new LocalizedMessages("de,en,fr");

            Locale locale = messages.getLocale();

            assertEquals("de", locale.getLanguage());
        }

        @Test
        @DisplayName("Constructor with regional variant is accepted")
        void constructorWithRegionalVariant() {
            LocalizedMessages messages = new LocalizedMessages("fr-CH");

            Locale locale = messages.getLocale();

            // The locale stores the exact language code provided (fr-CH), not just the language part
            assertEquals("fr-ch", locale.getLanguage());
        }
    }

    @Nested
    @DisplayName("getString tests")
    class GetStringTests {

        @Test
        @DisplayName("Returns localized string for plugin.label key")
        void returnsLocalizedStringForPluginLabel() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String label = messages.getString("plugin.label");

            assertNotNull(label);
            assertFalse(label.isEmpty());
            assertEquals("Extraction FME Form", label);
        }

        @Test
        @DisplayName("Returns localized string for plugin.description key")
        void returnsLocalizedStringForPluginDescription() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String description = messages.getString("plugin.description");

            assertNotNull(description);
            assertFalse(description.isEmpty());
            assertTrue(description.contains("FME Desktop"));
        }

        @Test
        @DisplayName("Returns localized string for paramPath.label")
        void returnsLocalizedStringForParamPathLabel() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String label = messages.getString("paramPath.label");

            assertNotNull(label);
            assertTrue(label.contains("workspace FME"));
        }

        @Test
        @DisplayName("Returns localized string for fme.executable.notfound")
        void returnsLocalizedStringForFmeExecutableNotFound() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String message = messages.getString("fme.executable.notfound");

            assertNotNull(message);
            assertTrue(message.contains("fme.exe"));
        }

        @Test
        @DisplayName("Returns localized string for fme.script.notfound")
        void returnsLocalizedStringForFmeScriptNotFound() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String message = messages.getString("fme.script.notfound");

            assertNotNull(message);
            assertTrue(message.contains("script FME"));
        }

        @Test
        @DisplayName("Returns localized string for fmeresult.message.success")
        void returnsLocalizedStringForFmeResultSuccess() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String message = messages.getString("fmeresult.message.success");

            assertNotNull(message);
            assertEquals("OK", message);
        }

        @Test
        @DisplayName("Returns localized string for fmeresult.error.folderout.empty")
        void returnsLocalizedStringForFolderOutEmpty() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String message = messages.getString("fmeresult.error.folderout.empty");

            assertNotNull(message);
            assertTrue(message.contains("aucun fichier"));
        }

        @Test
        @DisplayName("Returns key when key not found")
        void returnsKeyWhenNotFound() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String result = messages.getString("nonexistent.key");

            assertEquals("nonexistent.key", result);
        }

        @Test
        @DisplayName("Throws exception for null key")
        void throwsExceptionForNullKey() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            assertThrows(IllegalArgumentException.class, () -> messages.getString(null));
        }

        @Test
        @DisplayName("Throws exception for empty key")
        void throwsExceptionForEmptyKey() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            assertThrows(IllegalArgumentException.class, () -> messages.getString(""));
        }

        @Test
        @DisplayName("Throws exception for blank key")
        void throwsExceptionForBlankKey() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            assertThrows(IllegalArgumentException.class, () -> messages.getString("   "));
        }

        @Test
        @DisplayName("Returns error messages with placeholders")
        void returnsErrorMessagesWithPlaceholders() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String message = messages.getString("fme.executing.failed");

            assertNotNull(message);
            assertTrue(message.contains("%s"));
        }

        @Test
        @DisplayName("Returns Python interpreter error message")
        void returnsPythonInterpreterErrorMessage() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String message = messages.getString("error.pythonInterpreter.config");

            assertNotNull(message);
            assertFalse(message.isEmpty());
        }
    }

    @Nested
    @DisplayName("getFileContent tests")
    class GetFileContentTests {

        @Test
        @DisplayName("Returns help file content")
        void returnsHelpFileContent() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String content = messages.getFileContent("fmeDesktopHelp.html");

            assertNotNull(content);
            assertFalse(content.isEmpty());
        }

        @Test
        @DisplayName("Returns null for non-existent file")
        void returnsNullForNonExistentFile() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String content = messages.getFileContent("nonexistent.html");

            assertNull(content);
        }

        @Test
        @DisplayName("Throws exception for null filename")
        void throwsExceptionForNullFilename() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            assertThrows(IllegalArgumentException.class, () -> messages.getFileContent(null));
        }

        @Test
        @DisplayName("Throws exception for empty filename")
        void throwsExceptionForEmptyFilename() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            assertThrows(IllegalArgumentException.class, () -> messages.getFileContent(""));
        }

        @Test
        @DisplayName("Throws exception for path traversal attempt")
        void throwsExceptionForPathTraversal() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            assertThrows(IllegalArgumentException.class, () -> messages.getFileContent("../../../etc/passwd"));
        }
    }

    @Nested
    @DisplayName("getLocale tests")
    class GetLocaleTests {

        @Test
        @DisplayName("Returns correct Locale for French")
        void returnsCorrectLocaleForFrench() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            Locale locale = messages.getLocale();

            assertNotNull(locale);
            assertEquals("fr", locale.getLanguage());
        }

        @Test
        @DisplayName("Returns correct Locale for German")
        void returnsCorrectLocaleForGerman() {
            LocalizedMessages messages = new LocalizedMessages("de");

            Locale locale = messages.getLocale();

            assertNotNull(locale);
            assertEquals("de", locale.getLanguage());
        }

        @Test
        @DisplayName("Returns correct Locale for English")
        void returnsCorrectLocaleForEnglish() {
            LocalizedMessages messages = new LocalizedMessages("en");

            Locale locale = messages.getLocale();

            assertNotNull(locale);
            assertEquals("en", locale.getLanguage());
        }
    }

    @Nested
    @DisplayName("getHelp tests")
    class GetHelpTests {

        @Test
        @DisplayName("Returns help content for valid path")
        void returnsHelpContentForValidPath() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String help = messages.getHelp("plugins/fme/lang/fr/fmeDesktopHelp.html");

            assertNotNull(help);
            assertFalse(help.isEmpty());
            assertFalse(help.startsWith("Help file not found"));
        }

        @Test
        @DisplayName("Returns not found message for invalid path")
        void returnsNotFoundMessageForInvalidPath() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String help = messages.getHelp("nonexistent/path/help.html");

            assertNotNull(help);
            assertTrue(help.startsWith("Help file not found"));
        }
    }

    @Nested
    @DisplayName("Language fallback tests")
    class LanguageFallbackTests {

        @Test
        @DisplayName("Falls back to French when language not available")
        void fallsBackToFrenchWhenLanguageNotAvailable() {
            LocalizedMessages messages = new LocalizedMessages("es");

            String label = messages.getString("plugin.label");

            assertNotNull(label);
            assertFalse(label.isEmpty());
        }

        @Test
        @DisplayName("Multiple languages with comma separator")
        void multipleLanguagesWithCommaSeparator() {
            LocalizedMessages messages = new LocalizedMessages("en,de,fr");

            String label = messages.getString("plugin.label");

            assertNotNull(label);
            assertFalse(label.isEmpty());
        }
    }

    @Nested
    @DisplayName("Additional edge case tests")
    class AdditionalEdgeCaseTests {

        @Test
        @DisplayName("Constructor with whitespace-only language uses default")
        void constructorWithWhitespaceOnlyLanguageUsesDefault() {
            LocalizedMessages messages = new LocalizedMessages("   ");

            Locale locale = messages.getLocale();

            assertEquals("fr", locale.getLanguage());
        }

        @Test
        @DisplayName("Constructor with comma-separated invalid languages uses default")
        void constructorWithCommaSeparatedInvalidLanguagesUsesDefault() {
            LocalizedMessages messages = new LocalizedMessages("invalid1,invalid2,invalid3");

            Locale locale = messages.getLocale();

            assertEquals("fr", locale.getLanguage());
        }

        @Test
        @DisplayName("Constructor with mixed valid and invalid languages")
        void constructorWithMixedValidAndInvalidLanguages() {
            LocalizedMessages messages = new LocalizedMessages("invalid,de,fr");

            Locale locale = messages.getLocale();

            // First valid language should be used
            assertEquals("de", locale.getLanguage());
        }

        @Test
        @DisplayName("Constructor with spaces around language codes")
        void constructorWithSpacesAroundLanguageCodes() {
            LocalizedMessages messages = new LocalizedMessages(" de , en , fr ");

            Locale locale = messages.getLocale();

            assertEquals("de", locale.getLanguage());
        }

        @Test
        @DisplayName("getString for paramPathFME.label returns FME executable label")
        void getStringForParamPathFmeLabel() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String label = messages.getString("paramPathFME.label");

            assertNotNull(label);
            assertTrue(label.contains("fme.exe"));
        }

        @Test
        @DisplayName("getString for paramInstances.label contains placeholder")
        void getStringForParamInstancesLabelContainsPlaceholder() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String label = messages.getString("paramInstances.label");

            assertNotNull(label);
            assertTrue(label.contains("{maxInstances}"));
        }

        @Test
        @DisplayName("getFileContent with blank filename throws exception")
        void getFileContentWithBlankFilenameThrowsException() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            assertThrows(IllegalArgumentException.class, () -> messages.getFileContent("   "));
        }

        @Test
        @DisplayName("Help file content contains HTML structure")
        void helpFileContentContainsHtmlStructure() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String content = messages.getFileContent("fmeDesktopHelp.html");

            assertNotNull(content);
            assertTrue(content.contains("<") && content.contains(">"));
        }

        @Test
        @DisplayName("getHelp with invalid path returns not found message")
        void getHelpWithInvalidPathReturnsNotFoundMessage() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String help = messages.getHelp("invalid/nonexistent/path.html");

            assertNotNull(help);
            assertTrue(help.contains("not found"));
        }

        @Test
        @DisplayName("German locale returns correct language code")
        void germanLocaleReturnsCorrectLanguageCode() {
            LocalizedMessages messages = new LocalizedMessages("de");

            Locale locale = messages.getLocale();

            assertNotNull(locale);
            assertEquals("de", locale.getLanguage());
        }

        @Test
        @DisplayName("Fallback chain with regional variant")
        void fallbackChainWithRegionalVariant() {
            LocalizedMessages messages = new LocalizedMessages("de-AT,de,fr");

            // Should use de-AT as first language (falls back to de for messages)
            String label = messages.getString("plugin.label");

            assertNotNull(label);
            assertFalse(label.isEmpty());
        }

        @Test
        @DisplayName("Single language with trailing comma")
        void singleLanguageWithTrailingComma() {
            LocalizedMessages messages = new LocalizedMessages("fr,");

            Locale locale = messages.getLocale();

            assertEquals("fr", locale.getLanguage());
        }

        @Test
        @DisplayName("Empty string between commas uses valid languages")
        void emptyStringBetweenCommasUsesValidLanguages() {
            LocalizedMessages messages = new LocalizedMessages("de,,fr");

            Locale locale = messages.getLocale();

            assertEquals("de", locale.getLanguage());
        }
    }

    @Nested
    @DisplayName("All message keys tests")
    class AllMessageKeysTests {

        @Test
        @DisplayName("All FME error messages are available")
        void allFmeErrorMessagesAreAvailable() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String[] errorKeys = {
                "fme.script.notfound",
                "fme.executable.notfound",
                "fme.executing.failed",
                "fmeresult.message.success",
                "fmeresult.error.folderout.empty"
            };

            for (String key : errorKeys) {
                String message = messages.getString(key);
                assertNotNull(message, "Message for key '" + key + "' should not be null");
                assertNotEquals(key, message, "Key '" + key + "' should return actual message, not key itself");
            }
        }

        @Test
        @DisplayName("All parameter labels are available")
        void allParameterLabelsAreAvailable() {
            LocalizedMessages messages = new LocalizedMessages("fr");

            String[] paramKeys = {
                "paramPath.label",
                "paramPathFME.label",
                "paramInstances.label"
            };

            for (String key : paramKeys) {
                String label = messages.getString(key);
                assertNotNull(label, "Label for key '" + key + "' should not be null");
                assertFalse(label.isEmpty(), "Label for key '" + key + "' should not be empty");
            }
        }
    }
}
