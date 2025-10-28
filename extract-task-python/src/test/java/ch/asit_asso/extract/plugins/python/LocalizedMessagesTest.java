package ch.asit_asso.extract.plugins.python;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LocalizedMessagesTest {

    @Test
    void testDefaultConstructor() {
        LocalizedMessages messages = new LocalizedMessages();
        assertNotNull(messages);
    }

    @Test
    void testConstructorWithLanguage() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        assertNotNull(messages);
    }

    @Test
    void testGetStringWithValidKey() {
        LocalizedMessages messages = new LocalizedMessages("fr");
        String value = messages.getString("plugin.label");
        assertNotNull(value);
    }
}
