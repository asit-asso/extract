package ch.asit_asso.extract.plugins.fmedesktopv2;

import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class FmeDesktopV2ResultTest {

    @Mock
    private ITaskProcessorRequest mockRequest;

    private FmeDesktopV2Result result;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        result = new FmeDesktopV2Result();
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
    void testSetAndGetStatus() {
        result.setStatus(Status.SUCCESS);
        assertEquals(Status.SUCCESS, result.getStatus());
    }

    @Test
    void testSetStatusWithError() {
        result.setStatus(Status.ERROR);
        assertEquals(Status.ERROR, result.getStatus());
    }

    @Test
    void testSetAndGetErrorCode() {
        result.setErrorCode("ERR-001");
        assertEquals("ERR-001", result.getErrorCode());
    }

    @Test
    void testSetErrorCodeWithNull() {
        result.setErrorCode(null);
        assertNull(result.getErrorCode());
    }

    @Test
    void testSetAndGetResultFilePath() {
        result.setResultFilePath("/path/to/result");
        assertEquals("/path/to/result", result.getResultFilePath());
    }

    @Test
    void testSetResultFilePathWithNull() {
        result.setResultFilePath(null);
        assertNull(result.getResultFilePath());
    }

    @Test
    void testSetAndGetRequestData() {
        when(mockRequest.getId()).thenReturn(123);
        result.setRequestData(mockRequest);
        assertEquals(mockRequest, result.getRequestData());
    }

    @Test
    void testToString() {
        result.setStatus(Status.SUCCESS);
        result.setErrorCode("ERR-001");
        result.setMessage("Test message");

        String toString = result.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("SUCCESS"));
        assertTrue(toString.contains("ERR-001"));
        assertTrue(toString.contains("Test message"));
    }

    @Test
    void testCompleteWorkflow() {
        when(mockRequest.getId()).thenReturn(999);

        result.setRequestData(mockRequest);
        result.setMessage("Processing started");
        result.setStatus(Status.SUCCESS);
        result.setResultFilePath("/output/result.zip");
        result.setErrorCode("");

        assertEquals(mockRequest, result.getRequestData());
        assertEquals("Processing started", result.getMessage());
        assertEquals(Status.SUCCESS, result.getStatus());
        assertEquals("/output/result.zip", result.getResultFilePath());
        assertEquals("", result.getErrorCode());
    }
}
