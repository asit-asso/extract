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
package ch.asit_asso.extract.plugins.reject;

import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RejectResult
 */
class RejectResultTest {

    @Mock
    private ITaskProcessorRequest mockRequest;

    private RejectResult result;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        result = new RejectResult();
    }

    @Test
    @DisplayName("New result has null default values")
    void testNewResultHasNullDefaults() {
        RejectResult newResult = new RejectResult();
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
        result.setErrorCode("REJECT_ERROR_001");
        assertEquals("REJECT_ERROR_001", result.getErrorCode());

        result.setErrorCode(null);
        assertNull(result.getErrorCode());

        result.setErrorCode("");
        assertEquals("", result.getErrorCode());
    }

    @Test
    @DisplayName("setMessage and getMessage work correctly")
    void testSetAndGetMessage() {
        result.setMessage("Request rejected successfully");
        assertEquals("Request rejected successfully", result.getMessage());

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
        result.setErrorCode("");
        result.setMessage("Rejection completed");

        String str = result.toString();

        assertNotNull(str);
        assertTrue(str.contains("SUCCESS"));
        assertTrue(str.contains("Rejection completed"));
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
            longMessage.append("This is a very long rejection message. ");
        }
        result.setMessage(longMessage.toString());
        assertEquals(longMessage.toString(), result.getMessage());
    }

    @Test
    @DisplayName("Special characters in message work correctly")
    void testSpecialCharactersInMessage() {
        String specialMessage = "Rejection: äöü éèà ñç 漢字 <>&\"'";
        result.setMessage(specialMessage);
        assertEquals(specialMessage, result.getMessage());
    }

    @Test
    @DisplayName("Result implements ITaskProcessorResult interface")
    void testImplementsInterface() {
        assertTrue(result instanceof ITaskProcessorResult);
    }

    @Test
    @DisplayName("Successful rejection pattern")
    void testSuccessfulRejectionPattern() {
        result.setStatus(ITaskProcessorResult.Status.SUCCESS);
        result.setErrorCode("");
        result.setMessage("Request has been rejected");
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
        result.setErrorCode("-1");
        result.setMessage("Rejection failed: no remark provided");
        result.setRequestData(mockRequest);

        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertEquals("-1", result.getErrorCode());
        assertNotNull(result.getMessage());
        assertSame(mockRequest, result.getRequestData());
    }
}
