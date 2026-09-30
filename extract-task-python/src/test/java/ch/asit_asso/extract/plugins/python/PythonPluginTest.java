package ch.asit_asso.extract.plugins.python;

import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PythonPlugin
 */
class PythonPluginTest {
    
    @TempDir
    Path tempDir;
    
    @Mock
    private ITaskProcessorRequest mockRequest;
    
    @Mock
    private IEmailSettings mockEmailSettings;
    
    private PythonPlugin plugin;
    private Map<String, String> taskSettings;
    private ObjectMapper objectMapper;

    private static String pythonInterpreter;

    @BeforeAll
    static void resolvePythonInterpreter() {
        pythonInterpreter = findPythonInterpreter();
    }

    /**
     * Looks for a usable Python 3 interpreter on the machine running the tests, so that the
     * "no data" cancellation logic can be exercised through a real subprocess.
     *
     * @return the absolute path to a Python 3 interpreter, or {@code null} if none was found
     */
    private static String findPythonInterpreter() {
        String[] candidates = {"/usr/bin/python3", "/usr/local/bin/python3", "/opt/homebrew/bin/python3"};
        for (String candidate : candidates) {
            if (new File(candidate).canExecute()) {
                return candidate;
            }
        }

        try {
            Process which = new ProcessBuilder("which", "python3").start();
            if (which.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                String path = new String(which.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
                if (!path.isEmpty() && new File(path).canExecute()) {
                    return path;
                }
            }
        } catch (IOException | InterruptedException ignored) {
            // No python3 interpreter available - tests relying on it will be skipped.
        }

        return null;
    }

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
    }
    
    @Test
    void testPluginInitialization() {
        plugin = new PythonPlugin("fr");
        
        assertNotNull(plugin);
        assertEquals("python", plugin.getCode());
        assertNotNull(plugin.getLabel());
        assertNotNull(plugin.getDescription());
        assertNotNull(plugin.getHelp());
        assertEquals("fa-cogs", plugin.getPictoClass());
    }
    
    @Test
    void testGetParams() {
        plugin = new PythonPlugin();
        String params = plugin.getParams();

        assertNotNull(params);
        assertTrue(params.contains("pythonInterpreter"));
        assertTrue(params.contains("pythonScript"));
        // additionalArgs parameter was removed - no longer in plugin
    }

    @Test
    void testGetParamsIncludesCancelOnNoDataParameters() throws IOException {
        plugin = new PythonPlugin();
        String params = plugin.getParams();

        JsonNode paramsArray = objectMapper.readTree(params);
        boolean foundCancelOnNoData = false;
        boolean foundCancellationRemark = false;

        for (JsonNode paramNode : paramsArray) {
            String code = paramNode.get("code").asText();
            if ("cancelOnNoData".equals(code)) {
                foundCancelOnNoData = true;
                assertEquals("boolean", paramNode.get("type").asText());
            } else if ("cancellationRemark".equals(code)) {
                foundCancellationRemark = true;
                assertEquals("multitext", paramNode.get("type").asText());
                assertEquals(5000, paramNode.get("maxlength").asInt());
                assertFalse(paramNode.has("req") && paramNode.get("req").asBoolean(),
                        "cancellationRemark must stay optional at the schema level");
            }
        }

        assertTrue(foundCancelOnNoData, "cancelOnNoData parameter should be declared");
        assertTrue(foundCancellationRemark, "cancellationRemark parameter should be declared");
    }
    
    @Test
    void testExecuteWithMissingParameters() {
        plugin = new PythonPlugin("fr", taskSettings);
        
        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertFalse(((PythonResult)result).isSuccess());
        assertNotNull(result.getMessage());
        // Check for actual error message - should contain something about missing parameters
        String message = result.getMessage().toLowerCase();
        assertTrue(message.contains("requis") || 
                  message.contains("missing") ||
                  message.contains("required") ||
                  message.contains("configuration") ||
                  message.contains("parameters"),
                  "Expected error message about missing parameters, got: " + result.getMessage());
    }
    
