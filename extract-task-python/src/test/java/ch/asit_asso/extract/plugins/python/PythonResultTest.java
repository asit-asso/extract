package ch.asit_asso.extract.plugins.python;

import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PythonResultTest {

    @Mock
    private ITaskProcessorRequest mockRequest;

    private PythonResult result;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        result = new PythonResult();
    }

    @Test
    void testConstructor() {
        assertNotNull(result);
    }

    @Test
    void testSetAndGetMessage() {
        result.setMessage("Test message");
        assertEquals("Test message", result.getMessage());
    }

    @Test
    void testSetMessageWithNull() {
        result.setMessage(null);
        assertNull(result.getMessage());
    }

    @Test
    void testGetErrorCode() {
        result.setSuccess(false);
        assertNotNull(result.getErrorCode());
    }

    @Test
    void testSetAndGetRequestData() {
        when(mockRequest.getId()).thenReturn(123);
        result.setRequestData(mockRequest);
        assertEquals(mockRequest, result.getRequestData());
    }

    @Test
    void testIsSuccess() {
        result.setSuccess(true);
        assertTrue(result.isSuccess());
    }

    @Test
    void testIsNotSuccess() {
        result.setSuccess(false);
        assertFalse(result.isSuccess());
    }

    @Test
    void testToString() {
        result.setSuccess(true);
        result.setMessage("Test message");

        String toString = result.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("true") || toString.contains("Test message"));
    }

    @Test
    void testGetStatus() {
        result.setSuccess(true);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());

        result.setSuccess(false);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
    }

    @Test
    void testSetAndGetErrorMessage() {
        result.setErrorMessage("Error occurred");
        assertEquals("Error occurred", result.getErrorMessage());
    }

    @Test
    void testSetAndGetResultFilePath() {
        result.setResultFilePath("/path/to/result");
        assertEquals("/path/to/result", result.getResultFilePath());
    }
}
