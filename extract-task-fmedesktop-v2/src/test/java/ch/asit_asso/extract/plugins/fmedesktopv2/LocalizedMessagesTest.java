package ch.asit_asso.extract.plugins.fmedesktopv2;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LocalizedMessagesTest {

    @Test
    void testDefaultConstructor() {
        LocalizedMessages messages = new LocalizedMessages();
        assertNotNull(messages);
        assertEquals("fr", messages.getLocale().getLanguage());
    }

    @Test
    void testConstructorWithLanguage() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        assertEquals("fr", messages.getLocale().getLanguage());
    }

    @Test
    void testGetStringWithValidKey() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        String value = messages.getString("plugin.label");
        assertNotNull(value);
    }

    @Test
    void testGetLocale() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        assertNotNull(messages.getLocale());
        assertEquals("fr", messages.getLocale().getLanguage());
    }

    @Test
    void testGetHelpWithValidFile() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        String help = messages.getHelp("plugins/fmedesktopv2/lang/fr/help.html");
        assertNotNull(help);
        assertFalse(help.isEmpty());
    }

    @Test
    void testGetHelpWithNonExistentFile() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        String help = messages.getHelp("nonexistent/file.html");
        assertNotNull(help);
        assertTrue(help.contains("not found") || help.contains("Help file not found"));
    }
}
