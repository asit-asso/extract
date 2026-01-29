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
package ch.asit_asso.extract.plugins.validation;

import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationResult
 */
class ValidationResultTest {

    @Mock
    private ITaskProcessorRequest mockRequest;

    private ValidationResult result;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        result = new ValidationResult();
    }

    @Test
    @DisplayName("New result has null default values")
    void testNewResultHasNullDefaults() {
        ValidationResult newResult = new ValidationResult();
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
        result.setErrorCode("VALIDATION_ERROR_001");
        assertEquals("VALIDATION_ERROR_001", result.getErrorCode());

        result.setErrorCode(null);
        assertNull(result.getErrorCode());

        result.setErrorCode("");
        assertEquals("", result.getErrorCode());
    }

    @Test
    @DisplayName("setMessage and getMessage work correctly")
    void testSetAndGetMessage() {
        result.setMessage("Waiting for validation");
        assertEquals("Waiting for validation", result.getMessage());

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
        result.setStatus(ITaskProcessorResult.Status.STANDBY);
        result.setErrorCode(null);
        result.setMessage("Awaiting operator validation");

        String str = result.toString();

        assertNotNull(str);
        assertTrue(str.contains("STANDBY"));
        assertTrue(str.contains("Awaiting operator validation"));
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
            longMessage.append("This is a very long validation message. ");
        }
        result.setMessage(longMessage.toString());
        assertEquals(longMessage.toString(), result.getMessage());
    }

    @Test
    @DisplayName("Special characters in message work correctly")
    void testSpecialCharactersInMessage() {
        String specialMessage = "Validation: äöü éèà ñç 漢字 <>&\"'";
        result.setMessage(specialMessage);
        assertEquals(specialMessage, result.getMessage());
    }

    @Test
    @DisplayName("Result implements ITaskProcessorResult interface")
    void testImplementsInterface() {
        assertTrue(result instanceof ITaskProcessorResult);
    }

    @Test
    @DisplayName("Standby validation pattern")
    void testStandbyValidationPattern() {
        result.setStatus(ITaskProcessorResult.Status.STANDBY);
        result.setErrorCode(null);
        result.setMessage("Request awaiting operator validation");
        result.setRequestData(mockRequest);

        assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());
        assertNull(result.getErrorCode());
        assertNotNull(result.getMessage());
        assertSame(mockRequest, result.getRequestData());
    }

    @Test
    @DisplayName("Error result pattern")
    void testErrorResultPattern() {
        result.setStatus(ITaskProcessorResult.Status.ERROR);
        result.setErrorCode("-1");
        result.setMessage("Validation setup failed");
        result.setRequestData(mockRequest);

        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertEquals("-1", result.getErrorCode());
        assertNotNull(result.getMessage());
        assertSame(mockRequest, result.getRequestData());
    }

    @Test
    @DisplayName("Success result pattern (after operator approval)")
    void testSuccessResultPattern() {
        result.setStatus(ITaskProcessorResult.Status.SUCCESS);
        result.setErrorCode("");
        result.setMessage("Request validated by operator");
        result.setRequestData(mockRequest);

        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        assertEquals("", result.getErrorCode());
        assertNotNull(result.getMessage());
        assertSame(mockRequest, result.getRequestData());
    }
}
