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

import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FmeDesktopResult class.
 */
@DisplayName("FmeDesktopResult")
public class FmeDesktopResultTest {

    private FmeDesktopResult result;

    @Mock
    private ITaskProcessorRequest mockRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        result = new FmeDesktopResult();
    }

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Creates instance with null values by default")
        void createsInstanceWithNullValues() {
            FmeDesktopResult newResult = new FmeDesktopResult();

            assertNull(newResult.getStatus());
            assertNull(newResult.getErrorCode());
            assertNull(newResult.getMessage());
            assertNull(newResult.getRequestData());
        }
    }

    @Nested
    @DisplayName("Status tests")
    class StatusTests {

        @Test
        @DisplayName("Sets and gets SUCCESS status")
        void setsAndGetsSuccessStatus() {
            result.setStatus(ITaskProcessorResult.Status.SUCCESS);

            assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        }

        @Test
        @DisplayName("Sets and gets ERROR status")
        void setsAndGetsErrorStatus() {
            result.setStatus(ITaskProcessorResult.Status.ERROR);

            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        }

        @Test
        @DisplayName("Sets and gets STANDBY status")
        void setsAndGetsStandbyStatus() {
            result.setStatus(ITaskProcessorResult.Status.STANDBY);

            assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());
        }

        @Test
        @DisplayName("Sets and gets NOT_RUN status")
        void setsAndGetsNotRunStatus() {
            result.setStatus(ITaskProcessorResult.Status.NOT_RUN);

            assertEquals(ITaskProcessorResult.Status.NOT_RUN, result.getStatus());
        }

        @Test
        @DisplayName("Can set status to null")
        void canSetStatusToNull() {
            result.setStatus(ITaskProcessorResult.Status.SUCCESS);
            result.setStatus(null);

            assertNull(result.getStatus());
        }
    }

    @Nested
    @DisplayName("ErrorCode tests")
    class ErrorCodeTests {

        @Test
        @DisplayName("Sets and gets error code")
        void setsAndGetsErrorCode() {
            String errorCode = "FME_EXECUTION_ERROR";

            result.setErrorCode(errorCode);

            assertEquals(errorCode, result.getErrorCode());
        }

        @Test
        @DisplayName("Can set error code to null")
        void canSetErrorCodeToNull() {
            result.setErrorCode("SOME_ERROR");
            result.setErrorCode(null);

            assertNull(result.getErrorCode());
        }

        @Test
        @DisplayName("Can set empty error code")
        void canSetEmptyErrorCode() {
            result.setErrorCode("");

            assertEquals("", result.getErrorCode());
        }
    }

    @Nested
    @DisplayName("Message tests")
    class MessageTests {

        @Test
        @DisplayName("Sets and gets message")
        void setsAndGetsMessage() {
            String message = "FME execution completed successfully";

            result.setMessage(message);

            assertEquals(message, result.getMessage());
        }

        @Test
        @DisplayName("Can set message to null")
        void canSetMessageToNull() {
            result.setMessage("Some message");
            result.setMessage(null);

            assertNull(result.getMessage());
        }

        @Test
        @DisplayName("Can set empty message")
        void canSetEmptyMessage() {
            result.setMessage("");

            assertEquals("", result.getMessage());
        }

        @Test
        @DisplayName("Handles long messages")
        void handlesLongMessages() {
            String longMessage = "A".repeat(10000);

            result.setMessage(longMessage);

            assertEquals(longMessage, result.getMessage());
        }

        @Test
        @DisplayName("Handles special characters in message")
        void handlesSpecialCharactersInMessage() {
            String specialMessage = "Erreur: L'exécution a échoué à cause d'un problème avec le fichier <test.gdb>";

            result.setMessage(specialMessage);

            assertEquals(specialMessage, result.getMessage());
        }
    }

    @Nested
    @DisplayName("RequestData tests")
    class RequestDataTests {

        @Test
        @DisplayName("Sets and gets request data")
        void setsAndGetsRequestData() {
            result.setRequestData(mockRequest);

            assertSame(mockRequest, result.getRequestData());
        }

        @Test
        @DisplayName("Can set request data to null")
        void canSetRequestDataToNull() {
            result.setRequestData(mockRequest);
            result.setRequestData(null);

            assertNull(result.getRequestData());
        }

        @Test
        @DisplayName("Request data properties are accessible")
        void requestDataPropertiesAreAccessible() {
            when(mockRequest.getOrderLabel()).thenReturn("ORDER-001");
            when(mockRequest.getProductLabel()).thenReturn("Test Product");

            result.setRequestData(mockRequest);

            assertEquals("ORDER-001", result.getRequestData().getOrderLabel());
            assertEquals("Test Product", result.getRequestData().getProductLabel());
        }
    }

    @Nested
    @DisplayName("toString tests")
    class ToStringTests {

        @Test
        @DisplayName("Returns formatted string with SUCCESS status")
        void returnsFormattedStringWithSuccessStatus() {
            result.setStatus(ITaskProcessorResult.Status.SUCCESS);
            result.setErrorCode(null);
            result.setMessage("OK");

            String toString = result.toString();

            assertNotNull(toString);
            assertTrue(toString.contains("SUCCESS"));
            assertTrue(toString.contains("OK"));
        }

        @Test
        @DisplayName("Returns formatted string with ERROR status")
        void returnsFormattedStringWithErrorStatus() {
            result.setStatus(ITaskProcessorResult.Status.ERROR);
            result.setErrorCode("FME_ERROR");
            result.setMessage("Execution failed");

            String toString = result.toString();

            assertNotNull(toString);
            assertTrue(toString.contains("ERROR"));
            assertTrue(toString.contains("FME_ERROR"));
            assertTrue(toString.contains("Execution failed"));
        }

        @Test
        @DisplayName("Returns formatted string with null values")
        void returnsFormattedStringWithNullValues() {
            result.setStatus(ITaskProcessorResult.Status.SUCCESS);
            result.setErrorCode(null);
            result.setMessage(null);

            String toString = result.toString();

            assertNotNull(toString);
            assertTrue(toString.contains("null"));
        }

        @Test
        @DisplayName("toString includes all fields")
        void toStringIncludesAllFields() {
            result.setStatus(ITaskProcessorResult.Status.ERROR);
            result.setErrorCode("TEST_ERROR");
            result.setMessage("Test message");

            String toString = result.toString();

            assertTrue(toString.contains("status"));
            assertTrue(toString.contains("errorCode"));
            assertTrue(toString.contains("message"));
        }
    }

    @Nested
    @DisplayName("Complete workflow tests")
    class CompleteWorkflowTests {

        @Test
        @DisplayName("Simulates successful FME execution result")
        void simulatesSuccessfulFmeExecutionResult() {
            when(mockRequest.getOrderLabel()).thenReturn("ORDER-FME-001");

            result.setStatus(ITaskProcessorResult.Status.SUCCESS);
            result.setErrorCode(null);
            result.setMessage("OK");
            result.setRequestData(mockRequest);

            assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
            assertNull(result.getErrorCode());
            assertEquals("OK", result.getMessage());
            assertEquals("ORDER-FME-001", result.getRequestData().getOrderLabel());
        }

        @Test
        @DisplayName("Simulates failed FME execution result")
        void simulatesFailedFmeExecutionResult() {
            when(mockRequest.getOrderLabel()).thenReturn("ORDER-FME-002");

            result.setStatus(ITaskProcessorResult.Status.ERROR);
            result.setErrorCode("FME_SCRIPT_ERROR");
            result.setMessage("Le script FME configuré dans le traitement n'existe pas ou n'est pas accessible.");
            result.setRequestData(mockRequest);

            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
            assertEquals("FME_SCRIPT_ERROR", result.getErrorCode());
            assertTrue(result.getMessage().contains("script FME"));
        }

        @Test
        @DisplayName("Simulates standby result")
        void simulatesStandbyResult() {
            result.setStatus(ITaskProcessorResult.Status.STANDBY);
            result.setMessage("Waiting for FME server availability");
            result.setRequestData(mockRequest);

            assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());
        }

        @Test
        @DisplayName("Simulates empty folder output error")
        void simulatesEmptyFolderOutputError() {
            result.setStatus(ITaskProcessorResult.Status.ERROR);
            result.setErrorCode("EMPTY_OUTPUT");
            result.setMessage("L'extraction FME n'a généré aucun fichier.");
            result.setRequestData(mockRequest);

            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
            assertTrue(result.getMessage().contains("aucun fichier"));
        }
    }

    @Nested
    @DisplayName("Interface implementation tests")
    class InterfaceImplementationTests {

        @Test
        @DisplayName("Implements ITaskProcessorResult interface")
        void implementsITaskProcessorResultInterface() {
            assertTrue(result instanceof ITaskProcessorResult);
        }

        @Test
        @DisplayName("All interface methods are implemented")
        void allInterfaceMethodsAreImplemented() {
            assertDoesNotThrow(() -> result.getStatus());
            assertDoesNotThrow(() -> result.getErrorCode());
            assertDoesNotThrow(() -> result.getMessage());
            assertDoesNotThrow(() -> result.getRequestData());
        }
    }

    @Nested
    @DisplayName("Status enum coverage tests")
    class StatusEnumCoverageTests {

        @Test
        @DisplayName("All Status enum values can be set")
        void allStatusEnumValuesCanBeSet() {
            for (ITaskProcessorResult.Status status : ITaskProcessorResult.Status.values()) {
                result.setStatus(status);
                assertEquals(status, result.getStatus());
            }
        }

        @Test
        @DisplayName("Status enum has expected values")
        void statusEnumHasExpectedValues() {
            ITaskProcessorResult.Status[] values = ITaskProcessorResult.Status.values();

            assertTrue(values.length >= 3, "Status should have at least 3 values");
        }
    }

    @Nested
    @DisplayName("Result state combination tests")
    class ResultStateCombinationTests {

        @Test
        @DisplayName("Success result with all fields set")
        void successResultWithAllFieldsSet() {
            when(mockRequest.getId()).thenReturn(123);
            when(mockRequest.getOrderLabel()).thenReturn("ORDER-123");

            result.setStatus(ITaskProcessorResult.Status.SUCCESS);
            result.setErrorCode("");
            result.setMessage("OK");
            result.setRequestData(mockRequest);

            assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
            assertEquals("", result.getErrorCode());
            assertEquals("OK", result.getMessage());
            assertEquals(123, result.getRequestData().getId());
        }

        @Test
        @DisplayName("Error result typical configuration")
        void errorResultTypicalConfiguration() {
            result.setStatus(ITaskProcessorResult.Status.ERROR);
            result.setErrorCode("-1");
            result.setMessage("Le script FME configure dans le traitement n'existe pas");
            result.setRequestData(mockRequest);

            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
            assertEquals("-1", result.getErrorCode());
            assertNotNull(result.getMessage());
            assertNotNull(result.getRequestData());
        }

        @Test
        @DisplayName("NOT_RUN result configuration")
        void notRunResultConfiguration() {
            result.setStatus(ITaskProcessorResult.Status.NOT_RUN);
            result.setErrorCode(null);
            result.setMessage(null);
            result.setRequestData(mockRequest);

            assertEquals(ITaskProcessorResult.Status.NOT_RUN, result.getStatus());
            assertNull(result.getErrorCode());
            assertNull(result.getMessage());
            assertNotNull(result.getRequestData());
        }

        @Test
        @DisplayName("Result can be modified after creation")
        void resultCanBeModifiedAfterCreation() {
            // Initial state
            result.setStatus(ITaskProcessorResult.Status.SUCCESS);
            result.setMessage("Initial");

            // Modify
            result.setStatus(ITaskProcessorResult.Status.ERROR);
            result.setMessage("Modified");

            assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
            assertEquals("Modified", result.getMessage());
        }
    }

    @Nested
    @DisplayName("toString format tests")
    class ToStringFormatTests {

        @Test
        @DisplayName("toString contains status name")
        void toStringContainsStatusName() {
            result.setStatus(ITaskProcessorResult.Status.SUCCESS);
            result.setErrorCode("code");
            result.setMessage("msg");

            String str = result.toString();

            assertTrue(str.contains("SUCCESS"));
        }

        @Test
        @DisplayName("toString format is bracket delimited")
        void toStringFormatIsBracketDelimited() {
            result.setStatus(ITaskProcessorResult.Status.ERROR);
            result.setErrorCode("ERR");
            result.setMessage("Error message");

            String str = result.toString();

            assertTrue(str.startsWith("["));
            assertTrue(str.endsWith("]"));
        }

        @Test
        @DisplayName("toString contains all three fields")
        void toStringContainsAllThreeFields() {
            result.setStatus(ITaskProcessorResult.Status.ERROR);
            result.setErrorCode("TEST_CODE");
            result.setMessage("Test message");

            String str = result.toString();

            assertTrue(str.contains("status"));
            assertTrue(str.contains("errorCode"));
            assertTrue(str.contains("message"));
        }

        @Test
        @DisplayName("toString handles special characters in message")
        void toStringHandlesSpecialCharactersInMessage() {
            result.setStatus(ITaskProcessorResult.Status.ERROR);
            result.setErrorCode("ERR");
            result.setMessage("Error: \"file\" not found at <path>");

            String str = result.toString();

            assertNotNull(str);
            assertTrue(str.contains("Error"));
        }
    }

    @Nested
    @DisplayName("Request data relationship tests")
    class RequestDataRelationshipTests {

        @Test
        @DisplayName("Request data can be FmeDesktopRequest")
        void requestDataCanBeFmeDesktopRequest() {
            FmeDesktopRequest concreteRequest = new FmeDesktopRequest();
            concreteRequest.setId(42);
            concreteRequest.setOrderLabel("ORDER-42");

            result.setRequestData(concreteRequest);

            assertEquals(42, result.getRequestData().getId());
            assertEquals("ORDER-42", result.getRequestData().getOrderLabel());
        }

        @Test
        @DisplayName("Request data interface methods accessible")
        void requestDataInterfaceMethodsAccessible() {
            when(mockRequest.getId()).thenReturn(1);
            when(mockRequest.getOrderGuid()).thenReturn("order-guid");
            when(mockRequest.getProductGuid()).thenReturn("product-guid");
            when(mockRequest.getFolderIn()).thenReturn("/in");
            when(mockRequest.getFolderOut()).thenReturn("/out");

            result.setRequestData(mockRequest);

            assertEquals(1, result.getRequestData().getId());
            assertEquals("order-guid", result.getRequestData().getOrderGuid());
            assertEquals("product-guid", result.getRequestData().getProductGuid());
            assertEquals("/in", result.getRequestData().getFolderIn());
            assertEquals("/out", result.getRequestData().getFolderOut());
        }
    }

    @Nested
    @DisplayName("Error code patterns tests")
    class ErrorCodePatternsTests {

        @Test
        @DisplayName("Numeric error codes work")
        void numericErrorCodesWork() {
            String[] numericCodes = {"-1", "0", "1", "100", "500"};

            for (String code : numericCodes) {
                result.setErrorCode(code);
                assertEquals(code, result.getErrorCode());
            }
        }

        @Test
        @DisplayName("String error codes work")
        void stringErrorCodesWork() {
            String[] stringCodes = {"FME_ERROR", "SCRIPT_NOT_FOUND", "EMPTY_OUTPUT", "LICENSE_ERROR"};

            for (String code : stringCodes) {
                result.setErrorCode(code);
                assertEquals(code, result.getErrorCode());
            }
        }

        @Test
        @DisplayName("Mixed format error codes work")
        void mixedFormatErrorCodesWork() {
            String[] mixedCodes = {"ERR_001", "FME-500", "error.script.missing"};

            for (String code : mixedCodes) {
                result.setErrorCode(code);
                assertEquals(code, result.getErrorCode());
            }
        }
    }

    @Nested
    @DisplayName("Message content tests")
    class MessageContentTests {

        @Test
        @DisplayName("French error messages work")
        void frenchErrorMessagesWork() {
            String frenchMessage = "L'extraction FME n'a genere aucun fichier.";

            result.setMessage(frenchMessage);

            assertEquals(frenchMessage, result.getMessage());
        }

        @Test
        @DisplayName("Messages with line breaks work")
        void messagesWithLineBreaksWork() {
            String multiLineMessage = "Error on line 1\nError on line 2\nError on line 3";

            result.setMessage(multiLineMessage);

            assertEquals(multiLineMessage, result.getMessage());
            assertTrue(result.getMessage().contains("\n"));
        }

        @Test
        @DisplayName("Messages with file paths work")
        void messagesWithFilePathsWork() {
            String pathMessage = "Script not found at: C:\\FME\\scripts\\extract.fmw";

            result.setMessage(pathMessage);

            assertEquals(pathMessage, result.getMessage());
        }
    }
}
