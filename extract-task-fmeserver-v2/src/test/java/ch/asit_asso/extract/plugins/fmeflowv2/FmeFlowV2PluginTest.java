package ch.asit_asso.extract.plugins.fmeflowv2;

import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import ch.asit_asso.extract.plugins.fmeserverv2.FmeServerV2Plugin;
import ch.asit_asso.extract.plugins.fmeserverv2.FmeServerV2Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FmeServerV2Plugin (formerly known as FmeFlowV2Plugin)
 */
class FmeFlowV2PluginTest {

    @TempDir
    Path tempDir;

    @Mock
    private ITaskProcessorRequest mockRequest;

    @Mock
    private IEmailSettings mockEmailSettings;

    private FmeServerV2Plugin plugin;
    private Map<String, String> taskSettings;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        taskSettings = new HashMap<>();
        objectMapper = new ObjectMapper();
        
        // Setup default mock behavior
        when(mockRequest.getFolderOut()).thenReturn(tempDir.toString());
        when(mockRequest.getFolderIn()).thenReturn(tempDir.toString());
        when(mockRequest.getId()).thenReturn(123);
        when(mockRequest.getOrderGuid()).thenReturn("order-guid-456");
        when(mockRequest.getOrderLabel()).thenReturn("Test Order");
        when(mockRequest.getClientGuid()).thenReturn("client-guid-789");
        when(mockRequest.getClient()).thenReturn("Test Client");
        when(mockRequest.getOrganismGuid()).thenReturn("org-guid-111");
        when(mockRequest.getOrganism()).thenReturn("Test Organism");
        when(mockRequest.getProductGuid()).thenReturn("product-guid-222");
        when(mockRequest.getProductLabel()).thenReturn("Test Product");
        when(mockRequest.getPerimeter()).thenReturn("POLYGON((6.886727164248283 46.44372031957538, 6.881351862162561 46.44126511019801, 6.886480507180103 46.43919870486726, 6.893221678307809 46.441705238743005, 6.886727164248283 46.44372031957538))");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("extract.ssrf.allowLocalForTesting");
    }
    
    @Test
    void testPluginInitialization() {
        plugin = new FmeServerV2Plugin("fr");

        assertNotNull(plugin);
        assertEquals("FMESERVERV2", plugin.getCode());
        assertNotNull(plugin.getLabel());
        assertNotNull(plugin.getDescription());
        assertNotNull(plugin.getHelp());
        assertEquals("fa-cogs", plugin.getPictoClass());
    }

    @Test
    void testGetParams() {
        plugin = new FmeServerV2Plugin();
        String params = plugin.getParams();
        
        assertNotNull(params);
        assertFalse(params.isEmpty());
        
        // Parse JSON to verify structure
        assertDoesNotThrow(() -> {
            JsonNode paramsJson = objectMapper.readTree(params);
            assertTrue(paramsJson.isArray());
            assertTrue(paramsJson.size() > 0);
            
            // Check for required parameters
            boolean hasServiceUrl = false;
            boolean hasApiToken = false;
            boolean hasCancelOnNoData = false;
            boolean hasCancellationRemark = false;
            
            for (JsonNode param : paramsJson) {
                String code = param.get("code").asText();
                switch (code) {
                    case "serviceURL":
                        hasServiceUrl = true;
                        assertTrue(param.get("req").asBoolean());
                        assertEquals("text", param.get("type").asText());
                        break;
                    case "apiToken":
                        hasApiToken = true;
                        assertTrue(param.get("req").asBoolean());
                        assertEquals("pass", param.get("type").asText());
                        break;
                    case "cancelOnNoData":
                        hasCancelOnNoData = true;
                        assertEquals("boolean", param.get("type").asText());
                        assertFalse(param.has("req"));
                        break;
                    case "cancellationRemark":
                        hasCancellationRemark = true;
                        assertEquals("text", param.get("type").asText());
                        assertEquals(4000, param.get("maxlength").asInt());
                        assertTrue(param.get("req").asBoolean());
                        assertEquals("cancelOnNoData", param.get("dependsOn").asText());
                        break;
                }
            }
            
            assertTrue(hasServiceUrl, "Should have serviceURL parameter");
            assertTrue(hasApiToken, "Should have apiToken parameter");
            assertTrue(hasCancelOnNoData, "Should have cancelOnNoData parameter");
            assertTrue(hasCancellationRemark, "Should have cancellationRemark parameter");
        });
    }
    
    @Test
    void testNewInstanceWithLanguage() {
        plugin = new FmeServerV2Plugin();
        FmeServerV2Plugin newInstance = (FmeServerV2Plugin) plugin.newInstance("en");

        assertNotNull(newInstance);
        assertNotSame(plugin, newInstance);
    }

    @Test
    void testNewInstanceWithLanguageAndInputs() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("serviceURL", "http://example.com/service");
        inputs.put("apiToken", "test-token");

        plugin = new FmeServerV2Plugin();
        FmeServerV2Plugin newInstance = (FmeServerV2Plugin) plugin.newInstance("en", inputs);
        
        assertNotNull(newInstance);
        assertNotSame(plugin, newInstance);
    }
    
    @Test
    void testExecuteWithNoInputs() {
        plugin = new FmeServerV2Plugin("fr", null);
        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertNotNull(result.getMessage());
    }
    
    @Test
    void testExecuteWithEmptyInputs() {
        plugin = new FmeServerV2Plugin("fr", taskSettings);
        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertNotNull(result.getMessage());
    }
    
    @Test
    void testExecuteWithMissingServiceUrl() {
        taskSettings.put("apiToken", "test-token");
        plugin = new FmeServerV2Plugin("fr", taskSettings);
        
        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertNotNull(result.getMessage());
    }
    
    @Test
    void testExecuteWithMissingApiToken() {
        taskSettings.put("serviceURL", "https://valid.example.com/service");
        plugin = new FmeServerV2Plugin("fr", taskSettings);
        
        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertNotNull(result.getMessage());
    }
    
    @Test
    void testExecuteWithInvalidUrl() {
        taskSettings.put("serviceURL", "ftp://invalid.example.com");
        taskSettings.put("apiToken", "test-token");
        plugin = new FmeServerV2Plugin("fr", taskSettings);
        
        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertNotNull(result.getMessage());
    }
    
    @Test
    void testExecuteWithLocalhostUrl() {
        taskSettings.put("serviceURL", "http://localhost:8080/service");
        taskSettings.put("apiToken", "test-token");
        plugin = new FmeServerV2Plugin("fr", taskSettings);
        
        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertNotNull(result.getMessage());
    }
    
    @Test
    void testExecuteWithPrivateNetworkUrl() {
        taskSettings.put("serviceURL", "http://192.168.1.1/service");
        taskSettings.put("apiToken", "test-token");
        plugin = new FmeServerV2Plugin("fr", taskSettings);
        
        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertNotNull(result.getMessage());
    }
    
    @Test
    void testGeoJsonCreation() throws Exception {
        taskSettings.put("serviceURL", "https://valid.example.com/fmedatadownload/repo/workspace.fmw");
        taskSettings.put("apiToken", "test-token");
        
        when(mockRequest.getParameters()).thenReturn("{\"FORMAT\":\"SHP\",\"PROJECTION\":\"EPSG:2056\"}");
        
        plugin = new FmeServerV2Plugin("fr", taskSettings);
        
        // We can't easily test the private method, but we can test that the plugin doesn't crash
        // with valid inputs during parameter setup
        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);
        
        // The result will be ERROR due to network call failure, but it should not be due to JSON creation
        assertNotNull(result);
        assertNotNull(result.getMessage());
    }
    
    @Test
    void testGeoJsonAsRequestBody() {
        taskSettings.put("serviceURL", "https://valid.example.com/service");
        taskSettings.put("apiToken", "test-token");
        // GeoJSON is now sent as request body, not as a parameter
        
        plugin = new FmeServerV2Plugin("fr", taskSettings);
        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        // Should handle GeoJSON as request body correctly
    }
    
    @Test
    void testWithNullPerimeter() {
        taskSettings.put("serviceURL", "https://valid.example.com/service");
        taskSettings.put("apiToken", "test-token");
        
        when(mockRequest.getPerimeter()).thenReturn(null);
        
        plugin = new FmeServerV2Plugin("fr", taskSettings);
        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        // Should handle null perimeter gracefully
    }
    
    @Test
    void testWithEmptyPerimeter() {
        taskSettings.put("serviceURL", "https://valid.example.com/service");
        taskSettings.put("apiToken", "test-token");
        
        when(mockRequest.getPerimeter()).thenReturn("");
        
        plugin = new FmeServerV2Plugin("fr", taskSettings);
        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        // Should handle empty perimeter gracefully
    }
    
    @Test
    void testWithInvalidWKT() {
        taskSettings.put("serviceURL", "https://valid.example.com/service");
        taskSettings.put("apiToken", "test-token");
        
        when(mockRequest.getPerimeter()).thenReturn("INVALID WKT STRING");
        
        plugin = new FmeServerV2Plugin("fr", taskSettings);
        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        // Should handle invalid WKT gracefully - geometry should be null in JSON
    }
    
    @Test
    void testWithNullParameters() {
        taskSettings.put("serviceURL", "https://valid.example.com/service");
        taskSettings.put("apiToken", "test-token");
        
        when(mockRequest.getParameters()).thenReturn(null);
        
        plugin = new FmeServerV2Plugin("fr", taskSettings);
        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        // Should handle null parameters gracefully
    }
    
    @Test
    void testWithInvalidJsonParameters() {
        taskSettings.put("serviceURL", "https://valid.example.com/service");
        taskSettings.put("apiToken", "test-token");
        
        when(mockRequest.getParameters()).thenReturn("invalid json");
        
        plugin = new FmeServerV2Plugin("fr", taskSettings);
        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        // Should handle invalid JSON parameters gracefully
    }

    /**
     * Invokes the private {@code processErrorResponse} method of the plugin via reflection, after
     * constructing a private {@code FmeServerResponse} instance with the given failure details.
     */
    private FmeServerV2Result invokeProcessErrorResponse(FmeServerV2Plugin pluginInstance, boolean success,
            String downloadUrl, String errorMessage, int statusCode, FmeServerV2Result result,
            ITaskProcessorRequest request) throws Exception {
        Class<?> responseClass = Class.forName(
                "ch.asit_asso.extract.plugins.fmeserverv2.FmeServerV2Plugin$FmeServerResponse");
        Constructor<?> responseConstructor = responseClass.getDeclaredConstructor(
                boolean.class, String.class, String.class, int.class);
        responseConstructor.setAccessible(true);
        Object fmeResponse = responseConstructor.newInstance(success, downloadUrl, errorMessage, statusCode);

        Method processErrorResponseMethod = FmeServerV2Plugin.class.getDeclaredMethod("processErrorResponse",
                responseClass, FmeServerV2Result.class, ITaskProcessorRequest.class);
        processErrorResponseMethod.setAccessible(true);
        processErrorResponseMethod.invoke(pluginInstance, fmeResponse, result, request);

        return result;
    }

    @Test
    void testCancelOnNoData_MarkerFound_ReturnsSuccessAndRejected() throws Exception {
        taskSettings.put("serviceURL", "https://valid.example.com/service");
        taskSettings.put("apiToken", "test-token-1234567890");
        taskSettings.put("cancelOnNoData", "true");
        taskSettings.put("cancellationRemark", "Aucune donnee trouvee pour ce perimetre.");

        plugin = new FmeServerV2Plugin("fr", taskSettings);
        FmeServerV2Result result = new FmeServerV2Result();
        result.setRequestData(mockRequest);

        invokeProcessErrorResponse(plugin, false, null,
                "{\"serviceResponse\":{\"statusInfo\":{\"message\":\"noDataForExtract\"}}}", 500, result, mockRequest);

        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        assertTrue(result.getRequestData().isRejected());
        assertEquals("Aucune donnee trouvee pour ce perimetre.", result.getRequestData().getRemark());
    }

    @Test
    void testCancelOnNoData_MarkerNotFound_ReturnsError() throws Exception {
        taskSettings.put("serviceURL", "https://valid.example.com/service");
        taskSettings.put("apiToken", "test-token-1234567890");
        taskSettings.put("cancelOnNoData", "true");
        taskSettings.put("cancellationRemark", "Aucune donnee trouvee pour ce perimetre.");

        plugin = new FmeServerV2Plugin("fr", taskSettings);
        FmeServerV2Result result = new FmeServerV2Result();
        result.setRequestData(mockRequest);

        invokeProcessErrorResponse(plugin, false, null, "Some other transformation error", 500, result, mockRequest);

        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertFalse(result.getRequestData().isRejected());
    }

    @Test
    void testCancelOnNoData_OptionDisabled_MarkerFound_ReturnsError() throws Exception {
        taskSettings.put("serviceURL", "https://valid.example.com/service");
        taskSettings.put("apiToken", "test-token-1234567890");
        // cancelOnNoData intentionally left unset (disabled)
        taskSettings.put("cancellationRemark", "Aucune donnee trouvee pour ce perimetre.");

        plugin = new FmeServerV2Plugin("fr", taskSettings);
        FmeServerV2Result result = new FmeServerV2Result();
        result.setRequestData(mockRequest);

        invokeProcessErrorResponse(plugin, false, null,
                "{\"serviceResponse\":{\"statusInfo\":{\"message\":\"noDataForExtract\"}}}", 500, result, mockRequest);

        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertFalse(result.getRequestData().isRejected());
    }

    @Test
    void testCancelOnNoData_SuccessPathUnaffected() throws Exception {
        System.setProperty("extract.ssrf.allowLocalForTesting", "true");

        byte[] fileContent = "result-file-content".getBytes(StandardCharsets.UTF_8);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        int port = server.getAddress().getPort();

        server.createContext("/service", exchange -> {
            String responseBody = "{\"url\":\"http://127.0.0.1:" + port + "/download\"}";
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        server.createContext("/download", exchange -> {
            exchange.getResponseHeaders().set("Content-Type", "application/zip");
            exchange.sendResponseHeaders(200, fileContent.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileContent);
            }
        });

        server.start();

        try {
            taskSettings.put("serviceURL", "http://127.0.0.1:" + port + "/service");
            taskSettings.put("apiToken", "test-token-1234567890");
            taskSettings.put("cancelOnNoData", "true");
            taskSettings.put("cancellationRemark", "noDataForExtract should be ignored on success");

            plugin = new FmeServerV2Plugin("fr", taskSettings);
            ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);

            assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
            assertNotNull(result.getRequestData());
            assertFalse(result.getRequestData().isRejected());
            assertSame(mockRequest, result.getRequestData());
        } finally {
            server.stop(0);
        }
    }
}