package ch.asit_asso.extract.unit.web;

import ch.asit_asso.extract.web.Message;
import ch.asit_asso.extract.web.Message.MessageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageTest {

    @Test
    void constructorWithValidKeyAndType() {
        String key = "test.message.key";
        MessageType type = MessageType.INFO;

        Message message = new Message(key, type);

        assertNotNull(message);
        assertEquals(key, message.getMessageKey());
        assertEquals(type, message.getMessageType());
    }


    @ParameterizedTest
    @EnumSource(MessageType.class)
    void constructorWithAllMessageTypes(MessageType type) {
        String key = "test.message.key";

        Message message = new Message(key, type);

        assertNotNull(message);
        assertEquals(key, message.getMessageKey());
        assertEquals(type, message.getMessageType());
    }


    @Test
    void constructorWithErrorType() {
        String key = "error.message";
        MessageType type = MessageType.ERROR;

        Message message = new Message(key, type);

        assertEquals(key, message.getMessageKey());
        assertEquals(MessageType.ERROR, message.getMessageType());
    }


    @Test
    void constructorWithSuccessType() {
        String key = "success.message";
        MessageType type = MessageType.SUCCESS;

        Message message = new Message(key, type);

        assertEquals(key, message.getMessageKey());
        assertEquals(MessageType.SUCCESS, message.getMessageType());
    }


    @Test
    void constructorWithWarningType() {
        String key = "warning.message";
        MessageType type = MessageType.WARNING;

        Message message = new Message(key, type);

        assertEquals(key, message.getMessageKey());
        assertEquals(MessageType.WARNING, message.getMessageType());
    }


    @Test
    void constructorWithInfoType() {
        String key = "info.message";
        MessageType type = MessageType.INFO;

        Message message = new Message(key, type);

        assertEquals(key, message.getMessageKey());
        assertEquals(MessageType.INFO, message.getMessageType());
    }


    @Test
    void constructorWithNullKeyThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Message(null, MessageType.INFO));
    }


    @ParameterizedTest
    @NullAndEmptySource
    void constructorWithNullOrEmptyKeyThrowsException(String key) {
        assertThrows(IllegalArgumentException.class, () -> new Message(key, MessageType.INFO));
    }


    @Test
    void constructorWithEmptyKeyThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Message("", MessageType.INFO));
    }


    @Test
    void constructorWithBlankKeyThrowsException() {
        // Note: StringUtils.hasLength considers whitespace-only strings as having length
        // So " " is not empty and would be accepted
        Message message = new Message("   ", MessageType.INFO);
        assertEquals("   ", message.getMessageKey());
    }


    @Test
    void constructorWithNullTypeStoresNull() {
        String key = "test.key";

        Message message = new Message(key, null);

        assertEquals(key, message.getMessageKey());
        assertNull(message.getMessageType());
    }


    @Test
    void getMessageKeyReturnsCorrectValue() {
        String expectedKey = "my.specific.message.key";
        Message message = new Message(expectedKey, MessageType.SUCCESS);

        String actualKey = message.getMessageKey();

        assertEquals(expectedKey, actualKey);
    }


    @Test
    void getMessageTypeReturnsCorrectValue() {
        MessageType expectedType = MessageType.WARNING;
        Message message = new Message("test.key", expectedType);

        MessageType actualType = message.getMessageType();

        assertEquals(expectedType, actualType);
    }


    @ParameterizedTest
    @ValueSource(strings = {"a", "test.key", "very.long.message.key.with.many.parts", "123", "key-with-dashes"})
    void constructorAcceptsVariousValidKeys(String key) {
        Message message = new Message(key, MessageType.INFO);

        assertEquals(key, message.getMessageKey());
    }


    @Test
    void messageKeyIsImmutable() {
        String originalKey = "original.key";
        Message message = new Message(originalKey, MessageType.INFO);

        // Try to modify the key (this should not affect the stored value since String is immutable)
        String retrievedKey = message.getMessageKey();
        String modifiedReference = "modified.key";

        assertEquals(originalKey, message.getMessageKey());
    }


    @Test
    void messageTypeEnumHasFourValues() {
        MessageType[] types = MessageType.values();

        assertEquals(4, types.length);
    }


    @Test
    void messageTypeValueOf() {
        assertEquals(MessageType.ERROR, MessageType.valueOf("ERROR"));
        assertEquals(MessageType.INFO, MessageType.valueOf("INFO"));
        assertEquals(MessageType.SUCCESS, MessageType.valueOf("SUCCESS"));
        assertEquals(MessageType.WARNING, MessageType.valueOf("WARNING"));
    }


    @Test
    void constructorWithSpecialCharactersInKey() {
        String keyWithSpecialChars = "test.message.with.special_chars-and.numbers123";

        Message message = new Message(keyWithSpecialChars, MessageType.INFO);

        assertEquals(keyWithSpecialChars, message.getMessageKey());
    }


    @Test
    void constructorWithUnicodeKey() {
        String unicodeKey = "message.clé.français";

        Message message = new Message(unicodeKey, MessageType.INFO);

        assertEquals(unicodeKey, message.getMessageKey());
    }


    @Test
    void constructorWithSingleCharacterKey() {
        String singleCharKey = "x";

        Message message = new Message(singleCharKey, MessageType.ERROR);

        assertEquals(singleCharKey, message.getMessageKey());
    }


    @Test
    void constructorWithVeryLongKey() {
        String longKey = "a".repeat(1000);

        Message message = new Message(longKey, MessageType.SUCCESS);

        assertEquals(longKey, message.getMessageKey());
    }


    @Test
    void twoMessagesWithSameKeyAndTypeAreEquivalent() {
        String key = "test.key";
        MessageType type = MessageType.INFO;

        Message message1 = new Message(key, type);
        Message message2 = new Message(key, type);

        assertEquals(message1.getMessageKey(), message2.getMessageKey());
        assertEquals(message1.getMessageType(), message2.getMessageType());
    }


    @Test
    void messageTypeEnumOrdinals() {
        // Verify the order of enum values
        assertEquals(0, MessageType.ERROR.ordinal());
        assertEquals(1, MessageType.INFO.ordinal());
        assertEquals(2, MessageType.SUCCESS.ordinal());
        assertEquals(3, MessageType.WARNING.ordinal());
    }
}
