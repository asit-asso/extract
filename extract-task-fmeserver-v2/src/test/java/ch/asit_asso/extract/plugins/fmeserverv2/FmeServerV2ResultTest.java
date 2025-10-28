package ch.asit_asso.extract.plugins.fmeserverv2;

import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FmeServerV2Result class
 */
class FmeServerV2ResultTest {

    @Mock
    private ITaskProcessorRequest mockRequest;

    private FmeServerV2Result result;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        result = new FmeServerV2Result();
    }

    @Test
    void testConstructor() {
        assertNotNull(result);
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
    }

    @Test
    void testDefaultStatus() {
        // Default status should be ERROR for safety
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
    }

    @Test
    void testSetAndGetMessage() {
        String testMessage = "Test message";

        result.setMessage(testMessage);

        assertEquals(testMessage, result.getMessage());
    }

    @Test
    void testSetMessageWithNull() {
        result.setMessage(null);

        assertNull(result.getMessage());
    }

    @Test
    void testSetMessageWithEmptyString() {
        result.setMessage("");

        assertEquals("", result.getMessage());
    }

    @Test
    void testSetMessageWithWhitespace() {
        result.setMessage("   ");

        assertEquals("   ", result.getMessage());
    }

    @Test
    void testSetAndGetStatus() {
        result.setStatus(ITaskProcessorResult.Status.SUCCESS);

        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
    }

    @Test
    void testSetStatusWithNull() {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            result.setStatus(null);
        });

        assertNotNull(exception);
    }

    @Test
    void testSetStatusUpdatesResultInfo() {
        result.setStatus(ITaskProcessorResult.Status.SUCCESS);

        Map<String, String> resultInfo = result.getResultInfo();
        assertEquals("SUCCESS", resultInfo.get("status"));
    }

    @Test
    void testSetAndGetRequestData() {
        when(mockRequest.getId()).thenReturn(123);
        when(mockRequest.getOrderGuid()).thenReturn("order-guid-456");
        when(mockRequest.getProductGuid()).thenReturn("product-guid-789");

        result.setRequestData(mockRequest);

        assertEquals(mockRequest, result.getRequestData());
    }

    @Test
    void testSetRequestDataWithNull() {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            result.setRequestData(null);
        });

        assertNotNull(exception);
    }

    @Test
    void testSetRequestDataUpdatesResultInfo() {
        when(mockRequest.getId()).thenReturn(123);
        when(mockRequest.getOrderGuid()).thenReturn("order-456");
        when(mockRequest.getProductGuid()).thenReturn("product-789");

        result.setRequestData(mockRequest);

        Map<String, String> resultInfo = result.getResultInfo();
        assertEquals("123", resultInfo.get("requestId"));
        assertEquals("order-456", resultInfo.get("orderGuid"));
        assertEquals("product-789", resultInfo.get("productGuid"));
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
    void testSetErrorCodeUpdatesResultInfo() {
        result.setErrorCode("ERR-002");

        Map<String, String> resultInfo = result.getResultInfo();
        assertEquals("ERR-002", resultInfo.get("errorCode"));
    }

    @Test
    void testSetAndGetErrorDetails() {
        String details = "Detailed error description";

        result.setErrorDetails(details);

        assertEquals(details, result.getErrorDetails());
    }

    @Test
    void testSetErrorDetailsWithNull() {
        result.setErrorDetails(null);

        assertNull(result.getErrorDetails());
    }

    @Test
    void testSetErrorDetailsWithLongString() {
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            longString.append("A");
        }

        result.setErrorDetails(longString.toString());

        assertEquals(longString.toString(), result.getErrorDetails());

        // Result info should have truncated version
        Map<String, String> resultInfo = result.getResultInfo();
        String truncated = resultInfo.get("errorDetails");
        assertNotNull(truncated);
        assertTrue(truncated.length() <= 1000);
        assertTrue(truncated.endsWith("..."));
    }

    @Test
    void testSetError() {
        result.setError("ERR-003", "Error details");

        assertEquals("ERR-003", result.getErrorCode());
        assertEquals("Error details", result.getErrorDetails());
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
    }

    @Test
    void testSetAndGetResultFilePath() {
        String path = "/path/to/result.zip";

        result.setResultFilePath(path);

        assertEquals(path, result.getResultFilePath());
    }

    @Test
    void testSetResultFilePathWithNull() {
        result.setResultFilePath(null);

        assertNull(result.getResultFilePath());
    }

    @Test
    void testSetResultFilePathWithEmptyString() {
        result.setResultFilePath("");

        assertEquals("", result.getResultFilePath());
    }

    @Test
    void testSetResultFilePathUpdatesResultInfo() {
        result.setResultFilePath("/output/result.zip");

        Map<String, String> resultInfo = result.getResultInfo();
        assertEquals("/output/result.zip", resultInfo.get("resultPath"));
    }

    @Test
    void testGetCreatedAt() {
        Instant createdAt = result.getCreatedAt();

        assertNotNull(createdAt);
        assertTrue(createdAt.isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    void testGetUpdatedAt() {
        Instant updatedAt = result.getUpdatedAt();

        assertNotNull(updatedAt);
        assertEquals(result.getCreatedAt(), updatedAt);
    }

    @Test
    void testUpdatedAtChangesAfterSet() throws InterruptedException {
        Instant initialUpdatedAt = result.getUpdatedAt();

        Thread.sleep(10);
        result.setMessage("New message");

        Instant newUpdatedAt = result.getUpdatedAt();
        assertTrue(newUpdatedAt.isAfter(initialUpdatedAt));
    }

    @Test
    void testSetProcessingDuration() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        Thread.sleep(50);

        result.setProcessingDuration(startTime);

        Long duration = result.getProcessingDuration();
        assertNotNull(duration);
        assertTrue(duration >= 50);
        assertTrue(duration < 200);
    }

    @Test
    void testGetProcessingDurationBeforeSet() {
        assertNull(result.getProcessingDuration());
    }

    @Test
    void testSetProcessingDurationUpdatesResultInfo() {
        long startTime = System.currentTimeMillis() - 1000;

        result.setProcessingDuration(startTime);

        Map<String, String> resultInfo = result.getResultInfo();
        assertNotNull(resultInfo.get("processingDurationMs"));
    }

    @Test
    void testAddResultInfo() {
        result.addResultInfo("customKey", "customValue");

        Map<String, String> resultInfo = result.getResultInfo();
        assertEquals("customValue", resultInfo.get("customKey"));
    }

    @Test
    void testAddResultInfoWithNullKey() {
        result.addResultInfo(null, "value");

        Map<String, String> resultInfo = result.getResultInfo();
        assertFalse(resultInfo.containsKey(null));
    }

    @Test
    void testAddResultInfoWithNullValue() {
        result.addResultInfo("key", null);

        Map<String, String> resultInfo = result.getResultInfo();
        assertFalse(resultInfo.containsKey("key"));
    }

    @Test
    void testGetResultInfoIsDefensiveCopy() {
        Map<String, String> resultInfo1 = result.getResultInfo();
        Map<String, String> resultInfo2 = result.getResultInfo();

        assertNotSame(resultInfo1, resultInfo2);
    }

    @Test
    void testGetResultInfoModificationDoesNotAffectInternal() {
        result.addResultInfo("key", "value");

        Map<String, String> resultInfo = result.getResultInfo();
        resultInfo.put("newKey", "newValue");

        Map<String, String> resultInfo2 = result.getResultInfo();
        assertFalse(resultInfo2.containsKey("newKey"));
        assertTrue(resultInfo2.containsKey("key"));
    }

    @Test
    void testIsSuccess() {
        result.setStatus(ITaskProcessorResult.Status.SUCCESS);

        assertTrue(result.isSuccess());
        assertFalse(result.isError());
    }

    @Test
    void testIsError() {
        result.setStatus(ITaskProcessorResult.Status.ERROR);

        assertTrue(result.isError());
        assertFalse(result.isSuccess());
    }

    @Test
    void testGetRequestDataAsStringWithNullRequest() {
        String json = result.getRequestDataAsString();

        assertNull(json);
    }

    @Test
    void testGetRequestDataAsStringWithValidRequest() {
        when(mockRequest.getId()).thenReturn(123);
        when(mockRequest.getOrderGuid()).thenReturn("order-guid");

        result.setRequestData(mockRequest);

        // This may return null if mockRequest is not serializable
        // but should not throw exception
        assertDoesNotThrow(() -> result.getRequestDataAsString());
    }

    @Test
    void testToString() {
        result.setStatus(ITaskProcessorResult.Status.SUCCESS);
        result.setMessage("Test message");
        result.setErrorCode("ERR-001");
        result.setResultFilePath("/path/to/result");

        String toString = result.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("SUCCESS"));
        assertTrue(toString.contains("Test message"));
        assertTrue(toString.contains("ERR-001"));
        assertTrue(toString.contains("/path/to/result"));
    }

    @Test
    void testToStringWithNullValues() {
        String toString = result.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("ERROR")); // Default status
    }

    @Test
    void testMultipleOperationsUpdateTimestamp() throws InterruptedException {
        Instant initial = result.getUpdatedAt();

        Thread.sleep(10);
        result.setMessage("Message 1");
        Instant after1 = result.getUpdatedAt();

        Thread.sleep(10);
        result.setStatus(ITaskProcessorResult.Status.SUCCESS);
        Instant after2 = result.getUpdatedAt();

        assertTrue(after1.isAfter(initial));
        assertTrue(after2.isAfter(after1));
    }

    @Test
    void testCompleteWorkflow() {
        // Simulate a complete workflow
        when(mockRequest.getId()).thenReturn(999);
        when(mockRequest.getOrderGuid()).thenReturn("order-999");
        when(mockRequest.getProductGuid()).thenReturn("product-999");

        long startTime = System.currentTimeMillis();

        result.setRequestData(mockRequest);
        result.setMessage("Processing started");
        result.setStatus(ITaskProcessorResult.Status.SUCCESS);
        result.setResultFilePath("/output/result.zip");
        result.setProcessingDuration(startTime);
        result.addResultInfo("customInfo", "someValue");

        // Verify all fields
        assertEquals(mockRequest, result.getRequestData());
        assertEquals("Processing started", result.getMessage());
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        assertEquals("/output/result.zip", result.getResultFilePath());
        assertNotNull(result.getProcessingDuration());
        assertTrue(result.isSuccess());
        assertFalse(result.isError());

        Map<String, String> resultInfo = result.getResultInfo();
        assertEquals("999", resultInfo.get("requestId"));
        assertEquals("SUCCESS", resultInfo.get("status"));
        assertEquals("someValue", resultInfo.get("customInfo"));
    }
}
