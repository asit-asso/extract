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

import ch.asit_asso.extract.plugins.common.IEmailSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the Email class.
 */
@DisplayName("Email")
@ExtendWith(MockitoExtension.class)
class EmailTest {

    @Mock
    private IEmailSettings mockEmailSettings;

    private Email email;

    @BeforeEach
    void setUp() {
        email = new Email(mockEmailSettings);
    }

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor with valid settings creates instance")
        void constructorWithValidSettingsCreatesInstance() {
            Email testEmail = new Email(mockEmailSettings);
            assertNotNull(testEmail);
        }

        @Test
        @DisplayName("Constructor with null settings throws IllegalArgumentException")
        void constructorWithNullSettingsThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> new Email(null));
        }

        @Test
        @DisplayName("Default content type is TEXT")
        void defaultContentTypeIsText() {
            assertEquals(Email.ContentType.TEXT, email.getContentType());
        }

        @Test
        @DisplayName("Recipients list is initially empty")
        void recipientsListIsInitiallyEmpty() {
            assertEquals(0, email.getRecipients().length);
        }
    }

    @Nested
    @DisplayName("Content tests")
    class ContentTests {

        @Test
        @DisplayName("setContent with valid text sets content")
        void setContentWithValidTextSetsContent() {
            email.setContent("Test email content");
            assertEquals("Test email content", email.getContent());
        }

        @Test
        @DisplayName("setContent with empty string throws IllegalArgumentException")
        void setContentWithEmptyStringThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> email.setContent(""));
        }

        @Test
        @DisplayName("setContent with null throws IllegalArgumentException")
        void setContentWithNullThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> email.setContent(null));
        }

        @Test
        @DisplayName("getContent returns null by default")
        void getContentReturnsNullByDefault() {
            assertNull(email.getContent());
        }

        @Test
        @DisplayName("setContent with HTML content works")
        void setContentWithHtmlContentWorks() {
            String htmlContent = "<html><body><h1>Test</h1></body></html>";
            email.setContent(htmlContent);
            assertEquals(htmlContent, email.getContent());
        }

        @Test
        @DisplayName("setContent with multiline content works")
        void setContentWithMultilineContentWorks() {
            String multilineContent = "Line 1\nLine 2\nLine 3";
            email.setContent(multilineContent);
            assertEquals(multilineContent, email.getContent());
        }

        @Test
        @DisplayName("setContent with special characters works")
        void setContentWithSpecialCharactersWorks() {
            String specialContent = "Test avec accents: e, e, a, u et symboles: @#$%^&*()";
            email.setContent(specialContent);
            assertEquals(specialContent, email.getContent());
        }
    }

    @Nested
    @DisplayName("ContentType tests")
    class ContentTypeTests {

        @Test
        @DisplayName("setContentType to HTML works")
        void setContentTypeToHtmlWorks() {
            email.setContentType(Email.ContentType.HTML);
            assertEquals(Email.ContentType.HTML, email.getContentType());
        }

        @Test
        @DisplayName("setContentType to TEXT works")
        void setContentTypeToTextWorks() {
            email.setContentType(Email.ContentType.TEXT);
            assertEquals(Email.ContentType.TEXT, email.getContentType());
        }

        @Test
        @DisplayName("setContentType with null throws IllegalArgumentException")
        void setContentTypeWithNullThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> email.setContentType(null));
        }

        @Test
        @DisplayName("ContentType enum has HTML and TEXT values")
        void contentTypeEnumHasCorrectValues() {
            assertEquals(2, Email.ContentType.values().length);
            assertNotNull(Email.ContentType.valueOf("HTML"));
            assertNotNull(Email.ContentType.valueOf("TEXT"));
        }
    }

    @Nested
    @DisplayName("Subject tests")
    class SubjectTests {

        @Test
        @DisplayName("setSubject sets subject correctly")
        void setSubjectSetsSubjectCorrectly() {
            email.setSubject("Test Subject");
            assertEquals("Test Subject", email.getSubject());
        }

        @Test
        @DisplayName("getSubject returns null by default")
        void getSubjectReturnsNullByDefault() {
            assertNull(email.getSubject());
        }

        @Test
        @DisplayName("setSubject with null sets null")
        void setSubjectWithNullSetsNull() {
            email.setSubject("Initial");
            email.setSubject(null);
            assertNull(email.getSubject());
        }

        @Test
        @DisplayName("setSubject with empty string works")
        void setSubjectWithEmptyStringWorks() {
            email.setSubject("");
            assertEquals("", email.getSubject());
        }

        @Test
        @DisplayName("setSubject with special characters works")
        void setSubjectWithSpecialCharactersWorks() {
            String subject = "Re: Notification - Action requise!";
            email.setSubject(subject);
            assertEquals(subject, email.getSubject());
        }
    }

    @Nested
    @DisplayName("Recipient tests")
    class RecipientTests {

        @Test
        @DisplayName("addRecipient with valid email adds recipient")
        void addRecipientWithValidEmailAddsRecipient() throws AddressException {
            email.addRecipient("test@example.com");
            assertEquals(1, email.getRecipients().length);
        }

        @Test
        @DisplayName("addRecipient with severely malformed email throws AddressException")
        void addRecipientWithMalformedEmailThrowsException() {
            // InternetAddress is permissive - use a clearly malformed address with spaces
            assertThrows(AddressException.class, () -> email.addRecipient("invalid email with spaces"));
        }

        @Test
        @DisplayName("addRecipient multiple times adds all recipients")
        void addRecipientMultipleTimesAddsAllRecipients() throws AddressException {
            email.addRecipient("user1@example.com");
            email.addRecipient("user2@example.com");
            email.addRecipient("user3@example.com");
            assertEquals(3, email.getRecipients().length);
        }

        @Test
        @DisplayName("getRecipients returns array of InternetAddress")
        void getRecipientsReturnsArrayOfInternetAddress() throws AddressException {
            email.addRecipient("test@example.com");
            InternetAddress[] recipients = email.getRecipients();
            assertNotNull(recipients);
            assertEquals(1, recipients.length);
            assertEquals("test@example.com", recipients[0].getAddress());
        }

        @Test
        @DisplayName("addAllRecipients with valid array adds all recipients")
        void addAllRecipientsWithValidArrayAddsAllRecipients() throws AddressException {
            String[] addresses = {"user1@example.com", "user2@example.com"};
            email.addAllRecipients(addresses);
            assertEquals(2, email.getRecipients().length);
        }

        @Test
        @DisplayName("addAllRecipients with null throws IllegalArgumentException")
        void addAllRecipientsWithNullThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> email.addAllRecipients(null));
        }

        @Test
        @DisplayName("addAllRecipients with empty array throws IllegalArgumentException")
        void addAllRecipientsWithEmptyArrayThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> email.addAllRecipients(new String[]{}));
        }

        @Test
        @DisplayName("addAllRecipients with malformed email throws AddressException")
        void addAllRecipientsWithMalformedEmailThrowsException() {
            // InternetAddress is permissive - use clearly malformed address with spaces
            String[] addresses = {"valid@example.com", "invalid email with spaces"};
            assertThrows(AddressException.class, () -> email.addAllRecipients(addresses));
        }

        @Test
        @DisplayName("addRecipients with valid array returns true")
        void addRecipientsWithValidArrayReturnsTrue() {
            String[] addresses = {"user1@example.com", "user2@example.com"};
            boolean result = email.addRecipients(addresses);
            assertTrue(result);
            assertEquals(2, email.getRecipients().length);
        }

        @Test
        @DisplayName("addRecipients with mixed valid/malformed addresses adds valid ones")
        void addRecipientsWithMixedAddressesAddsValidOnes() {
            // InternetAddress is permissive - only addresses with spaces are rejected
            String[] addresses = {"valid@example.com", "address with spaces", "another@test.com"};
            boolean result = email.addRecipients(addresses);
            assertTrue(result);
            assertEquals(2, email.getRecipients().length);
        }

        @Test
        @DisplayName("addRecipients with all malformed addresses returns false")
        void addRecipientsWithAllMalformedAddressesReturnsFalse() {
            // Only addresses with spaces or clearly malformed syntax are rejected by InternetAddress
            String[] addresses = {"invalid address one", "another bad address"};
            boolean result = email.addRecipients(addresses);
            assertFalse(result);
            assertEquals(0, email.getRecipients().length);
        }
    }

    @Nested
    @DisplayName("Send tests")
    class SendTests {

        @Test
        @DisplayName("send returns false when notifications disabled")
        void sendReturnsFalseWhenNotificationsDisabled() throws AddressException {
            when(mockEmailSettings.isNotificationEnabled()).thenReturn(false);

            email.addRecipient("test@example.com");
            email.setSubject("Test");
            email.setContent("Test content");

            boolean result = email.send();
            assertFalse(result);
        }

        @Test
        @DisplayName("send returns false when email settings invalid")
        void sendReturnsFalseWhenEmailSettingsInvalid() throws AddressException {
            when(mockEmailSettings.isNotificationEnabled()).thenReturn(true);
            when(mockEmailSettings.toSystemProperties()).thenReturn(new Properties());
            when(mockEmailSettings.isValid()).thenReturn(false);

            email.addRecipient("test@example.com");
            email.setSubject("Test");
            email.setContent("Test content");

            boolean result = email.send();
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Integration scenario tests")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("Complete email setup with all properties")
        void completeEmailSetupWithAllProperties() throws AddressException {
            email.setSubject("Important Notification");
            email.setContent("<html><body><p>This is the message</p></body></html>");
            email.setContentType(Email.ContentType.HTML);
            email.addRecipient("recipient@example.com");

            assertEquals("Important Notification", email.getSubject());
            assertEquals("<html><body><p>This is the message</p></body></html>", email.getContent());
            assertEquals(Email.ContentType.HTML, email.getContentType());
            assertEquals(1, email.getRecipients().length);
        }

        @Test
        @DisplayName("Email with multiple recipients in batch")
        void emailWithMultipleRecipientsInBatch() throws AddressException {
            String[] recipients = {
                "user1@domain.com",
                "user2@domain.com",
                "user3@domain.com"
            };

            email.addAllRecipients(recipients);
            email.setSubject("Batch notification");
            email.setContent("Message for all recipients");

            assertEquals(3, email.getRecipients().length);
            assertEquals("Batch notification", email.getSubject());
        }

        @Test
        @DisplayName("Email content type switch from TEXT to HTML")
        void emailContentTypeSwitchFromTextToHtml() {
            assertEquals(Email.ContentType.TEXT, email.getContentType());

            email.setContentType(Email.ContentType.HTML);
            assertEquals(Email.ContentType.HTML, email.getContentType());

            email.setContentType(Email.ContentType.TEXT);
            assertEquals(Email.ContentType.TEXT, email.getContentType());
        }
    }
}
