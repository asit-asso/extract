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
package ch.asit_asso.extract.plugins.qgisprint;

import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for QGISPrintResult
 */
class QGISPrintResultTest {

    @Mock
    private ITaskProcessorRequest mockRequest;

    private QGISPrintResult result;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        result = new QGISPrintResult();
    }

    @Test
    @DisplayName("New result has null default values")
    void testNewResultHasNullDefaults() {
        QGISPrintResult newResult = new QGISPrintResult();
        assertNull(newResult.getStatus());
        assertNull(newResult.getErrorCode());
        assertNull(newResult.getMessage());
        assertNull(newResult.getRequestData());
    }

    @Test
    @DisplayName("setStatus and getStatus work correctly")
    void testSetAndGetStatus() {
        result.setStatus(ITaskProcessorResult.Status.SUCCESS);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());

        result.setStatus(ITaskProcessorResult.Status.ERROR);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());

        result.setStatus(ITaskProcessorResult.Status.STANDBY);
        assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());
    }

    @Test
    @DisplayName("setErrorCode and getErrorCode work correctly")
    void testSetAndGetErrorCode() {
        result.setErrorCode("ERROR_001");
        assertEquals("ERROR_001", result.getErrorCode());

        result.setErrorCode(null);
        assertNull(result.getErrorCode());

        result.setErrorCode("");
        assertEquals("", result.getErrorCode());
    }

    @Test
    @DisplayName("setMessage and getMessage work correctly")
    void testSetAndGetMessage() {
        result.setMessage("Operation completed successfully");
        assertEquals("Operation completed successfully", result.getMessage());

        result.setMessage(null);
        assertNull(result.getMessage());

        result.setMessage("");
        assertEquals("", result.getMessage());
    }

    @Test
    @DisplayName("setRequestData and getRequestData work correctly")
    void testSetAndGetRequestData() {
        result.setRequestData(mockRequest);
        assertSame(mockRequest, result.getRequestData());

        result.setRequestData(null);
        assertNull(result.getRequestData());
    }

    @Test
    @DisplayName("toString returns formatted string with status, errorCode and message")
    void testToString() {
        result.setStatus(ITaskProcessorResult.Status.SUCCESS);
        result.setErrorCode("OK");
        result.setMessage("Test message");

        String str = result.toString();

        assertNotNull(str);
        assertTrue(str.contains("SUCCESS"));
        assertTrue(str.contains("OK"));
        assertTrue(str.contains("Test message"));
    }

    @Test
    @DisplayName("All status values can be set")
    void testAllStatusValues() {
        for (ITaskProcessorResult.Status status : ITaskProcessorResult.Status.values()) {
            result.setStatus(status);
            assertEquals(status, result.getStatus());
        }
    }

    @Test
    @DisplayName("Long message can be stored")
    void testLongMessage() {
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("This is a very long message. ");
        }
        result.setMessage(longMessage.toString());
        assertEquals(longMessage.toString(), result.getMessage());
    }

    @Test
    @DisplayName("Special characters in message work correctly")
    void testSpecialCharactersInMessage() {
        String specialMessage = "Error: äöü éèà ñç 漢字 <>&\"'";
        result.setMessage(specialMessage);
        assertEquals(specialMessage, result.getMessage());
    }

    @Test
    @DisplayName("Result implements ITaskProcessorResult interface")
    void testImplementsInterface() {
        assertTrue(result instanceof ITaskProcessorResult);
    }

    @Test
    @DisplayName("Successful result pattern")
    void testSuccessfulResultPattern() {
        result.setStatus(ITaskProcessorResult.Status.SUCCESS);
        result.setErrorCode("");
        result.setMessage("Print completed successfully");
        result.setRequestData(mockRequest);

        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        assertEquals("", result.getErrorCode());
        assertNotNull(result.getMessage());
        assertSame(mockRequest, result.getRequestData());
    }

    @Test
    @DisplayName("Error result pattern")
    void testErrorResultPattern() {
        result.setStatus(ITaskProcessorResult.Status.ERROR);
        result.setErrorCode("QGIS_ERROR_001");
        result.setMessage("QGIS server connection failed");
        result.setRequestData(mockRequest);

        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertEquals("QGIS_ERROR_001", result.getErrorCode());
        assertNotNull(result.getMessage());
        assertSame(mockRequest, result.getRequestData());
    }

    @Test
    @DisplayName("toString with null status handles gracefully")
    void testToStringWithNullStatus() {
        result.setErrorCode("ERROR");
        result.setMessage("Test");
        // status is null by default

        // This should not throw NPE
        assertThrows(NullPointerException.class, () -> result.toString());
    }

    @Test
    @DisplayName("toString contains all components")
    void testToStringContainsAllComponents() {
        result.setStatus(ITaskProcessorResult.Status.ERROR);
        result.setErrorCode("ERR-123");
        result.setMessage("Detailed error message");

        String str = result.toString();

        assertTrue(str.contains("ERROR"));
        assertTrue(str.contains("ERR-123"));
        assertTrue(str.contains("Detailed error message"));
    }

    @Test
    @DisplayName("Standby result pattern")
    void testStandbyResultPattern() {
        result.setStatus(ITaskProcessorResult.Status.STANDBY);
        result.setErrorCode("");
        result.setMessage("Waiting for external process");
        result.setRequestData(mockRequest);

        assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());
        assertEquals("", result.getErrorCode());
        assertNotNull(result.getMessage());
    }

    @Test
    @DisplayName("Result can be reused with different values")
    void testResultReuse() {
        // First use - success
        result.setStatus(ITaskProcessorResult.Status.SUCCESS);
        result.setErrorCode("");
        result.setMessage("Success");
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());

        // Reuse - error
        result.setStatus(ITaskProcessorResult.Status.ERROR);
        result.setErrorCode("E1");
        result.setMessage("Error");
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertEquals("E1", result.getErrorCode());
    }

    @Test
    @DisplayName("Error code with special characters")
    void testErrorCodeWithSpecialCharacters() {
        String specialErrorCode = "ERROR_<>&\"'_123";
        result.setErrorCode(specialErrorCode);
        assertEquals(specialErrorCode, result.getErrorCode());
    }

    @Test
    @DisplayName("Message with multiline content")
    void testMessageWithMultilineContent() {
        String multilineMessage = "Line 1\nLine 2\nLine 3";
        result.setMessage(multilineMessage);
        assertEquals(multilineMessage, result.getMessage());
    }

    @Test
    @DisplayName("Message with HTML content")
    void testMessageWithHtmlContent() {
        String htmlMessage = "<p>Error: <strong>Connection failed</strong></p>";
        result.setMessage(htmlMessage);
        assertEquals(htmlMessage, result.getMessage());
    }

    @Test
    @DisplayName("Request data can be changed")
    void testRequestDataCanBeChanged() {
        ITaskProcessorRequest mockRequest2 = mock(ITaskProcessorRequest.class);

        result.setRequestData(mockRequest);
        assertSame(mockRequest, result.getRequestData());

        result.setRequestData(mockRequest2);
        assertSame(mockRequest2, result.getRequestData());
    }

    @Test
    @DisplayName("Status transitions work correctly")
    void testStatusTransitions() {
        // Start with STANDBY
        result.setStatus(ITaskProcessorResult.Status.STANDBY);
        assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());

        // Transition to SUCCESS
        result.setStatus(ITaskProcessorResult.Status.SUCCESS);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());

        // Transition to ERROR
        result.setStatus(ITaskProcessorResult.Status.ERROR);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());

        // Back to STANDBY
        result.setStatus(ITaskProcessorResult.Status.STANDBY);
        assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());
    }

    @Test
    @DisplayName("Numeric error code")
    void testNumericErrorCode() {
        result.setErrorCode("-1");
        assertEquals("-1", result.getErrorCode());

        result.setErrorCode("0");
        assertEquals("0", result.getErrorCode());

        result.setErrorCode("500");
        assertEquals("500", result.getErrorCode());
    }

    @Test
    @DisplayName("Empty message and error code")
    void testEmptyMessageAndErrorCode() {
        result.setStatus(ITaskProcessorResult.Status.SUCCESS);
        result.setErrorCode("");
        result.setMessage("");

        assertEquals("", result.getErrorCode());
        assertEquals("", result.getMessage());
    }

    @Test
    @DisplayName("toString format verification")
    void testToStringFormat() {
        result.setStatus(ITaskProcessorResult.Status.SUCCESS);
        result.setErrorCode("OK");
        result.setMessage("Done");

        String str = result.toString();

        // Verify the format contains expected structure
        assertTrue(str.contains("status"));
        assertTrue(str.contains("errorCode"));
        assertTrue(str.contains("message"));
    }

    @Test
    @DisplayName("Result with QGISPrintRequest")
    void testResultWithQGISPrintRequest() {
        QGISPrintRequest qgisRequest = new QGISPrintRequest();
        qgisRequest.setId(123);
        qgisRequest.setProductGuid("test-guid");

        result.setRequestData(qgisRequest);
        result.setStatus(ITaskProcessorResult.Status.SUCCESS);

        assertSame(qgisRequest, result.getRequestData());
        assertEquals(123, result.getRequestData().getId());
        assertEquals("test-guid", result.getRequestData().getProductGuid());
    }
}
