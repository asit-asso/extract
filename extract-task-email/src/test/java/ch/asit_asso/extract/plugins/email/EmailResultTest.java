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

import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the EmailResult class.
 */
@DisplayName("EmailResult")
@ExtendWith(MockitoExtension.class)
class EmailResultTest {

    private EmailResult result;

    @Mock
    private ITaskProcessorRequest mockRequest;

    @BeforeEach
    void setUp() {
        result = new EmailResult();
    }

    @Nested
    @DisplayName("Status tests")
    class StatusTests {

        @Test
        @DisplayName("Status can be set to SUCCESS")
        void statusCanBeSetToSuccess() {
            result.setStatus(ITaskProcessorResult.Status.SUCCESS);

            assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        }

        @Test
        @DisplayName("Status can be set to ERROR")
        void statusCanBeSetToError() {
            result.setStatus(ITaskProcessorResult.Status.ERROR);

            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        }

        @Test
        @DisplayName("Status can be set to STANDBY")
        void statusCanBeSetToStandby() {
            result.setStatus(ITaskProcessorResult.Status.STANDBY);

            assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());
        }

        @Test
        @DisplayName("Status is null by default")
        void statusIsNullByDefault() {
            assertNull(result.getStatus());
        }
    }

    @Nested
    @DisplayName("Error code tests")
    class ErrorCodeTests {

        @Test
        @DisplayName("Error code can be set and retrieved")
        void errorCodeCanBeSetAndRetrieved() {
            result.setErrorCode("EMAIL_FAILED");

            assertEquals("EMAIL_FAILED", result.getErrorCode());
        }

        @Test
        @DisplayName("Error code is null by default")
        void errorCodeIsNullByDefault() {
            assertNull(result.getErrorCode());
        }

        @Test
        @DisplayName("Error code can be set to null")
        void errorCodeCanBeSetToNull() {
            result.setErrorCode("SOME_ERROR");
            result.setErrorCode(null);

            assertNull(result.getErrorCode());
        }

        @Test
        @DisplayName("Error code can be empty string")
        void errorCodeCanBeEmptyString() {
            result.setErrorCode("");

            assertEquals("", result.getErrorCode());
        }
    }

    @Nested
    @DisplayName("Message tests")
    class MessageTests {

        @Test
        @DisplayName("Message can be set and retrieved")
        void messageCanBeSetAndRetrieved() {
            result.setMessage("Email sent successfully");

            assertEquals("Email sent successfully", result.getMessage());
        }

        @Test
        @DisplayName("Message is null by default")
        void messageIsNullByDefault() {
            assertNull(result.getMessage());
        }

        @Test
        @DisplayName("Message can be set to null")
        void messageCanBeSetToNull() {
            result.setMessage("Some message");
            result.setMessage(null);

            assertNull(result.getMessage());
        }

        @Test
        @DisplayName("Message can contain special characters")
        void messageCanContainSpecialCharacters() {
            String message = "L'envoi de la notification a réussi !";
            result.setMessage(message);

            assertEquals(message, result.getMessage());
        }

        @Test
        @DisplayName("Message can be multiline")
        void messageCanBeMultiline() {
            String message = "Line 1\nLine 2\nLine 3";
            result.setMessage(message);

            assertEquals(message, result.getMessage());
        }
    }

    @Nested
    @DisplayName("Request data tests")
    class RequestDataTests {

        @Test
        @DisplayName("Request data can be set and retrieved")
        void requestDataCanBeSetAndRetrieved() {
            when(mockRequest.getId()).thenReturn(123);

            result.setRequestData(mockRequest);

            assertNotNull(result.getRequestData());
            assertEquals(123, result.getRequestData().getId());
        }

        @Test
        @DisplayName("Request data is null by default")
        void requestDataIsNullByDefault() {
            assertNull(result.getRequestData());
        }

        @Test
        @DisplayName("Request data can be set to null")
        void requestDataCanBeSetToNull() {
            result.setRequestData(mockRequest);
            result.setRequestData(null);

            assertNull(result.getRequestData());
        }
    }

    @Nested
    @DisplayName("toString tests")
    class ToStringTests {

        @Test
        @DisplayName("toString contains status")
        void toStringContainsStatus() {
            result.setStatus(ITaskProcessorResult.Status.SUCCESS);

            String str = result.toString();

            assertTrue(str.contains("SUCCESS"));
        }

        @Test
        @DisplayName("toString contains error code")
        void toStringContainsErrorCode() {
            result.setStatus(ITaskProcessorResult.Status.ERROR);
            result.setErrorCode("TEST_ERROR");

            String str = result.toString();

            assertTrue(str.contains("TEST_ERROR"));
        }

        @Test
        @DisplayName("toString contains message")
        void toStringContainsMessage() {
            result.setStatus(ITaskProcessorResult.Status.ERROR);
            result.setMessage("Test message");

            String str = result.toString();

            assertTrue(str.contains("Test message"));
        }

        @Test
        @DisplayName("toString handles null values")
        void toStringHandlesNullValues() {
            result.setStatus(ITaskProcessorResult.Status.SUCCESS);

            String str = result.toString();

            assertNotNull(str);
            assertTrue(str.contains("SUCCESS"));
        }
    }

    @Nested
    @DisplayName("Interface implementation tests")
    class InterfaceImplementationTests {

        @Test
        @DisplayName("Implements ITaskProcessorResult")
        void implementsITaskProcessorResult() {
            assertTrue(result instanceof ITaskProcessorResult);
        }

        @Test
        @DisplayName("All interface methods are implemented")
        void allInterfaceMethodsAreImplemented() {
            assertDoesNotThrow(() -> result.getErrorCode());
            assertDoesNotThrow(() -> result.getMessage());
            assertDoesNotThrow(() -> result.getStatus());
            assertDoesNotThrow(() -> result.getRequestData());
        }
    }

    @Nested
    @DisplayName("Complete result scenario tests")
    class CompleteResultScenarioTests {

        @Test
        @DisplayName("Success result has correct state")
        void successResultHasCorrectState() {
            result.setStatus(ITaskProcessorResult.Status.SUCCESS);
            result.setMessage("OK");
            result.setErrorCode(null);

            assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
            assertEquals("OK", result.getMessage());
            assertNull(result.getErrorCode());
        }

        @Test
        @DisplayName("Error result for no addressee has correct state")
        void errorResultForNoAddresseeHasCorrectState() {
            result.setStatus(ITaskProcessorResult.Status.ERROR);
            result.setErrorCode("NO_ADDRESSEE");
            result.setMessage("Aucune adresse valide de destinataire n'a été fournie.");

            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
            assertEquals("NO_ADDRESSEE", result.getErrorCode());
            assertTrue(result.getMessage().contains("destinataire"));
        }

        @Test
        @DisplayName("Result with request data maintains consistency")
        void resultWithRequestDataMaintainsConsistency() {
            when(mockRequest.getId()).thenReturn(42);

            result.setStatus(ITaskProcessorResult.Status.SUCCESS);
            result.setMessage("Email sent");
            result.setRequestData(mockRequest);

            assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
            assertNotNull(result.getRequestData());
            assertEquals(42, result.getRequestData().getId());
        }
    }
}