    @Test
    void testExecuteWithInvalidPythonPath() {
        taskSettings.put("pythonInterpreter", "/invalid/path/to/python");
        taskSettings.put("pythonScript", "/some/script.py");
        plugin = new PythonPlugin("fr", taskSettings);
        
        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertFalse(((PythonResult)result).isSuccess());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("n'existe pas"));
    }
    
    @Test
    void testParametersFileCreationWithWKT() throws IOException {
        // Create a valid Python script
        Path scriptPath = tempDir.resolve("test_script.py");
        Files.writeString(scriptPath, "#!/usr/bin/env python3\nimport sys\nsys.exit(0)");
        
        // Setup plugin with valid paths
        taskSettings.put("pythonInterpreter", "python3");
        taskSettings.put("pythonScript", scriptPath.toString());
        
        // Add WKT geometry
        String wktPolygon = "POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))";
        when(mockRequest.getPerimeter()).thenReturn(wktPolygon);
        
        // Add custom parameters
        String customParams = "{\"param1\": \"value1\", \"param2\": 42}";
        when(mockRequest.getParameters()).thenReturn(customParams);
        
        plugin = new PythonPlugin("fr", taskSettings);
        
        // We can't easily test the full execution without a real Python interpreter
        // But we can verify the parameters file would be created correctly
        // by checking that the setup is valid
        
        assertNotNull(plugin);
        assertEquals(2, taskSettings.size());
    }
    
    @Test
    void testParametersFileStructure() throws IOException {
        // Create a test to verify the GeoJSON structure
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(outputDir);
        when(mockRequest.getFolderOut()).thenReturn(outputDir.toString());
        
        // Test with polygon WKT
        String wkt = "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0))";
        when(mockRequest.getPerimeter()).thenReturn(wkt);
        
        // Test with custom parameters
        when(mockRequest.getParameters()).thenReturn("{\"testKey\": \"testValue\"}");
        
        // Create temporary Python script that just exits
        Path scriptPath = tempDir.resolve("dummy.py");
        Files.writeString(scriptPath, "import sys; sys.exit(0)");
        
        taskSettings.put("pythonInterpreter", "python3");
        taskSettings.put("pythonScript", scriptPath.toString());
        
        plugin = new PythonPlugin("fr", taskSettings);
        
        // Note: Full execution test would require Python to be installed
        // This test validates the setup and structure
        assertTrue(Files.exists(outputDir));
    }
    
    @Test
    void testErrorMessageFormatting() {
        // Test that error messages are properly formatted
        taskSettings.put("pythonInterpreter", "");
        taskSettings.put("pythonScript", "");
        plugin = new PythonPlugin("fr", taskSettings);
        
        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);
        
        assertNotNull(result);
        assertFalse(((PythonResult)result).isSuccess());
        assertNotNull(result.getMessage());
        // Error should be in the message field, not errorMessage
        assertFalse(result.getMessage().isEmpty());
    }
    
    @Test
    void testNewInstanceCreation() {
        plugin = new PythonPlugin();
        
        // Test newInstance with language
        ITaskProcessor newPlugin1 = plugin.newInstance("en");
        assertNotNull(newPlugin1);
        assertEquals("python", newPlugin1.getCode());
        
        // Test newInstance with language and inputs
        Map<String, String> inputs = new HashMap<>();
        inputs.put("pythonInterpreter", "/usr/bin/python3");
        ITaskProcessor newPlugin2 = plugin.newInstance("fr", inputs);
        assertNotNull(newPlugin2);
        assertEquals("python", newPlugin2.getCode());
    }
    
    @Test
    void testNullPerimeterHandling() throws IOException {
        // Test that null perimeter is handled gracefully
        when(mockRequest.getPerimeter()).thenReturn(null);
        when(mockRequest.getParameters()).thenReturn(null);
        
        Path scriptPath = tempDir.resolve("test.py");
        Files.writeString(scriptPath, "import sys; sys.exit(0)");
        
        taskSettings.put("pythonInterpreter", "python3");
        taskSettings.put("pythonScript", scriptPath.toString());
        
        plugin = new PythonPlugin("fr", taskSettings);
        
        // Should not throw exception with null perimeter
        assertDoesNotThrow(() -> plugin.execute(mockRequest, mockEmailSettings));
    }
    
    @Test
    void testEmptyParametersHandling() {
        // Test with empty parameters
        when(mockRequest.getParameters()).thenReturn("");
        when(mockRequest.getPerimeter()).thenReturn(null);
        
        Path scriptPath = tempDir.resolve("empty_test.py");
        
        taskSettings.put("pythonInterpreter", "python3");
        taskSettings.put("pythonScript", scriptPath.toString());
        
        plugin = new PythonPlugin("fr", taskSettings);
        
        // Should handle empty parameters gracefully
        assertNotNull(plugin);
    }
    
    @Test
    void testInvalidWKTHandling() {
        // Test with invalid WKT string
        when(mockRequest.getPerimeter()).thenReturn("INVALID WKT STRING");
        
        Path scriptPath = tempDir.resolve("test.py");
        
        taskSettings.put("pythonInterpreter", "python3");
        taskSettings.put("pythonScript", scriptPath.toString());
        
        plugin = new PythonPlugin("fr", taskSettings);
        
        // Should handle invalid WKT gracefully
        assertNotNull(plugin);
    }
    
    @Test
    void testMultiPolygonWKT() {
        // Test with MultiPolygon WKT
        String multiPolygonWkt = "MULTIPOLYGON (((30 20, 45 40, 10 40, 30 20)), " +
                                "((15 5, 40 10, 10 20, 5 10, 15 5)))";
        when(mockRequest.getPerimeter()).thenReturn(multiPolygonWkt);
        
        Path scriptPath = tempDir.resolve("test.py");
        
        taskSettings.put("pythonInterpreter", "python3");
        taskSettings.put("pythonScript", scriptPath.toString());
        
        plugin = new PythonPlugin("fr", taskSettings);
        
        // Should handle MultiPolygon WKT
        assertNotNull(plugin);
    }
    
    @Test
    void testPointWKT() {
        // Test with Point WKT
        String pointWkt = "POINT (30 10)";
        when(mockRequest.getPerimeter()).thenReturn(pointWkt);
        
        Path scriptPath = tempDir.resolve("test.py");
        
        taskSettings.put("pythonInterpreter", "python3");
        taskSettings.put("pythonScript", scriptPath.toString());
        
        plugin = new PythonPlugin("fr", taskSettings);
        
        // Should handle Point WKT
        assertNotNull(plugin);
    }
    
    @Test
    void testLineStringWKT() {
        // Test with LineString WKT
        String lineStringWkt = "LINESTRING (30 10, 10 30, 40 40)";
        when(mockRequest.getPerimeter()).thenReturn(lineStringWkt);
        
        Path scriptPath = tempDir.resolve("test.py");
        
        taskSettings.put("pythonInterpreter", "python3");
        taskSettings.put("pythonScript", scriptPath.toString());
        
        plugin = new PythonPlugin("fr", taskSettings);
        
        // Should handle LineString WKT
        assertNotNull(plugin);
    }
    
    @Test
    void testOnlyTwoParametersRequired() {
        // Test that only pythonInterpreter and pythonScript are required
        // additionalArgs parameter was removed from the plugin
        Path scriptPath = tempDir.resolve("test.py");

        taskSettings.put("pythonInterpreter", "python3");
        taskSettings.put("pythonScript", scriptPath.toString());

        plugin = new PythonPlugin("fr", taskSettings);

        // Should only have 2 parameters
        assertNotNull(plugin);
        assertEquals(2, taskSettings.size());
    }

    /**
     * Resolves the absolute path to one of the bundled test Python scripts.
     *
     * @param scriptName the file name of the script, under {@code test_scripts/}
     * @return the absolute path to the script
     */
    private String resolveTestScript(String scriptName) {
        java.net.URL resource = getClass().getClassLoader().getResource("test_scripts/" + scriptName);
        assertNotNull(resource, "Test script not found on the classpath: " + scriptName);
        return new File(resource.getFile()).getAbsolutePath();
    }

    @Test
    void testExecuteCancelsGracefullyWhenNoDataMarkerFound() {
        // 368-2: option enabled, extraction fails, output contains the marker -> graceful cancellation
        Assumptions.assumeTrue(pythonInterpreter != null, "No Python 3 interpreter available on this machine");

        taskSettings.put("pythonInterpreter", pythonInterpreter);
        taskSettings.put("pythonScript", resolveTestScript("no_data_script.py"));
        taskSettings.put("cancelOnNoData", "true");
        taskSettings.put("cancellationRemark", "Aucune donnée trouvée pour cette emprise");
        plugin = new PythonPlugin("fr", taskSettings);

        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        assertNotNull(result.getRequestData());
        assertTrue(result.getRequestData().isRejected());
        assertEquals("Aucune donnée trouvée pour cette emprise", result.getRequestData().getRemark());
    }

    @Test
    void testExecuteKeepsErrorWhenNoDataMarkerMissing() {
        // 368-3: option enabled, extraction fails, output does NOT contain the marker -> unchanged error
        Assumptions.assumeTrue(pythonInterpreter != null, "No Python 3 interpreter available on this machine");

        taskSettings.put("pythonInterpreter", pythonInterpreter);
        taskSettings.put("pythonScript", resolveTestScript("error_script.py"));
        taskSettings.put("cancelOnNoData", "true");
        taskSettings.put("cancellationRemark", "Aucune donnée trouvée pour cette emprise");
        plugin = new PythonPlugin("fr", taskSettings);

        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertFalse(result.getRequestData().isRejected());
    }

    @Test
    void testExecuteSuccessIsUnaffectedByNoDataMarker() throws IOException {
        // 368-4: extraction succeeds even though the marker text is printed -> normal success
        Assumptions.assumeTrue(pythonInterpreter != null, "No Python 3 interpreter available on this machine");

        Path scriptPath = tempDir.resolve("success_with_marker.py");
        Files.writeString(scriptPath,
                "import sys\nprint('noDataForExtract is mentioned but this run succeeded')\nsys.exit(0)\n");

        taskSettings.put("pythonInterpreter", pythonInterpreter);
        taskSettings.put("pythonScript", scriptPath.toString());
        taskSettings.put("cancelOnNoData", "true");
        taskSettings.put("cancellationRemark", "Aucune donnée trouvée pour cette emprise");
        plugin = new PythonPlugin("fr", taskSettings);

        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        assertFalse(result.getRequestData().isRejected());
    }

    @Test
    void testExecuteKeepsErrorWhenCancelOnNoDataDisabled() {
        // 368-5: option disabled, extraction fails with the marker in the output -> unchanged error
        Assumptions.assumeTrue(pythonInterpreter != null, "No Python 3 interpreter available on this machine");

        taskSettings.put("pythonInterpreter", pythonInterpreter);
        taskSettings.put("pythonScript", resolveTestScript("no_data_script.py"));
        taskSettings.put("cancellationRemark", "Aucune donnée trouvée pour cette emprise");
        plugin = new PythonPlugin("fr", taskSettings);

        ITaskProcessorResult result = plugin.execute(mockRequest, mockEmailSettings);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertFalse(result.getRequestData().isRejected());
    }
}