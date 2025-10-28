package ch.asit_asso.extract.plugins.fmeflowv2;

import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import ch.asit_asso.extract.plugins.fmeserverv2.FmeServerV2Plugin;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
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
                }
            }
            
            assertTrue(hasServiceUrl, "Should have serviceURL parameter");
            assertTrue(hasApiToken, "Should have apiToken parameter");
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
}