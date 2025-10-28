package ch.asit_asso.extract.plugins.fmeserverv2;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LocalizedMessages class
 */
class LocalizedMessagesTest {

    @Test
    void testDefaultConstructor() {
        LocalizedMessages messages = new LocalizedMessages();

        assertNotNull(messages);
        assertEquals("fr", messages.getLanguage());
        assertTrue(messages.isLoaded());
    }

    @Test
    void testConstructorWithValidLanguage() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        assertNotNull(messages);
        assertEquals("fr", messages.getLanguage());
        assertTrue(messages.isLoaded());
    }

    @Test
    void testConstructorWithEnglishLanguage() {
        LocalizedMessages messages = new LocalizedMessages("en");

        assertNotNull(messages);
        assertEquals("en", messages.getLanguage());
    }

    @Test
    void testConstructorWithCommaSeparatedLanguages() {
        LocalizedMessages messages = new LocalizedMessages("en,fr");

        assertNotNull(messages);
        assertEquals("en", messages.getLanguage());
    }

    @Test
    void testConstructorWithNullLanguage() {
        LocalizedMessages messages = new LocalizedMessages(null);

        assertNotNull(messages);
        assertEquals("fr", messages.getLanguage());
    }

    @Test
    void testConstructorWithEmptyLanguage() {
        LocalizedMessages messages = new LocalizedMessages("");

        assertNotNull(messages);
        assertEquals("fr", messages.getLanguage());
    }

    @Test
    void testConstructorWithWhitespaceLanguage() {
        LocalizedMessages messages = new LocalizedMessages("  ");

        assertNotNull(messages);
        assertEquals("fr", messages.getLanguage());
    }

    @Test
    void testConstructorWithInvalidLanguage() {
        // Should fallback to default language
        LocalizedMessages messages = new LocalizedMessages("invalid123");

        assertNotNull(messages);
        assertEquals("fr", messages.getLanguage());
    }

    @Test
    void testGetStringWithValidKey() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String value = messages.getString("plugin.label");

        assertNotNull(value);
        assertFalse(value.startsWith("["));
        assertFalse(value.endsWith("]"));
    }

    @Test
    void testGetStringWithInvalidKey() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String value = messages.getString("invalid.key.that.does.not.exist");

        assertNotNull(value);
        assertTrue(value.startsWith("["));
        assertTrue(value.endsWith("]"));
        assertTrue(value.contains("invalid.key.that.does.not.exist"));
    }

    @Test
    void testGetStringWithBlankKey() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String value = messages.getString("");

        assertNotNull(value);
        assertEquals("[EMPTY_KEY]", value);
    }

    @Test
    void testGetStringWithNullKey() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String value = messages.getString(null);

        assertNotNull(value);
        assertEquals("[EMPTY_KEY]", value);
    }

    @Test
    void testGetStringWithFormattingOneArg() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        // Assuming there's a message with %s placeholder
        String value = messages.getString("plugin.errors.request.validation", "testArg");

        assertNotNull(value);
    }

    @Test
    void testGetStringWithFormattingMultipleArgs() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String value = messages.getString("plugin.errors.request.validation", "arg1", "arg2", "arg3");

        assertNotNull(value);
    }

    @Test
    void testGetStringWithFormattingButMissingKey() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String value = messages.getString("missing.key", "arg1", "arg2");

        assertNotNull(value);
        assertTrue(value.contains("[missing.key]"));
        assertTrue(value.contains("arg1"));
    }

    @Test
    void testHasKeyWithValidKey() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        assertTrue(messages.hasKey("plugin.label"));
    }

    @Test
    void testHasKeyWithInvalidKey() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        assertFalse(messages.hasKey("invalid.key.does.not.exist"));
    }

    @Test
    void testHasKeyWithNullKey() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        assertFalse(messages.hasKey(null));
    }

    @Test
    void testHasKeyWithBlankKey() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        assertFalse(messages.hasKey(""));
    }

    @Test
    void testGetFileContentWithValidFile() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String content = messages.getFileContent("help.html");

        assertNotNull(content);
        assertFalse(content.isEmpty());
        // Help file should contain HTML
        assertTrue(content.contains("<") || content.contains("plugin.errors"));
    }

    @Test
    void testGetFileContentWithNonExistentFile() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String content = messages.getFileContent("nonexistent_file.html");

        assertNotNull(content);
        // Should return error message or warning
        assertFalse(content.isEmpty());
    }

    @Test
    void testGetFileContentWithBlankFilename() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String content = messages.getFileContent("");

        assertNotNull(content);
        // Should return error message about blank filename
        assertFalse(content.isEmpty());
    }

    @Test
    void testGetFileContentWithNullFilename() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String content = messages.getFileContent(null);

        assertNotNull(content);
        assertFalse(content.isEmpty());
    }

    @Test
    void testGetFileContentWithPathTraversal() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String content = messages.getFileContent("../../../etc/passwd");

        assertNotNull(content);
        // Should return error message about invalid path
        assertTrue(content.contains("invalid") || content.startsWith("["));
    }

    @Test
    void testGetFileContentWithBackslashPathTraversal() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        String content = messages.getFileContent("..\\..\\windows\\system32\\config\\sam");

        assertNotNull(content);
        assertTrue(content.contains("invalid") || content.startsWith("["));
    }

    @Test
    void testIsLoaded() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        assertTrue(messages.isLoaded());
    }

    @Test
    void testGetLanguage() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        assertEquals("fr", messages.getLanguage());
    }

    @Test
    void testLanguageWithRegion() {
        LocalizedMessages messages = new LocalizedMessages("fr-CH");

        assertNotNull(messages);
        // Should either use fr-CH or fallback to fr
        assertTrue(messages.getLanguage().equals("fr-CH") || messages.getLanguage().equals("fr"));
    }

    @Test
    void testMultipleInstancesIndependent() {
        LocalizedMessages messagesFr = new LocalizedMessages("fr");
        LocalizedMessages messagesEn = new LocalizedMessages("en");

        assertEquals("fr", messagesFr.getLanguage());
        assertEquals("en", messagesEn.getLanguage());

        // Both should be loaded independently
        assertTrue(messagesFr.isLoaded());
        assertTrue(messagesEn.isLoaded());
    }

    @Test
    void testDefaultErrorMessagesExist() {
        LocalizedMessages messages = new LocalizedMessages("fr");

        // These default error messages should always exist
        String blankError = messages.getString("plugin.errors.internal.file.blank");
        String invalidError = messages.getString("plugin.errors.internal.file.invalid");
        String notfoundError = messages.getString("plugin.errors.internal.file.notfound");

        assertNotNull(blankError);
        assertNotNull(invalidError);
        assertNotNull(notfoundError);

        // Should not be key placeholders
        assertFalse(blankError.startsWith("[") && blankError.endsWith("]"));
        assertFalse(invalidError.startsWith("[") && invalidError.endsWith("]"));
        assertFalse(notfoundError.startsWith("[") && notfoundError.endsWith("]"));
    }
}
