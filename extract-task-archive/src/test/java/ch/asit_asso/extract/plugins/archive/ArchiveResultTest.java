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
package ch.asit_asso.extract.plugins.archive;

import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ArchiveResult class.
 */
@DisplayName("ArchiveResult")
class ArchiveResultTest {

    private ArchiveResult result;

    @BeforeEach
    void setUp() {
        result = new ArchiveResult();
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
            result.setErrorCode("ARCHIVE_FAILED");

            assertEquals("ARCHIVE_FAILED", result.getErrorCode());
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
            result.setMessage("Archiving completed successfully");

            assertEquals("Archiving completed successfully", result.getMessage());
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
            String message = "Emplacement : /archive/données/2024/";
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
            ArchiveRequest request = new ArchiveRequest();
            request.setId(123);

            result.setRequestData(request);

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
            ArchiveRequest request = new ArchiveRequest();
            result.setRequestData(request);
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
            // errorCode and message are null

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
            // These should not throw
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
            result.setMessage("Emplacement : /archive/test/");
            result.setErrorCode(null);

            assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
            assertEquals("Emplacement : /archive/test/", result.getMessage());
            assertNull(result.getErrorCode());
        }

        @Test
        @DisplayName("Error result has correct state")
        void errorResultHasCorrectState() {
            result.setStatus(ITaskProcessorResult.Status.ERROR);
            result.setErrorCode("SOURCE_NOT_FOUND");
            result.setMessage("Le répertoire source n'existe pas");

            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
            assertEquals("SOURCE_NOT_FOUND", result.getErrorCode());
            assertEquals("Le répertoire source n'existe pas", result.getMessage());
        }

        @Test
        @DisplayName("Result with request data maintains consistency")
        void resultWithRequestDataMaintainsConsistency() {
            ArchiveRequest request = new ArchiveRequest();
            request.setId(42);
            request.setOrderLabel("ARCHIVE-ORDER-001");

            result.setStatus(ITaskProcessorResult.Status.SUCCESS);
            result.setMessage("Archiving complete");
            result.setRequestData(request);

            assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
            assertNotNull(result.getRequestData());
            assertEquals(42, result.getRequestData().getId());
        }
    }
}
